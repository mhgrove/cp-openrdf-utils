package com.complexible.common.openrdf.vocabulary;

import org.openrdf.model.URI;

/**
 * <p></p>
 *
 * @author  Michael Grove
 * @since   1.1
 * @version 1.1
 */
public final class SSD extends Vocabulary {
    public SSD() {
        super("http://www.w3.org/ns/sparql-service-description#");
    }

    private static final SSD INSTANCE = new SSD();
    public static SSD ontology() {
        return INSTANCE;
    }

    public final URI Service = term("Service");

    public final URI Language = term("Language");
    public final URI SPARQL11Query = term("SPARQL11Query");
    public final URI SPARQL11Update = term("SPARQL11Update");
    public final URI SPARQL10Query = term("SPARQL10Query");

    public final URI Feature = term("Feature");
    public final URI DereferencesURIs = term("DereferencesURIs");
    public final URI UnionDefaultGraph = term("UnionDefaultGraph");
    public final URI RequiresDataset = term("RequiresDataset");
    public final URI EmptyGraphs = term("EmptyGraphs");
    public final URI BasicFederatedQuery = term("BasicFederatedQuery");

    public final URI EntailmentProfile = term("EntailmentProfile");
    public final URI EntailmentRegime = term("EntailmentRegime");
    public final URI Dataset = term("Dataset");
    public final URI Graph = term("Graph");
    public final URI NamedGraph = term("NamedGraph");

    public final URI Function = term("Function");
    public final URI Aggregate = term("Aggregate");

    public final URI endpoint = term("endpoint");
    public final URI feature = term("feature");
    public final URI resultFormat = term("resultFormat");
    public final URI defaultEntailmentRegime = term("defaultEntailmentRegime");
    public final URI entailmentRegime = term("entailmentRegime");
    public final URI defaultSupportedEntailmentProfile = term("defaultSupportedEntailmentProfile");
    public final URI supportedEntailmentProfile = term("supportedEntailmentProfile");
    public final URI extensionFunction = term("extensionFunction");
    public final URI extensionAggregate = term("extensionAggregate");
    public final URI languageExtension = term("languageExtension");
    public final URI supportedLanguage = term("supportedLanguage");
    public final URI propertyFeature = term("propertyFeature");
    public final URI defaultDataset = term("defaultDataset");
    public final URI availableGraphs = term("availableGraphs");
    public final URI inputFormat = term("inputFormat");
    public final URI defaultGraph = term("defaultGraph");
    public final URI namedGraph = term("namedGraph");
    public final URI name = term("name");
    public final URI graph = term("graph");

}
