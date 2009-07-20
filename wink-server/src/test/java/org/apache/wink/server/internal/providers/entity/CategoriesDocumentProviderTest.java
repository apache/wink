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

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.wink.common.categories.Categories;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.atom.AtomCategory;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.diff.DiffIgnoreUpdateWithAttributeQualifier;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class CategoriesDocumentProviderTest extends MockServletInvocationTest {

    private String ATOM_CATEGORIES_DUCUMENT = "atom_categories_document.xml";

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {CategoriesResource.class};
    }

    @Path("/")
    public static class CategoriesResource {

        @GET
        @Produces(MediaTypeUtils.ATOM_CATEGORIES_DOCUMENT)
        public Categories getCategories() {
            return buildCategories();
        }
    }

    public void testAtomCategoriesSerialization() throws Exception {

        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/",
                                                        MediaTypeUtils.ATOM_CATEGORIES_DOCUMENT);

        MockHttpServletResponse response = invoke(mockRequest);
        String content = response.getContentAsString();

        String expectedSerialization = null;
        try {
            expectedSerialization = readCategoriesDocumentFromFile();
        } catch (IOException e) {
            fail("Failed to read " + ATOM_CATEGORIES_DUCUMENT);
        }

        DiffIgnoreUpdateWithAttributeQualifier diff;
        try {
            diff = new DiffIgnoreUpdateWithAttributeQualifier(expectedSerialization, content);
        } catch (Exception e) {
            fail("Failed to perform diff");
            throw e;
        }

        assertTrue("Expected atom feed documents to be similar" + " "
            + diff.toString()
            + "\nexpected:\n"
            + expectedSerialization
            + "\nresult:\n"
            + content, diff.similar());
    }

    private static Categories buildCategories() {
        // Create CategoriesDocumentResource with Categories data
        Categories cats = new Categories();
        cats.setScheme("urn:org.apache.wink.example.default.scheme");
        cats.setFixed(true);
        // Get category listing for somewhere
        for (int i = 0; i < 4; i++) {
            AtomCategory cb = new AtomCategory();
            cb.setLabel("label" + i);
            cb.setScheme("urn:org.apache.wink.scheme" + i);
            cb.setTerm("term" + i);
            cats.addCategory(cb);
        }
        return cats;
    }

    private String readCategoriesDocumentFromFile() throws IOException {
        // Read expected Entry from file
        InputStream is =
            CategoriesDocumentProviderTest.class.getResourceAsStream(ATOM_CATEGORIES_DUCUMENT);
        byte[] b = new byte[4096];
        int read = is.read(b);
        String expectedSerialization = new String(b, 0, read);
        return expectedSerialization;
    }

}
