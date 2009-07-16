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

package org.apache.wink.jaxrs.test.largeentity;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class LargeEntityTest extends TestCase {

    public static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/largeentity";
    }

    /**
     * Tests sending a large string. Possible failures including the servlet
     * request buffer being too small, so the status headers do not get set
     * correctly since the message body will have to be flushed out of the
     * servlet response buffer.
     * 
     * @throws Exception
     */
    public void testSendLargeString() throws Exception {
        PostMethod postMethod = new PostMethod(getBaseURI() + "/large");
        HttpClient client = new HttpClient();
        try {
            StringBuffer sb = new StringBuffer();
            for (long c = 0; c < 5000000; ++c) {
                sb.append("a");
            }
            postMethod.setRequestEntity(new StringRequestEntity(sb.toString(), "text/xml", null));
            client.executeMethod(postMethod);
            assertEquals(277, postMethod.getStatusCode());
            Header header = postMethod.getResponseHeader("appendStringsHeader");
            assertNotNull(header);
            assertEquals(sb.subSequence(0, 2042) + "header", header.getValue());
            String resp = postMethod.getResponseBodyAsString();
            assertEquals(sb.toString() + "entity", resp);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests sending a JAR file.
     * 
     * @throws Exception
     */
    public void testSendJAR() throws Exception {
        PostMethod postMethod = new PostMethod(getBaseURI() + "/large/zip");
        HttpClient client = new HttpClient();
        try {
            System.out
                .println(new File(
                                  ServerEnvironmentInfo.getWorkDir() + "/wink-jaxrs-test-targetting-0.1-SNAPSHOT.war")
                    .getAbsoluteFile().getAbsolutePath());
            postMethod
                .setRequestEntity(new FileRequestEntity(
                                                        new File(
                                  ServerEnvironmentInfo.getWorkDir() + "/wink-jaxrs-test-targetting-0.1-SNAPSHOT.war"),
                                                        "application/jar"));
            client.executeMethod(postMethod);
            assertEquals(290, postMethod.getStatusCode());
            String resp = postMethod.getResponseBodyAsString();
            assertEquals("META-INF/DEPENDENCIES", resp);
        } finally {
            postMethod.releaseConnection();
        }
    }
}
