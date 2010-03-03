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

package org.apache.wink.common.internal.utils;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.CharacterData;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class DomHelper {

    private static final String XMLNS_PREFIX              = "xmlns"; //$NON-NLS-1$
    private static final String XMLNS_PREFIX_COLON        = XMLNS_PREFIX + ':';
    private static final int    XMLNS_PREFIX_COLON_LENGTH = XMLNS_PREFIX_COLON.length();

    private DomHelper() {
    } // no instances

    /**
     * Returns the QName of the element.
     * 
     * @param element the element which name is to be returned
     * @return the QName of the element
     */
    public static QName getElementQName(Element element) {

        if (element.getPrefix() != null) {
            return new QName(element.getNamespaceURI(), element.getLocalName(), element.getPrefix());
        } else {
            return new QName(element.getNamespaceURI(), element.getLocalName());
        }
    }

    /**
     * Returns the name of the element.
     * 
     * @param element the element which name is to be returned
     * @return the name of the element
     */
    public static String getElementName(Element element) {

        if (element.getPrefix() != null && element.getPrefix().length() > 0) {
            return element.getPrefix() + ':' + element.getLocalName();
        } else {
            return element.getLocalName();
        }
    }

    /**
     * Returns the text content of the element. Concatenates all text nodes into
     * a single string.
     * 
     * @param element the element which content is to be returned
     * @return the text content of the element or empty string if the element
     *         has no text content
     */
    public static String getElementText(Element element) {

        StringBuilder builder = new StringBuilder(10);
        for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Text) {
                builder.append(((CharacterData)n).getData());
            }
        }
        return builder.toString();
    }

    /**
     * Get attributes without namespace declarations of specified element.
     * 
     * @param element element to get namespaces from
     * @return map of attributes (name -> value)
     */
    public static Map<String, String> getAttributes(Element element) {

        Map<String, String> ret = new HashMap<String, String>();
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {

            Attr attr = (Attr)attrs.item(i);
            String attrName = attr.getName();
            if (!attrName.startsWith(XMLNS_PREFIX_COLON) && !attrName.equals(XMLNS_PREFIX)) {
                ret.put(attrName, attr.getValue());
            }
        }
        return ret;
    }

    /**
     * Get namespace declarations of specified element. Any 'inherited'
     * declarations are NOT present.
     * 
     * @param element element to get namespaces from
     * @param parentPrefixes parent prefixes (the default values)
     * @return map of namespaces (prefix -> namespace)
     */
    public static Map<String, String> getDeclaredPrefixes(Element element,
                                                          Map<String, String> parentPrefixes) {

        Map<String, String> ret = new TreeMap<String, String>(parentPrefixes);

        // default namespace
        if (element.getNamespaceURI() != null) {
            ret.put("", element.getNamespaceURI()); //$NON-NLS-1$
        }

        // other namespaces
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {

            Attr attr = (Attr)attrs.item(i);
            String attrName = attr.getName();
            if (attrName.startsWith(XMLNS_PREFIX_COLON)) {
                ret.put(attrName.substring(XMLNS_PREFIX_COLON_LENGTH), attr.getValue());
            }
            // if(attrName.equals(XMLNS_PREFIX)) {
            // // default namespace
            // ret.put("", attr.getValue());
            // }
        }
        return ret;
    }

    /**
     * Gets all child elements.
     * 
     * @param node the node whose children should be returned
     * @return list of elements or empty list if no subelements found
     */
    public static List<Element> getChildElements(Node node) {

        ArrayList<Element> ret = new ArrayList<Element>(5);
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {

            if (child.getNodeType() == Node.ELEMENT_NODE) {
                ret.add((Element)child);
            }
        }
        return ret;
    }
}
