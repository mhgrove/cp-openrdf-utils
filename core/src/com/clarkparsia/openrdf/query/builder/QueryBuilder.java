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
import org.openrdf.query.parser.ParsedQuery;

/**
 * <p>Interface for a QueryBuilder which provides a simple fluent API for constructing Sesame query
 * object programmatically.</p>
 *
 * @author Michael Grove
 * @since 0.2
 */
public interface QueryBuilder<T extends ParsedQuery> {
    /**
     * Return the query constructed by this query builder
     * @return the query
     */
    public T query();

    /**
     * Specify an offset for the query
     * @param theOffset the new offset
     * @return this query builder
     */
    public QueryBuilder<T> offset(int theOffset);

    /**
     * Specify a limit for the query
     * @param theLimit the new limit for the query
     * @return this query builder
     */
    public QueryBuilder<T> limit(int theLimit);

    /**
     * Specify that this query should use the "distinct" keyword
     * @return this query builder
     */
    public QueryBuilder<T> distinct();

    /**
     * Specify that this query should use the "reduced" keyword
     * @return this query builder
     */
    public QueryBuilder<T> reduced();

    public GroupBuilder<T> optional();
    public GroupBuilder<T> group();

    public QueryBuilder<T> addProjectionVar(String... theNames);

    /**
     * Reset the state of the query builder
     */
    public void reset();

    public QueryBuilder<T> addGroup(Group theGroup);

    public QueryBuilder<T> addProjectionStatement(String theSubj, String thePred, String theObj);
    public QueryBuilder<T> addProjectionStatement(String theSubj, String thePred, Value theObj);
    public QueryBuilder<T> addProjectionStatement(String theSubj, Value thePred, Value theObj);
}
