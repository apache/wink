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
 *  
 */
package org.apache.wink.server.internal.properties;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.HandlersFactory;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class WinkSystemProperties2Test extends MockServletInvocationTest {

    private static final String JVM_PROPERTY_VALUE = "abcd";

    @Path("/resource1")
    public static class Resource {

        @GET
        public Response get() {
            return Response.ok().build();
        }
    }

    public static class UserHandlersProvider extends HandlersFactory {
        public List<? extends RequestHandler> getRequestHandlers() {
            return Collections.singletonList(new RequestHandler() {

                private Properties props = null;

                public void init(Properties props) {
                    this.props = props;
                }

                public void handleRequest(MessageContext context, HandlersChain chain)
                    throws Throwable {
                    chain.doChain(context);
                    context.setResponseEntity(props.getProperty("wink.SysPropertiesTest"));
                }
            });
        }
    }

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {Resource.class};
    }

    /**
     * Tests that the property is set in the custom config file and it stays set
     * to the value specified in the config file (even if there is a JVM property
     * set).  The config file has precedence over the JVM property.
     * 
     * @throws Exception
     */
    public void testPropertiesReadFromConfigPrecedence() throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", "/resource1", MediaType.WILDCARD);

        MockHttpServletResponse response = invoke(mockRequest);
        assertEquals(200, response.getStatus());
        assertEquals("valueInPropertiesFile", response.getContentAsString());
    }

    @Override
    protected String getPropertiesFile() {
        /*
         * set system properties here to trick the properties loading
         */
        System.setProperty("wink.SysPropertiesTest", JVM_PROPERTY_VALUE);
        return getClass().getName().replaceAll("\\.", "/") + ".properties";
    }
}
