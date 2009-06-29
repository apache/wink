/*******************************************************************************
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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/
 
package org.apache.wink.test.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.wink.test.diff.DiffIgnoreUpdateWithAttributeQualifier;
import org.custommonkey.xmlunit.Diff;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;


/**
 * Base class for tests using mock servlet invocation (= invoking directly
 * servlet's method with Spring mock request/response).
 */
public abstract class SpringMockServletInvocationTest extends SpringAwareTestCase {

    private Object   requestProcessor;
    private Method   methodHandleRequest;
    private Class<?> requestProcessorClass;

    protected void setUp() throws Exception {
        super.setUp();
        requestProcessorClass = Class.forName("org.apache.wink.server.internal.RequestProcessor");
        Method getRequestProcessorMethod = requestProcessorClass.getMethod("getRequestProcessor",
            ServletContext.class, String.class);
        requestProcessor = getRequestProcessorMethod.invoke(null, servletContext, (String) null);
        methodHandleRequest = requestProcessorClass.getMethod("handleRequest",
            HttpServletRequest.class, HttpServletResponse.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T getRequestProcessor(Class<T> cls) {
        return (T) requestProcessor;
    }
    
    public static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder();
    }

    @SuppressWarnings("unchecked")
    protected <T> T getResourceRegistry(Class<T> cls) {
        try {
            Method getConfigurationMethod = requestProcessor.getClass().getMethod(
                "getConfiguration");
            Object configuration = getConfigurationMethod.invoke(requestProcessor);
            Method getUrletRegistryMethod = configuration.getClass().getMethod(
                "getResourceRegistry");
            return (T) getUrletRegistryMethod.invoke(configuration);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * appends package to the name and returns it as stream.
     * 
     * @param name
     * @param cls
     *            TODO
     * @return
     */
    public static InputStream getResourceOfSamePackage(String name, Class<?> cls) {
        String packagePath = packageToPath(cls.getPackage().getName());
        return SpringMockServletInvocationTest.class.getClassLoader().getResourceAsStream(
            packagePath + File.separator + name);
    }

    public static byte[] getResourceOfSamePackageAsBytes(String name, Class<?> cls)
        throws Exception {
        InputStream resource = getResourceOfSamePackage(name, cls);
        byte[] b = new byte[4096];
        int read = resource.read(b);
        byte[] result = new byte[read];
        System.arraycopy(b, 0, result, 0, read);
        return result;
    }

    /**
     * returns xml from file. Xml must be located in the same package as the
     * current class.
     * 
     * @param fileName
     * @param cls
     *            TODO
     * @return
     * @throws Exception
     */
    protected static Document getXML(String fileName, Class<?> cls) throws Exception {
        return createDocumentBuilder().parse(getResourceOfSamePackage(fileName, cls));
    }

    public static Document getXML(byte[] bs) throws Exception {
        return createDocumentBuilder().parse(new ByteArrayInputStream(bs));
    }

    public static String diffIgnoreUpdateWithAttributeQualifier(String expectedFileName,
        byte[] actual, Class<?> cls) throws Exception {
        Document xmlExpected = getXML(expectedFileName, cls);
        Document xmlActual = getXML(actual);
        Diff diff = new DiffIgnoreUpdateWithAttributeQualifier(xmlExpected, xmlActual);
        if (diff.similar()) {
            return null;
        }
        System.err.println("Expected:\r\n" + printPrettyXML(xmlExpected));
        System.err.println("Actual:\r\n" + printPrettyXML(xmlActual));
        return diff.toString();
    }

    protected String diffIgnoreUpdateWithAttributeQualifier(byte[] expected, byte[] actual)
        throws Exception {
        Document xmlExpected = getXML(expected);
        Document xmlActual = getXML(actual);
        Diff diff = new DiffIgnoreUpdateWithAttributeQualifier(xmlExpected, xmlActual);
        if (diff.similar()) {
            return null;
        }
        System.err.println("Expected:\r\n" + printPrettyXML(xmlExpected));
        System.err.println("Actual:\r\n" + printPrettyXML(xmlActual));
        return diff.toString();
    }

    public static String printPrettyXML(Document doc) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty("indent", "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
        StringWriter output = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(output));
        return output.toString();
    }

    /**
     * Passes the test to the servlet instance simulating AS behaviour.
     * 
     * @param request
     *            the filled request
     * @return a new response as filled by the servlet
     * @throws IOException
     *             io error
     */
    public MockHttpServletResponse invoke(MockHttpServletRequest request) throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        try {
            methodHandleRequest.invoke(requestProcessor, request, response);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

}
