/*******************************************************************************
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *     
 *      http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 *******************************************************************************/
package org.apache.wink.common.model.atom;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

/* package */class XmlWrapper implements Element {

    private Object value;
    private String type;

    public XmlWrapper(Object value, String type) {
        this.value = value;
        this.type = type;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getAttribute(String name) {
        throw new UnsupportedOperationException();
    }

    public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public Attr getAttributeNode(String name) {
        throw new UnsupportedOperationException();
    }

    public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public NodeList getElementsByTagName(String name) {
        throw new UnsupportedOperationException();
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
        throws DOMException {
        throw new UnsupportedOperationException();
    }

    public TypeInfo getSchemaTypeInfo() {
        throw new UnsupportedOperationException();
    }

    public String getTagName() {
        throw new UnsupportedOperationException();
    }

    public boolean hasAttribute(String name) {
        throw new UnsupportedOperationException();
    }

    public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public void removeAttribute(String name) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public void setAttribute(String name, String value) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public void setAttributeNS(String namespaceURI, String qualifiedName, String value)
        throws DOMException {
        throw new UnsupportedOperationException();
    }

    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public void setIdAttribute(String name, boolean isId) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId)
        throws DOMException {
        throw new UnsupportedOperationException();
    }

    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public Node appendChild(Node newChild) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public Node cloneNode(boolean deep) {
        throw new UnsupportedOperationException();
    }

    public short compareDocumentPosition(Node other) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public NamedNodeMap getAttributes() {
        throw new UnsupportedOperationException();
    }

    public String getBaseURI() {
        throw new UnsupportedOperationException();
    }

    public NodeList getChildNodes() {
        throw new UnsupportedOperationException();
    }

    public Object getFeature(String feature, String version) {
        throw new UnsupportedOperationException();
    }

    public Node getFirstChild() {
        throw new UnsupportedOperationException();
    }

    public Node getLastChild() {
        throw new UnsupportedOperationException();
    }

    public String getLocalName() {
        throw new UnsupportedOperationException();
    }

    public String getNamespaceURI() {
        throw new UnsupportedOperationException();
    }

    public Node getNextSibling() {
        throw new UnsupportedOperationException();
    }

    public String getNodeName() {
        throw new UnsupportedOperationException();
    }

    public short getNodeType() {
        throw new UnsupportedOperationException();
    }

    public String getNodeValue() throws DOMException {
        throw new UnsupportedOperationException();
    }

    public Document getOwnerDocument() {
        throw new UnsupportedOperationException();
    }

    public Node getParentNode() {
        throw new UnsupportedOperationException();
    }

    public String getPrefix() {
        throw new UnsupportedOperationException();
    }

    public Node getPreviousSibling() {
        throw new UnsupportedOperationException();
    }

    public String getTextContent() throws DOMException {
        throw new UnsupportedOperationException();
    }

    public Object getUserData(String key) {
        throw new UnsupportedOperationException();
    }

    public boolean hasAttributes() {
        throw new UnsupportedOperationException();
    }

    public boolean hasChildNodes() {
        throw new UnsupportedOperationException();
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public boolean isDefaultNamespace(String namespaceURI) {
        throw new UnsupportedOperationException();
    }

    public boolean isEqualNode(Node arg) {
        throw new UnsupportedOperationException();
    }

    public boolean isSameNode(Node other) {
        throw new UnsupportedOperationException();
    }

    public boolean isSupported(String feature, String version) {
        throw new UnsupportedOperationException();
    }

    public String lookupNamespaceURI(String prefix) {
        throw new UnsupportedOperationException();
    }

    public String lookupPrefix(String namespaceURI) {
        throw new UnsupportedOperationException();
    }

    public void normalize() {
        throw new UnsupportedOperationException();
    }

    public Node removeChild(Node oldChild) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public void setNodeValue(String nodeValue) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public void setPrefix(String prefix) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public void setTextContent(String textContent) throws DOMException {
        throw new UnsupportedOperationException();
    }

    public Object setUserData(String key, Object data, UserDataHandler handler) {
        throw new UnsupportedOperationException();
    }

}
