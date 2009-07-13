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

package org.apache.wink.server.internal.contexts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.wink.common.internal.runtime.RuntimeContext;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;

/**
 * <code>HttpServletRequestWrapperImpl</code> merely finds the real
 * <code>HttpServletRequestWrapper</code> on the RuntimeContext and delegates
 * the calls. A HttpServletRequestWrapper is needed for containers that strictly
 * follow the specification and do checks for it (i.e. Tomcat).
 */
public class HttpServletRequestWrapperImpl extends HttpServletRequestWrapper {

    private HttpServletRequestWrapper getCorrectRequest() {
        RuntimeContext context = RuntimeContextTLS.getRuntimeContext();
        if (context == null) {
            throw new IllegalStateException();
        }

        HttpServletRequestWrapper wrapper =
            RuntimeContextTLS.getRuntimeContext().getAttribute(HttpServletRequestWrapper.class);
        if (wrapper == null) {
            throw new IllegalStateException();
        }
        return wrapper;
    }

    @Override
    public void setRequest(ServletRequest request) {
        getCorrectRequest().setRequest(request);
    }

    @Override
    public ServletRequest getRequest() {
        return getCorrectRequest().getRequest();
    }

    public HttpServletRequestWrapperImpl() {
        super(new IllegalHttpServletRequest());
    }

    @Override
    public String getAuthType() {
        return getCorrectRequest().getAuthType();
    }

    @Override
    public String getContextPath() {
        return getCorrectRequest().getContextPath();
    }

    @Override
    public Cookie[] getCookies() {
        return getCorrectRequest().getCookies();
    }

    @Override
    public long getDateHeader(String name) {
        return getCorrectRequest().getDateHeader(name);
    }

