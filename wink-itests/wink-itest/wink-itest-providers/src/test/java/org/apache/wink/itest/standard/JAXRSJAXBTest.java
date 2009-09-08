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
package org.apache.wink.itest.standard;

import java.io.IOException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class JAXRSJAXBTest extends TestCase {

    public String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/standard";
    }

    /**
     * Tests sending in no request entity to a JAXB entity parameter.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testSendingNoRequestEntityJAXB() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        PostMethod postMethod = new PostMethod(getBaseURI() + "/providers/standard/jaxb/empty");
        postMethod.addRequestHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML);
        try {
            client.executeMethod(postMethod);
            assertEquals(400, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }
}
