/*
 * Copyright (c) 2009-2012 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

package com.complexible.common.openrdf.model;

import com.google.common.base.Function;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.turtle.TurtleUtil;

/**
 * <p>Some common Function implementations for working with Statements</p>
 *
 * @author  Michael Grove
 * @since	0.4.1
 * @version	2.0.3
 */
public final class Statements {
	private static final GetSubject GET_SUBJECT = new GetSubject();
	private static final GetPredicate GET_PREDICATE = new GetPredicate();
	private static final GetObject GET_OBJECT = new GetObject();
	private static final GetContext GET_CONTEXT = new GetContext();

	/**
	 * No instances
	 */
	private Statements() {
		throw new AssertionError();
	}

	public static Predicate<Statement> subjectIs(final Resource theSubj) {
		return Predicates.compose(Predicates.equalTo(theSubj), subject());
	}

	public static Predicate<Statement> predicateIs(final URI thePred) {
		return Predicates.compose(Predicates.equalTo(thePred), predicate());
	}

	public static Predicate<Statement> objectIs(final Value theObj) {
		return Predicates.compose(Predicates.equalTo(theObj), object());
	}

	public static Predicate<Statement> contextIs(final Resource theContext) {
		return Predicates.compose(Predicates.equalTo(theContext), context());
	}

	public static Predicate<Statement> objectIs(final Class<? extends Value> theValue) {
		return Predicates.compose(Predicates.instanceOf(theValue), object());
	}

	/**
	 * Return a Function which will retrieve the Subject of a Statement
	 * @return the function
	 */
	public static Function<Statement, Resource> subject() {
		return GET_SUBJECT;
	}

	/**
	 * Return a Function which will retrieve the Predicate of a Statement
	 * @return the function
	 */
	public static Function<Statement, URI> predicate() {
		return GET_PREDICATE;
	}

	/**
	 * Return a Function which will retrieve the Object of a Statement
	 * @return the function
	 */
	public static Function<Statement, Value> object() {
		return GET_OBJECT;
	}

	/**
	 * Return a Function which will retrieve the Context of a Statement
	 * @return the function
	 */
	public static Function<Statement, Resource> context() {
		return GET_CONTEXT;
	}

	public static Function<Statement, Optional<Resource>> subjectOptional() {
		return new Function<Statement, Optional<Resource>>() {
			public Optional<Resource> apply(final Statement theStatement) {
				return Optional.of(theStatement.getSubject());
			}
		};
	}

	public static Function<Statement, Optional<URI>> predicateOptional() {
		return new Function<Statement, Optional<URI>>() {
			public Optional<URI> apply(final Statement theStatement) {
				return Optional.of(theStatement.getPredicate());
			}
		};
	}

	public static Function<Statement, Optional<Value>> objectOptional() {
		return new Function<Statement, Optional<Value>>() {
			public Optional<Value> apply(final Statement theStatement) {
				return Optional.of(theStatement.getObject());
			}
		};
	}

	public static Function<Statement, Optional<Literal>> objectAsLiteral() {
		return new Function<Statement, Optional<Literal>>() {
			public Optional<Literal> apply(final Statement theStatement) {
				return theStatement.getObject() instanceof Literal
					   ? Optional.of((Literal) theStatement.getObject()) : Optional.<Literal>absent();
			}
		};
	}

	public static Function<Statement, Optional<Resource>> objectAsResource() {
		return new Function<Statement, Optional<Resource>>() {
			public Optional<Resource> apply(final Statement theStatement) {
				return theStatement.getObject() instanceof Resource ? Optional.of((Resource) theStatement.getObject()) : Optional.<Resource>absent();
			}
		};
	}

	public static Function<Statement, Optional<Resource>> contextOptional() {
		return new Function<Statement, Optional<Resource>>() {
			public Optional<Resource> apply(final Statement theStatement) {
				return theStatement.getContext() != null ? Optional.of(theStatement.getContext())
														 : Optional.<Resource>absent();
			}
		};
	}

	/**
	 * Return whether or not the literal object is valid.  This will return true if the literal represented by this
	 * object would have been parseable.  Used to validate input coming in from users from non-IO sources (which get
	 * validated via the fact they got parsed).
	 *
	 * Validates the language tag is not malformed and basic XSD datatype checks.
	 *
	 * @param theLiteral	the literal to validate
	 *
	 * @return 				true if its a valid/parseable literal, false otherwise
	 */
	public static boolean isLiteralValid(final Literal theLiteral) {
		if (theLiteral.getLanguage() != null && theLiteral.getLanguage().length() > 0) {
			final String aLang = theLiteral.getLanguage();

			if (!TurtleUtil.isLanguageStartChar(aLang.charAt(0))) {
				return false;
			}

			for (int aIndex = 1; aIndex < aLang.length(); aIndex++) {
				if (!TurtleUtil.isLanguageChar(aLang.charAt(aIndex))) {
					return false;
				}
			}
		}

		// TODO: all datatypes?  all variations?
		if (theLiteral.getDatatype() != null && theLiteral.getDatatype().getNamespace().equals(XMLSchema.NAMESPACE)) {
			final String aTypeName = theLiteral.getDatatype().getLocalName();

			try {
				if (aTypeName.equals(XMLSchema.DATETIME.getLocalName())) {
					theLiteral.calendarValue();
				}
				else if (aTypeName.equals(XMLSchema.INT.getLocalName())) {
					theLiteral.intValue();
				}
				else if (aTypeName.equals(XMLSchema.FLOAT.getLocalName())) {
					theLiteral.floatValue();
				}
				else if (aTypeName.equals(XMLSchema.LONG.getLocalName())) {
					theLiteral.longValue();
				}
				else if (aTypeName.equals(XMLSchema.DOUBLE.getLocalName())) {
					theLiteral.doubleValue();
				}
				else if (aTypeName.equals(XMLSchema.SHORT.getLocalName())) {
					theLiteral.shortValue();
				}
				else if (aTypeName.equals(XMLSchema.BOOLEAN.getLocalName())) {
					theLiteral.booleanValue();
				}
				else if (aTypeName.equals(XMLSchema.BYTE.getLocalName())) {
					theLiteral.byteValue();
				}
				else if (aTypeName.equals(XMLSchema.DECIMAL.getLocalName())) {
					theLiteral.decimalValue();
				}
			}
			catch (Exception e) {
				return false;
			}
		}

		return true;
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
