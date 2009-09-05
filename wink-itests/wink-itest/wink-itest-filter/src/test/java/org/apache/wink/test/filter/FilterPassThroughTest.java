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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests that requests not filtered through the RestFilter pass through so that
 * JSPs can be used.
 */
public class FilterPassThroughTest extends TestCase {

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI();
    }

    private HttpClient client;

    public void setUp() {
        client = new HttpClient();
    }

    public void testGetJSPPage() throws HttpException, IOException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/testpage.jsp");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertTrue(getMethod.getResponseBodyAsString(), getMethod.getResponseBodyAsString()
                .contains("<html><body><h2>Hit the test page!</h2></body></html>"));
        } finally {
            getMethod.releaseConnection();
        }
    }

    public void testGetSubdirectoryJSPPage() throws HttpException, IOException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/testing/index.jsp");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertTrue(getMethod.getResponseBodyAsString(), getMethod.getResponseBodyAsString()
                .contains("<html><body><h2>Hit the testing test page!</h2></body></html>"));
        } finally {
            getMethod.releaseConnection();
        }
    }

}
