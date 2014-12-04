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

import java.util.Collection;

import com.complexible.common.openrdf.query.TupleQueryResult;
import info.aduna.iteration.Iteration;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * {@link AutoCloseable} wrapper for {@link org.openrdf.query.impl.MutableTupleQueryResult}
 * @author  Fernando Hernandez
 *
 * @since   3.0
 * @version 3.0
 */
public final class MutableTupleQueryResult extends org.openrdf.query.impl.MutableTupleQueryResult implements TupleQueryResult {

	public MutableTupleQueryResult(final Collection<String> bindingNames,
	                               final BindingSet... bindingSets) {
		super(bindingNames, bindingSets);
	}

	public MutableTupleQueryResult(final Collection<String> bindingNames,
	                               final Collection<? extends BindingSet> bindingSets) {
		super(bindingNames, bindingSets);
	}

	public <E extends Exception> MutableTupleQueryResult(final Collection<String> bindingNames,
	                                                     final Iteration<? extends BindingSet, E> bindingSetIter) throws E {
		super(bindingNames, bindingSetIter);
	}

	public MutableTupleQueryResult(final org.openrdf.query.TupleQueryResult tqr) throws QueryEvaluationException {
		super(tqr);
	}
}
