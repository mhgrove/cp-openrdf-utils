package com.clarkparsia.openrdf.vocabulary;

import org.openrdf.model.URI;

/**
 * <p>Term constants for the WGS ontology</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public class WGS extends Vocabulary {
    private static WGS VOCAB = new WGS("http://www.w3.org/2003/01/geo/wgs84_pos#");

    private WGS(String theURI) {
        super(theURI);
    }

    public static WGS ontology() {
        return VOCAB;
    }

    public final URI lat = term("lat");
    public final URI _long = term("long");
    public final URI alt = term("alt");
    public final URI lat_long = term("lat_long");
    public final URI Point = term("Point");
    public final URI SpatialThing = term("SpatialThing");
}
