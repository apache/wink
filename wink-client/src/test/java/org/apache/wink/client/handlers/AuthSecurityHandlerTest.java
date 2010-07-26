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
package org.apache.wink.client.handlers;

import org.apache.wink.client.BaseTest;
import org.apache.wink.client.ClientAuthenticationException;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.MockHttpServer;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

public class AuthSecurityHandlerTest extends BaseTest {
    
    /*
     * API TESTS
     */
    
    // basic auth handler should throw exception when challenged but no username is set
    public void testNoUserNameBasicAuthFailure() throws Exception {
        server.getMockHttpServerResponses().get(0).setMockResponseCode(401);
        ClientConfig config = new ClientConfig();
        BasicAuthSecurityHandler basicAuthSecurityHandler = new BasicAuthSecurityHandler();
        // oops, forgot to set username!
        basicAuthSecurityHandler.setPassword("password");
        config.handlers(basicAuthSecurityHandler);
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        try {
            @SuppressWarnings("unused")
            ClientResponse response = resource.get();
            fail("should have got a ClientAuthenticationException");
        } catch (ClientAuthenticationException e) {
            // do nothing
        }
    }
    
    // basic auth handler should throw exception when challenged but no password is set
    public void testNoPasswordBasicAuthFailure() throws Exception {
        server.getMockHttpServerResponses().get(0).setMockResponseCode(401);
        ClientConfig config = new ClientConfig();
        BasicAuthSecurityHandler basicAuthSecurityHandler = new BasicAuthSecurityHandler();
        basicAuthSecurityHandler.setUserName("username");
        // oops, forgot to set password!
        config.handlers(basicAuthSecurityHandler);
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        try {
            @SuppressWarnings("unused")
            ClientResponse response = resource.get();
            fail("should have got a ClientAuthenticationException");
        } catch (ClientAuthenticationException e) {
            // do nothing
        }
    }
    
    // basic auth handler should flow through when NOT challenged but no username is set
    public void testNoUserNameBasicAuthAllowed() throws Exception {
        server.getMockHttpServerResponses().get(0).setMockResponseCode(200);
        ClientConfig config = new ClientConfig();
        BasicAuthSecurityHandler basicAuthSecurityHandler = new BasicAuthSecurityHandler();
        // oops, forgot to set username!
        basicAuthSecurityHandler.setPassword("password");
        config.handlers(basicAuthSecurityHandler);
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        ClientResponse response = resource.get();
        assertEquals(200, response.getStatusCode());
    }
    
    // proxy auth handler should throw exception when challenged but no username is set
    public void testNoUserNameProxyAuthFailure() throws Exception {
        server.getMockHttpServerResponses().get(0).setMockResponseCode(407);
        ClientConfig config = new ClientConfig();
        ProxyAuthSecurityHandler proxyAuthSecurityHandler = new ProxyAuthSecurityHandler();
        // oops, forgot to set username!
        proxyAuthSecurityHandler.setPassword("password");
        config.handlers(proxyAuthSecurityHandler);
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        try {
            @SuppressWarnings("unused")
            ClientResponse response = resource.get();
            fail("should have got a ClientAuthenticationException");
        } catch (ClientAuthenticationException e) {
            // do nothing
        }
    }
    
    // proxy auth handler should throw exception when challenged but no password is set
    public void testNoPasswordProxyAuthFailure() throws Exception {
        server.getMockHttpServerResponses().get(0).setMockResponseCode(407);
        ClientConfig config = new ClientConfig();
        ProxyAuthSecurityHandler proxyAuthSecurityHandler = new ProxyAuthSecurityHandler();
        proxyAuthSecurityHandler.setUserName("username");
        // oops, forgot to set password!
        config.handlers(proxyAuthSecurityHandler);
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        try {
            @SuppressWarnings("unused")
            ClientResponse response = resource.get();
            fail("should have got a ClientAuthenticationException");
        } catch (ClientAuthenticationException e) {
            // do nothing
        }
    }
    
