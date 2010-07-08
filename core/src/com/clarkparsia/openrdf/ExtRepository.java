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

import org.openrdf.repository.base.RepositoryWrapper;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.Statement;
import org.openrdf.model.Graph;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import org.openrdf.model.impl.GraphImpl;

import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.BindingSet;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.GraphQueryResult;

import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Collections;
import java.io.IOException;
import java.io.InputStream;

import com.clarkparsia.utils.collections.CollectionUtil;
import static com.clarkparsia.utils.collections.CollectionUtil.transform;
import com.clarkparsia.utils.Function;
import com.clarkparsia.utils.FunctionUtil;
import static com.clarkparsia.utils.FunctionUtil.compose;

import static com.clarkparsia.openrdf.OpenRdfUtil.close;
import static com.clarkparsia.openrdf.OpenRdfUtil.asGraph;
import com.clarkparsia.openrdf.util.IterationIterator;
import com.clarkparsia.openrdf.query.SesameQueryUtils;

import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.CloseableIteration;

/**
 * <p>Extends the normal Sesame Repository, via RepositoryWrapper, with some additional utility functions.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @since 0.2.2
 */
public class ExtRepository extends RepositoryWrapper {
	private static Logger LOGGER = LogManager.getLogger("com.clarkparsia.openrdf");

	/**
	 * Create a new in-memory ExtRepository
	 */
	public ExtRepository() {
		super(new SailRepository(new MemoryStore()));
	}

	/**
	 * Create a new ExtRepository which wraps the provided source.
	 * @param theRepository the source repository
	 */
	public ExtRepository(final Repository theRepository) {
		super(theRepository);
	}

	/**
	 * Return a graph which describes the given Resource
	 * @param theResource the resource to describe
	 * @return the graph which describes the URI
	 */
	public ExtGraph describe(Resource theResource) {
		Graph aGraph = new GraphImpl();

		RepositoryConnection aConn = null;

		try {
			aConn = getConnection();

			aGraph.addAll(CollectionUtil.set(new IterationIterator<Statement>(getStatements(theResource, null, null))));
		}
		catch (Exception ex) {
			LOGGER.error(ex);
		}
		finally {
			close(aConn);
		}

		return new ExtGraph(aGraph);
	}

	/**
	 * Return a RepositoryResult over all the statements in the repository.
	 * @return an iteration of all statements
	 */
	public RepositoryResult<Statement> getStatements() {
		return getStatements(null, null, null);
	}


	/**
	 * Return a RepositoryResult over the statements in this Repository which match the given spo pattern.
	 * @param theSubj the subject to search for, or null for any
	 * @param thePred the predicate to search for, or null for any
	 * @param theObj the object to search for, or null for any
	 * @return an Iterable over the matching statements
	 */
	public RepositoryResult<Statement> getStatements(Resource theSubj, URI thePred, Value theObj) {
		RepositoryConnection aConn = null;
		try {
			aConn = getConnection();

			return aConn.getStatements(theSubj, thePred, theObj, true);
		}
		catch (Exception ex) {
			close(aConn);

			LOGGER.error(ex);

			return new RepositoryResult<Statement>(emptyStatementIteration());
		}
	}

	/**
	 * Return an empty Iteration over Statements
	 * @return an empty iteration
	 */
	private CloseableIteration<Statement, RepositoryException> emptyStatementIteration() {
		return new EmptyIteration<Statement, RepositoryException>();
	}

