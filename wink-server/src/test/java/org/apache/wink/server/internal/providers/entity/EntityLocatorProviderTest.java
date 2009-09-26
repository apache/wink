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
package org.apache.wink.server.internal.providers.entity;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import org.apache.wink.common.annotations.Asset;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomText;
import org.apache.wink.common.model.atom.AtomTextType;
import org.apache.wink.common.model.atom.ObjectFactory;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class EntityLocatorProviderTest extends MockServletInvocationTest {

    private static final String ENTRY =
                                          "<entry xmlns=\"http://www.w3.org/2005/Atom\">" + "<id>id</id>"
                                              + "<title type=\"text\">title</title>"
                                              + "</entry>";

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {AtomEntryResource.class};
    }

    @Path("test")
    public static class AtomEntryResource {

        @GET
        @Produces("application/atom+xml")
        public AtomEntryAsset getAsset() {
            AtomEntryAsset asset = new AtomEntryAsset();
            AtomEntry ae = new AtomEntry();
            ae.setId("id");
            ae.setTitle(new AtomText("title", AtomTextType.text));
            asset.setEntity(ae);
            return asset;

        }

        @POST
        @Produces("application/atom+xml")
        @Consumes("application/atom+xml")
        public AtomEntryAsset postAsset(AtomEntryAsset asset) {
            assertEquals("id", asset.entry.getId());
            assertEquals("title", asset.entry.getTitle().getValue());
            return asset;
        }

        @GET
        @Path("jaxbelement")
        @Produces("application/atom+xml")
        public AtomEntryJaxbAsset getAssetAsJaxbElement() {
            AtomEntryJaxbAsset asset = new AtomEntryJaxbAsset();
            AtomEntry ae = new AtomEntry();
            ae.setId("id");
            ae.setTitle(new AtomText("title", AtomTextType.text));
            ObjectFactory of = new ObjectFactory();
            asset.entry = of.createEntry(ae);
            return asset;
        }

        @POST
        @Path("jaxbelement")
        @Produces("application/atom+xml")
        public AtomEntryJaxbAsset postAssetAsJaxbElement(AtomEntryJaxbAsset asset) {
            assertEquals("id", asset.entry.getValue().getId());
            assertEquals("title", asset.entry.getValue().getTitle().getValue());
            return asset;
        }

    }

    @Asset
    public static class AtomEntryAsset {

        public AtomEntry entry;

        @Produces(MediaType.APPLICATION_ATOM_XML)
        public AtomEntry getEntity() {
            return entry;
        }

        @Consumes(MediaType.APPLICATION_ATOM_XML)
        public void setEntity(AtomEntry entity) {
            entry = entity;

        }
    }

    @Asset
    public static class AtomEntryJaxbAsset {

        public JAXBElement<AtomEntry> entry;

        @Produces(MediaType.APPLICATION_ATOM_XML)
        public JAXBElement<AtomEntry> getEntity() {
            return entry;
        }

        @Consumes(MediaType.APPLICATION_ATOM_XML)
        public void setEntity(JAXBElement<AtomEntry> entity) {
            entry = entity;
        }

    }

    public void testGetAtomEntry() throws Exception {
        getAtomEntry("/test");
    }

    public void testPostAtomEntry() throws Exception {
        postAtomEntry("/test");
    }

    public void testGetAtomEntryAsJaxbElement() throws Exception {
        getAtomEntry("/test/jaxbelement");
    }

    public void testPostAtomEntryAsJaxbElement() throws Exception {
        postAtomEntry("/test/jaxbelement");
    }

    private void getAtomEntry(String url) throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", url, "application/atom+xml");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(ENTRY, response.getContentAsString());
        assertNull(msg, msg);
    }

    private void postAtomEntry(String url) throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST", url, "application/atom+xml");
        request.setContentType("application/atom+xml");
        request.setContent(ENTRY.getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(ENTRY, response.getContentAsString());
        assertNull(msg, msg);
    }

}
