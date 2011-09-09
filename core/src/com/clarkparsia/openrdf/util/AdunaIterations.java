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

package com.clarkparsia.openrdf.util;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.Iterations;
import com.google.common.base.Predicate;

/**
 * <p>Utility methods for Aduna {@link info.aduna.iteration.Iteration Iterations} not already present in {@link Iterations}</p>
 *
 * @author Michael Grove
 * @version 0.4
 * @since 0.4
 */
public class AdunaIterations {
	/**
	 * the logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AdunaIterations.class);

	/**
	 * Quietly close the iteration
	 * @param theCloseableIteration the iteration to close
	 */
	public static void closeQuietly(final CloseableIteration<?,?> theCloseableIteration) {
		try {
			if (theCloseableIteration != null) {
				Iterations.closeCloseable(theCloseableIteration);
			}
		}
		catch (Exception e) {
			LOGGER.warn("Ignoring error while closing iteration.", e);
		}
	}

	/**
	 * Apply the predicate to everything in the Iteration
	 * @param theIter the iteratino
	 * @param theEach the predicate to apply
	 * @param <T> the type of objects in the iteration
	 * @param <E> the type of exception that can be thrown
	 * @throws E the exception
	 */
	public static <T, E extends Exception> void each(final CloseableIteration<T, E> theIter, final Predicate<T> theEach) throws E {
		try {
			while (theIter.hasNext()) {
				theEach.apply(theIter.next());
			}
		}
		finally {
			theIter.close();
		}
	}
}
