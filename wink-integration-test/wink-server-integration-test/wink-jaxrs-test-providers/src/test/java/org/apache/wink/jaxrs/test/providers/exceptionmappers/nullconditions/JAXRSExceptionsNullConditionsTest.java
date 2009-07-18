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

package org.apache.wink.jaxrs.test.providers.exceptionmappers.nullconditions;

import javax.ws.rs.core.Response;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.wink.test.integration.ServerContainerAssertions;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests various default null conditions and error paths when exceptions are
 * thrown in resource methods.
 */
public class JAXRSExceptionsNullConditionsTest extends TestCase {

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/exceptionsnull" + "/guestbooknullconditions";
    }

    /**
     * Tests that an empty constructor constructed
     * <code>WebApplicationException</code> will return status 500 and no
     * response body by default.
     * 
     * @throws Exception
     */
    public void testEmptyWebException() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/emptywebappexception");
        try {
            client.executeMethod(getMethod);
            assertEquals(500, getMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(500, getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a <code>WebApplicationException</code> constructed with a
     * cause will return status 500 and no response body by default.
     * 
     * @throws Exception
     */
    public void testWebExceptionWithCause() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/webappexceptionwithcause");
        try {
            client.executeMethod(getMethod);
            assertEquals(500, getMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(500, getMethod
                                                                    .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a <code>WebApplicationException</code> constructed with a
     * cause and status will return status and no response body by default.
     * 
     * @throws Exception
     */
    public void testWebExceptionWithCauseAndStatus() throws Exception {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI() + "/webappexceptionwithcauseandstatus");
        try {
            client.executeMethod(postMethod);
            assertEquals(499, postMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(499, postMethod
                                                                    .getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a <code>WebApplicationException</code> constructed with a
     * cause and response will return the Response entity by default.
     * 
     * @throws Exception
     */
    public void testWebExceptionWithCauseAndResponse() throws Exception {
        HttpClient client = new HttpClient();

        PutMethod putMethod = new PutMethod(getBaseURI() + "/webappexceptionwithcauseandresponse");
        try {
            client.executeMethod(putMethod);
            assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), putMethod.getStatusCode());
            assertEquals("Entity inside response", putMethod.getResponseBodyAsString());
        } finally {
            putMethod.releaseConnection();
        }
    }

    /**
     * Tests that a <code>WebApplicationException</code> constructed with a
     * cause and response status will return the response status and empty
     * response body by default.
     * 
     * @throws Exception
     */
    public void testWebExceptionWithCauseAndResponseStatus() throws Exception {
        HttpClient client = new HttpClient();

        DeleteMethod deleteMethod =
            new DeleteMethod(getBaseURI() + "/webappexceptionwithcauseandresponsestatus");
        try {
            client.executeMethod(deleteMethod);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), deleteMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(400, deleteMethod
                                                                    .getResponseBodyAsString());
        } finally {
            deleteMethod.releaseConnection();
        }
    }

    /**
     * Tests that a <code>ExceptionMapper</code> that returns null should see a
     * HTTP 204 status.
     * 
     * @throws Exception
     */
    public void testExceptionMapperReturnNull() throws Exception {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/exceptionmappernull");
        try {
            client.executeMethod(getMethod);
            assertEquals(Response.Status.NO_CONTENT.getStatusCode(), getMethod.getStatusCode());
            assertEquals(null, getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a <code>ExceptionMapper</code> that throws an exception or
     * error should see a HTTP 500 status error and empty response.
     * 
     * @throws Exception
     */
    public void testExceptionMapperThrowsException() throws Exception {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI() + "/exceptionmapperthrowsexception");
        try {
            client.executeMethod(postMethod);
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), postMethod
                .getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(500, postMethod
                                                                    .getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a <code>ExceptionMapper</code> that throws an error should see
     * a HTTP 500 status error and unknown response.
     * 
     * @throws Exception
     */
    public void testExceptionMapperThrowsError() throws Exception {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI() + "/exceptionmapperthrowserror");
        try {
            client.executeMethod(postMethod);
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), postMethod
                .getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a <code>ExceptionMapper</code> can catch a generic Throwable.
     * 
     * @throws Exception
     */
    public void testExceptionMapperForSpecificThrowable() throws Exception {
        HttpClient client = new HttpClient();

        PutMethod putMethod = new PutMethod(getBaseURI() + "/throwableexceptionmapper");
        try {
            client.executeMethod(putMethod);
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), putMethod
                .getStatusCode());
            assertEquals("Throwable mapper used", putMethod.getResponseBodyAsString());
        } finally {
            putMethod.releaseConnection();
        }
    }

    /**
     * Tests that a Throwable can propagate throughout the code.
     * 
     * @throws Exception
     */
    public void testThrowableCanBeThrown() throws Exception {
        HttpClient client = new HttpClient();

        DeleteMethod deleteMethod = new DeleteMethod(getBaseURI() + "/throwsthrowable");
        try {
            client.executeMethod(deleteMethod);
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), deleteMethod
                .getStatusCode());
            // assertLogContainsException("jaxrs.tests.exceptions.nullconditions.server.GuestbookResource$1: Throwable was thrown");
        } finally {
            deleteMethod.releaseConnection();
        }
    }
}
