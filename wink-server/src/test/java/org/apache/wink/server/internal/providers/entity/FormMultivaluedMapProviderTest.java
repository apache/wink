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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.providers.entity.FormMultivaluedMapProvider;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class FormMultivaluedMapProviderTest extends MockServletInvocationTest {

    private static String FORM       = "a=A1&a=A2&b=B+B&c=C%24C&d";
    private static byte[] FORM_BYTES = FORM.getBytes();

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {TestResource.class};
    }

    @Path("/form")
    public static class TestResource {

        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Produces(MediaType.APPLICATION_FORM_URLENCODED)
        public MultivaluedMap<String, String> getForm(MultivaluedMap<String, String> map) {
            // convert all parameter values to lower case and return them
            MultivaluedMapImpl<String, String> ret = new MultivaluedMapImpl<String, String>();
            for (String key : map.keySet()) {
                for (String value : map.get(key)) {
                    if (value != null) {
                        value = value.toLowerCase();
                    }
                    ret.add(key, value);
                }
            }
            return ret;
        }
    }

    @SuppressWarnings("serial")
    public static class MyMap extends MultivaluedMapImpl<String, String> implements
        MultivaluedMap<String, String> {
    }

    @SuppressWarnings("unchecked")
    public void testFormMultivaluedMapProvider() throws Exception {
        FormMultivaluedMapProvider provider = new FormMultivaluedMapProvider();
        Type type = MyMap.class.getGenericInterfaces()[0];
        Class<MultivaluedMap<String, String>> rawType =
            (Class<MultivaluedMap<String, String>>)((ParameterizedType)type).getRawType();

        assertTrue(provider.isReadable(rawType,
                                       type,
                                       null,
                                       MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        MultivaluedMap<String, String> map =
            provider.readFrom(rawType,
                              type,
                              null,
                              MediaType.APPLICATION_FORM_URLENCODED_TYPE,
                              null,
                              new ByteArrayInputStream(FORM_BYTES));
        assertNotNull(map);
        assertEquals(4, map.size());
        assertEquals("A1", map.get("a").get(0));
        assertEquals("A2", map.get("a").get(1));
        assertEquals("B+B", map.getFirst("b"));
        assertEquals("C%24C", map.getFirst("c"));
        assertEquals(null, map.getFirst("d"));

        assertTrue(provider.isWriteable(rawType,
                                        type,
                                        null,
                                        MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        provider.writeTo(map,
                         rawType,
                         type,
                         null,
                         MediaType.APPLICATION_FORM_URLENCODED_TYPE,
                         null,
                         os);
        String written = os.toString();
        assertEquals(FORM, written);
    }

    public void testFormMultivaluedMapInvocation() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/form",
                                                        MediaType.APPLICATION_FORM_URLENCODED,
                                                        MediaType.APPLICATION_FORM_URLENCODED,
                                                        FORM_BYTES);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        // all parameter values should be in lower case
        assertEquals(FORM.toLowerCase(), response.getContentAsString());
    }
}
