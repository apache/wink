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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public abstract class BaseTest extends TestCase {

    public static String  SERVICE_URL      = "http://localhost:{0}/some/service";
    public static String  RECEIVED_MESSAGE = "received message";
    public static String  SENT_MESSAGE     = "sent message";

    // private static int SERVER_PORT = 3456;
    public String         serviceURL;
    public MockHttpServer server           = null;
    public int            serverPort;

    @Override
    protected void setUp() throws Exception {
        server = startMockHttpServer();
        serviceURL = "http://localhost:" + String.valueOf(serverPort) + "/some/service";
    }

    @Override
    protected void tearDown() throws Exception {
        if (server != null) {
            server.stopServer();
        }
    }

    private MockHttpServer startMockHttpServer() {
        MockHttpServer server = new MockHttpServer(34567);
        serverPort = server.getServerPort();
        server.setMockResponseContent(RECEIVED_MESSAGE);
        server.setMockResponseHeaders(getMockResponseHeaders());
        server.startServer();
        return server;
    }

    private Map<String, String> getMockResponseHeaders() {
        Map<String, String> map = new HashMap<String, String>();
        return map;
    }

}
