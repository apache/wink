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

package org.apache.wink.test.integration;

import org.junit.Assert;

/**
 * Assertions to deal with responses from various containers. Some containers
 * like to have default error pages.
 */
final public class ServerContainerAssertions {

    /**
     * For exceptions that are propagated to the container, verify that the
     * exception has the expected information.
     * 
     * @param statusCode
     * @param responseBody
     */
    public static void assertExceptionBodyFromServer(int statusCode, String responseBody) {
        if ("tomcat".equals(ServerEnvironmentInfo.getContainerName())) {
            Assert.assertTrue(responseBody, responseBody.contains(Integer.toString(statusCode)));
            return;
        }
        Assert.assertTrue(responseBody, (responseBody == null) || ("".equals(responseBody)));
    }
}
