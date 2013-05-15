// Copyright (c) 2010 - 2013 -- Clark & Parsia, LLC. <http://www.clarkparsia.com>
// For more information about licensing and copyright of this software, please contact
// inquiries@clarkparsia.com or visit http://stardog.com

package com.complexible.common.openrdf;

import org.junit.Test;
import org.openrdf.model.Graph;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;

import static org.junit.Assert.assertEquals;

/**
 * <p></p>
 *
 * @author  Michael Grove
 * @since   0.9
 * @version 0.9
 */
public class IOGraphTests {
    @Test
    public void testToString() {
        Graph aGraph = Graphs.newGraph(ValueFactoryImpl.getInstance().createStatement(ValueFactoryImpl.getInstance().createURI("urn:s"),
                                                                                      ValueFactoryImpl.getInstance().createURI("urn:p"),
                                                                                      ValueFactoryImpl.getInstance().createURI("urn:o")));

        String aStr = Graphs.extend(aGraph).toString(RDFFormat.NTRIPLES);

        assertEquals("<urn:s> <urn:p> <urn:o> .", aStr.trim());
    }
}
