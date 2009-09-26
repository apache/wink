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
package org.apache.wink.itest.version;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkClientVersioningTest extends TestCase {

    protected RestClient client;

    protected static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI() + "/taxform";
        }
        return ServerEnvironmentInfo.getBaseURI() + "/version" + "/taxform";
    }

    @Override
    public void setUp() {
        client = new RestClient();
    }

    public void testVersionByAccept() {
        ClientResponse response =
            client.resource(getBaseURI()).queryParam("form", "1040")
                .header("Accept", "application/taxform+2007").get();

        String responseBody = response.getEntity(String.class);
        assertTrue("Response does not contain expected 'deductions' element", responseBody
            .contains("<deductions>"));
        assertEquals(200, response.getStatusCode());

        response =
            client.resource(getBaseURI()).queryParam("form", "1040")
                .header("Accept", "application/taxform+2008").get();
        responseBody = response.getEntity(String.class);
        assertFalse("Response contains unexpected 'deductions' element", responseBody
            .contains("<deductions>"));
        assertEquals(200, response.getStatusCode());
    }

    public void testVersionByQueryString() {
        ClientResponse response =
            client.resource(getBaseURI() + "/1040").queryParam("version", "2007").get();
        String responseBody = response.getEntity(String.class);
        assertTrue("Response does not contain expected 'deductions' element", responseBody
            .contains("<deductions>"));
        assertEquals(200, response.getStatusCode());

        response = client.resource(getBaseURI() + "/1040").queryParam("version", "2008").get();
        responseBody = response.getEntity(String.class);
        assertFalse("Response contains unexpected 'deductions' element", responseBody
            .contains("<deductions>"));
        assertEquals(200, response.getStatusCode());
    }

    public void testVersionByPath() {
        ClientResponse response = client.resource(getBaseURI() + "/1040/2007").get();
        String responseBody = response.getEntity(String.class);
        assertTrue("Response does not contain expected 'deductions' element", responseBody
            .contains("<deductions>"));
        assertEquals(200, response.getStatusCode());

        response = client.resource(getBaseURI() + "/1040/2008").get();
        responseBody = response.getEntity(String.class);
        assertFalse("Response contains unexpected 'deductions' element", responseBody
            .contains("<deductions>"));
        assertEquals(200, response.getStatusCode());
    }

}
