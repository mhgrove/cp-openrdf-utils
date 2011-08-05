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
import org.openrdf.query.algebra.Var;

import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;

import com.clarkparsia.openrdf.query.BaseTupleExprRenderer;
import com.clarkparsia.openrdf.query.SesameQueryUtils;

import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Extends the BaseTupleExprRenderer to provide support for rendering tuple expressions as SPARQL queries.</p>
 *
 * @author Michael Grove
 * @since 0.2
 * @version 0.4
 */
public final class SparqlTupleExprRenderer extends BaseTupleExprRenderer {

	private StringBuffer mJoinBuffer = new StringBuffer();
	private Map<TupleExpr,Var> mContexts = new HashMap<TupleExpr, Var>();
	private int mIndent = 2;

	/**
	 * @inheritDoc
	 */
	@Override
	public void reset() {
		super.reset();

		mJoinBuffer = new StringBuffer();
		mContexts.clear();
	}

	/**
	 * @inheritDoc
	 */
	public String render(final TupleExpr theExpr) throws Exception {
		mContexts = ContextCollector.collectContexts(theExpr);
		
		theExpr.visit(this);

		return mJoinBuffer.toString();
	}

	private String indent() {
		return Strings.repeat(" ", mIndent);
	}

	/**
	 * @inheritDoc
	 */
	protected String renderValueExpr(final ValueExpr theExpr) throws Exception {
		return new SparqlValueExprRenderer().render(theExpr);
	}

	private void ctxOpen(TupleExpr theExpr) {
		Var aContext = mContexts.get(theExpr);

		if (aContext != null) {
			mJoinBuffer.append(indent()).append("GRAPH ");
			if (aContext.hasValue()) {
				mJoinBuffer.append(SesameQueryUtils.getSPARQLQueryString(aContext.getValue()));
			}
			else {
				mJoinBuffer.append("?").append(aContext.getName());
			}
			mJoinBuffer.append(" {\n");
			mIndent += 2;
		}
	}

	private void ctxClose(TupleExpr theExpr) {
		Var aContext = mContexts.get(theExpr);

		if (aContext != null) {
			mJoinBuffer.append("}");
			mIndent -= 2;
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Join theJoin) throws Exception {
		ctxOpen(theJoin);

		theJoin.getLeftArg().visit(this);

		theJoin.getRightArg().visit(this);

		ctxClose(theJoin);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(LeftJoin theJoin) throws Exception {
		ctxOpen(theJoin);
		
		theJoin.getLeftArg().visit(this);

		mJoinBuffer.append(indent()).append("OPTIONAL {\n");

		mIndent+=2;
		theJoin.getRightArg().visit(this);

		if (theJoin.getCondition() != null) {
			mJoinBuffer.append(indent()).append("filter").append(renderValueExpr(theJoin.getCondition())).append("\n");
		}

		mIndent-=2;

		mJoinBuffer.append(indent()).append("}.\n");
		
		ctxClose(theJoin);
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

//		aRenderer.mProjection = new ArrayList<ProjectionElemList>(mProjection);
//		aRenderer.mDistinct = mDistinct;
//		aRenderer.mReduced = mReduced;
//		aRenderer.mExtensions = new HashMap<String, ValueExpr>(mExtensions);
//		aRenderer.mOrdering = new ArrayList<OrderElem>(mOrdering);
//		aRenderer.mLimit = mLimit;
//		aRenderer.mOffset = mOffset;

		aRenderer.mIndent = mIndent;
		aRenderer.mContexts = new HashMap<TupleExpr, Var>(mContexts);

		return aRenderer.render(theExpr);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Union theOp) throws Exception {
		ctxOpen(theOp);

		String aLeft = renderTupleExpr(theOp.getLeftArg());
		if (aLeft.endsWith("\n")) {
			aLeft = aLeft.substring(0, aLeft.length()-1);
		}

		String aRight = renderTupleExpr(theOp.getRightArg());
		if (aRight.endsWith("\n")) {
			aRight = aRight.substring(0, aRight.length()-1);
		}

		mJoinBuffer.append(indent()).append("{\n").append(aLeft).append("\n").append(indent()).append("}\n").append(indent()).append("union\n").append(indent()).append("{\n").append(aRight).append("\n").append(indent()).append("}.\n");

		ctxClose(theOp);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Difference theOp) throws Exception {
		String aLeft = renderTupleExpr(theOp.getLeftArg());
		String aRight = renderTupleExpr(theOp.getRightArg());

		mJoinBuffer.append("\n{").append(aLeft).append("}").append("\nminus\n").append("{").append(aRight).append("}.\n");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Intersection theOp) throws Exception {
		String aLeft = renderTupleExpr(theOp.getLeftArg());
		String aRight = renderTupleExpr(theOp.getRightArg());

		mJoinBuffer.append("\n").append(aLeft).append("}").append("\nintersection\n").append("{").append(aRight).append("}.\n");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final Filter theFilter) throws Exception {
		ctxOpen(theFilter);

		if (theFilter.getArg() != null) {
			theFilter.getArg().visit(this);
		}

		mJoinBuffer.append(indent()).append("filter ").append(renderValueExpr(theFilter.getCondition())).append(".\n");

		ctxClose(theFilter);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(StatementPattern thePattern) throws Exception {
		ctxOpen(thePattern);

		mJoinBuffer.append(indent()).append(renderPattern(thePattern));

		ctxClose(thePattern);
	}


	String renderPattern(StatementPattern thePattern) throws Exception {
		return renderValueExpr(thePattern.getSubjectVar()) + " " +
			   renderValueExpr(thePattern.getPredicateVar()) + " " +
			   "" + renderValueExpr(thePattern.getObjectVar()) + ".\n";

	}
}
