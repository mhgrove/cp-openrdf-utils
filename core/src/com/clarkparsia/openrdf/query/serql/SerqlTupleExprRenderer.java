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

package com.clarkparsia.openrdf.query.serql;

import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.And;

import static com.clarkparsia.utils.collections.CollectionUtil.each;
import static com.clarkparsia.utils.collections.CollectionUtil.transform;
import com.clarkparsia.openrdf.query.BaseTupleExprRenderer;

/**
 * <p>Renders a Sesame {@link TupleExpr} into SeRQL Syntax</p>
 *
 * @author Michael Grove
 * @since 0.2
 */
class SerqlTupleExprRenderer extends BaseTupleExprRenderer {

	private StringBuffer mBuffer = new StringBuffer();
	private StringBuffer mJoinBuffer = new StringBuffer();

	private ValueExpr mFilter;

	private final SerqlValueExprRenderer mValueExprRenderer = new SerqlValueExprRenderer();

	/**
	 * @inheritDoc
	 */
	@Override
	public void reset() {
		super.reset();

		mFilter = null;

		mBuffer = new StringBuffer();
		mJoinBuffer = new StringBuffer();
	}

	/**
	 * @inheritDoc
	 */
	public String render(TupleExpr theExpr) throws Exception {
		reset();

		theExpr.visit(this);

		if (mBuffer.length() > 0) {
			return mBuffer.toString();
		}

		boolean aFirst = true;

		StringBuffer aQuery = new StringBuffer();

		if (!mProjection.isEmpty()) {
			if (isSelect()) {
				aQuery.append("select ");
			}
			else {
				aQuery.append("construct ");
			}

			if (mDistinct) {
				aQuery.append("distinct ");
			}

			if (mReduced && isSelect()) {
				aQuery.append("reduced ");
			}

			aFirst = true;

			if (!isSelect()) {
				aQuery.append("\n");
			}

			for (ProjectionElemList aList : mProjection) {
				if (isSPOElemList(aList)) {
					if (!aFirst) {
						aQuery.append(",\n");
					}
					else {
						aFirst = false;
					}

					aQuery.append(renderPattern(toStatementPattern(aList)));
				}
				else {
					for (ProjectionElem aElem : aList.getElements()) {
						if (!aFirst) {
							aQuery.append(", ");
						}
						else {
							aFirst = false;
						}

						aQuery.append(mExtensions.containsKey(aElem.getSourceName()) ? mValueExprRenderer.render(mExtensions.get(aElem.getSourceName())) : aElem.getSourceName());

						if (!aElem.getSourceName().equals(aElem.getTargetName()) || (mExtensions.containsKey(aElem.getTargetName()) && !mExtensions.containsKey(aElem.getSourceName()))) {
							aQuery.append(" as ").append(mExtensions.containsKey(aElem.getTargetName()) ? mValueExprRenderer.render(mExtensions.get(aElem.getTargetName())) : aElem.getTargetName());
						}
					}
				}
			}

			aQuery.append("\n");
		}

		if (mJoinBuffer.length() > 0) {
			mJoinBuffer.setCharAt(mJoinBuffer.lastIndexOf(","), ' ');
			aQuery.append("from\n");
			aQuery.append(mJoinBuffer);
		}
		
//		if (!mQueryAtoms.isEmpty()) {
//			aFirst = true;
//			aQuery.append("from \n");
//			for (StatementPattern thePattern : mQueryAtoms) {
//				if (!aFirst) {
//					aQuery.append(",\n");
//				}
//				else {
//					aFirst = false;
//				}
//
//				aQuery.append(renderPattern(thePattern));
//			}
//
//			aQuery.append("\n");
//		}


		if (mFilter != null) {
			aQuery.append("where\n");
			aQuery.append(mValueExprRenderer.render(mFilter));
		}

		if (!mOrdering.isEmpty()) {
			aQuery.append("\norder by ");

			aFirst = true;
			for (OrderElem aOrder : mOrdering) {
				if (!aFirst) {
					aQuery.append(", ");
				}
				else {
					aFirst = false;
				}

				aQuery.append(aOrder.getExpr());
				aQuery.append(" " );
				if (aOrder.isAscending()) {
					aQuery.append("asc");
				}
				else {
					aQuery.append("desc");
				}
			}
		}

		if (mLimit != -1) {
			aQuery.append("\nlimit ").append(mLimit);
		}

		if (mOffset != -1) {
			aQuery.append("\noffset ").append(mOffset);
		}

		return aQuery.toString();
	}


	/**
	 * Renders the tuple expression as a query string.  It creates a new SerqlTupleExprRenderer rather than reusing
	 * this one.
	 * @param theExpr the expr to render
	 * @return the rendered expression
	 * @throws Exception if there is an error while rendering
	 */
	private String renderTupleExpr(TupleExpr theExpr) throws Exception {
		SerqlTupleExprRenderer aRenderer = new SerqlTupleExprRenderer();
		return aRenderer.render(theExpr);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Union theOp) throws Exception {
		String aLeft = renderTupleExpr(theOp.getLeftArg());
		String aRight = renderTupleExpr(theOp.getRightArg());

		mBuffer.append(aLeft).append("\nunion\n").append(aRight);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Difference theOp) throws Exception {
		String aLeft = renderTupleExpr(theOp.getLeftArg());
		String aRight = renderTupleExpr(theOp.getRightArg());

		mBuffer.append(aLeft).append("\nminus\n").append(aRight);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Intersection theOp) throws Exception {
		String aLeft = renderTupleExpr(theOp.getLeftArg());
		String aRight = renderTupleExpr(theOp.getRightArg());

		mBuffer.append(aLeft).append("\nintersection\n").append(aRight);
	}

	/**
	 * Render a StatementPattern
	 * @param thePattern the pattern to render
	 * @return the rendered pattern
	 * @throws Exception if there is an error while rendering
	 */
	private String renderPattern(StatementPattern thePattern) throws Exception {
		return "{" + renderValueExpr(thePattern.getSubjectVar()) + "} " +
			   renderValueExpr(thePattern.getPredicateVar()) + " " +
			   "{" + renderValueExpr(thePattern.getObjectVar()) + "} ";

	}

	/**
	 * @inheritDoc
	 */
	protected String renderValueExpr(final ValueExpr theExpr) throws Exception {
		return mValueExprRenderer.render(theExpr);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Join theJoin) throws Exception {
        theJoin.getLeftArg().visit(this);

        theJoin.getRightArg().visit(this);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(LeftJoin theJoin) throws Exception {

		theJoin.getLeftArg().visit(this);

		mJoinBuffer.append(" [");

		theJoin.getRightArg().visit(this);

		if (theJoin.getCondition() != null) {
			mJoinBuffer.append(renderValueExpr(theJoin.getCondition()));
		}

		mJoinBuffer.setCharAt(mJoinBuffer.lastIndexOf(","),' ');

		mJoinBuffer.append("], ");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(StatementPattern thePattern) throws Exception {
		mJoinBuffer.append(renderPattern(thePattern)).append(",\n");
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final Filter theFilter) throws Exception {
		if (mFilter == null) {
			mFilter = theFilter.getCondition();
		}
		else {
			mFilter = new And(mFilter, theFilter.getCondition());
		}

		theFilter.visitChildren(this);
	}
}
