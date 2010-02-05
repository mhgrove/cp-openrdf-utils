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

package com.clarkparsia.openrdf.query.sparql;

import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.OrderElem;
import com.clarkparsia.openrdf.query.BaseTupleExprRenderer;

/**
 * <p></p>
 *
 * @author Michael Grove
 * @since 0.2
 */
class SparqlTupleExprRenderer extends BaseTupleExprRenderer {

	private StringBuffer mJoinBuffer = new StringBuffer();

	/**
	 * @inheritDoc
	 */
	@Override
	public void reset() {
		super.reset();

		mJoinBuffer = new StringBuffer();
	}

	/**
	 * @inheritDoc
	 */
	public String render(final TupleExpr theExpr) throws Exception {
		reset();

		theExpr.visit(this);

		if (mJoinBuffer.length() > 0 && mProjection.isEmpty()) {
			// return the rendered joins if that's all that's been rendered
			return mJoinBuffer.toString();
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
				aQuery.append(" {\n");
			}

			for (ProjectionElemList aList : mProjection) {
				if (isSPOElemList(aList)) {
					if (!aFirst) {
						aQuery.append("\n");
					}
					else {
						aFirst = false;
					}

					aQuery.append(renderPattern(toStatementPattern(aList)));
				}
				else {
					for (ProjectionElem aElem : aList.getElements()) {
						if (!aFirst) {
							aQuery.append(" ");
						}
						else {
							aFirst = false;
						}

						aQuery.append(mExtensions.containsKey(aElem.getSourceName()) ? renderValueExpr(mExtensions.get(aElem.getSourceName())) : "?"+aElem.getSourceName());

						if (!aElem.getSourceName().equals(aElem.getTargetName()) || (mExtensions.containsKey(aElem.getTargetName()) && !mExtensions.containsKey(aElem.getSourceName()))) {
							aQuery.append(" as ").append(mExtensions.containsKey(aElem.getTargetName()) ? renderValueExpr(mExtensions.get(aElem.getTargetName())) : aElem.getTargetName());
						}
					}
				}
			}

			if (!isSelect()) {
				aQuery.append("}");
			}

			aQuery.append("\n");
		}

		if (mJoinBuffer.length() > 0) {
			if (mJoinBuffer.lastIndexOf(",") != -1) {
				mJoinBuffer.setCharAt(mJoinBuffer.lastIndexOf(","), ' ');
			}
			
			aQuery.append("where {\n");
			aQuery.append(mJoinBuffer);
			aQuery.append("}");
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
	 * @inheritDoc
	 */
	protected String renderValueExpr(final ValueExpr theExpr) throws Exception {
		return new SparqlValueExprRenderer().render(theExpr);
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

		mJoinBuffer.append(" OPTIONAL {");

		theJoin.getRightArg().visit(this);

		if (theJoin.getCondition() != null) {
			mJoinBuffer.append(" filter").append(renderValueExpr(theJoin.getCondition()));
		}

		mJoinBuffer.append("}.");
	}

	/**
	 * Renders the tuple expression as a query string.  It creates a new SparqlTupleExprRenderer rather than reusing
	 * this one.
	 * @param theExpr the expr to render
	 * @return the rendered expression
	 * @throws Exception if there is an error while rendering
	 */
	private String renderTupleExpr(TupleExpr theExpr) throws Exception {
		SparqlTupleExprRenderer aRenderer = new SparqlTupleExprRenderer();
		return aRenderer.render(theExpr);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Union theOp) throws Exception {
		String aLeft = renderTupleExpr(theOp.getLeftArg());
		String aRight = renderTupleExpr(theOp.getRightArg());

		mJoinBuffer.append(" {").append(aLeft).append("}").append("\nunion\n").append("{").append(aRight).append("}.\n");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Difference theOp) throws Exception {
		String aLeft = renderTupleExpr(theOp.getLeftArg());
		String aRight = renderTupleExpr(theOp.getRightArg());

		mJoinBuffer.append(" {").append(aLeft).append("}").append("\nminus\n").append("{").append(aRight).append("}.\n");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Intersection theOp) throws Exception {
		String aLeft = renderTupleExpr(theOp.getLeftArg());
		String aRight = renderTupleExpr(theOp.getRightArg());

		mJoinBuffer.append(" {").append(aLeft).append("}").append("\nintersection\n").append("{").append(aRight).append("}.\n");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final Filter theFilter) throws Exception {
		mJoinBuffer.append("{");

		if (theFilter.getArg() != null) {
			theFilter.getArg().visit(this);
		}

		mJoinBuffer.append(" filter ").append(renderValueExpr(theFilter.getCondition())).append(".");

		mJoinBuffer.append("}. ");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(StatementPattern thePattern) throws Exception {
		mJoinBuffer.append(renderPattern(thePattern)).append(" ");
	}


	private String renderPattern(StatementPattern thePattern) throws Exception {
		return "" + renderValueExpr(thePattern.getSubjectVar()) + " " +
			   renderValueExpr(thePattern.getPredicateVar()) + " " +
			   "" + renderValueExpr(thePattern.getObjectVar()) + ". ";

	}
}
