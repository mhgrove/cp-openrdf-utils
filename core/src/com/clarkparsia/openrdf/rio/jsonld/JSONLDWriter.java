package com.clarkparsia.openrdf.rio.jsonld;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

import de.dfki.km.json.jsonld.JSONLDSerializer;
import de.dfki.km.json.jsonld.impl.SesameJSONLDSerializer;

/**
 * An implementation of the RDFWriter interface that writes RDF documents in
 * JSON-LD format. The JSON-LD format is defined in <a
 * href="http://json-ld.org/spec/latest/json-ld-syntax/">in this document</a> 
 * and is using the <a href="https://github.com/edgarRd/jsonld-java.git">jsonld-java</a> 
 * implementation of its <a href="http://json-ld.org/spec/latest/json-ld-api/">API</a>.
 * 
 * @author Edgar Rodriguez <edgar@clarkparsia.com>
 * @since 0.7
 */
public class JSONLDWriter implements RDFWriter {
	
	protected Writer mWriter;
	protected SesameJSONLDSerializer mSerializer;
	protected Map<String, String> mNamespaceTable;
	
	protected boolean mWritingStarted;
	
	/*--------------*
	 * Constructors *
	 *--------------*/
	
	public JSONLDWriter(OutputStream out) {
		this(new OutputStreamWriter(out, Charset.forName("UTF-8")));
	}
	
	public JSONLDWriter(Writer writer) {
		mWriter = writer;
		mNamespaceTable = new LinkedHashMap<String, String>();
		mSerializer = new SesameJSONLDSerializer();
		mWritingStarted = false;
	}
	
	/*---------*
	 * Methods *
	 *---------*/
	
	public JSONLDSerializer getSerializer() {
		return this.mSerializer;
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	public void startRDF() throws RDFHandlerException {
		
		if (mWritingStarted) {
			throw new RuntimeException("Document writing has already started");
		}
		
		mWritingStarted = true;
		
		try {
			// Add namespace declarations.
			for (Map.Entry<String, String> entry : mNamespaceTable.entrySet()) {
				String name = entry.getKey();
				String prefix = entry.getValue();
				
				mSerializer.setPrefix(name, prefix);
			}
		}
		catch (Exception e) {
			throw new RDFHandlerException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void endRDF() throws RDFHandlerException {
		
		if (!mWritingStarted) {
			throw new RuntimeException("Document writing has not yet started");
		}
		
		try {
			// Dump serialization in writer.
			mSerializer.toWriter(mWriter);
			mWriter.flush();
		}
		catch(IOException ioe) {
			throw new RDFHandlerException(ioe);
		}
		finally {
			mWritingStarted = false;
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void handleComment(String arg0) throws RDFHandlerException {
		// No support for comments.
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void handleNamespace(String prefix, String name)
			throws RDFHandlerException {
		
		try {
			if (!mNamespaceTable.containsKey(name)) {
				
				boolean isLegalPrefix = prefix.length() == 0;
				
				if (!isLegalPrefix || mNamespaceTable.containsValue(prefix)) {
					prefix = "ns";
				
					
					int number = 1;
					
					while (mNamespaceTable.containsValue(prefix + number)) {
						number++;
					}
					
					prefix += number;
				}
				mNamespaceTable.put(name, prefix);
				
				if (mWritingStarted) {
					mSerializer.setPrefix(name, prefix);
				}
			}
		}
		catch (Exception e) {
			throw new RDFHandlerException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void handleStatement(Statement theStatement) throws RDFHandlerException {
		mSerializer.handleStatement(theStatement);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public RDFFormat getRDFFormat() {
		return JSONLDRDFFormat.FORMAT;
	}

}
