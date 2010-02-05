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
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.model.Value;
import com.clarkparsia.utils.BasicUtils;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
public class GroupFactory {
	private QueryBuilder mBuilder;
	private Group mGroup;

	private StatementPattern.Scope mScope = StatementPattern.Scope.DEFAULT_CONTEXTS;
	private Var mContext = null;

	GroupFactory(final QueryBuilder theBuilder) {
		this(theBuilder, false, null);
	}

	GroupFactory(final QueryBuilder theBuilder, boolean theOptional) {
		this(theBuilder, theOptional, null);
	}

	GroupFactory(final QueryBuilder theBuilder, boolean theOptional, Group theParent) {
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

	public GroupFactory group() {
		return new GroupFactory(mBuilder, false, mGroup);
	}

	public GroupFactory optional() {
		return new GroupFactory(mBuilder, true, mGroup);
	}

	public QueryBuilder closeGroup() {
		return mBuilder;
	}

	public GroupFactory setScope(StatementPattern.Scope theScope) {
		mScope = theScope;
		return this;
	}

	public GroupFactory setContext(String theContextVar) {
		mContext = new Var(theContextVar);
		return this;
	}

	public GroupFactory setContext(Value theContextValue) {
		mContext = valueToVar(theContextValue);
		return this;
	}

	public FilterFactory filter() {
		return new FilterFactory(this);
	}

	public GroupFactory filter(String theVar, Compare.CompareOp theOp, Value theValue) {
		Compare aComp = new Compare(new Var(theVar), new ValueConstant(theValue), theOp);
		mGroup.addFilter(aComp);

		return this;
	}

	public GroupFactory atom(String theSubjVar, String thePredVar, String theObjVar) {
		return addPattern(newPattern(new Var(theSubjVar), new Var(thePredVar), new Var(theObjVar)));
	}

	public GroupFactory atom(String theSubjVar, String thePredVar, Value theObj) {
		return addPattern(newPattern(new Var(theSubjVar), new Var(thePredVar), valueToVar(theObj)));
	}

	public GroupFactory atom(String theSubjVar, Value thePredVar, String theObj) {
		return addPattern(newPattern(new Var(theSubjVar), valueToVar(thePredVar), new Var(theObj)));
	}

	public GroupFactory atom(String theSubjVar, Value thePred, Value theObj) {
		return addPattern(newPattern(new Var(theSubjVar), valueToVar(thePred), valueToVar(theObj)));
	}

	public GroupFactory atom(Value theSubjVar, Value thePredVar, Value theObj) {
		return addPattern(newPattern(valueToVar(theSubjVar), valueToVar(thePredVar), valueToVar(theObj)));
	}

	public GroupFactory atom(Value theSubjVar, Value thePredVar, String theObj) {
		return addPattern(newPattern(valueToVar(theSubjVar), valueToVar(thePredVar), new Var(theObj)));
	}

	public GroupFactory atom(Value theSubjVar, String thePredVar, String theObj) {
		return addPattern(newPattern(valueToVar(theSubjVar), new Var(thePredVar), new Var(theObj)));
	}

	private GroupFactory addPattern(StatementPattern thePattern) {
		mGroup.add(thePattern);
		return this;
	}

	private StatementPattern newPattern(Var theSubj, Var thePred, Var theObj) {
		return new StatementPattern(mScope, theSubj, thePred, theObj, mContext);
	}

	private Var valueToVar(Value theValue) {
		Var aVar = new Var(BasicUtils.getRandomString(4), theValue);
		aVar.setAnonymous(true);

		return aVar;
	}
}
