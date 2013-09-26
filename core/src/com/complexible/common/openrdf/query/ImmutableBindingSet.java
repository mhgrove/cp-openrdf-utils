// Copyright (c) 2010 - 2013, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// For more information about licensing and copyright of this software, please contact
// inquiries@clarkparsia.com or visit http://stardog.com

package com.complexible.common.openrdf.query;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;

/**
 * <p>An immutable {@link BindingSet}</p>
 *
 * @author  Michael Grove
 * @since   1.1
 * @version 1.1
 */
public final class ImmutableBindingSet extends DelegatingBindingSet {

    private final ImmutableSet<String> mBindingNames;

    public ImmutableBindingSet(final BindingSet theBindingSet) {
        super(theBindingSet);
        mBindingNames = ImmutableSet.copyOf(theBindingSet.getBindingNames());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Iterator<Binding> iterator() {
        return Iterators.unmodifiableIterator(super.iterator());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Set<String> getBindingNames() {
        return mBindingNames;
    }
}
