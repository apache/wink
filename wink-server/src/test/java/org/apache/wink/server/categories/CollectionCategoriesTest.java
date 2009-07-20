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
package org.apache.wink.server.categories;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.annotations.Workspace;
import org.apache.wink.common.categories.Categories;
import org.apache.wink.common.categories.CollectionCategories;
import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.atom.AtomCategory;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.diff.DiffIgnoreUpdateWithAttributeQualifier;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class CollectionCategoriesTest extends MockServletInvocationTest {

    private String ATOM_CATEGORIES_DUCUMENT = "collection_categories_document.xml";

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {CategoriesDocResource.class, CategoriesResource.class};
    }

    @Path("/cat")
    @Workspace(workspaceTitle = "Workspace", collectionTitle = "Title")
    public static class CategoriesDocResource implements CollectionCategories {

        public List<Categories> getCategories() {

            List<Categories> catsList = new ArrayList<Categories>();
            CategoriesResource categoriesResource = new CategoriesResource();

            // Defect severity categories are defined in stand-alone Categories
            // Document
            // created by CategoriesResource
            Categories severityCategories = new Categories();
            MultivaluedMap<String, String> variables = new MultivaluedMapImpl<String, String>();
            variables.add(CategoriesResource.CategoryParamCN, CategoriesResource.SeverityCN);
            severityCategories.setHref(categoriesResource, variables);
            catsList.add(severityCategories);

            Categories severityCategoriesClass = new Categories();
            MultivaluedMap<String, String> variables4Class =
                new MultivaluedMapImpl<String, String>();
            variables4Class.add(CategoriesResource.CategoryParamCN, CategoriesResource.SeverityCN);
            severityCategoriesClass.setHref(CategoriesResource.class, variables4Class);
            catsList.add(severityCategoriesClass);

            // Build defect status categories object for ServiceDocument
            Categories statusCategories = CategoriesResource.buildStatusCategoriesDocument();
            statusCategories.setFixed(true);

            if (!statusCategories.contains("Deffered", "urn:com:hp:qadefects:categories:status")) {
                statusCategories.addCategory("urn:com:hp:qadefects:categories:status",
                                             "Deffered",
                                             "Deffered");
            }
            if (!statusCategories.contains("Approved")) {
                statusCategories.addCategory("Approved");
            }

            catsList.add(statusCategories);

            return catsList;

        }
    }

    public void testAtomCategoriesSerialization() throws Exception {

        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/",
                                                        MediaTypeUtils.ATOM_SERVICE_DOCUMENT);

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

    private String readCategoriesDocumentFromFile() throws IOException {
        // Read expected Entry from file
        InputStream is =
            CollectionCategoriesTest.class.getResourceAsStream(ATOM_CATEGORIES_DUCUMENT);
        byte[] b = new byte[4096];
        int read = is.read(b);
        String expectedSerialization = new String(b, 0, read);
        return expectedSerialization;
    }

    @Path("/categories/{category_name}")
    public static class CategoriesResource {

        public static final String StatusCN        = "status";
        public static final String SeverityCN      = "severity";
        public static final String AllCatgoriesCN  = "all";
        public static final String CategoryParamCN = "category_name";
        public static final String CategoriesURL   = "categories";

        /**
         * This method will be invoked to get Categories Document
         * 
         * @param categoryName
         * @return CategoriesDocumentResource
         */
        @GET
        @Produces(MediaTypeUtils.ATOM_CATEGORIES_DOCUMENT)
        public Categories getCategoriesDocument(@PathParam(CategoryParamCN) String categoryName) {

            Categories cats = null;
            if (categoryName.equals(SeverityCN)) {
                cats = buildSeverityCategoryDocument();
            } else if (categoryName.equals(StatusCN)) {
                cats = buildStatusCategoriesDocument();
            } else if (categoryName.equals(AllCatgoriesCN)) {
                cats = buildCompleteCategoriesDocument();
            }
            return cats;
        }

        private Categories buildCompleteCategoriesDocument() {
            Categories severityCategoryDocument = buildSeverityCategoryDocument();
            Categories statusCategoriesDocument = buildStatusCategoriesDocument();

            List<AtomCategory> categories = severityCategoryDocument.getCategories();
            List<AtomCategory> categories2 = statusCategoriesDocument.getCategories();
            List<AtomCategory> categoriesAll = new ArrayList<AtomCategory>();
            categoriesAll.addAll(categories);
            categoriesAll.addAll(categories2);
            Categories allCategories = new Categories(categoriesAll);
            allCategories.setScheme("urn:com:hp:qadefects:categories");
            return allCategories;
        }

        public static Categories buildSeverityCategoryDocument() {
            Categories cats = new Categories();
            cats.setScheme("urn:com:hp:qadefects:categories:severity");
            cats.setFixed(true);

            AtomCategory severityCritical = new AtomCategory();
            severityCritical.setLabel("critical");
            severityCritical.setScheme("urn:com:hp:qadefects:categories:severity");
            severityCritical.setTerm("1-critical");
            cats.addCategory(severityCritical);

            AtomCategory severityHigh = new AtomCategory();
            severityHigh.setLabel("high");
            severityHigh.setScheme("urn:com:hp:qadefects:categories:severity");
            severityHigh.setTerm("2-high");
            cats.addCategory(severityHigh);

            AtomCategory severityMidium = new AtomCategory();
            severityMidium.setLabel("medium");
            severityMidium.setScheme("urn:com:hp:qadefects:categories:severity");
            severityMidium.setTerm("3-medium");
            cats.addCategory(severityMidium);

            AtomCategory severityMinor = new AtomCategory();
            severityMinor.setLabel("minor");
            severityMinor.setScheme("urn:com:hp:qadefects:categories:severity");
            severityMinor.setTerm("4-minor");
            cats.addCategory(severityMinor);

            return cats;
        }

        public static Categories buildStatusCategoriesDocument() {
            Categories statusCategories = new Categories();
            statusCategories.setScheme("urn:com:hp:qadefects:categories:status");

            AtomCategory assigned = new AtomCategory();
            assigned.setLabel("Assigned");
            assigned.setScheme("urn:com:hp:qadefects:categories:status");
            assigned.setTerm("Assigned");
            statusCategories.addCategory(assigned);

            AtomCategory statusFixed = new AtomCategory();
            statusFixed.setLabel("Fixed");
            statusFixed.setScheme("urn:com:hp:qadefects:categories:status");
            statusFixed.setTerm("Fixed");
            statusCategories.addCategory(statusFixed);

            AtomCategory statusNew = new AtomCategory();
            statusNew.setLabel("New");
            statusNew.setScheme("urn:com:hp:qadefects:categories:status");
            statusNew.setTerm("New");
            statusCategories.addCategory(statusNew);

            AtomCategory statusRejected = new AtomCategory();
            statusRejected.setLabel("Rejected");
            statusRejected.setScheme("urn:com:hp:qadefects:categories:status");
            statusRejected.setTerm("Rejected");
            statusCategories.addCategory(statusRejected);

            return statusCategories;
        }
    }

}
