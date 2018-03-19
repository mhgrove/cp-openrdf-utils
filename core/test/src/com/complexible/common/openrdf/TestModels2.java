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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.Test;

import com.complexible.common.openrdf.model.ModelIO;
import com.complexible.common.openrdf.model.Models2;
import com.complexible.common.openrdf.model.Statements;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * <p>Tests for Models2</p>
 *
 * @author Michael Grove
 * @since	0.8
 * @version	1.1
 */
public class TestModels2 {
	@Test
	public void testNewGraph() throws Exception {
		Model aInput = TestUtils.createRandomModel(3);

		List<Statement> aStmtList = Lists.newArrayList(aInput);

		assertTrue(Models.isomorphic(aInput, Models2.newModel(aStmtList)));

		assertTrue(Models.isomorphic(aInput, Models2.newModel(aStmtList.toArray(new Statement[3]))));

		assertTrue(Models.isomorphic(aInput, Models2.newModel(aStmtList.iterator())));

		assertTrue(Models.isomorphic(aInput, Models2.newModel(new IteratingGraphQueryResult(Maps.<String, String>newHashMap(), aInput))));
	}

	@Test
    @SuppressWarnings("deprecation")
	public void testWithContext() throws Exception {
		final IRI aCxt = SimpleValueFactory.getInstance().createIRI("urn:context");

		Model aGraph = Models2.withContext(TestUtils.createRandomModel(20), aCxt);

		for (Statement aStmt : aGraph) {
			assertEquals(aCxt, aStmt.getContext());
		}
	}

	@Test
	public void testUnion() throws Exception {
		Model one = TestUtils.createRandomModel(20);
		Model two = TestUtils.createRandomModel(20);

		Model union = Models2.union(one, two);

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
			aElems.add(SimpleValueFactory.getInstance().createIRI("urn:" + i));
		}

		Model aGraph = Models2.newModel(Models2.toList(aElems));

		assertEquals(aElems, Models2.asList(aGraph, aGraph.stream()
		                                                 .filter(Statements.matches(null, RDF.FIRST, aElems.get(0)))
		                                                 .map(Statement::getSubject)
		                                                 .findFirst()
		                                                 .get()));

		for (Resource aRes : aElems) {
			assertTrue(Models2.isList(aGraph, aGraph.stream()
			                                       .filter(Statements.matches(null, RDF.FIRST, aRes))
			                                       .map(Statement::getSubject)
			                                       .findFirst()
			                                       .get()));
		}

		final IRI s = SimpleValueFactory.getInstance().createIRI("urn:s");
		final IRI p = SimpleValueFactory.getInstance().createIRI("urn:p");
		final IRI o = SimpleValueFactory.getInstance().createIRI("urn:o");

		aGraph.add(s,p,o);

