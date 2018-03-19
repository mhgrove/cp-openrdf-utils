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

import com.complexible.common.openrdf.model.ModelIO;
import com.complexible.common.openrdf.repository.Repositories;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * <p>Tests for ExtRepository</p>
 *
 * @author Michael Grove
 * @version 0.4
 * @since 0.4
 */
public class TestRepositories {

	/**
	 * Create a simple in-memory {@link Repository} which is already initialized
	 *
	 * @return an in memory Repository
	 */
	public static Repository createInMemoryRepo() {
		try {
			Repository aRepo = new SailRepository(new MemoryStore());

			aRepo.initialize();

			return aRepo;
		}
		catch (RepositoryException e) {
			// impossible?
			throw new AssertionError(e);
		}
	}

	/**
	 * Test for clear
	 * @throws RepositoryException test failure
	 */
	@Test
	public void testClear() throws RepositoryException {
		Repository aRepo = TestUtils.createRandomRepository();

		assertTrue(Repositories.size(aRepo) > 0);

		Repositories.clear(aRepo);

		assertEquals(0, Repositories.size(aRepo));
	}

	/**
	 * Test getting the size of a repository
	 * @throws RepositoryException test failure
	 */
	@Test
	public void testSize() throws RepositoryException {
		Repository aRepo = createInMemoryRepo();

		Model aGraph = TestUtils.createRandomModel(25);

		Repositories.add(aRepo, aGraph);

		assertEquals(25, Repositories.size(aRepo));
	}

	/**
	 * Test adding a graph to a repository
	 * @throws RepositoryException the repo
	 */
	@Test
	public void testAddGraph() throws RepositoryException {
		Repository aRepo = createInMemoryRepo();

		Model aGraph = TestUtils.createRandomModel(25);

		Repositories.add(aRepo, aGraph);

		assertEquals(25, Repositories.size(aRepo));

		for (Statement aStmt : aGraph) {
			assertTrue(Repositories.contains(aRepo, aStmt));
		}
	}

	/**
	 * Test removing a graph from a repository
	 * @throws RepositoryException the repo
	 */
	@Test
	public void testRemoveGraph() throws RepositoryException {
		Repository aRepo = createInMemoryRepo();

		Model aGraph = TestUtils.createRandomModel(25);

		Repositories.add(aRepo, aGraph);

		assertEquals(25, Repositories.size(aRepo));

		for (Statement aStmt : aGraph) {
			assertTrue(Repositories.contains(aRepo, aStmt));
		}

		Repositories.remove(aRepo, aGraph);

		assertEquals(0, Repositories.size(aRepo));

		for (Statement aStmt : aGraph) {
			assertFalse(Repositories.contains(aRepo, aStmt));
		}
	}


	/**
	 * Test adding a graph to a repository
	 * @throws Exception the repo
	 */
	@Test
	public void testAddFromStream() throws Exception {
		Repository aRepo = createInMemoryRepo();

		Model aGraph = TestUtils.createRandomModel(25);

		ByteArrayOutputStream aOut = new ByteArrayOutputStream();

		ModelIO.write(aGraph, aOut, RDFFormat.TURTLE);

		Repositories.add(aRepo, new ByteArrayInputStream(aOut.toByteArray()), RDFFormat.TURTLE);

		assertEquals(25, Repositories.size(aRepo));

		for (Statement aStmt : aGraph) {
			assertTrue(Repositories.contains(aRepo, aStmt));
		}
	}
}