	/**
	 * Execute a select query
	 * @param theQuery the query to execute
	 * @return the query result set.
	 * @throws RepositoryException if there is an error while querying
	 * @throws MalformedQueryException if the query cannot be parsed
	 * @throws QueryEvaluationException if there is an error while querying
	 */
	public TupleQueryResult selectQuery(SesameQuery theQuery) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		return selectQuery(theQuery.getLanguage(), theQuery.getQueryString()); 
	}

	/**
	 * Execute a select query.
	 * @param theLang the query language
	 * @param theQuery the query to execute
	 * @return the result set
	 * @throws RepositoryException if there is an error while querying
	 * @throws MalformedQueryException if the query cannot be parsed
	 * @throws QueryEvaluationException if there is an error while querying
	 */
	public TupleQueryResult selectQuery(QueryLanguage theLang, String theQuery) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		RepositoryConnection aConn = null;
		try {
			aConn = getConnection();
			return aConn.prepareTupleQuery(theLang, theQuery).evaluate();
		}
		catch (RepositoryException e) {
			close(aConn);
			throw e;
		}
	}

	/**
	 * Execute a construct query against this repository
	 * @param theQuery the Query to execute
	 * @return the results of the construct query
	 * @throws RepositoryException if there is an error while querying
	 * @throws MalformedQueryException if the query cannot be parsed
	 * @throws QueryEvaluationException if there is an error while querying
	 */
	public ExtGraph constructQuery(SesameQuery theQuery) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		return constructQuery(theQuery.getLanguage(), theQuery.getQueryString());
	}

	/**
	 * Execute a construct query against this repository
	 * @param theLang the query language
	 * @param theQuery the query string
	 * @return the results of the construct query
	 * @throws RepositoryException if there is an error while querying
	 * @throws MalformedQueryException if the query cannot be parsed
	 * @throws QueryEvaluationException if there is an error while querying
	 */
	public ExtGraph constructQuery(QueryLanguage theLang, String theQuery) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		RepositoryConnection aConn = null;

		try {
			aConn = getConnection();

			ExtGraph aGraph = new ExtGraph();

			GraphQueryResult aResult = aConn.prepareGraphQuery(theLang, theQuery).evaluate();
			while (aResult.hasNext()) {
				aGraph.add(aResult.next());
			}

			return aGraph;
		}
		finally {
			close(aConn);
		}
	}
	/**
	 * Read data in the specified format from the stream and insert it into this Repository
	 * @param theStream the stream to read data from
	 * @param theFormat the format the data is in
	 * @throws IOException thrown if there is an error while reading from the stream
	 * @throws RDFParseException thrown if the data cannot be parsed into the specified format
	 */
	public void read(InputStream theStream, RDFFormat theFormat) throws IOException, RDFParseException {
		OpenRdfIO.addData(this, theStream, theFormat);
	}

	/**
	 * List all the subjects which have the given predicate and object.
	 * @param thePredicate the predicate to search for, or null for any predicate
	 * @param theObject the object to search for, or null for any object
	 * @return the list of subjects who have properties matching the po pattern.
	 */
	public Collection<Resource> getSubjects(URI thePredicate, Value theObject) {
		String aQuery = "select uri from {uri} " + (thePredicate == null ? "p" : SesameQueryUtils.getSerqlQueryString(thePredicate)) + " {" + (theObject == null ? "o" : SesameQueryUtils.getSerqlQueryString(theObject)) + "}";

		RepositoryConnection aConn = null;

		try {
			Collection<Resource> aSubjects = new HashSet<Resource>();

			aConn = getConnection();

			TupleQueryResult aResult = aConn.prepareTupleQuery(QueryLanguage.SERQL, aQuery).evaluate();

			while (aResult.hasNext()) {
				aSubjects.add((Resource) aResult.next().getValue("uri"));
			}

			aResult.close();

			return aSubjects;
		}
		catch (Exception e) {
			LOGGER.error(e);

			return Collections.emptySet();
		}
		finally {
			close(aConn);
		}
	}

	/**
	 * Return the value of the property on the resource
	 * @param theSubj the subject
	 * @param thePred the property to get from the subject
	 * @return the first value of the property for the resource, or null if it does not have the specified property or does not exist.
	 */
	public Value getValue(Resource theSubj, URI thePred) {
        Iterable<Value> aIter = getValues(theSubj, thePred);

        if (aIter.iterator().hasNext()) {
            return aIter.iterator().next();
        }
        else {
			return null;
		}
	}

	/**
	 * Return the superclasses of the given resource
	 * @param theRes the resource
	 * @return the resource's superclasses
	 */
	public Iterable<Resource> getSuperclasses(Resource theRes) {
		return transform(new IterationIterator<Statement>(getStatements(theRes, RDFS.SUBCLASSOF, null)),
						 compose(new Function<Statement, Value>() {public Value apply(Statement theStmt) { return theStmt.getObject(); } },
								 new FunctionUtil.Cast<Value, Resource>(Resource.class)));
	}

	/**
	 * Return the values of the subject for the given property
	 * @param theSubj the subject
	 * @param thePred the property of the subject to get values for
	 * @return an iterable set of values of the property
	 */
	public Iterable<Value> getValues(Resource theSubj, URI thePred) {
		if (theSubj == null || thePred == null) {
			return Collections.emptySet();
		}

        try {
            String aQuery = "select value from {"+ SesameQueryUtils.getSerqlQueryString(theSubj)+"} <"+thePred+"> {value}";

            TupleQueryResult aTable = selectQuery(QueryLanguage.SERQL, aQuery);

            return transform(new IterationIterator<BindingSet>(aTable), new Function<BindingSet, Value>() {
				public Value apply(final BindingSet theIn) {
					return theIn.getValue("value");
				}
			});
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return new HashSet<Value>();
	}

	/**
	 * Return whether or not the given resource represents an rdf:List.
	 * @param theRes the resource to inspect
	 * @return true if it is an rdf:List, false otherwise
	 */
	public boolean isList(Resource theRes) {
		return theRes.equals(RDF.NIL) || getValue(theRes, RDF.FIRST) != null;
	}

	/**
	 * Add the statements to the repository
	 * @param theStatement the statement(s) to add
	 * @throws RepositoryException thrown if there is an error while adding
	 */
	public void add(Statement... theStatement) throws RepositoryException {
		addGraph(asGraph(theStatement));
	}

	/**
	 * Add the graph to the repository
	 * @param theGraph the graph to add
	 * @throws RepositoryException if there is an error while adding the graph
	 */
	public void addGraph(final Graph theGraph) throws RepositoryException {
		RepositoryConnection aConn = null;

		try {
			aConn = getConnection();

			aConn.add(theGraph);
		}
		finally {
			close(aConn);
		}
	}

	/**
	 * Remove the graph from the repository
	 * @param theGraph the graph to remove
	 * @throws RepositoryException if there is an error while removing the graph
	 */
	public void removeGraph(final Graph theGraph) throws RepositoryException {
		RepositoryConnection aConn = null;

		try {
			aConn = getConnection();

			aConn.remove(theGraph);
		}
		finally {
			close(aConn);
		}
	}

	/**
	 * Return the number of statements in this repository
	 * @return the size of the repo
	 * @throws RepositoryException if there is an error while retrieving the size.
	 */
	public long size() throws RepositoryException {
		RepositoryConnection aConn = null;

		try {
			aConn = getConnection();

			return aConn.size();
		}
		finally {
			close(aConn);
		}
	}
}
