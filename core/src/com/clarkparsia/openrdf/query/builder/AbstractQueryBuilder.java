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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
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
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;

/**
 * <p>Base implementation of a QueryBuilder.</p>
 *
 * @author Michael Grove
 * @since 0.2
 * @version 0.3.1
 */
public class AbstractQueryBuilder<T extends ParsedQuery> implements QueryBuilder<T> {

	// this is a bit of a hack making these protected so the select/construct query impl can access it.
	// would be better to encapsulate building the projection element up so the subclasses just handle it.
	protected List<StatementPattern> mProjectionPatterns = new ArrayList<StatementPattern>();
	protected List<String> mProjectionVars = new ArrayList<String>();

	private List<Group> mQueryAtoms = new ArrayList<Group>();

	/**
	 * the current limit on the number of results
	 */
	private int mLimit = -1;

	/**
	 * The current result offset
	 */
	private int mOffset = -1;

	private boolean mDistinct = false;
	private boolean mReduced = false;

	/**
	 * the from clauses in the query
	 */
	private Set<URI> mFrom = new HashSet<URI>();

	/**
	 * The from named clauses of the query
	 */
	private Set<URI> mFromNamed = new HashSet<URI>();

	/**
	 * The query to be built
	 */
    private T mQuery;

    AbstractQueryBuilder(T theQuery) {
        mQuery = theQuery;
    }

	/**
	 * @inheritDoc
	 */
	public void reset() {
		mDistinct = mReduced = false;
		mLimit = mOffset = -1;
		mProjectionVars.clear();
		mQueryAtoms.clear();
		mProjectionPatterns.clear();
	}

	/**
	 * @inheritDoc
	 */
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

		if (aJoin != null) {
			aCurr.setArg(aJoin);
		}

		mQuery.setTupleExpr(aRoot);

		if (!mFrom.isEmpty() || !mFromNamed.isEmpty()) {
			DatasetImpl aDataset = new DatasetImpl();

			for (URI aFrom : mFrom) {
				aDataset.addDefaultGraph(aFrom);
			}

			for (URI aFrom : mFromNamed) {
				aDataset.addNamedGraph(aFrom);
			}

			mQuery.setDataset(aDataset);
		}

