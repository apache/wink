/*******************************************************************************
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *     
 *      http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 *******************************************************************************/
package org.apache.wink.server.internal.handlers;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.HandlersFactory;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.apache.wink.server.handlers.ResponseHandler;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class UserHandlersTest extends MockServletInvocationTest {

    public static boolean requestHandlerInvoked  = false;
    public static boolean responseHandlerInvoked = false;
    public static boolean errorHandlerInvoked    = false;

    public static class MyRequestHandler implements RequestHandler {

        public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
            requestHandlerInvoked = true;
            chain.doChain(context);
        }

        public void init(Properties props) {
        }
    }

    public static class MyResponseHandler implements ResponseHandler {

        public void handleResponse(MessageContext context, HandlersChain chain) throws Throwable {
            responseHandlerInvoked = true;
            chain.doChain(context);
        }

        public void init(Properties props) {

        }

    }

    public static class MyErrorHandler implements ResponseHandler {

        public void handleResponse(MessageContext context, HandlersChain chain) throws Throwable {
            errorHandlerInvoked = true;
            chain.doChain(context);
        }

        public void init(Properties props) {

        }

    }

    public static class UserHandlersProvider extends HandlersFactory {

        @Override
        public List<? extends RequestHandler> getRequestHandlers() {
            return Arrays.asList(new MyRequestHandler());
        }

        @Override
        public List<? extends ResponseHandler> getResponseHandlers() {
            return Arrays.asList(new MyResponseHandler());
        }

        @Override
        public List<? extends ResponseHandler> getErrorHandlers() {
            return Arrays.asList(new MyErrorHandler());
        }
    }

    @Path("/hello")
    public static class MyResource {

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return "hello";
        }

    }

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {MyResource.class};
    }

    public void testRequestAndResponseHandlers() throws Exception {
        requestHandlerInvoked = false;
        responseHandlerInvoked = false;
        errorHandlerInvoked = false;

        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/hello", MediaType.TEXT_PLAIN);
        MockHttpServletResponse response = invoke(mockRequest);
        assertEquals(200, response.getStatus());
        assertTrue(requestHandlerInvoked);
        assertTrue(responseHandlerInvoked);
        assertFalse(errorHandlerInvoked);
    }

    public void testExceptionHandlers() throws Exception {
        requestHandlerInvoked  = false;
        responseHandlerInvoked = false;
        errorHandlerInvoked    = false;

        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/h", MediaType.TEXT_PLAIN);
        MockHttpServletResponse response = invoke(mockRequest);
        assertEquals(404, response.getStatus());
        assertFalse(requestHandlerInvoked);
        assertFalse(responseHandlerInvoked);
        assertTrue(errorHandlerInvoked);

    }

    @Override
    protected String getPropertiesFile() {
        return getClass().getName().replaceAll("\\.", "/") + ".properties";
    }
}
