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

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.NodeDetail;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Class that overrides XmlUnit DiffWithAttributeQualifier class. It is
 * designated to ignore fields with time values, like "updated".
 * <p/>
 * It is implemented to be used by Stm application tests for XML comparison.
 */
public class DiffIgnoreUpdateWithAttributeQualifier extends DiffWithAttributeQualifier {

    /**
     * Constructor of XmlUnit Diff that ignores field "updated". XQuery of
     * omitted field: /feed[1]/updated[1]/text()[1]
     * 
     * @param controlDocument XML document that contains expected results
     * @param testedDocument XML document which is being tested
     */
    public DiffIgnoreUpdateWithAttributeQualifier(Document controlDocument, Document testedDocument) {
        super(controlDocument, testedDocument);
        this.overrideDifferenceListener(new DifferenceListenerIgnoreUpdateOrCompareTimes());
    }

    /**
     * Constructor of XmlUnit Diff that ignores field "updated". XQuery of
     * omitted field: /feed[1]/updated[1]/text()[1]
     * 
     * @param controlDocument String containing XML document that contains
     *            expected results
     * @param testedDocument String containing XML document which is being
     *            tested
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public DiffIgnoreUpdateWithAttributeQualifier(String controlDocument, String testedDocument)
        throws IOException, SAXException {
        super(controlDocument, testedDocument);
        this.overrideDifferenceListener(new DifferenceListenerIgnoreUpdateOrCompareTimes());
    }

    /**
     * Implementation of difference listener that ignores element <feed><update>
     */
    private class DifferenceListenerIgnoreUpdateOrCompareTimes implements DifferenceListener {
        private Set<String>     timeNodeNameSet = new HashSet<String>();
        private DatatypeFactory datatypeFactory;

        public DifferenceListenerIgnoreUpdateOrCompareTimes() {
            try {
                datatypeFactory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException e) {
                throw new RuntimeException(e);
            }
            String[] names =
                {"updated", "published", "atom:updated", "atom:published", "stm:created",
                    "created", "stm:closed", "closed", "ns:expires"};
            for (String name : names)
                timeNodeNameSet.add(name);
        }

        public int differenceFound(Difference difference) {
            String xpath = difference.getTestNodeDetail().getXpathLocation();
            if (xpath.equals("/feed[1]/updated[1]/text()[1]")) {
                // ignore field "updated" since it contains current time
                return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }
            if (xpath.equals("/feed[1]/entry[2]/expires[1]/text()[1]")) {
                // ignore field "expires" since it contains current time
                return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }
            // compare the times if possible
            NodeDetail testDetail = difference.getTestNodeDetail();
            String testNodeName = testDetail.getNode().getParentNode().getNodeName();
            if (timeNodeNameSet.contains(testNodeName)) {
                String testDateStr = testDetail.getValue();
                String controlDateStr = difference.getControlNodeDetail().getValue();
                XMLGregorianCalendar gDateBuilderTest =
                    datatypeFactory.newXMLGregorianCalendar(testDateStr);
                XMLGregorianCalendar gDateBuilderControl =
                    datatypeFactory.newXMLGregorianCalendar(controlDateStr);
                if (gDateBuilderTest.equals(gDateBuilderControl))
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }
            return RETURN_ACCEPT_DIFFERENCE;
        }

        public void skippedComparison(Node node, Node node1) {
        }
    }

}
