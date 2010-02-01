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

package com.clarkparsia.openrdf;

import org.openrdf.model.Graph;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.Resource;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.rio.RDFParseException;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import com.clarkparsia.utils.collections.CollectionUtil;

/**
 * <p>Extends the {@link DelegatingGraph} class to provide additional convenience methods for working with a graph
 * object.</p>
 *
 * @author Michael Grove
 */
public class ExtGraph extends DelegatingGraph {
	public ExtGraph() {
	}

	public ExtGraph(final Graph theGraph) {
		super(theGraph);
	}

	/**
	 * Alias for the {@link #match} method
	 * @param theSubj the subject to match, or null for wildcard
	 * @param thePred the predicate to match, or null for wildcard
	 * @param theObject the object to match, or null for wildcard
	 * @return an iterator over all the statements which match the given spo pattern
	 */
	public Iterator<Statement> getStatements(Resource theSubj, URI thePred, Value theObject) {
		return match(theSubj, thePred, theObject);
	}
	
	/**
	 * Return the value of the property for the given subject.  If there are multiple values, only the first value will
	 * be returned.  Use {@link #getValues} if you want all values for the property.
	 * @param theSubj the subject
	 * @param thePred the property of the subject whose value should be retrieved
	 * @return the value of the the property for the subject, or null if there is no value.
	 */
	public Value getValue(Resource theSubj, URI thePred) {
		Collection<Value> aCollection = getValues(theSubj, thePred);
		if (!aCollection.isEmpty()) {
			return aCollection.iterator().next();
		}
		else {
			return null;
		}
	}

	/**
	 * Return an Iterable over all the values of the property on the given resource
	 * @param theSubj the resource
	 * @param thePred the property
	 * @return all values of the property on the resource.
	 */
	public Collection<Value> getValues(Resource theSubj, URI thePred) {
		return CollectionUtil.list(GraphUtil.getObjectIterator(this, theSubj, thePred));
	}

	/**
	 * Return the value of of the property as a Literal
	 * @param theSubj the resource
	 * @param thePred the property whose value is to be retrieved
	 * @return the property value as a literal, or null if the value is not a literal, or the property does not have a value
	 */
	public Literal getLiteral(Resource theSubj, URI thePred) {
		Value aVal = getValue(theSubj, thePred);

		if (aVal instanceof Literal) {
			return (Literal) aVal;
		}
		else {
			return null;
		}
	}

	/**
	 * Returns whether or not the given resource is a rdf:List
	 * @param theRes the resource to check
	 * @return true if its a list, false otherwise
	 */
	public boolean isList(Resource theRes) {
        Iterator<Statement> sIter = getStatements(theRes, RDF.FIRST, null);

		return theRes != null && theRes.equals(RDF.NIL) || sIter.hasNext();
	}

	/**
	 * Return the contents of the given list by following the rdf:first/rdf:rest structure of the list
	 * @param theRes the resouce which is the head of the list
	 * @return the contents of the list.
	 */
	public List<Value> asList(Resource theRes) {
        List<Value> aList = new ArrayList<Value>();

        Resource aListRes = theRes;

        while (aListRes != null) {

            Resource aFirst = (Resource) getValue(aListRes, RDF.FIRST);
            Resource aRest = (Resource) getValue(aListRes, RDF.REST);

            if (aFirst != null) {
               aList.add(aFirst);
            }

            if (aRest == null || aRest.equals(RDF.NIL)) {
               aListRes = null;
            }
            else {
                aListRes = aRest;
            }
        }

        return aList;
	}

	/**
	 * Return the rdf:type of the resource
	 * @param theSubj the resource
	 * @return the rdf:type, or null if it is not typed.
	 */
	public URI getType(Resource theSubj) {
		return (URI) getValue(theSubj, RDF.TYPE);
	}

	public void read(final File theFile) throws IOException, RDFParseException {
		read(new FileInputStream(theFile), Rio.getParserFormatForFileName(theFile.getName()));
	}

	public void read(final InputStream theStream, RDFFormat theFormat) throws IOException, RDFParseException {
		addAll(OpenRdfIO.readGraph(theStream, theFormat));
	}

	public void add(Graph theGraph) {
		addAll(theGraph);
	}
}
