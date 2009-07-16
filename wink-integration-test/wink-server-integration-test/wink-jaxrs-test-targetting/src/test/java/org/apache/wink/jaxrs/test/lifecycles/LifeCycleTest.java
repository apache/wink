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

package org.apache.wink.jaxrs.test.lifecycles;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class LifeCycleTest extends TestCase {

    public static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/lifecycles";
    }

    /**
     * Tests that providers are singletons no matter what.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testProvidersAreSingleton() throws HttpException, IOException {
        HttpClient client = new HttpClient();
        StringBuffer sb = new StringBuffer();
        for (long c = 0; c < 5000; ++c) {
            sb.append("a");
        }
        for (int counter = 0; counter < 100; ++counter) {
            PostMethod postMethod = new PostMethod(getBaseURI() + "/jaxrs/tests/lifecycles");
            try {
                postMethod.setRequestEntity(new StringRequestEntity(sb.toString(), "text/plain",
                                                                    null));
                client.executeMethod(postMethod);
                assertEquals(200, postMethod.getStatusCode());
                assertEquals(sb.toString(), postMethod.getResponseBodyAsString());
            } finally {
                postMethod.releaseConnection();
            }
        }

        GetMethod getMethod = new GetMethod(getBaseURI() + "/jaxrs/tests/lifecycles");
        client.executeMethod(getMethod);
        assertEquals(200, getMethod.getStatusCode());
        assertEquals("1:100:100:101:100:1", getMethod.getResponseBodyAsString());
    }
}
