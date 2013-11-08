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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.openrdf.model.Graph;
import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.Resource;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;

/**
 * <p>Default implementation of {@link ExtGraph}.</p>
 *
 * @author	Michael Grove
 * @since	0.1
 * @version	1.1
 */
final class ExtGraphImpl extends DelegatingGraph implements ExtGraph {
	public ExtGraphImpl() {
		super(new SetGraph());
	}

	public ExtGraphImpl(final Graph theGraph) {
		super(theGraph);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Optional<Literal> getLiteral(final Resource theSubj, final URI thePred) {
		return Graphs.getLiteral(this, theSubj, thePred);
	}

	/**
	 * @inheritDoc
	 */
	@Override
    public Iterable<Resource> getInstancesOf(final URI theType) {
        return GraphUtil.getSubjects(this, RDF.TYPE, theType);
    }

	/**
	 * @inheritDoc
	 */
	@Override
    public Iterable<Resource> getIndividuals() {
        return GraphUtil.getSubjects(this, RDF.TYPE, null);
    }

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean isList(final Resource theRes) {
        return Graphs.isList(this, theRes);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<Value> asList(final Resource theRes) {
       return Graphs.asList(this, theRes);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void read(final File theFile) throws IOException, RDFParseException {
		addAll(GraphIO.readGraph(theFile));
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void read(final InputStream theStream, RDFFormat theFormat) throws IOException, RDFParseException {
		addAll(GraphIO.readGraph(theStream, theFormat));
	}

	/**
	 * @inheritDoc
	 */
	@Override
    public boolean contains(final Resource theSubj, final URI thePred, final Value theObj, final Resource... theContexts) {
        return !Iterables.isEmpty(Graphs.filter(this, theSubj, thePred, theObj, theContexts));
    }

    /**
     * @inheritDoc
     */
	@Override
    public void write(OutputStream theStream, RDFFormat theFormat) throws IOException {
        write(new OutputStreamWriter(theStream), theFormat);
    }

	/**
	 * @inheritDoc
	 */
	@Override
    public void write(Writer theWriter, RDFFormat theFormat) throws IOException {
        GraphIO.writeGraph(this, theWriter, theFormat);
    }

	/**
	 * @inheritDoc
	 */
	@Override
	public String toString(final RDFFormat theFormat) {
		try {
			StringWriter aStringWriter = new StringWriter();
			write(aStringWriter, theFormat);
			return aStringWriter.toString();
		}
		catch (IOException e) {
			// this should not happen w/ a StringWriter
			throw new RuntimeException(e);
		}
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Optional<Value> getObject(final Resource theSubj, final URI thePred) {
		return Graphs.getObject(this, theSubj, thePred);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Optional<Resource> getResource(final Resource theSubj, final URI thePred) {
		return Graphs.getResource(this, theSubj, thePred);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Iterable<Resource> getTypes(final Resource theRes) {
		return Graphs.getTypes(this, theRes);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean isInstanceOf(final Resource theRes, final Resource theType) {
		return contains(theRes, RDF.TYPE, theType);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Graph filter(final Predicate<Statement> thePredicate) {
		return Graphs.filter(this, thePredicate);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Graph transform(final Function<Statement, Statement> theFunction) {
		return Graphs.transform(this, theFunction);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public Optional<Statement> find(final Predicate<Statement> thePredicate) {
		return Graphs.find(this, thePredicate);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean any(final Predicate<Statement> thePredicate) {
		return Graphs.any(this, thePredicate);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean all(final Predicate<Statement> thePredicate) {
		return Graphs.all(this, thePredicate);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public <T> Collection<T> collect(final Function<Statement, Optional<T>> theFunction) {
		return Graphs.collect(this, theFunction);
	}
}
