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

import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.LangMatches;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Var;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.Value;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
public class FilterFactory {
	// TODO: merge this somehow with ValueExprFactory
	
	private GroupFactory mGroup;

	FilterFactory(final GroupFactory theGroup) {
		mGroup = theGroup;
	}

	public GroupFactory filter(ValueExpr theExpr) {
		mGroup.getGroup().addFilter(theExpr);

		return mGroup;
	}

	public GroupFactory bound(String theVar) {
		return filter(new Bound(new Var(theVar)));
	}

	public GroupFactory not(ValueExpr theExpr) {
		return filter(new Not(theExpr));
	}

	public GroupFactory and(ValueExpr theLeft, ValueExpr theRight) {
		return filter(new And(theLeft, theRight));
	}

	public GroupFactory or(ValueExpr theLeft, ValueExpr theRight) {
		return filter(new Or(theLeft, theRight));
	}

	public GroupFactory sameTerm(ValueExpr theLeft, ValueExpr theRight) {
		return filter(new SameTerm(theLeft, theRight));
	}

	public GroupFactory regex(ValueExpr theExpr, String thePattern) {
		return regex(theExpr, thePattern, null);
	}

	public GroupFactory langMatches(ValueExpr theLeft, ValueExpr theRight) {
		return filter(new LangMatches(theLeft, theRight));
	}

	public GroupFactory lt(String theVar, String theOtherVar) {
		return filter(ValueExprFactory.lt(theVar, theOtherVar));
	}

	public GroupFactory lt(String theVar, Value theValue) {
		return filter(ValueExprFactory.lt(theVar, theValue));
	}

	public GroupFactory gt(String theVar, String theOtherVar) {
		return filter(ValueExprFactory.gt(theVar, theOtherVar));
	}

	public GroupFactory gt(String theVar, Value theValue) {
		return filter(ValueExprFactory.gt(theVar, theValue));
	}

	public GroupFactory eq(String theVar, String theOtherVar) {
		return filter(ValueExprFactory.eq(theVar, theOtherVar));
	}

	public GroupFactory eq(String theVar, Value theValue) {
		return filter(ValueExprFactory.eq(theVar, theValue));
	}

	public GroupFactory ne(String theVar, String theOtherVar) {
		return filter(ValueExprFactory.ne(theVar, theOtherVar));
	}

	public GroupFactory ne(String theVar, Value theValue) {
		return filter(ValueExprFactory.ne(theVar, theValue));
	}

	public GroupFactory le(String theVar, String theOtherVar) {
		return filter(ValueExprFactory.le(theVar, theOtherVar));
	}

	public GroupFactory le(String theVar, Value theValue) {
		return filter(ValueExprFactory.le(theVar, theValue));
	}

	public GroupFactory ge(String theVar, String theOtherVar) {
		return filter(ValueExprFactory.ge(theVar, theOtherVar));
	}

	public GroupFactory ge(String theVar, Value theValue) {
		return filter(ValueExprFactory.ge(theVar, theValue));
	}

	public GroupFactory regex(ValueExpr theExpr, String thePattern, String theFlags) {
		Regex aRegex = new Regex();
		aRegex.setArg(theExpr);
		aRegex.setPatternArg(new ValueConstant(ValueFactoryImpl.getInstance().createLiteral(thePattern)));
		if (theFlags != null) {
			aRegex.setFlagsArg(new ValueConstant(ValueFactoryImpl.getInstance().createLiteral(theFlags)));
		}

		return filter(aRegex);
	}
}
