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

import org.junit.Test;
import org.junit.Ignore;

import static com.clarkparsia.openrdf.TestUtils.render;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Filter;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.URI;
import com.clarkparsia.openrdf.query.builder.QueryBuilder;
import com.clarkparsia.openrdf.query.builder.QueryBuilderFactory;
import com.clarkparsia.openrdf.query.builder.ValueExprFactory;
import static com.clarkparsia.openrdf.TestUtils.parse;
import com.clarkparsia.openrdf.vocabulary.FOAF;
import com.clarkparsia.openrdf.vocabulary.DC;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @version 0.3
 * @since 0.3.1
 */
public class TestRendering {

	@Test
	public void testRenderFromInSelect() throws Exception {
		URI aFrom = ValueFactoryImpl.getInstance().createURI("urn:from");

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.from(aFrom)
			.group()
			.atom("s", "p", "o");

		final String aExpected = "select ?s ?p ?o\n" +
								 "from <" + aFrom + ">\n" +
								 "where {\n" +
								 "  ?s ?p ?o.\n" +
								 "}";

		assertEquals(aExpected, render(aBuilder.query()));

	}

	@Test
	public void testRenderFromInAsk() throws Exception {
		URI aFrom = ValueFactoryImpl.getInstance().createURI("urn:from");

		QueryBuilder<ParsedBooleanQuery> aBuilder = QueryBuilderFactory.ask();

		aBuilder.from(aFrom)
			.group()
			.atom("s", "p", "o");

		final String aExpected = "ask\n" +
								 "from <" + aFrom + ">\n" +
								 "{\n" +
								 "  ?s ?p ?o.\n" +
								 "}";

		assertEquals(aExpected, render(aBuilder.query()));
	}

	@Test
	public void testRenderFromNamedInSelect() throws Exception {
		URI aFromNamed = ValueFactoryImpl.getInstance().createURI("urn:from_named");

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.fromNamed(aFromNamed)
			.group()
			.atom("s", "p", "o");

		final String aExpected = "select ?s ?p ?o\n" +
								 "from named <" + aFromNamed + ">\n" +
								 "where {\n" +
								 "  ?s ?p ?o.\n" +
								 "}";

		assertEquals(aExpected, render(aBuilder.query()));
	}

	@Test
	public void testRenderFromNamedInAsk() throws Exception {
		URI aFromNamed = ValueFactoryImpl.getInstance().createURI("urn:from_named");

		QueryBuilder<ParsedBooleanQuery> aBuilder = QueryBuilderFactory.ask();

		aBuilder.fromNamed(aFromNamed)
			.group()
			.atom("s", "p", "o");

		final String aExpected = "ask\n" +
								 "from named <" + aFromNamed + ">\n" +
								 "{\n" +
								 "  ?s ?p ?o.\n" +
								 "}";

		assertEquals(aExpected, render(aBuilder.query()));
	}

	@Test
	public void testSimpleRender() throws Exception {
		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.addProjectionVar("name", "mbox")
				.group().atom("x", FOAF.ontology().name, "name")
						.atom("x", FOAF.ontology().mbox, "mbox");

		final String aExpected = "select ?name ?mbox\n" +
						   "where {\n" +
						   "  ?x <http://xmlns.com/foaf/0.1/name> ?name.\n" +
						   "  ?x <http://xmlns.com/foaf/0.1/mbox> ?mbox.\n" +
						   "}";

		assertEquals(aExpected, render(aBuilder.query()));
	}

	@Test
	public void testRenderWithOptional() throws Exception {
		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.addProjectionVar("name", "mbox", "fn", "ln")
			.distinct().limit(100)
			.group().atom("x", FOAF.ontology().name, "name")
					.atom("x", FOAF.ontology().mbox, "mbox")
					.optional()
						.atom("x",FOAF.ontology().firstName,"fn")
						.atom("x",FOAF.ontology().surname,"ln");

		String aExpected = "select distinct ?name ?mbox ?fn ?ln\n" +
						   "where {\n" +
						   "  ?x <http://xmlns.com/foaf/0.1/name> ?name.\n" +
						   "  ?x <http://xmlns.com/foaf/0.1/mbox> ?mbox.\n" +
						   "  OPTIONAL {\n" +
						   "    ?x <http://xmlns.com/foaf/0.1/firstName> ?fn.\n" +
						   "    ?x <http://xmlns.com/foaf/0.1/surname> ?ln.\n" +
						   "  }.\n" +
						   "}\n" +
						   "limit 100";
		
		assertEquals(aExpected, render(aBuilder.query()));
	}

