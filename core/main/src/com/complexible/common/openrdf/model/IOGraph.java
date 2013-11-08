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

package com.complexible.common.openrdf.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.model.Graph;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 * <p>Extends the standard Sesame {@link Graph} to provide some RDF IO methods for inserting data from input or writing the Graph out.</p>
 *
 * @author Michael Grove
 * @since	0.8
 * @version	0.8
 *
 * @see GraphIO
 */
public interface IOGraph extends Graph {

	/**
	 * Insert the contents of the RDF file into this graph.
	 *
	 * @param theFile				the file to read
	 * @throws IOException			if the file cannot be read or there was an error while reading
	 * @throws RDFParseException	if the file does not contain valid RDF
	 */
	public void read(final File theFile) throws IOException, RDFParseException;


	/**
	 * Insert the RDF contained in the stream into this graph.
	 *
	 * @param theStream				the stream containing the RDf
	 * @param theFormat				the format of the RDF
	 *
	 * @throws IOException			if the file cannot be read or there was an error while reading
	 * @throws RDFParseException	if the file does not contain valid RDF
	 */
	public void read(final InputStream theStream, RDFFormat theFormat) throws IOException, RDFParseException;

	/**
	 * Write the contents of this graph in the specified format to the output stream
	 *
	 * @param theStream the stream to write to
	 * @param theFormat the format to write the data in
	 *
	 * @throws java.io.IOException if there is an error while writing to the stream
	 */
	public void write(OutputStream theStream, RDFFormat theFormat) throws IOException;

	/**
	 * Write the contents of this graph in the specified format to the output
	 *
	 * @param theWriter the output to write to
	 * @param theFormat the format to write the graph data in
	 *
	 * @throws IOException thrown if there is an error while writing the data
	 */
	public void write(Writer theWriter, RDFFormat theFormat) throws IOException;

	/**
	 * Return the graph as a String serialized in the specified RDF format
	 *
	 * @param theFormat the format to serialize in
	 *
	 * @return 			the graph as a string
	 */
	public String toString(final RDFFormat theFormat);
}
