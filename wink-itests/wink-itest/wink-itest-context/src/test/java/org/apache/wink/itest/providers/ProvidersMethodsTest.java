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

package org.apache.wink.itest.providers;

import java.io.IOException;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Providers;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class ProvidersMethodsTest extends TestCase {

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/providers";
    }

    /**
     * Tests that
     * {@link Providers#getContextResolver(Class, javax.ws.rs.core.MediaType)}
     * will return null when a {@link ContextResolver} is not provided that
     * matches the requested Context type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testContextResolverNoMatch() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=java.lang.Throwable&mediaType=*%2F*");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("nullcontextresolver", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a
     * {@link Providers#getContextResolver(Class, javax.ws.rs.core.MediaType)}
     * will return a single context resolver when a single matching
     * {@link ContextResolver} is provided by the application.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testContextResolverMatchSingle() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=java.lang.Exception&mediaType=*%2F*");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("org.apache.wink.itest.providers.MyExceptionContextResolver",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link ContextResolver} with a {@link Produces} annotation
     * of text/xml will not be returned when given a non-compatible media type
     * (my/type).
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testContextResolverNoMatchBySpecificMediaType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=java.lang.Exception&mediaType=my%2Ftype");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("nullcontextresolver", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link ContextResolver} with a {@link Produces} annotation
     * of text/xml will be returned when given the specific text/xml type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testContextResolverMatchBySpecificMediaType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=java.lang.Exception&mediaType=text%2Fxml");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("org.apache.wink.itest.providers.MyExceptionContextResolver",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link ContextResolver} with a {@link Produces} annotation
     * of text/xml will be returned when given the wildcard/xml type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testContextResolverMatchBySpecificMediaTypeTypeWildcard() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=java.lang.Exception&mediaType=*%2Fxml");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("org.apache.wink.itest.providers.MyExceptionContextResolver",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link ContextResolver} with a {@link Produces} annotation
     * of text/xml will be returned when given the text/* type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testContextResolverMatchBySpecificMediaTypeSubtypeWildcard() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=java.lang.Exception&mediaType=text%2F*");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("org.apache.wink.itest.providers.MyExceptionContextResolver",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

//    /**
//     * Tests that when finding a {@link ContextResolver} both the application
//     * provided and runtime provided context resolvers are searched. Invokes
//     * with a specific JAXB class and verifies that the final context is a
//     * JAXBContext. In this case, the runtime provided context resolver is used.
//     * 
//     * @throws HttpException
//     * @throws IOException
//     */
//    public void testMultipleContextResolverRuntimeAndApplicationMatchByMediaTypeWildcardInvokeWithClassInvokeRuntimeProvided()
//        throws HttpException, IOException {
//        HttpClient client = new HttpClient();
//
//        GetMethod getMethod =
//            new GetMethod(
//                          getBaseURI() + "/context/providers/contextresolver?className=javax.xml.bind.JAXBContext&mediaType=*%2F*&invokeWithClassName=org.apache.wink.itest.providers.otherxml.OtherRootElement");
//        try {
//            client.executeMethod(getMethod);
//            assertEquals(200, getMethod.getStatusCode());
//            assertTrue(getMethod.getResponseBodyAsString(), getMethod.getResponseBodyAsString()
//                .contains("JAXBContext"));
//        } finally {
//            getMethod.releaseConnection();
//        }
//    }

    /**
     * Tests that when finding a {@link ContextResolver} both the application
     * provided and runtime provided context resolvers are searched. Invokes
     * with a specific JAXB class and verifies that the final context is a
     * JAXBContext. In this case, the application provided context resolver is
     * used.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testMultipleContextResolverRuntimeAndApplicationMatchByMediaTypeWildcardInvokeWithClassInvokeApplicationProvided()
        throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=javax.xml.bind.JAXBContext&mediaType=*%2F*&invokeWithClassName=org.apache.wink.itest.providers.xml.RootElement");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertTrue(getMethod.getResponseBodyAsString(), getMethod.getResponseBodyAsString()
                .contains("JAXBContext"));
        } finally {
            getMethod.releaseConnection();
        }
    }

//    /**
//     * Tests that when there are multiple {@link ContextResolver}s that could
//     * respond to a given type, that a proxy is returned that will call all of
//     * them.
//     * 
//     * @throws HttpException
//     * @throws IOException
//     */
//    public void testMultipleContextResolverRuntimeAndApplicationMatchByMediaTypeSpecificTextXML()
//        throws HttpException, IOException {
//        HttpClient client = new HttpClient();
//
//        GetMethod getMethod =
//            new GetMethod(
//                          getBaseURI() + "/context/providers/contextresolver?className=javax.xml.bind.JAXBContext&mediaType=text%2Fxml");
//        try {
//            client.executeMethod(getMethod);
//            assertEquals(200, getMethod.getStatusCode());
//            assertTrue(getMethod.getResponseBodyAsString().startsWith("$Proxy"));
//        } finally {
//            getMethod.releaseConnection();
//        }
//    }

