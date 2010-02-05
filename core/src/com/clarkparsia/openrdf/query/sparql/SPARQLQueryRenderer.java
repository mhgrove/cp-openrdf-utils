package com.clarkparsia.openrdf.query.sparql;

import com.clarkparsia.openrdf.query.QueryRenderer;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;

/**
 * <p>Implementation of the {@link QueryRenderer} interface which renders queries into the SPARQL syntax.</p>
 *
 * @author Michael Grove
 * @since 0.2
 */
public class SPARQLQueryRenderer implements QueryRenderer {

	/**
	 * The query renderer
	 */
	private SparqlTupleExprRenderer mRenderer = new SparqlTupleExprRenderer();

	/**
	 * @inheritDoc
	 */
	public QueryLanguage getLanguage() {
		return QueryLanguage.SPARQL;
	}

	/**
	 * @inheritDoc
	 */
	public String render(final ParsedQuery theQuery) throws Exception {
		mRenderer.reset();

		return mRenderer.render(theQuery.getTupleExpr());
	}
}
