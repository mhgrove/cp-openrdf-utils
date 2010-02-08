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
 * <p></p>
 *
 * @author Michael Grove
 */
public interface QueryBuilder<T extends ParsedQuery> {
    public T query();

    public QueryBuilder<T> offset(int theOffset);
    public QueryBuilder<T> limit(int theOffset);

    public QueryBuilder<T> distinct();
    public QueryBuilder<T> reduced();
    public GroupBuilder<T> optional();
    public GroupBuilder<T> group();

    public QueryBuilder<T> addProjectionVar(String... theNames);

    public void reset();

    public QueryBuilder<T> addGroup(Group theGroup);

    public QueryBuilder<T> addProjectionStatement(String theSubj, String thePred, String theObj);
    public QueryBuilder<T> addProjectionStatement(String theSubj, String thePred, Value theObj);
    public QueryBuilder<T> addProjectionStatement(String theSubj, Value thePred, Value theObj);
}
