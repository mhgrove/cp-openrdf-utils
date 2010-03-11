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

import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.StatementPattern;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import com.clarkparsia.utils.Predicate;
import com.clarkparsia.utils.FunctionUtil;
import static com.clarkparsia.utils.collections.CollectionUtil.filter;
import static com.clarkparsia.utils.collections.CollectionUtil.transform;

/**
 * <p>Internal class for representing a group within a query.</p>
 *
 * @author Michael Grove
 * @since 0.2
 * @version 0.2.1
 */
public class Group {
	private boolean optional = false;
	private Collection<TupleExpr> mExpressions = new HashSet<TupleExpr>();
	private List<Group> children = new ArrayList<Group>();
	private Collection<ValueExpr> mFilters = new HashSet<ValueExpr>();

	public Group(final boolean theOptional) {
		optional = theOptional;
	}

	public void addChild(Group theGroup) {
		children.add(theGroup);
	}

	public void addFilter(ValueExpr theExpr) {
		mFilters.add(theExpr);
	}

	public boolean isOptional() {
		return optional;
	}

	public TupleExpr expr() {
		return expr(true);
	}

	private TupleExpr expr(boolean filterExpr) {
		TupleExpr aExpr = asJoin(mExpressions);

		if (filterExpr) {
			aExpr = filteredTuple(aExpr);
		}

		if (!children.isEmpty()) {

			for (Group aGroup : children) {
				BinaryTupleOperator aJoin = aGroup.isOptional() ? new LeftJoin() : new Join();

				aJoin.setLeftArg(aExpr);

				if (!aGroup.mFilters.isEmpty() && aGroup.isOptional() && aJoin instanceof LeftJoin) {
					aJoin.setRightArg(aGroup.expr(false));
					((LeftJoin)aJoin).setCondition(aGroup.filtersAsAnd());
				}
				else {
					aJoin.setRightArg(aGroup.expr());
				}

				aExpr = aJoin;
			}
		}
		
		return aExpr;
	}

	private TupleExpr filteredTuple(TupleExpr theExpr) {
		TupleExpr aExpr = theExpr ;

		for (ValueExpr aValEx : mFilters) {
			Filter aFilter = new Filter();
			aFilter.setCondition(aValEx);
			aFilter.setArg(aExpr);
			aExpr = aFilter;
		}

		return aExpr;
	}

	private ValueExpr filtersAsAnd() {
		ValueExpr aExpr = null;

		for (ValueExpr aValEx : mFilters) {
			if (aExpr == null) {
				aExpr = aValEx;
			}
			else {
				And aAnd = new And();
				aAnd.setLeftArg(aValEx);
				aAnd.setRightArg(aExpr);
				aExpr = aAnd;
			}
		}

		return aExpr;
	}

	public void add(final TupleExpr theExpr) {
		mExpressions.add(theExpr);
	}

    public void addAll(final Collection<? extends TupleExpr> theTupleExprs) {
        mExpressions.addAll(theTupleExprs);
    }

	private TupleExpr asJoin(Collection<TupleExpr> theList) {
		Join aJoin = new Join();

		if (theList.isEmpty()) {
			throw new RuntimeException("Can't have an empty or missing join.");
		}
		else if (theList.size() == 1) {
			return theList.iterator().next();
		}

		for (TupleExpr aExpr : theList) {
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

		return aJoin;
	}

	Collection<StatementPattern> getPatterns() {
		return transform(filter(mExpressions, new Predicate<TupleExpr>() { public boolean accept(TupleExpr theExpr) { return theExpr instanceof StatementPattern; } }),
						 new FunctionUtil.Cast<TupleExpr, StatementPattern>(StatementPattern.class));
	}
}
