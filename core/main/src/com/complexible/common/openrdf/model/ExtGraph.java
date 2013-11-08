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

package com.complexible.common.openrdf.model;

import java.util.List;

import com.google.common.base.Optional;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;


/**
 * <p>Extends the basic Sesame {@link Graph} interface to provide more concise access to various utility methods defined
 * in {@link Graphs} or other interfaces as well as safer access to some methods that are functionally equivalent to
 * what is defined in {@link org.openrdf.model.util.GraphUtil}; nulls are not returned nor is GraphUtilException thrown
 * for these methods.</p>
 *
 * <p>Also provides convenient accessors to common RDF & RDFS properties.</p>
 *
 * @author	Michael Grove
 * @since	0.8
 * @version 0.8
 *
 * @see Graphs
 * @see IOGraph
 * @see FunctionalGraph
 */
public interface ExtGraph extends IOGraph, FunctionalGraph {

	/**
	 * <p>Return whether or the graph contains any {@link Statement statements} matching the given SPO and optionally C pattern.</p>
	 *
	 * <p>This is functionally equivalent to calling <code>Graph.match(...).hasNext()</code></p>
	 *
	 * @param theSubj		the subject, or null for wildcard
	 * @param thePred		the predicate, or null for wildcard
	 * @param theObj		the object, or null for wildcard
	 * @param theContexts	optionally, the contexts to check
	 * @return				true if the Graph contains at least one statement matching the pattern, false otherwise
	 */
	public boolean contains(final Resource theSubj, final URI thePred, final Value theObj, final Resource... theContexts);

	/**
	 * Returns all the instances of the specified type
	 *
	 * @param theType	the type for instances to return
	 * @return			all instances in the graph rdf:type'd to the given type.
	 */
	public Iterable<Resource> getInstancesOf(final URI theType);

	/**
	 * Return a collection of all the individuals in the graph
	 * @return all individuals
	 */
	public Iterable<Resource> getIndividuals();

	/**
	 * Return the value of the property for the given subject.  If there are multiple values, only the first value will
	 * be returned.  Use {@link org.openrdf.model.util.GraphUtil#getObjectIterator} if you want all values for the property.
	 *
	 * @param theSubj	the subject
	 * @param thePred	the property of the subject whose value should be retrieved
	 *
	 * @return 			optionally, the value of the the property for the subject
	 *
	 * @see Graphs#getObject
	 */
	public Optional<Value> getObject(final Resource theSubj, final URI thePred);

	/**
	 * Return the value of of the property as a Literal
	 *
	 * @param theSubj	the resource
	 * @param thePred	the property whose value is to be retrieved
	 * @return 			Optionally, the property value as a literal.  Value will be absent of the SP does not have an O, or the O is not a literal
	 *
	 * @see Graphs#getLiteral(org.openrdf.model.Graph, org.openrdf.model.Resource, org.openrdf.model.URI)
	 */
	public Optional<Literal> getLiteral(final Resource theSubj, final URI thePred);

	/**
	 * Return the value of of the property as a Resource
	 *
	 * @param theSubj	the resource
	 * @param thePred	the property whose value is to be retrieved
	 * @return 			Optionally, the property value as a Resource.  Value will be absent of the SP does not have an O, or the O is not a Resource
	 *
	 * @see Graphs#getResource
	 */
	public Optional<Resource> getResource(final Resource theSubj, final URI thePred);

	/**
	 * Returns whether or not the given resource is a rdf:List
	 * @param theRes	the resource to check
	 * @return			true if its a list, false otherwise
	 * @see Graphs#isList
	 */
	public boolean isList(final Resource theRes);

	/**
	 * Return the contents of the given list by following the rdf:first/rdf:rest structure of the list
	 * @param theRes	the resource which is the head of the list
	 * @return 			the contents of the list.
	 *
	 * @see Graphs#asList(org.openrdf.model.Graph, org.openrdf.model.Resource)
	 */
	public List<Value> asList(final Resource theRes);

	/**
	 * Return the types of the provided instance
	 * @param theRes	the instance
	 * @return			the types
	 */
	public Iterable<Resource> getTypes(final Resource theRes);

	/**
	 * Return whether or not the instances is asserted to be of the provided type
	 * @param theRes	the instance
	 * @param theType	the type
	 * @return			true if the instance is asserted to be of the provided type, false otherwise
	 */
	public boolean isInstanceOf(final Resource theRes, final Resource theType);
}
