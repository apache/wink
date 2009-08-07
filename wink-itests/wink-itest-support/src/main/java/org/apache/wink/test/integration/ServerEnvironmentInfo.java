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

/**
 * This class provides some basic information for clients about the server.
 */
final public class ServerEnvironmentInfo {

    public static String getHostname() {
        return System.getProperty("wink-test-hostname");
    }

    public static String getPort() {
        return System.getProperty("wink-test-port");
    }

    public static String getContextRoot() {
        return System.getProperty("wink-test-context-root");
    }

    public static String getBaseURI() {
        String contextRoot = getContextRoot();
        if (contextRoot == null) {
            return "http://" + getHostname() + ":" + getPort();
        }
        return "http://" + getHostname() + ":" + getPort() + "/" + contextRoot;
    }

    public static String getWorkDir() {
        return System.getProperty("wink-test-work-dir");
    }

    static String getContainerName() {
        /*
         * tests should not use this
         */
        return System.getProperty("wink-test-container");
    }
}
