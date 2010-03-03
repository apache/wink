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

package org.apache.wink.common.model.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.wink.common.RestException;
import org.apache.wink.common.internal.model.ModelUtils;
import org.apache.wink.common.internal.utils.JAXBUtils;
import org.apache.wink.common.model.atom.AtomCategory;
import org.apache.wink.common.model.atom.AtomText;
import org.apache.wink.test.mock.TestUtils;

public class AppTest extends TestCase {

    private static final String SERVICE_DOCUMENT =
                                                     "<service xml:lang=\"en-us\" xml:base=\"http://base/service\" anyAttirbService=\"anyAttribValueService\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns=\"http://www.w3.org/2007/app\">\n" + "    <workspace xml:lang=\"en-us\" xml:base=\"http://base/workspace\" anyAttirbWorkspace=\"anyAttribValueWorkspace\">\n"
                                                         + "        <atom:title type=\"text\">workspace1</atom:title>\n"
                                                         + "        <collection href=\"href1\" xml:lang=\"en-us\" xml:base=\"http://base/collection\" anyAttirbCollection=\"anyAttribValueCollection\">\n"
                                                         + "            <atom:title type=\"text\">collection1</atom:title>\n"
                                                         + "            <accept>accept1</accept>\n"
                                                         + "            <accept>accept2</accept>\n"
                                                         + "            <categories scheme=\"scheme\" fixed=\"no\">\n"
                                                         + "                <atom:category label=\"label\" scheme=\"scheme1\" term=\"term1\"/>\n"
                                                         + "                <atom:category label=\"labe2\" scheme=\"scheme2\" term=\"term2\"/>\n"
                                                         + "            </categories>\n"
                                                         + "            <categories href=\"href\"/>\n"
                                                         + "        </collection>\n"
                                                         + "        <collection href=\"href2\" xml:lang=\"en-us\" xml:base=\"http://base/collection\" anyAttirbCollection=\"anyAttribValueCollection\">\n"
                                                         + "            <atom:title type=\"text\">collection2</atom:title>\n"
                                                         + "            <accept>accept1</accept>\n"
                                                         + "            <accept>accept2</accept>\n"
                                                         + "            <categories scheme=\"scheme\" fixed=\"no\">\n"
                                                         + "                <atom:category label=\"label\" scheme=\"scheme1\" term=\"term1\"/>\n"
                                                         + "                <atom:category label=\"labe2\" scheme=\"scheme2\" term=\"term2\"/>\n"
                                                         + "            </categories>\n"
                                                         + "            <categories href=\"href\"/>\n"
                                                         + "        </collection>\n"
                                                         + "    </workspace>\n"
                                                         + "    <workspace xml:lang=\"en-us\" xml:base=\"http://base/workspace\" anyAttirbWorkspace=\"anyAttribValueWorkspace\">\n"
                                                         + "        <atom:title type=\"text\">workspace2</atom:title>\n"
                                                         + "        <collection href=\"href1\" xml:lang=\"en-us\" xml:base=\"http://base/collection\" anyAttirbCollection=\"anyAttribValueCollection\">\n"
                                                         + "            <atom:title type=\"text\">collection1</atom:title>\n"
                                                         + "            <accept>accept1</accept>\n"
                                                         + "            <accept>accept2</accept>\n"
                                                         + "            <categories scheme=\"scheme\" fixed=\"no\">\n"
                                                         + "                <atom:category label=\"label\" scheme=\"scheme1\" term=\"term1\"/>\n"
                                                         + "                <atom:category label=\"labe2\" scheme=\"scheme2\" term=\"term2\"/>\n"
                                                         + "            </categories>\n"
                                                         + "            <categories href=\"href\"/>\n"
                                                         + "        </collection>\n"
                                                         + "        <collection href=\"href2\" xml:lang=\"en-us\" xml:base=\"http://base/collection\" anyAttirbCollection=\"anyAttribValueCollection\">\n"
                                                         + "            <atom:title type=\"text\">collection2</atom:title>\n"
                                                         + "            <accept>accept1</accept>\n"
                                                         + "            <accept>accept2</accept>\n"
                                                         + "            <categories scheme=\"scheme\" fixed=\"no\">\n"
                                                         + "                <atom:category label=\"label\" scheme=\"scheme1\" term=\"term1\"/>\n"
                                                         + "                <atom:category label=\"labe2\" scheme=\"scheme2\" term=\"term2\"/>\n"
                                                         + "            </categories>\n"
                                                         + "            <categories href=\"href\"/>\n"
                                                         + "        </collection>\n"
                                                         + "    </workspace>\n"
                                                         + "</service>\n";

