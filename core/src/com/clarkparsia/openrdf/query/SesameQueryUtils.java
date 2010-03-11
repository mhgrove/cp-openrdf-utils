/*
 * Copyright (c) 2009-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.openrdf.query;

import com.clarkparsia.openrdf.query.builder.QueryBuilderFactory;

import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;

import org.openrdf.model.impl.ValueFactoryImpl;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import org.openrdf.query.algebra.Slice;

import org.openrdf.query.algebra.TupleExpr;

import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;

import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.IterativeEvaluationOptimizer;
import org.openrdf.query.algebra.evaluation.impl.OrderLimitOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryModelNormalizer;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;

import org.openrdf.query.algebra.evaluation.util.QueryOptimizerList;

import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import org.openrdf.query.impl.MapBindingSet;

import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;

import org.openrdf.query.parser.sparql.SPARQLParser;

import org.openrdf.query.algebra.Compare;

import com.clarkparsia.openrdf.vocabulary.FOAF;
import com.clarkparsia.openrdf.vocabulary.DC;

import com.clarkparsia.openrdf.query.builder.ValueExprFactory;
import com.clarkparsia.openrdf.query.builder.SelectQueryBuilder;
import com.clarkparsia.openrdf.query.builder.ConstructQueryBuilder;

/**
 * <p>Collection of utility methods for working with the OpenRdf Sesame Query API.</p>
 *
 * @author Michael Grove
 * @since 0.2
 * @version 0.2.1
 */
public class SesameQueryUtils {

	/**
	 * Return the query string rendering of the {@link Value}
	 * @param theValue the value to render
	 * @return the value rendered in its query string representation
	 */
	public static String getQueryString(Value theValue) {
        StringBuffer aBuffer = new StringBuffer();

        if (theValue instanceof URI) {
            URI aURI = (URI) theValue;
            aBuffer.append("<").append(aURI.toString()).append(">");
        }
        else if (theValue instanceof BNode) {
            aBuffer.append("_:").append(((BNode)theValue).getID());
        }
        else if (theValue instanceof Literal) {
            Literal aLit = (Literal)theValue;
            aBuffer.append("\"").append(escape(aLit.getLabel())).append("\"").append(aLit.getLanguage() != null ? "@" + aLit.getLanguage() : "");
            if (aLit.getDatatype() != null) {
                aBuffer.append("^^<").append(aLit.getDatatype().toString()).append(">");
            }
        }

        return aBuffer.toString();
	}

	/**
	 * Properly escape out any " characters in the query string
	 * @param theString the query string to escape quotes in
	 * @return the query string with quotes escaped
	 */
	public static String escape(String theString) {
		theString = theString.replaceAll("\"", "\\\\\"");

		return theString;
	}

