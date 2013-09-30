/*
 * Copyright (c) 2009-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.complexible.common.openrdf.vocabulary;

import org.openrdf.model.URI;

/**
 * <p>Constants for the concepts in the FOAF vocabulary</p>
 *
 * @author  Michael Grove
 * @since   0.1
 * @version 1.1
 */
public final class FOAF extends Vocabulary {
	private static FOAF INSTANCE;
	
    private static final java.net.URI FOAF_URI = java.net.URI.create("http://xmlns.com/foaf/0.1/");

	private FOAF() {
		super(FOAF_URI.toString());
	}
	
	public static FOAF ontology() {
		if (INSTANCE == null) {
			INSTANCE = new FOAF();
		}
		
		return INSTANCE;
	}

    public final URI Person = term("Person");
    public final URI Organization = term("Organization");
    public final URI Image = term("Image");
	public final URI Agent = term("Agent");
	public final URI Group = term("Group");
	public final URI Document = term("Document");

    public final URI givenName = term("givenName");
    public final URI familyName = term("familyName");
    public final URI firstName = term("firstName");
    public final URI surname = term("surname");
	public final URI name = term("name");
    public final URI mbox = term("mbox");
    public final URI depicts = term("depicts");
    public final URI depiction = term("depiction");
    public final URI maker = term("maker");
    public final URI phone = term("phone");
    public final URI fax = term("fax");
    public final URI based_near = term("based_near");
	public final URI thumbnail = term("thumbnail");
	public final URI homepage = term("homepage");
	public final URI birthday = term("birthday");
	public final URI knows = term("knows");
	public final URI lastName = term("lastName");
	public final URI title = term("title");
	public final URI openId = term("openId");
	public final URI pastProject = term("pastProject");
	public final URI topic_interest = term("topic_interest");
	public final URI age = term("age");
	public final URI member = term("member");
	public final URI primaryTopic = term("primaryTopic");
	public final URI made = term("made");
	public final URI logo = term("logo");
	public final URI currentProject = term("currentProject");
}
