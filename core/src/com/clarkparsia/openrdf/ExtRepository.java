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
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.repository.util.RDFRemover;
import org.openrdf.repository.sail.SailRepository;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.Statement;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import org.openrdf.model.impl.ValueFactoryImpl;

import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.BindingSet;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.GraphQueryResult;

import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFFormat;

import org.openrdf.sail.memory.MemoryStore;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import static com.clarkparsia.openrdf.OpenRdfUtil.close;

import com.clarkparsia.openrdf.util.IterationIterator;
import com.clarkparsia.openrdf.util.IterationIterable;
import com.clarkparsia.openrdf.util.AdunaIterations;
import com.clarkparsia.openrdf.query.SesameQueryUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Function;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.google.common.collect.Iterables;

import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.CloseableIteration;

/**
 * <p>Extends the normal Sesame Repository, via RepositoryWrapper, with some additional utility functions.</p>
 *
 * @author	Michael Grove
 * @since	0.1
 * @version	0.4
 */
public class ExtRepository extends RepositoryWrapper {
	/**
	 * the logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtRepository.class);

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
	 * Clear all data from this repository
	 */
	public void clear() {
		RepositoryConnection aConn = null;

		try {
			aConn = getConnection();

			aConn.clear();
		}
		catch (RepositoryException e) {
			LOGGER.error("There was an error while cleaning the repository", e);
		}
		finally {
			close(aConn);
		}
	}

	public void add(final InputStream theInput, final RDFFormat theFormat, final String theBase) throws IOException, RDFParseException {
		OpenRdfIO.addData(this, new InputStreamReader(theInput, Charsets.UTF_8), theFormat, ValueFactoryImpl.getInstance().createURI(theBase));
	}

	public void add(InputStream theStream, RDFFormat theFormat) throws IOException, RDFParseException {
		OpenRdfIO.addData(this, theStream, theFormat);
	}

	public void add(Repository theRepo) {
		RepositoryConnection aExportConn = null;
		RepositoryConnection aConn = null;

		try {
			aExportConn = theRepo.getConnection();
			aConn = getConnection();

			RDFInserter aInsert = new RDFInserter(aConn);
			aExportConn.export(aInsert);
		}
		catch (Exception e) {
			LOGGER.error("There was an error while adding", e);
		}
		finally {
			close(aExportConn);
			close(aConn);
		}
	}

