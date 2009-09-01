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
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.ClientWebException;
import org.apache.wink.client.EntityType;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.common.utils.ProviderUtils;

public class ClientTest extends BaseTest {

    public static class TestGenerics<T> {
        private T t;

        public TestGenerics() {
        }

        public T getT() {
            return t;
        }

        public void setT(T t) {
            this.t = t;
        }
    }

    @Provider
    public static class TestGenericsProvider implements MessageBodyWriter<TestGenerics<String>>,
        MessageBodyReader<TestGenerics<String>> {

        public long getSize(TestGenerics<String> t,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType) {
            return t.getT().length();
        }

        public boolean isWriteable(Class<?> type,
                                   Type genericType,
                                   Annotation[] annotations,
                                   MediaType mediaType) {
            return isTestGenericsString(type, genericType);
        }

        private boolean isTestGenericsString(Class<?> type, Type genericType) {
            return type.equals(TestGenerics.class) && genericType instanceof ParameterizedType
                && ((ParameterizedType)genericType).getActualTypeArguments()[0]
                    .equals(String.class);
        }

        public void writeTo(TestGenerics<String> t,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {
            String string = t.getT();
            ProviderUtils.writeToStream(string, entityStream, mediaType);
        }

        public boolean isReadable(Class<?> type,
                                  Type genericType,
                                  Annotation[] annotations,
                                  MediaType mediaType) {
            return isTestGenericsString(type, genericType);
        }

        public TestGenerics<String> readFrom(Class<TestGenerics<String>> type,
                                             Type genericType,
                                             Annotation[] annotations,
                                             MediaType mediaType,
                                             MultivaluedMap<String, String> httpHeaders,
                                             InputStream entityStream) throws IOException,
            WebApplicationException {
            TestGenerics<String> tg = new TestGenerics<String>();
            String string = ProviderUtils.readFromStreamAsString(entityStream, mediaType);
            tg.setT(string);
            return tg;
        }
    }

    private RestClient getRestClient() {
        return new RestClient(new ClientConfig().applications(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> set = new HashSet<Class<?>>();
                set.add(TestGenericsProvider.class);
                return set;
            }

        }));
    }

    public void testResourceGet() {
        server.setMockResponseCode(200);
        RestClient client = getRestClient();
        Resource resource = client.resource(serviceURL);

        String string = resource.get(String.class);
        assertEquals(RECEIVED_MESSAGE, string);

        // do get with response
        ClientResponse clientResponse = resource.get();
        assertEquals(RECEIVED_MESSAGE, clientResponse.getEntity(String.class));

        // test generic entity
        TestGenerics<String> tg = resource.get(new EntityType<TestGenerics<String>>() {
        });
        assertEquals(RECEIVED_MESSAGE, tg.getT());
    }

    public void testResourcePut() throws IOException {
        server.setMockResponseCode(200);
        RestClient client = getRestClient();
        Resource resource = client.resource(serviceURL + "/testResourcePut");
        String response =
            resource.contentType("text/plain").accept("text/plain").put(String.class, SENT_MESSAGE);
        assertEquals(RECEIVED_MESSAGE, response);
        assertEquals(SENT_MESSAGE, server.getRequestContentAsString());

        // do put with response
        ClientResponse clientResponse = resource.put(SENT_MESSAGE);
        assertEquals(RECEIVED_MESSAGE, clientResponse.getEntity(String.class));

        // test generic entity
        TestGenerics<String> tg = resource.put(new EntityType<TestGenerics<String>>() {
        }, SENT_MESSAGE);
        assertEquals(RECEIVED_MESSAGE, tg.getT());

    }

    public void testResourcePost() throws IOException {
        server.setMockResponseCode(200);
        RestClient client = getRestClient();
        Resource resource = client.resource(serviceURL + "/testResourcePost");
        String response =
            resource.contentType("text/plain").accept("text/plain")
                .post(String.class, SENT_MESSAGE);
        assertEquals(RECEIVED_MESSAGE, response);
        assertEquals(SENT_MESSAGE, server.getRequestContentAsString());

        // do post with response
        ClientResponse clientResponse = resource.post(SENT_MESSAGE);
        assertEquals(RECEIVED_MESSAGE, clientResponse.getEntity(String.class));

        // test generic entity
        TestGenerics<String> tg = resource.post(new EntityType<TestGenerics<String>>() {
        }, SENT_MESSAGE);
        assertEquals(RECEIVED_MESSAGE, tg.getT());
    }

    public void testResourceDelete() {
        server.setMockResponseCode(200);
        RestClient client = getRestClient();
        Resource resource = client.resource(serviceURL);
        String response = resource.accept(MediaType.TEXT_PLAIN_TYPE).delete(String.class);
        assertEquals(RECEIVED_MESSAGE, response);

        // do delete with response
        ClientResponse clientResponse = resource.delete();
        assertEquals(RECEIVED_MESSAGE, clientResponse.getEntity(String.class));

        // test generic entity
        TestGenerics<String> tg = resource.delete(new EntityType<TestGenerics<String>>() {
        });
        assertEquals(RECEIVED_MESSAGE, tg.getT());
    }

    public void testInvoke() {
        server.setMockResponseCode(200);
        RestClient client = getRestClient();
        Resource resource = client.resource(serviceURL);

        String string = resource.invoke("GET", String.class, null);
        assertEquals(RECEIVED_MESSAGE, string);

        // test generic entity
        TestGenerics<String> tg = resource.invoke("GET", new EntityType<TestGenerics<String>>() {
        }, null);
        assertEquals(RECEIVED_MESSAGE, tg.getT());
    }

    public void testHttpErrorNoResponse() throws IOException {
        server.setMockResponseCode(400);
        RestClient client = getRestClient();
        Resource resource = client.resource(serviceURL);
        try {
            resource.accept("text/plain").invoke("GET", String.class, null);
            fail("ClientWebException must be thrown");
        } catch (ClientWebException e) {
            assertTrue(e.getResponse().getStatusCode() == 400);
        }
    }

    public void testHttpErrorWithResponse() throws IOException {
        server.setMockResponseCode(400);
        RestClient client = getRestClient();
        Resource resource = client.resource(serviceURL);
        try {
            ClientResponse res = resource.accept("text/plain").get();
            assertTrue(res.getStatusCode() == 400);
        } catch (Exception e) {
            fail("Exception must not be thrown");
        }
    }

    public void testResponseCharset() throws IOException {

        MockHttpServer server = new MockHttpServer(34567);
        server.setMockResponseCode(200);
        server.setMockResponseContent("REQUEST".getBytes("UTF-16"));
        server.setMockResponseContentType("text/plain; charset=UTF-16");

        server.startServer();
        try {
            RestClient client = getRestClient();
            Resource resource =
                client.resource(MessageFormat.format(SERVICE_URL, String.valueOf(server
                    .getServerPort())));
            String response = resource.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
            assertEquals("REQUEST", response);

        } finally {
            server.stopServer();
        }
    }

    public void testResponseEmptyContentType() throws IOException {
        MockHttpServer server = new MockHttpServer(34567);
        server.setMockResponseCode(200);
        server.setMockResponseContent("REQUEST".getBytes("UTF-8"));
        server.setMockResponseContentType("");

        server.startServer();
        try {
            RestClient client = getRestClient();
            Resource resource =
                client.resource(MessageFormat.format(SERVICE_URL, String.valueOf(server
                    .getServerPort())));
            String response = resource.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class);
            assertEquals("REQUEST", response);
        } finally {
            server.stopServer();
        }
    }
}
