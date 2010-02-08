package com.clarkparsia.openrdf.vocabulary;

import org.openrdf.model.URI;

/**
 * <p>Term constants for the VCard ontology</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public class VCard extends Vocabulary {
    private static final VCard VOCAB = new VCard("http://www.w3.org/2006/vcard/ns#");

    private VCard(String theURI) {
        super(theURI);
    }

    public static VCard ontology() {
        return VOCAB;
    }

    public final URI Address = term("Address");

    public final URI GEO = term("GEO");
    public final URI ADR = term("ADR");

	public final URI street_address = term("street-address");
	public final URI locality = term("locality");
	public final URI region = term("region");
	public final URI postal_code = term("postal-code");

	@Deprecated
    public final URI Street = term("Street");

	@Deprecated
    public final URI Locality = term("Locality");

	@Deprecated
    public final URI Region = term("Region");
}

