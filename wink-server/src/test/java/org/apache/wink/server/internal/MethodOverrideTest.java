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

package org.apache.wink.server.internal;

import java.util.Properties;

import javax.ws.rs.HttpMethod;

import junit.framework.TestCase;

import org.apache.wink.common.http.HttpHeadersEx;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.handlers.ServerMessageContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test of EnhancedRequest
 */
public class MethodOverrideTest extends TestCase {

    public void testXHttpMethodOverride() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.addHeader(HttpHeadersEx.X_HTTP_METHOD_OVERRIDE, "PUT");
        MessageContext context =
            new ServerMessageContext(
                                     request,
                                     new MockHttpServletResponse(),
                                     getDeploymentConfiguration("X-HTTP-Method-Override,X-Method-Override"));
        assertEquals("HTTP method", HttpMethod.PUT, context.getHttpMethod());
    }
    
    public void testDefaultXHttpMethodOverride() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.addHeader(HttpHeadersEx.X_HTTP_METHOD_OVERRIDE, "PUT");
        MessageContext context =
            new ServerMessageContext(
                                     request,
                                     new MockHttpServletResponse(),
                                     getDeploymentConfiguration(null));
        assertEquals("HTTP method", HttpMethod.POST, context.getHttpMethod());
    }

    public void testXMethodOverride() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.addHeader(HttpHeadersEx.X_METHOD_OVERRIDE, "DELETE");
        MessageContext context =
            new ServerMessageContext(
                                     request,
                                     new MockHttpServletResponse(),
                                     getDeploymentConfiguration("X-HTTP-Method-Override,X-Method-Override"));
        assertEquals("HTTP method", HttpMethod.DELETE, context.getHttpMethod());
    }
    
    public void testDefaultXMethodOverride() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.addHeader(HttpHeadersEx.X_METHOD_OVERRIDE, "DELETE");
        MessageContext context =
            new ServerMessageContext(
                                     request,
                                     new MockHttpServletResponse(),
                                     getDeploymentConfiguration(null));
        assertEquals("HTTP method", HttpMethod.POST, context.getHttpMethod());
    }

    private DeploymentConfiguration getDeploymentConfiguration(String winkhttpMethodOverrideHeader) {
        DeploymentConfiguration configuration = new DeploymentConfiguration();
        Properties properties = new Properties();
        if (winkhttpMethodOverrideHeader != null) {
            properties.setProperty("wink.httpMethodOverrideHeaders", winkhttpMethodOverrideHeader);
        }
        configuration.setProperties(properties);
        configuration.init();
        return configuration;
    }

}