    private static JAXBContext  ctx;

    static {
        try {
            ctx = JAXBContext.newInstance(AppService.class.getPackage().getName());
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public void testAppMarshal() throws Exception {
        Marshaller m = JAXBUtils.createMarshaller(ctx);

        AppService service = getService();
        JAXBElement<AppService> element = (new ObjectFactory()).createService(service);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ModelUtils.marshal(m, element, os);
        String msg = TestUtils.diffIgnoreUpdateWithAttributeQualifier(SERVICE_DOCUMENT, os.toString());
        assertNull(msg, msg);
    }

    public void testAppUnmarshal() throws IOException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = ModelUtils.unmarshal(u, new StringReader(SERVICE_DOCUMENT));
        assertNotNull(element);
        assertTrue(element instanceof AppService);

        AppService service = (AppService)element;
        AppService expectedService = getService();

        assertService(expectedService, service);
    }

    public void testAppUnmarshalMarshal() throws Exception {
        Marshaller m = JAXBUtils.createMarshaller(ctx);
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);

        Object service = ModelUtils.unmarshal(u, new StringReader(SERVICE_DOCUMENT));
        JAXBElement<AppService> element = (new ObjectFactory()).createService((AppService)service);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ModelUtils.marshal(m, element, os);
        String msg = TestUtils.diffIgnoreUpdateWithAttributeQualifier(SERVICE_DOCUMENT, os.toString());
        assertNull(msg, msg);
    }

    public void testCategories() {
        AppCategories cats = new AppCategories();
        cats.setHref("href");
        try {
            cats.setScheme("scheme");
        } catch (RestException e) {
            assertEquals("cannot mix inline and out-of-line categories attributes", e.getMessage());
        }

        cats = new AppCategories();
        cats.setHref("href");
        try {
            cats.setFixed(AppYesNo.YES);
        } catch (RestException e) {
            assertEquals("cannot mix inline and out-of-line categories attributes", e.getMessage());
        }

        cats = new AppCategories();
        cats.setScheme("scheme");
        cats.setFixed(AppYesNo.YES);
        try {
            cats.setHref("scheme");
        } catch (RestException e) {
            assertEquals("cannot mix inline and out-of-line categories attributes", e.getMessage());
        }

        cats = new AppCategories();
        cats.setHref("href");
        assertFalse(cats.isInline());

        cats = new AppCategories();
        assertFalse(cats.isFixedSet());
        cats.setFixed(AppYesNo.YES);
        assertTrue(cats.isFixedSet());
    }

    private void assertService(AppService expectedService, AppService service) {
        assertEquals(expectedService.getBase(), service.getBase());
        assertEquals(expectedService.getLang(), service.getLang());
        assertEquals(expectedService.getOtherAttributes().get(new QName("anyAttirbService")),
                     service.getOtherAttributes().get(new QName("anyAttirbService")));

        assertNotNull(service.getWorkspace("workspace1"));
        assertNotNull(service.getWorkspace("workspace2"));
        assertNull(service.getWorkspace("workspace3"));

        List<AppWorkspace> expectedWorkspaces = expectedService.getWorkspace();
        List<AppWorkspace> workspaces = service.getWorkspace();
        assertTrue(workspaces.size() == 2);

        assertWorkspaces(expectedWorkspaces, workspaces);
    }

    private void assertWorkspaces(List<AppWorkspace> expectedWorkspaces,
                                  List<AppWorkspace> workspaces) {
        assertEquals(expectedWorkspaces.size(), workspaces.size());
        for (int i = 0; i < expectedWorkspaces.size(); ++i) {
            AppWorkspace expectedWorkspace = expectedWorkspaces.get(i);
            AppWorkspace workspace = workspaces.get(i);

            assertEquals(expectedWorkspace.getLang(), workspace.getLang());
            assertEquals(expectedWorkspace.getBase(), workspace.getBase());
            assertEquals(expectedWorkspace.getTitle().getType(), workspace.getTitle().getType());
            assertEquals(expectedWorkspace.getTitle().getValue(), workspace.getTitle().getValue());
            assertEquals(expectedWorkspace.getOtherAttributes()
                .get(new QName("anyAttirbCollection")), workspace.getOtherAttributes()
                .get(new QName("anyAttirbCollection")));

            assertNotNull(workspace.getCollection("collection1"));
            assertNotNull(workspace.getCollection("collection2"));
            assertNull(workspace.getCollection("collection3"));

            assertCollections(expectedWorkspace.getCollection(), workspace.getCollection());
        }

    }