	@Test
	public void testRenderWithFiltersAndOptionals() throws Exception {
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


		final String aExpected = "select ?title ?price\n" +
								 "where {\n" +
								 "  ?x <http://purl.org/dc/elements/1.1/title> ?title.\n" +
								 "  ?x <http://purl.org/dc/elements/1.1/subject> ?subject.\n" +
								 "  filter (?price < \"\"\"300\"\"\"^^<http://www.w3.org/2001/XMLSchema#int>).\n" +
								 "  filter  ((?price > \"\"\"0\"\"\"^^<http://www.w3.org/2001/XMLSchema#int>) && (?price != \"\"\"10\"\"\"^^<http://www.w3.org/2001/XMLSchema#int>)).\n" +
								 "  OPTIONAL {\n" +
								 "    ?x <http://example.org/ns#price> ?price.\n" +
								 "    ?x <http://example.org/ns#discount> ?d.\n" +
								 "    filter ( bound(?d) && (?price < \"\"\"30\"\"\"^^<http://www.w3.org/2001/XMLSchema#int>))\n" +
								 "  }.\n" +
								 "}";

		assertEquals(aExpected, render(aBuilder.query()));
	}

	@Test
	public void testRenderWithFilters() throws Exception {
		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.addProjectionVar("title", "price")
				.group()
					.atom("x", DC.ontology().title,"title")
					.atom("x", DC.ontology().subject,"subject")
					.filter("price", Compare.CompareOp.LT, ValueFactoryImpl.getInstance().createLiteral(300))
					.filter().and(ValueExprFactory.gt("price", ValueFactoryImpl.getInstance().createLiteral(0)),
								  ValueExprFactory.ne("price", ValueFactoryImpl.getInstance().createLiteral(10)));


		final String aExpected = "select ?title ?price\n" +
								 "where {\n" +
								 "  ?x <http://purl.org/dc/elements/1.1/title> ?title.\n" +
								 "  ?x <http://purl.org/dc/elements/1.1/subject> ?subject.\n" +
								 "  filter (?price < \"\"\"300\"\"\"^^<http://www.w3.org/2001/XMLSchema#int>).\n" +
								 "  filter  ((?price > \"\"\"0\"\"\"^^<http://www.w3.org/2001/XMLSchema#int>) && (?price != \"\"\"10\"\"\"^^<http://www.w3.org/2001/XMLSchema#int>)).\n" +
								 "}";

		assertEquals(aExpected, render(aBuilder.query()));
	}

	@Test
	public void testRenderWithUnions() throws Exception {
		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

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

		final String aExpected = "select distinct ?uri ?aLabel\n" +
								 "where {\n" +
								 "  ?var4 <http://www.clarkparsia.com/baseball/team> ?uri.\n" +
								 "  ?var4 <http://www.clarkparsia.com/baseball/player> ?goal_base.\n" +
								 "  {\n" +
								 "  ?var4 <http://www.clarkparsia.com/baseball/position> ?var0_0.\n" +
								 "  filter (?var0_0 = <http://www.clarkparsia.com/baseball/FirstBase>).\n" +
								 "  }\n" +
								 "  union\n" +
								 "  {\n" +
								 "  ?var4 <http://www.clarkparsia.com/baseball/position> ?var0_1.\n" +
								 "  filter (?var0_1 = <http://www.clarkparsia.com/baseball/ThirdBase>).\n" +
								 "  }.\n" +
								 "  OPTIONAL {\n" +
								 "    ?uri <http://www.w3.org/2000/01/rdf-schema#label> ?aLabel.\n" +
								 "  }.\n" +
								 "}";

		assertEquals(aExpected, render(aBuilder.query()));
	}

