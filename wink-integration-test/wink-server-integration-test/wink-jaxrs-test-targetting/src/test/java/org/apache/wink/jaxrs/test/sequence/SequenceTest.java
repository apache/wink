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

package org.apache.wink.jaxrs.test.sequence;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

/**
 * Verifies that a sequence of basic calls to the same resource have the
 * appropriate resource life-cycles.
 */
public class SequenceTest extends TestCase {

    private HttpClient client = new HttpClient();

    public static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/sequence";
    }

    /**
     * Calls a resource (which is not a singleton) several times. Verifies that
     * the resource instance is created each time.
     * 
     * @throws Exception
     */
    public void testHit100TimesRegularResource() throws Exception {
        client = new HttpClient();

        DeleteMethod deleteMethod = new DeleteMethod(getBaseURI() + "/sequence/static");
        try {
            client.executeMethod(deleteMethod);
            assertEquals(204, deleteMethod.getStatusCode());
        } finally {
            deleteMethod.releaseConnection();
        }

        deleteMethod = new DeleteMethod(getBaseURI() + "/sequence/constructor");
        try {
            client.executeMethod(deleteMethod);
            assertEquals(204, deleteMethod.getStatusCode());
        } finally {
            deleteMethod.releaseConnection();
        }

        for (int c = 0; c < 10; ++c) {
            GetMethod getMethod = new GetMethod(getBaseURI() + "/sequence");
            try {
                client.executeMethod(getMethod);
                assertEquals(200, getMethod.getStatusCode());
                assertEquals("0", new BufferedReader(new InputStreamReader(getMethod
                    .getResponseBodyAsStream(), getMethod.getResponseCharSet())).readLine());
            } finally {
                getMethod.releaseConnection();
            }

            getMethod = new GetMethod(getBaseURI() + "/sequence/static");
            try {
                client.executeMethod(getMethod);
                assertEquals(200, getMethod.getStatusCode());
                assertEquals("" + c, new BufferedReader(new InputStreamReader(getMethod
                    .getResponseBodyAsStream(), getMethod.getResponseCharSet())).readLine());
            } finally {
                getMethod.releaseConnection();
            }

            PostMethod postMethod = new PostMethod(getBaseURI() + "/sequence");
            try {
                client.executeMethod(postMethod);
                assertEquals(200, postMethod.getStatusCode());
                assertEquals("1", new BufferedReader(new InputStreamReader(postMethod
                    .getResponseBodyAsStream(), postMethod.getResponseCharSet())).readLine());
            } finally {
                postMethod.releaseConnection();
            }

            getMethod = new GetMethod(getBaseURI() + "/sequence/static");
            try {
                client.executeMethod(getMethod);
                assertEquals(200, getMethod.getStatusCode());
                assertEquals("" + (c + 1), new BufferedReader(new InputStreamReader(getMethod
                    .getResponseBodyAsStream(), getMethod.getResponseCharSet())).readLine());
            } finally {
                getMethod.releaseConnection();
            }

            getMethod = new GetMethod(getBaseURI() + "/sequence/constructor");
            try {
                client.executeMethod(getMethod);
                assertEquals(200, getMethod.getStatusCode());
                assertEquals("" + ((c + 1) * 5), new BufferedReader(new InputStreamReader(getMethod
                    .getResponseBodyAsStream(), getMethod.getResponseCharSet())).readLine());
            } finally {
                getMethod.releaseConnection();
            }
        }
    }

    /**
     * Calls a singleton resource several times. Verifies that the resource
     * instance is re-used each time.
     * 
     * @throws Exception
     */
    public void testHit100TimesSingletonResource() throws Exception {
        client = new HttpClient();

        DeleteMethod deleteMethod = new DeleteMethod(getBaseURI() + "/singletonsequence/static");
        try {
            client.executeMethod(deleteMethod);
            assertEquals(204, deleteMethod.getStatusCode());
        } finally {
            deleteMethod.releaseConnection();
        }

        for (int c = 0; c < 10; ++c) {
            GetMethod getMethod = new GetMethod(getBaseURI() + "/singletonsequence");
            try {
                client.executeMethod(getMethod);
                assertEquals(200, getMethod.getStatusCode());
                assertEquals("" + c, new BufferedReader(new InputStreamReader(getMethod
                    .getResponseBodyAsStream(), getMethod.getResponseCharSet())).readLine());
            } finally {
                getMethod.releaseConnection();
            }

            getMethod = new GetMethod(getBaseURI() + "/singletonsequence/static");
            try {
                client.executeMethod(getMethod);
                assertEquals(200, getMethod.getStatusCode());
                assertEquals("" + c, new BufferedReader(new InputStreamReader(getMethod
                    .getResponseBodyAsStream(), getMethod.getResponseCharSet())).readLine());
            } finally {
                getMethod.releaseConnection();
            }

            PostMethod postMethod = new PostMethod(getBaseURI() + "/singletonsequence");
            try {
                client.executeMethod(postMethod);
                assertEquals(200, postMethod.getStatusCode());
                assertEquals("" + (c + 1), new BufferedReader(new InputStreamReader(postMethod
                    .getResponseBodyAsStream(), postMethod.getResponseCharSet())).readLine());
            } finally {
                postMethod.releaseConnection();
            }

            getMethod = new GetMethod(getBaseURI() + "/singletonsequence/static");
            try {
                client.executeMethod(getMethod);
                assertEquals(200, getMethod.getStatusCode());
                assertEquals("" + (c + 1), new BufferedReader(new InputStreamReader(getMethod
                    .getResponseBodyAsStream(), getMethod.getResponseCharSet())).readLine());
            } finally {
                getMethod.releaseConnection();
            }

            /*
             * the constructor for this resource should never be more than 1.
             * note the constructor hit count is never cleared.
             */
            getMethod = new GetMethod(getBaseURI() + "/singletonsequence/constructor");
            try {
                client.executeMethod(getMethod);
                assertEquals(200, getMethod.getStatusCode());
                assertEquals("1", new BufferedReader(new InputStreamReader(getMethod
                    .getResponseBodyAsStream(), getMethod.getResponseCharSet())).readLine());
            } finally {
                getMethod.releaseConnection();
            }
        }
    }
}
