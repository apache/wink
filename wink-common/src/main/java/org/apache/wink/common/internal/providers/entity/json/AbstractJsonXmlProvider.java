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

package org.apache.wink.common.internal.providers.entity.json;

import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.internal.providers.entity.xml.AbstractJAXBProvider;
import org.apache.wink.common.model.atom.AtomJAXBUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.json.JSONObject;
import org.json.JSONException;

public class AbstractJsonXmlProvider extends AbstractJAXBProvider {

    private static final Logger       logger =
                                                 LoggerFactory
                                                     .getLogger(AbstractJsonXmlProvider.class);

    protected static SAXParserFactory spf;
    static {
        spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setValidating(false);
        // this feature will cause all attributes, including the ones specifying
        // namespaces, to be
        // given to the handler's startElement() method during the parsing of an
        // xml
        try {
            spf.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        } catch (Exception e) {
            // shouldn't happen!
            logger.error("Error while setting SAX parser feature for JSON provider");
            throw new WebApplicationException(e);
        }
    }

    static class JsonContentHandler extends Marshaller.Listener implements ContentHandler {

        private LinkedList<JSONObject>          jsonObjects = new LinkedList<JSONObject>();
        private LinkedList<Map<String, String>> namespaces  = new LinkedList<Map<String, String>>();

        private StringBuilder                   xmlStringToParse;
        private boolean                         charactersAreXml;

        public JsonContentHandler() {
            JSONObject json = new JSONObject();
            jsonObjects.addFirst(json);
            namespaces.addFirst(new HashMap<String, String>());
            charactersAreXml = false;
        }

        public JSONObject getJsonResult() {
            JSONObject json = jsonObjects.getFirst();
            return json;
        }

        public String getJsonString() {
            JSONObject json = jsonObjects.getFirst();
            String string = null;
            try {
                string = json.toString(2);
            } catch (JSONException e) {
                string = json.toString();
            }
            return string;
        }

        // ========== Marshaller Listener

        @Override
        public void beforeMarshal(Object source) {
            if (AtomJAXBUtils.isValueActuallyXml(source)) {
                xmlStringToParse = new StringBuilder();
                charactersAreXml = true;
            }
        }

        // ========== SAX Events
        public void startElement(String uri, String localName, String name, Attributes atts)
            throws SAXException {
            try {
                JSONObject properties = new JSONObject();
                jsonObjects.addFirst(properties);

                Map<String, String> currentNamespaces = namespaces.getFirst();
                Map<String, String> declaredNamespaces =
                    new HashMap<String, String>(currentNamespaces);
                namespaces.addFirst(declaredNamespaces);

                if (atts != null) {
                    String qName = null;
                    String value = null;
                    String declaredNamespace = null;
                    int length = atts.getLength();
                    for (int i = 0; i < length; ++i) {
                        value = atts.getValue(i);
                        qName = atts.getQName(i);
                        if (qName.startsWith("xmlns:")) {
                            declaredNamespace = qName.substring("xmlns:".length());
                            declaredNamespaces.put(declaredNamespace, value);
                        } else {
                            properties.put("@" + qName, value);
                        }
                    }
                }

                // add the xmlns properties
                JSONObject namespaceProps = new JSONObject();
                if (uri != null && uri.length() > 0) {
                    namespaceProps.put("$", uri);
                }
                Set<String> nsNames = declaredNamespaces.keySet();
                for (String nsName : nsNames) {
                    namespaceProps.put(nsName, declaredNamespaces.get(nsName));
                }
                if (namespaceProps.length() > 0) {
                    properties.put("@xmlns", namespaceProps);
                }

            } catch (JSONException e) {
                logger.error("failed to convert XML to JSon");
                throw new WebApplicationException(e);
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if (charactersAreXml) {
                xmlStringToParse.append(ch, start, length);
                return;
            }

            try {
                JSONObject properties = jsonObjects.getFirst();
                String elementText = new String(ch, start, length);
                String text = elementText.trim();
                if (text.length() > 0) {
                    if (!properties.isNull("$")) {
                        text = properties.getString("$") + text;
                    }
                    properties.put("$", text);
                }
            } catch (JSONException e) {
                logger.error("failed to convert XML to JSon");
                throw new WebApplicationException(e);
            }
        }

        public void endElement(String uri, String localName, String name) throws SAXException {
            if (charactersAreXml) {
                charactersAreXml = false;
                String xmlToParse = xmlStringToParse.toString();
                AtomJAXBUtils.saxParse(new StringReader(xmlToParse),
                                       this,
                                       "failed to convert XML to JSon");
            }

            try {
                namespaces.removeFirst();
                JSONObject properties = jsonObjects.removeFirst();
                JSONObject jsonObject = jsonObjects.getFirst();
                jsonObject.accumulate(name, properties);
            } catch (JSONException e) {
                logger.error("failed to convert XML to JSon");
                throw new WebApplicationException(e);
            }
        }

        public void endPrefixMapping(String prefix) throws SAXException {
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        public void processingInstruction(String target, String data) throws SAXException {
        }

        public void setDocumentLocator(Locator locator) {
        }

        public void skippedEntity(String name) throws SAXException {
        }

        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }
    }

}
