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

package com.complexible.common.openrdf;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.complexible.common.openrdf.model.ContextAwareStatement;
import com.complexible.common.openrdf.model.ContextAwareValueFactory;
import com.complexible.common.openrdf.model.GraphIO;
import com.complexible.common.openrdf.model.Graphs;
import com.complexible.common.openrdf.model.Statements;
import com.complexible.common.openrdf.query.impl.GraphQueryResultImpl;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.junit.Test;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * <p>Tests for Graphs</p>
 *
 * @author Michael Grove
 * @since	0.8
 * @version	1.1
 */
public class TestGraphs {
	@Test
	public void testNewGraph() throws Exception {
		Graph aInput = TestUtils.createRandomGraph(3);
		List<Statement> aStmtList = Lists.newArrayList(aInput);

		assertTrue(ModelUtil.equals(aInput, Graphs.newGraph(aStmtList)));

		assertTrue(ModelUtil.equals(aInput, Graphs.newGraph(aStmtList.toArray(new Statement[3]))));

		assertTrue(ModelUtil.equals(aInput, Graphs.newGraph(aStmtList.iterator())));

		assertTrue(ModelUtil.equals(aInput, Graphs.newGraph(new GraphQueryResultImpl(Maps.<String, String>newHashMap(), aInput))));
	}

	@Test
    @SuppressWarnings("deprecation")
	public void testContextGraph() throws Exception {
		assertTrue(Graphs.contextGraph().getValueFactory() instanceof ContextAwareValueFactory);
	}

	@Test
    @SuppressWarnings("deprecation")
	public void testWithContext() throws Exception {
		final URI aCxt = ValueFactoryImpl.getInstance().createURI("urn:context");

		Graph aGraph = Graphs.withContext(TestUtils.createRandomGraph(20), aCxt);

		assertTrue(aGraph.getValueFactory() instanceof ContextAwareValueFactory);

		for (Statement aStmt : aGraph) {
			assertTrue(aStmt instanceof ContextAwareStatement);
			assertEquals(aCxt, aStmt.getContext());
		}
	}

	@Test
	public void testUnion() throws Exception {
		Graph one = TestUtils.createRandomGraph(20);
		Graph two = TestUtils.createRandomGraph(20);

		Graph union = Graphs.union(one, two);

		assertTrue(union.size() >= one.size() + two.size());

		assertTrue(union.containsAll(one));
		assertTrue(union.containsAll(two));

		union.removeAll(one);
		union.removeAll(two);

		assertTrue(union.isEmpty());
	}

	@Test
	public void testList() throws Exception {
		List<Resource> aElems = Lists.newArrayList();

		for (int i = 0; i < 10; i++) {
			aElems.add(ValueFactoryImpl.getInstance().createURI("urn:" + i));
		}

		Graph aGraph = Graphs.newGraph(Graphs.toList(aElems));

		assertEquals(aElems, Graphs.asList(aGraph, Graphs.filter(aGraph, null, RDF.FIRST, aElems.get(0)).iterator().next().getSubject()));

		for (Resource aRes : aElems) {
			assertTrue(Graphs.isList(aGraph, Graphs.filter(aGraph, null, RDF.FIRST, aRes).iterator().next().getSubject()));
		}

		final URI s = ValueFactoryImpl.getInstance().createURI("urn:s");
		final URI p = ValueFactoryImpl.getInstance().createURI("urn:p");
		final URI o = ValueFactoryImpl.getInstance().createURI("urn:o");

		aGraph.add(s,p,o);

		assertFalse(Graphs.isList(aGraph, s));
	}

	@Test
	public void testOf() throws Exception {
		Graph aInput = TestUtils.createRandomGraph(20);

		File aFile = File.createTempFile("foo", ".ttl");

		try {
			Files.write(GraphIO.toString(aInput, RDFFormat.TURTLE), aFile, Charsets.UTF_8);

			assertTrue(ModelUtil.equals(aInput, Graphs.of(aFile)));
		}
		finally {
			if (!aFile.delete()) {
				aFile.deleteOnExit();
			}
		}
	}

