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
import org.openrdf.query.parser.ParsedQuery;
import com.clarkparsia.openrdf.query.builder.BasicGroup;

/**
 * <p>Builder class for creating a filter expression in a query.</p>
 *
 * @author Michael Grove
 * @since 0.2
 * @since 0.2.2
 */
public class FilterBuilder<T extends ParsedQuery, E extends SupportsGroups> {
	// TODO: merge this somehow with ValueExprFactory
	
	private GroupBuilder<T, E> mGroup;

	FilterBuilder(final GroupBuilder<T, E> theGroup) {
		mGroup = theGroup;
	}

    public GroupBuilder<T, E> filter(ValueExpr theExpr) {
		((BasicGroup)mGroup.getGroup()).addFilter(theExpr);

		return mGroup;
	}

	public GroupBuilder<T, E> bound(String theVar) {
		return filter(new Bound(new Var(theVar)));
	}

	public GroupBuilder<T, E> not(ValueExpr theExpr) {
		return filter(new Not(theExpr));
	}

	public GroupBuilder<T, E> and(ValueExpr theLeft, ValueExpr theRight) {
		return filter(new And(theLeft, theRight));
	}

	public GroupBuilder<T, E> or(ValueExpr theLeft, ValueExpr theRight) {
		return filter(new Or(theLeft, theRight));
	}

	public GroupBuilder<T, E> sameTerm(ValueExpr theLeft, ValueExpr theRight) {
		return filter(new SameTerm(theLeft, theRight));
	}

	public GroupBuilder<T, E> regex(ValueExpr theExpr, String thePattern) {
		return regex(theExpr, thePattern, null);
	}

	public GroupBuilder<T, E> langMatches(ValueExpr theLeft, ValueExpr theRight) {
		return filter(new LangMatches(theLeft, theRight));
	}

	public GroupBuilder<T, E> lt(String theVar, String theOtherVar) {
		return filter(ValueExprFactory.lt(theVar, theOtherVar));
	}

	public GroupBuilder<T, E> lt(String theVar, Value theValue) {
		return filter(ValueExprFactory.lt(theVar, theValue));
	}

	public GroupBuilder<T, E> gt(String theVar, String theOtherVar) {
		return filter(ValueExprFactory.gt(theVar, theOtherVar));
	}

	public GroupBuilder<T, E> gt(String theVar, Value theValue) {
		return filter(ValueExprFactory.gt(theVar, theValue));
	}

	public GroupBuilder<T, E> eq(String theVar, String theOtherVar) {
		return filter(ValueExprFactory.eq(theVar, theOtherVar));
	}

	public GroupBuilder<T, E> eq(String theVar, Value theValue) {
		return filter(ValueExprFactory.eq(theVar, theValue));
	}

	public GroupBuilder<T, E> ne(String theVar, String theOtherVar) {
		return filter(ValueExprFactory.ne(theVar, theOtherVar));
	}

	public GroupBuilder<T, E> ne(String theVar, Value theValue) {
		return filter(ValueExprFactory.ne(theVar, theValue));
	}

	public GroupBuilder<T, E> le(String theVar, String theOtherVar) {
		return filter(ValueExprFactory.le(theVar, theOtherVar));
	}

	public GroupBuilder<T, E> le(String theVar, Value theValue) {
		return filter(ValueExprFactory.le(theVar, theValue));
	}

	public GroupBuilder<T, E> ge(String theVar, String theOtherVar) {
		return filter(ValueExprFactory.ge(theVar, theOtherVar));
	}

	public GroupBuilder<T, E> ge(String theVar, Value theValue) {
		return filter(ValueExprFactory.ge(theVar, theValue));
	}

	public GroupBuilder<T, E> regex(ValueExpr theExpr, String thePattern, String theFlags) {
		Regex aRegex = new Regex();
		aRegex.setArg(theExpr);
		aRegex.setPatternArg(new ValueConstant(ValueFactoryImpl.getInstance().createLiteral(thePattern)));
		if (theFlags != null) {
			aRegex.setFlagsArg(new ValueConstant(ValueFactoryImpl.getInstance().createLiteral(theFlags)));
		}

		return filter(aRegex);
	}
}
