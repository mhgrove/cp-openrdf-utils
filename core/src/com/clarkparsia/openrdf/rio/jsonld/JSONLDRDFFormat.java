package com.clarkparsia.openrdf.rio.jsonld;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriterRegistry;

/**
 * Definition of the JSON-LD RDFFormat.
 * 
 * @author Edgar Rodriguez <edgar@clarkparsia.com>
 * @since 0.7
 */
public class JSONLDRDFFormat {
	
	/**
	 * JSON-LD RDFFormat defining mime-type as in the latest spec: 'application/ld+json'
	 * http://json-ld.org/spec/latest/json-ld-syntax/#iana-considerations
	 */
    public static final RDFFormat FORMAT = new RDFFormat(
            "JSON-LD",
            Arrays.asList("application/ld+json", "application/json"),
            Charset.forName("UTF-8"),
            Arrays.asList("jsonld","js", "json"),
            false,
            false
    );
    
    /**
     * Register the JSON-LD RDFFormat and the JSONLDWriterFactory
     */
    static {
        RDFFormat.register(FORMAT);
        RDFWriterRegistry registry = RDFWriterRegistry.getInstance();
        registry.add(new JSONLDWriterFactory());
    }
}