	@Test
	public void testRenderWithContexts() throws Exception {
		String aContextQuery = "select distinct ?resource\n" +
							   "where {\n" +
							   " GRAPH <tag:sswap.info:2011-02:sswap:ds:MergedABox-a8e7a5a3-099d-44cb-8785-6872898e7126> { {?object <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://sswapmeet.sswap.info/sswap/SSWAP>. \n" +
							   " ?subject <http://sswapmeet.sswap.info/sswap/mapsTo> ?object. \n" +
							   " ?graph <http://sswapmeet.sswap.info/sswap/hasMapping> ?subject. \n" +
							   " ?object <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://sswapmeet.sswap.info/qtl/QTL>. \n" +
							   " ?object <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://sswapmeet.sswap.info/sswap/Object>. \n" +
							   " ?resource <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://sswapmeet.sswap.info/sswap/Resource>. \n" +
							   " ?object <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://sswapmeet.sswap.info/qtl/QTLs>. \n" +
							   " ?resource <http://sswapmeet.sswap.info/sswap/operatesOn> ?graph. \n" +
							   " ?resource <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://sswapmeet.sswap.info/sswap/SSWAP>. \n" +
							   "  filter ((?resource = <http://sswap.gramene.org/vpin/qtl-by-trait-accession>)) .}. }}";

		String aExpected = "select distinct ?resource\n" +
						   "where {\n" +
						   "  GRAPH <tag:sswap.info:2011-02:sswap:ds:MergedABox-a8e7a5a3-099d-44cb-8785-6872898e7126> {\n" +
						   "    ?object <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://sswapmeet.sswap.info/sswap/SSWAP>.\n" +
						   "    ?subject <http://sswapmeet.sswap.info/sswap/mapsTo> ?object.\n" +
						   "    ?graph <http://sswapmeet.sswap.info/sswap/hasMapping> ?subject.\n" +
						   "    ?object <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://sswapmeet.sswap.info/qtl/QTL>.\n" +
						   "    ?object <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://sswapmeet.sswap.info/sswap/Object>.\n" +
						   "    ?resource <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://sswapmeet.sswap.info/sswap/Resource>.\n" +
						   "    ?object <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://sswapmeet.sswap.info/qtl/QTLs>.\n" +
						   "    ?resource <http://sswapmeet.sswap.info/sswap/operatesOn> ?graph.\n" +
						   "    ?resource <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://sswapmeet.sswap.info/sswap/SSWAP>.\n" +
						   "    filter (?resource = <http://sswap.gramene.org/vpin/qtl-by-trait-accession>).\n" +
						   "}}";
		
		assertEquals(aExpected, render(new SPARQLParser().parseQuery(aContextQuery, "http://example.org")));
	}

	@Test
	public void testRenderConstruct() throws Exception {
		QueryBuilder<ParsedGraphQuery> aConstructBuilder = QueryBuilderFactory.construct();

		aConstructBuilder.addProjectionStatement("s", RDF.TYPE, RDFS.RESOURCE)
                .group().atom("s", RDF.TYPE, "o").closeGroup().query();

		final String aExpected = "construct  {\n" +
								 "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2000/01/rdf-schema#Resource>.\n" +
								 "}\n" +
								 "where {\n" +
								 "  ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o.\n" +
								 "}";

		assertEquals(aExpected, render(aConstructBuilder.query()));
	}

	@Test
	public void testRenderAsk() throws Exception {
		String aQuery = "ask \n" +
						"where {\n" +
						"?var6 <http://www.clarkparsia.com/baseball/battingAverage> ?uri." +
						"?var5 <http://www.clarkparsia.com/baseball/team> ?var2.  " +
						"?var5 <http://www.clarkparsia.com/baseball/player> ?goal_base.  " +
						"?goal_base <http://www.clarkparsia.com/baseball/careerBatting> ?var6.   {?var5 <http://www.clarkparsia.com/baseball/position> <http://www.clarkparsia.com/baseball/position/FirstBase>.  }\n" +
						"union\n" +
						"{?var5 <http://www.clarkparsia.com/baseball/position> <http://www.clarkparsia.com/baseball/position/ThirdBase>.  }.\n" +
						"}\n";

		final String aExpected = "ask\n" +
								 "{\n" +
								 "  ?var6 <http://www.clarkparsia.com/baseball/battingAverage> ?uri.\n" +
								 "  ?var5 <http://www.clarkparsia.com/baseball/team> ?var2.\n" +
								 "  ?var5 <http://www.clarkparsia.com/baseball/player> ?goal_base.\n" +
								 "  ?goal_base <http://www.clarkparsia.com/baseball/careerBatting> ?var6.\n" +
								 "  {\n" +
								 "  ?var5 <http://www.clarkparsia.com/baseball/position> <http://www.clarkparsia.com/baseball/position/FirstBase>.\n" +
								 "  }\n" +
								 "  union\n" +
								 "  {\n" +
								 "  ?var5 <http://www.clarkparsia.com/baseball/position> <http://www.clarkparsia.com/baseball/position/ThirdBase>.\n" +
								 "  }.\n" +
								 "}";

		assertEquals(aExpected, render(new SPARQLParser().parseQuery(aQuery, "http://example.org")));
	}

