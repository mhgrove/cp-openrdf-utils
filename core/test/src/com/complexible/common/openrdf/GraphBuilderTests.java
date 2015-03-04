package com.complexible.common.openrdf;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;

import com.complexible.common.openrdf.util.GraphBuilder;
import org.junit.Test;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
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
		final URI aURI = ValueFactoryImpl.getInstance().createURI("urn:foo");
		GraphBuilder aBuilder = new GraphBuilder();

		final Date aDate = Calendar.getInstance().getTime();

		aBuilder.uri(aURI).addProperty(ValueFactoryImpl.getInstance().createURI("urn:bar"), aDate);

		Graph aGraph = aBuilder.graph();

		Statement aStmt = aGraph.iterator().next();

		Literal aLiteral = (Literal) aStmt.getObject();

		assertEquals(XMLSchema.DATETIME, aLiteral.getDatatype());

		final GregorianCalendar aCalendar = new GregorianCalendar();
		aCalendar.setTime(aDate);

		assertEquals(DatatypeFactory.newInstance().newXMLGregorianCalendar(aCalendar),
		             DatatypeFactory.newInstance().newXMLGregorianCalendar(aLiteral.stringValue()));
	}
}
