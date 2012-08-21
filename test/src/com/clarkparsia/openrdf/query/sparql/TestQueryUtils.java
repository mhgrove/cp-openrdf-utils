package com.clarkparsia.openrdf.query.sparql;

import com.clarkparsia.openrdf.query.SesameQueryUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.algebra.Slice;
import static com.clarkparsia.openrdf.query.SesameQueryUtils.parse;

/**
 * <p></p>
 *
 * @author	Michael Grove
 * @since	0.7
 * @version	0.7
 */
public class TestQueryUtils {

	@Test
	public void testSetLimit() throws Exception {
		String aQuery = "select * where {?s ?p ?o }";

		ParsedQuery aParsedQuery = parse(aQuery);

		assertTrue(!contains(aParsedQuery, Slice.class));

		SesameQueryUtils.setLimit(aParsedQuery, 10);

		assertEquals("Limit should be 10", 10L, getLimit(aParsedQuery));
	}

	@Test
	public void testSetOffset() throws Exception {
		String aQuery = "select * where {?s ?p ?o }";

		ParsedQuery aParsedQuery = parse(aQuery);

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
