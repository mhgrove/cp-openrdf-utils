/*
 * Copyright (c) 2009-2014 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.complexible.common.openrdf.query.impl;

import java.util.Iterator;
import java.util.Map;

import com.complexible.common.openrdf.query.GraphQueryResult;
import com.google.common.collect.ImmutableMap;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.IterationWrapper;
import org.openrdf.model.Statement;
import org.openrdf.query.QueryEvaluationException;

/**
 * {@link AutoCloseable} wrapper for {@link org.openrdf.query.impl.GraphQueryResultImpl}
 * @author  Fernando Hernandez
 *
 * @since   3.0
 * @version 3.0
 */
public final class GraphQueryResultImpl extends IterationWrapper<Statement, QueryEvaluationException> implements GraphQueryResult {

	private final Map<String, String> mNamespaces;

	public GraphQueryResultImpl(Map<String, String> theNamespaces, Iterable<? extends Statement> theStatements) {
		this(theNamespaces, theStatements.iterator());
	}

	public GraphQueryResultImpl(Map<String, String> theNamespaces, Iterator<? extends Statement> theIterator) {
		this(theNamespaces, new CloseableIteratorIteration<Statement, QueryEvaluationException>(theIterator));
	}

	public GraphQueryResultImpl(final Map<String, String> theNamespaces,
	                            final CloseableIteration<? extends Statement, ? extends QueryEvaluationException> theIteration) {
		super(theIteration);
		mNamespaces = ImmutableMap.copyOf(theNamespaces);
	}

	@Override
	public Map<String, String> getNamespaces() throws QueryEvaluationException {
		return mNamespaces;
	}
}
