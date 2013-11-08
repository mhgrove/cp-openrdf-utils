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

import com.complexible.common.openrdf.model.Graphs;
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
