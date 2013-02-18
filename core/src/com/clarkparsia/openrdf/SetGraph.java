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

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
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
 * @author Michael Grove
 * @since	0.5
 * @version 0.5
 */
public final class SetGraph extends AbstractCollection<Statement> implements Graph {

	/**
	 * The contents of the graph as a Set
	 */
	private final Set<Statement> mStatements = Sets.newHashSet();

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
	public ValueFactory getValueFactory() {
		return mValueFactory;
	}

	/**
	 * @inheritDoc
	 */
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
	public Iterator<Statement> match(final Resource theSubject, final URI thePredicate, final Value theObject, final Resource... theContexts) {
		return Iterators.filter(iterator(), new Predicate<Statement>() {
			@Override
			public boolean apply(final Statement theStatement) {
				if (theSubject != null && !theSubject.equals(theStatement.getSubject())) {
					return false;
				}
				if (thePredicate != null && !thePredicate.equals(theStatement.getPredicate())) {
					return false;
				}
				if (theObject != null && !theObject.equals(theStatement.getObject())) {
					return false;
				}

				if (theContexts == null || theContexts.length == 0) {
					// no context specified, SPO were all equal, so this is equals as null/empty context is a wildcard
					return true;
				}
				else {
					Resource aContext = theStatement.getContext();

					for (Resource aCxt : theContexts) {
						if (aCxt == null && aContext == null) {
							return true;
						}
						if (aCxt != null && aCxt.equals(aContext)) {
							return true;
						}
					}

					return false;
				}
			}
		});
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