//    /**
//     * Tests that when the application provided {@link ContextResolver} which
//     * has a {@link Produces} annotation with text/xml is not the
//     * ContextResolver returned when searching for application/json media type.
//     * 
//     * @throws HttpException
//     * @throws IOException
//     */
//    public void testMultipleContextResolverRuntimeAndApplicationMatchByMediaTypeSpecificApplicationJSON()
//        throws HttpException, IOException {
//        HttpClient client = new HttpClient();
//
//        GetMethod getMethod =
//            new GetMethod(
//                          getBaseURI() + "/context/providers/contextresolver?className=javax.xml.bind.JAXBContext&mediaType=application%2Fjson");
//        try {
//            client.executeMethod(getMethod);
//            assertEquals(200, getMethod.getStatusCode());
//            assertTrue(getMethod.getResponseBodyAsString().contains("JAXBContext"));
//        } finally {
//            getMethod.releaseConnection();
//        }
//    }

//    /**
//     * Tests that when the application provided {@link ContextResolver} which
//     * has a {@link Produces} annotation with text/xml is not called when an
//     * application/json is searched. This method should be able to invoke the
//     * runtime provided JAXBContext ContextResolver but return null.
//     * 
//     * @throws HttpException
//     * @throws IOException
//     */
//    public void testMultipleContextResolverRuntimeAndApplicationMatchByMediaTypeSpecificApplicationJSONInvokeNegative()
//        throws HttpException, IOException {
//        HttpClient client = new HttpClient();
//
//        GetMethod getMethod =
//            new GetMethod(
//                          getBaseURI() + "/context/providers/contextresolver?className=javax.xml.bind.JAXBContext&mediaType=application%2Fjson&invokeWithClassName=org.apache.wink.itest.providers.xml.RootElement");
//        try {
//            client.executeMethod(getMethod);
//            assertEquals(200, getMethod.getStatusCode());
//            assertEquals("null", getMethod.getResponseBodyAsString());
//        } finally {
//            getMethod.releaseConnection();
//        }
//    }

    /**
     * Tests that when the application provided {@link ContextResolver} which
     * has a {@link Produces} annotation with text/xml is called when an
     * text/xml is searched. This method should be able to invoke the
     * application provided JAXBContext ContextResolver and return a
     * JAXBContext.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testMultipleContextResolverRuntimeAndApplicationMatchByMediaTypeSpecificTextXMLInvoke()
        throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=javax.xml.bind.JAXBContext&mediaType=text%2Fxml&invokeWithClassName=org.apache.wink.itest.providers.xml.RootElement");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertTrue(getMethod.getResponseBodyAsString(), getMethod.getResponseBodyAsString()
                .contains("JAXBContext"));
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link ContextResolver} with a wildcard/xml {@link Produces}
     * annotation will match a (specific)/xml type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testContextResolverMatchMultipleSortByProducesWildcardType() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=java.lang.String&mediaType=abcd%2Fxml&invokeWithClassName=java.lang.Short&returnToStringValue=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("allwildcardshort", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link ContextResolver} with a text/wildcard
     * {@link Produces} annotation will match a text/(specific) type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testContextResolverMatchMultipleSortByProducesWildcardSubtype()
        throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=java.lang.String&mediaType=text%2Fabcd&invokeWithClassName=java.lang.Short&returnToStringValue=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("textwildcardonly", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link ContextResolver} with a wildcard/wildcard
     * {@link Produces} annotation will match a (specific)/(specific) type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testContextResolverMatchMultipleSortByProducesAllWildcard() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=java.lang.String&mediaType=abcd%2Fdef&invokeWithClassName=java.lang.Short&returnToStringValue=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("allwildcardshort", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a match with multiple {@link ContextResolver}s with the same
     * media type and generic type will use a proxy that finds the single
     * resolver that returns a non-null.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testContextResolverMatchMultipleSortByProducesFindOne() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=java.lang.String&mediaType=text%2Fxml&invokeWithClassName=java.lang.Integer&returnToStringValue=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("integerxmlonly", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        client = new HttpClient();

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=java.lang.String&mediaType=text%2Fxml&invokeWithClassName=java.lang.Long&returnToStringValue=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("longxml2only", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a match with multiple {@link ContextResolver}s will return the
     * most specific responses before the wildcard responses.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testContextResolverMatchAnyMoreSpecificThanWildcards() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=java.lang.String&mediaType=text%2Fxml&invokeWithClassName=java.lang.Short&returnToStringValue=true");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String responseBody = getMethod.getResponseBodyAsString();
            assertTrue(responseBody, "shortxmlandjson".equals(responseBody) || "shortxml2only"
                .equals(responseBody)
                || "shortxmlonly".equals(responseBody));
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link ContextResolver} with a wildcard/wildcard
     * {@link Produces} annotation will match any media type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testContextResolverNoProducesMatchNotExpectedMediaType() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/contextresolver?className=java.lang.String&mediaType=my%2Ftype");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("org.apache.wink.itest.providers.MyStringContextForAllWildcard",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a Provider can return an {@link ExceptionMapper}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetExceptionMapperAppSuppliedProvider() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/exception?className=org.apache.wink.itest.providers.MyException");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("org.apache.wink.itest.providers.ExceptionMapperForMyException",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link Providers} will return the correct exception mapper
     * given an exception that is a sub-class of the {@link ExceptionMapper}
     * class.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetExceptionMapperAppSuppliedInheritanceTree() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/exception?className=org.apache.wink.itest.providers.MyException2");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("org.apache.wink.itest.providers.ExceptionMapperForMyException",
                         getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a {@link Providers} will return a null value when given a
     * class that does not have an {@link ExceptionMapper}.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetExceptionMapperNoMatching() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/exception?className=java.lang.Throwable");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("null", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link Providers#getMessageBodyWriter(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * uses the media type to filter out potential message body writers.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetMessageBodyWriterSortByProducesMediaType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.Integer&mediaType=text%2Fxml");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.MyMessageBodyWriterXMLAndJSONForNumber",
                         response);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.Integer&mediaType=application%2Fjson");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertTrue(response,
                       response
                           .contains("org.apache.wink.itest.providers.MyMessageBodyWriterXMLAndJSONForNumber") || response
                           .contains("org.apache.wink.itest.providers.MyMessageBodyWriterJSONForInteger"));
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link Providers#getMessageBodyWriter(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * uses the media type to filter out potential message body writers and will
     * respect more specific types over wildcards.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetMessageBodyWriterSortByProducesMediaTypeWithWildcards()
        throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.Short&mediaType=application%2Fjson");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.MyMessageBodyWriterJSONForShort",
                         response);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.Short&mediaType=application%2F*");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertTrue(response,
                       response
                           .contains("org.apache.wink.itest.providers.MyMessageBodyWriterJSONForShort"));
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.Short&mediaType=application%2Fmytype");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.MyMessageBodyWriterApplicationWildcardForShort",
                         response);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.Short&mediaType=mytype%2Fmysubtype");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();

            assertEquals("org.apache.wink.itest.providers.MyMessageBodyWriterWildcardForShort",
                         response);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link Providers#getMessageBodyWriter(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * will find MessageBodyWriters that have inherited the MessageBodyWriter
     * interface.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testGetMessageBodyWriterWhichInheritsWriterInterface() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.util.List&mediaType=abcd%2Fefgh");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.MyMessageBodyWriterInherited",
                         response);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link Providers#getMessageBodyWriter(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * will filter out writers that do not match the isWritable method.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetMessageBodyWriterSortByIsWritable() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.Integer&mediaType=application%2Fjson");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertTrue(response,
                       "org.apache.wink.itest.providers.MyMessageBodyWriterJSONForInteger"
                           .equals(response) || "org.apache.wink.itest.providers.MyMessageBodyWriterXMLAndJSONForNumber"
                           .equals(response));
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.Long&mediaType=application%2Fjson");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertTrue(response,
                       "org.apache.wink.itest.providers.MyMessageBodyWriterJSONForLong"
                           .equals(response) || "org.apache.wink.itest.providers.MyMessageBodyWriterXMLAndJSONForNumber"
                           .equals(response));
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.Short&mediaType=application%2Fjson");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals(response,
                         "org.apache.wink.itest.providers.MyMessageBodyWriterJSONForShort");
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.Long&mediaType=text%2Fxml");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.MyMessageBodyWriterXMLAndJSONForNumber",
                         response);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.Integer&mediaType=text%2Fxml");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.MyMessageBodyWriterXMLAndJSONForNumber",
                         response);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link Providers#getMessageBodyWriter(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * will use the application provided MessageBodyWriters before runtime
     * provided.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetMessageBodyWriterUserPrecedenceOverRuntime() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.String&mediaType=text%2Fplain");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.MyStringWriterForStrings",
                         response);
        } finally {
            getMethod.releaseConnection();
        }

    }

    /**
     * Tests that a null is returned when calling
     * {@link Providers#getMessageBodyWriter(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * if there are no suitable MessageBodyWriters.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetMessageBodyWriterNoMatching() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.Float&mediaType=application%2Fjson");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("nullwriter", response);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodywriter?className=java.lang.Exception&mediaType=*%2F*");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.common.internal.providers.entity.FormatedExceptionProvider",
                         response);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a null is returned when calling
     * {@link Providers#getMessageBodyReader(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * if there are no suitable MessageBodyReader.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testGetMessageBodyReaderNoMatching() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.Float&mediaType=mynonexistenttype%2Fmysubtype");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("nullreader", response);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.Exception&mediaType=*%2F*");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("nullreader", response);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link Providers#getMessageBodyReader(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * will filter out writers that do not match the isWritable method.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetMessageBodyReaderSortByIsReadable() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.Integer&mediaType=application%2Fjson");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertTrue(response,
                       "org.apache.wink.itest.providers.readers.MyMessageBodyReaderJSONForInteger"
                           .equals(response) || "org.apache.wink.itest.providers.readers.MyMessageBodyReaderXMLAndJSONForNumber"
                           .equals(response));
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.Long&mediaType=application%2Fjson");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertTrue(response,
                       "org.apache.wink.itest.providers.readers.MyMessageBodyReaderJSONForLong"
                           .equals(response) || "org.apache.wink.itest.providers.readers.MyMessageBodyReaderXMLAndJSONForNumber"
                           .equals(response));
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.Short&mediaType=application%2Fjson");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.readers.MyMessageBodyReaderJSONForShort",
                         response);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.Long&mediaType=text%2Fxml");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.readers.MyMessageBodyReaderXMLAndJSONForNumber",
                         response);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.Integer&mediaType=text%2Fxml");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.readers.MyMessageBodyReaderXMLAndJSONForNumber",
                         response);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link Providers#getMessageBodyReader(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * uses the media type to filter out potential message body readers.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetMessageBodyReaderSortByConsumesMediaType() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.Integer&mediaType=text%2Fxml");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.readers.MyMessageBodyReaderXMLAndJSONForNumber",
                         response);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.Integer&mediaType=application%2Fjson");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertTrue(response,
                       response
                           .contains("org.apache.wink.itest.providers.readers.MyMessageBodyReaderXMLAndJSONForNumber") || response
                           .contains("org.apache.wink.itest.providers.readers.MyMessageBodyReaderJSONForInteger"));
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link Providers#getMessageBodyReader(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * uses the media type to filter out potential message body readers and will
     * respect more specific types over wildcards.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetMessageBodyReaderSortByConsumesMediaTypeWithWildcards()
        throws HttpException, IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.Short&mediaType=application%2Fjson");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.readers.MyMessageBodyReaderJSONForShort",
                         response);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.Short&mediaType=application%2F*");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.readers.MyMessageBodyReaderJSONForShort",
                         response);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.Short&mediaType=application%2Fmytype");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.readers.MyMessageBodyReaderApplicationWildcardForShort",
                         response);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.Short&mediaType=mytype%2Fmysubtype");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();

            assertEquals("org.apache.wink.itest.providers.readers.MyMessageBodyReaderWildcardForShort",
                         response);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that
     * {@link Providers#getMessageBodyReader(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * will use the application provided MessageBodyReaders before runtime
     * provided.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testGetMessageBodyReaderUserPrecedenceOverRuntime() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.lang.String&mediaType=text%2Fplain");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.readers.MyMessageBodyReaderForStrings",
                         response);
        } finally {
            getMethod.releaseConnection();
        }

    }

    /**
     * Tests that
     * {@link Providers#getMessageBodyReader(Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)}
     * will find MessageBodyReaders that have inherited the MessageBodyReader
     * interface.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testGetMessageBodyReaderWhichInheritsReaderInterface() throws HttpException,
        IOException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.util.Set&mediaType=tuv%2Fwxyz");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.itest.providers.readers.MyMessageBodyReaderInherited",
                         response);
        } finally {
            getMethod.releaseConnection();
        }

        getMethod =
            new GetMethod(
                          getBaseURI() + "/context/providers/messagebodyreader?className=java.util.List&mediaType=tuv%2Fwxyz");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String response = getMethod.getResponseBodyAsString();
            assertEquals("nullreader", response);
        } finally {
            getMethod.releaseConnection();
        }
    }
}
