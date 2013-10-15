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

import com.google.common.collect.ImmutableSet;
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

		public ImmutableDatasetBuilder defaultGraphs(final Set<URI> theDefaultGraphs) {
			mDefaultGraphs = theDefaultGraphs;
			return this;
		}

		public ImmutableDatasetBuilder removeGraphs(final Set<URI> theRemoveGraphs) {
			mRemoveGraphs = theRemoveGraphs;
			return this;
		}

		public ImmutableDatasetBuilder namedGraphs(final Set<URI> theNamedGraphs) {
			mNamedGraphs = theNamedGraphs;
			return this;
		}
	}
}
