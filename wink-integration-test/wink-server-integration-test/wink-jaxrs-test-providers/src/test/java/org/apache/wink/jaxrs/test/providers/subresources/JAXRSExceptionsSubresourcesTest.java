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

package org.apache.wink.jaxrs.test.providers.subresources;

import javax.ws.rs.core.Response.Status;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class JAXRSExceptionsSubresourcesTest extends TestCase {

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/subresourceexceptions/guestbooksubresources";
    }

    /**
     * Test the positive workflow where a comment with a message and author is
     * successfully posted to the Guestbook.
     * 
     * @throws Exception
     */
    public void testRegularWorkflow() throws Exception {
        HttpClient client = new HttpClient();
        PostMethod postMethod = new PostMethod(getBaseURI() + "/commentdata");
        try {
            postMethod
                .setRequestEntity(new StringRequestEntity(
                                                          "<comment><id>10000</id><author>Anonymous</author><message>Hi there</message></comment>",
                                                          "text/xml", null));
            client.executeMethod(postMethod);
            assertEquals(201, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
        String postURILocation = postMethod.getResponseHeader("Location").getValue();

        GetMethod getMethod = new GetMethod(postURILocation);
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><comment><author>Anonymous</author><id>10000</id><message>Hi there</message></comment>",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Test that a <code>WebApplicationException</code> thrown from a
     * sub-resource is still processed properly.
     * 
     * @throws Exception
     */
    public void testWebApplicationException() throws Exception {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI() + "/commentdata");
        try {
            postMethod.setRequestEntity(new StringRequestEntity("<comment></comment>", "text/xml",
                                                                null));
//            postMethod.addRequestHeader("Accept", "text/xml");
            client.executeMethod(postMethod);
            assertEquals(Status.BAD_REQUEST.getStatusCode(), postMethod.getStatusCode());
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><commenterror><message>Please include a comment ID, a message, and your name.</message></commenterror>",
                         postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Test that a checked exception is processed properly.
     * 
     * @throws Exception
     */
    public void testCheckedException() throws Exception {
        HttpClient client = new HttpClient();

        PutMethod putMethod = new PutMethod(getBaseURI() + "/commentdata");
        try {
            putMethod.setRequestEntity(new StringRequestEntity("<comment></comment>", "text/xml",
                                                               null));
            client.executeMethod(putMethod);
            assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), putMethod.getStatusCode());
            // assertLogContainsException("jaxrs.tests.exceptions.subresources.server.GuestbookException: Unexpected ID.");
        } finally {
            putMethod.releaseConnection();
        }
    }

    /**
     * Test the positive workflow where a comment with a message and author is
     * successfully posted to the Guestbook.
     * 
     * @throws Exception
     */
    public void testRuntimeException() throws Exception {
        HttpClient client = new HttpClient();
        DeleteMethod deleteMethod = new DeleteMethod(getBaseURI() + "/commentdata/afdsfsdf");
        try {
            client.executeMethod(deleteMethod);
            assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), deleteMethod.getStatusCode());
            // assertLogContainsException("java.lang.NumberFormatException: For input string: \"afdsfsdf\"");
        } finally {
            deleteMethod.releaseConnection();
        }
    }
}
