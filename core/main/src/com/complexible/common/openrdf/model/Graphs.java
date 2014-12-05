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

package com.complexible.common.openrdf.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

/**
 * <p>Utility methods for working with Graph objects</p>
 *
 * @author	Michael Grove
 * @since	0.4
 * @version	3.0
 */
public final class Graphs {

    private Graphs() {
        throw new AssertionError();
    }

    /**
     * Wrap the graph as an {@link ExtGraph}
     * @param theGraph  the graph
     * @return          the graph as an ExtGraph
     */
    public static ExtGraph extend(final Graph theGraph) {
		if (theGraph instanceof ExtGraph) {
            return (ExtGraph) theGraph;
        }
        else {
            return new ExtGraphImpl(theGraph);
        }
	}

	/**
	 * Return an immutable version of the specified graph
	 * @param theGraph  the graph
	 * @return          an immutable version of the graph
	 */
	public static ImmutableGraph immutable(final Graph theGraph) {
		return ImmutableGraph.of(theGraph);
	}

	/**
	 * Return the contents of the list serialized as an RDF list
	 * @param theResources	the list
	 * @return				the list as RDF
	 */
	public static Graph toList(final Resource... theResources) {
		return toList(Arrays.asList(theResources));
	}

	/**
	 * Create a Graph from the RDF in the specified file
	 *
	 * @param theFile	the file to read the RDF from
	 * @return			a new graph containing the RDF from the file
	 *
	 * @throws IOException			if there was an error reading the file
	 * @throws RDFParseException	if the file did not contain valid RDF
	 */
	public static Graph of(final File theFile) throws IOException, RDFParseException {
		return GraphIO.readGraph(theFile);
	}

	/**
	 * Create a Sesame graph from the GraphQueryResult.  The query result is always closed regardless of whether or not
	 * it was successfully transformed into a graph.
	 *
	 * @param theResult	the result of the query
	 * @return			the graph built from the result
	 *
	 * @throws org.openrdf.query.QueryEvaluationException if there was an error while creating the graph from the query result
	 */
	public static Graph newGraph(final GraphQueryResult theResult) throws QueryEvaluationException {
		Graph aGraph = new SetGraph();

		try {
			while (theResult.hasNext()) {
				aGraph.add(theResult.next());
			}
		}
		finally {
			theResult.close();
		}

		return aGraph;
	}

	/**
	 * Return the contents of the list serialized as an RDF list
	 * @param theResources	the list
	 * @return				the list as RDF
	 */
	public static Graph toList(final List<Resource> theResources) {
		Resource aCurr = ValueFactoryImpl.getInstance().createBNode();

		int i = 0;
		Graph aGraph = new SetGraph();
		for (Resource r : theResources) {
			Resource aNext = ValueFactoryImpl.getInstance().createBNode();
			aGraph.add(aCurr, RDF.FIRST, r);
			aGraph.add(aCurr, RDF.REST, ++i < theResources.size() ? aNext : RDF.NIL);
			aCurr = aNext;
		}

		return aGraph;
	}

	/**
	 * Return the {@link Statement statements} as a {@link Graph}
	 *
	 * @param theStatements	the statements that will make up the Graph
	 * @return 				a Graph containing all the provided statements
	 */
	public static Graph newGraph(final Statement... theStatements) {
		Graph aGraph = new SetGraph();

		aGraph.addAll(Arrays.asList(theStatements));

		return aGraph;
	}

	/**
	 * Return the {@link Iterator} of {@link Statement statements} as a new {@link Graph}
	 *
	 * @param theStatements	the statements that will make up the Graph
	 * @return				a Graph containing all the provided statements
	 */
	public static Graph newGraph(final Iterator<Statement> theStatements) {
		final Graph aGraph = new SetGraph();

		while (theStatements.hasNext()) {
			aGraph.add(theStatements.next());
		}

		return aGraph;
	}

