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

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;

import org.openrdf.model.Graph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 * <p>Implementation of the Sesame {@link Graph} interface which assumes Set semantics as opposed to GraphImpl which uses a LinkedList internally.
 * This implementation also assumes the user cares about context in statements and thus uses {@link ContextAwareValueFactory} for creating values and
 * statements.</p>
 *
 * @author  Michael Grove
 * @since	0.5
 * @version 0.9
 */
public final class SetGraph extends AbstractCollection<Statement> implements Graph {

	/**
	 * The contents of the graph as a Set
	 */
	private final Set<Statement> mStatements = Sets.newLinkedHashSet();

	/**
	 * The ValueFactory for this graph
	 */
	private final ValueFactory mValueFactory = new ContextAwareValueFactory();

	/**
	 * @inheritDoc
	 */
	@Override
	public Iterator<Statement> iterator() {
		return mStatements.iterator();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int size() {
		return mStatements.size();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean add(final Statement theStatement) {
		return mStatements.add(theStatement);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean contains(final Object theStatement) {
		return mStatements.contains(theStatement);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean remove(final Object theStatement) {
		return mStatements.remove(theStatement);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void clear() {
		mStatements.clear();
	}

	/**
	 * @inheritDoc
	 */
    @Override
    @Deprecated
	public ValueFactory getValueFactory() {
		return mValueFactory;
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public boolean add(final Resource theSubject, final URI thePredicate, final Value theObject, final Resource... theContexts) {
		boolean aAdded = false;

		for (Resource aContext : (theContexts == null || theContexts.length == 0 ? new Resource[] {null} : theContexts)) {
			aAdded |= mStatements.add(mValueFactory.createStatement(theSubject, thePredicate, theObject, aContext));
		}
		
		return aAdded;
	}

	/**
	 * @inheritDoc
	 */
    @Override
    @Deprecated
	public Iterator<Statement> match(final Resource theSubject, final URI thePredicate, final Value theObject, final Resource... theContexts) {
		return Graphs.filter(this, theSubject, thePredicate, theObject, theContexts).iterator();
	}

    /**
     * @inheritDoc
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        else if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SetGraph that = (SetGraph) o;

        return mStatements.equals(that.mStatements);
    }

    /**
     * @inheritDoc
     */
    @Override
    public int hashCode() {
        return mStatements.hashCode();
    }
}
