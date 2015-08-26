/*
 * Copyright (c) 2009-2011 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.complexible.common.openrdf;

import com.complexible.common.openrdf.model.Models2;
import com.complexible.common.openrdf.repository.Repositories;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.impl.SimpleStatement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.MalformedQueryException;

import java.util.Random;

/**
 * <p></p>
 *
 * @author  Michael Grove
 * @since   0.4
 * @version 0.4
 */
public final class TestUtils {
	private static final Random RANDOM = new Random();

	private static final SPARQLParser parser = new SPARQLParser();

	private TestUtils() {
		throw new AssertionError();
	}

	public static ParsedQuery parse(final String theQuery) throws MalformedQueryException {
		return parser.parseQuery(theQuery, "http://openrdf.complexible.com");
	}

	public static Model createRandomModel() {
		return createRandomModel(RANDOM.nextInt(500));
	}

	public static Model createRandomModel(final int theSize) {
		Model aGraph = Models2.newModel();

		for (int i = 0; i < theSize; i++) {
			aGraph.add(createRandomStatement());
		}

		return aGraph;
	}

	public static Repository createRandomRepository() throws RepositoryException {
		Repository aRepo = TestRepositories.createInMemoryRepo();

		Repositories.add(aRepo, createRandomModel());

		return aRepo;
	}

	public static Statement createRandomStatement() {
		Resource aSubj = SimpleValueFactory.getInstance().createIRI("urn:" + RANDOM.nextLong());
		IRI aPred = SimpleValueFactory.getInstance().createIRI("urn:" + RANDOM.nextLong());
		Value aObj = RANDOM.nextBoolean()
						? SimpleValueFactory.getInstance().createIRI("urn:" + RANDOM.nextLong())
						: SimpleValueFactory.getInstance().createLiteral(""+ RANDOM.nextLong());

		return new SimpleStatement(aSubj, aPred, aObj);
	}
}