	@Test
	public void testWithOrderBy() throws Exception {
		String aQuery = "select ?uri \n" +
						"where {\n" +
						"?var6 <http://www.clarkparsia.com/baseball/battingAverage> ?uri." +
						"?var5 <http://www.clarkparsia.com/baseball/team> ?var2.  " +
						"?var5 <http://www.clarkparsia.com/baseball/player> ?goal_base.  " +
						"?goal_base <http://www.clarkparsia.com/baseball/careerBatting> ?var6.   {?var5 <http://www.clarkparsia.com/baseball/position> <http://www.clarkparsia.com/baseball/position/FirstBase>.  }\n" +
						"union\n" +
						"{?var5 <http://www.clarkparsia.com/baseball/position> <http://www.clarkparsia.com/baseball/position/ThirdBase>.  }.\n" +
						"}\n" +
						"order by desc(?uri)\n";

		final String aExpected = "select ?uri\n" +
								 "where {\n" +
								 "  ?var6 <http://www.clarkparsia.com/baseball/battingAverage> ?uri.\n" +
								 "  ?var5 <http://www.clarkparsia.com/baseball/team> ?var2.\n" +
								 "  ?var5 <http://www.clarkparsia.com/baseball/player> ?goal_base.\n" +
								 "  ?goal_base <http://www.clarkparsia.com/baseball/careerBatting> ?var6.\n" +
								 "  {\n" +
								 "  ?var5 <http://www.clarkparsia.com/baseball/position> <http://www.clarkparsia.com/baseball/position/FirstBase>.\n" +
								 "  }\n" +
								 "  union\n" +
								 "  {\n" +
								 "  ?var5 <http://www.clarkparsia.com/baseball/position> <http://www.clarkparsia.com/baseball/position/ThirdBase>.\n" +
								 "  }.\n" +
								 "}\n" +
								 "order by desc(?uri)";

		assertEquals(aExpected, render(new SPARQLParser().parseQuery(aQuery, "http://example.org")));
	}

	@Test
	public void testRenderDescribe() throws Exception {
		QueryBuilder<ParsedGraphQuery> aBuilder = QueryBuilderFactory.describe(new String[] {"x", "y"}, ValueFactoryImpl.getInstance().createURI("http://example.org/")).group().atom("x", FOAF.ontology().knows, "y").closeGroup();

		final String aExpected = "construct  {\n" +
								 "  ?descr_subj ?descr_pred ?descr_obj.\n" +
								 "\n" +
								 "  ?x <http://xmlns.com/foaf/0.1/knows> ?y.\n" +
								 "}\n" +
								 "where {\n" +
								 "  ?descr_subj ?descr_pred ?descr_obj.\n" +
								 "  filter  ( sameTerm(?x, ?descr_subj) ||  sameTerm(?x, ?descr_obj)).\n" +
								 "  filter  ( sameTerm(?y, ?descr_subj) ||  sameTerm(?y, ?descr_obj)).\n" +
								 "  filter  ( sameTerm(<http://example.org/>, ?descr_subj) ||  sameTerm(<http://example.org/>, ?descr_obj)).\n" +
								 "  ?x <http://xmlns.com/foaf/0.1/knows> ?y.\n" +
								 "}";

		assertEquals(aExpected, render(aBuilder.query()));
	}

