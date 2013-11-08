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

package com.complexible.common.openrdf.model;

import org.openrdf.model.Statement;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.URI;

import com.google.common.base.Preconditions;
import com.google.common.base.Objects;

/**
 * <p>Implementation of a Sesame {@link Statement} which takes into account the context for {@link #equals} but not 
 * {@link #hashCode} so it is still compatible with default Sesame implementations.</p>
 *
 * @author	Michael Grove
 * @since	0.4.1
 * @version 0.4.1
 */
public final class ContextAwareStatement implements Statement {

	/**
	 * The subject
	 */
	private final Resource mSubject;

	/**
	 * The predicate
	 */
	private final URI mPredicate;

	/**
	 * The object
	 */
	private final Value mObject;

	/**
	 * The context
	 */
	private final Resource mContext;

	/**
	 * Create a new StatementWithContext
	 *
	 * @param theSubject	the subject
	 * @param thePredicate	the predicate
	 * @param theObject		the object
	 * @param theContext	the context, or null
	 */
	public ContextAwareStatement(final Resource theSubject, final URI thePredicate, final Value theObject, final Resource theContext) {
		Preconditions.checkNotNull(theSubject);
		Preconditions.checkNotNull(thePredicate);
		Preconditions.checkNotNull(theObject);
		
		mSubject = theSubject;
		mPredicate = thePredicate;
		mObject = theObject;
		mContext = theContext;
	}

	/**
	 * @inheritDoc
	 */
	public Resource getSubject() {
		return mSubject;
	}

	/**
	 * @inheritDoc
	 */
	public URI getPredicate() {
		return mPredicate;
	}

	/**
	 * @inheritDoc
	 */
	public Value getObject() {
		return mObject;
	}

	/**
	 * @inheritDoc
	 */
	public Resource getContext() {
		return mContext;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean equals(final Object theObj) {
		if (this == theObj) {
			return true;
		}

		// this instanceof check is technically incorrect, it breaks that contract of equals since this thing
		// could be considered equals to an instance of StatementImpl -- but for now, that's kind of desireable
		// that lets something like ModelUtil.equal work as expected when the spo & c's are equal on the statements
		// but the types are not strictly equal.  this should be resolved at some point, but this class exists
		// as a workaround for the fact that StatementImpl works incorrectly for most use cases, this is probably ok.
		if (theObj == null || !(theObj instanceof Statement)) {
			return false;
		}

		final Statement that = (Statement) theObj;

		return mObject.equals(that.getObject())
			   && mSubject.equals(that.getSubject())
			   && mPredicate.equals(that.getPredicate())
			   && Objects.equal(mContext, that.getContext());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		return 961 * mSubject.hashCode() + 31 * mPredicate.hashCode() + mObject.hashCode();// + (mContext == null ? 0 : 17 * mContext.hashCode());
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String toString() {
		StringBuilder aStringBuilder = new StringBuilder(256);

		aStringBuilder.append("(");
		aStringBuilder.append(getSubject());
		aStringBuilder.append(", ");
		aStringBuilder.append(getPredicate());
		aStringBuilder.append(", ");
		aStringBuilder.append(getObject());
		aStringBuilder.append(")");

		aStringBuilder.append(" [").append(getContext()).append("]");

		return aStringBuilder.toString();
	}
}
