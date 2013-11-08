/*
 * Copyright (c) 2005-2013 Clark & Parsia, LLC. <http://www.clarkparsia.com>
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

import java.io.File;

import com.complexible.common.openrdf.model.ExtGraph;
import com.complexible.common.openrdf.model.Graphs;
import com.complexible.common.openrdf.model.Statements;
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
        Graph aGraph = Graphs.of(new File("core/test/data/test0.ttl"));

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