    /**
     * Set the value of the limit on the query object to a new value, or specify a limit if one is not specified.
     * @param theQuery the query to alter
     * @param theLimit the new limit
     */
    public static void setLimit(final ParsedQuery theQuery, final int theLimit) {
        try {
            SetLimit aLimitSetter = new SetLimit(theLimit);
            theQuery.getTupleExpr().visit(aLimitSetter);

            if (!aLimitSetter.limitWasSet()) {
                Slice aSlice = new Slice();

                aSlice.setLimit(theLimit);
                aSlice.setArg(theQuery.getTupleExpr());

                theQuery.getTupleExpr().setParentNode(aSlice);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Implementation of a {@link org.openrdf.query.algebra.QueryModelVisitor} which will set the limit of a query
     * object to the provided value.  If there is no limit specified, {@link #limitWasSet} will return false.
     */
    private static class SetLimit extends QueryModelVisitorBase<Exception> {
        /**
         * Whether or not the limit was set on the query object
         */
        private boolean mLimitWasSet = false;

        /**
         * The new limit for the query
         */
        private int mNewLimit;

        /**
         * Create a new SetLimit object
         * @param theNewLimit the new limit to use for the query
         */
        private SetLimit(final int theNewLimit) {
            mNewLimit = theNewLimit;
        }

        /**
         * Resets the state of this visitor so it can be re-used.
         */
        public void reset() {
            mLimitWasSet = false;
        }

        /**
         * Return whether or not the limit was set by this visitor
         * @return true if the limit was set, false otherwse
         */
        public boolean limitWasSet() {
            return mLimitWasSet;
        }

        /**
         * @inheritDoc
         */
        @Override
        public void meet(Slice theSlice) {
            mLimitWasSet = true;
            theSlice.setLimit(mNewLimit);
        }
    }

    /**
     * Apply a set of built-in sesame query optimizers to the given Query object.  This reflects most of the
     * optimizations applied to queries by the standard Sesame MemoryStore.
     * @param theQuery the query to optimize
     * @return the query after optimization
     */
    public static <T extends ParsedQuery> T optimize(T theQuery) {
        QueryOptimizerList aList = new QueryOptimizerList();

        aList.add(new BindingAssigner());
        aList.add(new CompareOptimizer());
		// we're not currently using these since they don't seem to make sense from a client's perspective.  for example,
		// they split OR value expressions into unioned queries.  this might make sense for the eval of the sesame query
		// algebra internally, but the point of this method is to apply general purpose optimizations that make sense
		// for everyone.  i dont think unioned queries is a way to achieve that.  so we'll disable them here and
		// use the other "standard" optimizers.
//        aList.add(new ConjunctiveConstraintSplitter());
//        aList.add(new DisjunctiveConstraintOptimizer());
        aList.add(new SameTermFilterOptimizer());
        aList.add(new QueryModelNormalizer());
        aList.add(new IterativeEvaluationOptimizer());
        aList.add(new FilterOptimizer());
        aList.add(new OrderLimitOptimizer());

        TupleExpr aExpr = theQuery.getTupleExpr().clone();

        aList.optimize(aExpr, theQuery.getDataset(), new MapBindingSet());

        theQuery.setTupleExpr(aExpr);

        return theQuery;
    }

	public static void main(String[] args) throws Exception {

		String aGroupedQuery = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n" +
							   "SELECT ?name ?mbox\n" +
							   "WHERE  { { ?x foaf:name ?name . }\n" +
							   "         { ?x foaf:mbox ?mbox . }\n" +
							   "       }";

		SelectQueryBuilder aBuilder = QueryBuilderFactory.select();

		aBuilder.addProjectionVar("name", "mbox")
				.group().atom("x", FOAF.ontology().name, "name")
						.atom("x", FOAF.ontology().mbox, "mbox");


		ParsedQuery pq = aBuilder.query();
        optimize(pq);

		System.err.println("---------------------------");
		System.err.println(pq);
		System.err.println("---------------------------");
		System.err.println(new SPARQLParser().parseQuery(aGroupedQuery, "http://example.org"));


		String aGroupedQuery2 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n" +
							   "SELECT distinct ?name ?mbox ?fn ?ln\n" +
							   "WHERE  { { ?x foaf:name ?name . }\n" +
							   "         { ?x foaf:mbox ?mbox . }\n" +
							   "         OPTIONAL { ?x foaf:firstName ?fn . ?x foaf:lastName ?ln .}\n" +

							   "       } limit 100";

		aBuilder.reset();

		aBuilder.distinct().limit(100)
			.addProjectionVar("name", "mbox", "fn", "ln")
			.group().atom("x", FOAF.ontology().name, "name")
					.atom("x", FOAF.ontology().mbox, "mbox")
					.optional()
						.atom("x",FOAF.ontology().firstName,"fn")
						.atom("x",FOAF.ontology().surname,"ln");

		pq = aBuilder.query();
        optimize(pq);

		System.err.println("---------------------------");
		System.err.println(pq);
		System.err.println("---------------------------");
		System.err.println(new SPARQLParser().parseQuery(aGroupedQuery2, "http://example.org"));

		String aOptionalWithFilter = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
									 "PREFIX  ns:  <http://example.org/ns#>\n" +
									 "SELECT  ?title ?price\n" +
									 "WHERE   { ?x dc:title ?title .\n" +
									 "			?x dc:subject ?subj .\n" +
									 "			FILTER (?price < 300).\n"+
									 "			FILTER (?price > 0 && ?price != 10).\n"+
									 "          OPTIONAL { ?x ns:price ?price . ?x ns:discount ?d. FILTER (?price < 30). FILTER (bound(?d)) }\n" +
									 "        }";

		aBuilder.reset();

		aBuilder.addProjectionVar("title", "price")
				.group()
					.atom("x",DC.ontology().title,"title")
					.atom("x", DC.ontology().subject,"subject")
					.filter("price", Compare.CompareOp.LT, ValueFactoryImpl.getInstance().createLiteral(300))
					.filter().and(ValueExprFactory.gt("price", ValueFactoryImpl.getInstance().createLiteral(0)),
								  ValueExprFactory.ne("price", ValueFactoryImpl.getInstance().createLiteral(10)))
					.optional()
						.atom("x", ValueFactoryImpl.getInstance().createURI("http://example.org/ns#price"), "price")
						.atom("x", ValueFactoryImpl.getInstance().createURI("http://example.org/ns#discount"), "d")
						.filter("price", Compare.CompareOp.LT, ValueFactoryImpl.getInstance().createLiteral(30))
						.filter().bound("d");

		pq = aBuilder.query();
        optimize(pq);

		System.err.println("---------------------------");
		System.err.println(pq);
		System.err.println("---------------------------");
		System.err.println(new SPARQLParser().parseQuery(aOptionalWithFilter, "http://example.org"));


		aBuilder.reset();

        String aSelectStar = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n" +
                               "SELECT *\n" +
                               "WHERE  { { ?x foaf:name ?name . }\n" +
                               "         { ?x foaf:mbox ?mbox . }\n" +
                               "       }";

        aBuilder.group().atom("x", FOAF.ontology().name, "name")
                        .atom("x", FOAF.ontology().mbox, "mbox");

		pq = aBuilder.query();
        optimize(pq);

		System.err.println("---------------------------");
		System.err.println(pq);
		System.err.println("---------------------------");
		System.err.println(new SPARQLParser().parseQuery(aSelectStar, "http://example.org"));

        ConstructQueryBuilder aConstructBuilder = QueryBuilderFactory.construct();

        ParsedGraphQuery gq = aConstructBuilder
                .group().atom("s", "p", "o").closeGroup().query();

        System.err.println("---------------------------");
        System.err.println(gq);
        System.err.println("---------------------------");
        System.err.println(new SPARQLParser().parseQuery("construct {?s ?p ?o} where {?s ?p ?o } ", "http://example.org"));

        aConstructBuilder.reset();

        gq = aConstructBuilder.addProjectionStatement("s", RDF.TYPE, RDFS.RESOURCE)
                .group().atom("s", "p", "o").closeGroup().query();

        System.err.println("---------------------------");
        System.err.println(gq);
        System.err.println("---------------------------");
        System.err.println(new SPARQLParser().parseQuery("construct {?s <"+RDF.TYPE+"> <"+RDFS.RESOURCE+">} where {?s ?p ?o } ", "http://example.org"));

		System.err.println(new SPARQLParser().parseQuery("construct {?s ?p ?o}\n" +
														 "from <http://lurch.hq.nasa.gov/2006/09/26/ldap/210195930>\n" +
														 "where {?s ?p ?o. filter(?s = <http://lurch.hq.nasa.gov/2006/09/26/ldap/210195930>) }", "http://example.org"));
	}
}
