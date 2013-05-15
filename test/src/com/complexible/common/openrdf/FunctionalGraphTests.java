// Copyright (c) 2010 - 2013 -- Clark & Parsia, LLC. <http://www.clarkparsia.com>
// For more information about licensing and copyright of this software, please contact
// inquiries@clarkparsia.com or visit http://stardog.com

package com.complexible.common.openrdf;

import java.io.File;

import org.junit.Test;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * <p></p>
 *
 * @author  Michael Grove
 * @since   0.9
 * @version 0.9
 */
public class FunctionalGraphTests {
    @Test
    public void testFilter() throws Exception {
        Graph aGraph = Graphs.of(new File("test/data/test0.ttl"));

        URI aPred = ValueFactoryImpl.getInstance().createURI("http://api.linkedin.com/v1/rdf#lastName");

        ExtGraph aExtGraph = Graphs.extend(aGraph);

        Graph aResult = aExtGraph.filter(Statements.predicateIs(aPred));

        assertEquals(70, aResult.size());

        for (Statement aStmt : aResult) {
            if (!aStmt.getPredicate().equals(aPred)) {
                fail();
            }
        }
    }
}
