// Copyright (c) 2010 - 2011 -- Clark & Parsia, LLC. <http://www.clarkparsia.com>
// For more information about licensing and copyright of this software, please contact
// inquiries@clarkparsia.com or visit http://stardog.com

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
