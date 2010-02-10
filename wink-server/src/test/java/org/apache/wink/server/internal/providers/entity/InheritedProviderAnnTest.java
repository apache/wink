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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 */
package org.apache.wink.server.internal.providers.entity;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.utils.ProviderUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class InheritedProviderAnnTest extends MockServletInvocationTest {

    public static interface MyMessageBodyWriterInterface extends MessageBodyWriter<String> {

    }

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {TestResource.class, TestProvider.class};
    }

    @Provider
    @Produces("abcd/efgh")
    public static class TestParentProvider implements MyMessageBodyWriterInterface {

        public long getSize(String arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4) {
            return -1;
        }

        public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
            return String.class.equals(arg0);
        }

        public void writeTo(String arg0,
                            Class<?> arg1,
                            Type arg2,
                            Annotation[] arg3,
                            MediaType arg4,
                            MultivaluedMap<String, Object> arg5,
                            OutputStream arg6) throws IOException, WebApplicationException {
            arg6.write("parent".getBytes(ProviderUtils.getCharset(arg4)));
        }
    }

    public static class TestProvider extends TestParentProvider {

        public void writeTo(String arg0,
                            Class<?> arg1,
                            Type arg2,
                            Annotation[] arg3,
                            MediaType arg4,
                            MultivaluedMap<String, Object> arg5,
                            OutputStream arg6) throws IOException, WebApplicationException {
            arg6.write("child".getBytes(ProviderUtils.getCharset(arg4)));
        }
    }

    @Path("/string")
    public static class TestResource {

        @GET
        public String getForm() {
            return "hello";
        }
    }

    /**
     * Tests that a {@link Provider} can be inherited from the superclass. While
     * this is not required by the specification, it is in order to promote
     * better compatibility with providers (i.e. Jackson) that expected this
     * behavior.
     * 
     * @throws Exception
     */
    public void testInheritedProvider() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET", "/string", MediaType.TEXT_PLAIN);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("hello", response.getContentAsString());

        request = MockRequestConstructor.constructMockRequest("GET", "/string", "abcd/efgh");
        response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("child", response.getContentAsString());
    }
}
