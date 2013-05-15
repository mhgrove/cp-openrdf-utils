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

package com.complexible.common.openrdf.query.util;

import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ProjectionElem;

import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.ParsedQuery;


/**
 * <p>Visitor to check a query model and see if it represents an Ask query.  Ask queries do not have a projection
 * in their parsed representation, so if this finds a projection in the query model, it's not an ask query.</p>
 *
 * @author Michael Grove
 */
public final class AskVisitor extends QueryModelVisitorBase<Exception> {

	/**
	 * Whether or not this visitor just scanned a query model that represents an Ask query
	 */
	private boolean mIsAsk = true;

	/**
	 * Resets this visitor
	 */
	public void reset() {
		mIsAsk = true;
	}

	/**
	 * Scans the query model to see if it's an ask query or not
	 * @param theExpr the query model
	 * @throws Exception if there is an error while scanning
	 * @return this visitor
	 */
	public AskVisitor checkQuery(final TupleExpr theExpr) throws Exception {
		reset();
		theExpr.visit(this);

		return this;
	}

	/**
	 * Scans the query model to see if its an ask query
	 * @param theQuery the query model
	 * @throws Exception if there is an error while scanning
	 * @return this visitor
	 */
	public AskVisitor checkQuery(final ParsedQuery theQuery) throws Exception {
		return checkQuery(theQuery.getTupleExpr());
	}

	/**
	 * Return whether or not the query model represents an ask query
	 * @return true if its an ask, false otherwise
	 */
	public boolean isAsk() {
		return mIsAsk;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final ProjectionElem theProjectionElem) throws Exception {
		super.meet(theProjectionElem);

		mIsAsk = false;
	}
}
