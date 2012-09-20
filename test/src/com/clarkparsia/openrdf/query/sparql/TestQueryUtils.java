/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.clarkparsia.openrdf.query.sparql;

import java.util.NoSuchElementException;

import com.clarkparsia.openrdf.TestUtils;
import com.clarkparsia.openrdf.query.BooleanQueryResult;
import com.clarkparsia.openrdf.query.BooleanQueryResultImpl;
import com.clarkparsia.openrdf.query.SesameQueryUtils;
import com.clarkparsia.openrdf.query.builder.QueryBuilder;
import com.clarkparsia.openrdf.query.builder.QueryBuilderFactory;
import com.clarkparsia.openrdf.query.builder.ValueExprFactory;
import com.clarkparsia.openrdf.vocabulary.DC;
import com.clarkparsia.openrdf.vocabulary.FOAF;
import org.junit.Ignore;
import org.junit.Test;

import static com.clarkparsia.openrdf.TestUtils.parse;
import static com.clarkparsia.openrdf.TestUtils.render;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.QueryModelNode;

import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.parser.ParsedTupleQuery;

/**
 * <p></p>
 *
 * @author	Michael Grove
 * @since	0.7
 * @version	0.8
 */
public class TestQueryUtils {
	@Test
	public void testBooleanQueryResult() throws QueryEvaluationException {
		BooleanQueryResult aResult = new BooleanQueryResultImpl(true);

		try {
			aResult.remove();
			fail("Remove should not have succeeded");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}

		assertTrue(aResult.hasNext());

		assertTrue(aResult.next());

		assertFalse(aResult.hasNext());

		try {
			aResult.next();
			fail("Should not be able to call next a second time");
		}
		catch (NoSuchElementException e) {
			// expected
		}

		assertFalse(new BooleanQueryResultImpl(false).next());
	}
	@Test
	public void testBuilderSimple() throws Exception {
		String aGroupedQuery = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n" +
							   "SELECT ?name ?mbox\n" +
							   "WHERE  { { ?x foaf:name ?name . }\n" +
							   "         { ?x foaf:mbox ?mbox . }\n" +
							   "       }";

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder
			.addProjectionVar("name", "mbox")
			.group().atom("x", FOAF.ontology().name, "name")
			.atom("x", FOAF.ontology().mbox, "mbox");

		assertAlgebraEquals(parse(aGroupedQuery), aBuilder.query());
	}

	@Test
	public void testBuilderWithLimitAndOptional() throws Exception {
		String aGroupedQuery = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n" +
								"SELECT distinct ?name ?mbox ?fn ?ln\n" +
								"WHERE  { { ?x foaf:name ?name . }\n" +
								"         { ?x foaf:mbox ?mbox . }\n" +
								"         OPTIONAL { ?x foaf:firstName ?fn . ?x foaf:surname ?ln .}\n" +

								"       } limit 100";

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.addProjectionVar("name", "mbox", "fn", "ln")
			.distinct().limit(100)
			.group()
				.atom("x", FOAF.ontology().name, "name")
				.atom("x", FOAF.ontology().mbox, "mbox")
			.optional()
				.atom("x",FOAF.ontology().firstName,"fn")
				.atom("x",FOAF.ontology().surname,"ln");

		assertAlgebraEquals(parse(aGroupedQuery), aBuilder.query());
	}

	@Test
	@Ignore("We're producing the correct answer, but assertAlgebraEquals is brittle enough that we can't check it")
	public void testBuilderWithFilterAndOptional() throws Exception {
		String aQuery = "PREFIX  dc:  <http://purl.org/dc/elements/1.1/>\n" +
						"PREFIX  ns:  <http://example.org/ns#>\n" +
						"SELECT  ?title ?price\n" +
						"WHERE   { ?x dc:title ?title .\n" +
						"			?x dc:subject ?subject .\n" +
						"			FILTER (?price < 300).\n"+
						"			FILTER (?price > 0 && ?price != 10).\n"+
						"          OPTIONAL { ?x ns:price ?price . ?x ns:discount ?d. FILTER (?price < 30). FILTER (bound(?d)) }\n" +
						"        }";

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.addProjectionVar("title", "price")
			.group()
				.atom("x", DC.ontology().title,"title")
				.atom("x", DC.ontology().subject,"subject")
				.filter("price", Compare.CompareOp.LT, ValueFactoryImpl.getInstance().createLiteral(300))
				.filter().and(ValueExprFactory.gt("price", ValueFactoryImpl.getInstance().createLiteral(0)),
						  ValueExprFactory.ne("price", ValueFactoryImpl.getInstance().createLiteral(10)))
			.optional()
				.atom("x", ValueFactoryImpl.getInstance().createURI("http://example.org/ns#price"), "price")
				.atom("x", ValueFactoryImpl.getInstance().createURI("http://example.org/ns#discount"), "d")
				.filter("price", Compare.CompareOp.LT, ValueFactoryImpl.getInstance().createLiteral(30))
				.filter().bound("d");

		assertAlgebraEquals(parse(aQuery), aBuilder.query());
	}

