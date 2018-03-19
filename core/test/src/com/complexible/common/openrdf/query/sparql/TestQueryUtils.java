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

package com.complexible.common.openrdf.query.sparql;

import java.util.NoSuchElementException;

import com.complexible.common.openrdf.TestUtils;
import com.complexible.common.openrdf.query.BooleanQueryResult;
import com.complexible.common.openrdf.query.BooleanQueryResultImpl;
import com.complexible.common.openrdf.query.SesameQueryUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryEvaluationException;

import org.eclipse.rdf4j.query.algebra.QueryModelNode;
import org.eclipse.rdf4j.query.algebra.Slice;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

/**
 * <p></p>
 *
 * @author	Michael Grove
 * @since	0.7
 * @version	0.8
 */
public class TestQueryUtils {
	@Test
	public void testBooleanQueryResult() throws QueryEvaluationException {
		BooleanQueryResult aResult = new BooleanQueryResultImpl(true);

		try {
			aResult.remove();
			fail("Remove should not have succeeded");
		}
		catch (UnsupportedOperationException e) {
			// expected
		}

		assertTrue(aResult.hasNext());

		assertTrue(aResult.next());

		assertFalse(aResult.hasNext());

		try {
			aResult.next();
			fail("Should not be able to call next a second time");
		}
		catch (NoSuchElementException e) {
			// expected
		}

		assertFalse(new BooleanQueryResultImpl(false).next());
	}

	@Test
	public void testBindingSetUtils() {
		MapBindingSet aBindingSet = new MapBindingSet();

		final Literal aLiteral = SimpleValueFactory.getInstance().createLiteral("literal");
		final IRI aURI = SimpleValueFactory.getInstance().createIRI("urn:s");
		final BNode aBNode = SimpleValueFactory.getInstance().createBNode();

		aBindingSet.addBinding("lit", aLiteral);
		aBindingSet.addBinding("bnode", aBNode);
		aBindingSet.addBinding("uri", aURI);

		assertEquals(SesameQueryUtils.getLiteral(aBindingSet, "lit"), aLiteral);
		assertEquals(SesameQueryUtils.getBNode(aBindingSet, "bnode"), aBNode);
		assertEquals(SesameQueryUtils.getIRI(aBindingSet, "uri"), aURI);

		assertEquals(SesameQueryUtils.getResource(aBindingSet, "bnode"), aBNode);
		assertEquals(SesameQueryUtils.getResource(aBindingSet, "uri"), aURI);

		assertTrue(SesameQueryUtils.getLiteral(aBindingSet, "uri") == null);
		assertTrue(SesameQueryUtils.getLiteral(aBindingSet, "bnode") == null);
		assertTrue(SesameQueryUtils.getResource(aBindingSet, "lit") == null);
		assertTrue(SesameQueryUtils.getIRI(aBindingSet, "lit") == null);
		assertTrue(SesameQueryUtils.getBNode(aBindingSet, "lit") == null);
	}

	@Test
	public void testSetLimit() throws Exception {
		String aQuery = "select * where {?s ?p ?o }";

		ParsedQuery aParsedQuery = TestUtils.parse(aQuery);

		assertTrue(!contains(aParsedQuery, Slice.class));

		SesameQueryUtils.setLimit(aParsedQuery, 10);

		assertEquals("Limit should be 10", 10L, getLimit(aParsedQuery));
	}

	@Test
	public void testSetOffset() throws Exception {
		String aQuery = "select * where {?s ?p ?o }";

		ParsedQuery aParsedQuery = TestUtils.parse(aQuery);

		assertTrue(!contains(aParsedQuery, Slice.class));

		SesameQueryUtils.setOffset(aParsedQuery, 10);

		assertEquals("Offset should be 10", 10L, getOffset(aParsedQuery));
	}

	private long getOffset(final ParsedQuery theQuery) {
		GetSlice aGetLimit = new GetSlice();

		try {
			theQuery.getTupleExpr().visit(aGetLimit);
		}
		catch (Exception e) {
			return -1;
		}

		return aGetLimit.getOffset();
	}

	private long getLimit(final ParsedQuery theQuery) {
		GetSlice aGetLimit = new GetSlice();

		try {
			theQuery.getTupleExpr().visit(aGetLimit);
		}
		catch (Exception e) {
			return -1;
		}

		return aGetLimit.getLimit();
	}

	private boolean contains(final ParsedQuery theQuery, final Class<? extends QueryModelNode> theType) {

		ContainsVisitor aVisitor = new ContainsVisitor(theType);

		try {
			theQuery.getTupleExpr().visit(aVisitor);
		}
		catch (Exception e) {
			return false;
		}

		return aVisitor.isContains();
	}

	private static class GetSlice extends AbstractQueryModelVisitor<Exception> {
		private long mLimit = -1;
		private long mOffset = -1;

		@Override
		public void meet(final Slice node) throws Exception {
			mLimit = node.getLimit();
			mOffset = node.getOffset();
		}

		public long getLimit() {
			return mLimit;
		}

		public long getOffset() {
			return mOffset;
		}
	}

	private static class ContainsVisitor extends AbstractQueryModelVisitor<Exception> {
		private boolean mContains = false;

		private final Class<? extends QueryModelNode> mClass;

		private ContainsVisitor(final Class<? extends QueryModelNode> theClass) {
			mClass = theClass;
		}

		public boolean isContains() {
			return mContains;
		}

		/**
		 * @inheritDoc
		 */
		@Override
		protected void meetNode(final QueryModelNode theQueryModelNode) throws Exception {
			if (mClass.equals(theQueryModelNode.getClass())) {
				mContains = true;
			}
			else {
				super.meetNode(theQueryModelNode);
			}
		}
	}
}
