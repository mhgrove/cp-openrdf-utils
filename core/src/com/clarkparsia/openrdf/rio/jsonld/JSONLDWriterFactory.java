package com.clarkparsia.openrdf.rio.jsonld;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * An {@link RDFWriterFactory} for JSON-LD writers.
 * 
 * @author Edgar Rodriguez <edgar@clarkparsia.com>
 */
public class JSONLDWriterFactory {
	
	/**
	 * Returns a JSON-LD RDFFormat object.
	 * @return the {@link JSONLDRDFFormat}
	 */
	public RDFFormat getRDFFormat() {
		return JSONLDRDFFormat.FORMAT;
	}
	
	/**
	 * Returns a new instance of {@link JSONLDWriter}
	 * @param out the OutputStream
	 * @return the RDFWriter instance.
	 */
	public RDFWriter getWriter(OutputStream out) {
		return null;
	}
	
	/**
	 * Returns a new instance of {@link JSONLDWriter}
	 * @param writer the Writer.
	 * @return the RDFWriter instance.
	 */
	public RDFWriter getWriter(Writer writer) {
		return null;
	}
}
