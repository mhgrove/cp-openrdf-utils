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

package com.clarkparsia.openrdf;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParseException;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.util.RDFInserter;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import java.io.Writer;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;

import com.clarkparsia.openrdf.util.GraphBuildingRDFHandler;
import static com.clarkparsia.openrdf.OpenRdfUtil.close;

/**
 * <p>Collection of utility methods for doing IO operations with RIO and the OpenRdf API</p>
 *
 * @author Michael Grove
 */
public class OpenRdfIO {

	/**
	 * The logger
	 */
	private static Logger LOGGER = LogManager.getLogger("com.clarkparsia.openrdf");

	/**
	 * Read an RDF graph from the stream using the specified format
	 * @param theInput the stream to read from
	 * @param theFormat the format the data is in
	 * @return the graph represented by the data from the stream
	 * @throws IOException if there is an error while reading
	 * @throws RDFParseException if there is an error while trying to parse the data as the specified format
	 */
	public static Graph readGraph(InputStream theInput, RDFFormat theFormat) throws IOException, RDFParseException {
		return readGraph(new InputStreamReader(theInput), theFormat);
	}

	/**
	 * Read an RDF graph from the Reader using the specified format
	 * @param theInput the reader to read from
	 * @param theFormat the format the data is in
	 * @return the graph represented by the data from the stream
	 * @throws IOException if there is an error while reading
	 * @throws RDFParseException if there is an error while trying to parse the data as the specified format
	 */
	public static ExtGraph readGraph(Reader theInput, RDFFormat theFormat) throws IOException, RDFParseException {
		RDFParser aParser = Rio.createParser(theFormat);

		GraphBuildingRDFHandler aHandler = new GraphBuildingRDFHandler();

		aParser.setRDFHandler(aHandler);

		try {
			aParser.parse(theInput, "");
		}
		catch (RDFHandlerException e) {
			throw new RDFParseException(e);
		}

		return aHandler.getGraph();
	}
	
	/**
	 * Write the contents of the Graph to the writer in the specified RDF format
	 * @param theGraph the graph to write
	 * @param theWriter the stream to write to
	 * @param theFormat the RDF format to write in
	 * @throws IOException thrown if there is an error while writing
	 */
	public static void writeGraph(Graph theGraph, Writer theWriter, RDFFormat theFormat) throws IOException {
		writeGraph(theGraph, Rio.createWriter(theFormat, theWriter));
	}

	public static void addData(Repository theRepo, File theFile) throws RDFParseException, IOException {
		addData(theRepo, new FileInputStream(theFile),Rio.getParserFormatForFileName(theFile.getName()));
	}

	public static void addData(Repository theRepo, InputStream theStream, RDFFormat theFormat) throws RDFParseException, IOException {
		addData(theRepo, new InputStreamReader(theStream), theFormat);
	}

	public static void addData(Repository theRepo, Reader theStream, RDFFormat theFormat) throws RDFParseException, IOException {
		RDFParser aParser = Rio.createParser(theFormat);

		RepositoryConnection aConn = null;

		try {
			aConn = theRepo.getConnection();

			aParser.setRDFHandler(new RDFInserter(aConn));

			aParser.parse(theStream, "");
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		finally {
			close(aConn);
		}
	}

	private static void writeGraph(Graph theGraph, RDFWriter theWriter) throws IOException {
		try {
			theWriter.startRDF();

			for (Statement aStmt : theGraph) {
				theWriter.handleStatement(aStmt);
			}

			theWriter.endRDF();
		}
		catch (RDFHandlerException e) {
			throw new IOException(e);
		}
	}
}
