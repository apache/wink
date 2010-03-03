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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.wink.common.internal.uri.UriEncoder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Parse HTTP request query - generate request parameters. This filter is needed
 * since WebSphere throws away parameters which lack values (e.g.
 * ...?create&resource-uri=abc - create parameter gets discarded).
 */
public class WebSphereParametersFilter implements Filter {

    public static final String CONTENT_TYPE_WWW_FORM_URLENCODED =
                                                                    "application/x-www-form-urlencoded"; //$NON-NLS-1$

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        if (request instanceof HttpServletRequest) {

            HttpServletRequest httpRequest = (HttpServletRequest)request;
            Map<String, List<String>> paramMapWithList = new HashMap<String, List<String>>();

            String query = httpRequest.getQueryString();
            if (query != null && query.length() != 0) {
                // parse query string

                parseQuery(paramMapWithList, query);
            }
            String contentType = httpRequest.getContentType();
            if (contentType != null && contentType.startsWith(CONTENT_TYPE_WWW_FORM_URLENCODED)) {
                // parse form data

                InputStreamReader isr = new InputStreamReader(httpRequest.getInputStream());
                StringWriter sw = new StringWriter();
                char[] buffer = new char[4096];
                int len;
                while ((len = isr.read(buffer)) > 0) {
                    sw.write(buffer, 0, len);
                }
                parseQuery(paramMapWithList, sw.toString());
            }
            if (!paramMapWithList.isEmpty()) {
                // something parsed - convert map to String -> String[] map

                Map<String, String[]> paramMap =
                    new HashMap<String, String[]>(paramMapWithList.size());
                for (Map.Entry<String, List<String>> e : paramMapWithList.entrySet()) {
                    paramMap.put(e.getKey(), e.getValue().toArray(new String[e.getValue().size()]));
                }

                // create wrapping request and forward to chain
                request = new ParametersFilterRequestWrapper(httpRequest, paramMap);
            }
        }
        // no query present
        chain.doFilter(request, response);
    }

    public void destroy() {
    }

    /**
     * Parse query into String -> ArrayList<String> map.
     * 
     * @param paramMap map of parameters (String -> ArrayList<String>)
     * @param query query to parse
     */
    private static void parseQuery(Map<String, List<String>> paramMap, String query) {

        StringTokenizer tokenizer = new StringTokenizer(query, "&"); //$NON-NLS-1$
        while (tokenizer.hasMoreTokens()) {

            String name;
            String value;
            String token = tokenizer.nextToken();

            int equal = token.indexOf('=');
            if (equal != -1) {
                name = UriEncoder.decodeString(token.substring(0, equal));
                value = UriEncoder.decodeString(token.substring(equal + 1));
            } else {
                name = UriEncoder.decodeString(token);
                value = ""; //$NON-NLS-1$
            }

            List<String> values = paramMap.get(name);
            if (values == null) {
                values = new ArrayList<String>(1);
            }
            values.add(value);
            paramMap.put(name, values);
        }
    }

    private static class ParametersFilterRequestWrapper extends HttpServletRequestWrapper {

        private Map<String, String[]> paramMap;

        ParametersFilterRequestWrapper(HttpServletRequest request, Map<String, String[]> paramMap) {
            super(request);
            this.paramMap = paramMap;
        }

        @Override
        public String[] getParameterValues(String name) {
            return paramMap.get(name);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(paramMap.keySet());
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return Collections.unmodifiableMap(paramMap);
        }

        @Override
        public String getParameter(String name) {
            String[] v = getParameterValues(name);
            return v == null ? null : v[0];
        }
    }

}
