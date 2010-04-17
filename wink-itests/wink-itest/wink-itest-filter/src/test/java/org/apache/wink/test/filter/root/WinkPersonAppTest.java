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

package org.apache.wink.test.filter.root;

import java.io.IOException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpException;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class WinkPersonAppTest extends TestCase {

    protected static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI();
    }

    protected RestClient client;

    @Override
    public void setUp() {
        client = new RestClient();
    }

    public void testPostPerson() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/person/abcd").contentType(MediaType.TEXT_PLAIN)
                .post("Hello");
        assertEquals(200, response.getStatusCode());
        assertEquals(MediaType.TEXT_PLAIN + ";charset=UTF-8", response.getHeaders()
            .getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals("Person: abcd query parameter: defaultQuery matrix parameter: defaultMatrix entity: Hello",
                     response.getEntity(String.class));
    }

    public void testPostPersonInXML() throws HttpException, IOException {
        ClientResponse response =
            client.resource(getBaseURI() + "/person/abcd").contentType(MediaType.TEXT_XML)
                .post("Hello");
        assertEquals(200, response.getStatusCode());
        assertEquals(MediaType.TEXT_XML + ";charset=UTF-8", response.getHeaders()
            .getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals("Person: abcd query parameter: defaultQuery matrix parameter: defaultMatrix entity: Hello",
                     response.getEntity(String.class));
    }
}
