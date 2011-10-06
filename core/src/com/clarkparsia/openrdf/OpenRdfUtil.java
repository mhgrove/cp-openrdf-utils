/*
 * Copyright (c) 2009-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import org.openrdf.model.vocabulary.RDF;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryConnection;

import org.openrdf.repository.sail.SailRepository;

import org.openrdf.sail.memory.MemoryStore;

import org.openrdf.model.URI;

import org.openrdf.model.Statement;

import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.BindingSet;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import info.aduna.iteration.Iteration;

import java.util.Iterator;
import java.util.Arrays;

import com.clarkparsia.openrdf.util.IterationIterator;
import com.clarkparsia.openrdf.util.AdunaIterations;
import com.google.common.collect.Lists;

/**
 * <p>Utility methods for working with the OpenRDF Sesame API.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.4
 */
public class OpenRdfUtil {
	/**
	 * the logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenRdfUtil.class);
	
	/**
	 * Create a simple in-memory {@link Repository}
	 * @return an in memory Repository
	 */
	public static ExtRepository createInMemoryRepo() {
		try {
			Repository aRepo = new SailRepository(new MemoryStore());

			aRepo.initialize();

			return new ExtRepository(aRepo);
		}
		catch (RepositoryException e) {
			// impossible?
			throw new RuntimeException(e);
		}
	}

	/**
	 * Return the TupleQueryResult as an {@link Iterable} of {@link BindingSet BindingSets}
	 * @param theResult the TupleQueryResult to wrap
	 * @return the TupleQueryResult as an Iterable
	 * @deprecated use AdunaIterations.iterable
	 */
	@Deprecated
	public static <T, E extends Exception> Iterable<T> iterable(final Iteration<T,E> theResult) {
		return new Iterable<T>() {
			public Iterator<T> iterator() {
				return AdunaIterations.iterator(theResult);
			}
		};
	}

	/**
	 * Return the list of Statements as a Graph
	 * @param theStatements the statements that will make up the Graph
	 * @return a Graph containing all the provided statements
	 */
	public static ExtGraph asGraph(final Statement... theStatements) {
		ExtGraph aGraph = new ExtGraph();

		aGraph.addAll(Arrays.asList(theStatements));

		return aGraph;
	}

	/**
	 * Return the Iterable of Statements as a Graph
	 * @param theStatements the statements that will make up the Graph
	 * @return a Graph containing all the provided statements
	 */
	public static ExtGraph asGraph(final Iterable<Statement> theStatements) {
		final ExtGraph aGraph = new ExtGraph();

		for (Statement aStmt : theStatements) {
			aGraph.add(aStmt);
		}

		return aGraph;
	}

	/**
	 * Quietly close the connection object
	 * @param theConn the connection to close
	 */
	public static void close(RepositoryConnection theConn) {
		if (theConn != null) {
			try {
				theConn.commit();

				theConn.close();
			}
			catch (RepositoryException e) {
				LOGGER.error("There was an error while closing the RepositoryConnection.", e);
			}
		}
	}

    public static boolean isType(final ExtGraph theGraph, final URI theSubj, final URI theType) {
        return theGraph.getValues(theSubj, RDF.TYPE).contains(theType);
    }

    public static boolean isType(final ExtRepository theRepository, final URI theSubj, final URI theType) {
        return Lists.newArrayList(theRepository.getValues(theSubj, RDF.TYPE)).contains(theType);
    }
}
