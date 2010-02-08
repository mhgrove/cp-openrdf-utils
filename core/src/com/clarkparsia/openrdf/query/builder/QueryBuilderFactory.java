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

/**
 * <p></p>
 *
 * @author Michael Grove
 * @since 0.2
 */
public class QueryBuilderFactory {
    public static QueryBuilder<ParsedTupleQuery> select() {
        return new AbstractQueryBuilder<ParsedTupleQuery>(new ParsedTupleQuery());
    }

    public static QueryBuilder<ParsedTupleQuery> select(String... theProjectionVars) {
        AbstractQueryBuilder<ParsedTupleQuery> aBuilder = new AbstractQueryBuilder<ParsedTupleQuery>(new ParsedTupleQuery());
        aBuilder.addProjectionVar(theProjectionVars);

        return aBuilder;
    }

    public static QueryBuilder<ParsedGraphQuery> construct() {
        return new AbstractQueryBuilder<ParsedGraphQuery>(new ParsedGraphQuery()).reduced();
    }
}
