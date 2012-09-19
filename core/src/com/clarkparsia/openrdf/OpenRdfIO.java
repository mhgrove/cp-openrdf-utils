/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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
import org.openrdf.rio.RDFHandler;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.RDFInserter;

import java.io.StringWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import com.clarkparsia.openrdf.util.GraphBuildingRDFHandler;
import static com.clarkparsia.openrdf.OpenRdfUtil.close;
import com.google.common.base.Charsets;

/**
 * <p>Collection of utility methods for doing IO operations with RIO and the OpenRdf API.  Provides convenience
 * methods to add data to {@link Graph} and {@link Repository} objects and to write them out to a stream.</p>
 *
 * @author	Michael Grove
 * @since	0.1
 * @version	0.4
 */
public final class OpenRdfIO {

	/**
	 * No instances
	 */
	private OpenRdfIO() {
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
		aParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
		aParser.setPreserveBNodeIDs(true);

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
		aParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
		aParser.setPreserveBNodeIDs(true);

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

	public static void addData(Repository theRepo, File theFile) throws RDFParseException, IOException {
		addData(theRepo, new FileInputStream(theFile), Rio.getParserFormatForFileName(theFile.getName()));
	}

	public static void addData(Repository theRepo, InputStream theStream, RDFFormat theFormat) throws RDFParseException, IOException {
		addData(theRepo, new InputStreamReader(theStream, Charsets.UTF_8), theFormat);
	}

	public static void addData(Repository theRepo, Reader theStream, RDFFormat theFormat) throws RDFParseException, IOException {
		addData(theRepo, theStream, theFormat, null, null);
	}

	public static void addData(final Repository theRepo, final Reader theStream, final RDFFormat theFormat, final Resource theContext) throws IOException, RDFParseException {
		addData(theRepo, theStream, theFormat, theContext, null);
	}

	public static void addData(Repository theRepo, Reader theStream, RDFFormat theFormat, Resource theContext, String theBase) throws RDFParseException, IOException {
		RDFParser aParser = Rio.createParser(theFormat);
		
		aParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);
		aParser.setVerifyData(false);

		RepositoryConnection aConn = null;

		try {
			aConn = theRepo.getConnection();

			aConn.setAutoCommit(false);

			RDFInserter aInserter = new RDFInserter(aConn);

			if (theContext != null) {
				aInserter.enforceContext(theContext);
			}

			aParser.setRDFHandler(aInserter);

			aParser.parse(theStream, theBase == null ? (theContext != null ? theContext.stringValue() : "http://openrdf.clarkparsia.com") : theBase);

			aConn.commit();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		finally {
			close(aConn);
			if (theStream != null) theStream.close();
		}
	}

	/**
	 * Write the contents of the repository to the given file in the specified format
	 * @param theRepo the repository to write
	 * @param theFile the file to write to
	 * @param theFormat the format to write the RDF in
	 * @throws RepositoryException if there is an error getting the data from the repository
	 * @throws IOException if there is an error writing to the file
	 */
	public static void writeRepository(Repository theRepo, File theFile, RDFFormat theFormat) throws RepositoryException, IOException {
		writeRepository(theRepo, Rio.createWriter(theFormat, new OutputStreamWriter(new FileOutputStream(theFile), Charsets.UTF_8)));
	}

	/**
	 * Write the contents of the repository to the given stream in the specified format
	 * @param theRepo the repository to write
	 * @param theStream the stream to write to
	 * @param theFormat the format to write the RDF in
	 * @throws RepositoryException if there is an error getting the data from the repository
	 * @throws IOException if there is an error writing to the stream
	 */
	public static void writeRepository(Repository theRepo, OutputStream theStream, RDFFormat theFormat) throws RepositoryException, IOException {
		writeRepository(theRepo, Rio.createWriter(theFormat, new OutputStreamWriter(theStream, Charsets.UTF_8)));
	}

	/**
	 * Write the contents of the repository to the given writer in the specified format
	 * @param theRepo the repository to write
	 * @param theWriter the writer to write to
	 * @param theFormat the format to write the RDF in
	 * @throws RepositoryException if there is an error getting the data from the repository
	 * @throws IOException if there is an error writing to the writer
	 */
	public static void writeRepository(Repository theRepo, Writer theWriter, RDFFormat theFormat) throws RepositoryException, IOException {
		writeRepository(theRepo, Rio.createWriter(theFormat, theWriter));
	}

	private static void writeRepository(Repository theRepo, RDFWriter theWriter) throws IOException, RepositoryException {
		RepositoryConnection aConn = null;
		try {
			aConn = theRepo.getConnection();

			aConn.exportStatements(null, null, null, true, theWriter);
		}
		catch (RDFHandlerException e) {
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

	public static ExtRepository readRepository(final InputStream theStream, final RDFFormat theFormat) throws IOException, RDFParseException {
		return readRepository(new InputStreamReader(theStream, Charsets.UTF_8), theFormat);
	}

	public static ExtRepository readRepository(final Reader theStream, final RDFFormat theFormat) throws IOException, RDFParseException {
		ExtRepository aRepo = OpenRdfUtil.createInMemoryRepo();

		aRepo.read(theStream, theFormat);

		return aRepo;
	}
}
