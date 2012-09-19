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

package com.clarkparsia.openrdf;

import com.clarkparsia.openrdf.query.sparql.SPARQLQueryRenderer;
import org.openrdf.repository.RepositoryException;
import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.MalformedQueryException;

import java.util.Random;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @version 0.4
 * @since 0.4
 */
public final class TestUtils {
	private static final Random RANDOM = new Random();

	private static final SPARQLParser parser = new SPARQLParser();

	private TestUtils() {
	}

	public static String render(final ParsedQuery theQuery) throws Exception {
		return new SPARQLQueryRenderer().render(theQuery);
	}

	public static ParsedQuery parse(final String theQuery) throws MalformedQueryException {
		return parser.parseQuery(theQuery, "http://openrdf.clarkparsia.com");
	}

	public static ExtGraphImpl createRandomGraph() {
		return createRandomGraph(RANDOM.nextInt(500));
	}

	public static ExtGraphImpl createRandomGraph(final int theSize) {
		ExtGraphImpl aGraph = new ExtGraphImpl();

		for (int i = 0; i < theSize; i++) {
			aGraph.add(createRandomStatement());
		}

		return aGraph;
	}

	public static ExtRepository createRandomRepository() throws RepositoryException {
		ExtRepository aRepo = OpenRdfUtil.createInMemoryRepo();

		aRepo.add(createRandomGraph());

		return aRepo;
	}

	public static Statement createRandomStatement() {
		Resource aSubj = ValueFactoryImpl.getInstance().createURI("urn:" + RANDOM.nextLong());
		URI aPred = ValueFactoryImpl.getInstance().createURI("urn:" + RANDOM.nextLong());
		Value aObj = RANDOM.nextBoolean()
						? ValueFactoryImpl.getInstance().createURI("urn:" + RANDOM.nextLong())
						: ValueFactoryImpl.getInstance().createLiteral(""+ RANDOM.nextLong());

		return new StatementImpl(aSubj, aPred, aObj);
	}
}
