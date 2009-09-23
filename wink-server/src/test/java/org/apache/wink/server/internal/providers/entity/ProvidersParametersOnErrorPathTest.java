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

package org.apache.wink.server.internal.providers.entity;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.DELETE;
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

public class ProvidersParametersOnErrorPathTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {TestResource.class, ProviderUsingAnnotations.class};
    }

    @Provider
    public static class ProviderUsingAnnotations implements MessageBodyWriter<String> {

        public long getSize(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
            return -1;
        }

        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
            for (Annotation a : arg2) {
                if (a.annotationType().equals(GET.class)) {
                    return true;
                }
            }
            return false;
        }

        public void writeTo(String arg0,
                            Class<?> arg1,
                            Type arg2,
                            Annotation[] arg3,
                            MediaType arg4,
                            MultivaluedMap<String, Object> arg5,
                            OutputStream arg6) throws IOException, WebApplicationException {
            for (Annotation a : arg3) {
                if (a.annotationType().equals(GET.class)) {
                    arg6.write("Hello ".getBytes());
                }
            }

            arg6.write(arg0.getBytes());
        }

    }

    @Path("test")
    public static class TestResource {
        @GET
        public String getAsset() {
            return "world";
        }

        @DELETE
        public String deleteAsset() {
            throw new WebApplicationException(Response.status(499).entity("Goodbye").build());
        }
    }

    public void testAnnotationsNotNullOnGoodPath() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/test", "*/*");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("Hello world", response.getContentAsString());
    }

    public void testAnnotationsNotNullOnErrorPath() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("DELETE", "/test", "*/*");
        MockHttpServletResponse response = invoke(request);
        assertEquals(499, response.getStatus());
        assertEquals("Goodbye", response.getContentAsString());
    }
}
