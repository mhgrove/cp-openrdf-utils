package com.complexible.common.openrdf.vocabulary;

import org.openrdf.model.URI;

/**
 * <p></p>
 *
 * @author  Michael Grove
 * @since   0
 * @version 0
 */
public final class VoiD extends Vocabulary {
    public VoiD() {
        super("http://rdfs.org/ns/void#");
    }

    private static final VoiD INSTANCE = new VoiD();
    public static VoiD ontology() {
        return INSTANCE;
    }

    public final URI triples = term("triples");
}
