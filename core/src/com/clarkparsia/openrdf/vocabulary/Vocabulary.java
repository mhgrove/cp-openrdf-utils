package com.clarkparsia.openrdf.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * <p>Base class for creating a term factory for an ontology or schema.</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public abstract class Vocabulary {
    protected static final ValueFactory FACTORY = new ValueFactoryImpl();

    private String mURI;

    public Vocabulary(String theURI) {
        mURI = theURI;
    }

    public URI term(String theName) {
        return FACTORY.createURI(mURI + theName);
    }

    public java.net.URI uri() {
        return java.net.URI.create(mURI);
    }
}