/*
 * Copyright (c) 2009-2011 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.clarkparsia.openrdf;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.openrdf.repository.RepositoryException;
import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * <p>Tests for ExtGraph</p>
 *
 * @author Michael Grove
 * @version 0.4
 * @since 0.4
 */
public class TestExtGraph {

	/**
	 * Test for clear
	 */
	@Test
	public void testClear() {
		ExtGraph aGraph = TestUtils.createRandomGraph();

		assertTrue(aGraph.size() > 0);

		aGraph.clear();

		assertEquals(0, aGraph.size());
	}

	/**
	 * Test that de-duping works
	 */
	@Test
	public void testAddNoDupe() {
		ExtGraph aGraph = new ExtGraph();

		Statement aStmt = TestUtils.createRandomStatement();

		assertTrue(aGraph.add(aStmt));

		assertEquals(1, aGraph.size());

		assertFalse(aGraph.add(aStmt));

		assertEquals(1, aGraph.size());

		assertFalse(aGraph.addAll(Graphs.newGraph(aStmt)));

		assertEquals(1, aGraph.size());
	}

	/**
	 * Test methods dealing with getting individuals from the graph
	 */
	@Test
	public void testIndividuals() {
		URI aType = ValueFactoryImpl.getInstance().createURI("urn:some:type");
		URI aOtherType = ValueFactoryImpl.getInstance().createURI("urn:some:other:type");

		ExtGraph aGraph = TestUtils.createRandomGraph(20);

		Set<Resource> aInds = Sets.newHashSet();

		for (Statement aStmt : aGraph) {
			aInds.add(aStmt.getSubject());
		}

		for (Resource aInd: aInds) {
			assertTrue(aGraph.addType(aInd, aType));
		}

		Set<Resource> aIndsOfType = Sets.newHashSet(aInds);

		Statement aStmt = TestUtils.createRandomStatement();
		aInds.add(aStmt.getSubject());
		assertTrue(aGraph.add(aStmt));
		assertTrue(aGraph.addType(aStmt.getSubject(), aOtherType));

		assertEquals(aInds,
					 aGraph.listIndividuals());

		assertEquals(aIndsOfType,
					 aGraph.instancesOf(aType));

		assertEquals(Sets.newHashSet(aStmt.getSubject()),
					 aGraph.instancesOf(aOtherType));
	}
}
