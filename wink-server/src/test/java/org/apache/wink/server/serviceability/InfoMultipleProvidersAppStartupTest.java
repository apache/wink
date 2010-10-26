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
 */
package org.apache.wink.server.serviceability;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.wink.logging.InMemoryHandler;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;

/**
 * Tests the logging of multiple providers at INFO level.
 */
public class InfoMultipleProvidersAppStartupTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {MyEntityProvider.class, MyEntityReader.class,
            MyExceptionMapper.class, MyContextResolver.class};
    }

    @Provider
    @Produces(MediaType.TEXT_PLAIN)
    public static class MyEntityProvider implements MessageBodyReader<Object>,
        MessageBodyWriter<Object> {

        public boolean isReadable(Class<?> type,
                                  Type genericType,
                                  Annotation[] annotations,
                                  MediaType mediaType) {
            return false;
        }

        public Object readFrom(Class<Object> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders,
                               InputStream entityStream) throws IOException {
            return null;
        }

        public long getSize(Object t,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType) {
            return 0;
        }

        public boolean isWriteable(Class<?> type,
                                   Type genericType,
                                   Annotation[] annotations,
                                   MediaType mediaType) {
            return false;
        }

        public void writeTo(Object t,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException {
        }

    }

    @Provider
    @Consumes( {"custom/*", "custom2/type2"})
    public static class MyEntityReader implements MessageBodyReader<String> {

        public boolean isReadable(Class<?> type,
                                  Type genericType,
                                  Annotation[] annotations,
                                  MediaType mediaType) {
            return false;
        }

        public String readFrom(Class<String> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders,
                               InputStream entityStream) throws IOException {
            return null;
        }

    }

    @Provider
    public static class MyExceptionMapper implements ExceptionMapper<NullPointerException> {

        public Response toResponse(NullPointerException exception) {
            return null;
        }

    }

    @Provider
    public static class MyContextResolver implements ContextResolver<String> {

        public String getContext(Class<?> type) {
            return null;
        }

    }

    private InMemoryHandler handler;

    private Logger          winkLogger = Logger.getLogger("org.apache.wink");

    @Override
    protected void setUp() throws Exception {
        handler = new InMemoryHandler();
        handler.setLevel(Level.INFO);

        winkLogger.setLevel(Level.INFO);
        winkLogger.addHandler(handler);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        winkLogger.removeHandler(handler);
        winkLogger.setLevel(Level.INFO);
        super.tearDown();
    }

    /**
     * Tests the INFO logging for multiple providers.
     * 
     * @throws Exception
     */
    public void testLogMultipleProviders() throws Exception {
        List<LogRecord> records = handler.getRecords();


        /*
         * This is actually a deterministic order for this particular test. The
         * type of provider and the media type determine the order. In
         * situations where there are no order (i.e. application/json versus
         * application/custom), the test will be fairly random.
         */
        final String provider1Message =
            "The class org.apache.wink.server.serviceability.InfoMultipleProvidersAppStartupTest$MyEntityReader was registered as a JAX-RS MessageBodyReader provider for java.lang.String Java types and custom2/type2 media types.";
        assertEquals(provider1Message, records.get(2).getMessage());
        assertEquals(Level.INFO, records.get(2).getLevel());
        final String provider2Message =
            "The class org.apache.wink.server.serviceability.InfoMultipleProvidersAppStartupTest$MyEntityReader was registered as a JAX-RS MessageBodyReader provider for java.lang.String Java types and custom/* media types.";
        assertEquals(provider2Message, records.get(3).getMessage());
        assertEquals(Level.INFO, records.get(3).getLevel());
        final String provider3Message =
            "The class org.apache.wink.server.serviceability.InfoMultipleProvidersAppStartupTest$MyEntityProvider was registered as a JAX-RS MessageBodyReader provider for all Java types and */* media types.";
        assertEquals(provider3Message, records.get(4).getMessage());
        assertEquals(Level.INFO, records.get(4).getLevel());
        final String provider4Message =
            "The class org.apache.wink.server.serviceability.InfoMultipleProvidersAppStartupTest$MyEntityProvider was registered as a JAX-RS MessageBodyWriter provider for all Java types and text/plain media types.";
        assertEquals(provider4Message, records.get(5).getMessage());
        assertEquals(Level.INFO, records.get(5).getLevel());

        final String provider5Message =
            "The class org.apache.wink.server.serviceability.InfoMultipleProvidersAppStartupTest$MyExceptionMapper was registered as a JAX-RS ExceptionMapper provider for java.lang.NullPointerException Java types.";
        assertEquals(provider5Message, records.get(6).getMessage());
        assertEquals(Level.INFO, records.get(6).getLevel());

        final String provider6Message =
            "The class org.apache.wink.server.serviceability.InfoMultipleProvidersAppStartupTest$MyContextResolver was registered as a JAX-RS ContextResolver provider for java.lang.String Java types and */* media types.";
        assertEquals(provider6Message, records.get(7).getMessage());
        assertEquals(Level.INFO, records.get(7).getLevel());

        assertEquals(8, records.size());
    }

}
