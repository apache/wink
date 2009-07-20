/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/

package org.apache.wink.client.utils;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Class that overrides XmlUnit Diff class. Only Elements with the same name as
 * well as the same values for all attributes qualify. The order of elements is
 * ignored. The qualifier is {@link ElementNameAndAttributeQualifier} with no
 * argument.
 * <p/>
 * It is implemented to be used by Stm application tests for XML comparison.
 */
public class DiffWithAttributeQualifier extends Diff {

    /**
     * Constructor of XmlUnit Diff with ElementNameAndAttributeQualifier
     * 
     * @param controlDocument XML document that contains expected results
     * @param testedDocument XML document which is being tested
     */
    public DiffWithAttributeQualifier(Document controlDocument, Document testedDocument) {
        super(controlDocument, testedDocument);
        this.overrideElementQualifier(new ElementNameAndAttributeQualifier());
    }

    /**
     * Constructor of XmlUnit Diff with ElementNameAndAttributeQualifier
     * 
     * @param controlDocument String containing XML document that contains
     *            expected results
     * @param testedDocument String containing XML document which is being
     *            tested
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public DiffWithAttributeQualifier(String controlDocument, String testedDocument)
        throws IOException, SAXException {
        super(controlDocument, testedDocument);
        this.overrideElementQualifier(new ElementNameAndAttributeQualifier());
    }
}
