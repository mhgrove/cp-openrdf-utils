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