    private void assertCollections(List<AppCollection> expectedcollections,
                                   List<AppCollection> collections) {
        assertEquals(expectedcollections.size(), collections.size());
        for (int i = 0; i < expectedcollections.size(); ++i) {
            AppCollection expectedCollection = expectedcollections.get(i);
            AppCollection collection = collections.get(i);

            assertEquals(expectedCollection.getLang(), collection.getLang());
            assertEquals(expectedCollection.getBase(), collection.getBase());
            assertEquals(expectedCollection.getHref(), collection.getHref());
            assertEquals(expectedCollection.getTitle().getType(), collection.getTitle().getType());
            assertEquals(expectedCollection.getTitle().getValue(), collection.getTitle().getValue());
            assertEquals(expectedCollection.getOtherAttributes()
                .get(new QName("anyAttirbCollection")), collection.getOtherAttributes()
                .get(new QName("anyAttirbCollection")));

            List<AppAccept> expectedAccepts = expectedCollection.getAccept();
            List<AppAccept> accepts = collection.getAccept();
            assertEquals(expectedAccepts.size(), accepts.size());
            assertEquals(expectedAccepts.get(0).getValue(), accepts.get(0).getValue());
            assertEquals(expectedAccepts.get(1).getValue(), accepts.get(1).getValue());

            List<AppCategories> expectedCategories = expectedCollection.getCategories();
            List<AppCategories> categories = collection.getCategories();
            assertEquals(expectedCategories.size(), categories.size());

            assertNull(categories.get(0).getHref());
            assertEquals(expectedCategories.get(0).getScheme(), categories.get(0).getScheme());
            assertEquals(expectedCategories.get(0).getFixed(), categories.get(0).getFixed());
            assertEquals(expectedCategories.get(0).getCategory().get(0).getScheme(), categories
                .get(0).getCategory().get(0).getScheme());
            assertEquals(expectedCategories.get(0).getCategory().get(1).getScheme(), categories
                .get(0).getCategory().get(1).getScheme());

            assertNull(categories.get(1).getScheme());
            assertEquals(expectedCategories.get(1).getHref(), categories.get(1).getHref());
        }
    }

    private AppService getService() {
        AppService service = new AppService();
        service.setLang("en-us");
        service.setBase("http://base/service");
        service.getOtherAttributes().put(new QName("anyAttirbService"), "anyAttribValueService");

        service.getWorkspace().add(getWorkspace("1"));
        service.getWorkspace().add(getWorkspace("2"));

        service.getWorkspace();
        return service;
    }

    private AppWorkspace getWorkspace(String id) {
        AppWorkspace ws = new AppWorkspace();
        ws.setLang("en-us");
        ws.setBase("http://base/workspace");
        ws.setTitle(new AtomText("workspace" + id));
        ws.getOtherAttributes().put(new QName("anyAttirbWorkspace"), "anyAttribValueWorkspace");

        ws.getCollection().add(getCollection("1"));
        ws.getCollection().add(getCollection("2"));

        return ws;
    }

    private AppCollection getCollection(String id) {
        AppCollection col = new AppCollection();
        col.setLang("en-us");
        col.setBase("http://base/collection");
        col.setTitle(new AtomText("collection" + id));
        col.getOtherAttributes().put(new QName("anyAttirbCollection"), "anyAttribValueCollection");
        col.setHref("href" + id);

        AppAccept accept1 = new AppAccept();
        accept1.setValue("accept1");
        AppAccept accept2 = new AppAccept();
        accept2.setValue("accept2");
        col.getAccept().add(accept1);
        col.getAccept().add(accept2);

        col.getCategories().add(getInlineCategories());
        col.getCategories().add(getOutOfLineCategories());

        return col;
    }

    private AppCategories getInlineCategories() {
        AppCategories cats = new AppCategories();
        cats.setScheme("scheme");
        cats.setFixed(AppYesNo.NO);

        AtomCategory cat = new AtomCategory();
        cat.setScheme("scheme1");
        cat.setLabel("label");
        cat.setTerm("term1");
        cats.getCategory().add(cat);

        cat = new AtomCategory();
        cat.setScheme("scheme2");
        cat.setLabel("labe2");
        cat.setTerm("term2");
        cats.getCategory().add(cat);
        return cats;
    }

    private AppCategories getOutOfLineCategories() {
        AppCategories cats = new AppCategories();
        cats.setHref("href");
        return cats;
    }

}
