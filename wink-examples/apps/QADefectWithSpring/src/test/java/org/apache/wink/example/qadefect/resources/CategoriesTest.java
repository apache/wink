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

package org.apache.wink.example.qadefect.resources;

import java.io.StringReader;

import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.app.AppCategories;
import org.apache.wink.common.model.app.AppCollection;
import org.apache.wink.common.model.app.AppService;
import org.apache.wink.common.model.app.AppWorkspace;
import org.apache.wink.common.model.atom.AtomCategory;
import org.apache.wink.example.qadefect.QADefectsTest;
import org.apache.wink.example.qadefect.resources.CategoriesResource;
import org.apache.wink.example.qadefect.resources.DefectsResource;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 *
 */
public class CategoriesTest extends QADefectsTest {

    public void testServiceDocument() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/",
                                                        MediaTypeUtils.ATOM_SERVICE_DOCUMENT_TYPE);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());

        String content = response.getContentAsString();
        AppService service = AppService.unmarshal(new StringReader(content));
        AppWorkspace workspace = service.getWorkspace().get(0);
        AppCollection defectsCollection = null;
        for (AppCollection collection : workspace.getCollection()) {
            String href = collection.getHref();
            if (href.endsWith(DefectsResource.URL)) {
                defectsCollection = collection;
                break;
            }
        }
        if (defectsCollection == null) {
            fail("Defects collection doesn't appear in service document");
        }
        AppCategories categoriesHref = defectsCollection.getCategories().get(0);
        assertFalse(categoriesHref.isInline());
        assertEquals("http://localhost:80/categories/severity?alt=application%2Fatomcat%2Bxml",
                     categoriesHref.getHref());

        AppCategories categoriesInline = defectsCollection.getCategories().get(1);
        AtomCategory category1 = categoriesInline.getCategory().get(0);
        assertEquals("Assigned", category1.getLabel());
        assertEquals("urn:com:hp:qadefects:categories:status", category1.getScheme());
        assertEquals("Assigned", category1.getTerm());
        AtomCategory category2 = categoriesInline.getCategory().get(1);
        assertEquals("Fixed", category2.getLabel());
        assertEquals("urn:com:hp:qadefects:categories:status", category2.getScheme());
        assertEquals("Fixed", category2.getTerm());
        AtomCategory category3 = categoriesInline.getCategory().get(2);
        assertEquals("New", category3.getLabel());
        assertEquals("urn:com:hp:qadefects:categories:status", category3.getScheme());
        assertEquals("New", category3.getTerm());
        AtomCategory category4 = categoriesInline.getCategory().get(3);
        assertEquals("Rejected", category4.getLabel());
        assertEquals("urn:com:hp:qadefects:categories:status", category4.getScheme());
        assertEquals("Rejected", category4.getTerm());
    }

    public void testAltLink() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor
                .constructMockRequest("GET", CategoriesResource.CategoriesURL + "/"
                    + CategoriesResource.SeverityCN, MediaTypeUtils.ATOM_CATEGORIES_DOCUMENT_TYPE);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        AppCategories categories =
            AppCategories.unmarshal(new StringReader(response.getContentAsString()));
        assertEquals("urn:com:hp:qadefects:categories:severity", categories.getScheme());
        AtomCategory category1 = categories.getCategory().get(0);
        assertEquals("critical", category1.getLabel());
        assertEquals("urn:com:hp:qadefects:categories:severity", category1.getScheme());
        assertEquals("1-critical", category1.getTerm());
        AtomCategory category2 = categories.getCategory().get(1);
        assertEquals("high", category2.getLabel());
        assertEquals("urn:com:hp:qadefects:categories:severity", category2.getScheme());
        assertEquals("2-high", category2.getTerm());
        AtomCategory category3 = categories.getCategory().get(2);
        assertEquals("medium", category3.getLabel());
        assertEquals("urn:com:hp:qadefects:categories:severity", category3.getScheme());
        assertEquals("3-medium", category3.getTerm());
        AtomCategory category4 = categories.getCategory().get(3);
        assertEquals("minor", category4.getLabel());
        assertEquals("urn:com:hp:qadefects:categories:severity", category4.getScheme());
        assertEquals("4-minor", category4.getTerm());
    }

}