	@Test
	public void testBuilderSelectStar() throws Exception {
		String aQuery = "PREFIX foaf:    <http://xmlns.com/foaf/0.1/>\n" +
							 "SELECT *\n" +
							 "WHERE  { { ?x foaf:name ?name . }\n" +
							 "         { ?x foaf:mbox ?mbox . }\n" +
							 "       }";

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.group()
			.atom("x", FOAF.ontology().name, "name")
			.atom("x", FOAF.ontology().mbox, "mbox");

		assertAlgebraEquals(parse(aQuery), aBuilder.query());
	}

	@Test
	public void testBuilderWithUnions() throws Exception {
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

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.addProjectionVar("uri", "aLabel")
			.distinct()
			.group()
				.atom("var4", ValueFactoryImpl.getInstance().createURI("http://www.clarkparsia.com/baseball/team"), "uri")
				.atom("var4", ValueFactoryImpl.getInstance().createURI("http://www.clarkparsia.com/baseball/player"), "goal_base")
			.union()
				.left()
					.atom("var4", ValueFactoryImpl.getInstance().createURI("http://www.clarkparsia.com/baseball/position"), "var0_1")
					.filter().eq("var0_1", ValueFactoryImpl.getInstance().createURI("http://www.clarkparsia.com/baseball/position/FirstBase")).closeGroup()
				.right()
					.atom("var4", ValueFactoryImpl.getInstance().createURI("http://www.clarkparsia.com/baseball/position"), "var0_0")
					.filter().eq("var0_0", ValueFactoryImpl.getInstance().createURI("http://www.clarkparsia.com/baseball/position/ThirdBase")).closeGroup()
			.closeUnion()
			.optional()
				.atom("uri", RDFS.LABEL, "aLabel");

		assertAlgebraEquals(parse(aUnionQuery), aBuilder.query());
	}

	@Test
	public void testBuilderSimpleConstruct() throws Exception {
		final String aQuery = "construct {?s ?p ?o} where {?s ?p ?o } ";

		QueryBuilder<ParsedGraphQuery> aConstructBuilder = QueryBuilderFactory.construct();

		ParsedGraphQuery aGraphQuery = aConstructBuilder
			.group().atom("s", "p", "o").closeGroup().query();

		assertAlgebraEquals(parse(aQuery), aGraphQuery);
	}

	@Test
	public void testBuilderConstructConstsInProjection() throws Exception {
		final String aQuery = "construct {?s <"+RDF.TYPE+"> <"+RDFS.RESOURCE+">} where {?s ?p ?o } ";

		QueryBuilder<ParsedGraphQuery> aConstructBuilder = QueryBuilderFactory.construct();

		ParsedGraphQuery aGraphQuery= aConstructBuilder.addProjectionStatement("s", RDF.TYPE, RDFS.RESOURCE)
			.group().atom("s", "p", "o").closeGroup().query();

		assertAlgebraEquals(parse(aQuery), aGraphQuery);
	}

	private void assertAlgebraEquals(final ParsedQuery theTupleExpr, final ParsedQuery theOtherExpr) throws Exception {
		// currently, afaik, equals on TupleExpr does not work like one would hope to make comparisons
		// easy here.  so for now, we'll compare toString representations, they should be the same
		// if the expressions are the same.  That is brittle because the toString of Join(A, B) will
		// be different from Join(B, A) even though, semantically, they're equivalent, but for now,
		// this is the best we can do

		//assertEquals(theTupleExpr.toString(), theOtherExpr.toString());
		assertEquals(render(theTupleExpr), render(theOtherExpr));
	}

	@Test
	public void testIsDescribe() throws Exception {
		assertTrue(SesameQueryUtils.isDescribe(parse("describe <http://google.com> <http://foo.bar.baz>").getTupleExpr()));
		assertTrue(SesameQueryUtils.isDescribe(parse("PREFIX foaf:   <http://xmlns.com/foaf/0.1/>\n" +
													 "DESCRIBE ?x ?y <http://example.org/>\n" +
													 "WHERE    {?x foaf:knows ?y}").getTupleExpr()));
		assertFalse(SesameQueryUtils.isDescribe(parse("select * where { ?s ?p ?o }").getTupleExpr()));
	}

