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
package org.apache.wink.server.internal.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.server.handlers.MediaTypeMapperFactory;
import org.apache.wink.server.handlers.MediaTypeMappingRecord;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class MediaTypeMapping2Test extends MockServletInvocationTest {

    @Path("/resource1")
    public static class Resource {

        @GET
        public Response get() {
            return Response.ok("Hello world!").type("custom/type").build();
        }
    }

    private static class AgentStartsWith implements MediaTypeMappingRecord {

        private String    userAgentStartsWith;
        private MediaType responseType;
        private MediaType replacementType;

        public AgentStartsWith(String userAgentStartsWith,
                               String responseType,
                               String replacementType) {
            if (userAgentStartsWith == null)
                throw new NullPointerException();
            this.userAgentStartsWith = userAgentStartsWith;
            this.responseType = MediaType.valueOf(responseType);
            this.replacementType = MediaType.valueOf(replacementType);
        }

        public MediaType match(HttpHeaders requestHeaders, MediaType responseMediaType) {
            if (!MediaTypeUtils.equalsIgnoreParameters(responseMediaType, responseType)) {
                return null;
            }
            String userAgent = requestHeaders.getRequestHeaders().getFirst(HttpHeaders.USER_AGENT);
            if (userAgent != null && userAgent.startsWith(userAgentStartsWith)) {
                return replacementType;
            }
            return null;
        }
    }

    public static class UserMediaTypeMapperFactory extends MediaTypeMapperFactory {

        @Override
        public List<? extends MediaTypeMappingRecord> getMediaTypeMappings() {
            List<MediaTypeMappingRecord> records = new ArrayList<MediaTypeMappingRecord>();
            records.add(new AgentStartsWith("Agent123/", "custom/type", MediaType.TEXT_XML));
            records.add(new MediaTypeMappingRecord() {

                public MediaType match(HttpHeaders requestHeaders, MediaType responseMediaType) {
                    return MediaType.valueOf("my/type");
                }
            });
            return records;
        }

    }

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {Resource.class};
    }

    public void testMediaTypeMapping() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/resource1", MediaType.WILDCARD);
        MockHttpServletResponse response = invoke(mockRequest);
        assertEquals(200, response.getStatus());
        assertEquals("Hello world!", response.getContentAsString());
        assertEquals("my/type", response.getContentType());

        mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/resource1", MediaType.WILDCARD);
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "Agent123/abcd");
        response = invoke(mockRequest);
        assertEquals(200, response.getStatus());
        assertEquals("Hello world!", response.getContentAsString());
        assertEquals(MediaType.TEXT_XML, response.getContentType());

        mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/resource1", MediaType.WILDCARD);
        mockRequest.addHeader(HttpHeaders.USER_AGENT, "Agent123abcd");
        response = invoke(mockRequest);
        assertEquals(200, response.getStatus());
        assertEquals("Hello world!", response.getContentAsString());
        assertEquals("my/type", response.getContentType());
    }

    @Override
    protected String getPropertiesFile() {
        return getClass().getName().replaceAll("\\.", "/") + ".properties";
    }
}
