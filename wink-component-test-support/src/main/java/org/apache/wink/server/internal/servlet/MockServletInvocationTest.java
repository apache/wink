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
package org.apache.wink.server.internal.servlet;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

/**
 * <p>
 * The unit tests should extend this class in order to simulate the servlet
 * invocation.
 * <p>
 * The method <tt>invoke</tt> invokes the servlet call.
 */
public abstract class MockServletInvocationTest extends TestCase {

    private static ThreadLocal<MockServletInvocationTest> tls =
                                                                  new ThreadLocal<MockServletInvocationTest>();
    private HttpServlet                                   servlet;

    public static class MockApplication extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> set = new LinkedHashSet<Class<?>>();
            for (Class<?> cls : tls.get().getClasses()) {
                set.add(cls);
            }
            return set;
        }

        @Override
        public Set<Object> getSingletons() {
            HashSet<Object> set = new LinkedHashSet<Object>();
            for (Object o : tls.get().getSingletons()) {
                set.add(o);
            }
            return set;
        }

    }

    /**
     * Returns the class name of an application class. Override this method in
     * order to provide a custom Application class. Pay attention that when
     * overridden, the methods getClasses() and getSingletons() will be ignored.
     */
    protected String getApplicationClassName() {
        return MockApplication.class.getName();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        servlet =
            (HttpServlet)Class.forName("org.apache.wink.server.internal.servlet.RestServlet")
                .newInstance();
        MockServletConfig servletConfig = new MockServletConfig();
        servletConfig.addInitParameter("javax.ws.rs.Application", getApplicationClassName());

        String propertiesFile = getPropertiesFile();
        if (propertiesFile != null) {
            servletConfig.addInitParameter("propertiesLocation", propertiesFile);
        }

        tls.set(this);
        servlet.init(servletConfig);
    }

    /**
     * @return the name of the properties file. The file must be located on the
     *         classpath. By default returns null.
     */
    protected String getPropertiesFile() {
        return null;
    }

    /**
     * @return the classes of JAX-RS that will be returned by the
     *         Application.getClasses()
     */
    protected Class<?>[] getClasses() {
        return new Class<?>[0];
    }

    /**
     * @return the singletons of JAX-RS that will be returned by the
     *         Application.getSingletons
     */
    protected Object[] getSingletons() {
        return new Object[0];
    }

    /**
     * Passes the test to the servlet instance simulating AS behaviour.
     * 
     * @param request the filled request
     * @return a new response as filled by the servlet
     * @throws IOException
     * @throws ServletException
     */
    public MockHttpServletResponse invoke(MockHttpServletRequest request) throws ServletException,
        IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        return response;
    }

}
