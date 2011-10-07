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

package com.clarkparsia.openrdf.query;

import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;

import org.openrdf.query.parser.ParsedQuery;

import org.openrdf.model.Value;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * <p>Base class for rendering Sesame query API objects into strings.</p>
 *
 * @author Michael Grove
 * @since 0.2
 */
public abstract class BaseTupleExprRenderer extends QueryModelVisitorBase<Exception> {

	/**
	 * A map of the extensions specified in the query.
	 */
	protected Map<String, ValueExpr> mExtensions = new HashMap<String, ValueExpr>();

	/**
	 * The list of elements include in the projection of the query
	 */
	protected List<ProjectionElemList> mProjection = new ArrayList<ProjectionElemList>();

	/**
	 * The elements specified in the order by clause of the query
	 */
	protected List<OrderElem> mOrdering = new ArrayList<OrderElem>();

	/**
	 * Whether or not the query is distinct
	 */
	protected boolean mDistinct = false;

	/**
	 * Whether or not the query is reduced
	 */
	protected boolean mReduced = false;

	/**
	 * The limit of results for the query, or -1 for no limit
	 */
	protected int mLimit = -1;

	/**
	 * The query offset, or -1 for no offset
	 */
	protected int mOffset = -1;

	/**
	 * Reset the state of the renderer
	 */
	public void reset() {
		mLimit = mOffset = -1;
		mDistinct = mReduced = false;

		mExtensions.clear();
		mOrdering.clear();
		mProjection.clear();
	}

	public Map<String, ValueExpr> getExtensions() {
		return mExtensions;
	}

	public List<ProjectionElemList> getProjection() {
		return mProjection;
	}

	public List<OrderElem> getOrdering() {
		return mOrdering;
	}

	public boolean isDistinct() {
		return mDistinct;
	}

	public boolean isReduced() {
		return mReduced;
	}

	public int getLimit() {
		return mLimit;
	}

	public int getOffset() {
		return mOffset;
	}

	/**
	 * Render the ParsedQuery as a query string
	 * @param theQuery the parsed query to render
	 * @return the query object rendered in the serql syntax
	 * @throws Exception if there is an error while rendering
	 */
	public String render(ParsedQuery theQuery) throws Exception {
		return render(theQuery.getTupleExpr());
	}

	/**
	 * Render the TupleExpr as a query or query fragment depending on what kind of TupleExpr it is
	 * @param theExpr the expression to render
	 * @return the TupleExpr rendered in the serql syntax
	 * @throws Exception if there is an error while rendering
	 */
	public abstract String render(TupleExpr theExpr) throws Exception;

	/**
	 * Render the given ValueExpr
	 * @param theExpr the expr to render
	 * @return the rendered expression
	 * @throws Exception if there is an error while rendering
	 */
	protected abstract String renderValueExpr(final ValueExpr theExpr) throws Exception;

	/**
	 * Turn a ProjectionElemList for a construct query projection (three elements aliased as 'subject', 'predicate'
	 * and 'object' in that order) into a StatementPattern.
	 * @param theList the elem list to render
	 * @return the elem list for a construct projection as a statement pattern
	 * @throws Exception if there is an exception while rendering
	 */
	public StatementPattern toStatementPattern(ProjectionElemList theList) throws Exception {
		ProjectionElem aSubj = theList.getElements().get(0);
		ProjectionElem aPred = theList.getElements().get(1);
		ProjectionElem aObj = theList.getElements().get(2);

		return new StatementPattern(mExtensions.containsKey(aSubj.getSourceName()) ? new Var(scrubVarName(aSubj.getSourceName()), asValue(mExtensions.get(aSubj.getSourceName()))) : new Var(scrubVarName(aSubj.getSourceName())),
									mExtensions.containsKey(aPred.getSourceName()) ? new Var(scrubVarName(aPred.getSourceName()), asValue(mExtensions.get(aPred.getSourceName()))) : new Var(scrubVarName(aPred.getSourceName())),
									mExtensions.containsKey(aObj.getSourceName()) ? new Var(scrubVarName(aObj.getSourceName()), asValue(mExtensions.get(aObj.getSourceName()))) : new Var(scrubVarName(aObj.getSourceName())));
	}

	/**
	 * Scrub any illegal characters out of the variable name
	 * @param theName the potential variable name
	 * @return the name scrubbed of any illegal characters
	 */
	public static String scrubVarName(String theName) {
		return theName.replaceAll("-","");
	}

	/**
	 * Return the {@link ValueExpr} as a {@link Value} if possible.
	 * @param theValue the ValueExpr to convert
	 * @return the expression as a Value, or null if it cannot be converted
	 * @throws Exception if there is an error converting to a Value
	 */
	private Value asValue(ValueExpr theValue) throws Exception {
		if (theValue instanceof ValueConstant) {
			return ((ValueConstant)theValue).getValue();
		}
		else if (theValue instanceof Var) {
			Var aVar = (Var) theValue;
			if (aVar.hasValue()) {
				return aVar.getValue();
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
	}

	/**
	 * Returns whether or not the results of scanning the query model indicates that this represents a select query
	 * @return true if its a select query, false if its a construct query
	 */
	protected boolean isSelect() {
		boolean aIsSelect = false;

		for (ProjectionElemList aList : mProjection) {
			if (!isSPOElemList(aList)) {
				aIsSelect = true;
				break;
			}
		}

		return aIsSelect;
	}

	/**
	 * Return whether or not this projection looks like an spo binding for a construct query
	 * @param theList the projection element list to inspect
	 * @return true if it has the format of a spo construct projection element, false otherwise
	 */
	public static boolean isSPOElemList(ProjectionElemList theList) {
		return theList.getElements().size() == 3
				&& theList.getElements().get(0).getTargetName().equalsIgnoreCase("subject")
				&& theList.getElements().get(1).getTargetName().equalsIgnoreCase("predicate")
				&& theList.getElements().get(2).getTargetName().equalsIgnoreCase("object");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final StatementPattern theStatementPattern) throws Exception {
		theStatementPattern.visitChildren(this);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final Slice theSlice) throws Exception {
		if (theSlice.hasOffset()) {
			mOffset = theSlice.getOffset();
		}

		if (theSlice.hasLimit()) {
			mLimit = theSlice.getLimit();
		}

		theSlice.visitChildren(this);
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final ExtensionElem theExtensionElem) throws Exception {
		mExtensions.put(theExtensionElem.getName(), theExtensionElem.getExpr());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final ProjectionElemList theProjectionElemList) throws Exception {
		if (!theProjectionElemList.getElements().isEmpty()) {
			mProjection.add(theProjectionElemList.clone());
		}

		theProjectionElemList.visitChildren(this);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final OrderElem theOrderElem) throws Exception {
		mOrdering.add(theOrderElem);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final Distinct theDistinct) throws Exception {
		mDistinct = true;

		theDistinct.getArg().visit(this);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final Reduced theReduced) throws Exception {
		mReduced = true;

		theReduced.visitChildren(this);
	}
}
