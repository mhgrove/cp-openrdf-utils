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
