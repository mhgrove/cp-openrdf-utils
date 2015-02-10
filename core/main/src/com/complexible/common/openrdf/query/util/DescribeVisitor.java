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

import org.openrdf.query.algebra.DescribeOperator;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.TupleExpr;

import org.openrdf.query.parser.ParsedQuery;

/**
 * <p>TupleExprVisitor implementation that will scan a query model and see if it looks like the model for a describe
 * query.  If so, {@link #isDescribe} will return true after {@link #checkQuery(TupleExpr) checking} the query. </p>
 *
 * <p>The main aim of this class is to provide some functionality to scan a query model to find out if its a describe,
 * and what is being described so you could "compile" the describe query into a query language that does not support
 * describe natively, such as SeRQL.  Once you know what is being described, you could then simulate the support with
 * a construct query.</p>
 *
 * @author  Michael Grove
 * @since   0.2.4
 * @version 0.3
*/
public final class DescribeVisitor extends QueryModelVisitorBase<Exception> {

	/**
	 * Whether or not the query model represents a describe query
	 */
	private boolean mIsDescribe = false;

	public void reset() {
		mIsDescribe = false;
	}

	/**
	 * Check to see if this query is a describe query or not
	 * @param theExpr the query model to check
	 * @return this visitor
	 * @throws Exception if there is an error while checking
	 */
	public boolean checkQuery(TupleExpr theExpr) throws Exception {
		reset();
		theExpr.visit(this);

		return isDescribe();
	}

	/**
	 * Check to see if this query is a describe query or not
	 * @param theQuery the query to check
	 * @return this visitor
	 * @throws Exception if there is an error while checking
	 */
	public boolean checkQuery(ParsedQuery theQuery) throws Exception {
		return checkQuery(theQuery.getTupleExpr());
	}

	/**
	 * Whether or not the query model represents a describe query
	 * @return true if it is a describe query, false otherwise
	 */
	public boolean isDescribe() {
		return mIsDescribe;
	}

	@Override
	public void meet(final DescribeOperator node) throws Exception {
		mIsDescribe = true;
	}

    public static boolean isDescribeName(final String theName) {
        return theName.startsWith("-descr") || theName.startsWith("descr") || theName.startsWith("descr_");
    }
}
