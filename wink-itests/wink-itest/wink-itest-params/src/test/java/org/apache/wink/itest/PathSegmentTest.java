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

package org.apache.wink.itest;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class PathSegmentTest extends TestCase {

    private static HttpClient httpclient = new HttpClient();

    public String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/params/params/pathsegment";
    }

    public void testPathSegmentNoMatrixParameters() throws Exception {
        GetMethod httpMethod = new GetMethod(getBaseURI() + "/somepath");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(200, httpMethod.getStatusCode());
            assertEquals("somepath", httpMethod.getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod = new GetMethod(getBaseURI() + "/123456");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(200, httpMethod.getStatusCode());
            assertEquals("123456", httpMethod.getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod = new GetMethod(getBaseURI() + "/123456;mp=3145");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(200, httpMethod.getStatusCode());
            assertEquals("123456", httpMethod.getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }
    }

    public void testPathSegmentMatrixParameters() throws Exception {
        GetMethod httpMethod = new GetMethod(getBaseURI() + "/matrix/somepath");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(200, httpMethod.getStatusCode());
            assertEquals("somepath-somepath-null-null", httpMethod.getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod = new GetMethod(getBaseURI() + "/matrix/somepath;mp=val");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(200, httpMethod.getStatusCode());
            assertEquals("somepath-somepath-null-val", httpMethod.getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod = new GetMethod(getBaseURI() + "/matrix/somepath;mp=val;val=abc");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(200, httpMethod.getStatusCode());
            assertEquals("somepath-somepath-[abc]-val", httpMethod.getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }

        httpMethod = new GetMethod(getBaseURI() + "/matrix/somepath;mp=val;val=abc;val=123");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(200, httpMethod.getStatusCode());
            assertEquals("somepath-somepath-[abc, 123]-val", httpMethod.getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }
    }
}
