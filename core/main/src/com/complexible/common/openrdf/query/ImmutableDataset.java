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

package com.complexible.common.openrdf.query;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ObjectArrays;
import org.openrdf.model.URI;
import org.openrdf.query.Dataset;

import java.util.Set;

/**
 * <p>A {@link Dataset} implementation which is immutable</p>
 *
 * @author  Michael Grove
 * @since   1.1.1
 * @version 1.1.1
 */
public final class ImmutableDataset implements Dataset {
	private final ImmutableSet<URI> mNamedGraphs;

	private final URI mInsertURI;

	private final ImmutableSet<URI> mRemoveGraphs;

	private final ImmutableSet<URI> mDefaultGraphs;

	private ImmutableDataset(final ImmutableSet<URI> theDefaultGraphs,
                             final ImmutableSet<URI> theNamedGraphs,
                             final URI theInsertURI,
                             final ImmutableSet<URI> theRemoveURI) {
		mInsertURI = theInsertURI;
		mRemoveGraphs = theRemoveURI;
		mDefaultGraphs = theDefaultGraphs;
		mNamedGraphs = theNamedGraphs;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Set<URI> getDefaultRemoveGraphs() {
		return mRemoveGraphs;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public URI getDefaultInsertGraph() {
		return mInsertURI;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Set<URI> getDefaultGraphs() {
		return mDefaultGraphs;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Set<URI> getNamedGraphs() {
		return mNamedGraphs;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(mDefaultGraphs, mNamedGraphs, mInsertURI, mRemoveGraphs);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean equals(final Object theObj) {
		if (theObj == null) {
			return false;
		}
		else if (theObj == this) {
			return true;
		}
		else if (theObj instanceof ImmutableDataset) {
			ImmutableDataset aDataset = (ImmutableDataset) theObj;
			return Objects.equal(mDefaultGraphs, aDataset.mDefaultGraphs)
				&& Objects.equal(mNamedGraphs, aDataset.mNamedGraphs)
				&& Objects.equal(mRemoveGraphs, aDataset.mRemoveGraphs)
				&& Objects.equal(mInsertURI, aDataset.mInsertURI);
		}
		else {
			return false;
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public String toString() {
		final Objects.ToStringHelper aStringHelper = Objects.toStringHelper("Dataset")
		                                                    .add("defaultGraphs", mDefaultGraphs)
		                                                    .add("namedGraphs", mNamedGraphs);
		if (!mRemoveGraphs.isEmpty()) {
			aStringHelper.add("removeGraphs", mRemoveGraphs);
		}

		if (mInsertURI != null) {
			aStringHelper.add("insertURI", mInsertURI);
		}

		return aStringHelper.toString();
	}

	public static ImmutableDatasetBuilder builder() {
		return new ImmutableDatasetBuilder();
	}

	public static final class ImmutableDatasetBuilder {
		private Set<URI> mNamedGraphs = ImmutableSet.of();

		private URI mInsertURI = null;

		private Set<URI> mRemoveGraphs = ImmutableSet.of();

		private Set<URI> mDefaultGraphs = ImmutableSet.of();

		public ImmutableDataset build() {
			return new ImmutableDataset(ImmutableSet.copyOf(mDefaultGraphs),
		                                ImmutableSet.copyOf(mNamedGraphs),
		                                mInsertURI,
		                                ImmutableSet.copyOf(mRemoveGraphs));
		}

		public ImmutableDatasetBuilder insertGraph(final URI theInsertGraph) {
			mInsertURI = theInsertGraph;
			return this;
		}

		public ImmutableDatasetBuilder defaultGraphs(final URI theDefaultGraph, final URI... theOtherGraphs) {
			defaultGraphs(ImmutableSet.copyOf(ObjectArrays.concat(theDefaultGraph, theOtherGraphs)));
			return this;
		}

		public ImmutableDatasetBuilder defaultGraphs(final Iterable<URI> theDefaultGraphs) {
			mDefaultGraphs = ImmutableSet.copyOf(theDefaultGraphs);
			return this;
		}

		public ImmutableDatasetBuilder removeGraphs(final URI theGraph, final URI... theOtherGraphs) {
			removeGraphs(ImmutableSet.copyOf(ObjectArrays.concat(theGraph, theOtherGraphs)));
			return this;
		}

		public ImmutableDatasetBuilder removeGraphs(final Iterable<URI> theRemoveGraphs) {
			mRemoveGraphs = ImmutableSet.copyOf(theRemoveGraphs);
			return this;
		}

		public ImmutableDatasetBuilder namedGraphs(final URI theGraph, final URI... theOtherGraphs) {
			namedGraphs(ImmutableSet.copyOf(ObjectArrays.concat(theGraph, theOtherGraphs)));
			return this;
		}

		public ImmutableDatasetBuilder namedGraphs(final Iterable<URI> theNamedGraphs) {
			mNamedGraphs = ImmutableSet.copyOf(theNamedGraphs);
			return this;
		}
	}
}
