/*
 * Copyright (c) 2009-2015 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.complexible.common.openrdf.util.AdunaIterations;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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
 * @version	4.0
 */
public final class Graphs {

    private Graphs() {
        throw new AssertionError();
    }

	public static Collector<Statement, Graph, Graph> toGraph() {
		return new Collector<Statement, Graph, Graph>() {
			@Override
			public Supplier<Graph> supplier() {
				return Graphs::newGraph;
			}

			@Override
			public BiConsumer<Graph, Statement> accumulator() {
				return Graph::add;
			}

			@Override
			public BinaryOperator<Graph> combiner() {
				return (theGraph, theOtherGraph) -> {
					theGraph.addAll(theOtherGraph);
					return theGraph;
				};
			}

			@Override
			public Function<Graph, Graph> finisher() {
				return Function.identity();
			}

			@Override
			public Set<Characteristics> characteristics() {
				return Sets.newHashSet(Characteristics.IDENTITY_FINISH, Characteristics.UNORDERED);
			}
		};
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
	public static Graph of(final Path theFile) throws IOException, RDFParseException {
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

		try (Stream<Statement> aResults = AdunaIterations.stream(theResult)) {
			aResults.forEach(aGraph::add);
		}

		return aGraph;
	}

	/**
	 * Return the contents of the list serialized as an RDF list
	 * @param theResources	the list
	 * @return				the list as RDF
	 */
	public static Graph toList(final List<Resource> theResources) {
		Resource aCurr = ContextAwareValueFactory.getInstance().createBNode();

		int i = 0;
		Graph aGraph = new SetGraph();
		for (Resource aRes : theResources) {
			Resource aNext = ContextAwareValueFactory.getInstance().createBNode();
			aGraph.add(aCurr, RDF.FIRST, aRes);
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
		final SetGraph aGraph = new SetGraph();

		for (Statement aStmt : theStatements) {
			aGraph.add(aStmt);
		}

		return aGraph;
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
		final Graph aGraph = newGraph();

		theGraph.stream()
		        .map(Statements.applyContext(theResource, theGraph.getValueFactory()))
		        .forEach(aGraph::add);

		return aGraph;
	}

	/**
	 * Return a new Graph which is the union of all the provided graphs.  Be careful if you are using statements w/ a
	 * context as the equals method for Statement does not take into account context, so two statements with the same
	 * SPO, but different contexts will be considered the same statement and only one will be included in the union.
	 * You can use {@link ContextAwareStatement} which implements equals & hashcode taking into account the context
	 * if you need to use Statements with contexts where context is considered in this way.
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

    public static boolean contains(final Iterable<Statement> theGraph, final Resource theSubject,
                                   final URI thePredicate, final Value theObject, final Resource... theContexts) {

	    return (theGraph instanceof Graph ? ((Graph) theGraph).stream() : StreamSupport.stream(theGraph.spliterator(), false))
		           .filter(Statements.matches(theSubject, thePredicate, theObject, theContexts))
		           .findFirst()
		           .isPresent();
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
			return Optional.empty();
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
			return Optional.empty();
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
			return Optional.empty();
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
			return Optional.empty();
		}

		Literal aLiteral = aLitOpt.get();

		if (((aLiteral.getDatatype() != null && aLiteral.getDatatype().equals(XMLSchema.BOOLEAN))
			 || (aLiteral.getLabel().equalsIgnoreCase("true") || aLiteral.getLabel().equalsIgnoreCase("false")))) {
			return Optional.of(Boolean.valueOf(aLiteral.getLabel()));
		}
		else {
			return Optional.empty();
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
		return theRes != null && (theRes.equals(RDF.NIL) || theGraph.stream().filter(Statements.matches(theRes, RDF.FIRST, null)).findFirst().isPresent());
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

			if (aRest.orElse(RDF.NIL).equals(RDF.NIL)) {
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
		return theGraph.stream()
		               .filter(Statements.matches(theRes, RDF.TYPE, null))
		               .map(Statement::getObject)
		               .map(theObject -> (Resource) theObject)
		               .collect(Collectors.toList());
	}

	public static boolean isInstanceOf(final Graph theGraph, final Resource theSubject, final Resource theType) {
		return theGraph.contains(ContextAwareValueFactory.getInstance().createStatement(theSubject, RDF.TYPE, theType));
	}

    public static void write(final Graph theGraph, final RDFFormat theFormat, final File theFile) throws IOException {
	    try (FileOutputStream aOut = new FileOutputStream(theFile)) {
		    write(theGraph, theFormat, aOut);
	    }
    }

    public static void write(final Graph theGraph, final RDFFormat theFormat, final OutputStream theStream) throws IOException {
        write(theGraph, theFormat, new OutputStreamWriter(theStream));
    }

    public static void write(final Graph theGraph, final RDFFormat theFormat, final Writer theWriter) throws IOException {
        GraphIO.writeGraph(theGraph, theWriter, theFormat);
    }

	public static Set<Resource> individuals(final Graph theGraph) {
		return GraphUtil.getSubjects(theGraph, RDF.TYPE, null);
	}

	public static Set<Resource> instancesOf(final Graph theGraph, final URI theType) {
		return theGraph.stream()
		               .filter(Statements.matches(null, RDF.TYPE, theType))
		               .map(Statement::getSubject)
		               .collect(Collectors.toSet());
	}
}
