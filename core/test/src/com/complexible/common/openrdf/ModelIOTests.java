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

import com.complexible.common.openrdf.model.ModelIO;
import com.complexible.common.openrdf.model.Models2;
import org.junit.Test;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;

import static org.junit.Assert.assertEquals;

/**
 * <p></p>
 *
 * @author  Michael Grove
 * @since   0.9
 * @version 0.9
 */
public class ModelIOTests {
    @Test
    public void testToString() {
	    Model aGraph = Models2.newModel(SimpleValueFactory.getInstance()
	                                                      .createStatement(SimpleValueFactory.getInstance()
	                                                                                         .createIRI("urn:s"),
	                                                                       SimpleValueFactory.getInstance()
	                                                                                         .createIRI("urn:p"),
	                                                                       SimpleValueFactory.getInstance()
	                                                                                         .createIRI("urn:o")));

        String aStr = ModelIO.toString(aGraph, RDFFormat.NTRIPLES);

        assertEquals("<urn:s> <urn:p> <urn:o> .", aStr.trim());
    }
}
