package com.clarkparsia.openrdf.rio.jsonld;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

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
			String aExpectedSerialization = readFileAsString("test/data/test0.jsonld");
			Graph graph = OpenRdfIO.readGraph(fIn, RDFFormat.TURTLE);
			
			// Once we have the data in the graph, serialize it to JSON-LD
			StringWriter sWriter = new StringWriter();
			OpenRdfIO.writeGraph(graph, sWriter, JSONLDRDFFormat.FORMAT);
			
			assertTrue("Serialization was not as expected.", aExpectedSerialization.equals(sWriter.toString()));
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
	
	private static String readFileAsString(String filePath) throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead=0;
	
		while((numRead=reader.read(buf)) != -1){
	
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		
		reader.close();
		return fileData.toString();
	}
}