	/**
	 * Return the {@link Iterable} of {@link Statement statements} as a {@link Graph}
	 *
	 * @param theStatements	the statements that will make up the Graph
	 * @return 				a Graph containing all the provided statements
	 */
	public static Graph newGraph(final Iterable<Statement> theStatements) {
		final ExtGraphImpl aGraph = new ExtGraphImpl();

		for (Statement aStmt : theStatements) {
			aGraph.add(aStmt);
		}

		return aGraph;
	}

	/**
	 * Return a new {@link #contextGraph} whose contents are the statements contained in the array.
	 *
	 * @param theStatements	the statements for the new graph
	 * @return				the new graph
	 */
	public static Graph newContextGraph(final Statement... theStatements) {
		return newContextGraph(Iterators.forArray(theStatements));
	}

	/**
	 * Return a new {@link #contextGraph} whose contents are the statements contained in the iterator.
	 *
	 * @param theStatements the statements for the new graph
	 * @return 				the new graph
	 */
	public static Graph newContextGraph(final Iterator<Statement> theStatements) {
		Graph aGraph = contextGraph();

		while (theStatements.hasNext()) {
			aGraph.add(theStatements.next());
		}

		return aGraph;
	}

	/**
	 * Return a new {@link #contextGraph} whose contents are the statements contained in the {@link Iterable}.
	 *
	 * @param theStatements	the statements for the new graph
	 * @return				the new graph
	 */
	public static Graph newContextGraph(final Iterable<Statement> theStatements) {
		return newContextGraph(theStatements.iterator());
	}

	/**
	 * Returns a copy of the provided graph where all the statements belong to the specified context.
	 * This will overwrite any existing contexts on the statements in the graph.
	 * 
	 * @param theGraph		the graph
	 * @param theResource	the context for all the statements in the graph
	 * @return 				the new graph
	 */
	public static Graph withContext(final Graph theGraph, final Resource theResource) {
		final Graph aGraph = contextGraph();

		for (Statement aStmt : theGraph) {
			if (Objects.equal(aStmt.getContext(), theResource)) {
				aGraph.add(aStmt);
			}
			else {
				aGraph.add(aStmt.getSubject(),
						   aStmt.getPredicate(),
						   aStmt.getObject(),
						   theResource);
			}
		}

		return aGraph;
	}

	/**
	 * Return a new Graph which is the union of all the provided graphs.  Be careful if you are using statements w/ a context as the equals method for Statement
	 * does not take into account context, so two statements with the same SPO, but different contexts will be considered the same statement and only one will
	 * be included in the union.  You can use {@link ContextAwareStatement} which implements equals & hashcode taking into account the context if you need to use
	 * Statements with contexts where context is considered in this way.
	 * 
	 * @param theGraphs the graphs to union
	 * @return			the union of the graphs
	 */
	public static Graph union(final Graph... theGraphs) {
		SetGraph aSetGraph = new SetGraph();

		for (Graph aGraph : theGraphs) {
			aSetGraph.addAll(aGraph);
		}

		return aSetGraph;
	}

	/**
	 * Return a new (empty) graph whose ValueFactory is an instance of {@link ContextAwareValueFactory}
	 * @return a new "context aware" graph
	 */
	public static Graph contextGraph() {
		return new DelegatingGraph(new SetGraph()) {
			@Override
			public boolean add(final Statement e) {
                // SetGraph uses the context aware value factory, so we just need to xform the statement here
                // to a statement that uses that value factory
				return super.add(e.getSubject(), e.getPredicate(), e.getObject(), e.getContext());
			}
		};
	}

	/**
	 * Create a {@link Predicate filtered} copy of the provided {@link Graph}
	 *
	 * @param theGraph		the graph to filter
	 * @param thePredicate	the predicate to use for filtering
	 * @return				the filtered graph
	 */
	public static Graph filter(final Graph theGraph, final Predicate<Statement> thePredicate) {
		final SetGraph aGraph = new SetGraph();
		for (Statement aStmt : theGraph) {
			if (thePredicate.apply(aStmt)) {
				aGraph.add(aStmt);
			}
		}
		return aGraph;
	}

