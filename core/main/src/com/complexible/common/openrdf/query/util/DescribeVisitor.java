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

package com.complexible.common.openrdf.query.util;

import org.openrdf.query.algebra.DescribeOperator;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.UnaryValueOperator;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.model.Value;
import org.openrdf.model.Literal;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.impl.BooleanLiteralImpl;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>TupleExprVisitor implementation that will scan a query model and see if it looks like the model for a describe
 * query.  If so, {@link #isDescribe} will return true after {@link #checkQuery(TupleExpr) checking} the query. </p>
 *
 * <p>The main aim of this class is to provide some functionality to scan a query model to find out if its a describe,
 * and what is being described so you could "compile" the describe query into a query language that does not support
 * describe natively, such as SeRQL.  Once you know what is being described, you could then simulate the support with
 * a construct query.</p>
 *
 * @author  Michael Grove
 * @since   0.2.4
 * @version 0.3
*/
public final class DescribeVisitor extends QueryModelVisitorBase<Exception> {

	/**
	 * Whether or not the query model represents a describe query
	 */
	private boolean mIsDescribe = false;

	public void reset() {
		mIsDescribe = false;
	}

	/**
	 * Check to see if this query is a describe query or not
	 * @param theExpr the query model to check
	 * @return this visitor
	 * @throws Exception if there is an error while checking
	 */
	public boolean checkQuery(TupleExpr theExpr) throws Exception {
		reset();
		theExpr.visit(this);

		return isDescribe();
	}

	/**
	 * Check to see if this query is a describe query or not
	 * @param theQuery the query to check
	 * @return this visitor
	 * @throws Exception if there is an error while checking
	 */
	public boolean checkQuery(ParsedQuery theQuery) throws Exception {
		return checkQuery(theQuery.getTupleExpr());
	}

	/**
	 * Whether or not the query model represents a describe query
	 * @return true if it is a describe query, false otherwise
	 */
	public boolean isDescribe() {
		return mIsDescribe;
	}

	@Override
	public void meet(final DescribeOperator node) throws Exception {
		mIsDescribe = true;
	}

    public static boolean isDescribeName(final String theName) {
        return theName.startsWith("-descr") || theName.startsWith("descr") || theName.startsWith("descr_");
    }
}
