/*
 * Copyright (c) 2009-2013 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * <p>{@link ValueFactory} implementation which will create {@link Statement statements} of the type {@link ContextAwareStatement},
 * otherwise it delegates creation of objects to the default ValueFactory</p>
 *
 *
 * @author  Michael Grove
 * @since   0.4.1
 * @version 1.0
 */
public final class ContextAwareValueFactory implements ValueFactory {
	private final ValueFactory mFactory;

	/**
	 * Create a new ContextAwareValueFactory wrapping {@link ValueFactoryImpl#getInstance()}
	 */
	public ContextAwareValueFactory() {
		this(ValueFactoryImpl.getInstance());
	}

	/**
	 * Create a new ContextAwareValueFactory wrapping the provided ValueFactory
	 * @param theFactory	the actual ValueFactory
	 */
	public ContextAwareValueFactory(final ValueFactory theFactory) {
		mFactory = theFactory;
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public URI createURI(final String theURI) {
		return mFactory.createURI(theURI);
	}

	/**
	 * @inheritDoc
	 */
	public URI createURI(final String s, final String s1) {
		return mFactory.createURI(s, s1);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public BNode createBNode() {
		return mFactory.createBNode();
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public BNode createBNode(final String theId) {
		return mFactory.createBNode(theId);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Literal createLiteral(final String theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Literal createLiteral(final String theValue, final String theLang) {
		return mFactory.createLiteral(theValue, theLang);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Literal createLiteral(final String theValue, final URI theDatatype) {
		return mFactory.createLiteral(theValue, theDatatype);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Literal createLiteral(final boolean theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Literal createLiteral(final byte theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Literal createLiteral(final short theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Literal createLiteral(final int theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Literal createLiteral(final long theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Literal createLiteral(final float theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Literal createLiteral(final double theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Literal createLiteral(final XMLGregorianCalendar theXMLGregorianCalendar) {
		return mFactory.createLiteral(theXMLGregorianCalendar);
	}

    /**
     * @inheritDoc
     */
    @Override
    public Literal createLiteral(final Date theDate) {
        return mFactory.createLiteral(theDate);
    }

    /**
	 * @inheritDoc
	 */
    @Override
	public Statement createStatement(final Resource theSubject, final URI thePredicate, final Value theObject) {
		return createStatement(theSubject, thePredicate, theObject, null);
	}

	/**
	 * @inheritDoc
	 */
    @Override
	public Statement createStatement(final Resource theSubject, final URI thePredicate, final Value theObject, final Resource theContext) {
		return new ContextAwareStatement(theSubject,  thePredicate, theObject, theContext);
	}
}
