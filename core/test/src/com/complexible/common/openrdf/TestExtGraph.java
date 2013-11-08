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

import com.complexible.common.openrdf.model.ExtGraph;
import com.complexible.common.openrdf.model.Graphs;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.openrdf.model.Graph;
import org.openrdf.model.vocabulary.RDF;
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
 * @since	0.4
 * @version 0.8
 */
public class TestExtGraph {

	/**
	 * Test methods dealing with getting individuals from the graph
	 */
	@Test
	public void testIndividuals() {
		URI aType = ValueFactoryImpl.getInstance().createURI("urn:some:type");
		URI aOtherType = ValueFactoryImpl.getInstance().createURI("urn:some:other:type");

		ExtGraph aGraph = Graphs.extend(TestUtils.createRandomGraph(20));

		Set<Resource> aInds = Sets.newHashSet();

		for (Statement aStmt : aGraph) {
			aInds.add(aStmt.getSubject());
		}

		for (Resource aInd: aInds) {
			assertTrue(aGraph.add(aInd, RDF.TYPE, aType));
		}

		Set<Resource> aIndsOfType = Sets.newHashSet(aInds);

		Statement aStmt = TestUtils.createRandomStatement();
		aInds.add(aStmt.getSubject());

		assertTrue(aGraph.add(aStmt));
		assertTrue(aGraph.add(aStmt.getSubject(), RDF.TYPE, aOtherType));

		assertEquals(aInds, aGraph.getIndividuals());

		assertEquals(aIndsOfType,
					 aGraph.getInstancesOf(aType));

		assertEquals(Sets.newHashSet(aStmt.getSubject()),
					 aGraph.getInstancesOf(aOtherType));
	}
}
