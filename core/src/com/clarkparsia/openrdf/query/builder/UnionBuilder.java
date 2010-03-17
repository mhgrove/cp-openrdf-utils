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
import com.clarkparsia.openrdf.query.builder.SupportsGroups;
import com.clarkparsia.openrdf.query.builder.Group;
import com.clarkparsia.openrdf.query.builder.GroupBuilder;

/**
 * <p>Builder class for creating Unioned groups</p>
 *
 * @author Michael Grove
 * @version 0.2.2
 * @since 0.2.2
 */
public class UnionBuilder<T extends ParsedQuery> implements SupportsGroups<UnionBuilder<T>>, Group {
	private Group mLeft;
	private Group mRight;
	private GroupBuilder<T,?> mParent;

	public UnionBuilder(final GroupBuilder<T, ?> theParent) {
		mParent = theParent;
	}

	public GroupBuilder<T, UnionBuilder<T>> left() {
		return new GroupBuilder<T, UnionBuilder<T>>(this);
	}

	public GroupBuilder<T, UnionBuilder<T>> right() {
		return new GroupBuilder<T, UnionBuilder<T>>(this);
	}

	public GroupBuilder<T,?> closeUnion() {
		return mParent;
	}

	/**
	 * @inheritDoc
	 */
	public UnionBuilder<T> addGroup(final Group theGroup) {
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

	public UnionBuilder<T> removeGroup(final Group theGroup) {
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
