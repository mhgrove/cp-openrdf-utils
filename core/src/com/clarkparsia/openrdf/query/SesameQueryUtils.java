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
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.serql.SeRQLParserFactory;

import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.query.parser.sparql.SPARQLParserFactory;

import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.clarkparsia.openrdf.vocabulary.FOAF;
import com.clarkparsia.openrdf.vocabulary.DC;

import com.clarkparsia.openrdf.query.builder.ValueExprFactory;
import com.clarkparsia.openrdf.query.builder.QueryBuilder;

import com.clarkparsia.openrdf.query.sparql.SPARQLQueryRenderer;
import com.clarkparsia.openrdf.query.serql.SeRQLQueryRenderer;
import com.clarkparsia.openrdf.query.util.DescribeVisitor;
import com.clarkparsia.openrdf.query.util.DescribeRewriter;
import com.clarkparsia.openrdf.ExtGraph;

import java.util.HashSet;
import java.util.Collection;

import info.aduna.iteration.CloseableIteration;

/**
 * <p>Collection of utility methods for working with the OpenRdf Sesame Query API.</p>
 *
 * @author Michael Grove
 * @since 0.2
 * @version 0.3.1
 */
public final class SesameQueryUtils {
	/**
	 * the logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SesameQueryUtils.class);

	private SesameQueryUtils() {
	}

	public static Collection<String> getProjection(TupleExpr theExpr) {
		final Collection<String> aVars = new HashSet<String>();

		try {
			theExpr.visit(new QueryModelVisitorBase<Exception>() {
				@Override
				public void meet(final ProjectionElem theProjectionElem) throws Exception {
					super.meet(theProjectionElem);
					aVars.add(theProjectionElem.getTargetName());
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return aVars;
	}

	public static boolean isDescribe(final TupleExpr theExpr) {
		try {
			DescribeVisitor aVisitor = new DescribeVisitor();
			theExpr.visit(aVisitor);
			return aVisitor.isDescribe();
		}
		catch (Exception e) {
			return false;
		}
	}

	public static void rewriteDescribe(final TupleExpr theExpr) {
		try {
			DescribeRewriter aRewriter = new DescribeRewriter();
			theExpr.visit(aRewriter);
			theExpr.visit(new DescribeRewriter.Clean());
		}
		catch (Exception e) {
			// no-op
		}
	}

	/**
	 * Create a Sesame graph from the GraphQueryResult
	 * @param theResult the result of the query
	 * @return the graph built from the result
	 * @throws QueryEvaluationException if there was an error while creating the graph from the query result
	 */
	public static ExtGraph asGraph(GraphQueryResult theResult) throws QueryEvaluationException {
		ExtGraph aGraph = new ExtGraph();

		while (theResult.hasNext()) {
			aGraph.add(theResult.next());
		}

		return aGraph;
	}

	/**
	 * Return a parsed query object from the query string
	 * @param theQuery the query string to parse
	 * @return the parsed query object
	 * @throws MalformedQueryException if the query string is not in a known query format.
	 */
	public static ParsedQuery parse(String theQuery) throws MalformedQueryException {
		try {
			return new SPARQLParserFactory().getParser().parseQuery(theQuery, "http://openrdf.clarkparsia.com");
		}
		catch (MalformedQueryException e) {
			return new SeRQLParserFactory().getParser().parseQuery(theQuery, "http://openrdf.clarkparsia.com");
		}
	}

	/**
	 * Return the query string rendering of the {@link Value}
	 * @param theValue the value to render
	 * @return the value rendered in its query string representation
	 */
	public static String getARQSPARQLQueryString(Value theValue) {
        StringBuffer aBuffer = new StringBuffer();

        if (theValue instanceof URI) {
            URI aURI = (URI) theValue;
            aBuffer.append("<").append(aURI.toString()).append(">");
        }
        else if (theValue instanceof BNode) {
            aBuffer.append("<_:").append(((BNode)theValue).getID()).append(">");
        }
        else if (theValue instanceof Literal) {
            Literal aLit = (Literal)theValue;

            aBuffer.append("\"\"\"").append(escape(aLit.getLabel())).append("\"\"\"").append(aLit.getLanguage() != null ? "@" + aLit.getLanguage() : "");

            if (aLit.getDatatype() != null) {
                aBuffer.append("^^<").append(aLit.getDatatype().toString()).append(">");
            }
        }

        return aBuffer.toString();
	}