	@Test
	public void testFilter() {
		Graph aInput = TestUtils.createRandomGraph(20);

		Resource s = ValueFactoryImpl.getInstance().createURI("urn:s");
		Resource s2 = ValueFactoryImpl.getInstance().createURI("urn:s2");

		URI o = ValueFactoryImpl.getInstance().createURI("urn:o");
		URI o2 = ValueFactoryImpl.getInstance().createURI("urn:o2");

		Statement st1 = ValueFactoryImpl.getInstance().createStatement(s, RDF.TYPE, o);
		Statement st2 = ValueFactoryImpl.getInstance().createStatement(s2, RDF.TYPE, o2);

		aInput.add(st1);
		aInput.add(st2);

		Graph aGraph = Graphs.filter(aInput, Statements.predicateIs(RDF.TYPE));

		assertEquals(2, aGraph.size());

		assertTrue(aGraph.contains(st1));
		assertTrue(aGraph.contains(st2));
	}

	@Test
	public void testTransform() {
		final Graph aInput = TestUtils.createRandomGraph(20);

		Function<Statement, Statement> xform = new Function<Statement, Statement>() {
			@Override
			public Statement apply(final Statement theStatement) {
				return ValueFactoryImpl.getInstance().createStatement(theStatement.getSubject(), RDF.TYPE, RDFS.CLASS);
			}
		};

		final Graph aOutput = Graphs.transform(aInput, xform);

		Collection<Resource> subjs = Graphs.collect(aInput, Statements.subjectOptional());

		for (Statement aStmt : aOutput) {
			assertTrue(subjs.contains(aStmt.getSubject()));
			assertEquals(RDF.TYPE, aStmt.getPredicate());
			assertEquals(RDFS.CLASS, aStmt.getObject());
		}
	}

	@Test
	public void testAny() {
		final Graph aInput = TestUtils.createRandomGraph(20);

		assertFalse(Graphs.any(aInput, Statements.predicateIs(RDF.TYPE)));

		aInput.add(ValueFactoryImpl.getInstance().createBNode(), RDF.TYPE, ValueFactoryImpl.getInstance().createBNode());

		assertTrue(Graphs.any(aInput, Statements.predicateIs(RDF.TYPE)));
	}

	@Test
	public void testAll() {
		final Graph aInput = TestUtils.createRandomGraph(20);

		assertFalse(Graphs.all(aInput, Statements.predicateIs(RDF.TYPE)));

		Function<Statement, Statement> xform = new Function<Statement, Statement>() {
			@Override
			public Statement apply(final Statement theStatement) {
				return ValueFactoryImpl.getInstance().createStatement(theStatement.getSubject(), RDF.TYPE, theStatement.getObject());
			}
		};

		assertTrue(Graphs.all(Graphs.transform(aInput, xform), Statements.predicateIs(RDF.TYPE)));
	}

	@Test
	public void testCollect() {
		final Graph aInput = TestUtils.createRandomGraph(20);

		Set<Resource> subjs = Sets.newHashSet(Graphs.collect(aInput, Statements.subjectOptional()));

		Set<Resource> aExpected = Sets.newHashSet();

		for (Statement st : aInput) {
			aExpected.add(st.getSubject());
		}

		assertEquals(aExpected, subjs);
	}

	@Test
	public void testFind() {
		final Graph aInput = TestUtils.createRandomGraph(20);

		Optional<Statement> aResult = Graphs.find(aInput, Statements.predicateIs(RDF.TYPE));

		assertTrue(!aResult.isPresent());

		Statement st = ValueFactoryImpl.getInstance().createStatement(ValueFactoryImpl.getInstance().createBNode(), RDF.TYPE, ValueFactoryImpl.getInstance().createBNode());

		aInput.add(st);

		aResult = Graphs.find(aInput, Statements.predicateIs(RDF.TYPE));

		assertEquals(st, aResult.orNull());
	}

