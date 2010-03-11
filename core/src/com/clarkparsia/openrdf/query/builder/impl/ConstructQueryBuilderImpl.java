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

import com.clarkparsia.openrdf.query.builder.ConstructQueryBuilder;
import com.clarkparsia.openrdf.query.builder.GroupBuilder;

import org.openrdf.query.parser.ParsedGraphQuery;

import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;

import org.openrdf.model.Value;

/**
 * <p>Implementation of a ConstructQueryBuilder</p>
 *
 * @author Michael Grove
 * @since 0.2.1
 * @version 0.2.1
 */
public class ConstructQueryBuilderImpl extends AbstractQueryBuilder<ParsedGraphQuery> implements ConstructQueryBuilder {

	public ConstructQueryBuilderImpl() {
		super(new ParsedGraphQuery());
	}

	/**
	 * @inheritDoc
	 */
    public ConstructQueryBuilder addProjectionStatement(final String theSubj, final String thePred, final String theObj) {
        mProjectionPatterns.add(new StatementPattern(new Var(theSubj), new Var(thePred), new Var(theObj)));

        return this;
    }

	/**
	 * @inheritDoc
	 */
    public ConstructQueryBuilder addProjectionStatement(final String theSubj, final Value thePred, final Value theObj) {
        mProjectionPatterns.add(new StatementPattern(new Var(theSubj), GroupBuilder.valueToVar(thePred), GroupBuilder.valueToVar(theObj)));

        return this;
    }

	/**
	 * @inheritDoc
	 */
    public ConstructQueryBuilder addProjectionStatement(final String theSubj, final String thePred, final Value theObj) {
        mProjectionPatterns.add(new StatementPattern(new Var(theSubj), new Var(thePred), GroupBuilder.valueToVar(theObj)));

        return this;
    }
}
