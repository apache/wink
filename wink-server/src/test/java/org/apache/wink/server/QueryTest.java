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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests @QueryParam.
 */
public class QueryTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {QueryResource.class};
    }

    @Path("/query")
    public static class QueryResource {

        @GET
        @Produces
        public String getQuery2(@QueryParam("q") String q,
                                @QueryParam("b") String b,
                                @QueryParam("c") String c) {
            if (q == null && b == null && c == null) {
                return "normal";
            }
            if (c != null && q == null && b == null) {
                return "queryOptional:" + c;
            }
            if (c == null && q != null) {
                if (b == null) {
                    return "query1:" + q;
                }
                return "query2:q=" + q + ",b=" + b;
            }
            return "query3";
        }

    }

    public void testNoQuery() throws Exception {
        MockHttpServletResponse resp =
            invoke(MockRequestConstructor.constructMockRequest("GET", "/query", "*/*"));
        assertEquals("result", "normal", resp.getContentAsString());
    }

    public void testQueryWithOneParam() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/query", "*/*");
        request.setQueryString("q=A");
        MockHttpServletResponse resp = invoke(request);
        assertEquals("result", "query1:A", resp.getContentAsString());
    }

    public void testQueryWithNoParam() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/query", "*/*");
        request.setQueryString("q=A&b=B");
        MockHttpServletResponse resp = invoke(request);
        assertEquals("result", "query2:q=A,b=B", resp.getContentAsString());
    }

    public void testQueryOptional() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/query", "*/*");
        request.setQueryString("c=C");
        MockHttpServletResponse resp = invoke(request);
        assertEquals("result", "queryOptional:C", resp.getContentAsString());
    }

}
