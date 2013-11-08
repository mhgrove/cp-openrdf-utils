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

import com.complexible.common.openrdf.model.ContextAwareStatement;
import com.complexible.common.openrdf.model.ContextAwareValueFactory;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;

import org.openrdf.model.impl.ValueFactoryImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <p></p>
 *
 * @author	Michael Grove
 * @since	0.8
 * @version	0.8
 */
public class TestContextAware {
	@Test
	public void testContextAwareStmt() {
		final URI s = ValueFactoryImpl.getInstance().createURI("urn:s");
		final URI p = ValueFactoryImpl.getInstance().createURI("urn:p");
		final URI o = ValueFactoryImpl.getInstance().createURI("urn:o");
		final URI c = ValueFactoryImpl.getInstance().createURI("urn:c");
		final URI c2 = ValueFactoryImpl.getInstance().createURI("urn:c2");

		Statement aStmt = new StatementImpl(s, p, o);
		ContextAwareStatement aCxtStmt = new ContextAwareStatement(s, p, o, c);

		assertEquals(aStmt, aCxtStmt);

		assertFalse(aCxtStmt.equals(aStmt));

		assertEquals(aStmt.hashCode(), aCxtStmt.hashCode());

		assertFalse(aCxtStmt.equals(null));
		assertTrue(aCxtStmt.equals(aCxtStmt));

		assertTrue(aCxtStmt.equals(new ContextAwareStatement(s, p, o, c)));

		assertFalse(aCxtStmt.equals(new ContextAwareStatement(s, p, o, c2)));

		assertFalse(aCxtStmt.equals(new ContextAwareStatement(s, p, o, null)));

		assertEquals(new ContextAwareStatement(s, p, o, null), new ContextAwareStatement(s, p, o, null));
	}

	@Test
	public void testCxtStmtNoNulls() {
		final URI s = ValueFactoryImpl.getInstance().createURI("urn:s");
		final URI p = ValueFactoryImpl.getInstance().createURI("urn:p");
		final URI o = ValueFactoryImpl.getInstance().createURI("urn:o");
		final URI c = ValueFactoryImpl.getInstance().createURI("urn:c");

		// this is ok, no context
		new ContextAwareStatement(s, p, o, null);

		// but spo cannot be null

		try {
			new ContextAwareStatement(null, p, o, c);
			fail("No nulls in ContextAwareStatement");
		}
		catch (NullPointerException e) {
			// expected
		}
		try {
			new ContextAwareStatement(s, null, o, c);
			fail("No nulls in ContextAwareStatement");
		}
		catch (NullPointerException e) {
			// expected
		}

		try {
			new ContextAwareStatement(s, p, null, c);
			fail("No nulls in ContextAwareStatement");
		}
		catch (NullPointerException e) {
			// expected
		}
	}

	@Test
	public void testContextAwareVF() {
		final URI s = ValueFactoryImpl.getInstance().createURI("urn:s");
		final URI p = ValueFactoryImpl.getInstance().createURI("urn:p");
		final URI o = ValueFactoryImpl.getInstance().createURI("urn:o");
		final URI c = ValueFactoryImpl.getInstance().createURI("urn:c");

		ValueFactory vf = new ContextAwareValueFactory();

		assertTrue(vf.createStatement(s, p, o) instanceof ContextAwareStatement);
		assertTrue(vf.createStatement(s, p, o, c) instanceof ContextAwareStatement);
	}
}
