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

package org.apache.wink.itest.writerexceptions;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyWriter;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.wink.itest.writerexceptions.IOExceptionMapper;
import org.apache.wink.itest.writerexceptions.NullPointerExceptionMapper;
import org.apache.wink.test.integration.ServerContainerAssertions;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class JAXRSMessageBodyWriterExceptionThrownTest extends TestCase {

    public String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/writerexceptions";
    }

    /**
     * Tests that an exception thrown from the
     * {@link MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method which is not mapped to an {@link ExceptionMapper} is still thrown.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testIsWritableExceptionThrownWhichIsNotMappedIsThrownOut() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "writable/throwruntime");
        try {
            client.executeMethod(postMethod);
            assertEquals(500, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that an exception thrown from the
     * {@link MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method which is mapped to an {@link ExceptionMapper} uses the mapper.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testIsWritableExceptionThrownWhichIsMappedUsesExceptionMapper()
        throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "writable/thrownull");
        try {
            client.executeMethod(postMethod);
            assertEquals(495, postMethod.getStatusCode());
            assertEquals("Invoked" + NullPointerExceptionMapper.class.getName(), postMethod
                .getResponseBodyAsString());
            assertEquals("text/plain", postMethod.getResponseHeader("Content-Type").getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link WebApplicationException} thrown from the
     * {@link MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method is correctly processed.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testIsWritableWebApplicationExceptionThrown() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "writable/throwwebapplicationexception");
        try {
            client.executeMethod(postMethod);
            assertEquals(499, postMethod.getStatusCode());
            assertEquals("can not write type", postMethod.getResponseBodyAsString());
            assertEquals("text/plain", postMethod.getResponseHeader("Content-Type").getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that an exception thrown from the
     * {@link MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method which is not mapped to an {@link ExceptionMapper} is still thrown.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetSizeExceptionThrownWhichIsNotMappedIsThrownOut() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "getsize/throwruntime");
        try {
            client.executeMethod(postMethod);
            assertEquals(500, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that an exception thrown from the
     * {@link MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method which is mapped to an {@link ExceptionMapper} uses the mapper.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetSizeExceptionThrownWhichIsMappedUsesExceptionMapper() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "getsize/thrownull");
        try {
            client.executeMethod(postMethod);
            assertEquals(495, postMethod.getStatusCode());
            assertEquals("Invoked" + NullPointerExceptionMapper.class.getName(), postMethod
                .getResponseBodyAsString());
            assertEquals("text/plain", postMethod.getResponseHeader("Content-Type").getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link WebApplicationException} thrown from the
     * {@link MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method is correctly processed.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetSizeWebApplicationExceptionThrown() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "getsize/throwwebapplicationexception");
        try {
            client.executeMethod(postMethod);
            assertEquals(499, postMethod.getStatusCode());
            assertEquals("can not write type", postMethod.getResponseBodyAsString());
            assertEquals("text/plain", postMethod.getResponseHeader("Content-Type").getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that an exception thrown from the
     * {@link MessageBodyWriter#writeTo(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)}
     * method which is not mapped to an {@link ExceptionMapper} is still thrown.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriteToExceptionThrownWhichIsNotMappedIsThrownOut() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "writeto/throwruntime");
        try {
            client.executeMethod(postMethod);
            assertEquals(500, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that an exception thrown from the
     * {@link MessageBodyWriter#writeTo(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)}
     * method which is mapped to an {@link ExceptionMapper} uses the mapper.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriteToExceptionThrownWhichIsMappedUsesExceptionMapper() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "writeto/thrownull");
        try {
            client.executeMethod(postMethod);
            assertEquals(495, postMethod.getStatusCode());
            assertEquals("Invoked" + NullPointerExceptionMapper.class.getName(), postMethod
                .getResponseBodyAsString());
            assertEquals("text/plain", postMethod.getResponseHeader("Content-Type").getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link WebApplicationException} thrown from the
     * {@link MessageBodyWriter#writeTo(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)}
     * method is correctly processed.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriteToWebApplicationExceptionThrown() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "writeto/throwwebapplicationexception");
        try {
            client.executeMethod(postMethod);
            assertEquals(498, postMethod.getStatusCode());
            assertEquals("can not write type in writeto", postMethod.getResponseBodyAsString());
            assertEquals("text/plain", postMethod.getResponseHeader("Content-Type").getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link IOException} thrown from the
     * {@link MessageBodyWriter#writeTo(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)}
     * method can be mapped correctly.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriteToIOExceptionThrown() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "writeto/throwioexception");
        try {
            client.executeMethod(postMethod);
            assertEquals(455, postMethod.getStatusCode());
            assertEquals("Invoked" + IOExceptionMapper.class.getName(), postMethod
                .getResponseBodyAsString());
            assertEquals("text/plain", postMethod.getResponseHeader("Content-Type").getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that an exception thrown from the
     * {@link MessageBodyWriter#writeTo(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)}
     * method thrown after the stream is committed is handled.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriteToExceptionThrownWhichIsNotMappedIsThrownOutAfterStreamCommitted()
        throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "writetoafterwritten/throwruntime");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
//            assertEquals("written", postMethod.getResponseBodyAsString());
            assertEquals("writetoafterwritten/throwruntime", postMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that an exception thrown from the
     * {@link MessageBodyWriter#writeTo(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)}
     * method which is mapped to an {@link ExceptionMapper} after the stream is
     * committed is handled.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriteToExceptionThrownWhichIsMappedUsesExceptionMapperAfterStreamCommitted()
        throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "writetoafterwritten/thrownull");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("written", postMethod.getResponseBodyAsString());
            assertEquals("writetoafterwritten/thrownull", postMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link WebApplicationException} thrown from the
     * {@link MessageBodyWriter#writeTo(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)}
     * method is correctly processed.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriteToWebApplicationExceptionThrownAfterStreamCommitted()
        throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "writetoafterwritten/throwwebapplicationexception");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("written", postMethod.getResponseBodyAsString());
            assertEquals("writetoafterwritten/throwwebapplicationexception", postMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link IOException} thrown from the
     * {@link MessageBodyWriter#writeTo(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)}
     * method thrown after the stream is committed is handled.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriteToIOExceptionThrownAfterStreamCommitted() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        postMethod.setRequestHeader("Accept", "writetoafterwritten/throwioexception");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("written", postMethod.getResponseBodyAsString());
            assertEquals("writetoafterwritten/throwioexception", postMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a 500 error is returned when a {@link MessageBodyWriter}
     * cannot be found.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void test500IfWriterNotFound() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/writer/messagebodywriterexceptions");
        getMethod.setRequestHeader("Accept", "abcd/efgh");
        try {
            client.executeMethod(getMethod);
            assertEquals(500, getMethod.getStatusCode());
            ServerContainerAssertions.assertExceptionBodyFromServer(500, getMethod
                .getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }
}
