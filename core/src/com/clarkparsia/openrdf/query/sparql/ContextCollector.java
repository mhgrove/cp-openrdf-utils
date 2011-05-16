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

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import com.clarkparsia.utils.BasicUtils;

/**
 * <p>Visitor implementation for the sesame query algebra which walks the tree and figures out the context for nodes in the algebra.  The context for a node
 * is set on the highest node in the tree.  That is, everything below it shares the same context.</p>
 *
 * @author Blazej Bulka
 * @since 0.3
 * @version 0.3
 */
public class ContextCollector extends QueryModelVisitorBase<Exception> {
	
	/**
	 * Maps TupleExpr to contexts. This map contains only top-level expression elements
	 * that share the given context (i.e., all elements below share the same context) --
	 * this is because of where contexts are being introduced into a SPARQL query -- all
	 * elements sharing the same contexts are grouped together with a "GRAPH <ctx> { ... }"
	 * clause.
	 */
	private Map<TupleExpr,Var> mContexts = new HashMap<TupleExpr,Var>();
	
	private ContextCollector() {		
	}
	
	static Map<TupleExpr,Var> collectContexts(TupleExpr theTupleExpr) throws Exception {
		ContextCollector aContextVisitor = new ContextCollector();
		
		theTupleExpr.visit(aContextVisitor);
		
		return aContextVisitor.mContexts;
	}
	
	public void meet(Join theJoin) throws Exception {
		binaryOpMeet(theJoin, theJoin.getLeftArg(), theJoin.getRightArg());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(LeftJoin theJoin) throws Exception {
		binaryOpMeet(theJoin, theJoin.getLeftArg(), theJoin.getRightArg());
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Union theOp) throws Exception {
		binaryOpMeet(theOp, theOp.getLeftArg(), theOp.getRightArg());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Difference theOp) throws Exception {
		binaryOpMeet(theOp, theOp.getLeftArg(), theOp.getRightArg());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(Intersection theOp) throws Exception {
		binaryOpMeet(theOp, theOp.getLeftArg(), theOp.getRightArg());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(final Filter theFilter) throws Exception {			
		theFilter.getArg().visit(this);
		
		if (mContexts.containsKey(theFilter.getArg())) {
			Var aCtx = mContexts.get(theFilter.getArg());
			mContexts.remove(theFilter.getArg());
			mContexts.put(theFilter, aCtx);
		}
	}
	
	private void binaryOpMeet(TupleExpr theCurrentExpr, TupleExpr theLeftExpr, TupleExpr theRightExpr) throws Exception {
		theLeftExpr.visit(this);
		
		Var aLeftCtx = mContexts.get(theLeftExpr);
		
		theRightExpr.visit(this);
		
		Var aRightCtx = mContexts.get(theRightExpr);
		
		sameCtxCheck(theCurrentExpr, theLeftExpr, aLeftCtx, theRightExpr, aRightCtx);		
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void meet(StatementPattern thePattern) throws Exception {
		Var aCtxVar = thePattern.getContextVar();
		
		if (aCtxVar != null) {
			mContexts.put(thePattern, aCtxVar);
		}
	}
	
	private void sameCtxCheck(TupleExpr theCurrentExpr, TupleExpr theLeftExpr, Var theLeftCtx, TupleExpr theRightExpr, Var theRightCtx) {
		if ((theLeftCtx == null) && (theRightCtx != null)) {
			mContexts.remove(theRightExpr);
			mContexts.put(theCurrentExpr, theRightCtx);
		}
		else if ((theLeftCtx != null) && (theRightCtx == null)) {
			mContexts.remove(theLeftExpr);
			mContexts.put(theCurrentExpr, theLeftCtx);
		}
		else if ((theLeftCtx != null) && (theRightCtx != null) && isSameCtx(theLeftCtx, theRightCtx)) {
			mContexts.remove(theLeftExpr);			
			mContexts.remove(theRightExpr);
			mContexts.put(theCurrentExpr, theLeftCtx);
		}				
	}
	
	private boolean isSameCtx(Var v1, Var v2) {
		if ((v1 != null && v1.getValue() != null) && (v2 != null && v2.getValue() != null)) {
			return v1.getValue().equals(v2.getValue());
		}
		else if ((v1 != null && v1.getName() != null) && (v2 != null && v2.getName() != null)) {
			return v1.getName().equals(v2.getName());
		}
		
		return false;
	}
}
