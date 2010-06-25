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

package org.apache.wink.client;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

public class EmptyContentTypeTest extends BaseTest {

    @Provider
    @Produces("integer/integer")
    public static class MyIntegerProvider implements MessageBodyWriter<Integer> {

        public long getSize(Integer t,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType) {
            return -1;
        }

        public boolean isWriteable(Class<?> type,
                                   Type genericType,
                                   Annotation[] annotations,
                                   MediaType mediaType) {
            return type.isAssignableFrom(Integer.class);
        }

        public void writeTo(Integer t,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {
            int i = t;
            for (int k = 0; k < 32; ++k) {
                entityStream.write(i & 0x0001);
                i = i >> 1;
            }
        }

    }

    private RestClient getRestClient() {
        return new RestClient(new ClientConfig().applications(new Application() {

            @Override
            public Set<Object> getSingletons() {
                return null;
            }

            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> set = new HashSet<Class<?>>();
                set.add(MyIntegerProvider.class);
                return set;
            }

        }));
    }
    
    public void testEmptyContentType() throws Exception {
        server.setMockResponseCode(200);
        RestClient client = getRestClient();
        client.resource(serviceURL + "/integer").put(123);
        byte[] b = server.getRequestContent();
        String expectedBytes = "11011110000000000000000000000000";
        String actualBytes = "";
        for(int i = 0; i < b.length; ++i)
            actualBytes += b[i];
        assertEquals(expectedBytes, actualBytes);
    }
}
