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
package org.apache.wink.itest.readerexceptions;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.itest.readerexceptions.IOExceptionMapper;
import org.apache.wink.itest.readerexceptions.NullPointerExceptionMapper;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class JAXRSMessageBodyReaderExceptionsTest extends TestCase {

    public String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/readerexceptions";
    }

    /**
     * Tests that an exception thrown from the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method which is not mapped to an {@link ExceptionMapper} is still thrown.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testIsReadableExceptionThrownWhichIsNotMappedIsThrownOut() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/messagebodyreaderexceptions");
        postMethod.setRequestEntity(new StringRequestEntity("ignored input",
                                                            "readable/throwruntime", "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(500, postMethod.getStatusCode());
            // assertLogContainsException("javax.servlet.ServletException");
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that an exception thrown from the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method which is mapped to an {@link ExceptionMapper} uses the mapper.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testIsReadableExceptionThrownWhichIsMappedUsesExceptionMapper()
        throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/messagebodyreaderexceptions");
        postMethod.setRequestEntity(new StringRequestEntity("ignored input", "readable/thrownull",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(495, postMethod.getStatusCode());
            assertEquals("Invoked" + NullPointerExceptionMapper.class.getName(), postMethod
                .getResponseBodyAsString());
            assertEquals("text/plain", postMethod.getResponseHeader("Content-Type").getValue());
            // assertLogContainsException("NullPointerException");
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link WebApplicationException} thrown from the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method is correctly processed.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testIsReadableWebApplicationExceptionThrown() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/messagebodyreaderexceptions");
        postMethod
            .setRequestEntity(new StringRequestEntity("ignored input",
                                                      "readable/throwwebapplicationexception",
                                                      "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(499, postMethod.getStatusCode());
            assertEquals("can not read type", postMethod.getResponseBodyAsString());
            // assertEquals("application/octet-stream",
            // postMethod.getResponseHeader("Content-Type").getValue());
            // assertLogContainsException("WebApplicationException");
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that an exception thrown from the
     * {@link MessageBodyReader#readFrom(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)}
     * method which is not mapped to an {@link ExceptionMapper} is still thrown.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReadFromExceptionThrownWhichIsNotMappedIsThrownOut() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/messagebodyreaderexceptions");
        postMethod.setRequestEntity(new StringRequestEntity("ignored input",
                                                            "readfrom/throwruntime", "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(500, postMethod.getStatusCode());
            // assertLogContainsException("javax.servlet.ServletException");
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that an exception thrown from the
     * {@link MessageBodyReader#readFrom(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)}
     * method which is mapped to an {@link ExceptionMapper} uses the mapper.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReadFromExceptionThrownWhichIsMappedUsesExceptionMapper() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/messagebodyreaderexceptions");
        postMethod.setRequestEntity(new StringRequestEntity("ignored input", "readfrom/thrownull",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(495, postMethod.getStatusCode());
            assertEquals("Invoked" + NullPointerExceptionMapper.class.getName(), postMethod
                .getResponseBodyAsString());
            assertEquals("text/plain", postMethod.getResponseHeader("Content-Type").getValue());
            // assertLogContainsException("NullPointerException");
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link WebApplicationException} thrown from the
     * {@link MessageBodyReader#readFrom(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)}
     * method is correctly processed.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReadFromWebApplicationExceptionThrown() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/messagebodyreaderexceptions");
        postMethod
            .setRequestEntity(new StringRequestEntity("ignored input",
                                                      "readfrom/throwwebapplicationexception",
                                                      "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(498, postMethod.getStatusCode());
            assertEquals("can not read type in readfrom", postMethod.getResponseBodyAsString());
            // assertEquals("application/octet-stream",
            // postMethod.getResponseHeader("Content-Type").getValue());
            // assertLogContainsException("WebApplicationException");
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link IOException} thrown from the
     * {@link MessageBodyReader#readFrom(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)}
     * method can be mapped correctly.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReadFromIOExceptionThrown() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/messagebodyreaderexceptions");
        postMethod.setRequestEntity(new StringRequestEntity("ignored input",
                                                            "readfrom/throwioexception", "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(455, postMethod.getStatusCode());
            assertEquals("Invoked" + IOExceptionMapper.class.getName(), postMethod
                .getResponseBodyAsString());
            // assertEquals("application/octet-stream",
            // postMethod.getResponseHeader("Content-Type").getValue());
            // assertLogContainsException("IOException");
        } finally {
            postMethod.releaseConnection();
        }
    }
}
