package com.clarkparsia.openrdf.query.sparql;

import com.clarkparsia.openrdf.TestUtils;
import com.clarkparsia.openrdf.query.SesameQueryUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.algebra.Slice;

/**
 * <p></p>
 *
 * @author	Michael Grove
 * @since	0.7
 * @version	0.7
 */
public class TestQueryUtils {
	@Test
	public void testBindingSetUtils() {
		MapBindingSet aBindingSet = new MapBindingSet();

		final Literal aLiteral = ValueFactoryImpl.getInstance().createLiteral("literal");
		final URI aURI = ValueFactoryImpl.getInstance().createURI("urn:s");
		final BNode aBNode = ValueFactoryImpl.getInstance().createBNode();

		aBindingSet.addBinding("lit", aLiteral);
		aBindingSet.addBinding("bnode", aBNode);
		aBindingSet.addBinding("uri", aURI);

		assertEquals(SesameQueryUtils.getLiteral(aBindingSet, "lit"), aLiteral);
		assertEquals(SesameQueryUtils.getBNode(aBindingSet, "bnode"), aBNode);
		assertEquals(SesameQueryUtils.getURI(aBindingSet, "uri"), aURI);

		assertEquals(SesameQueryUtils.getResource(aBindingSet, "bnode"), aBNode);
		assertEquals(SesameQueryUtils.getResource(aBindingSet, "uri"), aURI);

		assertTrue(SesameQueryUtils.getLiteral(aBindingSet, "uri") == null);
		assertTrue(SesameQueryUtils.getLiteral(aBindingSet, "bnode") == null);
		assertTrue(SesameQueryUtils.getResource(aBindingSet, "lit") == null);
		assertTrue(SesameQueryUtils.getURI(aBindingSet, "lit") == null);
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

	private static class GetSlice extends QueryModelVisitorBase<Exception> {
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

	private static class ContainsVisitor extends QueryModelVisitorBase<Exception> {
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
