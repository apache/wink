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

package org.apache.wink.test.filter.specificurl;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests that a specific URL pattern given will match only those requests.
 */
public class FilterSpecificURLPatternTest extends TestCase {

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI();
    }

    private HttpClient client;

    public void setUp() {
        client = new HttpClient();
    }

    public void testGetBook() throws HttpException, IOException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/book/book1");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertTrue(getMethod.getResponseBodyAsString(), getMethod.getResponseBodyAsString()
                .contains("book resource hit"));
        } finally {
            getMethod.releaseConnection();
        }
    }

}
