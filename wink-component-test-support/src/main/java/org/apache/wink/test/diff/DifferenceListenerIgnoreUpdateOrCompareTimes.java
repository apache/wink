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

import java.util.HashSet;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.NodeDetail;
import org.w3c.dom.Node;

/**
 * Implementation of difference listener that ignores element <feed><update>
 */
public class DifferenceListenerIgnoreUpdateOrCompareTimes extends
    DifferenceListenerIgnoreWhitespace {
    private Set<String> timeNodeNameSet = new HashSet<String>();

    public DifferenceListenerIgnoreUpdateOrCompareTimes() {
        String[] names =
            {"updated", "published", "atom:updated", "atom:published", "stm:created", "created",
                "stm:closed", "closed"};
        for (String name : names)
            timeNodeNameSet.add(name);
    }

    public int differenceFound(Difference difference) {

        int result = super.differenceFound(difference);
        if (result != RETURN_ACCEPT_DIFFERENCE) {
            return result;
        }

        String xpath = difference.getTestNodeDetail().getXpathLocation();
        if (xpath.equals("/feed[1]/updated[1]/text()[1]")) {
            // ignore field "updated" since it contains current time
            return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
        }

        // compare the times if possible
        NodeDetail testDetail = difference.getTestNodeDetail();

        if (testDetail.getNode().getParentNode() != null) {
            String testNodeName = testDetail.getNode().getParentNode().getNodeName();
            if (timeNodeNameSet.contains(testNodeName)) {
                String testDateStr = testDetail.getValue();
                String controlDateStr = difference.getControlNodeDetail().getValue();
                DatatypeFactory datatypeFactory;
                try {
                    datatypeFactory = DatatypeFactory.newInstance();
                } catch (DatatypeConfigurationException e) {
                    throw new RuntimeException(e);
                }
                XMLGregorianCalendar test = datatypeFactory.newXMLGregorianCalendar(testDateStr);
                XMLGregorianCalendar control =
                    datatypeFactory.newXMLGregorianCalendar(controlDateStr);
                if (test.compare(control) == DatatypeConstants.EQUAL)
                    return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }
        }
        return RETURN_ACCEPT_DIFFERENCE;
    }

    public void skippedComparison(Node node, Node node1) {
    }
}