    public static boolean contains(final Iterable<Statement> theGraph, final Resource theSubject, final URI thePredicate, final Value theObject, final Resource... theContexts) {
        return !Iterables.isEmpty(filter(theGraph, theSubject, thePredicate, theObject, theContexts));
    }

    public static Iterable<Statement> filter(final Iterable<Statement> theGraph, final Resource theSubject, final URI thePredicate, final Value theObject, final Resource... theContexts) {
        return Iterables.filter(theGraph, new Predicate<Statement>() {
            @Override
            public boolean apply(final Statement theStatement) {
                if (theSubject != null && !theSubject.equals(theStatement.getSubject())) {
                    return false;
                }
                if (thePredicate != null && !thePredicate.equals(theStatement.getPredicate())) {
                    return false;
                }
                if (theObject != null && !theObject.equals(theStatement.getObject())) {
                    return false;
                }

                if (theContexts == null || theContexts.length == 0) {
                    // no context specified, SPO were all equal, so this is equals as null/empty context is a wildcard
                    return true;
                }
                else {
                    Resource aContext = theStatement.getContext();

                    for (Resource aCxt : theContexts) {
                        if (aCxt == null && aContext == null) {
                            return true;
                        }
                        if (aCxt != null && aCxt.equals(aContext)) {
                            return true;
                        }
                    }

                    return false;
                }
            }
        });
    }

	/**
	 * {@link Function Transform} the contents of the {@link Graph}.  This returns a copy of the original
	 * graph with the transformation applied
	 *
	 * @param theGraph		the graph to transform
	 * @param theFunction	the function for the transform
	 * @return				the transformed graph
	 */
	public static Graph transform(final Graph theGraph, final Function<Statement, Statement> theFunction) {
		final SetGraph aGraph = new SetGraph();
		for (Statement aStmt : theGraph) {
			aGraph.add(theFunction.apply(aStmt));
		}
		return aGraph;
	}

	/**
	 * Find a {@link Statement} which satisfies the given {@link Predicate}
	 *
	 * @param theGraph		the Graph
	 * @param thePredicate	the predicate
	 * @return				{@link Optional Optionally}, the first Statement to satisfy the Predicate, or an absent Optional if none do
	 */
	public static Optional<Statement> find(final Graph theGraph, final Predicate<Statement> thePredicate) {
		for (Statement aStmt : theGraph) {
			if (thePredicate.apply(aStmt)) {
				return Optional.of(aStmt);
			}
		}
		return Optional.absent();
	}

