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

package com.clarkparsia.openrdf.query.builder;

import org.openrdf.query.parser.ParsedQuery;

import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;

/**
 * <p>Builder class for creating Unioned groups</p>
 *
 * @author Michael Grove
 * @version 0.2.2
 * @since 0.2.2
 */
public class UnionBuilder<T extends ParsedQuery, E extends SupportsGroups> implements SupportsGroups<UnionBuilder<T, E>>, Group {

	/**
	 * Left operand
	 */
	private Group mLeft;

	/**
	 * Right operand
	 */
	private Group mRight;

	/**
	 * Parent builder
	 */
	private GroupBuilder<T,E> mParent;

	public UnionBuilder(final GroupBuilder<T, E> theParent) {
		mParent = theParent;
	}

	/**
	 * Return a builder for creating the left operand of the union
	 * @return builder for left operand
	 */
	public GroupBuilder<T, UnionBuilder<T, E>> left() {
		return new GroupBuilder<T, UnionBuilder<T, E>>(this);
	}

	/**
	 * Return a builder for creating the right operand of the union
	 * @return builder for right operand
	 */
	public GroupBuilder<T, UnionBuilder<T, E>> right() {
		return new GroupBuilder<T, UnionBuilder<T, E>>(this);
	}

	/**
	 * Close this union and return it's parent group builder.
	 * @return the parent builder
	 */
	public GroupBuilder<T,E> closeUnion() {
		return mParent;
	}

	/**
	 * @inheritDoc
	 */
	public UnionBuilder<T, E> addGroup(final Group theGroup) {
		if (mLeft == null) {
			mLeft = theGroup;
		}
		else if (mRight == null) {
			mRight = theGroup;
		}
		else {
			throw new IllegalArgumentException("Cannot set left or right arguments of union, both already set");
		}

		return this;
	}

	/**
	 * @inheritDoc
	 */
	public UnionBuilder<T, E> removeGroup(final Group theGroup) {
		if (mLeft != null && mLeft.equals(theGroup)) {
			mLeft = null;
		}
		else if (mRight != null && mRight.equals(theGroup)) {
			mRight = null;
		}

		return this;
	}

	/**
	 * @inheritDoc
	 */
	public void addChild(final Group theGroup) {
		addGroup(theGroup);
	}

	/**
	 * @inheritDoc
	 */
	public TupleExpr expr() {
		if (mLeft != null && mRight != null) {
			return new Union(mLeft.expr(), mRight.expr());
		}
		else if (mLeft != null && mRight == null) {
			return mLeft.expr();

		}
		else if (mRight != null && mLeft == null) {
			return mRight.expr();
		}
		else {
			return null;
		}
	}

	/**
	 * @inheritDoc
	 */
	public boolean isOptional() {
		return false;
	}
}
