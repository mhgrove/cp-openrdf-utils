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

import java.util.Iterator;

import com.complexible.common.openrdf.model.ImmutableGraph;
import com.complexible.common.openrdf.model.SetGraph;
import org.junit.Test;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <p>Tests for {@link ImmutableGraph}</p>
 *
 * @author	Michael Grove
 * @since	0.8
 * @version 1.0
 */
public class TestImmutableGraph {
	@Test
	public void testDontRecreateImmutable() {
		Graph aGraph = ImmutableGraph.of(new SetGraph());

		assertTrue(aGraph == ImmutableGraph.of(aGraph));
	}

	@Test
	public void testCantAdd() {
		Graph aGraph = ImmutableGraph.of(new SetGraph());

		try {
			aGraph.add(TestUtils.createRandomStatement());
			fail("Should not have been able to add a statement");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}

		try {
			aGraph.addAll(TestUtils.createRandomGraph(5));
			fail("Should not have been able to add a graph");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}

		try {
			final Statement aStmt = TestUtils.createRandomStatement();
			aGraph.add(aStmt.getSubject(), aStmt.getPredicate(), aStmt.getObject());

			fail("Should not have been able to add a statement");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}
	}

	@Test
	public void testCantRemove() {
		Graph aGraph = ImmutableGraph.of(new SetGraph());

		try {
			aGraph.remove(TestUtils.createRandomStatement());
			fail("Should not have been able to remove a statement");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}

		try {
			aGraph.removeAll(TestUtils.createRandomGraph(5));
			fail("Should not have been able to remove a graph of statements");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}


		try {
			aGraph.retainAll(TestUtils.createRandomGraph(5));
			fail("Should not have been able to remove statements via retain");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCantModifyFromIterator() {
		Graph aGraph = ImmutableGraph.of(TestUtils.createRandomGraph(5));

		Iterator aIter = aGraph.iterator();

		aIter.next();
		aIter.remove();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testCantClear() {
		Graph aGraph = ImmutableGraph.of(new SetGraph());

		aGraph.clear();
	}
}
