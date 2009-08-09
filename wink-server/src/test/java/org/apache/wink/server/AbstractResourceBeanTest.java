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

package org.apache.wink.server;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.AbstractDynamicResource;
import org.apache.wink.common.WinkApplication;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.custommonkey.xmlunit.Diff;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class AbstractResourceBeanTest extends MockServletInvocationTest {

    public static class InnerApplication extends WinkApplication {

        @Override
        public Set<Object> getInstances() {
            AbstractTestCollectionResource servicesCollection =
                new AbstractTestCollectionResource();
            servicesCollection.setPath("/services");

            AbstractTestCollectionResource servicesCollectionWithWorkspaceAndTitle =
                new AbstractTestCollectionResource();
            servicesCollectionWithWorkspaceAndTitle.setPath("/services/workspaceAndTitle");
            servicesCollectionWithWorkspaceAndTitle.setWorkspaceTitle("Services Workspace Title");
            servicesCollectionWithWorkspaceAndTitle.setCollectionTitle("Services Collection Title");

            AbstractTestSingleResource singleService = new AbstractTestSingleResource();
            singleService.setPath("/services/{id}");

            AbstractTestSingleResource singleServiceDifferentURIs =
                new AbstractTestSingleResource();
            singleServiceDifferentURIs.setPath("/services1/{id}");

            AbstractTestSingleParentResource singleServiceParent =
                new AbstractTestSingleParentResource();
            singleServiceParent.setPath("parent");
            singleServiceParent.setParent(singleService);

            AbstractTestSingleParentResource singleServiceMultipleParents =
                new AbstractTestSingleParentResource();
            singleServiceMultipleParents.setPath("parent1");
            singleServiceMultipleParents.setParent(singleService);

            AbstractTestReferencingBeanResource beanReferencingaAnotherBean =
                new AbstractTestReferencingBeanResource();
            beanReferencingaAnotherBean.setPath("/referenceBean/{id}");
            beanReferencingaAnotherBean.setRefdBean(singleService);

            AbstractTestWithAnnotationsResource resourceWithAnnotations =
                new AbstractTestWithAnnotationsResource();

            AbstractTestReferencingBeanResource beanReferencingClass =
                new AbstractTestReferencingBeanResource();
            beanReferencingClass.setPath("/referenceClass/{id}");
            beanReferencingClass.setRefdBean(resourceWithAnnotations);

            AbstractTestSingleParentResource singleServiceParentIsClass =
                new AbstractTestSingleParentResource();
            singleServiceParentIsClass.setPath("parent");
            singleServiceParentIsClass.setParent(resourceWithAnnotations);

            Set<Object> set = new HashSet<Object>();
            set.add(servicesCollection);
            set.add(servicesCollectionWithWorkspaceAndTitle);
            set.add(singleService);
            set.add(singleServiceDifferentURIs);
            set.add(singleServiceParent);
            set.add(singleServiceMultipleParents);
            set.add(beanReferencingaAnotherBean);
            set.add(resourceWithAnnotations);
            set.add(beanReferencingClass);
            set.add(singleServiceParentIsClass);
            return set;
        }
    }

    @Override
    protected String getApplicationClassName() {
        return InnerApplication.class.getName();
    }

    private static final String EXPECTED_SERVICE_COLLECTION    = "expected service collection";

    private static final String EXPECTED_SERVICE_DOCUMENT      =
                                                                   "<service xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns=\"http://www.w3.org/2007/app\">\n" + "    <workspace>\n"
                                                                       + "        <atom:title>Services Workspace Title</atom:title>\n"
                                                                       + "        <collection href=\"http://localhost:80/services/workspaceAndTitle\">\n"
                                                                       + "            <atom:title>Services Collection Title</atom:title>\n"
                                                                       + "            <accept/>\n"
                                                                       + "        </collection>\n"
                                                                       + "    </workspace>\n"
                                                                       + "</service>\n";

    private static final String EXPECTED_SINGLE_ENTRY          = "expected single entry ";

    private static final String EXPECTED_SINGLE_ENTRY_PARENT   = "expected single entry parent ";

    private static final String EXPECTED_BEAN_REFERENCING_BEAN = "expected bean referencing bean ";

    public static class AbstractTestCollectionResource extends AbstractDynamicResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getServiceCollection() {
            return EXPECTED_SERVICE_COLLECTION;
        }
    }

    public static class AbstractTestSingleResource extends AbstractDynamicResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getService(@PathParam("id") String id) {
            return EXPECTED_SINGLE_ENTRY + id;
        }
    }

    public static class AbstractTestSingleParentResource extends AbstractDynamicResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getService(@PathParam("id") String id) {
            return EXPECTED_SINGLE_ENTRY_PARENT + id;
        }
    }

    public static class AbstractTestReferencingBeanResource extends AbstractDynamicResource {

        private Object refdBean;

        public void setRefdBean(Object refdBean) {
            this.refdBean = refdBean;
        }

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getService(@PathParam("id") String id) {
            return EXPECTED_BEAN_REFERENCING_BEAN + id + " " + refdBean.getClass().getName();
        }
    }

    @Path("/resourceWithAnnotations/{id}")
    public static class AbstractTestWithAnnotationsResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getService(@PathParam("id") String id) {
            return "resource with annotations " + id;
        }
    }

    public void testServicesCollection() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/services",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        MockHttpServletResponse response = invoke(mockRequest);
        String responseContent = response.getContentAsString();
        assertEquals(EXPECTED_SERVICE_COLLECTION, responseContent);
    }

    public void testServicesCollectionWithWorkspaceAndTitle() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/services/workspaceAndTitle",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        MockHttpServletResponse response = invoke(mockRequest);
        String responseContent = response.getContentAsString();
        assertEquals(EXPECTED_SERVICE_COLLECTION, responseContent);

        mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/",
                                                        MediaTypeUtils.ATOM_SERVICE_DOCUMENT_TYPE);
        response = invoke(mockRequest);
        responseContent = response.getContentAsString();
        Diff diff = new Diff(EXPECTED_SERVICE_DOCUMENT, responseContent);
        assertTrue("Comparing service document with expected one: " + diff.toString(), diff
            .identical());
    }

    public void testServicesSingleEntry() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/services/1",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        MockHttpServletResponse response = invoke(mockRequest);
        String responseContent = response.getContentAsString();
        assertEquals(EXPECTED_SINGLE_ENTRY + "1", responseContent);
    }

    public void testServicesSingleEntryDifferentURIs() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/services1/1",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        MockHttpServletResponse response = invoke(mockRequest);
        String responseContent = response.getContentAsString();
        assertEquals(EXPECTED_SINGLE_ENTRY + "1", responseContent);

        // mockRequest = MockRequestConstructor.constructMockRequest("GET",
        // "/services2/1",
        // MediaType.APPLICATION_ATOM_XML_TYPE);
        // response = invoke(mockRequest);
        // responseContent = response.getContentAsString();
        // assertEquals(EXPECTED_SINGLE_ENTRY + "1", responseContent);
    }

    public void testServicesSingleEntryWithParent() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/services/2/parent",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        MockHttpServletResponse response = invoke(mockRequest);
        String responseContent = response.getContentAsString();
        assertEquals(EXPECTED_SINGLE_ENTRY_PARENT + "2", responseContent);
    }

    public void testServicesSingleEntryWithMultipleParents() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/services/2/parent1",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        MockHttpServletResponse response = invoke(mockRequest);
        String responseContent = response.getContentAsString();
        assertEquals(EXPECTED_SINGLE_ENTRY_PARENT + "2", responseContent);

        // TODO: do we support multiple paths and multiple parents
        // mockRequest = MockRequestConstructor.constructMockRequest("GET",
        // "/services/2/parent2",
        // MediaType.APPLICATION_ATOM_XML_TYPE);
        // response = invoke(mockRequest);
        // responseContent = response.getContentAsString();
        // assertEquals(EXPECTED_SINGLE_ENTRY_PARENT + "2", responseContent);
        //
        // mockRequest = MockRequestConstructor.constructMockRequest("GET",
        // "/services1/2/parent1",
        // MediaType.APPLICATION_ATOM_XML_TYPE);
        // response = invoke(mockRequest);
        // responseContent = response.getContentAsString();
        // assertEquals(EXPECTED_SINGLE_ENTRY_PARENT + "2", responseContent);
        //
        // mockRequest = MockRequestConstructor.constructMockRequest("GET",
        // "/services1/2/parent2",
        // MediaType.APPLICATION_ATOM_XML_TYPE);
        // response = invoke(mockRequest);
        // responseContent = response.getContentAsString();
        // assertEquals(EXPECTED_SINGLE_ENTRY_PARENT + "2", responseContent);
        //
        // mockRequest = MockRequestConstructor.constructMockRequest("GET",
        // "/services2/2/parent1",
        // MediaType.APPLICATION_ATOM_XML_TYPE);
        // response = invoke(mockRequest);
        // responseContent = response.getContentAsString();
        // assertEquals(EXPECTED_SINGLE_ENTRY_PARENT + "2", responseContent);
        //
        // mockRequest = MockRequestConstructor.constructMockRequest("GET",
        // "/services2/2/parent2",
        // MediaType.APPLICATION_ATOM_XML_TYPE);
        // response = invoke(mockRequest);
        // responseContent = response.getContentAsString();
        // assertEquals(EXPECTED_SINGLE_ENTRY_PARENT + "2", responseContent);
    }

    public void testBeanReferencingAnotherBean() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/referenceBean/1",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        MockHttpServletResponse response = invoke(mockRequest);
        String responseContent = response.getContentAsString();
        assertEquals(EXPECTED_BEAN_REFERENCING_BEAN + "1 "
                         + "org.apache.wink.server.AbstractResourceBeanTest$AbstractTestSingleResource",
                     responseContent);
    }

    public void testBeanReferencingAnotherResourceWithAnnotations() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/referenceClass/1",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        MockHttpServletResponse response = invoke(mockRequest);
        String responseContent = response.getContentAsString();
        assertEquals(EXPECTED_BEAN_REFERENCING_BEAN + "1 "
                         + "org.apache.wink.server.AbstractResourceBeanTest$AbstractTestWithAnnotationsResource",
                     responseContent);
    }

    public void testBeanWithParentIsResourceWithAnnotations() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/resourceWithAnnotations/2/parent",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        MockHttpServletResponse response = invoke(mockRequest);
        String responseContent = response.getContentAsString();
        assertEquals(EXPECTED_SINGLE_ENTRY_PARENT + "2", responseContent);
    }

}
