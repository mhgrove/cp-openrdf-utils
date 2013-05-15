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
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * <p>Term constants for the DC music ontology</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public class MusicOntology extends Vocabulary {
    public static final String ONT_URI = "http://purl.org/ontology/mo/";

    private static MusicOntology INSTANCE = null;

    private MusicOntology() {
        super(ONT_URI);
    }

    public static MusicOntology ontology() {
        if (INSTANCE == null) {
            INSTANCE = new MusicOntology();
        }

        return INSTANCE;
    }

    // properties
    public final URI track = term("track");
    public final URI release_type = term("release_type");
    public final URI release_status = term("release_status");
    public final URI track_number = term("track_number");
    public final URI length = term("length");
    public final URI made = term("made");
    public final URI musicbrainz = term("musicbrainz");
    public final URI olga = term("olga");
    public final URI genre = term("genre");
    public final URI sample_rate = term("sample_rate");
    public final URI bitsPerSample = term("bitsPerSample");

    // cp properties
    public final URI rating = term("rating");
    public final URI albumRating = term("albumRating");
    public final URI year = term("year");
    public final URI location = term("location");

    // classes
    public final URI Genre = term("Genre");
    public final URI Record = term("Record");
    public final URI Track = term("Track");
    public final URI MusicArtist = term("MusicArtist");
    public final URI MusicGroup = term("MusicGroup");

    // individuals
    public final URI Metal = FACTORY.createURI(Genre.stringValue() + "/Metal");
    public final URI Rock = FACTORY.createURI(Genre.stringValue() + "/Rock");
    public final URI Alternative = FACTORY.createURI(Genre.stringValue() + "/Alternative");
    public final URI Pop = FACTORY.createURI(Genre.stringValue() + "/Pop");
    public final URI Punk = FACTORY.createURI(Genre.stringValue() + "/Punk");
    public final URI Funk = FACTORY.createURI(Genre.stringValue() + "/Funk");
    public final URI Soundtrack = FACTORY.createURI(Genre.stringValue() + "/Soundtrack");
    public final URI Blues = FACTORY.createURI(Genre.stringValue() + "/Blues");
    public final URI Jazz = FACTORY.createURI(Genre.stringValue() + "/Jazz");
    public final URI Vocal = FACTORY.createURI(Genre.stringValue() + "/Vocal");
	public final URI Country = FACTORY.createURI(Genre.stringValue() + "/Country");

    public final URI album = term("album");
    public final URI official = term("official");
}
