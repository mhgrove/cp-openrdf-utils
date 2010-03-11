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

package com.clarkparsia.openrdf.query.builder.impl;

import com.clarkparsia.openrdf.query.builder.SelectQueryBuilder;

import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Reduced;

/**
 * <p>Implementation of a SelectQueryBuilder</p>
 *
 * @author Michael Grove
 * @version 0.2.1
 * @since 0.2.1
 */
public class SelectQueryBuilderImpl extends AbstractQueryBuilder<ParsedTupleQuery> implements SelectQueryBuilder {
	private boolean mDistinct = false;
	private boolean mReduced = false;

	public SelectQueryBuilderImpl() {
		super(new ParsedTupleQuery());
	}

	/**
	 * @inheritDoc
	 */
	public SelectQueryBuilder distinct() {
		mDistinct = true;
		return this;
	}

	/**
	 * @inheritDoc
	 */
	public SelectQueryBuilder reduced() {
		mReduced = true;
		return this;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void reset() {
		mDistinct = mReduced = false;
		super.reset();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public ParsedTupleQuery query() {
		ParsedTupleQuery aQuery = super.query();

		if (mDistinct) {
			Distinct aDistinct = new Distinct();

			aDistinct.setArg(aQuery.getTupleExpr());
			aQuery.setTupleExpr(aDistinct);
		}

		if (mReduced) {
			Reduced aReduced = new Reduced();

			aReduced.setArg(aQuery.getTupleExpr());
			aQuery.setTupleExpr(aReduced);
		}

		return aQuery;
	}
}
