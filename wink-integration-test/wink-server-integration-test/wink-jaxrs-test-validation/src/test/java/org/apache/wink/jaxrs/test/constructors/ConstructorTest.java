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

package org.apache.wink.jaxrs.test.constructors;

import java.io.IOException;

import javax.xml.ws.http.HTTPException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Tests the runtime for the specification regarding Constructors.
 */
public class ConstructorTest extends TestCase {

    private static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/constructors/";
    }

    /**
     * Tests that the runtime will use the correct constructor with a resource
     * that has multiple constructors. The resource has multiple constructors
     * with different number of parameters in each constructor.
     * 
     * @throws IOException
     * @throws HTTPException
     */
    public void testConstructorWithMostParams() throws IOException, HTTPException {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/constructors/multi");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("matrixAndQueryAndContext1", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the runtime will use the correct constructor with a resource
     * that has multiple constructors. The resource has multiple constructors
     * with different number of parameters in each constructor.
     * 
     * @throws IOException
     * @throws HTTPException
     */
    public void testConstructorWithMostParams2() throws IOException, HTTPException {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/constructors/multi2/somepath");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("contextAndHeaderAndCookieAndPath1", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the runtime will randomly choose a constructor between two
     * constructors with the same number of parameters. A warning should be
     * issued.
     * 
     * @throws IOException
     * @throws HTTPException
     */
    public void testConstructorWithSameParamWarning() throws IOException, HTTPException {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/constructors/samenumparam");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String c = getMethod.getResponseBodyAsString();
            boolean foundConstructor = false;
            if ("context1".equals(c)) {
                foundConstructor = true;
            } else if ("query1".equals(c)) {
                foundConstructor = true;
            }
            assertTrue("Returned message body was: " + c, foundConstructor);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the runtime will randomly choose a constructor between two
     * constructors with the same parameters except different types. A warning
     * should be issued.
     * 
     * @throws IOException
     * @throws HTTPException
     */
    public void testConstructorWithSameParamWarning2() throws IOException, HTTPException {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/constructors/samenumparam2?q=15");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            String c = getMethod.getResponseBodyAsString();
            boolean foundConstructor = false;
            if ("queryInt1".equals(c)) {
                foundConstructor = true;
            } else if ("queryString1".equals(c)) {
                foundConstructor = true;
            }
            assertTrue("Returned message body was: " + c, foundConstructor);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the sub-resources can use a package default constructor.
     * 
     * @throws IOException
     * @throws HTTPException
     */
    public void testSubResourceLocatorPackageEmptyConstructor() throws IOException, HTTPException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/constructors/subresource/emptypackage");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("package", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the sub-resources can use a package constructor with a String
     * parameter.
     * 
     * @throws IOException
     * @throws HTTPException
     */
    public void testSubResourceLocatorPackageStringConstructor() throws IOException, HTTPException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/constructors/subresource/stringpackage");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("packageString", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the sub-resources can use a public constructor.
     * 
     * @throws IOException
     * @throws HTTPException
     */
    public void testSubResourceLocatorPublicDefaultConstructor() throws IOException, HTTPException {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/constructors/subresource/emptypublic");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("public", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the sub-resources can use a public constructor with a String
     * parameter.
     * 
     * @throws IOException
     * @throws HTTPException
     */
    public void testSubResourceLocatorPublicStringConstructor() throws IOException, HTTPException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/constructors/subresource/stringpublic?q=Hello");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("Hello", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the sub-resources can use a private constructor.
     * 
     * @throws IOException
     * @throws HTTPException
     */
    public void testSubResourceLocatorPrivateDefaultConstructor() throws IOException, HTTPException {
        HttpClient client = new HttpClient();

        GetMethod getMethod = new GetMethod(getBaseURI() + "/constructors/subresource/emptyprivate");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("private", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the sub-resources can use a private constructor with a String
     * parameter.
     * 
     * @throws IOException
     * @throws HTTPException
     */
    public void testSubResourceLocatorPrivateStringConstructor() throws IOException, HTTPException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/constructors/subresource/stringprivate?q=Hello");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("Hello", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the sub-resources will eventually find the right resource.
     * 
     * @throws IOException
     * @throws HTTPException
     */
    public void testSubResourceOtherSubPublicToPackage() throws IOException, HTTPException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/constructors/subresource/stringpublic/other");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("subpackage", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the sub-resources will eventually find the right resource.
     * 
     * @throws IOException
     * @throws HTTPException
     */
    public void testSubResourceOtherSubPackageToPublic() throws IOException, HTTPException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/constructors/subresource/stringpackage/other");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("public", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests that the sub-resources will eventually return the right resource.
     * 
     * @throws IOException
     * @throws HTTPException
     */
    public void testSubResourceDecideSubDynamic() throws IOException, HTTPException {
        HttpClient client = new HttpClient();

        GetMethod getMethod =
            new GetMethod(getBaseURI() + "/constructors/subresource/sub?which=public");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("public", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }

        getMethod = new GetMethod(getBaseURI() + "/constructors/subresource/sub?which=package");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            assertEquals("package", getMethod.getResponseBodyAsString());
        } finally {
            getMethod.releaseConnection();
        }
    }
}
