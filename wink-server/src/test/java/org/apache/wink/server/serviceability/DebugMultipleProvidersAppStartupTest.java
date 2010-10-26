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
import java.util.Formatter;
import java.util.List;
import java.util.logging.Handler;
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

public class DebugMultipleProvidersAppStartupTest extends MockServletInvocationTest {

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

    private Handler         consoleHandler;

    @Override
    protected void setUp() throws Exception {
        Handler[] defaultHandlers = Logger.getLogger("").getHandlers();
        if (defaultHandlers.length == 1) {
            consoleHandler = defaultHandlers[0];
            consoleHandler.setLevel(Level.FINE);
        }

        handler = new InMemoryHandler();
        handler.setLevel(Level.FINE);

        winkLogger.setLevel(Level.FINE);
        winkLogger.addHandler(handler);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        if (consoleHandler != null) {
            consoleHandler.setLevel(Level.INFO);
        }
        winkLogger.removeHandler(handler);
        winkLogger.setLevel(Level.INFO);
        super.tearDown();
    }

    /**
     * Tests that a Provider is logged at the debug level.
     */
    public void testLogOneProvider() throws Exception {
        List<LogRecord> records = handler.getRecords();

        /*
         * This is actually a deterministic order for this particular test. The
         * type of provider and the media type determine the order. In
         * situations where there are no order (i.e. application/json versus
         * application/custom), the test will be fairly random.
         */
        final String provider1Message =
            "The class org.apache.wink.server.serviceability.DebugMultipleProvidersAppStartupTest$MyEntityReader was registered as a JAX-RS MessageBodyReader provider for java.lang.String Java types and custom2/type2 media types.";
        assertEquals(provider1Message, records.get(11).getMessage());
        assertEquals(Level.INFO, records.get(11).getLevel());
        final String provider2Message =
            "The class org.apache.wink.server.serviceability.DebugMultipleProvidersAppStartupTest$MyEntityReader was registered as a JAX-RS MessageBodyReader provider for java.lang.String Java types and custom/* media types.";
        assertEquals(provider2Message, records.get(12).getMessage());
        assertEquals(Level.INFO, records.get(12).getLevel());
        final String provider3Message =
            "The class org.apache.wink.server.serviceability.DebugMultipleProvidersAppStartupTest$MyEntityProvider was registered as a JAX-RS MessageBodyReader provider for all Java types and */* media types.";
        assertEquals(provider3Message, records.get(13).getMessage());
        assertEquals(Level.INFO, records.get(13).getLevel());
        final String provider4Message =
            "The class org.apache.wink.server.serviceability.DebugMultipleProvidersAppStartupTest$MyEntityProvider was registered as a JAX-RS MessageBodyWriter provider for all Java types and text/plain media types.";
        assertEquals(provider4Message, records.get(14).getMessage());
        assertEquals(Level.INFO, records.get(14).getLevel());

        final String provider5Message =
            "The class org.apache.wink.server.serviceability.DebugMultipleProvidersAppStartupTest$MyExceptionMapper was registered as a JAX-RS ExceptionMapper provider for java.lang.NullPointerException Java types.";
        assertEquals(provider5Message, records.get(15).getMessage());
        assertEquals(Level.INFO, records.get(15).getLevel());

        final String provider6Message =
            "The class org.apache.wink.server.serviceability.DebugMultipleProvidersAppStartupTest$MyContextResolver was registered as a JAX-RS ContextResolver provider for java.lang.String Java types and */* media types.";
        assertEquals(provider6Message, records.get(16).getMessage());
        assertEquals(Level.INFO, records.get(16).getLevel());

        assertEquals(Level.FINE, records.get(17).getLevel());
        StringBuffer sb = new StringBuffer();
        Formatter f = new Formatter(sb);
        f.format("The following JAX-RS MessageBodyReader providers are registered:");
        f.format("%n%1$-35s %2$-25s %3$-8s %4$s",
                 "Consumes Media Type",
                 "Generic Type",
                 "Custom?",
                 "Provider Class");
        f
            .format("%n%1$-35s %2$-25s %3$-8s %4$s",
                    "custom2/type2",
                    "String",
                    "true",
                    "org.apache.wink.server.serviceability.DebugMultipleProvidersAppStartupTest$MyEntityReader");
        f
            .format("%n%1$-35s %2$-25s %3$-8s %4$s",
                    "custom/*",
                    "String",
                    "true",
                    "org.apache.wink.server.serviceability.DebugMultipleProvidersAppStartupTest$MyEntityReader");
        f
            .format("%n%1$-35s %2$-25s %3$-8s %4$s",
                    "*/*",
                    "Object",
                    "true",
                    "org.apache.wink.server.serviceability.DebugMultipleProvidersAppStartupTest$MyEntityProvider");
        assertEquals(sb.toString(), records.get(17).getMessage());

        assertEquals(Level.FINE, records.get(18).getLevel());
        sb = new StringBuffer();
        f = new Formatter(sb);
        f.format("The following JAX-RS MessageBodyWriter providers are registered:");
        f.format("%n%1$-35s %2$-25s %3$-8s %4$s",
                 "Produces Media Type",
                 "Generic Type",
                 "Custom?",
                 "Provider Class");
        f
            .format("%n%1$-35s %2$-25s %3$-8s %4$s",
                    "text/plain",
                    "Object",
                    "true",
                    "org.apache.wink.server.serviceability.DebugMultipleProvidersAppStartupTest$MyEntityProvider");
        assertEquals(sb.toString(), records.get(18).getMessage());

        assertEquals(Level.FINE, records.get(19).getLevel());
        sb = new StringBuffer();
        f = new Formatter(sb);
        f.format("The following JAX-RS ExceptionMapper providers are registered:");
        f.format("%n%1$-25s %2$-8s %3$s", "Generic Type", "Custom?", "Provider Class");
        f
            .format("%n%1$-25s %2$-8s %3$s",
                    "NullPointerException",
                    "true",
                    "org.apache.wink.server.serviceability.DebugMultipleProvidersAppStartupTest$MyExceptionMapper");
        assertEquals(sb.toString(), records.get(19).getMessage());

        assertEquals(Level.FINE, records.get(20).getLevel());
        sb = new StringBuffer();
        f = new Formatter(sb);
        f.format("The following JAX-RS ContextResolver providers are registered:");
        f.format("%n%1$-35s %2$-25s %3$-8s %4$s",
                 "Produces Media Type",
                 "Generic Type",
                 "Custom?",
                 "Provider Class");
        f
            .format("%n%1$-35s %2$-25s %3$-8s %4$s",
                    "*/*",
                    "String",
                    "true",
                    "org.apache.wink.server.serviceability.DebugMultipleProvidersAppStartupTest$MyContextResolver");
        assertEquals(sb.toString(), records.get(20).getMessage());

        assertEquals(21, records.size());
    }
}