    // proxy auth handler should flow through when NOT challenged but no username is set
    public void testNoUserNameProxyAuthAllowed() throws Exception {
        server.getMockHttpServerResponses().get(0).setMockResponseCode(200);
        ClientConfig config = new ClientConfig();
        ProxyAuthSecurityHandler proxyAuthSecurityHandler = new ProxyAuthSecurityHandler();
        // oops, forgot to set username!
        proxyAuthSecurityHandler.setPassword("password");
        config.handlers(proxyAuthSecurityHandler);
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        ClientResponse response = resource.get();
        assertEquals(200, response.getStatusCode());
    }
    
    /*
     * BASIC AUTH
     */
    
    public void testNoBasicAuthHandler() throws Exception {
        server.getMockHttpServerResponses().get(0).setMockResponseCode(401);
        RestClient client = new RestClient(new ClientConfig());
        Resource resource = client.resource(serviceURL);
        ClientResponse response = resource.get();
        assertEquals(401, response.getStatusCode());  // should have challenged us due to lack of credentials
    }
    
    public void testBasicAuthHandlerNoAuthRequired() throws Exception {
        // try with BasicAuthSecurityHandler, but return 200 from server for first response; BasicAuthSecurityHandler should allow flow through
        server.getMockHttpServerResponses().get(0).setMockResponseCode(200);
        ClientConfig config = new ClientConfig();
        BasicAuthSecurityHandler basicAuthSecurityHandler = new BasicAuthSecurityHandler();
        basicAuthSecurityHandler.setUserName("username");
        basicAuthSecurityHandler.setPassword("password");
        config.handlers(basicAuthSecurityHandler);
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        ClientResponse response = resource.get();
        assertEquals(200, response.getStatusCode());
    }

    public void testBasicAuthHandlerAuthDenied() throws Exception {
        // try with BasicAuthSecurityHandler, but still return 401 from server for both first and second responses
        MockHttpServer.MockHttpServerResponse response1 = new MockHttpServer.MockHttpServerResponse();
        response1.setMockResponseCode(401);
        MockHttpServer.MockHttpServerResponse response2 = new MockHttpServer.MockHttpServerResponse();
        response2.setMockResponseCode(401);
        server.setMockHttpServerResponses(response1, response2);
        ClientConfig config = new ClientConfig();
        BasicAuthSecurityHandler basicAuthSecurityHandler = new BasicAuthSecurityHandler();
        basicAuthSecurityHandler.setUserName("username");
        basicAuthSecurityHandler.setPassword("password");
        config.handlers(basicAuthSecurityHandler);
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        try {
            @SuppressWarnings("unused")
            ClientResponse response = resource.get();
            fail("should have got a ClientAuthenticationException");
        } catch (ClientAuthenticationException e) {
            // do nothing
        }
    }
    
    public void testBasicAuthHandlerAuthGranted() throws Exception {
        // try again with BasicAuthSecurityHandler, but now return 200 from server on the second response
        MockHttpServer.MockHttpServerResponse response1 = new MockHttpServer.MockHttpServerResponse();
        response1.setMockResponseCode(401);
        MockHttpServer.MockHttpServerResponse response2 = new MockHttpServer.MockHttpServerResponse();
        response2.setMockResponseCode(200);
        server.setMockHttpServerResponses(response1, response2);
        ClientConfig config = new ClientConfig();
        BasicAuthSecurityHandler basicAuthSecurityHandler = new BasicAuthSecurityHandler();
        basicAuthSecurityHandler.setUserName("username");
        basicAuthSecurityHandler.setPassword("password");
        config.handlers(basicAuthSecurityHandler);
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        ClientResponse response = resource.get();
        assertEquals(200, response.getStatusCode());
    }
    
    /*
     * PROXY AUTH
     */
    
    public void testNoProxyAuthHandler() throws Exception {
        server.getMockHttpServerResponses().get(0).setMockResponseCode(407);
        RestClient client = new RestClient(new ClientConfig());
        Resource resource = client.resource(serviceURL);
        ClientResponse response = resource.get();
        assertEquals(407, response.getStatusCode());  // should have challenged us due to lack of credentials
    }
    