	@Test
	public void testGetObject() {
		final Graph aInput = TestUtils.createRandomGraph(20);

		final Statement aStatement = aInput.iterator().next();

		assertEquals(aStatement.getObject(), Graphs.getObject(aInput, aStatement.getSubject(), aStatement.getPredicate()).orNull());

		assertTrue(Graphs.getObject(aInput, aStatement.getSubject(), RDF.TYPE).orNull() == null);
	}

	@Test
	public void testGetLiteral() {
		final URI s = ValueFactoryImpl.getInstance().createURI("urn:s");
		final URI p = ValueFactoryImpl.getInstance().createURI("urn:p");
		final URI p2 = ValueFactoryImpl.getInstance().createURI("urn:p2");

		final URI o = ValueFactoryImpl.getInstance().createURI("urn:o");
		final Literal l = ValueFactoryImpl.getInstance().createLiteral("literal");

		Graph aGraph = Graphs.newGraph();

		aGraph.add(s, p, o);
		aGraph.add(s, p2, l);

		assertEquals(l, Graphs.getLiteral(aGraph, s, p2).orNull());

		assertTrue(Graphs.getLiteral(aGraph, s, p).orNull() == null);

		assertTrue(Graphs.getLiteral(aGraph, s, RDF.TYPE).orNull() == null);
	}

	@Test
	public void testGetResource() {
		final URI s = ValueFactoryImpl.getInstance().createURI("urn:s");
		final URI p = ValueFactoryImpl.getInstance().createURI("urn:p");
		final URI p2 = ValueFactoryImpl.getInstance().createURI("urn:p2");

		final URI o = ValueFactoryImpl.getInstance().createURI("urn:o");
		final Literal l = ValueFactoryImpl.getInstance().createLiteral("literal");

		Graph aGraph = Graphs.newGraph();

		aGraph.add(s, p, o);
		aGraph.add(s, p2, l);

		assertEquals(o, Graphs.getResource(aGraph, s, p).orNull());

		assertTrue(Graphs.getResource(aGraph, s, p2).orNull() == null);

		assertTrue(Graphs.getResource(aGraph, s, RDF.TYPE).orNull() == null);
	}

	@Test
	public void testGetBoolean() {
		final URI s = ValueFactoryImpl.getInstance().createURI("urn:s");
		final URI p = ValueFactoryImpl.getInstance().createURI("urn:p");
		final URI p2 = ValueFactoryImpl.getInstance().createURI("urn:p2");
		final URI p3 = ValueFactoryImpl.getInstance().createURI("urn:p3");
		final URI p4 = ValueFactoryImpl.getInstance().createURI("urn:p4");

		final URI o = ValueFactoryImpl.getInstance().createURI("urn:o");
		final Literal l = ValueFactoryImpl.getInstance().createLiteral("literal");
		final Literal b = ValueFactoryImpl.getInstance().createLiteral("true");
		final Literal b2 = ValueFactoryImpl.getInstance().createLiteral(true);

		Graph aGraph = Graphs.newGraph();

		aGraph.add(s, p, o);
		aGraph.add(s, p2, l);
		aGraph.add(s, p3, b);
		aGraph.add(s, p4, b2);

		assertTrue(Graphs.getBooleanValue(aGraph, s, p).orNull() == null);
		assertTrue(Graphs.getBooleanValue(aGraph, s, p2).orNull() == null);
		assertTrue(Graphs.getBooleanValue(aGraph, s, RDF.TYPE).orNull() == null);
		assertTrue(Graphs.getBooleanValue(aGraph, s, p3).or(false));
		assertTrue(Graphs.getBooleanValue(aGraph, s, p4).or(false));
	}
}