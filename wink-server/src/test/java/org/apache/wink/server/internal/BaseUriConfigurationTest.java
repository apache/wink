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

package org.apache.wink.server.internal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test the functionality of configured base uri.
 */
public class BaseUriConfigurationTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {GetBaseUriResource.class};
    }

    @Path("/bas%20eUri")
    public static class GetBaseUriResource {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String getBaseUri(@Context UriInfo uriInfo) {
            return uriInfo.getBaseUri().toString();
        }

    } // class GetBaseUriResource

    @Override
    protected String getPropertiesFile() {
        String name = getClass().getName();
        String fileName = TestUtils.packageToPath(name) + ".properties";
        return fileName;
    }

    public void testDetection() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor
                .constructMockRequest("GET", "/con%20textPath/servle%20tPath/bas%20eUri", "*/*");
        request.setSecure(false);
        request.setScheme("http");
        request.setServerName("my%20Server");
        request.setContextPath("/con%20textPath");
        request.setServletPath("/servle%20tPath");
        request.setServerPort(9090);
        MockHttpServletResponse response = invoke(request);
        String content = response.getContentAsString();
        assertEquals("base URI in content",
                     "http://my%20host:123/my%20service/con%20textPath/servle%20tPath/",
                     content);

        MockHttpServletRequest secureRequest =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/secureCont%20extPath/secu%20reServletPath/bas%20eUri",
                                      "*/*");
        secureRequest.setSecure(true);
        secureRequest.setScheme("https");
        secureRequest.setServerName("secureServer");
        secureRequest.setContextPath("/secureCont%20extPath");
        secureRequest.setServletPath("/secu%20reServletPath");
        secureRequest.setServerPort(9091);
        MockHttpServletResponse secureResponse = invoke(secureRequest);
        String secureContent = secureResponse.getContentAsString();
        assertEquals("base URI in content",
                     "https://myho%20st:456/myser%20vice/secureCont%20extPath/secu%20reServletPath/",
                     secureContent);
    }

}
