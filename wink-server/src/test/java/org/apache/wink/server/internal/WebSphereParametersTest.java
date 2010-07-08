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

package org.apache.wink.server.internal;

import java.util.Arrays;

import javax.servlet.ServletRequest;

import junit.framework.TestCase;

import org.apache.wink.server.internal.servlet.WebSphereParametersFilter;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class WebSphereParametersTest extends TestCase {

    /*
     * Test GET parameters.
     */
    public void testGetParametersFilter() throws Exception {

        // create mock servlet objects
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setQueryString("a&b=&c=1&c=2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // test query parameters before parsing
        assertEquals("Query parameters are not empty before parsing", 0, request.getParameterMap()
            .size());

        // invoke filter
        new WebSphereParametersFilter().doFilter(request, response, chain);

        // test query parameters
        assertEquals("There are not 3 query parameters", 3, chain.getRequest().getParameterMap()
            .size());
        assertParameters(chain.getRequest(), "a", "b", "c");
    }

    /*
     * Test POST (+GET) parameters.
     */
    public void testPostParametersFilter() throws Exception {

        // create mock servlet objects
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setQueryString("a&b=&c=1&c=2");
        request.setContentType(WebSphereParametersFilter.CONTENT_TYPE_WWW_FORM_URLENCODED);
        request.setContent("d&e=&f=1&f=2".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // test query parameters before parsing
        assertEquals("Query parameters are not empty before parsing", 0, request.getParameterMap()
            .size());

        // invoke filter
        new WebSphereParametersFilter().doFilter(request, response, chain);

        // test query and POST parameters
        assertEquals("There are not 6 query parameters", 6, chain.getRequest().getParameterMap()
            .size());
        assertParameters(chain.getRequest(), "a", "b", "c");
        assertParameters(chain.getRequest(), "d", "e", "f");
    }

    private static void assertParameters(ServletRequest request,
                                         String nameNonValue,
                                         String nameEmpty,
                                         String nameDouble12) {

        assertEquals("Parameter " + nameNonValue + " has value", "", request
            .getParameter(nameNonValue));
        assertEquals("Parameter " + nameEmpty + " is not empty", "", request
            .getParameter(nameEmpty));
        assertEquals("Parameter " + nameDouble12 + " has not two values",
                     Arrays.asList("1", "2"),
                     Arrays.asList(request.getParameterValues(nameDouble12)));
    }
}
