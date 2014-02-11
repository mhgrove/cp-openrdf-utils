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

import com.google.common.base.Predicate;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * <p>A {@link Graph} which has a {@link Predicate constraint} placed upon which statements can be added to the Graph.</p>
 *
 * @author Michael Grove
 * @since	0.8
 * @version	2.0.1
 */
public final class ConstrainedGraph extends DelegatingGraph {
	private final Predicate<Statement> mConstraint;

	ConstrainedGraph(final Graph theGraph, final Predicate<Statement> theConstraint) {
		super(theGraph);
		mConstraint = theConstraint;
	}

	/**
	 * Create a new empty, ConstrainedGraph, which will have the specified constraint place on all additions
	 *
	 * @param theConstraint	the constraint to enforce
	 * @return				the new ConstrainedGraph
	 */
	public static ConstrainedGraph of(final Predicate<Statement> theConstraint) {
		return of(new SetGraph(), theConstraint);
	}

	/**
	 * Create a new ConstrainedGraph which will have the specified constraint enforced on all additions.  Does not
	 * retroactively enforce the constraint, so the provided Graph can contain invalid elements.
	 *
	 * @param theGraph			the graph to constrain
	 * @param theConstraint		the constraint to enforce
	 * @return					the new ConstrainedGraph
	 */
	public static ConstrainedGraph of(final Graph theGraph, final Predicate<Statement> theConstraint) {
		return new ConstrainedGraph(theGraph, theConstraint);
	}

	/**
	 * Return a {@link Predicate} which will only allow {@link Statements#isLiteralValid(Literal) valid} literals into the graph.
	 *
	 * @return	a Constraint to enforce valid literals
	 */
	public static Predicate<Statement> onlyValidLiterals() {
		return new Predicate<Statement>() {
			@Override
			public boolean apply(final Statement theStatement) {
				if (theStatement.getObject() instanceof Literal && !Statements.isLiteralValid((Literal) theStatement.getObject())) {
					throw new StatementViolatedConstraintException(theStatement.getObject() + " is not a well-formed literal value.");
				}

				return true;
			}
		};
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean add(final Statement e) {
		mConstraint.apply(e);

		return super.add(e);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean add(final Resource theResource, final URI theURI, final Value theValue, final Resource... theContexts) {

		if (theContexts == null || theContexts.length == 0) {
			return add(ValueFactoryImpl.getInstance().createStatement(theResource, theURI, theValue));
		}
		else {
			boolean aAdded = true;
			for (Resource aCxt : theContexts) {
				aAdded |= add(ValueFactoryImpl.getInstance().createStatement(theResource, theURI, theValue, aCxt));
			}

			return aAdded;
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean addAll(final Collection<? extends Statement> c) {
		all(c, mConstraint);

		return super.addAll(c);
	}

	private static <T> void all(final Iterable<? extends T> theObjects, final Predicate<T> theConstraint) {
		for (T aObj : theObjects) {
			theConstraint.apply(aObj);
		}
	}

	/**
	 * A runtime exception suitable for being thrown from a {@link Predicate} on a {@link Statement}
	 */
	public static class StatementViolatedConstraintException extends RuntimeException {

		/**
		 * Create a new StatementViolatedConstraintException
		 * @param theMessage	a note about why the constraint was violated
		 */
		public StatementViolatedConstraintException(final String theMessage) {
			super(theMessage);
		}
	}
}
