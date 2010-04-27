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

import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.model.Value;
import org.openrdf.model.Resource;

/**
 * <p>Factory class for obtaining instances of {@link QueryBuilder} objects for the various types of queries.</p>
 *
 * @author Michael Grove
 * @since 0.2
 * @since 0.2.1
 */
public class QueryBuilderFactory {
    /**
     * Create a QueryBuilder for creating a select query
     * @return a select QueryBuilder
     */
    public static QueryBuilder<ParsedTupleQuery> select() {
        return new AbstractQueryBuilder<ParsedTupleQuery>(new ParsedTupleQuery());
    }

    /**
     * Create a QueryBuilder for creating a select query
     * @param theProjectionVars the list of elements in the projection of the query
     * @return a select query builder
     */
    public static QueryBuilder<ParsedTupleQuery> select(String... theProjectionVars) {
        QueryBuilder<ParsedTupleQuery> aBuilder = new AbstractQueryBuilder<ParsedTupleQuery>(new ParsedTupleQuery());
        aBuilder.addProjectionVar(theProjectionVars);

        return aBuilder;
    }

    /**
     * Create a QueryBuilder for building a construct query
     * @return a construct QueryBuilder
     */
    public static QueryBuilder<ParsedGraphQuery> construct() {
        return new AbstractQueryBuilder<ParsedGraphQuery>(new ParsedGraphQuery());
    }

	public static QueryBuilder<ParsedGraphQuery> describe(String[] theVars, Resource... theValues) {
		QueryBuilder<ParsedGraphQuery> aBuilder = new AbstractQueryBuilder<ParsedGraphQuery>(new ParsedGraphQuery());

		aBuilder.addProjectionStatement("-descr-subj", "-descr-pred", "-descr-obj");
		GroupBuilder<?,?> aGroup = aBuilder.group();

		for (String aVar : theVars) {
			aGroup.filter().or(new SameTerm(new Var(aVar), new Var("-descr-subj")),
				 			   new SameTerm(new Var(aVar), new Var("-descr-obj")));
		}

		for (Resource aVar : theValues) {
			aGroup.filter().or(new SameTerm(new ValueConstant(aVar), new Var("-descr-subj")),
				 			   new SameTerm(new ValueConstant(aVar), new Var("-descr-obj")));
		}

		aGroup.atom("-descr-subj", "-descr-pred", "-descr-obj");

		return aBuilder;
	}
}
