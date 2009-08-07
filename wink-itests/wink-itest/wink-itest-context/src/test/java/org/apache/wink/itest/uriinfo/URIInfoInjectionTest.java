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

package org.apache.wink.itest.uriinfo;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests that the UriInfo can be injected via various means.
 */
public class URIInfoInjectionTest extends TestCase {

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/uriinfo";
    }

    /**
     * Tests that a URIInfo object is injected into method parameters.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testURIInfoParamInjection() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/uriinfo/param");
        try {
            client.executeMethod(getMethod);
            assertEquals(204, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a URIInfo object is injected via a bean method.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testURIInfoBeanMethodInjection() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/uriinfo/bean");
        try {
            client.executeMethod(getMethod);
            assertEquals(204, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a URIInfo object is injected via a constructor parameter.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testURIInfoConstructorInjection() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/uriinfo/constructor");
        try {
            client.executeMethod(getMethod);
            assertEquals(204, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a URIInfo object is injected via a field member.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testURIInfoFieldMemberInjection() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/uriinfo/field");
        try {
            client.executeMethod(getMethod);
            assertEquals(204, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that a URIInfo object is not injected via non bean methods.
     * 
     * @throws IOException
     * @throws HttpException
     */
    public void testURIInfoNotBeanMethod() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(getBaseURI() + "/context/uriinfo/notbeanmethod");
        try {
            client.executeMethod(getMethod);
            assertEquals(204, getMethod.getStatusCode());
        } finally {
            getMethod.releaseConnection();
        }
    }

}
