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
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class AbstractResourceWithEmptyPathTest extends MockServletInvocationTest {

    public static class InnerApplication extends WinkApplication {

        @Override
        public Set<Object> getInstances() {
            AbstractTestCollectionResource emptyPath = new AbstractTestCollectionResource();
            emptyPath.setPath("/a");

            AbstractTestCollectionResource2 emptyParent = new AbstractTestCollectionResource2();
            emptyParent.setPath("/emptyParent");
            Set<Object> set = new HashSet<Object>();
            set.add(emptyPath);
            set.add(emptyParent);
            return set;
        }
    }

    @Override
    protected String getApplicationClassName() {
        return InnerApplication.class.getName();
    }

    private static final String EXPECTED_SERVICE_COLLECTION_1 = "expected service collection 1";

    private static final String EXPECTED_SERVICE_COLLECTION_2 = "expected service collection 2";

    public static class AbstractTestCollectionResource extends AbstractDynamicResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getServiceCollection() {
            return EXPECTED_SERVICE_COLLECTION_1;
        }
    }

    public static class AbstractTestCollectionResource2 extends AbstractDynamicResource {

        @GET
        @Produces( {MediaType.APPLICATION_ATOM_XML})
        public String getServiceCollection() {
            return EXPECTED_SERVICE_COLLECTION_2;
        }
    }

    public void testBeanWithEmptyPath() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/a",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        MockHttpServletResponse response = invoke(mockRequest);
        String responseContent = response.getContentAsString();
        assertEquals(EXPECTED_SERVICE_COLLECTION_1, responseContent);
    }

    public void testBeanWithEmptyParent() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/emptyParent",
                                                        MediaType.APPLICATION_ATOM_XML_TYPE);
        MockHttpServletResponse response = invoke(mockRequest);
        String responseContent = response.getContentAsString();
        assertEquals(EXPECTED_SERVICE_COLLECTION_2, responseContent);
    }

}