	@Test
	public void testLeftJoinScopeRender() throws Exception {
		String aQuery = "PREFIX : <http://example/>\n" +
						"\n" +
						"SELECT *\n" +
						"{ \n" +
						"  ?X  :name \"paul\"\n" +
						"  {?Y :name \"george\" . OPTIONAL { ?X :email ?Z } }\n" +
						"}\n" +
						"";

		String aQuery2 = "PREFIX : <http://example/>\n" +
						"\n" +
						"SELECT *\n" +
						"{ \n" +
						"  ?X  :name \"paul\".\n" +
						"  ?Y :name \"george\" . OPTIONAL { ?X :email ?Z }\n" +
						"}\n" +
						"";

		final ParsedQuery aParsedQuery = new SPARQLParser().parseQuery(aQuery, "http://example.org");

		final String aResult = render(aParsedQuery);

		final ParsedQuery aReparsedQuery = new SPARQLParser().parseQuery(aResult, "http://example.org");

		assertTrue(aReparsedQuery.getTupleExpr() instanceof Projection);

		Projection p = (Projection) aReparsedQuery.getTupleExpr();

		assertTrue(p.getArg() instanceof Join);

		Join aJoin = (Join) p.getArg();

		assertTrue(aJoin.getLeftArg() instanceof StatementPattern);
		assertTrue(aJoin.getRightArg() instanceof LeftJoin);
	}

	@Ignore
	@Test
	public void testFilterScoping() throws Exception {
		final String aQuery = "PREFIX : <http://example/> \n" +
							  "\n" +
							  "SELECT ?v\n" +
							  "{ :x :p ?v . { FILTER(?v = 1) } }";


		final ParsedQuery aParsedQuery = new SPARQLParser().parseQuery(aQuery, "http://example.org");

		final String aResult = render(aParsedQuery);

		final ParsedQuery aReparsedQuery = new SPARQLParser().parseQuery(aResult, "http://example.org");

		assertTrue(aReparsedQuery.getTupleExpr() instanceof Projection);

		Projection p = (Projection) aReparsedQuery.getTupleExpr();

		assertTrue(p.getArg() instanceof Join);

		Join aJoin = (Join) p.getArg();

		assertTrue(aJoin.getLeftArg() instanceof StatementPattern);
		assertTrue(aJoin.getRightArg() instanceof Filter);
	}

	@Ignore
	@Test
	public void testRenderConstructWithReification() throws Exception {
		final String aQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
							  "PREFIX  foaf:       <http://xmlns.com/foaf/0.1/>\n" +
							  "\n" +
							  "CONSTRUCT { [ rdf:subject ?s ;\n" +
							  "              rdf:predicate ?p ;\n" +
							  "              rdf:object ?o ] . }\n" +
							  "WHERE {\n" +
							  "  ?s ?p ?o .\n" +
							  "}";

		System.err.println(render(parse(aQuery)));
		fail();
	}

	@Test
	public void testRenderTrue() throws Exception {
		String aQuery = "prefix : <http://example.org/ns#>\n" +
						"select ?x where {\n" +
						"    ?x :p \"foo\" .\n" +
						"    FILTER (true) .\n" +
						"}";

		// verify this at least round trips
		parse(render(parse(aQuery)));

		// TODO: verify actual query model
	}
	
	// ------ Testing ORDER BY ------ //
	
	@Test
	public void testBuildQueryOrderBy() throws Exception {
		URI aFrom = ValueFactoryImpl.getInstance().createURI("urn:from");

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.from(aFrom)
			.group()
			.atom("s", "p", "o")
			.closeGroup()
			.orderBy("s")
			.orderByDesc("o");
			

		final String aExpected = "select ?s ?p ?o\n" +
								 "from <" + aFrom + ">\n" +
								 "where {\n" +
								 "  ?s ?p ?o.\n" +
								 "}\n" +
								 "order by ?s desc(?o)";
		
		assertEquals(aExpected, render(aBuilder.query()));
	}
	
	@Test
	public void testBuildQueryOrderByLimitOffset() throws Exception {
		URI aFrom = ValueFactoryImpl.getInstance().createURI("urn:from");

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.from(aFrom)
			.group()
			.atom("s", "p", "o")
			.closeGroup()
			.orderBy("s")
			.orderByDesc("o").limit(100).offset(200);
			

		final String aExpected = "select ?s ?p ?o\n" +
								 "from <" + aFrom + ">\n" +
								 "where {\n" +
								 "  ?s ?p ?o.\n" +
								 "}\n" +
								 "order by ?s desc(?o)\n" +
								 "limit 100\n" +
								 "offset 200";
		
		assertEquals(aExpected, render(aBuilder.query()));
	}
	
