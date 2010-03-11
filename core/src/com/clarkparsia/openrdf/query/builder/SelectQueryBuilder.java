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

/**
 * <p>Extends the QueryBuilder interface to provide Select query specific functions.</p>
 *
 * @author Michael Grove
 * @version 0.2.1
 * @since 0.2.1
 */
public interface SelectQueryBuilder extends QueryBuilder<ParsedTupleQuery> {
    /**
     * Specify that this query should use the "distinct" keyword
     * @return this query builder
     */
    public SelectQueryBuilder distinct();

    /**
     * Specify that this query should use the "reduced" keyword
     * @return this query builder
     */
    public SelectQueryBuilder reduced();

}
