/*******************************************************************************
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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/

package org.apache.wink.server.internal.registry;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class FindResourceMethod2Test extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return FindResourceMethod1Test.resourceClasses.toArray(new Class<?>[0]);
    }

    @Override
    protected String getPropertiesFile() {
        return TestUtils.packageToPath(FindResourceMethod2Test.class.getPackage().getName()) + "\\FindResourceMethodTest2.properties";
    }

    public void testContinuedSearch_1_2() throws Exception {
        MockHttpServletRequest request = null;
        MockHttpServletResponse response = null;

        // 1.2. make sure that ContinuedSearchResource is reachable when
        // continued search policy is activated
        request =
            MockRequestConstructor.constructMockRequest("PUT",
                                                        "/simpleGet",
                                                        "text/plain",
                                                        "text/plain",
                                                        null);
        response = invoke(request);
        FindResourceMethodTest
            .assertMethodFound(response,
                               FindResourceMethod1Test.ContinuedSearchResource.class,
                               "put");
    }

    public void testContinuedSearch_2_2() throws Exception {
        MockHttpServletRequest request = null;
        MockHttpServletResponse response = null;

        // 2.2. make sure that ContinuedSearchResource is reachable when
        // continued search policy is activated
        request =
            MockRequestConstructor.constructMockRequest("PUT",
                                                        "/subResourceMethodSimpleGet/1",
                                                        "text/plain",
                                                        "text/plain",
                                                        null);
        response = invoke(request);
        FindResourceMethodTest
            .assertMethodFound(response,
                               FindResourceMethod1Test.ContinuedSearchResource.class,
                               "subPut");

    }

    public void testContinuedSearch_3_2() throws Exception {
        MockHttpServletRequest request = null;
        MockHttpServletResponse response = null;

        // 3.2. make sure that ContinuedSearchResource is reachable when
        // continued search policy is activated
        request =
            MockRequestConstructor.constructMockRequest("PUT",
                                                        "/continuedSearchResourceLocatorBad/1/2",
                                                        "text/plain",
                                                        "text/plain",
                                                        null);
        response = invoke(request);
        FindResourceMethodTest
            .assertMethodFound(response,
                               FindResourceMethod1Test.LocatedContinuedSearchResource.class,
                               "subPut");
    }
}
