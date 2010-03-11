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

import org.openrdf.model.Value;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;

import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.StatementPatternCollector;
import org.openrdf.query.algebra.helpers.VarNameCollector;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Base implementation of a QueryBuilder.</p>
 *
 * @author Michael Grove
 * @since 0.2
 */
public class AbstractQueryBuilder<T extends ParsedQuery> implements QueryBuilder<T> {

	private List<String> mProjectionVars = new ArrayList<String>();
	private List<StatementPattern> mProjectionPatterns = new ArrayList<StatementPattern>();

	private List<Group> mQueryAtoms = new ArrayList<Group>();

	private boolean mDistinct = false;
	private boolean mReduced = false;
	private int mLimit = -1;
	private int mOffset = -1;

    private T mQuery;

    AbstractQueryBuilder(T theQuery) {
        mQuery = theQuery;
    }

	public void reset() {
		mLimit = mOffset = -1;
		mDistinct = mReduced = false;
		mProjectionVars.clear();
		mQueryAtoms.clear();
		mProjectionPatterns.clear();
	}

	public T query() {
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

			aRoot = aCurr = aSlice;
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

        if (mQuery instanceof ParsedTupleQuery && mProjectionVars.isEmpty()) {
            VarNameCollector aCollector = new VarNameCollector();

            aJoin.visit(aCollector);

            mProjectionVars.addAll(aCollector.getVarNames());
        }
        else if (mQuery instanceof ParsedGraphQuery && mProjectionPatterns.isEmpty()) {
            StatementPatternCollector aCollector = new StatementPatternCollector();

            aJoin.visit(aCollector);

            mProjectionPatterns.addAll(aCollector.getStatementPatterns());
        }

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

		mQuery.setTupleExpr(aRoot);

        return mQuery;
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
		MultiProjection aProjection = new MultiProjection();

        Extension aExt = null;

        for (StatementPattern aPattern : mProjectionPatterns) {
            ProjectionElemList aList = new ProjectionElemList();

            aList.addElement(new ProjectionElem(aPattern.getSubjectVar().getName(), "subject"));
            aList.addElement(new ProjectionElem(aPattern.getPredicateVar().getName(), "predicate"));
            aList.addElement(new ProjectionElem(aPattern.getObjectVar().getName(), "object"));

            if (aPattern.getSubjectVar().hasValue()) {
                if (aExt == null) {
                    aExt = new Extension();
                }

                aExt.addElements(new ExtensionElem(new ValueConstant(aPattern.getSubjectVar().getValue()),
                                                   aPattern.getSubjectVar().getName()));
            }

            if (aPattern.getPredicateVar().hasValue()) {
                if (aExt == null) {
                    aExt = new Extension();
                }

                aExt.addElements(new ExtensionElem(new ValueConstant(aPattern.getPredicateVar().getValue()),
                                                   aPattern.getPredicateVar().getName()));
            }

            if (aPattern.getObjectVar().hasValue()) {
                if (aExt == null) {
                    aExt = new Extension();
                }

                aExt.addElements(new ExtensionElem(new ValueConstant(aPattern.getObjectVar().getValue()),
                                                   aPattern.getObjectVar().getName()));
            }
            
            aProjection.addProjection(aList);
        }

        if (aExt != null) {
            aProjection.setArg(aExt);
        }

        return aProjection;
	}

	public QueryBuilder<T> addProjectionVar(String... theNames) {
		mProjectionVars.addAll(Arrays.asList(theNames));
		return this;
	}

	public GroupBuilder<T> group() {
		return new GroupBuilder<T>(this, false, null);
	}

	public GroupBuilder<T> optional() {
		return new GroupBuilder<T>(this, true, null);
	}

	public QueryBuilder<T> distinct() {
		mDistinct = true;
		return this;
	}

	public QueryBuilder<T> reduced() {
		mReduced = true;
		return this;
	}

	public QueryBuilder<T> limit(int theLimit) {
		mLimit = theLimit;
		return this;
	}

	public QueryBuilder<T> offset(int theOffset) {
		mOffset = theOffset;
		return this;
	}

	public QueryBuilder<T> addGroup(Group theGroup) {
		mQueryAtoms.add(theGroup);
        return this;
	}

    public QueryBuilder<T> addProjectionStatement(final String theSubj, final String thePred, final String theObj) {
        mProjectionPatterns.add(new StatementPattern(new Var(theSubj), new Var(thePred), new Var(theObj)));

        return this;
    }

    public QueryBuilder<T> addProjectionStatement(final String theSubj, final Value thePred, final Value theObj) {
        mProjectionPatterns.add(new StatementPattern(new Var(theSubj), GroupBuilder.valueToVar(thePred), GroupBuilder.valueToVar(theObj)));

        return this;
    }

    public QueryBuilder<T> addProjectionStatement(final String theSubj, final String thePred, final Value theObj) {
        mProjectionPatterns.add(new StatementPattern(new Var(theSubj), new Var(thePred), GroupBuilder.valueToVar(theObj)));

        return this;
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

				aNewJoin.setLeftArg(aJoin);
				aNewJoin.setRightArg(aExpr);

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
