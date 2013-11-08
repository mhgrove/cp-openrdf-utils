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

package com.complexible.common.openrdf.model;

import org.openrdf.model.Graph;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.Statement;

import java.util.Iterator;
import java.util.Collection;

/**
 * <p>Base class which implements the Graph interface, but delegates all operations to a sub-graph.</p>
 *
 * @author  Michael Grove
 * @since   0.1
 * @version 1.1
 */
public abstract class DelegatingGraph implements Graph {
	protected final Graph mGraph;

	public DelegatingGraph(final Graph theGraph) {
		mGraph = theGraph;
	}

	/**
	 * @inheritDoc
	 */
    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
	public ValueFactory getValueFactory() {
		return mGraph.getValueFactory();
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public boolean add(final Resource theResource, final URI theURI, final Value theValue, final Resource... theContexts) {
		return mGraph.add(theResource,  theURI, theValue, theContexts);
	}

	/**
	 * @inheritDoc
	 */
    @Override
    @Deprecated
    @SuppressWarnings("deprecation")
	public Iterator<Statement> match(final Resource theResource, final URI theURI, final Value theValue, final Resource... theContexts) {
		return mGraph.match(theResource, theURI, theValue, theContexts);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public int size() {
		return mGraph.size();
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public boolean isEmpty() {
		return mGraph.isEmpty();
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public boolean contains(final Object o) {
		return mGraph.contains(o);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Iterator<Statement> iterator() {
		return mGraph.iterator();
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Object[] toArray() {
		return mGraph.toArray();
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public <T> T[] toArray(final T[] a) {
		return mGraph.toArray(a);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public boolean add(final Statement e) {
		return mGraph.add(e);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public boolean remove(final Object o) {
		return mGraph.remove(o);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public boolean containsAll(final Collection<?> c) {
		return mGraph.containsAll(c);
	}

	/**
	 * @inheritDoc
	 */
    @Override
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
    @Override
	public boolean removeAll(final Collection<?> c) {
		return mGraph.removeAll(c);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public boolean retainAll(final Collection<?> c) {
		return mGraph.retainAll(c);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public void clear() {
		mGraph.clear();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final DelegatingGraph that = (DelegatingGraph) o;

		if (mGraph != null ? !mGraph.equals(that.mGraph) : that.mGraph != null) {
			return false;
		}

		return true;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		return mGraph != null ? mGraph.hashCode() : 0;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String toString() {
		return mGraph.toString();
	}
}