		assertFalse(Models2.isList(aGraph, s));
	}

	@Test
	public void testOf() throws Exception {
		Model aInput = TestUtils.createRandomModel(20);

		Path aFile = java.nio.file.Files.createTempFile("foo", ".ttl");

		try {
			Files.write(ModelIO.toString(aInput, RDFFormat.TURTLE), aFile.toFile(), Charsets.UTF_8);

			assertTrue(Models.isomorphic(aInput, Models2.of(aFile)));
		}
		finally {
			java.nio.file.Files.delete(aFile);
		}
	}

	@Test
	public void testFilter() {
		Model aInput = TestUtils.createRandomModel(20);

		Resource s = SimpleValueFactory.getInstance().createIRI("urn:s");
		Resource s2 = SimpleValueFactory.getInstance().createIRI("urn:s2");

		IRI o = SimpleValueFactory.getInstance().createIRI("urn:o");
		IRI o2 = SimpleValueFactory.getInstance().createIRI("urn:o2");

		Statement st1 = SimpleValueFactory.getInstance().createStatement(s, RDF.TYPE, o);
		Statement st2 = SimpleValueFactory.getInstance().createStatement(s2, RDF.TYPE, o2);

		aInput.add(st1);
		aInput.add(st2);

		Model aGraph = aInput.stream().filter(Statements.predicateIs(RDF.TYPE)).collect(Models2.toModel());

		assertEquals(2, aGraph.size());

		assertTrue(aGraph.contains(st1));
		assertTrue(aGraph.contains(st2));
	}

	@Test
	public void testGetObject() {
		final Model aInput = TestUtils.createRandomModel(20);

		final Statement aStatement = aInput.iterator().next();

		assertEquals(aStatement.getObject(), Models2.getObject(aInput, aStatement.getSubject(), aStatement.getPredicate()).orElse(null));

		assertTrue(Models2.getObject(aInput, aStatement.getSubject(), RDF.TYPE).orElse(null) == null);
	}

	@Test
	public void testGetLiteral() {
		final IRI s = SimpleValueFactory.getInstance().createIRI("urn:s");
		final IRI p = SimpleValueFactory.getInstance().createIRI("urn:p");
		final IRI p2 = SimpleValueFactory.getInstance().createIRI("urn:p2");

		final IRI o = SimpleValueFactory.getInstance().createIRI("urn:o");
		final Literal l = SimpleValueFactory.getInstance().createLiteral("literal");

		Model aGraph = Models2.newModel();

		aGraph.add(s, p, o);
		aGraph.add(s, p2, l);

		assertEquals(l, Models2.getLiteral(aGraph, s, p2).orElse(null));

		assertTrue(Models2.getLiteral(aGraph, s, p).orElse(null) == null);

		assertTrue(Models2.getLiteral(aGraph, s, RDF.TYPE).orElse(null) == null);
	}

	@Test
	public void testGetResource() {
		final IRI s = SimpleValueFactory.getInstance().createIRI("urn:s");
		final IRI p = SimpleValueFactory.getInstance().createIRI("urn:p");
		final IRI p2 = SimpleValueFactory.getInstance().createIRI("urn:p2");

		final IRI o = SimpleValueFactory.getInstance().createIRI("urn:o");
		final Literal l = SimpleValueFactory.getInstance().createLiteral("literal");

		Model aGraph = Models2.newModel();

		aGraph.add(s, p, o);
		aGraph.add(s, p2, l);

		assertEquals(o, Models2.getResource(aGraph, s, p).orElse(null));

		assertTrue(Models2.getResource(aGraph, s, p2).orElse(null) == null);

		assertTrue(Models2.getResource(aGraph, s, RDF.TYPE).orElse(null) == null);
	}

	@Test
	public void testGetBoolean() {
		final IRI s = SimpleValueFactory.getInstance().createIRI("urn:s");
		final IRI p = SimpleValueFactory.getInstance().createIRI("urn:p");
		final IRI p2 = SimpleValueFactory.getInstance().createIRI("urn:p2");
		final IRI p3 = SimpleValueFactory.getInstance().createIRI("urn:p3");
		final IRI p4 = SimpleValueFactory.getInstance().createIRI("urn:p4");

		final IRI o = SimpleValueFactory.getInstance().createIRI("urn:o");
		final Literal l = SimpleValueFactory.getInstance().createLiteral("literal");
		final Literal b = SimpleValueFactory.getInstance().createLiteral("true");
		final Literal b2 = SimpleValueFactory.getInstance().createLiteral(true);

		Model aGraph = Models2.newModel();

		aGraph.add(s, p, o);
		aGraph.add(s, p2, l);
		aGraph.add(s, p3, b);
		aGraph.add(s, p4, b2);

		assertTrue(Models2.getBooleanValue(aGraph, s, p).orElse(null) == null);
		assertTrue(Models2.getBooleanValue(aGraph, s, p2).orElse(null) == null);
		assertTrue(Models2.getBooleanValue(aGraph, s, RDF.TYPE).orElse(null) == null);
		assertTrue(Models2.getBooleanValue(aGraph, s, p3).orElse(false));
		assertTrue(Models2.getBooleanValue(aGraph, s, p4).orElse(false));
	}

//	/**
//	 * Test methods dealing with getting individuals from the graph
//	 */
//	@Test
//	public void testIndividuals() {
//		IRI aType = SimpleValueFactory.getInstance().createIRI("urn:some:type");
//		IRI aOtherType = SimpleValueFactory.getInstance().createIRI("urn:some:other:type");
//
//		Model aGraph = TestUtils.createRandomModel(20);
//
//		Set<Resource> aInds = Sets.newHashSet();
//
//		for (Statement aStmt : aGraph) {
//			aInds.add(aStmt.getSubject());
//		}
//
//		for (Resource aInd: aInds) {
//			assertTrue(aGraph.add(aInd, RDF.TYPE, aType));
//		}
//
//		Set<Resource> aIndsOfType = Sets.newHashSet(aInds);
//
//		Statement aStmt = TestUtils.createRandomStatement();
//		aInds.add(aStmt.getSubject());
//
//		assertTrue(aGraph.add(aStmt));
//		assertTrue(aGraph.add(aStmt.getSubject(), RDF.TYPE, aOtherType));
//
//		assertEquals(aInds, Models2.individuals(aGraph));
//
//		assertEquals(aIndsOfType,
//		             Models2.instancesOf(aGraph, aType));
//
//		assertEquals(Sets.newHashSet(aStmt.getSubject()),
//		             Models2.instancesOf(aGraph, aOtherType));
//	}
}