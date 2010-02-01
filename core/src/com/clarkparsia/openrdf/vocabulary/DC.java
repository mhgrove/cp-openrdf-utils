package com.clarkparsia.openrdf.vocabulary;

import org.openrdf.model.URI;

/**
 * <p>Term constants for the DC ontology</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public class DC extends Vocabulary {
	private static final DC VOCAB = new DC();
	
    private DC() {
        super("http://purl.org/dc/elements/1.1/");
    }
	
    public static DC ontology() {
        return VOCAB;
    }
	
    public final URI title = term("title");
    public final URI creator = term("creator");
    public final URI subject = term("subject");
    public final URI description = term("description");
    public final URI contributor = term("contributor");
    public final URI date = term("date");
    public final URI type = term("type");
    public final URI format = term ("format");
    public final URI identifier = term("identifier");
    public final URI source = term("source");
    public final URI language = term("language");
    public final URI relation = term("relation");
    public final URI coverage = term("coverage");
    public final URI rights = term("rights");
	public final URI publisher = term("publisher");
}