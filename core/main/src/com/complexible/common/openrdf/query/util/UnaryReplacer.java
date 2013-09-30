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

package com.complexible.common.openrdf.query.util;

import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.TupleExpr;

import com.google.common.base.Predicate;

/**
 * <p>Simple visitor class which will replace the projection in a query model with any other Unary operator.  If there is no projection in the model, the
 * provided unary operator is added to the top of the model.</p>
 *
 * @author Michael Grove
 * @version 0.4
 * @since 0.4
 */
public final class UnaryReplacer {

	private UnaryTupleOperator mOperator;

	private final Predicate<UnaryTupleOperator> mPredicate;

	private boolean insertIfNotPresent = true;

	/**
	 * Create a new UnaryReplacer
	 * @param theClass the type of node to replace
	 */
	public UnaryReplacer(final Class<? extends UnaryTupleOperator> theClass) {
		this(new Predicate<UnaryTupleOperator>() {
			/**
			 * @inheritDoc
			 */
			@Override
			public boolean apply(final UnaryTupleOperator theUnaryTupleOperator) {
				return theClass.isInstance(theUnaryTupleOperator);
			}
		});
	}

	/**
	 * Creaet a new UnaryReplacer
	 * @param thePredicate the predicate to use to determine if this is the operator we're looking to replace
	 */
	public UnaryReplacer(final Predicate<UnaryTupleOperator> thePredicate) {
		mPredicate = thePredicate;
	}

	/**
	 * Set the insertIfNotPresent.  When true, if a node cannot be found to replace, the replacement will be added as the
	 * root of the model.
	 * @param theInsertIfNotPresent the new insertIfNotPresent
	 */
	public void setInsertIfNotPresent(final boolean theInsertIfNotPresent) {
		insertIfNotPresent = theInsertIfNotPresent;
	}

	/**
	 * Given the TupleExpr, replace the existence of a UnaryTupleOperator with the provided unary operator.
	 * If there is nothing in the model that matches, the provided unary operator will be placed at the root of
	 * the model.
	 *
	 * @param theExpr the expression
	 * @param theNewProj the operator to replace the projectino with
	 * @return the expression w/ the projection operator removed
	 */
	public TupleExpr replace(final TupleExpr theExpr, final UnaryTupleOperator theNewProj) {
		Visitor aVisitor = new Visitor();

		try {
			theExpr.visit(aVisitor);
		}
		catch (Exception e) {
			// we really should not get an exception here
			throw new RuntimeException(e);
		}

		UnaryTupleOperator aProj = mOperator;

		if (aProj == null && insertIfNotPresent) {
			theNewProj.setArg(theExpr);

			return theNewProj;
		}
		else if (aProj != null && aProj.getParentNode() != null) {
			aProj.replaceWith(theNewProj);

			return theExpr;
		}
		else if (aProj != null) {
			theNewProj.setArg(aProj.getArg());
			return theNewProj;
		}
		else {
			return theExpr;
		}
	}

	private class Visitor extends QueryModelVisitorBase<Exception> {
		/**
		 * @inheritDoc
		 */
		@Override
		protected void meetUnaryTupleOperator(final UnaryTupleOperator theUnaryTupleOperator) throws Exception {
			if (mPredicate.apply(theUnaryTupleOperator)) {
				mOperator = theUnaryTupleOperator;
			}
			else {
				super.meetUnaryTupleOperator(theUnaryTupleOperator);
			}
		}
	}
}
