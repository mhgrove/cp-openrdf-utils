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
 * <p>Term constants for the DC ontology</p>
 *
 * @author Michael Grove
 * @since 0.1
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