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

package com.clarkparsia.openrdf.query.util;

import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.model.Value;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

/**
 * <p>TupleExprVisitor implementation that will scan a query model and see if it looks like the model for a describe
 * query.  If so, {@link #isDescribe} will return true after {@link #checkQuery checking} the query.  Also,
 * {@link #getValues} will return a list of all the concrete values to be described in the query.  This will <b>not</b>
 * return any variables to be described.</p>
 *
 * <p>The main aim of this class is to provide some functionality to scan a query model to find out if its a describe,
 * and what is being described so you could "compile" the describe query into a query language that does not support
 * describe natively, such as SeRQL.  Once you know what is being described, you could then simulate the support with
 * a construct query.</p>
*
* @author Michael Grove
*/
public class DescribeVisitor extends QueryModelVisitorBase<Exception> {

	/**
	 * Whether or not the query model represents a describe query
	 */
	private boolean mIsDescribe = false;

	/**
	 * Collection of Concepts to be described in the query
	 */
	private Set<Value> mValues = new HashSet<Value>();

	public void reset() {
		mIsDescribe = false;
		mValues = new HashSet<Value>();
	}

	/**
	 * Check to see if this query is a describe query or not
	 * @param theExpr the query model to check
	 * @return this visitor
	 * @throws Exception if there is an error while checking
	 */
	public DescribeVisitor checkQuery(TupleExpr theExpr) throws Exception {
		reset();
		theExpr.visit(this);

		return this;
	}

	/**
	 * Check to see if this query is a describe query or not
	 * @param theQuery the query to check
	 * @return this visitor
	 * @throws Exception if there is an error while checking
	 */
	public DescribeVisitor checkQuery(ParsedQuery theQuery) throws Exception {
		return checkQuery(theQuery.getTupleExpr());
	}

	/**
	 * Whether or not the query model represents a describe query
	 * @return true if it is a describe query, false otherwise
	 */
	public boolean isDescribe() {
		return mIsDescribe;
	}

	/**
	 * Collection of Concepts to be described in the query
	 * @return the described concepts
	 */
	public Collection<Value> getValues() {
		if (mIsDescribe) {
			return Collections.unmodifiableCollection(mValues);
		}
		else {
			return Collections.emptySet();
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final ProjectionElem theProjectionElem) throws Exception {
		super.meet(theProjectionElem);

		if (theProjectionElem.getSourceName().startsWith("-descr") && (theProjectionElem.getTargetName().equals("subject")
																	   || theProjectionElem.getTargetName().equals("predicate")
																	   || theProjectionElem.getTargetName().equals("object"))) {
			mIsDescribe = true;
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final SameTerm theSameTerm) throws Exception {
		super.meet(theSameTerm);

		if (theSameTerm.getLeftArg() instanceof ValueConstant) {
			mValues.add(((ValueConstant) theSameTerm.getLeftArg()).getValue());
		}

		if (theSameTerm.getRightArg() instanceof ValueConstant) {
			mValues.add(((ValueConstant) theSameTerm.getRightArg()).getValue());
		}
	}
}