	/**
	 * Return the query string rendering of the {@link Value}
	 * @param theValue the value to render
	 * @return the value rendered in its query string representation
	 */
	public static String getSPARQLQueryString(Value theValue) {
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

            aBuffer.append("\"\"\"").append(escape(aLit.getLabel())).append("\"\"\"").append(aLit.getLanguage() != null ? "@" + aLit.getLanguage() : "");

            if (aLit.getDatatype() != null) {
                aBuffer.append("^^<").append(aLit.getDatatype().toString()).append(">");
            }
        }

        return aBuffer.toString();
	}
	
	/**
	 * Return the query string rendering of the {@link Value}
	 * @param theValue the value to render
	 * @return the value rendered in its query string representation
	 */
	public static String getSerqlQueryString(Value theValue) {
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
            SliceMutator aLimitSetter = new SliceMutator(theLimit);
            theQuery.getTupleExpr().visit(aLimitSetter);

            if (!aLimitSetter.limitWasSet()) {
                Slice aSlice = new Slice();

                aSlice.setLimit(theLimit);
                aSlice.setArg(theQuery.getTupleExpr());

                theQuery.setTupleExpr(aSlice);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the value of the limit on the query object to a new value, or specify a limit if one is not specified.
     * @param theQuery the query to alter
     * @param theOffset the new limit
     */
    public static void setOffset(final ParsedQuery theQuery, final int theOffset) {
        try {
            SliceMutator aLimitSetter = new SliceMutator(theOffset);
            theQuery.getTupleExpr().visit(aLimitSetter);

            if (!aLimitSetter.offsetWasSet()) {
                Slice aSlice = new Slice();

                aSlice.setOffset(theOffset);
                aSlice.setArg(theQuery.getTupleExpr());

                theQuery.setTupleExpr(aSlice);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * Close the iteration, ignoring any thrown exception and instead just logging it.
	 * @param theResults the iteration to close
	 */
	public static void closeQuietly(final CloseableIteration<?, ? extends Exception> theResults) {
		try {
			if (theResults != null) {
				theResults.close();
			}
		}
		catch (Exception e) {
			LOGGER.warn("There was an error closing an iteration: " + e.getMessage());
		}
	}

	/**
     * Implementation of a {@link org.openrdf.query.algebra.QueryModelVisitor} which will set the limit or offset of a query
     * object to the provided value.  If there is no slice operator specified, {@link #limitWasSet} and {@link #offsetWasSet} will return false.
     */
    private static class SliceMutator extends QueryModelVisitorBase<Exception> {
        /**
         * Whether or not the limit was set on the query object
         */
        private boolean mLimitWasSet = false;

		/**
		 * Whether or not the offset was set on the query object
		 */
		private boolean mOffsetWasSet = false;

        /**
         * The new limit for the query
         */
        private int mNewLimit;

		/**
		 * The new offset for the query
		 */
		private int mNewOffset;

        /**
         * Create a new SetLimit object
         * @param theNewLimit the new limit to use for the query
         */
        private SliceMutator(final int theNewLimit) {
            mNewLimit = theNewLimit;
        }

		/**
         * Resets the state of this visitor so it can be re-used.
         */
        public void reset() {
            mLimitWasSet = false;
			mOffsetWasSet = false;
        }

        /**
         * Return whether or not the limit was set by this visitor
         * @return true if the limit was set, false otherwse
         */
        public boolean limitWasSet() {
            return mLimitWasSet;
        }

		/**
		 * Retun whether or not the offset was set by this visitor
		 * @return true of the offset was set, false otherwise
		 */
		public boolean offsetWasSet() {
			return mOffsetWasSet;
		}

        /**
         * @inheritDoc
         */
        @Override
        public void meet(Slice theSlice) {
			if (mNewLimit > 0) {
            	mLimitWasSet = true;
            	theSlice.setLimit(mNewLimit);
			}

			if (mNewOffset > 0) {
				mOffsetWasSet = true;
				theSlice.setOffset(mNewOffset);
			}
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

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.addProjectionVar("name", "mbox")
				.group().atom("x", FOAF.ontology().name, "name")
						.atom("x", FOAF.ontology().mbox, "mbox");


		ParsedQuery pq = aBuilder.query();
        optimize(pq);

		System.err.println("---------------------------");
		System.err.println(pq);
		System.err.println("---------------------------");
		System.err.println(new SPARQLParser().parseQuery(aGroupedQuery, "http://example.org"));
		System.err.println(new SPARQLQueryRenderer().render(pq));


		String aGroupedQuery2 = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n" +
							   "SELECT distinct ?name ?mbox ?fn ?ln\n" +
							   "WHERE  { { ?x foaf:name ?name . }\n" +
							   "         { ?x foaf:mbox ?mbox . }\n" +
							   "         OPTIONAL { ?x foaf:firstName ?fn . ?x foaf:lastName ?ln .}\n" +

							   "       } limit 100";

		aBuilder.reset();

		aBuilder.addProjectionVar("name", "mbox", "fn", "ln")
			.distinct().limit(100)
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
		System.err.println(new SPARQLQueryRenderer().render(pq));

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
		System.err.println(new SPARQLQueryRenderer().render(pq));


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
		System.err.println(new SPARQLQueryRenderer().render(pq));

        QueryBuilder<ParsedGraphQuery> aConstructBuilder = QueryBuilderFactory.construct();

        ParsedGraphQuery gq = aConstructBuilder
                .group().atom("s", "p", "o").closeGroup().query();

        System.err.println("---------------------------");
        System.err.println(gq);
        System.err.println("---------------------------");
        System.err.println(new SPARQLParser().parseQuery("construct {?s ?p ?o} where {?s ?p ?o } ", "http://example.org"));
		System.err.println(new SPARQLQueryRenderer().render(gq));

        aConstructBuilder.reset();

        gq = aConstructBuilder.addProjectionStatement("s", RDF.TYPE, RDFS.RESOURCE)
                .group().atom("s", "p", "o").closeGroup().query();

        System.err.println("---------------------------");
        System.err.println(gq);
        System.err.println("---------------------------");
        System.err.println(new SPARQLParser().parseQuery("construct {?s <"+RDF.TYPE+"> <"+RDFS.RESOURCE+">} where {?s ?p ?o } ", "http://example.org"));
		System.err.println(new SPARQLQueryRenderer().render(gq));

		System.err.println(new SPARQLParser().parseQuery("construct {?s ?p ?o}\n" +
														 "from <http://lurch.hq.nasa.gov/2006/09/26/ldap/210195930>\n" +
														 "where {?s ?p ?o. filter(?s = <http://lurch.hq.nasa.gov/2006/09/26/ldap/210195930>) }", "http://example.org"));

		System.err.println("---------------------------");
		System.err.println("---------------------------");

		String aUnionQuery = "select distinct ?uri ?aLabel\n" +
							 "where {\n" +
							 "{\n" +
							 "?var4 <http://www.clarkparsia.com/baseball/team> ?uri . \n" +
							 "?var4 <http://www.clarkparsia.com/baseball/player> ?goal_base.\n" +
							 "{\n" +
							 "?var4 <http://www.clarkparsia.com/baseball/position> ?var0_1 .\n" +
							 "filter  (?var0_1 = <http://www.clarkparsia.com/baseball/position/FirstBase>).\n" +
							 "}\n" +
							 "union {\n" +
							 "?var4 <http://www.clarkparsia.com/baseball/position> ?var0_0 .\n" +
							 "filter  (?var0_0 = <http://www.clarkparsia.com/baseball/position/ThirdBase>).\n" +
							 "}\n" +
							 "}.  \n" +
							 "OPTIONAL {?uri <http://www.w3.org/2000/01/rdf-schema#label> ?aLabel.  }.}";

		System.err.println(new SPARQLParser().parseQuery(aUnionQuery, "http://example.org"));


		aBuilder.reset();
		aBuilder.addProjectionVar("uri", "aLabel")
				.distinct()
				.group().atom("var4", ValueFactoryImpl.getInstance().createURI("http://www.clarkparsia.com/baseball/team"), "uri")
						.atom("var4", ValueFactoryImpl.getInstance().createURI("http://www.clarkparsia.com/baseball/player"), "goal_base")
						.union().left()
									.atom("var4", ValueFactoryImpl.getInstance().createURI("http://www.clarkparsia.com/baseball/position"), "var0_0")
									.filter().eq("var0_0", ValueFactoryImpl.getInstance().createURI("http://www.clarkparsia.com/baseball/FirstBase")).closeGroup()
								.right()
									.atom("var4", ValueFactoryImpl.getInstance().createURI("http://www.clarkparsia.com/baseball/position"), "var0_1")
									.filter().eq("var0_1", ValueFactoryImpl.getInstance().createURI("http://www.clarkparsia.com/baseball/ThirdBase")).closeGroup()
						.closeUnion()
						.optional().atom("uri", RDFS.LABEL, "aLabel");

		System.err.println(aBuilder.query());
		System.err.println(new SPARQLQueryRenderer().render(aBuilder.query()));

		String aUnionQuery2 = "select distinct ?uri ?aLabel\n" +
							 "where {\n" +
							 "{\n" +
							 "?var4 <http://www.clarkparsia.com/baseball/team> ?uri . \n" +
							 "?var4 <http://www.clarkparsia.com/baseball/player> ?goal_base.\n" +
							 "{\n" +
							 "?var4 <http://www.clarkparsia.com/baseball/position> ?var0_1 .\n" +
							 "filter  (?var0_1 = <http://www.clarkparsia.com/baseball/position/FirstBase>).\n" +
							 "}\n" +
							 "union {\n" +
							 "?var4 <http://www.clarkparsia.com/baseball/position> ?var0_0 .\n" +
							 "filter  (?var0_0 = <http://www.clarkparsia.com/baseball/position/ThirdBase>).\n" +
							 "}\n" +
							 "union {\n" +
							 "?var4 <http://www.clarkparsia.com/baseball/position> ?var0_2 .\n" +
							 "filter  (?var0_2 = <http://www.clarkparsia.com/baseball/position/SecondBase>).\n" +
							 "}\n" +

							 "}.  \n" +
							 "OPTIONAL {?uri <http://www.w3.org/2000/01/rdf-schema#label> ?aLabel.  }.}";

		System.err.println("---------------------------");

		System.err.println(new SPARQLParser().parseQuery(aUnionQuery2, "http://example.org"));

		String q = "select distinct ?uri\n" +
				   "where {\n" +
				   "?var6 <http://www.clarkparsia.com/baseball/battingAverage> ?uri.  ?var5 <http://www.clarkparsia.com/baseball/team> ?var2.  ?var5 <http://www.clarkparsia.com/baseball/player> ?goal_base.  ?goal_base <http://www.clarkparsia.com/baseball/careerBatting> ?var6.   {?var5 <http://www.clarkparsia.com/baseball/position> <http://www.clarkparsia.com/baseball/position/FirstBase>.  }\n" +
				   "union\n" +
				   "{?var5 <http://www.clarkparsia.com/baseball/position> <http://www.clarkparsia.com/baseball/position/ThirdBase>.  }.\n" +
				   "}\n" +
				   "limit 10000";

		System.err.println(new SeRQLQueryRenderer().render(new SPARQLParser().parseQuery(q, "http://example.org")));
System.err.println("---");
		System.err.println(new SPARQLParser().parseQuery("PREFIX foaf:   <http://xmlns.com/foaf/0.1/>\n" +
														 "DESCRIBE ?x ?y <http://example.org/>\n" +
														 "WHERE    {?x foaf:knows ?y}", "http://example.org"));

		ParsedQuery desc = QueryBuilderFactory.describe(new String[] {"x", "y"}, ValueFactoryImpl.getInstance().createURI("http://example.org/")).group().atom("x", FOAF.ontology().knows, "y").closeGroup().query();
		System.err.println(desc);
		System.err.println(new SPARQLQueryRenderer().render(desc));

//		System.err.println(new SPARQLParser().parseQuery("PREFIX foaf:   <http://xmlns.com/foaf/0.1/>\n" +
//														 "CONSTRUCT {?x ?y <http://example.org/>}\n" +
//														 "WHERE    {?x foaf:knows ?y}", "http://example.org"));

		System.err.println("---------------------------");
		String aOrderByQuery = "select distinct ?aLabel ?sim ?uri\n" +
							   "where {\n" +
							   "{<http://www.semanticweb.org/ontologies/WF.owl#Customer_45905> <http://www.semanticweb.org/ontologies/WF.owl#similarIndustryBusinessSalesAndEmployees> ?v0.  ?v0 <http://www.semanticweb.org/ontologies/WF.owl#similarTo> ?uri.  ?v0 <http://www.semanticweb.org/ontologies/WF.owl#similarity> ?sim.  ?uri <http://www.semanticweb.org/ontologies/WF.owl#name> ?aLabel.   filter (?uri != <http://www.semanticweb.org/ontologies/WF.owl#Customer_45905>).}. }\n" +
							   "\n" +
							   "order by desc(?sim)\n" +
							   "limit 10";
		System.err.println(new SPARQLParser().parseQuery(aOrderByQuery, "http://example.org"));
		System.err.println(new SPARQLQueryRenderer().render(new SPARQLParser().parseQuery(aOrderByQuery, "http://example.org")));

//		System.err.println("---------------------------");
//		String q1 = "select DISTINCT predicate from {subj} predicate {value}, [{subj} rdf:type {type}] WHERE isResource(value) and type = NULL";
////		String q2 = "select label(L) from {<urn:subject>} <urn:labelProperty> {L} WHERE isLiteral(L)";
//		String q2 =  "select DISTINCT subjec, predicate, object from " +
//                       "{subjec} <"+RDF.TYPE+"> {<urn:type>} ," +
//                       "{subjec} predicate {object} " +
//                       "WHERE isLiteral(object)";
//
//
//		System.err.println(new SPARQLQueryRenderer().render(new SeRQLParser().parseQuery(q1, "http://example.org")));
//		System.err.println(new SPARQLParser().parseQuery(new SPARQLQueryRenderer().render(new SeRQLParser().parseQuery(q1, "http://example.org")), "http://example.org"));
//
//		System.err.println(new SPARQLQueryRenderer().render(new SeRQLParser().parseQuery(q2, "http://example.org")));
//		System.err.println(new SPARQLParser().parseQuery(new SPARQLQueryRenderer().render(new SeRQLParser().parseQuery(q2, "http://example.org")), "http://example.org"));


		ParsedQuery asdf = new SPARQLParser().parseQuery("describe <http://google.com> <http://foo.bar.baz>", "http://example.org/");
		System.err.println(asdf);
		DescribeVisitor v = new DescribeVisitor();
		v.checkQuery(asdf);
		System.err.println(v.isDescribe());

		v.checkQuery(new SPARQLParser().parseQuery(aOptionalWithFilter, "http://example.org"));
		System.err.println(v.isDescribe());

		v.checkQuery(new SPARQLParser().parseQuery("PREFIX foaf:   <http://xmlns.com/foaf/0.1/>\n" +
														 "DESCRIBE ?x ?y <http://example.org/>\n" +
														 "WHERE    {?x foaf:knows ?y}", "http://example.org"));
		System.err.println(v.isDescribe());

		v.checkQuery(new SPARQLParser().parseQuery("construct {?s <"+RDF.TYPE+"> <"+RDFS.RESOURCE+">} where {?s ?p ?o } ", "http://example.org"));
		System.err.println(v.isDescribe());

				String qq = "ask \n" +
				   "where {\n" +
				   "?var6 <http://www.clarkparsia.com/baseball/battingAverage> ?uri.  ?var5 <http://www.clarkparsia.com/baseball/team> ?var2.  ?var5 <http://www.clarkparsia.com/baseball/player> ?goal_base.  ?goal_base <http://www.clarkparsia.com/baseball/careerBatting> ?var6.   {?var5 <http://www.clarkparsia.com/baseball/position> <http://www.clarkparsia.com/baseball/position/FirstBase>.  }\n" +
				   "union\n" +
				   "{?var5 <http://www.clarkparsia.com/baseball/position> <http://www.clarkparsia.com/baseball/position/ThirdBase>.  }.\n" +
				   "}\n";

		System.err.println(new SPARQLParser().parseQuery(qq, "http://example.org"));

		System.err.println(new SPARQLQueryRenderer().render(new SPARQLParser().parseQuery(qq, "http://example.org")));

		String str = new SPARQLQueryRenderer().render(new SPARQLParser().parseQuery(qq, "http://example.org"));

//		str = "select distinct * where { " + str + " } limit 1";
//
		System.err.println(str);

		new SPARQLParser().parseQuery(str, "http://example.org");
	}
}
