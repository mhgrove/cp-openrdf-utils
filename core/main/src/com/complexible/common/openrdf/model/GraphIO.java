/*
 * Copyright (c) 2009-2013 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.complexible.common.openrdf.model;

import com.complexible.common.openrdf.util.GraphBuildingRDFHandler;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;

import com.google.common.base.Charsets;
import org.openrdf.rio.helpers.BasicParserSettings;

/**
 * <p>Collection of utility methods for doing IO operations with RIO and the OpenRdf API with the {@link Graph} class</p>
 *
 * @author	Michael Grove
 * @since	0.1
 * @version	2.0
 */
public final class GraphIO {

	/**
	 * No instances
	 */
	private GraphIO() {
        throw new AssertionError();
	}

	/**
	 * Read an RDF graph from the specified file
	 * @param theFile	the file to read from
	 * @return			the RDF graph contained in the file
	 *
	 * @throws IOException			if there was an error reading from the file
	 * @throws RDFParseException	if the RDF could not be parsed
	 */
	public static Graph readGraph(final File theFile) throws IOException, RDFParseException {
		return readGraph(new FileInputStream(theFile), RDFFormat.forFileName(theFile.getName()));
	}

	/**
	 * Read an RDF graph from the stream using the specified format
	 * @param theInput the stream to read from
	 * @param theFormat the format the data is in
	 * @return the graph represented by the data from the stream
	 * @throws IOException if there is an error while reading
	 * @throws RDFParseException if there is an error while trying to parse the data as the specified format
	 */
	public static Graph readGraph(InputStream theInput, RDFFormat theFormat) throws IOException, RDFParseException {
		return readGraph(new InputStreamReader(theInput, Charsets.UTF_8), theFormat);
	}


	/**
	 * Read an RDF graph from the stream using the specified format
	 * @param theInput the stream to read from
	 * @param theFormat the format the data is in
	 * @param theBase the base url used for parsing
	 * @return the graph represented by the data from the stream
	 * @throws IOException if there is an error while reading
	 * @throws RDFParseException if there is an error while trying to parse the data as the specified format
	 */
	public static Graph readGraph(InputStream theInput, RDFFormat theFormat, String theBase) throws IOException, RDFParseException {
		return readGraph(new InputStreamReader(theInput, Charsets.UTF_8), theFormat, theBase);
	}

	/**
	 * Read an RDF graph from the Reader using the specified format
	 * @param theInput the reader to read from
	 * @param theFormat the format the data is in
	 * @return the graph represented by the data from the stream
	 * @throws IOException if there is an error while reading
	 * @throws RDFParseException if there is an error while trying to parse the data as the specified format
	 */
	public static Graph readGraph(Reader theInput, RDFFormat theFormat) throws IOException, RDFParseException {
		return readGraph(theInput, theFormat, "http://openrdf.clarkparsia.com/");
	}

	/**
	 * Read an RDF graph from the Reader using the specified format.  The reader is closed after parsing.
	 *
	 * @param theInput the reader to read from
	 * @param theFormat the format the data is in
	 * @param theBase the base url for parsing
	 * @return the graph represented by the data from the stream
	 * @throws IOException if there is an error while reading
	 * @throws RDFParseException if there is an error while trying to parse the data as the specified format
	 */
	public static Graph readGraph(Reader theInput, RDFFormat theFormat, String theBase) throws IOException, RDFParseException {
		RDFParser aParser = Rio.createParser(theFormat);

        aParser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        aParser.getParserConfig().set(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, false);
        aParser.getParserConfig().set(BasicParserSettings.NORMALIZE_DATATYPE_VALUES, false);
        aParser.getParserConfig().set(BasicParserSettings.PRESERVE_BNODE_IDS, true);

		GraphBuildingRDFHandler aHandler = new GraphBuildingRDFHandler();

		aParser.setRDFHandler(aHandler);

		try {
			aParser.parse(theInput, theBase);
		}
		catch (RDFHandlerException e) {
			throw new RDFParseException(e);
		}
		finally {
			if (theInput != null) {
				theInput.close();
			}
		}

		return aHandler.getGraph();
	}

	/**
	 * Read an RDF graph from the Reader using the specified format.  The reader is closed after parsing.
	 *
	 * @param theHandler the handler for the results of reading the data
	 * @param theInput the reader to read from
	 * @param theFormat the format the data is in
	 * @param theBase the base url for parsing
	 * @throws IOException if there is an error while reading
	 * @throws RDFParseException if there is an error while trying to parse the data as the specified format
	 */
	public static void readGraph(RDFHandler theHandler, Reader theInput, RDFFormat theFormat, String theBase) throws IOException, RDFParseException {
		RDFParser aParser = Rio.createParser(theFormat);

        aParser.getParserConfig().set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
        aParser.getParserConfig().set(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, false);
        aParser.getParserConfig().set(BasicParserSettings.NORMALIZE_DATATYPE_VALUES, false);
        aParser.getParserConfig().set(BasicParserSettings.PRESERVE_BNODE_IDS, true);

		aParser.setRDFHandler(theHandler);

		try {
			aParser.parse(theInput, theBase);
		}
		catch (RDFHandlerException e) {
			throw new RDFParseException(e);
		}
		finally {
			if (theInput != null) {
				theInput.close();
			}
		}
	}

	/**
	 * Iterate over the RDF Graph contained in the input stream, notifying the RDF handler for each statement
	 * @param theHandler the RDFHandler to receive notifications of statmements in the graph
	 * @param theInput the input to read the RDF from
	 * @param theFormat the format of the RDF
	 * @throws IOException if there is an error reading the RDF
	 * @throws RDFParseException if it is not valid RDF
	 */
	public static void iterateGraph(RDFHandler theHandler, InputStream theInput, RDFFormat theFormat) throws IOException, RDFParseException {
		RDFParser aParser = Rio.createParser(theFormat);

		aParser.setRDFHandler(theHandler);

		try {
			aParser.parse(theInput, "http://openrdf.clarkparsia.com/");
		}
		catch (RDFHandlerException e) {
			throw new RDFParseException(e);
		}
		finally {
			if (theInput != null) {
				theInput.close();
			}
		}
	}

	public static void writeGraph(final Graph theGraph, final OutputStream theStream, final RDFFormat theFormat) throws IOException {
		writeGraph(theGraph, new OutputStreamWriter(theStream), theFormat);
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

	/**
	 * Write the Graph to a String in the given format
	 * @param theGraph	the graph to write
	 * @param theFormat	the RDF format to write in
	 * @return			the Graph as RDF
	 */
	public static String toString(final Graph theGraph, final RDFFormat theFormat) {
		try {
			StringWriter aStringWriter = new StringWriter();
			writeGraph(theGraph, aStringWriter, theFormat);
			return aStringWriter.toString();
		}
		catch (IOException e) {
			// this should not happen w/ a StringWriter
			throw new RuntimeException(e);
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
