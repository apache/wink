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
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientConfigException;
import org.apache.wink.client.ClientRuntimeException;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.common.internal.providers.entity.StringProvider;
import org.apache.wink.common.utils.ProviderUtils;

public class ConfigurationTest extends BaseTest {

    public void testConfiguration() {
        ClientConfig conf = new ClientConfig();
        conf.proxyHost("localhost").proxyPort(8080);
        conf.connectTimeout(6000);
        conf.followRedirects(true);

        assertEquals(conf.getProxyHost(), "localhost");
        assertEquals(conf.getProxyPort(), 8080);
        assertEquals(conf.getConnectTimeout(), 6000);
        assertEquals(conf.isFollowRedirects(), true);

        RestClient rc = new RestClient(conf);
        ClientConfig config = rc.getConfig();

        // test configuration locking
        try {
            config.proxyHost("localhost");
            fail("Configuration is locked - IllegalStateException must be thrown");
        } catch (ClientConfigException e) {
            // Success - Configuration is locked
        }
    }

    public void testConnectionThroughProxy() {
        // the server will still listen on the default server port.
        // if we specify the server port as the proxy port, we in essence test
        // that the connection is going through the proxy, because we
        // specify a different port for the server in the resource URL
        server.setMockResponseCode(200);
        ClientConfig config = new ClientConfig();
        config.proxyHost("localhost").proxyPort(serverPort);
        RestClient client = new RestClient(config);
        String resourceUrl = "http://googoo:" + (serverPort + 1) + "/some/service";
        Resource resource = client.resource(resourceUrl);
        resource.get(String.class);
        assertEquals(resourceUrl, server.getRequestUrl());
    }

    public void testConnectTimeout() {
        int connectTimeout = 2000;
        ClientConfig config = new ClientConfig();

        // set the connect timeout
        config.connectTimeout(connectTimeout);
        RestClient client = new RestClient(config);

        // shouldn't be able to connect
        Resource resource = client.resource("http://localhost:1111/koko");
        long before = System.currentTimeMillis();
        try {
            // the client should "connect timeout"
            resource.get(String.class);
            fail("Expected Exception to be thrown");
        } catch (Exception e) {
            // assert that no more than 2 (+tolerance) seconds have passed
            long after = System.currentTimeMillis();
            // set a tolerance of 1 second
            long tolerance = 1000;
            long expectedTimeout = connectTimeout + tolerance;
            long duration = after - before;
            assertTrue("Expected a duration of less than " + expectedTimeout
                + " ms, but was "
                + duration, duration <= expectedTimeout);
        }
    }

    public void testReadTimeout() {
        server.setMockResponseCode(200);
        // set the server to delay the response by 5 seconds.
        server.setDelayResponse(5000);

        ClientConfig config = new ClientConfig();
        // set the read timeout to be 2 seconds
        config.readTimeout(2000);
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        long before = System.currentTimeMillis();
        try {
            // the client should "read timeout" after 2 seconds
            resource.get(String.class);
            fail("Expected Exception to be thrown");
        } catch (Exception e) {
            // assert that about 2 seconds have passed
            long after = System.currentTimeMillis();
            long duration = after - before;
            assertTrue("Expected a duration of about 2000 ms, but was " + duration,
                       duration >= 1500 && duration <= 5000);

            // get to the SocketTimeoutException
            Throwable throwable = e;
            while (throwable != null && !(throwable instanceof SocketTimeoutException)) {
                throwable = throwable.getCause();
            }
            assertNotNull(throwable);
            assertEquals("Read timed out", throwable.getMessage());
        }
    }

    public void testApplication() {
        server.setMockResponseCode(200);
        server.setDelayResponse(0);
        ClientConfig conf = new ClientConfig();

        // Create new JAX-RS Application
        Application app = new Application() {
            @Override
            public Set<Class<?>> getClasses() {
                HashSet<Class<?>> set = new HashSet<Class<?>>();
                set.add(FooProvider.class);
                return set;
            }
        };

        conf.applications(app);

        RestClient client = new RestClient(conf);
        Resource resource = client.resource(serviceURL + "/testResourcePut");
        Foo response =
            resource.contentType("text/plain").accept("text/plain").post(Foo.class,
                                                                         new Foo(SENT_MESSAGE));
        assertEquals(RECEIVED_MESSAGE, response.foo);

        // Negative test - Foo Provider not registered
        try {
            client = new RestClient();
            resource = client.resource(serviceURL + "/testResourcePut");
            response =
                resource.contentType("text/plain").accept("text/plain").post(Foo.class,
                                                                             new Foo(SENT_MESSAGE));
            fail("ClientRuntimeException must be thrown");
        } catch (ClientRuntimeException e) {
            // Success
        }

    }

    @Provider
    @Consumes
    @Produces
    public static class FooProvider implements MessageBodyReader<Foo>, MessageBodyWriter<Foo> {

        private static final Logger logger = LoggerFactory.getLogger(StringProvider.class);

        public boolean isReadable(Class<?> type,
                                  Type genericType,
                                  Annotation[] annotations,
                                  MediaType mediaType) {
            return type == Foo.class;
        }

        public Foo readFrom(Class<Foo> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, String> httpHeaders,
                            InputStream entityStream) throws IOException, WebApplicationException {
            byte[] bytes = ProviderUtils.readFromStreamAsBytes(entityStream);
            String string = new String(bytes, ProviderUtils.getCharset(mediaType));
            return new Foo(string);
        }

        public long getSize(Foo t,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType) {
            return t.foo.length();
        }

        public boolean isWriteable(Class<?> type,
                                   Type genericType,
                                   Annotation[] annotations,
                                   MediaType mediaType) {
            return type == Foo.class;
        }

        public void writeTo(Foo t,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {

            logger.debug("Writing {} to stream using {}", t, getClass().getName());

            entityStream.write(t.foo.getBytes(ProviderUtils.getCharset(mediaType)));
        }
    }

    public static class Foo {
        public String foo;

        public Foo(String foo) {
            this.foo = foo;
        }
    }

}
