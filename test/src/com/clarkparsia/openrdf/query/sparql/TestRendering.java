// Copyright (c) 2010 - 2011 -- Clark & Parsia, LLC. <http://www.clarkparsia.com>
// For more information about licensing and copyright of this software, please contact
// inquiries@clarkparsia.com or visit http://stardog.com

package com.clarkparsia.openrdf.query.sparql;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.query.algebra.Compare;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.RDF;
import com.clarkparsia.openrdf.query.builder.QueryBuilder;
import com.clarkparsia.openrdf.query.builder.QueryBuilderFactory;
import com.clarkparsia.openrdf.query.builder.ValueExprFactory;
import com.clarkparsia.openrdf.vocabulary.FOAF;
import com.clarkparsia.openrdf.vocabulary.DC;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @version 0
 * @since 0
 */
public class TestRendering {

	private String render(final ParsedQuery theQuery) throws Exception {
		return new SPARQLQueryRenderer().render(theQuery);
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

		final String aExpected = "ask {\n" +
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
}
