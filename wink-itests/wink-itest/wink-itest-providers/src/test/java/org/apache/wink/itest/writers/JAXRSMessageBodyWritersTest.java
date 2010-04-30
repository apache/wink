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
package org.apache.wink.itest.writers;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class JAXRSMessageBodyWritersTest extends TestCase {

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/writers";
    }

    /**
     * Tests that if a {@link Response} object sets its media type, it is passed
     * correctly to the {@link MessageBodyWriter}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReturnContentTypeSetOnResponse() throws HttpException, IOException {
        /*
         * and maybe the content type isn't supported by the writer i.e.
         * text/abcd and String
         */
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/classtype?type=sourcecontenttype");
        try {
            client.executeMethod(getMethod);

            assertEquals(500, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/classtype?type=source");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertTrue(getMethod.getResponseBodyAsString(), getMethod.getResponseBodyAsString()
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\""));
            assertEquals("text/xml", getMethod.getResponseHeader("Content-Type").getValue());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/classtype?type=stringcontenttype");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("str:foobarcontenttype", getMethod.getResponseBodyAsString());

            String contentType =
                (getMethod.getResponseHeader("Content-Type") == null) ? null : getMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
            assertEquals("21", getMethod.getResponseHeader("Content-Length").getValue());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/classtype?type=string");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("str:foobar", getMethod.getResponseBodyAsString());

            String contentType =
                (getMethod.getResponseHeader("Content-Type") == null) ? null : getMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
            assertEquals("10", getMethod.getResponseHeader("Content-Length").getValue());
        } finally {
            getMethod.releaseConnection();
        }
    }

    //
    // public void testMediaTypeIntersection() {
    // fail();
    // }
    //
    // public void
    // testMediaTypeCanBePassedThroughWithoutAddedOrRemovedParameters() {
    // fail();
    // }
    //
    // public void testProducesOnClassInsteadOfMethod() {
    // fail();
    // }

    /**
     * Tests that
     * {@link MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * if the Content-Length given is less than what is actually written, that
     * the full content is still sent.
     */
    public void testContentLengthLessThanWritten() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/contentlength?mt=length/shorter");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            InputStream is = getMethod.getResponseBodyAsStream();
            int read = is.read();
            for (int c = 0; read != -1; ++c, read = is.read()) {
                assertEquals(c % 256, read);
            }
            assertEquals("length/shorter", getMethod.getResponseHeader("Content-Type").getValue());
            assertEquals("99990", getMethod.getResponseHeader("Content-Length").getValue());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * if the Content-Length given is less than what is actually written, that
     * the full content is still sent.
     */
    public void testContentLengthClassCorrect() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/contentlength?class=Vector");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("vector:HelloThere", getMethod.getResponseBodyAsString());

            String contentType =
                (getMethod.getResponseHeader("Content-Type") == null) ? null : getMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
            assertEquals("17", getMethod.getResponseHeader("Content-Length").getValue());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * uses the correct generic type.
     */
    public void testContentLengthGenericEntityCorrect() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/contentlength?class=ListInteger");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("listinteger:12", getMethod.getResponseBodyAsString());

            String contentType =
                (getMethod.getResponseHeader("Content-Type") == null) ? null : getMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
            assertEquals("14", getMethod.getResponseHeader("Content-Length").getValue());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * uses the correct generic type.
     */
    public void testContentLengthAnnotationsCorrect() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/contentlength?class=String");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("string:hello there", getMethod.getResponseBodyAsString());

            String contentType =
                (getMethod.getResponseHeader("Content-Type") == null) ? null : getMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
            assertEquals("18", getMethod.getResponseHeader("Content-Length").getValue());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * if the Content-Length given is greater than what is actually written,
     * that the full content is still sent.
     */
    public void testContentLengthGreaterThanWritten() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/contentlength?mt=length/longer");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());

            InputStream is = getMethod.getResponseBodyAsStream();
            int read = is.read();
            for (int c = 0; read != -1; ++c, read = is.read()) {
                assertEquals(c % 256, read);
            }
            assertEquals("length/longer", getMethod.getResponseHeader("Content-Type").getValue());
            assertEquals("100010", getMethod.getResponseHeader("Content-Length").getValue());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link MessageBodyWriter#getSize(Object, Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * if the Content-Length given is -1 than the Content-Length is not sent.
     */
    public void testLessThanNegativeOneContentLength() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/contentlength");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            InputStream is = getMethod.getResponseBodyAsStream();
            int read = is.read();
            for (int c = 0; read != -1; ++c, read = is.read()) {
                assertEquals(c % 256, read);
            }

            String contentType =
                (getMethod.getResponseHeader("Content-Type") == null) ? null : getMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
            assertNull((getMethod.getResponseHeader("Content-Length") == null) ? "" : getMethod
                .getResponseHeader("Content-Length").getValue(), getMethod
                .getResponseHeader("Content-Length"));
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * */
    public void testGiveAcceptTypeWildcardGetConcreteTypeBack() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/concretetype");
        getMethod.addRequestHeader("Accept", "*/*");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("Hello there", getMethod.getResponseBodyAsString());

            String contentType =
                (getMethod.getResponseHeader("Content-Type") == null) ? null : getMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/concretetype");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("Hello there", getMethod.getResponseBodyAsString());

            String contentType =
                (getMethod.getResponseHeader("Content-Type") == null) ? null : getMethod
                    .getResponseHeader("Content-Type").getValue();
            assertNotNull(contentType, contentType);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/concretetype");
        getMethod.addRequestHeader("Accept", "text/plain");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("Hello there", getMethod.getResponseBodyAsString());
            assertEquals("text/plain" + ";charset=UTF-8", getMethod
                .getResponseHeader("Content-Type").getValue());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/concretetype");
        getMethod.addRequestHeader("Accept", "text/xml");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("Hello there", getMethod.getResponseBodyAsString());
            assertEquals("text/xml" + ";charset=UTF-8", getMethod.getResponseHeader("Content-Type")
                .getValue());
        } finally {
            getMethod.releaseConnection();
        }
    }

    //
    // public void testIsWritableReturnRealTypeInMethod() {
    // fail();
    // }
    //
    // public void testIsWritableReturnGenericEntityTypeInMethod() {
    // fail();
    // }
    //
    // public void testIsWritableReturnGenericTypeInResponse() {
    // fail();
    // }
    //
    // public void testIsWritableReturnRealTypeInResponse() {
    // fail();
    // }

    /**
     * Tests that the
     * {@link MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method receives the correct class type. The test should receive an error
     * 500 because no writer could be found for the type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriterIsWritableUnexpectedClassType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/classtype?type=mytype");
        getMethod.addRequestHeader(HttpHeaders.ACCEPT, "application/json");

        try {
            client.executeMethod(getMethod);

            assertEquals(500, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method receives the correct class type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriterIsWritableExpectedClassType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/classtype?type=deque");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("deque:str:foostr:bar", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method returns true when the expected argument type is specified on the
     * generic type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriterIsWritableGenericEntityTypeCorrect() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/genericentity?query=setstring");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("set<string>:helloworld", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/genericentity?query=setinteger");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("set<integer>:12", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/nogenericentity?query=setstring");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("set:helloworld", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/nogenericentity?query=setinteger");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("set:12", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method returns false when an unexpected argument type is specified on the
     * generic type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriterIsWritableGenericEntityTypeIncorrect() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/genericentity?query=setshort");
        postMethod.addRequestHeader(HttpHeaders.ACCEPT, "application/json");
        try {
            client.executeMethod(postMethod);
            assertEquals(500, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/nogenericentity?query=setshort");
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("set:12", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method is passed a single annotation.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriterIsWritableNotAnnotated() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/notannotated");
        getMethod.addRequestHeader(HttpHeaders.ACCEPT, "application/json");
        try {
            client.executeMethod(getMethod);

            assertEquals(500, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method is passed a single annotation.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriterIsWritableAnnotated() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/annotated");
        try {
            client.executeMethod(getMethod);

            assertEquals(200, getMethod.getStatusCode());
            assertEquals("getannotation:foobar", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/annotated");
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            assertEquals("postannotation:foobar", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method is passed an incompatiable media type and does not return true.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriterIsWritableIncorrectMediaType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/mediatype?mt=custom/incorrect");
        try {
            client.executeMethod(postMethod);

            assertEquals(500, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method is passed the expected media type and reads the data.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriterIsWritableCorrectMediaType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/mediatype?mt=custom/correct");
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            assertEquals("mediatype:foo=bar", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * When a {@link RuntimeException} is propagated back from
     * {@link MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * , verify that the exception is handled appropriately.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testWriterIsWritableThrowsRuntimeException() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/throwsexception?mt=throw/runtime");
        try {
            client.executeMethod(postMethod);

            assertEquals(500, postMethod.getStatusCode());
            // assertLogContainsException("javax.servlet.ServletException");
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/throwsexception?mt=throw/nullpointer");
        try {
            client.executeMethod(postMethod);

            assertEquals(500, postMethod.getStatusCode());
            // assertLogContainsException("javax.servlet.ServletException");
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * When a {@link WebApplicationException} is propagated back from
     * {@link MessageBodyWriter#isWriteable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * , verify that the exception is handled appropriately.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testWriterIsWritableThrowsWebApplicationException() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodywriter/throwsexception?mt=throw/webapplicationexception");
        try {
            client.executeMethod(postMethod);

            assertEquals("throwiswritableexception", postMethod.getResponseBodyAsString());
            assertEquals(461, postMethod.getStatusCode());

        } finally {
            postMethod.releaseConnection();
        }
    }
    //
    // public void testThrowingExceptionAfterContentFlushed() {
    // fail();
    // }

}
