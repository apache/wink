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

package org.apache.wink.example.locking;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.wink.example.locking.resources.DefectResource;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class PreconditionsTest extends MockServletInvocationTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {DefectResource.class, FormattingOptionsContextResolver.class};
    }

    public void testLockingExample() throws Exception {

        // get a single defect and ensure it returns with etag
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/defects/11",
                                                        MediaType.APPLICATION_XML_TYPE);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        byte[] defect11 = response.getContentAsString().getBytes();
        String diff =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier("/results/defect_11.xml",
                                                             defect11,
                                                             getClass());
        assertNull(diff, diff);
        String etag = (String)response.getHeader(HttpHeaders.ETAG);
        assertNotNull(etag);

        // get collection of defects and ensure that last-modified was sent
        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/defects",
                                                        MediaType.APPLICATION_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        String lastModified = (String)response.getHeader(HttpHeaders.LAST_MODIFIED);
        assertNotNull(lastModified);

        // get collection of defects with IF_MODIFIED_SINCE header
        // collection was not modified, so 304 should return
        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/defects",
                                                        MediaType.APPLICATION_XML_TYPE);
        request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
        response = invoke(request);
        assertEquals("status", 304, response.getStatus());

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/defects/11",
                                                        MediaType.APPLICATION_XML_TYPE);
        request.addHeader("if-none-match", etag);
        response = invoke(request);
        assertEquals("status", 304, response.getStatus());

        Thread.sleep(1000);

        request =
            MockRequestConstructor.constructMockRequest("PUT",
                                                        "/defects/11",
                                                        MediaType.APPLICATION_XML,
                                                        MediaType.APPLICATION_XML,
                                                        defect11);
        request.addHeader("if-match", etag);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        etag = (String)response.getHeader(HttpHeaders.ETAG);

        // now, after the collection was modified
        // 200 should return
        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/defects",
                                                        MediaType.APPLICATION_XML_TYPE);
        request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());

        request =
            MockRequestConstructor.constructMockRequest("PUT",
                                                        "/defects/11",
                                                        MediaType.APPLICATION_XML,
                                                        MediaType.APPLICATION_XML,
                                                        defect11);
        response = invoke(request);
        assertEquals("status", 400, response.getStatus());

        request =
            MockRequestConstructor.constructMockRequest("PUT",
                                                        "/defects/11",
                                                        MediaType.APPLICATION_XML,
                                                        MediaType.APPLICATION_XML,
                                                        defect11);
        request.addHeader("if-match", "\"blabla\"");
        response = invoke(request);
        assertEquals("status", 412, response.getStatus());

        request =
            MockRequestConstructor.constructMockRequest("DELETE",
                                                        "/defects/11",
                                                        MediaType.APPLICATION_XML);
        request.addHeader("if-match", "\"blabla\"");
        response = invoke(request);
        assertEquals("status", 412, response.getStatus());

        request =
            MockRequestConstructor.constructMockRequest("DELETE",
                                                        "/defects/11",
                                                        MediaType.APPLICATION_XML);
        response = invoke(request);
        assertEquals("status", 400, response.getStatus());

        request =
            MockRequestConstructor.constructMockRequest("DELETE",
                                                        "/defects/11",
                                                        MediaType.APPLICATION_XML);
        request.addHeader("if-match", etag);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());

        request =
            MockRequestConstructor.constructMockRequest("DELETE",
                                                        "/defects/11",
                                                        MediaType.APPLICATION_XML);
        request.addHeader("if-match", etag);
        response = invoke(request);
        assertEquals("status", 404, response.getStatus());

    }

}
