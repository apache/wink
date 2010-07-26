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

import org.apache.wink.client.MockHttpServer.MockHttpServerResponse;
import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.WinkConfiguration;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.utils.ProviderUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;

public class ApacheClientTest extends BaseTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Mockery mockery = new Mockery();
        final RuntimeContext context = mockery.mock(RuntimeContext.class);
        mockery.checking(new Expectations() {{
            allowing(context).getAttribute(WinkConfiguration.class); will(returnValue(null));
        }});
        
        RuntimeContextTLS.setRuntimeContext(context);
    }
    
    @Override
    public void tearDown() {
        RuntimeContextTLS.setRuntimeContext(null);
    }
    
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
        return new RestClient(new ApacheHttpClientConfig().applications(new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> set = new HashSet<Class<?>>();
                set.add(TestGenericsProvider.class);
                return set;
            }

        }));
    }

    public void testResourceGet() {
        MockHttpServerResponse response1 = new MockHttpServerResponse();
        response1.setMockResponseCode(200);
        MockHttpServerResponse response2 = new MockHttpServerResponse();
        response2.setMockResponseCode(200);
        MockHttpServerResponse response3 = new MockHttpServerResponse();
        response3.setMockResponseCode(200);
        server.setMockHttpServerResponses(response1, response2, response3);
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
        MockHttpServerResponse response1 = new MockHttpServerResponse();
        response1.setMockResponseCode(200);
        MockHttpServerResponse response2 = new MockHttpServerResponse();
        response2.setMockResponseCode(200);
        MockHttpServerResponse response3 = new MockHttpServerResponse();
        response3.setMockResponseCode(200);
        server.setMockHttpServerResponses(response1, response2, response3);
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
        MockHttpServerResponse response1 = new MockHttpServerResponse();
        response1.setMockResponseCode(200);
        MockHttpServerResponse response2 = new MockHttpServerResponse();
        response2.setMockResponseCode(200);
        MockHttpServerResponse response3 = new MockHttpServerResponse();
        response3.setMockResponseCode(200);
        server.setMockHttpServerResponses(response1, response2, response3);
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
        MockHttpServerResponse response1 = new MockHttpServerResponse();
        response1.setMockResponseCode(200);
        MockHttpServerResponse response2 = new MockHttpServerResponse();
        response2.setMockResponseCode(200);
        MockHttpServerResponse response3 = new MockHttpServerResponse();
        response3.setMockResponseCode(200);
        server.setMockHttpServerResponses(response1, response2, response3);
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
        MockHttpServerResponse response1 = new MockHttpServerResponse();
        response1.setMockResponseCode(200);
        MockHttpServerResponse response2 = new MockHttpServerResponse();
        response2.setMockResponseCode(200);
        server.setMockHttpServerResponses(response1, response2);
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
        server.getMockHttpServerResponses().get(0).setMockResponseCode(400);
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
        server.getMockHttpServerResponses().get(0).setMockResponseCode(400);
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
        server.getMockHttpServerResponses().get(0).setMockResponseCode(200);
        server.getMockHttpServerResponses().get(0).setMockResponseContent("REQUEST".getBytes("UTF-16"));
        server.getMockHttpServerResponses().get(0).setMockResponseContentType("text/plain; charset=UTF-16");

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
