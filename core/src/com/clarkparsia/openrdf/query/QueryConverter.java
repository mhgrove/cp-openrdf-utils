package com.clarkparsia.openrdf.query;

import org.openrdf.query.parser.sparql.SPARQLParserFactory;
import org.openrdf.query.parser.QueryParser;

import org.openrdf.query.parser.serql.SeRQLParserFactory;

import com.clarkparsia.openrdf.query.sparql.SPARQLQueryRenderer;
import com.clarkparsia.openrdf.query.serql.SeRQLQueryRenderer;
import com.clarkparsia.openrdf.OpenRdfUtil;

/**
 * <p></p>
 *
 * @author Michael Grove
 */
public class QueryConverter {

	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("missing required args");
		}

		if (System.getProperty("compatibility.mode") != null && System.getProperty("compatibility.mode").equalsIgnoreCase("true")) {
			SeRQLQueryRenderer.SERQL_ONE_X_COMPATIBILITY_MODE = true;
		}

		String aFrom = args[0].toLowerCase().trim();
		String aTo = args[1].toLowerCase().trim();
		String aQuery = args[2];

		QueryParser aParser = null;
		QueryRenderer aRenderer = null;

		if (aFrom.equals("sparql")) {
			aParser = new SPARQLParserFactory().getParser();
		}
		else if (aFrom.equals("serql")) {
			aParser = new SeRQLParserFactory().getParser();
		}

		if (aTo.equals("sparql")) {
			aRenderer = new SPARQLQueryRenderer();
		}
		else if (aTo.equals("serql")) {
			aRenderer = new SeRQLQueryRenderer();
		}

		try {
			System.out.println(aRenderer.render(aParser.parseQuery(aQuery, "http://example.org")));
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
