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
package org.apache.wink.server.internal.providers.entity;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class MessageBodyWriterProviderCorrectParametersTest extends MockServletInvocationTest {

    private static final String PROVIDER_STR = "write with myprovider";

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {SimpleResource.class};
    }

    @Override
    protected Object[] getSingletons() {
        return new Object[] {new MyMessageBodyWrite()};
    }

    @Path("/")
    public static class SimpleResource {

        @GET
        public Response getSomething() {
            return Response.ok("hello world").build();
        }
    }

    @Provider
    public static class MyMessageBodyWrite implements MessageBodyWriter<String> {

        public long getSize(String t,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType) {
            if (type != genericType) {
                throw new IllegalArgumentException("wrong arguments for this test");
            }
            return -1;
        }

        public boolean isWriteable(Class<?> type,
                                   Type genericType,
                                   Annotation[] annotations,
                                   MediaType mediaType) {
            if (type == genericType && String.class == type) {
                return true;
            }
            return false;
        }

        public void writeTo(String t,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {
            if (type != genericType || String.class != type) {
                throw new IllegalArgumentException("wrong arguments for this test");
            }
            entityStream.write(PROVIDER_STR.getBytes());
        }

    }

    public void testUserProviderOverBuiltIn() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/", "*/*");

        MockHttpServletResponse response = invoke(mockRequest);
        String content = response.getContentAsString();
        assertEquals(PROVIDER_STR, content);
    }
}
