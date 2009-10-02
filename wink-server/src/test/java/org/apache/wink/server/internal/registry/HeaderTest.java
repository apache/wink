/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.wink.server.internal.registry;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class HeaderTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {Resource.class};
    }

    @Path(value = "/test")
    public static class Resource {

        @GET
        public String stringParamHandling(@HeaderParam("A") boolean a, @HeaderParam("B") boolean b) {
            StringBuilder sb = new StringBuilder();
            if (a) {
                sb.append("a=" + a);
            }
            if (b) {
                sb.append("b=" + b);
            }
            return sb.toString();
        }
    }

    public void testHeaders() throws Exception {

        testHeader(new String[][] {{"a", "true"}}, "a=true");

        testHeader(new String[][] { {"a", "true"}, {"a", "false"}}, "a=true");

        testHeader(new String[][] { {"a", "false"}, {"b", "true"}}, "b=true");

        testHeader(new String[][] { {"a", "true"}, {"b", "false"}, {"a", "false"}}, "a=true");

    }

    private void testHeader(String[][] headers, String expected) throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test", MediaType.WILDCARD);
        for (String[] header : headers) {
            request.addHeader(header[0], header[1]);

        }
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains(expected));
    }
}
