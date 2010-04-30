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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests that the RestFilter will pick up on requests.
 */
public class PersonAppTest extends TestCase {

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI();
    }

    private HttpClient client;

    @Override
    public void setUp() {
        client = new HttpClient();
    }

    public void testPostPerson() throws HttpException, IOException {
        PostMethod postMethod = new PostMethod(getBaseURI() + "/person/abcd");
        postMethod
            .setRequestEntity(new StringRequestEntity("Hello", MediaType.TEXT_PLAIN, "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals(MediaType.TEXT_PLAIN, postMethod
                .getResponseHeader(HttpHeaders.CONTENT_TYPE).getValue());
            assertEquals("Person: abcd query parameter: defaultQuery matrix parameter: defaultMatrix entity: Hello",
                         postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    public void testPostPersonInXML() throws HttpException, IOException {
        PostMethod postMethod = new PostMethod(getBaseURI() + "/person/abcd");
        postMethod.setRequestEntity(new StringRequestEntity("Hello", MediaType.TEXT_XML, "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals(MediaType.TEXT_XML, postMethod.getResponseHeader(HttpHeaders.CONTENT_TYPE)
                .getValue());
            assertEquals("Person: abcd query parameter: defaultQuery matrix parameter: defaultMatrix entity: Hello",
                         postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

}