    public void testProxyAuthHandlerNoAuthRequired() throws Exception {
        // try with ProxyAuthSecurityHandler, but return 200 from server for first response; ProxyAuthSecurityHandler should allow flow through
        server.getMockHttpServerResponses().get(0).setMockResponseCode(200);
        ClientConfig config = new ClientConfig();
        ProxyAuthSecurityHandler proxyAuthSecurityHandler = new ProxyAuthSecurityHandler();
        proxyAuthSecurityHandler.setUserName("username");
        proxyAuthSecurityHandler.setPassword("password");
        config.handlers(proxyAuthSecurityHandler);
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        ClientResponse response = resource.get();
        assertEquals(200, response.getStatusCode());
    }

    public void testProxyAuthHandlerAuthDenied() throws Exception {
        // try with ProxyAuthSecurityHandler, but still return 407 from server for both first and second responses
        MockHttpServer.MockHttpServerResponse response1 = new MockHttpServer.MockHttpServerResponse();
        response1.setMockResponseCode(407);
        MockHttpServer.MockHttpServerResponse response2 = new MockHttpServer.MockHttpServerResponse();
        response2.setMockResponseCode(407);
        server.setMockHttpServerResponses(response1, response2);
        ClientConfig config = new ClientConfig();
        ProxyAuthSecurityHandler proxyAuthSecurityHandler = new ProxyAuthSecurityHandler();
        proxyAuthSecurityHandler.setUserName("username");
        proxyAuthSecurityHandler.setPassword("password");
        config.handlers(proxyAuthSecurityHandler);
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        try {
            @SuppressWarnings("unused")
            ClientResponse response = resource.get();
            fail("should have got a ClientAuthenticationException");
        } catch (ClientAuthenticationException e) {
            // do nothing
        }
    }
    
    public void testProxyAuthHandlerAuthGranted() throws Exception {
        // try again with ProxyAuthSecurityHandler, but now return 200 from server on the second response
        MockHttpServer.MockHttpServerResponse response1 = new MockHttpServer.MockHttpServerResponse();
        response1.setMockResponseCode(407);
        MockHttpServer.MockHttpServerResponse response2 = new MockHttpServer.MockHttpServerResponse();
        response2.setMockResponseCode(200);
        server.setMockHttpServerResponses(response1, response2);
        ClientConfig config = new ClientConfig();
        ProxyAuthSecurityHandler proxyAuthSecurityHandler = new ProxyAuthSecurityHandler();
        proxyAuthSecurityHandler.setUserName("username");
        proxyAuthSecurityHandler.setPassword("password");
        config.handlers(proxyAuthSecurityHandler);
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        ClientResponse response = resource.get();
        assertEquals(200, response.getStatusCode());
    }
    
    /*
     * BOTH PROXY AND BASIC
     */
    
    public void testProxyAndBasicAuthHandlerAuthGranted() throws Exception {
        // try again with ProxyAuthSecurityHandler and BasicAuthSecurityHandler, return 407, then 401, then 200
        MockHttpServer.MockHttpServerResponse response1 = new MockHttpServer.MockHttpServerResponse();
        response1.setMockResponseCode(407);
        MockHttpServer.MockHttpServerResponse response2 = new MockHttpServer.MockHttpServerResponse();
        response2.setMockResponseCode(401);
        MockHttpServer.MockHttpServerResponse response3 = new MockHttpServer.MockHttpServerResponse();
        response3.setMockResponseCode(200);
        server.setMockHttpServerResponses(response1, response2, response3);
        ClientConfig config = new ClientConfig();
        BasicAuthSecurityHandler basicAuthSecurityHandler = new BasicAuthSecurityHandler();
        basicAuthSecurityHandler.setUserName("basicuser");
        basicAuthSecurityHandler.setPassword("basicpassword");
        ProxyAuthSecurityHandler proxyAuthSecurityHandler = new ProxyAuthSecurityHandler();
        proxyAuthSecurityHandler.setUserName("username");
        proxyAuthSecurityHandler.setPassword("password");
        config.handlers(proxyAuthSecurityHandler, basicAuthSecurityHandler);  // proxy first, then basic, of course
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL);
        ClientResponse response = resource.get();
        assertEquals(200, response.getStatusCode());
    }
    
}
