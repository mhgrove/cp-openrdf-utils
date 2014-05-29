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

import com.complexible.common.openrdf.model.ConstrainedGraph;
import com.google.common.base.Predicate;
import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

import static org.junit.Assert.fail;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @version 0
 * @since 0
 */
public class ConstrainedGraphTests {
	@Test
	public void testCannotAddViolatingConstraint() {
		Predicate<Statement> noBNodes = new Predicate<Statement>() {
			@Override
			public boolean apply(final Statement theStatement) {
				if (theStatement.getSubject() instanceof BNode ||
					theStatement.getObject() instanceof BNode) {
					throw new ConstrainedGraph.StatementViolatedConstraintException("Cannot add statements with bnodes to this graph");
				}

				return true;
			}
		};

		Graph aGraph = ConstrainedGraph.of(noBNodes);

		// random graphs dont include bnodes so this should be ok
		aGraph.addAll(TestUtils.createRandomGraph(5));

		try {
			aGraph.add(ValueFactoryImpl.getInstance().createBNode(), RDF.TYPE, ValueFactoryImpl.getInstance().createURI("urn:o"));
			fail("should not allow an addition which violates a constraint");
		}
		catch (RuntimeException e) {
			// expected
		}

		try {
			aGraph.add(ValueFactoryImpl.getInstance().createURI("urn:s"), RDF.TYPE, ValueFactoryImpl.getInstance().createBNode());
			fail("should not allow an addition which violates a constraint");
		}
		catch (RuntimeException e) {
			// expected
		}

	}
}
