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

package org.apache.wink.itest.param.entity;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class EntityValidationTest extends TestCase {

    private static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/param/entity/";
    }

    public void testValidationMultipleEntities() throws Exception {
        HttpClient httpclient = new HttpClient();

        GetMethod httpMethod = new GetMethod(getBaseURI() + "/params/multientity");
        try {
            httpclient.executeMethod(httpMethod);
            assertEquals(404, httpMethod.getStatusCode());
            // framework.defaults.test.FVTAssert
            // .assertInstallLogContainsException("ResourceValidationException");
            // if (Environment.getCurrentEnvironment() ==
            // Environment.GENERIC_WAS) {
            // framework.defaults.test.FVTAssert
            // .assertInstallLogContainsException("Uncaught exception created in one of the service methods "
            // + "of the servlet jaxrs.tests.validation.param.entity in "
            // + "application jaxrs.tests.validation.param.entity. "
            // + "Exception created : javax.servlet.ServletException: An error "
            // + "occurred validating JAX-RS artifacts in the application.");
            // }
        } finally {
            httpMethod.releaseConnection();
        }
    }
}