    @Override
    public String getHeader(String name) {
        return getCorrectRequest().getHeader(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration getHeaderNames() {
        return getCorrectRequest().getHeaderNames();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration getHeaders(String name) {
        return getCorrectRequest().getHeaders(name);
    }

    @Override
    public int getIntHeader(String name) {
        return getCorrectRequest().getIntHeader(name);
    }

    @Override
    public String getMethod() {
        return getCorrectRequest().getMethod();
    }

    @Override
    public String getPathInfo() {
        return getCorrectRequest().getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return getCorrectRequest().getPathTranslated();
    }

    @Override
    public String getQueryString() {
        return getCorrectRequest().getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return getCorrectRequest().getRemoteUser();
    }

    @Override
    public String getRequestedSessionId() {
        return getCorrectRequest().getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        return getCorrectRequest().getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return getCorrectRequest().getRequestURL();
    }

    @Override
    public String getServletPath() {
        return getCorrectRequest().getServletPath();
    }

    @Override
    public HttpSession getSession() {
        return getCorrectRequest().getSession();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return getCorrectRequest().getSession(create);
    }

    @Override
    public Principal getUserPrincipal() {
        return getCorrectRequest().getUserPrincipal();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return getCorrectRequest().isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return getCorrectRequest().isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return getCorrectRequest().isRequestedSessionIdFromURL();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return getCorrectRequest().isRequestedSessionIdValid();
    }

    @Override
    public boolean isUserInRole(String role) {
        return getCorrectRequest().isUserInRole(role);
    }

    @Override
    public Object getAttribute(String name) {
        return getCorrectRequest().getAttribute(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration getAttributeNames() {
        return getCorrectRequest().getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return getCorrectRequest().getCharacterEncoding();
    }

    @Override
    public int getContentLength() {
        return getCorrectRequest().getContentLength();
    }

    @Override
    public String getContentType() {
        return getCorrectRequest().getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return getCorrectRequest().getInputStream();
    }

    @Override
    public String getLocalAddr() {
        return getCorrectRequest().getLocalAddr();
    }

    @Override
    public String getLocalName() {
        return getCorrectRequest().getLocalName();
    }

    @Override
    public int getLocalPort() {
        return getCorrectRequest().getLocalPort();
    }

    @Override
    public Locale getLocale() {
        return getCorrectRequest().getLocale();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration getLocales() {
        return getCorrectRequest().getLocales();
    }

    @Override
    public String getParameter(String name) {
        return getCorrectRequest().getParameter(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map getParameterMap() {
        return getCorrectRequest().getParameterMap();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Enumeration getParameterNames() {
        return getCorrectRequest().getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return getCorrectRequest().getParameterValues(name);
    }

    @Override
    public String getProtocol() {
        return getCorrectRequest().getProtocol();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return getCorrectRequest().getReader();
    }

    @Override
    public String getRealPath(String path) {
        return getCorrectRequest().getRealPath(path);
    }

    @Override
    public String getRemoteAddr() {
        return getCorrectRequest().getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return getCorrectRequest().getRemoteHost();
    }

    @Override
    public int getRemotePort() {
        return getCorrectRequest().getRemotePort();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return getCorrectRequest().getRequestDispatcher(path);
    }

    @Override
    public String getScheme() {
        return getCorrectRequest().getScheme();
    }

    @Override
    public String getServerName() {
        return getCorrectRequest().getServerName();
    }

    @Override
    public int getServerPort() {
        return getCorrectRequest().getServerPort();
    }

    @Override
    public boolean isSecure() {
        return getCorrectRequest().isSecure();
    }

    @Override
    public void removeAttribute(String name) {
        getCorrectRequest().removeAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object o) {
        getCorrectRequest().setAttribute(name, o);
    }

    @Override
    public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
        getCorrectRequest().setCharacterEncoding(enc);
    }

    static class IllegalHttpServletRequest implements HttpServletRequest {
        public String getAuthType() {
            throw new IllegalStateException();
        }

        public String getContextPath() {
            throw new IllegalStateException();
        }

        public Cookie[] getCookies() {
            throw new IllegalStateException();
        }

        public long getDateHeader(String arg0) {
            throw new IllegalStateException();
        }

        public String getHeader(String arg0) {
            throw new IllegalStateException();
        }

        @SuppressWarnings("unchecked")
        public Enumeration getHeaderNames() {
            throw new IllegalStateException();
        }

        @SuppressWarnings("unchecked")
        public Enumeration getHeaders(String arg0) {
            throw new IllegalStateException();
        }

        public int getIntHeader(String arg0) {
            throw new IllegalStateException();
        }

        public String getMethod() {
            throw new IllegalStateException();
        }

        public String getPathInfo() {
            throw new IllegalStateException();
        }

        public String getPathTranslated() {
            throw new IllegalStateException();
        }

        public String getQueryString() {
            throw new IllegalStateException();
        }

        public String getRemoteUser() {
            throw new IllegalStateException();
        }

        public String getRequestURI() {
            throw new IllegalStateException();
        }

        public StringBuffer getRequestURL() {
            throw new IllegalStateException();
        }

        public String getRequestedSessionId() {
            throw new IllegalStateException();
        }

        public String getServletPath() {
            throw new IllegalStateException();
        }

        public HttpSession getSession() {
            throw new IllegalStateException();
        }

        public HttpSession getSession(boolean arg0) {
            throw new IllegalStateException();
        }

        public Principal getUserPrincipal() {
            throw new IllegalStateException();
        }

        public boolean isRequestedSessionIdFromCookie() {
            throw new IllegalStateException();
        }

        public boolean isRequestedSessionIdFromURL() {
            throw new IllegalStateException();
        }

        public boolean isRequestedSessionIdFromUrl() {
            throw new IllegalStateException();
        }

        public boolean isRequestedSessionIdValid() {
            throw new IllegalStateException();
        }

        public boolean isUserInRole(String arg0) {
            throw new IllegalStateException();
        }

        public Object getAttribute(String arg0) {
            throw new IllegalStateException();
        }

        @SuppressWarnings("unchecked")
        public Enumeration getAttributeNames() {
            throw new IllegalStateException();
        }

        public String getCharacterEncoding() {
            throw new IllegalStateException();
        }

        public int getContentLength() {
            throw new IllegalStateException();
        }

        public String getContentType() {
            throw new IllegalStateException();
        }

        public ServletInputStream getInputStream() throws IOException {
            throw new IllegalStateException();
        }

        public String getLocalAddr() {
            throw new IllegalStateException();
        }

        public String getLocalName() {
            throw new IllegalStateException();
        }

        public int getLocalPort() {
            throw new IllegalStateException();
        }

        public Locale getLocale() {
            throw new IllegalStateException();
        }

        @SuppressWarnings("unchecked")
        public Enumeration getLocales() {
            throw new IllegalStateException();
        }

        public String getParameter(String arg0) {
            throw new IllegalStateException();
        }

        @SuppressWarnings("unchecked")
        public Map getParameterMap() {
            throw new IllegalStateException();
        }

        @SuppressWarnings("unchecked")
        public Enumeration getParameterNames() {
            throw new IllegalStateException();
        }

        public String[] getParameterValues(String arg0) {
            throw new IllegalStateException();
        }

        public String getProtocol() {
            throw new IllegalStateException();
        }

        public BufferedReader getReader() throws IOException {
            throw new IllegalStateException();
        }

        public String getRealPath(String arg0) {
            throw new IllegalStateException();
        }

        public String getRemoteAddr() {
            throw new IllegalStateException();
        }

        public String getRemoteHost() {
            throw new IllegalStateException();
        }

        public int getRemotePort() {
            throw new IllegalStateException();
        }

        public RequestDispatcher getRequestDispatcher(String arg0) {
            throw new IllegalStateException();
        }

        public String getScheme() {
            throw new IllegalStateException();
        }

        public String getServerName() {
            throw new IllegalStateException();
        }

        public int getServerPort() {
            throw new IllegalStateException();
        }

        public boolean isSecure() {
            throw new IllegalStateException();
        }

        public void removeAttribute(String arg0) {
            throw new IllegalStateException();
        }

        public void setAttribute(String arg0, Object arg1) {
            throw new IllegalStateException();
        }

        public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
            throw new IllegalStateException();
        }

    }
}
