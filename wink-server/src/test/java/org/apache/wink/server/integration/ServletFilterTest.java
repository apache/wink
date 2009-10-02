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
package org.apache.wink.server.integration;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ServletFilterTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {TestResource.class};
    }

    @Path("/test")
    public static class TestResource {
        @POST
        @Produces("text/plain")
        public String postFoo(@FormParam("formParam") String requestSingle,
                              @FormParam("formParamMulti") String[] requestMulti) {
            // make sure we can still "see" the request after the filter
            // consumed it
            return requestSingle + "_" + requestMulti[1] + "_response";
        }
    }

    public final class MyServletFilter implements Filter {

        public void destroy() {
            // nothing needed for test
        }

        public void init(FilterConfig arg0) throws ServletException {
            // nothing needed for test
        }

        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
            // consume the request body
            ServletInputStream is = request.getInputStream();
            while (is.read() != -1) {
                // munch munch
            }
            is.close();
        }

    }

    @Test
    public void testServletFilter() throws Exception {
        MockHttpServletRequest servletRequest =
            MockRequestConstructor.constructMockRequest("POST", "/test", MediaType.TEXT_PLAIN);
        servletRequest.addHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        servletRequest.setContent("formParam=single&formParamMulti=one&formParamMulti=two"
            .getBytes());
        servletRequest.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        servletRequest.setParameter("formParam", "single");
        servletRequest.setParameter("formParamMulti", new String[] {"one", "two"});

        // Perform servletFilter.doFilter just before HttpServlet.service is
        // called in invoke.
        // Honestly, having a real servlet filter here is overkill. We could
        // have just as simply NOT
        // set any content on the servletRequest object to simulate consumption
        // of the request message body.
        // In the interest of brevity, however, let's do it the right way.
        Filter servletFilter = new MyServletFilter();
        servletFilter.doFilter(servletRequest, null, null);

        MockHttpServletResponse servletResponse = invoke(servletRequest);
        assertEquals("single_two_response", servletResponse.getContentAsString());
    }

}