        return mQuery;
	}

	/**
	 * @inheritDoc
	 */
	public QueryBuilder<T> fromNamed(final URI theURI) {
		mFromNamed.add(theURI);
		return this;
	}

	/**
	 * @inheritDoc
	 */
	public QueryBuilder<T> from(final URI theURI) {
		mFrom.add(theURI);
		return this;
	}

	/**
	 * @inheritDoc
	 */
	public QueryBuilder<T> distinct() {
		// crappy way to only let this be set for select queries
		if (isSelect()) {
			mDistinct = true;
		}

		return this;
	}

	/**
	 * @inheritDoc
	 */
	public QueryBuilder<T> reduced() {
		mReduced = true;
		return this;
	}

	/**
	 * @inheritDoc
	 */
	public QueryBuilder<T> addProjectionVar(String... theNames) {
		if (isSelect()) {
			mProjectionVars.addAll(Arrays.asList(theNames));
		}

		return this;
	}

	private boolean isConstruct() {
		// crappy way to only let this be set for select queries
		return (mQuery instanceof ParsedGraphQuery);
	}

	private boolean isSelect() {
		// crappy way to only let this be set for select queries
		return (mQuery instanceof ParsedTupleQuery);
	}

	/**
	 * @inheritDoc
	 */
    public QueryBuilder<T> addProjectionStatement(final String theSubj, final String thePred, final String theObj) {
		if (isConstruct()) {
        	mProjectionPatterns.add(new StatementPattern(new Var(theSubj), new Var(thePred), new Var(theObj)));
		}

        return this;
    }

	/**
	 * @inheritDoc
	 */
    public QueryBuilder<T> addProjectionStatement(final String theSubj, final Value thePred, final Value theObj) {
		if (isConstruct()) {
        	mProjectionPatterns.add(new StatementPattern(new Var(theSubj), GroupBuilder.valueToVar(thePred), GroupBuilder.valueToVar(theObj)));
		}

        return this;
    }

	/**
	 * @inheritDoc
	 */
    public QueryBuilder<T> addProjectionStatement(final String theSubj, final String thePred, final Value theObj) {
		if (isConstruct()) {
        	mProjectionPatterns.add(new StatementPattern(new Var(theSubj), new Var(thePred), GroupBuilder.valueToVar(theObj)));
		}

        return this;
    }

    /**
	 * @inheritDoc
	 */
    public QueryBuilder<T> addProjectionStatement(String theSubj, URI thePred, Value theObj) {
    	if (isConstruct()) {
        	mProjectionPatterns.add(new StatementPattern(new Var(theSubj), GroupBuilder.valueToVar(thePred), GroupBuilder.valueToVar(theObj)));
		}

        return this;
    }

    /**
	 * @inheritDoc
	 */
    public QueryBuilder<T> addProjectionStatement(URI theSubj, String thePred, String theObj) {
    	if (isConstruct()) {
        	mProjectionPatterns.add(new StatementPattern(GroupBuilder.valueToVar(theSubj), new Var(thePred), new Var(theObj)));
		}

        return this;
    }

    /**
	 * @inheritDoc
	 */
    public QueryBuilder<T> addProjectionStatement(URI theSubj, URI thePred, String theObj) {
    	if (isConstruct()) {
        	mProjectionPatterns.add(new StatementPattern(GroupBuilder.valueToVar(theSubj), GroupBuilder.valueToVar(thePred), new Var(theObj)));
		}

        return this;
    }

    /**
	 * @inheritDoc
	 */
    public QueryBuilder<T> addProjectionStatement(String theSubj, URI thePred, String theObj) {
    	if (isConstruct()) {
        	mProjectionPatterns.add(new StatementPattern(new Var(theSubj), GroupBuilder.valueToVar(thePred), new Var(theObj)));
		}

        return this;
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

	/**
	 * @inheritDoc
	 */
	public GroupBuilder<T, QueryBuilder<T>> group() {
		return new GroupBuilder<T, QueryBuilder<T>>(this, false, null);
	}

	/**
	 * @inheritDoc
	 */
	public GroupBuilder<T, QueryBuilder<T>> optional() {
		return new GroupBuilder<T, QueryBuilder<T>>(this, true, null);
	}

	/**
	 * @inheritDoc
	 */
	public QueryBuilder<T> limit(int theLimit) {
		mLimit = theLimit;
		return this;
	}

	/**
	 * @inheritDoc
	 */
	public QueryBuilder<T> offset(int theOffset) {
		mOffset = theOffset;
		return this;
	}

	/**
	 * @inheritDoc
	 */
	public QueryBuilder<T> addGroup(Group theGroup) {
		mQueryAtoms.add(theGroup);
        return this;
	}

	/**
	 * @inheritDoc
	 */
	public QueryBuilder<T> removeGroup(Group theGroup) {
		mQueryAtoms.remove(theGroup);
		return this;
	}

    private TupleExpr groupAsJoin(List<Group> theList) {
		BinaryTupleOperator aJoin = new Join();

		Filter aFilter = null;
		for (Group aGroup : theList) {
			TupleExpr aExpr = aGroup.expr();

			if (aExpr == null) {
				continue;
			}

			if (aExpr instanceof Filter && (((Filter)aExpr).getArg() == null || ((Filter)aExpr).getArg() instanceof EmptySet)) {
				if (aFilter == null) {
					aFilter = (Filter) aExpr;
				}
				else {
					// if we already have a filter w/ an empty arg, let's And the conditions together.
					aFilter.setCondition(new And(aFilter.getCondition(), ((Filter)aExpr).getCondition()));
				}

				continue;
			}

			if (aFilter != null) {
				aFilter.setArg(aExpr);
				aExpr = aFilter;

				aFilter = null;
			}

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

		TupleExpr aExpr = joinOrExpr(aJoin);

		if (aFilter != null) {
			aFilter.setArg(aExpr);
			aExpr = aFilter;
		}

		return aExpr;
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