	/**
	 * Return a graph which describes the given Resource
	 * @param theResource the resource to describe
	 * @return the graph which describes the URI
	 */
	public ExtGraph describe(Resource theResource) {
		Graph aGraph = new SetGraph();

		RepositoryConnection aConn = null;

		try {
			aConn = getConnection();

			aGraph.addAll(Sets.newHashSet(new IterationIterator<Statement>(getStatements(theResource, null, null))));
		}
		catch (Exception ex) {
			LOGGER.error("There was an error doing simple describe.", ex);
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
	 * @param theContext the contexts for the statement(s)
	 * @return an Iterable over the matching statements
	 */
	public RepositoryResult<Statement> getStatements(Resource theSubj, URI thePred, Value theObj, final Resource... theContext) {
		RepositoryConnection aConn = null;
		try {
			aConn = getConnection();

			final RepositoryResult<Statement> aResult = aConn.getStatements(theSubj, thePred, theObj, true, theContext);
            return new ConnectionClosingRepositoryResult<Statement>(aConn, aResult);
		}
		catch (Exception ex) {
			OpenRdfUtil.close(aConn);

			LOGGER.error("There was an error getting statements, returning empty iteration.", ex);

			return new RepositoryResult<Statement>(emptyStatementIteration());
		}
	}

	public boolean contains(final Statement theStatement) throws RepositoryException {
		return contains(theStatement.getSubject(), theStatement.getPredicate(), theStatement.getObject(), theStatement.getContext());
	}

	public boolean contains(final Resource theSubject, final URI thePredicate, final Value theObject, final Resource... theContext) throws RepositoryException {
		RepositoryResult aResult = null;

		try {
			aResult = getStatements(theSubject, thePredicate, theObject, theContext);
			return aResult.hasNext();
		}
		finally {
			AdunaIterations.closeQuietly(aResult);
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
			return new ConnectionClosingTupleQueryResult(aConn, aConn.prepareTupleQuery(theLang, theQuery).evaluate());
		}
		catch (RepositoryException e) {
			close(aConn);
			throw e;
		}
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

	public void read(Reader theStream, RDFFormat theFormat) throws IOException, RDFParseException {
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
			LOGGER.error("There was an error getting the subjects", e);

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
	public Iterable<Resource> getSuperclasses(final Resource theRes) {
		return Iterables.transform(new IterationIterable<Statement>(new Supplier<RepositoryResult<Statement>>() { public RepositoryResult<Statement> get() { return getStatements(theRes, RDFS.SUBCLASSOF, null); } }),
								   new Function<Statement, Resource>() {public Resource apply(Statement theStmt) { return (Resource) theStmt.getObject(); } });
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

            // TODO: close result
            TupleQueryResult aTable = selectQuery(QueryLanguage.SERQL, aQuery);

            return Iterables.transform(new IterationIterable<BindingSet>(Suppliers.ofInstance(aTable)), new Function<BindingSet, Value>() {
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
	 * Add the statements to the repository
	 * @param theStatement the statement(s) to add
	 * @throws RepositoryException thrown if there is an error while adding
	 */
	public void add(Statement... theStatement) throws RepositoryException {
		add(Graphs.newGraph(theStatement));
	}

	/**
	 * Add the graph to the repository
	 * @param theGraph the graph to add
	 * @throws RepositoryException if there is an error while adding the graph
	 */
	public void add(final Graph theGraph) throws RepositoryException {
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
	public void remove(final Graph theGraph) throws RepositoryException {
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

	public void remove(final Repository theRepo) {
		RepositoryConnection aExportConn = null;
		RepositoryConnection aConn = null;

		try {
			aExportConn = theRepo.getConnection();
			aConn = getConnection();

			RDFRemover aRemover = new RDFRemover(aConn);
			aExportConn.export(aRemover);
		}
		catch (Exception e) {
			LOGGER.error("There was an error while removing", e);
		}
		finally {
			close(aExportConn);
			close(aConn);
		}
	}

	public Literal getLiteral(final URI theSubject, final URI theProperty) {
		return (Literal) getValue(theSubject, theProperty);
	}

    private static class ConnectionClosingGraphResult implements GraphQueryResult {
        private GraphQueryResult mResult;
        private RepositoryConnection mConn;

		private ConnectionClosingGraphResult(final RepositoryConnection theConn, final GraphQueryResult theResult) {
			mConn = theConn;
			mResult = theResult;
		}

		/**
         * @inheritDoc
         */
        public Map<String, String> getNamespaces() {
            return mResult.getNamespaces();
        }

        /**
         * @inheritDoc
         */
        public void close() throws QueryEvaluationException {
            mResult.close();
            try {
                mConn.close();
            }
            catch (RepositoryException e) {
                throw new QueryEvaluationException(e);
            }
        }

        /**
         * @inheritDoc
         */
        public boolean hasNext() throws QueryEvaluationException {
            return mResult.hasNext();
        }

        /**
         * @inheritDoc
         */
        public Statement next() throws QueryEvaluationException {
            return mResult.next();
        }

        /**
         * @inheritDoc
         */
        public void remove() throws QueryEvaluationException {
            mResult.next();
        }
    }

    private static class ConnectionClosingTupleQueryResult implements TupleQueryResult {
        private TupleQueryResult mResult;
        private RepositoryConnection mConn;

        private ConnectionClosingTupleQueryResult(final RepositoryConnection theConn, final TupleQueryResult theResult) {
            mResult = theResult;
            mConn = theConn;
        }

        /**
         * @inheritDoc
         */
        public List<String> getBindingNames() {
            return mResult.getBindingNames();
        }

        /**
         * @inheritDoc
         */
        public void close() throws QueryEvaluationException {
            mResult.close();
            try {
                mConn.close();
            }
            catch (RepositoryException e) {
                throw new QueryEvaluationException(e);
            }
        }

        /**
         * @inheritDoc
         */
        public boolean hasNext() throws QueryEvaluationException {
            return mResult.hasNext();
        }

        /**
         * @inheritDoc
         */
        public BindingSet next() throws QueryEvaluationException {
            return mResult.next();
        }

        /**
         * @inheritDoc
         */
        public void remove() throws QueryEvaluationException {
            mResult.remove();
        }
    }

	private static class ConnectionClosingRepositoryResult<T> extends RepositoryResult<Statement> {
		RepositoryConnection mConn;

		public ConnectionClosingRepositoryResult(final RepositoryConnection theConn, final RepositoryResult<Statement> theResult) {
			super(theResult);
			mConn = theConn;
		}

		@Override
		protected void handleClose() throws RepositoryException {
			super.handleClose();
			OpenRdfUtil.close(mConn);
		}
	}
}
