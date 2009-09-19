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

package org.apache.wink.test.filter;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpException;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkFilterPassThroughTest extends TestCase {

    private static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI();
    }

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    public void testGetJSPPage() throws HttpException, IOException {
        ClientResponse response = client.resource(getBaseURI() + "/testpage.jsp").get();
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getEntity(String.class), response.getEntity(String.class)
            .contains("<html><body><h2>Hit the test page!</h2></body></html>"));
    }

    public void testGetSubdirectoryJSPPage() throws HttpException, IOException {
        ClientResponse response = client.resource(getBaseURI() + "/testing/index.jsp").get();
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getEntity(String.class), response.getEntity(String.class)
            .contains("<html><body><h2>Hit the testing test page!</h2></body></html>"));
    }

}
