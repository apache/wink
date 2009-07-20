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

package org.apache.wink.test.diff;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.examples.MultiLevelElementNameAndTextQualifier;
import org.w3c.dom.Document;

/**
 * Class that overrides XmlUnit Diff class. Order of elements is ignored.
 * Qualifier looks at the element's name, as well as the name of the first child
 * element and the text nested into that first child element. The nesting level
 * is 2. The qualifier is {@link MultiLevelElementNameAndTextQualifier} with
 * argument "2".
 * <p/>
 * It is implemented to be used by Stm application tests for XML comparison.
 */
public class DiffWithMultiLevelQualifier extends Diff {

    /**
     * Constructor of XmlUnit Diff with MultiLevelElementNameAndTextQualifier(2)
     * 
     * @param controlDocument XML document that contains expected results
     * @param testedDocument XML document which is being tested
     */
    public DiffWithMultiLevelQualifier(Document controlDocument, Document testedDocument) {
        super(controlDocument, testedDocument);
        this.overrideElementQualifier(new MultiLevelElementNameAndTextQualifier(2));
        this.overrideDifferenceListener(new DifferenceListenerIgnoreWhitespace());
    }
}
