/*
 * Copyright (c) 2009-2013 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import com.complexible.common.openrdf.model.ContextAwareValueFactory;
import com.complexible.common.openrdf.model.Graphs;
import com.complexible.common.openrdf.model.SetGraph;
import org.junit.Test;
import org.openrdf.model.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * <p></p>
 *
 * @author	Michael Grove
 * @since	0.8
 * @version 0.8
 */
public class TestSetGraph {

	/**
	 * Test that de-duping works
	 */
	@Test
	public void testAddNoDupe() {
		SetGraph aGraph = new SetGraph();

		Statement aStmt = TestUtils.createRandomStatement();

		assertTrue(aGraph.add(aStmt));

		assertEquals(1, aGraph.size());

		assertFalse(aGraph.add(aStmt));

		assertEquals(1, aGraph.size());

		assertFalse(aGraph.addAll(Graphs.newGraph(aStmt)));

		assertEquals(1, aGraph.size());
	}

	@Test
    @SuppressWarnings("deprecation")
	public void testValueFactory() {
		assertTrue(new SetGraph().getValueFactory() instanceof ContextAwareValueFactory);
	}
}
