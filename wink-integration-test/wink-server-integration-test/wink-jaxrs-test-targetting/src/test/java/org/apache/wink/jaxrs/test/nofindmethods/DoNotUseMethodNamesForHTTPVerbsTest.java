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
package org.apache.wink.jaxrs.test.nofindmethods;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * These tests are designed to make sure the runtime does not invoke methods
 * that should not be invoked.
 */
public class DoNotUseMethodNamesForHTTPVerbsTest extends TestCase {

    public static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/nofindmethods";
    }

    /**
     * Negative tests that method names that begin with HTTP verbs are not
     * invoked on a root resource.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testMethodsNotValid() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        HttpMethod method =
            new PostMethod(getBaseURI() + "/nousemethodnamesforhttpverbs/someresource");
        try {
            client.executeMethod(method);
            assertEquals(405, method.getStatusCode());
        } finally {
            method.releaseConnection();
        }

        method = new GetMethod(getBaseURI() + "/nousemethodnamesforhttpverbs/someresource");
        try {
            client.executeMethod(method);
            assertEquals(405, method.getStatusCode());
        } finally {
            method.releaseConnection();
        }

        method = new PutMethod(getBaseURI() + "/nousemethodnamesforhttpverbs/someresource");
        try {
            client.executeMethod(method);
            assertEquals(405, method.getStatusCode());
        } finally {
            method.releaseConnection();
        }

        method = new DeleteMethod(getBaseURI() + "/nousemethodnamesforhttpverbs/someresource");
        try {
            client.executeMethod(method);
            assertEquals(405, method.getStatusCode());
        } finally {
            method.releaseConnection();
        }

        method = new GetMethod(getBaseURI() + "/nousemethodnamesforhttpverbs/counter/root");
        try {
            client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            assertEquals("0", method.getResponseBodyAsString());
        } finally {
            method.releaseConnection();
        }
    }

    /**
     * Negative tests that method names that begin with HTTP verbs are not
     * invoked on a sublocator method.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testSublocatorMethodsNotValid() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        HttpMethod method =
            new PostMethod(getBaseURI() + "/nousemethodnamesforhttpverbs/sublocatorresource/sub");
        try {
            client.executeMethod(method);
            assertEquals(405, method.getStatusCode());
        } finally {
            method.releaseConnection();
        }

        method =
            new GetMethod(getBaseURI() + "/nousemethodnamesforhttpverbs/sublocatorresource/sub");
        try {
            client.executeMethod(method);
            assertEquals(405, method.getStatusCode());
        } finally {
            method.releaseConnection();
        }

        method =
            new PutMethod(getBaseURI() + "/nousemethodnamesforhttpverbs/sublocatorresource/sub");
        try {
            client.executeMethod(method);
            assertEquals(405, method.getStatusCode());
        } finally {
            method.releaseConnection();
        }

        method =
            new DeleteMethod(getBaseURI() + "/nousemethodnamesforhttpverbs/sublocatorresource/sub");
        try {
            client.executeMethod(method);
            assertEquals(405, method.getStatusCode());
        } finally {
            method.releaseConnection();
        }

        method = new GetMethod(getBaseURI() + "/nousemethodnamesforhttpverbs/counter/sublocator");
        try {
            client.executeMethod(method);
            assertEquals(200, method.getStatusCode());
            assertEquals("0", method.getResponseBodyAsString());
        } finally {
            method.releaseConnection();
        }
    }
}
