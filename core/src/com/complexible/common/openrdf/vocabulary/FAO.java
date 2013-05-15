package com.complexible.common.openrdf.vocabulary;

import org.openrdf.model.URI;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @version 0
 * @since 0
 */
public class FAO extends Vocabulary {

	private static final FAO INSTANCE = new FAO();

	private FAO() {
		super("http://www.fao.org/countryprofiles/geoinfo/geopolitical/resource/");
	}

	public static FAO ontology() {
		return INSTANCE;
	}

	public final URI isoCode2 = term("codeISO2");
	public final URI isoCode3 = term("codeISO3");
}
