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

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * 
 */
public class MockHttpServletRequestWrapperTestCase {
    @Test
    public void testContentType() {
        HttpServletRequest req =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test",
                                                        "application/json",
                                                        "application/x-www-form-urlencoded",
                                                        null);
        Assert.assertEquals("application/x-www-form-urlencoded", req.getContentType());
        Assert.assertEquals("application/json", req.getHeader("Accept"));
    }

    @Test
    public void testParameter() throws Exception {
        HttpServletRequest req =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test",
                                                        "application/json",
                                                        "application/x-www-form-urlencoded",
                                                        "x=1&y=2&z=%20".getBytes("UTF-8"));
        String x = req.getParameter("x");
        String y = req.getParameter("y");
        String z = req.getParameter("z");
        String a = req.getParameter("a");

        Assert.assertEquals("1", x);
        Assert.assertEquals("2", y);
        Assert.assertEquals(" ", z);
        Assert.assertNull(a);

        Assert.assertEquals(3, req.getParameterMap().size());
    }

    @Test
    public void testQuery() throws Exception {
        MockHttpServletRequest req =
            MockRequestConstructor.constructMockRequest("GET", "/test", "application/json", (String)null, null);
        req.setQueryString("x=1&y=2&z=%20");
        String x = req.getParameter("x");
        String y = req.getParameter("y");
        String z = req.getParameter("z");
        String a = req.getParameter("a");

        Assert.assertEquals("1", x);
        Assert.assertEquals("2", y);
        Assert.assertEquals(" ", z);
        Assert.assertNull(a);

        Assert.assertEquals(3, req.getParameterMap().size());
    }
}
