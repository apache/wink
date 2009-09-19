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

package org.apache.wink.itest.uriinfo;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpException;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkURIInfoInjectionTest extends TestCase {

    private static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/uriinfo";
    }

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    /**
     * Tests that a URIInfo object is injected into method parameters.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testURIInfoParamInjection() throws HttpException, IOException {
        ClientResponse response = client.resource(getBaseURI() + "/context/uriinfo/param").get();
        assertEquals(204, response.getStatusCode());
    }

    /**
     * Tests that a URIInfo object is injected via a bean method.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testURIInfoBeanMethodInjection() throws HttpException, IOException {
        ClientResponse response = client.resource(getBaseURI() + "/context/uriinfo/bean").get();
        assertEquals(204, response.getStatusCode());
    }

    /**
     * Tests that a URIInfo object is injected via a constructor parameter.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testURIInfoConstructorInjection() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/context/uriinfo/constructor").get();
        assertEquals(204, response.getStatusCode());
    }

    /**
     * Tests that a URIInfo object is injected via a field member.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testURIInfoFieldMemberInjection() throws HttpException, IOException {
        ClientResponse response = client.resource(getBaseURI() + "/context/uriinfo/field").get();
        assertEquals(204, response.getStatusCode());
    }

    /**
     * Tests that a URIInfo object is not injected via non bean methods.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testURIInfoNotBeanMethod() throws HttpException, IOException {
        ClientResponse response = client.resource(getBaseURI() + "/context/uriinfo/notbeanmethod").get();
        assertEquals(204, response.getStatusCode());
    }

}
