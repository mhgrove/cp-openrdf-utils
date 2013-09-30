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

package com.complexible.common.openrdf.util;

import info.aduna.iteration.Iteration;
import info.aduna.iteration.CloseableIteration;

import java.util.Iterator;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * <p>Implementation of the Iterator interface to wrap a Sesame Iteration object. If the Iteration is a
 * {@link CloseableIteration} then the close method will be called when the last element is pulled from the Iterator.
 * If you do not iterate over the entire object, you will need to {@link #getIteration "get the iteration"} and
 * close it yourself.</p>
 *
 * @author Michael Grove
 * @since 0.2
 * @version 0.4
 */
public final class IterationIterator<T> implements Iterator<T> {
	/**
	 * the logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IterationIterator.class);

	/**
	 * The Sesame iteration
	 */
	private Iteration<T,?> mIteration;

	/**
	 * Create a new IterationIterator
	 * @param theIteration the iteration to wrap
	 */
	public IterationIterator(final Iteration<T, ?> theIteration) {
		mIteration = theIteration;
	}

	/**
	 * Return the underlying iteration
	 * @return the iteratino
	 */
	public Iteration<T,?> getIteration() {
		return mIteration;
	}

	/**
	 * @inheritDoc
	 */
	public boolean hasNext() {
		try {
			return mIteration.hasNext();
		}
		catch (Exception e) {
			LOGGER.error("Iteration.hasNext exception!",e);
			return false;
		}
	}

	/**
	 * @inheritDoc
	 */
	public T next() {
		try {
			T aObj = mIteration.next();

			if (!hasNext() && mIteration instanceof CloseableIteration) {
				((CloseableIteration)mIteration).close();
			}

			return aObj;
		}
		catch (Exception e) {
			// TODO: get better behavior for iterations throwing their native exception type
			throw new RuntimeException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void remove() {
		try {
			mIteration.remove();
		}
		catch (Exception e) {
			LOGGER.error("Iteration.remove exception!", e);
		}
	}
}
