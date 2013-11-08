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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;

/**
 * <p>Convenience interface for a Sesame Graph for applying some of the Guava functional objects to a Graph.</p>
 *
 * @author Michael Grove
 * @since	0.8
 * @version 0.8
 */
public interface FunctionalGraph extends Graph {

	/**
	 * Create a {@link com.google.common.base.Predicate filtered} copy of the graph
	 *
	 * @param thePredicate	the predicate to use for filtering
	 * @return				the filtered graph
	 */
	public Graph filter(final Predicate<Statement> thePredicate);

	/**
	 * {@link com.google.common.base.Function Transform} the contents of the graph.  This returns a copy of the original
	 * graph with the transformation applied
	 *
	 * @param theFunction	the function for the transform
	 * @return				the transformed graph
	 */
	public Graph transform(final Function<Statement, Statement> theFunction);

	/**
	 * Find a {@link Statement} which satisfies the given {@link Predicate}
	 *
	 * @param thePredicate	the predicate
	 * @return				{@link com.google.common.base.Optional Optionally}, the first Statement to satisfy the Predicate, or an absent Optional if none do
	 */
	public Optional<Statement> find(final Predicate<Statement> thePredicate);

	/**
	 * Return whether or not at least one {@link Statement} satisfies the {@link Predicate}
	 *
	 * @param thePredicate	the predicate
	 * @return				true if at least one Statement satisfies the Predicate, false otherwise
	 */
	public boolean any(final Predicate<Statement> thePredicate);

	/**
	 * Return whether or not all {@link Statement statements} satisfy the {@link Predicate}
	 *
	 * @param thePredicate	the predicate
	 * @return				true if at all Statements satisfy the Predicate, false otherwise
	 */
	public boolean all(final Predicate<Statement> thePredicate);

	/**
	 * Collect the results of the {@link Function} as it is applied to each {@link Statement}.  {@link Optional Absent}
	 * values are not collected; the provided function should never return a null value.
	 *
	 * @param theFunction	the function
	 * @return				the collected values
	 */
	public <T> Collection<T> collect(final Function<Statement, Optional<T>> theFunction);
}