	@Test
	public void testBuildQueryOrderByLimitOffsetOther() throws Exception {
		URI aFrom = ValueFactoryImpl.getInstance().createURI("urn:from");

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.from(aFrom)
			.group()
			.atom("s", "p", "o")
			.closeGroup()
			.limit(100).offset(200)
			.orderBy("s")
			.orderByDesc("o");
			

		final String aExpected = "select ?s ?p ?o\n" +
								 "from <" + aFrom + ">\n" +
								 "where {\n" +
								 "  ?s ?p ?o.\n" +
								 "}\n" +
								 "order by ?s desc(?o)\n" +
								 "limit 100\n" +
								 "offset 200";
		
		assertEquals(aExpected, render(aBuilder.query()));
		
		aBuilder.reset();
		aBuilder.from(aFrom)
			.group()
			.atom("s", "p", "o")
			.closeGroup()
			.offset(200)
			.orderByAsc("s")
			.orderByDesc("o")
			.limit(100);
		
		assertEquals(aExpected, render(aBuilder.query()));
	}
	
	/**
	 * SPARQL just ignores the ordering when using variables not in the projection.
	 * @throws Exception
	 */
	@Test
	public void testBuildQueryOrderByNonValidVars() throws Exception {
		URI aFrom = ValueFactoryImpl.getInstance().createURI("urn:from");

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.from(aFrom)
			.group()
			.atom("s", "p", "o")
			.closeGroup()
			.orderBy("x")
			.orderByDesc("y").limit(100).offset(200);
			

		final String aExpected = "select ?s ?p ?o\n" +
								 "from <" + aFrom + ">\n" +
								 "where {\n" +
								 "  ?s ?p ?o.\n" +
								 "}\n" +
								 "order by ?x desc(?y)\n" +
								 "limit 100\n" +
								 "offset 200";
		
		assertEquals(aExpected, render(aBuilder.query()));
	}
	
	@Test
	public void testBuildQueryOrderByNullVars() throws Exception {
		URI aFrom = ValueFactoryImpl.getInstance().createURI("urn:from");

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.from(aFrom)
			.group()
			.atom("s", "p", "o")
			.closeGroup()
			.orderBy((String[])null)
			.orderByDesc((String[])null).limit(100).offset(200);
			

		final String aExpected = "select ?s ?p ?o\n" +
								 "from <" + aFrom + ">\n" +
								 "where {\n" +
								 "  ?s ?p ?o.\n" +
								 "}\n" +
								 "limit 100\n" +
								 "offset 200";
		
		assertEquals(aExpected, render(aBuilder.query()));
	}
	
	@Test
	public void testBuildQueryOrderByMultipleVars() throws Exception {
		URI aFrom = ValueFactoryImpl.getInstance().createURI("urn:from");

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.from(aFrom)
			.group()
			.atom("s", "p", "o")
			.closeGroup()
			.orderBy("s").orderByDesc("p").orderByAsc("o");
			

		final String aExpected = "select ?s ?p ?o\n" +
								 "from <" + aFrom + ">\n" +
								 "where {\n" +
								 "  ?s ?p ?o.\n" +
								 "}\n" +
								 "order by ?s desc(?p) ?o";
		
		assertEquals(aExpected, render(aBuilder.query()));
	}
	
	@Test
	public void testBuildQueryOrderByAndReset() throws Exception {
		URI aFrom = ValueFactoryImpl.getInstance().createURI("urn:from");

		QueryBuilder<ParsedTupleQuery> aBuilder = QueryBuilderFactory.select();

		aBuilder.from(aFrom)
			.group()
			.atom("s", "p", "o")
			.closeGroup()
			.orderBy("s").orderByDesc("p").orderByAsc("o");
			

		final String aExpected = "select ?s ?p ?o\n" +
								 "from <" + aFrom + ">\n" +
								 "where {\n" +
								 "  ?s ?p ?o.\n" +
								 "}\n" +
								 "order by ?s desc(?p) ?o";
		
		assertEquals(aExpected, render(aBuilder.query()));
		
		// ------ RESET ------ //
		aBuilder.reset();
		
		aBuilder.from(aFrom)
		.group()
		.atom("s", "p", "o")
		.closeGroup()
		.orderBy("x")
		.orderByDesc("y").limit(100).offset(200);
		

		final String aExpected2 = "select ?s ?p ?o\n" +
							 "from <" + aFrom + ">\n" +
							 "where {\n" +
							 "  ?s ?p ?o.\n" +
							 "}\n" +
							 "order by ?x desc(?y)\n" +
							 "limit 100\n" +
							 "offset 200";
	
		assertEquals(aExpected2, render(aBuilder.query()));
	}
}
