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

package com.clarkparsia.openrdf.query;

import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.QueryLanguage;

/**
 * <p>Interface for Sesame-based query renderers</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public interface QueryRenderer {

	/**
	 * Return the language this QueryRenderer will render queries in.
	 * @return the query language
	 */
	public QueryLanguage getLanguage();

	/**
	 * Render the query object to a string in the language supported by this renderer
	 * @param theQuery the query to render
	 * @return the rendered query
	 * @throws Exception if there is an error while rendering
	 */
	public String render(ParsedQuery theQuery) throws Exception;
}
