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

package com.clarkparsia.openrdf;

import org.openrdf.model.Literal;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryConnection;

import org.openrdf.repository.sail.SailRepository;

import org.openrdf.rio.turtle.TurtleUtil;
import org.openrdf.sail.memory.MemoryStore;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * <p>Utility methods for working with the OpenRDF Sesame API.</p>
 *
 * @author	Michael Grove
 * @since	0.1
 * @version	0.7
 */
public final class OpenRdfUtil {
	/**
	 * the logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OpenRdfUtil.class);

	/**
	 * No instances
	 */
	private OpenRdfUtil() {
	}

	/**
	 * Create a simple in-memory {@link Repository}
	 * @return an in memory Repository
	 */
	public static ExtRepository createInMemoryRepo() {
		try {
			Repository aRepo = new SailRepository(new MemoryStore());

			aRepo.initialize();

			return new ExtRepository(aRepo);
		}
		catch (RepositoryException e) {
			// impossible?
			throw new RuntimeException(e);
		}
	}

	/**
	 * Quietly close the connection object
	 * @param theConn the connection to close
	 */
	public static void close(RepositoryConnection theConn) {
		if (theConn != null) {
			try {
				theConn.commit();

				theConn.close();
			}
			catch (RepositoryException e) {
				LOGGER.error("There was an error while closing the RepositoryConnection.", e);
			}
		}
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
}
