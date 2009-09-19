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

package org.apache.wink.itest.securitycontext;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.itest.securitycontext.xml.ObjectFactory;
import org.apache.wink.itest.securitycontext.xml.SecurityContextInfo;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class SecurityContextTest extends TestCase {

    public String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/securitycontext";
    }

    private HttpClient client;

    public void setUp() {
        client = new HttpClient();
    }

    /**
     * Tests that a security context can be injected via a parameter.
     * 
     * @throws IOException
     * @throws HttpException
     * @throws JAXBException
     */
    public void testSecurityContextParamResource() throws HttpException, IOException, JAXBException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/securitycontext/param");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());

            JAXBContext context =
                JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
            SecurityContextInfo secContextInfo =
                (SecurityContextInfo)context.createUnmarshaller().unmarshal(getMethod
                    .getResponseBodyAsStream());
            assertNotNull(secContextInfo);
            assertEquals(false, secContextInfo.isSecure());
            assertEquals(false, secContextInfo.isUserInRoleAdmin());
            assertEquals(false, secContextInfo.isUserInRoleNull());
            assertEquals(false, secContextInfo.isUserInRoleUser());
            assertEquals("null", secContextInfo.getUserPrincipal());
            assertNull(secContextInfo.getAuthScheme());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a security context can be injected via a constructor.
     * 
     * @throws IOException
     * @throws HttpException
     * @throws JAXBException
     */
    public void testSecurityContextConstructorResource() throws HttpException, IOException,
        JAXBException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/securitycontext/constructor");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());

            JAXBContext context =
                JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
            SecurityContextInfo secContextInfo =
                (SecurityContextInfo)context.createUnmarshaller().unmarshal(getMethod
                    .getResponseBodyAsStream());
            assertNotNull(secContextInfo);
            assertEquals(false, secContextInfo.isSecure());
            assertEquals(false, secContextInfo.isUserInRoleAdmin());
            assertEquals(false, secContextInfo.isUserInRoleNull());
            assertEquals(false, secContextInfo.isUserInRoleUser());
            assertEquals("null", secContextInfo.getUserPrincipal());
            assertNull(secContextInfo.getAuthScheme());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a security context can be injected via a bean method.
     * 
     * @throws IOException
     * @throws HttpException
     * @throws JAXBException
     */
    public void testSecurityContextBeanResource() throws HttpException, IOException, JAXBException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/securitycontext/bean");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());

            JAXBContext context =
                JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
            SecurityContextInfo secContextInfo =
                (SecurityContextInfo)context.createUnmarshaller().unmarshal(getMethod
                    .getResponseBodyAsStream());
            assertNotNull(secContextInfo);
            assertEquals(false, secContextInfo.isSecure());
            assertEquals(false, secContextInfo.isUserInRoleAdmin());
            assertEquals(false, secContextInfo.isUserInRoleNull());
            assertEquals(false, secContextInfo.isUserInRoleUser());
            assertEquals("null", secContextInfo.getUserPrincipal());
            assertNull(secContextInfo.getAuthScheme());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a security context will not be injected into non-bean methods.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testSecurityContextNotBeanResource() throws HttpException, IOException {
        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/context/securitycontext/notbeanmethod");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals(getMethod.getResponseBodyAsString(), "false");
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a security context can be injected via a member field.
     * 
     * @throws IOException
     * @throws HttpException
     * @throws JAXBException
     */
    public void testSecurityContextFieldResource() throws HttpException, IOException, JAXBException {
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/securitycontext/field");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());

            JAXBContext context =
                JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
            SecurityContextInfo secContextInfo =
                (SecurityContextInfo)context.createUnmarshaller().unmarshal(getMethod
                    .getResponseBodyAsStream());
            assertNotNull(secContextInfo);
            assertEquals(false, secContextInfo.isSecure());
            assertEquals(false, secContextInfo.isUserInRoleAdmin());
            assertEquals(false, secContextInfo.isUserInRoleNull());
            assertEquals(false, secContextInfo.isUserInRoleUser());
            assertEquals("null", secContextInfo.getUserPrincipal());
            assertNull(secContextInfo.getAuthScheme());
        } finally {
            getMethod.releaseConnection();
        }
    }
}
