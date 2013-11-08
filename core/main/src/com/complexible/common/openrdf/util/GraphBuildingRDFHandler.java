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

package com.complexible.common.openrdf.util;

import com.complexible.common.openrdf.model.SetGraph;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.model.Statement;
import org.openrdf.model.Graph;

/**
 * <p>Implementation of an RDFHandler which collects statements from the handler events and puts them into a Graph object.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.4.2
 */
public final class GraphBuildingRDFHandler extends RDFHandlerBase {

	/**
	 * The graph to collect statements in
	 */
	private final Graph mGraph;

	/**
	 * Create a new GraphBuildingRDFHandler
	 */
	public GraphBuildingRDFHandler() {
		this(new SetGraph());
	}

	/**
	 * Create a new GraphBuildingRDFHandler that will insert statements into the supplied Graph
	 * @param theGraph the graph to insert into
	 */
	public GraphBuildingRDFHandler(final Graph theGraph) {
		mGraph = theGraph;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void handleStatement(final Statement theStatement) throws RDFHandlerException {
		mGraph.add(theStatement);
	}

	/**
	 * Return the graph built from events fired to this handler
	 * @return the graph
	 */
	public Graph getGraph() {
		return mGraph;
	}

	/**
	 * Clear the underlying graph of all collected statements
	 */
	public void clear() {
		mGraph.clear();
	}
}
