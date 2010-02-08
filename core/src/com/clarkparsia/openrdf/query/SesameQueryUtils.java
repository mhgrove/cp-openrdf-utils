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

import com.clarkparsia.openrdf.query.builder.QueryBuilder;
import com.clarkparsia.openrdf.query.builder.QueryBuilderFactory;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.query.algebra.Compare;

import com.clarkparsia.openrdf.vocabulary.FOAF;
import com.clarkparsia.openrdf.vocabulary.DC;
import com.clarkparsia.openrdf.query.builder.ValueExprFactory;

/**
 * <p>Collection of utility methods for working with the OpenRdf Sesame Query API.</p>
 *
 * @author Michael Grove
 */
public class SesameQueryUtils {

	/**
	 * Return the query string rendering of the {@link Value}
	 * @param theValue the value to render
	 * @return the value rendered in its query string representation
	 */
	public static String getQueryString(Value theValue) {
		String aStr = theValue.toString();

		if (theValue instanceof URI)
			aStr = "<"+theValue.toString()+">";
		else if (theValue instanceof BNode)
			aStr = "_:"+((BNode)theValue).getID();
		else if (theValue instanceof Literal) {
			Literal aLit = (Literal)theValue;
			aStr = "\"" + escape(aLit.getLabel()) + "\"" + (aLit.getLanguage() != null ? "@"+aLit.getLanguage() : "") ;
			if (aLit.getDatatype() != null)
				aStr += "^^<"+aLit.getDatatype().toString()+">";
		}

		return aStr;
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

    public static void setLimit(final ParsedQuery theQuery, final int theLimit) {
        try {
            SetLimit aLimitSetter = new SetLimit(theLimit);
            theQuery.getTupleExpr().visit(aLimitSetter);
            if (!aLimitSetter.mLimitWasSet) {
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

    private static class SetLimit extends QueryModelVisitorBase<Exception> {
        boolean mLimitWasSet = false;
        int mNewLimit;

        private SetLimit(final int theNewLimit) {
            mNewLimit = theNewLimit;
        }

        public void meet(Slice theSlice) {
            mLimitWasSet = true;
            theSlice.setLimit(mNewLimit);
        }
    }

	public static void main(String[] args) throws Exception {

		String aGroupedQuery = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n" +
							   "SELECT ?name ?mbox\n" +
							   "WHERE  { { ?x foaf:name ?name . }\n" +
							   "         { ?x foaf:mbox ?mbox . }\n" +
							   "       }";

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.addProjectionVar("name", "mbox")
				.group().atom("x", FOAF.ontology().name, "name")
						.atom("x", FOAF.ontology().mbox, "mbox");


		ParsedQuery pq = aBuilder.query();

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

		System.err.println("---------------------------");
		System.err.println(pq);
		System.err.println("---------------------------");
		System.err.println(new SPARQLParser().parseQuery(aSelectStar, "http://example.org"));

        QueryBuilder<ParsedGraphQuery> aConstructBuilder = QueryBuilderFactory.construct();

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
	}
}
