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

package com.clarkparsia.openrdf;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;
import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.model.Graph;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.impl.GraphImpl;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.rio.RDFParseException;

/**
 * <p>Utility methods for working with Graph objects</p>
 *
 * @author	Michael Grove
 * @since	0.4
 * @version	0.8
 */
public final class Graphs {

	/**
	 * Return the contents of the list serialized as an RDF list
	 * @param theResources	the list
	 * @return				the list as RDF
	 */
	public static ExtGraph toList(final Resource... theResources) {
		return toList(Arrays.asList(theResources));
	}

	/**
	 * Create a Graph from the RDF in the specified file
	 *
	 * @param theFile	the file to read the RDF from
	 * @return			a new graph containing the RDF from the file
	 *
	 * @throws IOException			if there was an error reading the file
	 * @throws RDFParseException	if the file did not contain valid RDF
	 */
	public static ExtGraph of(final File theFile) throws IOException, RDFParseException {
		return (ExtGraph) OpenRdfIO.readGraph(theFile);
	}

	/**
	 * Create a Sesame graph from the GraphQueryResult.  If the invocation is successful, the query result is closed before returning the result.
	 * @param theResult	the result of the query
	 * @return			the graph built from the result
	 *
	 * @throws org.openrdf.query.QueryEvaluationException if there was an error while creating the graph from the query result
	 */
	public static ExtGraph newGraph(GraphQueryResult theResult) throws QueryEvaluationException {
		ExtGraph aGraph = new ExtGraph();

		while (theResult.hasNext()) {
			aGraph.add(theResult.next());
		}

		theResult.close();

		return aGraph;
	}

	/**
	 * Return the contents of the list serialized as an RDF list
	 * @param theResources	the list
	 * @return				the list as RDF
	 */
	public static ExtGraph toList(final List<Resource> theResources) {
		Resource aCurr = ValueFactoryImpl.getInstance().createBNode();

		int i = 0;
		ExtGraph aGraph = new ExtGraph();
		for (Resource r : theResources) {
			Resource aNext = ValueFactoryImpl.getInstance().createBNode();
			aGraph.add(aCurr, RDF.FIRST, r);
			aGraph.add(aCurr, RDF.REST, ++i < theResources.size() ? aNext : RDF.NIL);
			aCurr = aNext;
		}

		return aGraph;
	}

	/**
	 * Return the list of Statements as a Graph
	 * @param theStatements the statements that will make up the Graph
	 * @return a Graph containing all the provided statements
	 */
	public static ExtGraph newGraph(final Statement... theStatements) {
		ExtGraph aGraph = new ExtGraph();

		aGraph.addAll(Arrays.asList(theStatements));

		return aGraph;
	}

	/**
	 * Return the Iterator of Statements as a Graph
	 * @param theStatements the statements that will make up the Graph
	 * @return a Graph containing all the provided statements
	 */
	public static ExtGraph newGraph(final Iterator<Statement> theStatements) {
		final ExtGraph aGraph = new ExtGraph();

		while (theStatements.hasNext()) {
			aGraph.add(theStatements.next());
		}

		return aGraph;
	}

	/**
	 * Return the Iterable of Statements as a Graph
	 * @param theStatements the statements that will make up the Graph
	 * @return a Graph containing all the provided statements
	 */
	public static ExtGraph newGraph(final Iterable<Statement> theStatements) {
		final ExtGraph aGraph = new ExtGraph();

		for (Statement aStmt : theStatements) {
			aGraph.add(aStmt);
		}

		return aGraph;
	}

	/**
	 * Return a new {@link #contextGraph} whose contents are the statements contained in the array.
	 * @param theStatements the statements for the new graph
	 * @return the new graph
	 */
	public static Graph newContextGraph(final Statement... theStatements) {
		return newContextGraph(Iterators.forArray(theStatements));
	}

	/**
	 * Return a new {@link #contextGraph} whose contents are the statements contained in the iterator.
	 * @param theStatements the statements for the new graph
	 * @return the new graph
	 */
	public static Graph newContextGraph(final Iterator<Statement> theStatements) {
		Graph aGraph = contextGraph();

		while (theStatements.hasNext()) {
			aGraph.add(theStatements.next());
		}

		return aGraph;
	}

	/**
	 * Return a new {@link #contextGraph} whose contents are the statements contained in the iterable.
	 * @param theStatements the statements for the new graph
	 * @return the new graph
	 */
	public static Graph newContextGraph(final Iterable<Statement> theStatements) {
		return newContextGraph(theStatements.iterator());
	}

	/**
	 * Returns a copy of the provided graph where all the statements belong to the specified context.
	 * This will overwrite any existing contexts on the statements in the graph.
	 * 
	 * @param theGraph the graph
	 * @param theResource the context for all the statements in the graph
	 * @return the new graph
	 */
	public static Graph withContext(final Graph theGraph, final Resource theResource) {
		final Graph aGraph = contextGraph();

		for (Statement aStmt : theGraph) {
			if (Objects.equal(aStmt.getContext(), theResource)) {
				aGraph.add(aStmt);
			}
			else {
				aGraph.add(aStmt.getSubject(),
						   aStmt.getPredicate(),
						   aStmt.getObject(),
						   theResource);
			}
		}

		return aGraph;
	}

	/**
	 * Return a new Graph which is the union of all the provided graphs.  Be careful if you are using statements w/ a context as the equals method for Statement
	 * does not take into account context, so two statements with the same SPO, but different contexts will be considered the same statement and only one will
	 * be included in the union.  You can use {@link ContextAwareStatement} which implements equals & hashcode taking into account the context if you need to use
	 * Statements with contexts where context is considered in this way.
	 * 
	 * @param theGraphs the graphs to union
	 * @return			the union of the graphs
	 */
	public static Graph union(final Graph... theGraphs) {
		SetGraph aSetGraph = new SetGraph();

		for (Graph aGraph : theGraphs) {
			aSetGraph.addAll(aGraph);
		}

		return aSetGraph;
	}

	/**
	 * Return a new (empty) graph whose ValueFactory is an instance of {@link com.clarkparsia.openrdf.ContextAwareValueFactory}
	 * @return a new "context aware" graph
	 */
	public static Graph contextGraph() {
		return new ExtGraph() {
			@Override
			public boolean add(final Statement e) {
				return super.add(e.getSubject(), e.getPredicate(), e.getObject(), e.getContext());
			}

			private final ValueFactory mValueFactory = new ContextAwareValueFactory();
			@Override
			public ValueFactory getValueFactory() {
				return mValueFactory;
			}
		};
	}

	/**
	 * Return a new {@link #contextGraph context graph} copying the contents of the provided graph into the new graph so that all of its
	 * statements are instances of {@link com.clarkparsia.openrdf.ContextAwareStatement}.
	 *
	 * @param theGraph	the graph to copy
	 * @return			the copied graph, but context aware
	 */
	public static Graph contextGraph(final Graph theGraph) {
		Graph aGraph = contextGraph();

		for (Statement aStmt : theGraph) {
			aGraph.add(aStmt);
		}

		return aGraph;
	}
}
