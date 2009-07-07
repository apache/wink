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
package org.apache.wink.jaxrs.test.providers.readers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.MessageBodyReader;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class JAXRSMessageBodyReadersTest extends TestCase {

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/readers";
    }

    /**
     * Tests that an improperly formatted request content type is handled.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderImproperlyFormattedContentType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/requestcontenttype");
        postMethod.setRequestEntity(new StringRequestEntity("Hello\r\nWorld\r\n", "", "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(500, postMethod.getStatusCode());
            // assertLogContainsException("java.lang.IllegalArgumentException");
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that an empty request content type is handled.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderNoContentType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/requestcontenttype");
        byte[] requestContent = {0, 0, 0};
        postMethod
            .setRequestEntity(new InputStreamRequestEntity(new ByteArrayInputStream(requestContent)));
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            /*
             * should only invoke hello world
             */
            assertEquals("hello world", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method receives the correct class type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderIsReadableUnexpectedClassType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/unexpectedclasstype");
        postMethod.setRequestEntity(new StringRequestEntity("Hello\r\nWorld\r\n", "text/plain",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(415, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method receives the correct class type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderIsReadableExpectedClassType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/classtype");
        postMethod.setRequestEntity(new StringRequestEntity("Hello\r\nWorld\r\n", "text/plain",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            assertEquals("echo:Helloecho:World", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method works when there is no generic entity type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderIsReadableNoGenericEntityType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/nogenericentity");
        postMethod.setRequestEntity(new StringRequestEntity("Hello\r\nWorld\r\n", "text/plain",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            String response = postMethod.getResponseBodyAsString();
            assertEquals("echo:Hello\r\nWorld\r\n", response);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method works when there is no argument type specified on the generic
     * type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderIsReadableGenericEntityEmptyType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/genericentityempty");
        postMethod.setRequestEntity(new StringRequestEntity("Hello\r\nWorld\r\n", "text/plain",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(415, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method returns true when the expected argument type is specified on the
     * generic type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderIsReadableGenericEntityTypeCorrect() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/genericentityqueuestring");
        postMethod.setRequestEntity(new StringRequestEntity("Hello\r\nWorld\r\n", "text/plain",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("Hello thereWorld there", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method returns false when an unexpected argument type is specified on the
     * generic type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderIsReadableGenericEntityTypeIncorrect() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/genericentityqueueobject");
        postMethod.setRequestEntity(new StringRequestEntity("Hello\r\nWorld\r\n", "text/plain",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);
            assertEquals(415, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method still works without an annotated entity parameter.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderIsReadableEntityParameterNotAnnotated() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/notannotatedentity");
        postMethod.setRequestEntity(new StringRequestEntity("Hello\r\nWorld\r\n", "text/plain",
                                                            "UTF-8"));
        postMethod.addRequestHeader("Accept", "text/plain");
        try {
            client.executeMethod(postMethod);

            assertEquals(415, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method is passed a single annotation.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderIsReadableEntityParameterAnnotated() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/annotatedentity");
        postMethod.setRequestEntity(new StringRequestEntity("Hello\r\nWorld\r\n", "text/plain",
                                                            "UTF-8"));
        postMethod.addRequestHeader("Accept", "text/plain");
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            assertEquals("text/plain", postMethod.getResponseHeader("Content-Type").getValue());
            assertEquals("Hello thereWorld there", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method is passed multiple annotations.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderIsReadableEntityParameterAnnotatedMultiple() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/multipleannotatedentity");
        postMethod.setRequestEntity(new StringRequestEntity("Hello\r\nWorld\r\n", "text/plain",
                                                            "UTF-8"));
        postMethod.addRequestHeader("Accept", "text/plain");
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            assertEquals("text/plain", postMethod.getResponseHeader("Content-Type").getValue());
            assertEquals("Hello thereWorld there", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method is passed an incompatiable media type and does not return true.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderIsReadableIncorrectMediaType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/mediatype");
        postMethod.setRequestEntity(new StringRequestEntity("Hello\r\nWorld\r\n", "text/plain",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(415, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that the
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * method is passed the expected media type and reads the data.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderIsReadableCorrectMediaType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/mediatype");
        postMethod.setRequestEntity(new StringRequestEntity("Hello\r\nWorld\r\n", "custom/type",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertTrue(response, response.contains("Hello there"));
            assertTrue(response, response.contains("World there"));
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * When a {@link RuntimeException} is propagated back from
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * , verify that the exception is handled appropriately.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderIsReadableThrowsRuntimeException() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("clear", "custom/exception", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("0postReaderReadFrom:clear", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("Hello World", "custom/exception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("1postReaderReadFrom:Hello World", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("clear", "custom/exception", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("0postReaderReadFrom:clear", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("thrownull", "custom/runtimeexception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(500, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            // assertLogContainsException(response,
            // "javax.servlet.ServletException");
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("hello world", "custom/exception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("1postReaderReadFrom:hello world", response);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * When a {@link WebApplicationException} is propagated back from
     * {@link MessageBodyReader#isReadable(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * , verify that the exception is handled appropriately.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testReaderIsReadableThrowsWebApplicationException() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("clear", "custom/exception", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("0postReaderReadFrom:clear", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("Hello World", "custom/exception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("1postReaderReadFrom:Hello World", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("clear", "custom/exception", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("0postReaderReadFrom:clear", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("thrownull",
                                                            "custom/webapplicationexception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(478, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("hello world", "custom/exception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("1postReaderReadFrom:hello world", response);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a
     * {@link MessageBodyReader#readFrom(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, InputStream)}
     * can return a different object based on the class argument.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testReaderReadFromClassType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readdifferentlylong");
        postMethod.setRequestEntity(new StringRequestEntity("empty", "custom/long", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            String response = postMethod.getResponseBodyAsString();
            assertEquals("" + Long.MAX_VALUE, response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readdifferentlyinteger");
        postMethod.setRequestEntity(new StringRequestEntity("empty", "custom/int", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("" + Integer.MAX_VALUE, response);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a
     * {@link MessageBodyReader#readFrom(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, InputStream)}
     * can return a different object based on the generic type argument.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderReadFromGenericType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readdifferentlygenericlist");
        postMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "1\r\n2\r\n3\r\n4\r\n5\r\n6\r\n7\r\n8\r\n9\r\n10",
                                                      "custom/generic", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            String response = postMethod.getResponseBodyAsString();
            assertEquals("listnonspecified:obj:1obj:2obj:3obj:4obj:5obj:6obj:7obj:8obj:9obj:10",
                         response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readdifferentlygenericliststring");
        postMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "1\r\n2\r\n3\r\n4\r\n5\r\n6\r\n7\r\n8\r\n9\r\n10",
                                                      "custom/generic", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("liststring:str:1str:2str:3str:4str:5str:6str:7str:8str:9str:10", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readdifferentlygenericlistinteger");
        postMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "1\r\n2\r\n3\r\n4\r\n5\r\n6\r\n7\r\n8\r\n9\r\n10",
                                                      "custom/generic", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("listinteger:12345678910", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readdifferentlygenericinteger");
        postMethod
            .setRequestEntity(new StringRequestEntity(
                                                      "1\r\n2\r\n3\r\n4\r\n5\r\n6\r\n7\r\n8\r\n9\r\n10",
                                                      "custom/generic", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("integer:55", response);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a
     * {@link MessageBodyReader#readFrom(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, InputStream)}
     * can return a different object based on the annotations argument.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderReadFromAnnotationType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readdifferentlyshort");
        postMethod.setRequestEntity(new StringRequestEntity("empty", "custom/short", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            String response = postMethod.getResponseBodyAsString();
            assertEquals("" + Short.MAX_VALUE, response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readdifferentlyshortnoannotation");
        postMethod.setRequestEntity(new StringRequestEntity("empty", "custom/short", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("null", response);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a
     * {@link MessageBodyReader#readFrom(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, InputStream)}
     * can return a different object based on the media type argument.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderReadFromMediaType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readdifferentlybytemediatype");
        postMethod.setRequestEntity(new StringRequestEntity("empty", "custom/int", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            String response = postMethod.getResponseBodyAsString();
            assertEquals("null", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readdifferentlybytemediatype");
        postMethod.setRequestEntity(new StringRequestEntity("empty", "custom/byte", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("" + Byte.MAX_VALUE, response);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a
     * {@link MessageBodyReader#readFrom(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, InputStream)}
     * can use the HttpHeaders.
     * 
     * @throws IOException
     */
    public void testReaderReadFromGetHeader() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("clear", "custom/exception", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("0postReaderReadFrom:clear", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("Hello World!", "custom/exception",
                                                            "UTF-8"));
        postMethod.addRequestHeader("myCustomHeaderToappend", "abcdefgh");
        postMethod.addRequestHeader("MYCUSTOMHEADERTOAPPEND", "wxyz");
        postMethod.addRequestHeader("mycustomheadertoappend", "12345");
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("1postReaderReadFrom:Hello World!abcdefghwxyz12345", response);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that calling {@link InputStream#close()} in the
     * {@link MessageBodyReader#readFrom(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)}
     * method will not cause errors.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testReaderReadFromCloseInputStream() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("clear", "custom/exception", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("0postReaderReadFrom:clear", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("closeinput", "custom/exception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("1postReaderReadFrom:closeinput", response);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that throwing a {@link RuntimeException} in the
     * {@link MessageBodyReader#readFrom(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)}
     * method will propagate the exception appropriately.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testReaderReadFromThrowsRuntimeException() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("clear", "custom/exception", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("0postReaderReadFrom:clear", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("Hello World", "custom/exception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("1postReaderReadFrom:Hello World", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("clear", "custom/exception", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("0postReaderReadFrom:clear", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("thrownull", "custom/exception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(500, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            // assertLogContainsException(response,
            // "javax.servlet.ServletException");
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("hello world", "custom/exception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("1postReaderReadFrom:hello world", response);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that an IOException triggered by a
     * {@link MessageBodyReader#readFrom(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)}
     * will propagate appropriately.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderReadFromThrowsIOException() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("clear", "custom/exception", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("0postReaderReadFrom:clear", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("Hello World", "custom/exception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("1postReaderReadFrom:Hello World", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("clear", "custom/exception", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("0postReaderReadFrom:clear", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("ioexception", "custom/exception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(500, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            // assertLogContainsException(response,
            // "javax.servlet.ServletException");
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("hello world", "custom/exception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("1postReaderReadFrom:hello world", response);
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests that a WebApplicationException triggered by a
     * {@link MessageBodyReader#readFrom(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)}
     * will propagate appropriately.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testReaderReadFromThrowsWebApplicationException() throws IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("clear", "custom/exception", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("0postReaderReadFrom:clear", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("Hello World", "custom/exception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("1postReaderReadFrom:Hello World", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("clear", "custom/exception", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("0postReaderReadFrom:clear", response);
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("webapplicationexception",
                                                            "custom/exception", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(477, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }

        postMethod =
            new PostMethod(
                           getBaseURI() + "/jaxrs/tests/providers/messagebodyreader/reader/readfrom");
        postMethod.setRequestEntity(new StringRequestEntity("hello world", "custom/exception",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());

            String response = postMethod.getResponseBodyAsString();
            assertEquals("1postReaderReadFrom:hello world", response);
        } finally {
            postMethod.releaseConnection();
        }
    }
}
