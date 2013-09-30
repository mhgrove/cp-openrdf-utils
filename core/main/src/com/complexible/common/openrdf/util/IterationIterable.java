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

package com.complexible.common.openrdf.util;

import info.aduna.iteration.Iteration;

import java.util.Iterator;

import com.google.common.base.Supplier;

/**
 * <p>Wrapper around an Iteration to expose it as an Iterable.  Whether or not this is one time use only is up to the implementation of the Suppler of the Iteration.</p>
 *
 * @author Michael Grove
 * @version 0.4
 * @since 0.4
 */
public final class IterationIterable<T> implements Iterable<T> {

	/**
	 * The Sesame iteration
	 */
	private Supplier<? extends Iteration<T,?>> mIteration;

	/**
	 * Create a new IterationIterator
	 * @param theIteration the iteration to wrap
	 */
	public IterationIterable(final Supplier<? extends Iteration<T,?>> theIteration) {
		mIteration = theIteration;
	}

	/**
	 * @inheritDoc
	 */
	public Iterator<T> iterator() {
		return new IterationIterator<T>(mIteration.get());
	}
}
