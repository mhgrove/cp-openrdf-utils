package com.clarkparsia.openrdf.rio.jsonld;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.openrdf.rio.RDFFormat;

public class JSONLDRDFFormat {
	
    public static final RDFFormat FORMAT = new RDFFormat(
            "JSON-LD",
            Arrays.asList("application/json"),
            Charset.forName("UTF-8"),
            Arrays.asList("json","js"),
            false,
                        false
    );
    
    static {
        FORMAT.register(FORMAT);
    }
}
