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
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.model.Value;
import com.clarkparsia.utils.BasicUtils;
import org.openrdf.query.parser.ParsedQuery;

import java.util.Arrays;
import java.util.Collection;

/**
 * <p>Builder for creating a grouped set of query atoms and filters in a query.</p>
 *
 * @author Michael Grove
 * @since 0.2
 */
public class GroupBuilder<T extends ParsedQuery> {
	private QueryBuilder<T> mBuilder;
	private Group mGroup;

	private StatementPattern.Scope mScope = StatementPattern.Scope.DEFAULT_CONTEXTS;
	private Var mContext = null;

	GroupBuilder(final QueryBuilder<T> theBuilder) {
		this(theBuilder, false, null);
	}

	GroupBuilder(final QueryBuilder<T> theBuilder, boolean theOptional) {
		this(theBuilder, theOptional, null);
	}

	GroupBuilder(final QueryBuilder<T> theBuilder, boolean theOptional, Group theParent) {
		mBuilder = theBuilder;
		mGroup = new Group(theOptional);

		if (theParent == null) {
			mBuilder.addGroup(mGroup);
		}
		else {
			theParent.addChild(mGroup);
		}
	}

    Group getGroup() {
		return mGroup;
	}

	public GroupBuilder<T> group() {
		return new GroupBuilder<T>(mBuilder, false, mGroup);
	}

	public GroupBuilder<T> optional() {
		return new GroupBuilder<T>(mBuilder, true, mGroup);
	}

	public QueryBuilder<T> closeGroup() {
		return mBuilder;
	}

	public GroupBuilder setScope(StatementPattern.Scope theScope) {
		mScope = theScope;
		return this;
	}

	public GroupBuilder setContext(String theContextVar) {
		mContext = new Var(theContextVar);
		return this;
	}

	public GroupBuilder setContext(Value theContextValue) {
		mContext = valueToVar(theContextValue);
		return this;
	}

	public FilterBuilder<T> filter() {
		return new FilterBuilder<T>(this);
	}

    public GroupBuilder<T> filter(ValueExpr theExpr) {
        mGroup.addFilter(theExpr);

        return this;
    }

	public GroupBuilder<T> filter(String theVar, Compare.CompareOp theOp, Value theValue) {
		Compare aComp = new Compare(new Var(theVar), new ValueConstant(theValue), theOp);
		mGroup.addFilter(aComp);

		return this;
	}

    public GroupBuilder<T> atom(StatementPattern thePattern) {
        return addPattern(thePattern);
    }

    public GroupBuilder<T> atom(StatementPattern... thePatterns) {
        return atoms(Arrays.asList(thePatterns));
    }

    public GroupBuilder<T> atoms(Collection<StatementPattern> thePatterns) {
        mGroup.addAll(thePatterns);
        return this;
    }

	public GroupBuilder<T> atom(String theSubjVar, String thePredVar, String theObjVar) {
		return addPattern(newPattern(new Var(theSubjVar), new Var(thePredVar), new Var(theObjVar)));
	}

	public GroupBuilder<T> atom(String theSubjVar, String thePredVar, Value theObj) {
		return addPattern(newPattern(new Var(theSubjVar), new Var(thePredVar), valueToVar(theObj)));
	}

	public GroupBuilder<T> atom(String theSubjVar, Value thePredVar, String theObj) {
		return addPattern(newPattern(new Var(theSubjVar), valueToVar(thePredVar), new Var(theObj)));
	}

	public GroupBuilder<T> atom(String theSubjVar, Value thePred, Value theObj) {
		return addPattern(newPattern(new Var(theSubjVar), valueToVar(thePred), valueToVar(theObj)));
	}

	public GroupBuilder<T> atom(Value theSubjVar, Value thePredVar, Value theObj) {
		return addPattern(newPattern(valueToVar(theSubjVar), valueToVar(thePredVar), valueToVar(theObj)));
	}

	public GroupBuilder<T> atom(Value theSubjVar, Value thePredVar, String theObj) {
		return addPattern(newPattern(valueToVar(theSubjVar), valueToVar(thePredVar), new Var(theObj)));
	}

	public GroupBuilder<T> atom(Value theSubjVar, String thePredVar, String theObj) {
		return addPattern(newPattern(valueToVar(theSubjVar), new Var(thePredVar), new Var(theObj)));
	}

	private GroupBuilder<T> addPattern(StatementPattern thePattern) {
		mGroup.add(thePattern);
		return this;
	}

	private StatementPattern newPattern(Var theSubj, Var thePred, Var theObj) {
		return new StatementPattern(mScope, theSubj, thePred, theObj, mContext);
	}

	public static Var valueToVar(Value theValue) {
		Var aVar = new Var(BasicUtils.getRandomString(4), theValue);
		aVar.setAnonymous(true);

		return aVar;
	}
}
