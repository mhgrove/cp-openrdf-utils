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

package com.clarkparsia.openrdf.rio.jsonld;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Test;
import org.openrdf.model.Graph;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.UnsupportedRDFormatException;

import com.clarkparsia.openrdf.OpenRdfIO;

public class TestJSONLD {
	
	@Test
	public void testJSONWriterFactory() {
		RDFWriterFactory aWriterFactory = new JSONLDWriterFactory();
		
		RDFFormat aFormat = aWriterFactory.getRDFFormat();
		assertTrue("RDFFormat for JSON-LD Writer is null", (aFormat != null));
		assertTrue(aFormat.getName().equals("JSON-LD"));
	}
	
	@Test
	public void testJSONLDWriter() {
		RDFWriterFactory writerFact = RDFWriterRegistry.getInstance().get(JSONLDRDFFormat.FORMAT);
		
		assertTrue("Factory not found!", (writerFact != null));
	}

	@Test
	public void testSerialization() {
		// Get data file
		try {
			FileInputStream fIn = new FileInputStream("test/data/test0.ttl");
			String aExpectedSerialization = Files.toString(new File("test/data/test0.jsonld"), Charsets.UTF_8);
			Graph graph = OpenRdfIO.readGraph(fIn, RDFFormat.TURTLE);
			
			// Once we have the data in the graph, serialize it to JSON-LD
			StringWriter sWriter = new StringWriter();
			OpenRdfIO.writeGraph(graph, sWriter, JSONLDRDFFormat.FORMAT);
			
			assertEquals("Serialization was not as expected.", aExpectedSerialization, sWriter.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			assertTrue("Data file not found!", false);
		} catch (RDFParseException e) {
			e.printStackTrace();
			assertTrue("RDF Parse Error found!", false);
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue("IOException thrown!", false);
		} catch (UnsupportedRDFormatException e) {
			e.printStackTrace();
			assertTrue("RDFFormat not correctly registered!", false);
		}
	}
}
