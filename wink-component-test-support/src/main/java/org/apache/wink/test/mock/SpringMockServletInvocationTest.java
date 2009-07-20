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

package org.apache.wink.test.mock;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

/**
 * Base class for tests using mock servlet invocation (= invoking directly
 * servlet's method with Spring mock request/response).
 */
public abstract class SpringMockServletInvocationTest extends SpringAwareTestCase {

    private HttpServlet servlet;

    protected void setUp() throws Exception {
        super.setUp();

        servlet =
            (HttpServlet)Class.forName("org.apache.wink.server.internal.servlet.RestServlet")
                .newInstance();
        MockServletConfig servletConfig = new MockServletConfig(servletContext);
        servlet.init(servletConfig);
    }

    /**
     * Passes the test to the servlet instance simulating AS behaviour.
     * 
     * @param request the filled request
     * @return a new response as filled by the servlet
     * @throws IOException io error
     */
    public MockHttpServletResponse invoke(MockHttpServletRequest request) throws ServletException,
        IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        return response;
    }

}
