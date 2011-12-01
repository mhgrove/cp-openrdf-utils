// Copyright (c) 2010 - 2011 -- Clark & Parsia, LLC. <http://www.clarkparsia.com>
// For more information about licensing and copyright of this software, please contact
// inquiries@clarkparsia.com or visit http://stardog.com

package com.clarkparsia.openrdf;

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
 * @author Michael Grove
 * @version 0.4.1
 * @since 0.4.1
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
	public BNode createBNode() {
		return mFactory.createBNode();
	}

	/**
	 * @inheritDoc
	 */
	public BNode createBNode(final String theId) {
		return mFactory.createBNode(theId);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(final String theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(final String theValue, final String theLang) {
		return mFactory.createLiteral(theValue, theLang);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(final String theValue, final URI theDatatype) {
		return mFactory.createLiteral(theValue, theDatatype);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(final boolean theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(final byte theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(final short theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(final int theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(final long theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(final float theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(final double theValue) {
		return mFactory.createLiteral(theValue);
	}

	/**
	 * @inheritDoc
	 */
	public Literal createLiteral(final XMLGregorianCalendar theXMLGregorianCalendar) {
		return mFactory.createLiteral(theXMLGregorianCalendar);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createStatement(final Resource theSubject, final URI thePredicate, final Value theObject) {
		return createStatement(theSubject, thePredicate, theObject, null);
	}

	/**
	 * @inheritDoc
	 */
	public Statement createStatement(final Resource theSubject, final URI thePredicate, final Value theObject, final Resource theContext) {
		return new ContextAwareStatement(theSubject,  thePredicate, theObject, theContext);
	}
}
