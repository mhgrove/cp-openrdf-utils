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

package com.clarkparsia.openrdf.query.builder;

import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.ParsedGraphQuery;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
public class QueryBuilder {
	// TODO: extensions/aliases
	// TODO: multi projections
	// TODO: describe & ask support
	// TODO: support for set operations
	// TODO: fluent api for ValueExpr construction

	private List<String> mProjectionVars = new ArrayList<String>();
	private List<StatementPattern> mProjectionPatterns = new ArrayList<StatementPattern>();

	private List<Group> mQueryAtoms = new ArrayList<Group>();

	private boolean mDistinct = false;
	private boolean mReduced = false;
	private int mLimit = -1;
	private int mOffset = -1;

	public void reset() {
		mLimit = mOffset = -1;
		mDistinct = mReduced = false;
		mProjectionVars.clear();
		mQueryAtoms.clear();
		mProjectionPatterns.clear();
	}

	private TupleExpr query() {
		UnaryTupleOperator aRoot = null;
		UnaryTupleOperator aCurr = null;

		if (mLimit != -1 || mOffset != -1) {
			Slice aSlice = new Slice();
			if (mLimit != -1) {
				aSlice.setLimit(mLimit);
			}
			if (mOffset != -1) {
				aSlice.setOffset(mOffset);
			}

			if (aRoot == null) {
				aRoot = aCurr = aSlice;
			}
			else {
				aCurr.setArg(aSlice);
				aCurr = aSlice;
			}
		}

		if (mDistinct) {
			Distinct aDistinct = new Distinct();

			if (aRoot == null) {
				aRoot = aCurr = aDistinct;
			}
			else {
				aCurr.setArg(aDistinct);
				aCurr = aDistinct;
			}
		}

		if (mReduced) {
			Reduced aReduced = new Reduced();

			if (aRoot == null) {
				aRoot = aCurr = aReduced;
			}
			else {
				aCurr.setArg(aReduced);
				aCurr = aReduced;
			}
		}

		TupleExpr aJoin = join();

		UnaryTupleOperator aProjection = projection();

		if (aRoot == null) {
			aRoot = aCurr = aProjection;
		}
		else {
			aCurr.setArg(aProjection);
		}

		if (aProjection.getArg() == null) {
			aCurr = aProjection;
		}
		else {
			// I think this is always a safe cast
			aCurr = (UnaryTupleOperator) aProjection.getArg();
		}

		aCurr.setArg(aJoin);

		return aRoot;
	}

	public ParsedTupleQuery tupleQuery() {
		return new ParsedTupleQuery(query());
	}

	public ParsedGraphQuery graphQuery() {
		return new ParsedGraphQuery(query());
	}

	private TupleExpr join() {
		if (mQueryAtoms.isEmpty()) {
			throw new RuntimeException("Can't have an empty or missing join.");
		}
		else if (mQueryAtoms.size() == 1) {
			return mQueryAtoms.get(0).expr();
		}
		else {
			return groupAsJoin(mQueryAtoms);
		}
	}

	private UnaryTupleOperator projection() {
		if (!mProjectionPatterns.isEmpty()) {
			return multiProjection();
		}
		else {
			Extension aExt = null;

			ProjectionElemList aList = new ProjectionElemList();

			for (String aVar : mProjectionVars) {
				aList.addElement(new ProjectionElem(aVar));
			}

			Projection aProjection = new Projection();
			aProjection.setProjectionElemList(aList);

			if (aExt != null) {
				aProjection.setArg(aExt);
			}

			return aProjection;
		}
	}

	private UnaryTupleOperator multiProjection() {
		throw new RuntimeException("NYI");
	}

	public QueryBuilder addProjectionVar(String... theNames) {
		mProjectionVars.addAll(Arrays.asList(theNames));
		return this;
	}

	public GroupFactory group() {
		return new GroupFactory(this, false, null);
	}

	public GroupFactory optional() {
		return new GroupFactory(this, true, null);
	}

	public QueryBuilder distinct() {
		mDistinct = true;
		return this;
	}

	public QueryBuilder reduced() {
		mReduced = true;
		return this;
	}

	public QueryBuilder limit(int theLimit) {
		mLimit = theLimit;
		return this;
	}

	public QueryBuilder offset(int theOffset) {
		mOffset = theOffset;
		return this;
	}

	void addGroup(Group theGroup) {
		mQueryAtoms.add(theGroup);
	}

	private TupleExpr groupAsJoin(List<Group> theList) {
		BinaryTupleOperator aJoin = new Join();

		for (Group aGroup : theList) {
			TupleExpr aExpr = aGroup.expr();

			if (aGroup.isOptional()) {
				LeftJoin lj = new LeftJoin();

				TupleExpr aLeft = joinOrExpr(aJoin);

				if (aLeft != null) {
					lj.setLeftArg(aLeft);
					lj.setRightArg(aExpr);

					aJoin = lj;

					continue;
				}
			}

			if (aJoin.getLeftArg() == null) {
				aJoin.setLeftArg(aExpr);
			}
			else if (aJoin.getRightArg() == null) {
				aJoin.setRightArg(aExpr);
			}
			else {
				Join aNewJoin = new Join();

				aJoin.setLeftArg(aJoin);
				aJoin.setRightArg(aExpr);

				aJoin = aNewJoin;
			}
		}

		return joinOrExpr(aJoin);
	}

	private TupleExpr joinOrExpr(BinaryTupleOperator theExpr) {
		if (theExpr.getLeftArg() != null && theExpr.getRightArg() == null) {
			return theExpr.getLeftArg();
		}
		else if (theExpr.getLeftArg() == null && theExpr.getRightArg() != null) {
			return theExpr.getRightArg();
		}
		else if (theExpr.getLeftArg() == null && theExpr.getRightArg() == null) {
			return null;
		}
		else {
			return theExpr;
		}
	}
}
