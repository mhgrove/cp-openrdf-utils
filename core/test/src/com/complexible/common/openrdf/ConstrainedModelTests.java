/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import java.util.function.Predicate;

import com.complexible.common.openrdf.model.ConstrainedModel;
import org.junit.Test;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import static org.junit.Assert.fail;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @version 0
 * @since 0
 */
public class ConstrainedModelTests {
	@Test
	public void testCannotAddViolatingConstraint() {
		Predicate<Statement> noBNodes = theStatement -> {
			if (theStatement.getSubject() instanceof BNode ||
				theStatement.getObject() instanceof BNode) {
				throw new ConstrainedModel.StatementViolatedConstraintException("Cannot add statements with bnodes to this graph");
			}

			return true;
		};

		Model aGraph = ConstrainedModel.of(noBNodes);

		// random graphs dont include bnodes so this should be ok
		aGraph.addAll(TestUtils.createRandomModel(5));

		try {
			aGraph.add(SimpleValueFactory.getInstance().createBNode(), RDF.TYPE, SimpleValueFactory.getInstance().createIRI("urn:o"));
			fail("should not allow an addition which violates a constraint");
		}
		catch (RuntimeException e) {
			// expected
		}

		try {
			aGraph.add(SimpleValueFactory.getInstance().createIRI("urn:s"), RDF.TYPE, SimpleValueFactory.getInstance().createBNode());
			fail("should not allow an addition which violates a constraint");
		}
		catch (RuntimeException e) {
			// expected
		}

	}
}
