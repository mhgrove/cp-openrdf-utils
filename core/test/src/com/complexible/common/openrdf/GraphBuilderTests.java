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

package com.complexible.common.openrdf;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;

import com.complexible.common.openrdf.util.ModelBuilder;
import org.junit.Test;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import static org.junit.Assert.assertEquals;

/**
 * <p></p>
 *
 * @author  Michael Grove
 * @since   3.0
 * @version 3.0
 */
public class GraphBuilderTests {
	@Test
	public void createDate() throws Exception {
		final IRI aURI = SimpleValueFactory.getInstance().createIRI("urn:foo");
		ModelBuilder aBuilder = new ModelBuilder();

		final Date aDate = Calendar.getInstance().getTime();

		aBuilder.iri(aURI).addProperty(ValueFactoryImpl.getInstance().createIRI("urn:bar"), aDate);

		Model aGraph = aBuilder.model();

		Statement aStmt = aGraph.iterator().next();

		Literal aLiteral = (Literal) aStmt.getObject();

		assertEquals(XMLSchema.DATETIME, aLiteral.getDatatype());

		final GregorianCalendar aCalendar = new GregorianCalendar();
		aCalendar.setTime(aDate);

		assertEquals(DatatypeFactory.newInstance().newXMLGregorianCalendar(aCalendar),
		             DatatypeFactory.newInstance().newXMLGregorianCalendar(aLiteral.stringValue()));
	}
}
