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

import org.openrdf.query.BindingSet;
import org.openrdf.query.Binding;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.Resource;
import org.openrdf.model.Literal;
import org.openrdf.model.BNode;

import java.util.Iterator;
import java.util.Set;

/**
 * <p>Class which extends a BindingSet to give cast-safe access to values, such as getURI, getResource, and getLiteral</p>
 *
 * @author Michael Grove
 */
public class ExtBindingSet implements BindingSet {

	/**
	 * The underlying BindingSet
	 */
	private final BindingSet mBindingSet;

	/**
	 * Create a new EBindingSet
	 * @param theBindingSet the source BindingSet
	 */
	public ExtBindingSet(final BindingSet theBindingSet) {
		mBindingSet = theBindingSet;
	}

	/**
	 * @inheritDoc
	 */
	public Iterator<Binding> iterator() {
		return mBindingSet.iterator();
	}

	/**
	 * @inheritDoc
	 */
	public Set<String> getBindingNames() {
		return mBindingSet.getBindingNames();
	}

	/**
	 * @inheritDoc
	 */
	public Binding getBinding(final String theKey) {
		return mBindingSet.getBinding(theKey);
	}

	/**
	 * @inheritDoc
	 */
	public boolean hasBinding(final String theKey) {
		return mBindingSet.hasBinding(theKey);
	}

	/**
	 * @inheritDoc
	 */
	public Value getValue(final String theKey) {
		return mBindingSet.getValue(theKey);
	}

	/**
	 * @inheritDoc
	 */
	public int size() {
		return mBindingSet.size();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		return mBindingSet.hashCode();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean equals(Object theObj) {
		return mBindingSet.equals(theObj);
	}

	public URI getURI(String theKey) {
		Value aVal = getValue(theKey);
		if (aVal instanceof URI) {
			return (URI) aVal;
		}
		else {
			return null;
		}
	}

	public Literal getLiteral(String theKey) {
		Value aVal = getValue(theKey);
		if (aVal instanceof Literal) {
			return (Literal) aVal;
		}
		else {
			return null;
		}
	}

	public Resource getResource(String theKey) {
		Value aVal = getValue(theKey);
		if (aVal instanceof Resource) {
			return (Resource) aVal;
		}
		else {
			return null;
		}
	}

	public BNode getBNode(String theKey) {
		Value aVal = getValue(theKey);
		if (aVal instanceof BNode) {
			return (BNode) aVal;
		}
		else {
			return null;
		}
	}
}
