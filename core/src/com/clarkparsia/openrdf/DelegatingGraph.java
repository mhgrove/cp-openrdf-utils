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

import org.openrdf.model.Graph;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;

import java.util.Iterator;
import java.util.Collection;

/**
 * <p>Base class which implements the Graph interface, but delegates all operations to a sub-graph.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.2.4
 */
public class DelegatingGraph implements Graph {
	protected Graph mGraph;

	public DelegatingGraph() {
		this(new GraphImpl());
	}

	public DelegatingGraph(final Graph theGraph) {
		mGraph = theGraph;
	}

	/**
	 * @inheritDoc
	 */
	public ValueFactory getValueFactory() {
		return mGraph.getValueFactory();
	}

	/**
	 * @inheritDoc
	 */
	public boolean add(final Resource theResource, final URI theURI, final Value theValue, final Resource... theContexts) {
		if (!mGraph.match(theResource, theURI, theValue, theContexts).hasNext()) {
			return mGraph.add(theResource,  theURI, theValue, theContexts);
		}
		else {
			return false;
		}
	}

	/**
	 * @inheritDoc
	 */
	public Iterator<Statement> match(final Resource theResource, final URI theURI, final Value theValue, final Resource... theContexts) {
		return mGraph.match(theResource, theURI, theValue, theContexts);
	}

	/**
	 * @inheritDoc
	 */
	public int size() {
		return mGraph.size();
	}

	/**
	 * @inheritDoc
	 */
	public boolean isEmpty() {
		return mGraph.isEmpty();
	}

	/**
	 * @inheritDoc
	 */
	public boolean contains(final Object o) {
		return mGraph.contains(o);
	}

	/**
	 * @inheritDoc
	 */
	public Iterator<Statement> iterator() {
		return mGraph.iterator();
	}

	/**
	 * @inheritDoc
	 */
	public Object[] toArray() {
		return mGraph.toArray();
	}

	/**
	 * @inheritDoc
	 */
	public <T> T[] toArray(final T[] a) {
		return mGraph.toArray(a);
	}

	/**
	 * @inheritDoc
	 */
	public boolean add(final Statement e) {
		return !mGraph.contains(e) ? mGraph.add(e) : false;
	}

	/**
	 * @inheritDoc
	 */
	public boolean remove(final Object o) {
		return mGraph.remove(o);
	}

	/**
	 * @inheritDoc
	 */
	public boolean containsAll(final Collection<?> c) {
		return mGraph.containsAll(c);
	}

	/**
	 * @inheritDoc
	 */
	public boolean addAll(final Collection<? extends Statement> c) {
		boolean aGraphChanged = false;

		for (Statement aStmt : c) {
			boolean aAdded = add(aStmt);
			if (aAdded) {
				aGraphChanged = true;
			}
		}

		return aGraphChanged;
	}

	/**
	 * @inheritDoc
	 */
	public boolean removeAll(final Collection<?> c) {
		return mGraph.removeAll(c);
	}

	/**
	 * @inheritDoc
	 */
	public boolean retainAll(final Collection<?> c) {
		return mGraph.retainAll(c);
	}

	/**
	 * @inheritDoc
	 */
	public void clear() {
		mGraph.clear();
	}
}
