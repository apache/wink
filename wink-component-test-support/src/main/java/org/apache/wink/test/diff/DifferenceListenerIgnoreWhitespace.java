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

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

public class DifferenceListenerIgnoreWhitespace implements DifferenceListener {

    public int differenceFound(Difference difference) {
        Node controlNode = difference.getControlNodeDetail().getNode();
        Node testNode = difference.getTestNodeDetail().getNode();
        if (controlNode.getNodeType() == Node.TEXT_NODE && testNode.getNodeType() == Node.TEXT_NODE) {
            String controlText = controlNode.getNodeValue().trim();
            String testText = testNode.getNodeValue().trim();
            if (controlText.equals("") && testText.equals("")) {
                return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }
        }
        return RETURN_ACCEPT_DIFFERENCE;
    }

    public void skippedComparison(Node node, Node node1) {
    }

}
