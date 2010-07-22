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

package org.apache.wink.server.serviceability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.LogRecord;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.logging.WinkLogHandler;
import org.apache.wink.server.internal.application.ApplicationProcessor;
import org.apache.wink.server.internal.registry.ResourceRegistry;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 
 * When running this test in Eclipse or another IDE, make sure project wink-component-test-support is first in the classpath
 * so that SLF4J picks up the SLF4J bridge provided from it.  Otherwise, you'll get no log output to assert against.
 *
 */
public class ServiceabilityTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[]{MyResource.class, MyContextResolver.class, MyContextResolver1.class};
    }

    @Path("/root")
    public static class MyResource {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String getTEXT() {
            return "some text";
        }

        @GET
        @Produces(MediaType.TEXT_HTML)
        public String getHTML() {
            return "some html";
        }
        
        @GET
        @Path("2")
        @Produces(MediaType.TEXT_PLAIN)
        public String getTEXT2() {
            return "some text 2";
        }

    }
    
    // intentionally forgetting @Provider annotation as part of test
    public static class MyContextResolver implements ContextResolver {
        public Object getContext(Class type) {
            return null;
        }
    }
    
    @Provider
    public static class MyContextResolver1 implements ContextResolver {
        public Object getContext(Class type) {
            return null;
        }
    }
    
    public static class MyApp extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            Set<Class<?>> classes = new HashSet<Class<?>>();
            classes.add(MyAppResource.class);
            return classes;
        }
        
        @Path("/myapp")
        public static class MyAppResource {
            @GET
            @Produces(MediaType.TEXT_PLAIN)
            @Path("gettext")
            public String getTEXT() {
                return "some private text";
            }
        }
        
    }
    
    public static class MockAppValidator extends ApplicationValidator {

        @Override
        public boolean isValidProvider(Class<?> cls) {
            return true;
        }

        @Override
        public boolean isValidResource(Class<?> cls) {
            return true;
        }
        
    }
    

    @Override
    protected void setUp() throws Exception {
        WinkLogHandler.clearRecords();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        WinkLogHandler.clearRecords();
        super.tearDown();
    }
    
    public void testGoodAppStartupInfoLogOutput() throws Exception {
        WinkLogHandler.turnLoggingCaptureOn(WinkLogHandler.LEVEL.INFO);
        MockAppValidator mockAppValidator = new MockAppValidator();
        ResourceRegistry mockResourceRegistry = new ResourceRegistry(new LifecycleManagersRegistry(), mockAppValidator);
        ProvidersRegistry mockProvidersRegistry = new ProvidersRegistry(new LifecycleManagersRegistry(), mockAppValidator);
        ApplicationProcessor appProcessor = new ApplicationProcessor(new MyApp(), mockResourceRegistry, mockProvidersRegistry, false);
        appProcessor.process();
        WinkLogHandler.turnLoggingCaptureOff();
        ArrayList<LogRecord> records = WinkLogHandler.getRecords();
        
        assertEquals(3, records.size());
        assertEquals("The following application has been processed: org.apache.wink.server.serviceability.ServiceabilityTest$MyApp", records.get(0).getMessage());
        assertEquals("Registered resources: \n" +
                "  Path: myapp; ClassMetadata: Class: org.apache.wink.server.serviceability.ServiceabilityTest$MyApp$MyAppResource", records.get(1).getMessage());
        assertEquals("The following user-defined JAX-RS providers are registered: \n" +
                "RawType: interface javax.ws.rs.ext.ContextResolver\n" +
                "Data Map: {empty}\n" +
                "RawType: interface javax.ws.rs.ext.MessageBodyReader\n" +
                "Data Map: {empty}\n" +
                "RawType: interface javax.ws.rs.ext.MessageBodyWriter\n" +
                "Data Map: {empty}", records.get(2).getMessage());
    }
    
    public void testGoodURLLogOutput1() throws Exception {
        WinkLogHandler.turnLoggingCaptureOn(WinkLogHandler.LEVEL.DEBUG);
        
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/root", MediaType.TEXT_PLAIN);
        MockHttpServletResponse mockResponse = invoke(mockRequest);
        assertEquals(200, mockResponse.getStatus());
        assertEquals("some text", mockResponse.getContentAsString());
        
        WinkLogHandler.turnLoggingCaptureOff();
        ArrayList<LogRecord> records = WinkLogHandler.getRecords();
        
        assertEquals(1, records.size());
        assertEquals("Processing GET request to http://localhost:80/root, source content type is null, acceptable media types include text/plain", records.get(0).getMessage());
    }
    
    public void testGoodURLLogOutput2() throws Exception {
        WinkLogHandler.turnLoggingCaptureOn(WinkLogHandler.LEVEL.DEBUG);
        
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/root", MediaType.TEXT_HTML);
        MockHttpServletResponse mockResponse = invoke(mockRequest);
        assertEquals(200, mockResponse.getStatus());
        assertEquals("some html", mockResponse.getContentAsString());
        
        WinkLogHandler.turnLoggingCaptureOff();
        ArrayList<LogRecord> records = WinkLogHandler.getRecords();
        
        assertEquals(1, records.size());
        assertEquals("Processing GET request to http://localhost:80/root, source content type is null, acceptable media types include text/html", records.get(0).getMessage());
    }
    
    public void testGoodURLLogOutput3() throws Exception {
        WinkLogHandler.turnLoggingCaptureOn(WinkLogHandler.LEVEL.DEBUG);
        
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/root", MediaType.TEXT_PLAIN);
        mockRequest.setQueryString("param1=value1");
        MockHttpServletResponse mockResponse = invoke(mockRequest);
        assertEquals(200, mockResponse.getStatus());
        assertEquals("some text", mockResponse.getContentAsString());
        
        WinkLogHandler.turnLoggingCaptureOff();
        ArrayList<LogRecord> records = WinkLogHandler.getRecords();
        
        assertEquals(1, records.size());
        assertEquals("Processing GET request to http://localhost:80/root?param1=value1, source content type is null, acceptable media types include text/plain", records.get(0).getMessage());
    }
    
    public void testGoodURLLogOutput4() throws Exception {
        WinkLogHandler.turnLoggingCaptureOn(WinkLogHandler.LEVEL.DEBUG);
        
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/root/2", MediaType.TEXT_PLAIN);
        MockHttpServletResponse mockResponse = invoke(mockRequest);
        assertEquals(200, mockResponse.getStatus());
        assertEquals("some text 2", mockResponse.getContentAsString());
        
        WinkLogHandler.turnLoggingCaptureOff();
        ArrayList<LogRecord> records = WinkLogHandler.getRecords();
        
        assertEquals(1, records.size());
        assertEquals("Processing GET request to http://localhost:80/root/2, source content type is null, acceptable media types include text/plain", records.get(0).getMessage());
    }
    
    public void testBadURLLogOutput1() throws Exception {
        WinkLogHandler.turnLoggingCaptureOn(WinkLogHandler.LEVEL.INFO);
        
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/root/BAD", MediaType.TEXT_PLAIN);
        MockHttpServletResponse mockResponse = invoke(mockRequest);
        assertEquals(404, mockResponse.getStatus());
        
        WinkLogHandler.turnLoggingCaptureOff();
        ArrayList<LogRecord> records = WinkLogHandler.getRecords();
        
        assertEquals(1, records.size());
        assertEquals("The following error occurred during the invocation of the handlers chain: WebApplicationException (404 - Not Found) while processing GET request sent to http://localhost:80/root/BAD", records.get(0).getMessage());
        assertNull(records.get(0).getThrown());  // when NOT in debug mode, exception should NOT show up in the debug trace
    }
    
    public void testBadURLLogOutput2() throws Exception {
        WinkLogHandler.turnLoggingCaptureOn(WinkLogHandler.LEVEL.DEBUG);
        
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/root/BAD", MediaType.TEXT_PLAIN);
        MockHttpServletResponse mockResponse = invoke(mockRequest);
        assertEquals(404, mockResponse.getStatus());
        
        WinkLogHandler.turnLoggingCaptureOff();
        ArrayList<LogRecord> records = WinkLogHandler.getRecords();
        
        assertEquals(4, records.size());
        assertEquals("Processing GET request to http://localhost:80/root/BAD, source content type is null, acceptable media types include text/plain", records.get(0).getMessage());
        assertEquals("The following error occurred during the invocation of the handlers chain: WebApplicationException (404 - Not Found) while processing GET request sent to http://localhost:80/root/BAD", records.get(1).getMessage());
        assertNotNull(records.get(1).getThrown());  // when in debug mode, exception should show up in the debug trace
        assertEquals("Registered resources: \n" +
                "  Path: root; ClassMetadata: Class: org.apache.wink.server.serviceability.ServiceabilityTest$MyResource\n" +
                "  Path: ; ClassMetadata: Class: org.apache.wink.server.internal.resources.HtmlServiceDocumentResource", records.get(2).getMessage());
        assertEquals("The following user-defined JAX-RS providers are registered: \n" +
                "RawType: interface javax.ws.rs.ext.ContextResolver\nData Map: \n" +
                "MediaType key = */*\n" +
                "ObjectFactory Set value = {\n" +
                "  class org.apache.wink.server.serviceability.ServiceabilityTest$MyContextResolver1\n" +
                "}\n" +
                "\nRawType: interface javax.ws.rs.ext.MessageBodyReader\nData Map: {empty}" +
                "\nRawType: interface javax.ws.rs.ext.MessageBodyWriter\nData Map: {empty}", records.get(3).getMessage());
    }

}
