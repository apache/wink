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
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.wink.server.internal.DeploymentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Filter that is used by the runtime to handle the incoming request.
 * </p>
 * Initialization and configuration options are the same as the RestServlet.
 * 
 * @see RestServlet
 */
public class RestFilter implements Filter {

    private RestServlet         restServlet;

    private static final Logger logger = LoggerFactory.getLogger(RestFilter.class);

    private static class FilteredHttpServletResponse extends HttpServletResponseWrapper {

        private static final Logger logger =
                                               LoggerFactory
                                                   .getLogger(FilteredHttpServletResponse.class);

        public FilteredHttpServletResponse(HttpServletResponse response) {
            super(response);
        }

        private int statusCode;

        @Override
        public void setStatus(int statusCode) {
            super.setStatus(statusCode);
            this.statusCode = statusCode;
            logger.debug("FilteredHttpServletResponse set status code to {}", statusCode);
        }

        @Override
        public void setStatus(int statusCode, String msg) {
            super.setStatus(statusCode, msg);
            this.statusCode = statusCode;
            logger.debug("FilteredHttpServletResponse set status code to {}", statusCode);
        }

        int getStatusCode() {
            return this.statusCode;
        }

    }

    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            FilteredHttpServletResponse httpServletResponse =
                new FilteredHttpServletResponse((HttpServletResponse)servletResponse);
            restServlet.service((HttpServletRequest)servletRequest,
                                (HttpServletResponse)httpServletResponse);

            if (!httpServletResponse.isCommitted() && httpServletResponse.getStatusCode() == HttpServletResponse.SC_NOT_FOUND) {
                /*
                 * reset the status to 200 so that if a future
                 * HttpServletResponse call uses an include (or writes
                 * directly), then the status code is like the filter was never
                 * invoked
                 */
                logger
                    .debug("Filter {} did not match a resource so letting request continue on FilterChain {}",
                           this,
                           chain);
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                chain.doFilter(servletRequest, servletResponse);
            }
        } else {
            logger
                .debug("Filter {} did not expect a non-HttpServletRequest and/or non-HttpServletResponse but letting chain continue");
            chain.doFilter(servletRequest, servletResponse);
        }
    }

    protected static class RestServletForFilter extends RestServlet {
        protected FilterConfig    filterConfig;

        private static final long serialVersionUID = 5230595914609866319L;

        public RestServletForFilter(FilterConfig config) {
            super();
            this.filterConfig = config;
        }

        @Override
        protected DeploymentConfiguration getDeploymentConfiguration()
            throws ClassNotFoundException, InstantiationException, IllegalAccessException,
            IOException {
            DeploymentConfiguration deploymentConfiguration = super.getDeploymentConfiguration();
            deploymentConfiguration.setFilterConfig(filterConfig);
            return deploymentConfiguration;
        }
    }

    public void init(final FilterConfig filterConfig) throws ServletException {
        restServlet = new RestServletForFilter(filterConfig);

        logger.debug("Initializing RestFilter {} with {} config and {} servlet", new Object[] {
            this, filterConfig, restServlet});
        restServlet.init(new ServletConfig() {

            public String getServletName() {
                return filterConfig.getFilterName();
            }

            public ServletContext getServletContext() {
                return filterConfig.getServletContext();
            }

            public Enumeration<?> getInitParameterNames() {
                return filterConfig.getInitParameterNames();
            }

            public String getInitParameter(String paramName) {
                return filterConfig.getInitParameter(paramName);
            }
        });
    }

    public void destroy() {
        logger.debug("Destroying RestFilter {}", this);
        restServlet.destroy();
    }
}
