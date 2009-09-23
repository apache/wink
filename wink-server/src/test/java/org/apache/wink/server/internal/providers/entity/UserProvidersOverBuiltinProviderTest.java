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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * User providers should always be chosen over regular providers.
 */
public class UserProvidersOverBuiltinProviderTest extends MockServletInvocationTest {

    private static final String PROVIDER_STR = "read with myprovider";

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {SimpleResource.class};
    }

    @Override
    protected Object[] getSingletons() {
        return new Object[] {new StringReaderProvider()};
    }

    @Path("/")
    public static class SimpleResource {

        @GET
        public String getSomething(String o) {
            return o.toString();
        }
    }

    @Provider
    // @Consumes("abcd/xyz")
    public static class StringReaderProvider implements MessageBodyReader<String> {

        public boolean isReadable(Class<?> type,
                                  Type genericType,
                                  Annotation[] annotations,
                                  MediaType mediaType) {
            return true;
        }

        public String readFrom(Class<String> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders,
                               InputStream entityStream) throws IOException,
            WebApplicationException {
            return PROVIDER_STR;
        }
    }

    public void testUserProviderOverBuiltIn() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/", "*/*", "abcd/xyz", "hello"
                .getBytes());

        MockHttpServletResponse response = invoke(mockRequest);
        String content = response.getContentAsString();
        assertEquals(PROVIDER_STR, content);
    }
}
