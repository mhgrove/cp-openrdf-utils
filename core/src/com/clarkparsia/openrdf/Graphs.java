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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.model.Graph;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.impl.ValueFactoryImpl;
import com.google.common.base.Objects;

/**
 * <p>Utility methods for working with Graph objects</p>
 *
 * @author Michael Grove
 * @version 0.4
 * @since 0.4
 */
public final class Graphs {

	/**
	 * Return the contents of the list serialized as an RDF list
	 * @param theResources the list
	 * @return the list as RDF
	 */
	public static ExtGraph toList(final Resource... theResources) {
		return toList(Arrays.asList(theResources));
	}

	/**
	 * Return the contents of the list serialized as an RDF list
	 * @param theResources the list
	 * @return the list as RDF
	 */
	public static ExtGraph toList(final List<Resource> theResources) {
		Resource aHead = ValueFactoryImpl.getInstance().createBNode();
		Resource aCurr = aHead;

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
	 * Returns a copy of the provided graph where all the statements belong to the specified context.  This will overwrite any existing contexts on the statements in the graph.
	 * @param theGraph the graph
	 * @param theResource the context for all the statements in the graph
	 * @return the new graph
	 */
	public static ExtGraph withContext(final Graph theGraph, final Resource theResource) {
		final ExtGraph aGraph = new ExtGraph();

		for (Statement aStmt : theGraph) {
			if (Objects.equal(aStmt.getContext(), theResource)) {
				aGraph.add(aStmt);
			}
			else {
				aGraph.add(ValueFactoryImpl.getInstance().createStatement(aStmt.getSubject(),
																		  aStmt.getPredicate(),
																		  aStmt.getObject(),
																		  theResource));
			}
		}

		return aGraph;
	}
}
