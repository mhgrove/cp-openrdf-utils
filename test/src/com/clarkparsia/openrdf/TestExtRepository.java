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
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * <p>Tests for ExtRepository</p>
 *
 * @author Michael Grove
 * @version 0.4
 * @since 0.4
 */
public class TestExtRepository {

	/**
	 * Test for clear
	 * @throws RepositoryException test failure
	 */
	@Test
	public void testClear() throws RepositoryException {
		ExtRepository aRepo = TestUtils.createRandomRepository();

		assertTrue(aRepo.size() > 0);

		aRepo.clear();

		assertEquals(0, aRepo.size());
	}

	/**
	 * Test getting the size of a repository
	 * @throws RepositoryException test failure
	 */
	@Test
	public void testSize() throws RepositoryException {
		ExtRepository aRepo = OpenRdfUtil.createInMemoryRepo();

		Graph aGraph = TestUtils.createRandomGraph(25);

		aRepo.add(aGraph);

		assertEquals(25,
					 aRepo.size());
	}

	/**
	 * Test adding a graph to a repository
	 * @throws RepositoryException the repo
	 */
	@Test
	public void testAddGraph() throws RepositoryException {
		ExtRepository aRepo = OpenRdfUtil.createInMemoryRepo();

		Graph aGraph = TestUtils.createRandomGraph(25);

		aRepo.add(aGraph);

		assertEquals(25,
					 aRepo.size());

		for (Statement aStmt : aGraph) {
			assertTrue(aRepo.contains(aStmt));
		}
	}

	/**
	 * Test removing a graph from a repository
	 * @throws RepositoryException the repo
	 */
	@Test
	public void testRemoveGraph() throws RepositoryException {
		ExtRepository aRepo = OpenRdfUtil.createInMemoryRepo();

		Graph aGraph = TestUtils.createRandomGraph(25);

		aRepo.add(aGraph);

		assertEquals(25,
					 aRepo.size());

		for (Statement aStmt : aGraph) {
			assertTrue(aRepo.contains(aStmt));
		}

		aRepo.remove(aGraph);

		assertEquals(0,
					 aRepo.size());

		for (Statement aStmt : aGraph) {
			assertFalse(aRepo.contains(aStmt));
		}
	}


	/**
	 * Test adding a graph to a repository
	 * @throws Exception the repo
	 */
	@Test
	public void testAddFromStream() throws Exception {
		ExtRepository aRepo = OpenRdfUtil.createInMemoryRepo();

		ExtGraph aGraph = TestUtils.createRandomGraph(25);

		ByteArrayOutputStream aOut = new ByteArrayOutputStream();

		aGraph.write(aOut, RDFFormat.TURTLE);

		aRepo.add(new ByteArrayInputStream(aOut.toByteArray()), RDFFormat.TURTLE);

		assertEquals(25,
					 aRepo.size());

		for (Statement aStmt : aGraph) {
			assertTrue(aRepo.contains(aStmt));
		}
	}
}
