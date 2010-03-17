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

package com.clarkparsia.openrdf.util;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import com.clarkparsia.openrdf.ExtGraph;

/**
 * <p>Utility class for creating a set of statements using {@link ResourceBuilder ResourceBuilders}.</p>
 *
 * @author Michael Grove
 * @since 0.1
 */
public class GraphBuilder {
    private ExtGraph mGraph;

    public GraphBuilder() {
        mGraph = new ExtGraph();
    }

    public ExtGraph graph() {
        return mGraph;
    }

    public void reset() {
        mGraph.clear();
    }

    public ResourceBuilder uri(URI theURI) {
        return new ResourceBuilder(mGraph, mGraph.getValueFactory().createURI(theURI.toString()));
    }

    public ResourceBuilder uri(String theURI) {
        return instance(null, theURI);
    }

    public ResourceBuilder instance(URI theType) {
        return instance(theType, (String) null);
    }

	/**
	 * Create an un-typed BNode in the graph
	 * @return a ResourceBuilder wrapping the bnode
	 */
	public ResourceBuilder instance() {
		return instance(null, (String) null);
	}

	public ResourceBuilder instance(URI theType, java.net.URI theURI) {
		return instance(theType, theURI.toString());
	}

	public ResourceBuilder instance(URI theType, Resource theRes) {
		if (theType != null) {
			mGraph.add(theRes, RDF.TYPE, theType);
		}

		return new ResourceBuilder(mGraph, theRes);
	}

    public ResourceBuilder instance(URI theType, String theURI) {
        Resource aRes = theURI == null
                        ? mGraph.getValueFactory().createBNode()
                        : mGraph.getValueFactory().createURI(theURI);

        if (theType != null) {
            mGraph.add(aRes, RDF.TYPE, theType);
        }

        return new ResourceBuilder(mGraph, aRes);
    }

	public ValueFactory getValueFactory() {
		return graph().getValueFactory();
	}
}
