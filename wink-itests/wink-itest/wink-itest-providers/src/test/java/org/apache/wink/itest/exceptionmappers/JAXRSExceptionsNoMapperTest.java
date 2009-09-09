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

package org.apache.wink.itest.exceptionmappers;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.itest.exceptionmappers.nomapper.Comment;
import org.apache.wink.itest.exceptionmappers.nomapper.CommentError;
import org.apache.wink.test.integration.ServerContainerAssertions;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests exception throwing without any exception mapping providers.
 */
public class JAXRSExceptionsNoMapperTest extends TestCase {

    public String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI() + "/guestbooknomap";
        }
        return ServerEnvironmentInfo.getBaseURI() + "/exceptionsnomapper" + "/guestbooknomap";
    }

    /**
     * Test the positive workflow where a comment with a message and author is
     * successfully posted to the Guestbook.
     * 
     * @throws Exception
     */
    public void testRegularWorkflow() throws Exception {
        /* FIXME: this is not a repeatable test */
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI() + "/clear");
        client.executeMethod(postMethod);
        assertEquals(204, postMethod.getStatusCode());

        postMethod = new PostMethod(getBaseURI());
        postMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "<comment><message>Hello World!</message><author>Anonymous</author></comment>",
                                                      "text/xml", null));
        client.executeMethod(postMethod);
        assertEquals(201, postMethod.getStatusCode());
        String newPostURILocation = postMethod.getResponseHeader("Location").getValue();

        GetMethod getMethod = new GetMethod(newPostURILocation);
        client.executeMethod(getMethod);
        assertEquals(200, getMethod.getStatusCode());

        Comment c =
            (Comment)JAXBContext.newInstance(Comment.class.getPackage().getName())
                .createUnmarshaller().unmarshal(getMethod.getResponseBodyAsStream());
        assertEquals("Anonymous", c.getAuthor());
        assertEquals(1, c.getId().intValue());
        assertEquals("Hello World!", c.getMessage());
    }

    /**
     * Tests a method that throws an emptily constructed
     * <code>WebApplicationException</code>.
     * 
     * @throws Exception
     */
    public void testWebApplicationExceptionDefaultNoMappingProvider() throws Exception {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI());
        postMethod
            .setRequestEntity(new StringRequestEntity("<comment></comment>", "text/xml", null));
        client.executeMethod(postMethod);
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), postMethod.getStatusCode());
        ServerContainerAssertions.assertExceptionBodyFromServer(500, postMethod
            .getResponseBodyAsString());
    }

    /**
     * Tests a method that throws a <code>WebApplicationException</code> with an
     * integer status code.
     * 
     * @throws Exception
     */
    public void testWebApplicationExceptionStatusCodeSetNoMappingProvider() throws Exception {
        HttpClient client = new HttpClient();
        PostMethod postMethod = new PostMethod(getBaseURI());
        postMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "<comment><message>Suppose to fail with missing author.</message></comment>",
                                                      "text/xml", null));
        client.executeMethod(postMethod);
        assertEquals(499, postMethod.getStatusCode());
        ServerContainerAssertions.assertExceptionBodyFromServer(499, postMethod
            .getResponseBodyAsString());

    }

    /**
     * Tests a method that throws a <code>WebApplicationException</code> with a
     * Response.Status set.
     * 
     * @throws Exception
     */
    public void testWebApplicationExceptionResponseStatusSetNoMappingProvider() throws Exception {
        HttpClient client = new HttpClient();
        PostMethod postMethod = new PostMethod(getBaseURI());
        postMethod.setRequestEntity(new StringRequestEntity("", "text/xml", null));
        client.executeMethod(postMethod);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), postMethod.getStatusCode());
        ServerContainerAssertions.assertExceptionBodyFromServer(400, postMethod
            .getResponseBodyAsString());

    }

    /**
     * Tests a method that throws a <code>WebApplicationException</code> with a
     * Response.
     * 
     * @throws Exception
     */
    public void testWebApplicationExceptionResponseSetNoMappingProvider() throws Exception {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI());
        postMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "<comment><author>Anonymous</author></comment>",
                                                      "text/xml", null));
        client.executeMethod(postMethod);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), postMethod.getStatusCode());

        CommentError c =
            (CommentError)JAXBContext.newInstance(CommentError.class.getPackage().getName())
                .createUnmarshaller().unmarshal(postMethod.getResponseBodyAsStream());
        assertEquals("Missing the message in the comment.", c.getErrorMessage());
    }

    /**
     * Tests a method that throws a subclass of
     * <code>WebApplicationException</code> with a Response.
     * 
     * @throws Exception
     */
    public void testCustomWebApplicationExceptionNoMappingProvider() throws Exception {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI());
        postMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "<comment><message></message><author></author></comment>",
                                                      "text/xml", null));
        client.executeMethod(postMethod);
        assertEquals(498, postMethod.getStatusCode());

        CommentError c =
            (CommentError)JAXBContext.newInstance(CommentError.class.getPackage().getName())
                .createUnmarshaller().unmarshal(postMethod.getResponseBodyAsStream());
        assertEquals("Cannot post an invalid message.", c.getErrorMessage());
    }

    /**
     * Tests a method that throws a runtime exception.
     * 
     * @throws Exception
     */
    public void testRuntimeExceptionNoMappingProvider() throws Exception {
        HttpClient client = new HttpClient();

        /*
         * abcd is an invalid ID so a NumberFormatException will be thrown in
         * the resource
         */
        DeleteMethod postMethod = new DeleteMethod(getBaseURI() + "/abcd");
        client.executeMethod(postMethod);
        assertEquals(500, postMethod.getStatusCode());

        // assertLogContainsException("java.lang.NumberFormatException: For input string: \"abcd\"");
    }

    /**
     * Tests a method that throws a NullPointerException inside a called method.
     * 
     * @throws Exception
     */
    public void testNullPointerExceptionNoMappingProvider() throws Exception {
        HttpClient client = new HttpClient();

        DeleteMethod postMethod = new DeleteMethod(getBaseURI() + "/10000");
        client.executeMethod(postMethod);
        assertEquals(500, postMethod.getStatusCode());

        // assertLogContainsException("java.lang.NullPointerException: The comment did not previously exist.");
    }

    /**
     * Tests a method that throws an error.
     * 
     * @throws Exception
     */
    public void testErrorNoMappingProvider() throws Exception {
        HttpClient client = new HttpClient();

        DeleteMethod postMethod = new DeleteMethod(getBaseURI() + "/-99999");
        client.executeMethod(postMethod);
        assertEquals(500, postMethod.getStatusCode());
        // assertLogContainsException("java.lang.Error: Simulated error");
    }

    /**
     * Tests a method that throws a checked exception.
     * 
     * @throws Exception
     */
    public void testCheckExceptionNoMappingProvider() throws Exception {
        HttpClient client = new HttpClient();

        PutMethod putMethod = new PutMethod(getBaseURI() + "/-99999");
        putMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "<comment><id></id><message></message><author></author></comment>",
                                                      "text/xml", null));
        client.executeMethod(putMethod);
        assertEquals(500, putMethod.getStatusCode());
        // assertLogContainsException("jaxrs.tests.exceptions.nomapping.server.GuestbookException: Unexpected ID.");
    }
}