	@Test
	public void testBindingSetUtils() {
		MapBindingSet aBindingSet = new MapBindingSet();

		final Literal aLiteral = ValueFactoryImpl.getInstance().createLiteral("literal");
		final URI aURI = ValueFactoryImpl.getInstance().createURI("urn:s");
		final BNode aBNode = ValueFactoryImpl.getInstance().createBNode();

		aBindingSet.addBinding("lit", aLiteral);
		aBindingSet.addBinding("bnode", aBNode);
		aBindingSet.addBinding("uri", aURI);

		assertEquals(SesameQueryUtils.getLiteral(aBindingSet, "lit"), aLiteral);
		assertEquals(SesameQueryUtils.getBNode(aBindingSet, "bnode"), aBNode);
		assertEquals(SesameQueryUtils.getURI(aBindingSet, "uri"), aURI);

		assertEquals(SesameQueryUtils.getResource(aBindingSet, "bnode"), aBNode);
		assertEquals(SesameQueryUtils.getResource(aBindingSet, "uri"), aURI);

		assertTrue(SesameQueryUtils.getLiteral(aBindingSet, "uri") == null);
		assertTrue(SesameQueryUtils.getLiteral(aBindingSet, "bnode") == null);
		assertTrue(SesameQueryUtils.getResource(aBindingSet, "lit") == null);
		assertTrue(SesameQueryUtils.getURI(aBindingSet, "lit") == null);
		assertTrue(SesameQueryUtils.getBNode(aBindingSet, "lit") == null);
	}

	@Test
	public void testSetLimit() throws Exception {
		String aQuery = "select * where {?s ?p ?o }";

		ParsedQuery aParsedQuery = TestUtils.parse(aQuery);

		assertTrue(!contains(aParsedQuery, Slice.class));

		SesameQueryUtils.setLimit(aParsedQuery, 10);

		assertEquals("Limit should be 10", 10L, getLimit(aParsedQuery));
	}

	@Test
	public void testSetOffset() throws Exception {
		String aQuery = "select * where {?s ?p ?o }";

		ParsedQuery aParsedQuery = TestUtils.parse(aQuery);

		assertTrue(!contains(aParsedQuery, Slice.class));

		SesameQueryUtils.setOffset(aParsedQuery, 10);

		assertEquals("Offset should be 10", 10L, getOffset(aParsedQuery));
	}

	private long getOffset(final ParsedQuery theQuery) {
		GetSlice aGetLimit = new GetSlice();

		try {
			theQuery.getTupleExpr().visit(aGetLimit);
		}
		catch (Exception e) {
			return -1;
		}

		return aGetLimit.getOffset();
	}

	private long getLimit(final ParsedQuery theQuery) {
		GetSlice aGetLimit = new GetSlice();

		try {
			theQuery.getTupleExpr().visit(aGetLimit);
		}
		catch (Exception e) {
			return -1;
		}

		return aGetLimit.getLimit();
	}

	private boolean contains(final ParsedQuery theQuery, final Class<? extends QueryModelNode> theType) {

		ContainsVisitor aVisitor = new ContainsVisitor(theType);

		try {
			theQuery.getTupleExpr().visit(aVisitor);
		}
		catch (Exception e) {
			return false;
		}

		return aVisitor.isContains();
	}

	private static class GetSlice extends QueryModelVisitorBase<Exception> {
		private long mLimit = -1;
		private long mOffset = -1;

		@Override
		public void meet(final Slice node) throws Exception {
			mLimit = node.getLimit();
			mOffset = node.getOffset();
		}

		public long getLimit() {
			return mLimit;
		}

		public long getOffset() {
			return mOffset;
		}
	}

	private static class ContainsVisitor extends QueryModelVisitorBase<Exception> {
		private boolean mContains = false;

		private final Class<? extends QueryModelNode> mClass;

		private ContainsVisitor(final Class<? extends QueryModelNode> theClass) {
			mClass = theClass;
		}

		public boolean isContains() {
			return mContains;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		protected void meetNode(final QueryModelNode theQueryModelNode) throws Exception {
			if (mClass.equals(theQueryModelNode.getClass())) {
				mContains = true;
			}
			else {
				super.meetNode(theQueryModelNode);
			}
		}
	}
}
