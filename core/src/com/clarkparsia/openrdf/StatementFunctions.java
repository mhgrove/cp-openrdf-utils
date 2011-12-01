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

package com.clarkparsia.openrdf;

import com.google.common.base.Function;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.URI;

/**
 * <p>Some common Function implementations for working with Statements</p>
 *
 * @author Michael Grove
 * @version 0.4.1
 * @since 0.4.1
 */
public final class StatementFunctions {
	private static final GetSubject GET_SUBJECT = new GetSubject();
	private static final GetPredicate GET_PREDICATE = new GetPredicate();
	private static final GetObject GET_OBJECT = new GetObject();
	private static final GetContext GET_CONTEXT = new GetContext();

	/**
	 * No instances
	 */
	private StatementFunctions() {
	}

	/**
	 * Return a Function which will retrieve the Subject of a Statement
	 * @return the function
	 */
	public static Function<Statement, Resource> getSubject() {
		return GET_SUBJECT;
	}

	/**
	 * Return a Function which will retrieve the Predicate of a Statement
	 * @return the function
	 */
	public static Function<Statement, URI> getPredicate() {
		return GET_PREDICATE;
	}

	/**
	 * Return a Function which will retrieve the Object of a Statement
	 * @return the function
	 */
	public static Function<Statement, Value> getObject() {
		return GET_OBJECT;
	}

	/**
	 * Return a Function which will retrieve the Context of a Statement
	 * @return the function
	 */
	public static Function<Statement, Resource> getContext() {
		return GET_CONTEXT;
	}

	private static class GetSubject implements Function<Statement, Resource> {
		/**
		 * @inheritDoc
		 */
		public Resource apply(final Statement theStatement) {
			return theStatement.getSubject();
		}
	}

	private static class GetPredicate implements Function<Statement, URI> {
		/**
		 * @inheritDoc
		 */
		public URI apply(final Statement theStatement) {
			return theStatement.getPredicate();
		}
	}

	private static class GetObject implements Function<Statement, Value> {
		/**
		 * @inheritDoc
		 */
		public Value apply(final Statement theStatement) {
			return theStatement.getObject();
		}
	}

	private static class GetContext implements Function<Statement, Resource> {
		/**
		 * @inheritDoc
		 */
		public Resource apply(final Statement theStatement) {
			return theStatement.getContext();
		}
	}
}
