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
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.AbstractDynamicResource;
import org.apache.wink.common.WinkApplication;
import org.apache.wink.common.annotations.Workspace;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class AbstractResourceWithDuplicateWorkspaceTest extends MockServletInvocationTest {

    public static class InnerApplication extends WinkApplication {

        @Override
        public Set<Object> getInstances() {
            Set<Object> set = new HashSet<Object>();
            AbstractTestWithWorkspaceResource servicesCollectionWithWorskapce =
                new AbstractTestWithWorkspaceResource();
            servicesCollectionWithWorskapce.setPath("/services/withWorkspace");
            servicesCollectionWithWorskapce.setCollectionTitle("Spring Collection");
            servicesCollectionWithWorskapce.setWorkspaceTitle("Spring Workspace");

            AbstractTestWithWorkspaceResource servicesCollectionWithoutWorskapce =
                new AbstractTestWithWorkspaceResource();
            servicesCollectionWithoutWorskapce.setPath("/services/withoutWorkspace");

            set.add(servicesCollectionWithWorskapce);
            set.add(servicesCollectionWithoutWorskapce);
            return set;
        }
    }

    @Override
    protected String getApplicationClassName() {
        return InnerApplication.class.getName();
    }

    private static final String EXPECTED_SERVICE_COLLECTION = "expected service collection 1";

    private static final String EXPECTED_SERVICE_DOCUMENT   =
                                                                "<service xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns=\"http://www.w3.org/2007/app\">" + "<workspace>"
                                                                    + "<atom:title>Annotated Workspace</atom:title>"
                                                                    + "<collection href=\"http://localhost:80/services/withoutWorkspace\">"
                                                                    + "<atom:title>Annotated Collection</atom:title>"
                                                                    + "<accept/>"
                                                                    + "</collection>"
                                                                    + "</workspace>"
                                                                    + "<workspace>"
                                                                    + "<atom:title>Spring Workspace</atom:title>"
                                                                    + "<collection href=\"http://localhost:80/services/withWorkspace\">"
                                                                    + "<atom:title>Spring Collection</atom:title>"
                                                                    + "<accept/>"
                                                                    + "</collection>"
                                                                    + "</workspace>"
                                                                    + "</service>";

    @Workspace(workspaceTitle = "Annotated Workspace", collectionTitle = "Annotated Collection")
    public static class AbstractTestWithWorkspaceResource extends AbstractDynamicResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getServiceCollection() {
            return EXPECTED_SERVICE_COLLECTION;
        }
    }

    public void testBeanWithEmptyPath() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/",
                                                        MediaTypeUtils.ATOM_SERVICE_DOCUMENT_TYPE);
        MockHttpServletResponse response = invoke(mockRequest);
        String responseContent = response.getContentAsString();
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(EXPECTED_SERVICE_DOCUMENT,
                                                             responseContent);
        assertNull(msg, msg);
    }

}