	/**
	 * Return whether or not at least one {@link Statement} satisfies the {@link Predicate}
	 *
	 * @param theGraph		the graph
	 * @param thePredicate	the predicate
	 * @return				true if at least one Statement satisfies the Predicate, false otherwise
	 */
	public static boolean any(final Graph theGraph, final Predicate<Statement> thePredicate) {
		for (Statement aStmt : theGraph) {
			if (thePredicate.apply(aStmt)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return whether or not all {@link Statement statements} satisfy the {@link Predicate}
	 *
	 * @param theGraph		the graph
	 * @param thePredicate	the predicate
	 * @return				true if at all Statements satisfy the Predicate, false otherwise
	 */
	public static boolean all(final Graph theGraph, final Predicate<Statement> thePredicate) {
		for (Statement aStmt : theGraph) {
			if (!thePredicate.apply(aStmt)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Collect the results of the {@link Function} as it is applied to each {@link Statement}.  {@link Optional Absent}
	 * values are not collected; the provided function should never return a null value.
	 *
	 * @param theGraph		the statements
	 * @param theFunction	the function
	 * @return				the collected values
	 */
	public static <T> Collection<T> collect(final Iterable<Statement> theGraph, final Function<Statement, Optional<T>> theFunction) {
		final Set<T> aSet = Sets.newHashSet();
		for (Statement aStmt : theGraph) {
			final Optional<T> aResult = theFunction.apply(aStmt);
			if (aResult.isPresent()) {
				aSet.add(aResult.get());
			}
		}
		return aSet;
	}

	/**
	 * Collect the results of the {@link Function} as it is applied to each {@link Statement}.  {@link Optional Absent}
	 * values are not collected; the provided function should never return a null value.
	 *
	 * @param theStatementIterator		the statements
	 * @param theFunction	the function
	 * @return				the collected values
	 */
	public static <T> Collection<T> collect(final Iterator<Statement> theStatementIterator, final Function<Statement, Optional<T>> theFunction) {
		final Set<T> aSet = Sets.newHashSet();
		while (theStatementIterator.hasNext()) {
			final Statement aStmt = theStatementIterator.next();

			Optional<T> aResult = theFunction.apply(aStmt);
			if (aResult.isPresent()) {
				aSet.add(aResult.get());
			}
		}
		return aSet;
	}

	/**
	 * Return the value of the property for the given subject.  If there are multiple values, only the first value will
	 * be returned.  Use {@link GraphUtil#getObjectIterator} if you want all values for the property.
	 *
	 * @param theGraph	the graph
	 * @param theSubj	the subject
	 * @param thePred	the property of the subject whose value should be retrieved
	 *
	 * @return 			optionally, the value of the the property for the subject
	 *
	 * @see org.openrdf.model.util.GraphUtil#getOptionalObject
	 */
	public static Optional<Value> getObject(final Graph theGraph, final Resource theSubj, final URI thePred) {
		Iterator<Value> aCollection = GraphUtil.getObjectIterator(theGraph, theSubj, thePred);

		if (aCollection.hasNext()) {
			return Optional.of(aCollection.next());
		}
		else {
			return Optional.absent();
		}
	}

	/**
	 * Return the value of of the property as a Literal
	 *
	 * @param theGraph	the graph
	 * @param theSubj	the resource
	 * @param thePred	the property whose value is to be retrieved
	 * @return 			Optionally, the property value as a literal.  Value will be absent of the SP does not have an O, or the O is not a literal
	 *
	 * @see #getObject(org.openrdf.model.Graph, org.openrdf.model.Resource, org.openrdf.model.URI)
	 */
	public static Optional<Literal> getLiteral(final Graph theGraph, final Resource theSubj, final URI thePred) {
		Optional<Value> aVal = getObject(theGraph, theSubj, thePred) ;

		if (aVal.isPresent() && aVal.get() instanceof Literal) {
			return Optional.of((Literal) aVal.get());
		}
		else {
			return Optional.absent();
		}
	}

	/**
	 * Return the value of of the property as a Resource
	 *
	 * @param theGraph	the graph
	 * @param theSubj	the resource
	 * @param thePred	the property whose value is to be retrieved
	 * @return 			Optionally, the property value as a Resource.  Value will be absent of the SP does not have an O, or the O is not a Resource
	 *
	 * @see #getObject(org.openrdf.model.Graph, org.openrdf.model.Resource, org.openrdf.model.URI)
	 * @see GraphUtil#getOptionalObjectResource
	 */
	public static Optional<Resource> getResource(final Graph theGraph, final Resource theSubj, final URI thePred) {
		Optional<Value> aVal = getObject(theGraph, theSubj, thePred) ;

		if (aVal.isPresent() && aVal.get() instanceof Resource) {
			return Optional.of((Resource) aVal.get());
		}
		else {
			return Optional.absent();
		}
	}

	/**
	 * Returns the value of the property on the given resource as a boolean.
	 *
	 * @param theGraph	the graph
	 * @param theSubj	the resource
	 * @param thePred	the property
	 * @return 			Optionally, the value of the property as a boolean.  Value will be absent if the SP does not have an O,
	 * 					or that O is not a literal or not a valid boolean value
	 */
	public static Optional<Boolean> getBooleanValue(final Graph theGraph, final Resource theSubj, final URI thePred) {
		Optional<Literal> aLitOpt = getLiteral(theGraph, theSubj, thePred);

		if (!aLitOpt.isPresent()) {
			return Optional.absent();
		}

		Literal aLiteral = aLitOpt.get();

		if (((aLiteral.getDatatype() != null && aLiteral.getDatatype().equals(XMLSchema.BOOLEAN))
			 || (aLiteral.getLabel().equalsIgnoreCase("true") || aLiteral.getLabel().equalsIgnoreCase("false")))) {
			return Optional.of(Boolean.valueOf(aLiteral.getLabel()));
		}
		else {
			return Optional.absent();
		}
	}

	/**
	 * Returns whether or not the given resource is a rdf:List
	 *
	 * @param theGraph	the graph
	 * @param theRes	the resource to check
	 *
	 * @return			true if its a list, false otherwise
	 */
	public static boolean isList(final Graph theGraph, final Resource theRes) {
		Iterable<Statement> sIter = filter(theGraph, theRes, RDF.FIRST, null);

		return theRes != null && theRes.equals(RDF.NIL) || !Iterables.isEmpty(sIter);
	}

	/**
	 * Return the contents of the given list by following the rdf:first/rdf:rest structure of the list
	 * @param theGraph	the graph
	 * @param theRes	the resource which is the head of the list
	 *
	 * @return 			the contents of the list.
	 */
	public static List<Value> asList(final Graph theGraph, final Resource theRes) {
		List<Value> aList = Lists.newArrayList();

		Resource aListRes = theRes;

		while (aListRes != null) {

			Optional<Resource> aFirst = getResource(theGraph, aListRes, RDF.FIRST);
			Optional<Resource> aRest = getResource(theGraph, aListRes, RDF.REST);

			if (aFirst.isPresent()) {
				aList.add(aFirst.get());
			}

			if (aRest.or(RDF.NIL).equals(RDF.NIL)) {
				aListRes = null;
			}
			else {
				aListRes = aRest.get();
			}
		}

		return aList;
	}

	/**
	 * Return an {@link Iterable} of the types of the {@link Resource} in the specified {@link Graph}
	 *
	 * @param theGraph	the graph
	 * @param theRes	the resource
	 * @return			the asserted rdf:type's of the resource
	 */
	public static Iterable<Resource> getTypes(final Graph theGraph, final Resource theRes) {
		return collect(filter(theGraph, theRes, RDF.TYPE, null).iterator(), Statements.objectAsResource());
	}

    public static void write(final Graph theGraph, final RDFFormat theFormat, final File theFile) throws IOException {
        FileOutputStream aOut = new FileOutputStream(theFile);
        try {
            write(theGraph, theFormat, aOut);
        }
        finally {
            aOut.close();
        }
    }

    public static void write(final Graph theGraph, final RDFFormat theFormat, final OutputStream theStream) throws IOException {
        write(theGraph, theFormat, new OutputStreamWriter(theStream));
    }

    public static void write(final Graph theGraph, final RDFFormat theFormat, final Writer theWriter) throws IOException {
        GraphIO.writeGraph(theGraph, theWriter, theFormat);
    }

//    public static TupleQueryResult select(final Graph theGraph, final String theQuery) throws MalformedQueryException, QueryEvaluationException {
//        Repository aRepo = Repositories.createInMemoryRepo();
//        try {
//	        Repositories.add(aRepo, theGraph);
//            return aRepo.selectQuery(QueryLanguage.SPARQL, theQuery);
//        }
//        catch (RepositoryException e) {
//            throw new QueryEvaluationException("There was an error setting up the repository to execute the query", e);
//        }
//        finally {
//            try {
//                aRepo.shutDown();
//            }
//            catch (RepositoryException e) {
//                // can probably ignore this, its just an in mem repo anyway.
//            }
//        }
//    }
}
