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

package com.clarkparsia.openrdf.query.serql;

import com.clarkparsia.openrdf.query.QueryRenderer;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;

/**
 * <p>Implementation of the {@link QueryRenderer} interface which renders {@link org.openrdf.query.parser.ParsedQuery}
 * objects as strings in SeRQL syntax</p>
 *
 * @author Michael Grove
 * @since 0.2
 */
public class SeRQLQueryRenderer implements QueryRenderer {

	public static boolean SERQL_ONE_X_COMPATIBILITY_MODE = false;

	/**
	 * The renderer object
	 */
	private SerqlTupleExprRenderer mRenderer = new SerqlTupleExprRenderer();

	/**
	 * @inheritDoc
	 */
	public QueryLanguage getLanguage() {
		return QueryLanguage.SERQL;
	}

	/**
	 * @inheritDoc
	 */
	public String render(final ParsedQuery theQuery) throws Exception {
		mRenderer.reset();

		return mRenderer.render(theQuery.getTupleExpr());
	}
}
