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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

public class SAXHandlerWrapper extends DefaultHandler {
    private ContentHandler contentHandler;
    private LexicalHandler lexicalHandler;

    public SAXHandlerWrapper(ContentHandler handler) {
        this.contentHandler = handler;
        if (handler instanceof LexicalHandler) {
            lexicalHandler = (LexicalHandler)handler;
        }
    }

    public final ContentHandler getHandler() {
        return contentHandler;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (contentHandler == null)
            return;
        contentHandler.characters(ch, start, length);
    }

    public void endDocument() throws SAXException {
        if (contentHandler == null)
            return;
        contentHandler.endDocument();
    }

    public void endElement(String uri, String localName, String name) throws SAXException {
        if (contentHandler == null)
            return;
        contentHandler.endElement(uri, localName, name);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        if (contentHandler == null)
            return;
        contentHandler.endPrefixMapping(prefix);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (contentHandler == null)
            return;
        contentHandler.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        if (contentHandler == null)
            return;
        contentHandler.processingInstruction(target, data);
    }

    public void setDocumentLocator(Locator locator) {
        if (contentHandler == null)
            return;
        contentHandler.setDocumentLocator(locator);
    }

    public void skippedEntity(String name) throws SAXException {
        if (contentHandler == null)
            return;
        contentHandler.skippedEntity(name);
    }

    public void startDocument() throws SAXException {
        if (contentHandler == null)
            return;
        contentHandler.startDocument();
    }

    public void startElement(String uri, String localName, String name, Attributes atts)
        throws SAXException {
        if (contentHandler == null)
            return;
        contentHandler.startElement(uri, localName, name, atts);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (contentHandler == null)
            return;
        contentHandler.startPrefixMapping(prefix, uri);
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        if (lexicalHandler == null)
            return;
        lexicalHandler.comment(ch, start, length);
    }

    public void endCDATA() throws SAXException {
        if (lexicalHandler == null)
            return;
        lexicalHandler.endCDATA();
    }

    public void endDTD() throws SAXException {
        if (lexicalHandler == null)
            return;
        lexicalHandler.endDTD();
    }

    public void endEntity(String name) throws SAXException {
        if (lexicalHandler == null)
            return;
        lexicalHandler.endEntity(name);
    }

    public void startCDATA() throws SAXException {
        if (lexicalHandler == null)
            return;
        lexicalHandler.startCDATA();
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        if (lexicalHandler == null)
            return;
        lexicalHandler.startDTD(name, publicId, systemId);
    }

    public void startEntity(String name) throws SAXException {
        if (lexicalHandler == null)
            return;
        lexicalHandler.startEntity(name);
    }

}
