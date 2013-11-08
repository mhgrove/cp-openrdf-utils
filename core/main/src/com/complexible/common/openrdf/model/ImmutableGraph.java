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

package com.complexible.common.openrdf.model;

import java.util.Collection;
import java.util.Iterator;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import com.google.common.collect.Iterators;

/**
 * <p>An immutable version of an OpenRdf {@link org.openrdf.model.Graph}</p>
 *
 * @author	Michael Grove
 * @since	0.4
 * @version	0.4
 */
public final class ImmutableGraph extends DelegatingGraph {

	/**
	 * Create a new ImmutableGraph
	 * @param theGraph the graph that is to be immutable
	 */
	private ImmutableGraph(final Graph theGraph) {
		super(theGraph);
	}

	/**
	 * Return an immutable version of the graph
	 * @param theGraph	the graph
	 * @return 			an immutable version of the graph
	 */
	public static ImmutableGraph of(final Graph theGraph) {
		if (theGraph instanceof ImmutableGraph) {
			return (ImmutableGraph) theGraph;
		}
		else {
			return new ImmutableGraph(theGraph);
		}
	}

	/**
	 * Return an immutable version of the statements
	 * @param theStatements	the graph
	 * @return				an immutable version of the statements
	 */
	public static ImmutableGraph of(final Statement... theStatements) {
		return new ImmutableGraph(Graphs.newGraph(theStatements));
	}

	/**
	 * Return an immutable version of the statements
	 * @param theStatements	the graph
	 * @return 				an immutable version of the statements
	 */
	public static ImmutableGraph of(final Iterator<Statement> theStatements) {
		return new ImmutableGraph(Graphs.newGraph(theStatements));
	}

	/**
	 * Return an immutable version of the statements
	 * @param theStatements	the graph
	 * @return 				an immutable version of the statements
	 */
	public static ImmutableGraph of(final Iterable<Statement> theStatements) {
		return new ImmutableGraph(Graphs.newGraph(theStatements));
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean add(final Statement e) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean add(final Resource theResource, final URI theURI, final Value theValue, final Resource... theContexts) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean addAll(final Collection<? extends Statement> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean remove(final Object o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Iterator<Statement> iterator() {
		return Iterators.unmodifiableIterator(super.iterator());
	}
}
