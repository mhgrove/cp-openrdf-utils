package com.clarkparsia.openrdf.rio.jsonld;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriterRegistry;

public class JSONLDRDFFormat {
	
    public static final RDFFormat FORMAT = new RDFFormat(
            "JSON-LD",
            Arrays.asList("application/ld+json", "application/json"),
            Charset.forName("UTF-8"),
            Arrays.asList("jsonld","js", "json"),
            false,
            false
    );
    
    static {
        RDFFormat.register(FORMAT);
        RDFWriterRegistry registry = RDFWriterRegistry.getInstance();
        registry.add(new JSONLDWriterFactory());
    }
}
