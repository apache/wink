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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.wink.common.internal.runtime.RuntimeContext;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;

/**
 * <code>HttpServletResponseWrapperImpl</code> merely finds the real
 * <code>HttpServletResponseWrapper</code> on the RuntimeContext and delegates
 * the calls. A HttpServletResponseWrapper is needed for containers that
 * strictly follow the specification and do checks for it (i.e. Tomcat).
 */
public class HttpServletResponseWrapperImpl extends HttpServletResponseWrapper {

    private HttpServletResponseWrapper getCorrectResponse() {
        RuntimeContext context = RuntimeContextTLS.getRuntimeContext();
        if (context == null) {
            throw new IllegalStateException();
        }

        HttpServletResponseWrapper wrapper =
            RuntimeContextTLS.getRuntimeContext().getAttribute(HttpServletResponseWrapper.class);
        if (wrapper == null) {
            throw new IllegalStateException();
        }
        return wrapper;
    }

    @Override
    public void setResponse(ServletResponse response) {
        getCorrectResponse().setResponse(response);
    }

    @Override
    public ServletResponse getResponse() {
        return getCorrectResponse().getResponse();
    }

    public HttpServletResponseWrapperImpl() {
        super(new IllegalHttpServletResponse());
    }

    @Override
    public void addCookie(Cookie cookie) {
        getCorrectResponse().addCookie(cookie);
    }

    @Override
    public void addDateHeader(String name, long date) {
        getCorrectResponse().addDateHeader(name, date);
    }

    @Override
    public void addHeader(String name, String value) {
        getCorrectResponse().addHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        getCorrectResponse().addIntHeader(name, value);
    }

    @Override
    public boolean containsHeader(String name) {
        return getCorrectResponse().containsHeader(name);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return getCorrectResponse().encodeRedirectUrl(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return getCorrectResponse().encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(String url) {
        return getCorrectResponse().encodeUrl(url);
    }

    @Override
    public String encodeURL(String url) {
        return getCorrectResponse().encodeURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        getCorrectResponse().sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        getCorrectResponse().sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        getCorrectResponse().sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        getCorrectResponse().setDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        getCorrectResponse().setHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        getCorrectResponse().setIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc, String sm) {
        getCorrectResponse().setStatus(sc, sm);
    }

    @Override
    public void setStatus(int sc) {
        getCorrectResponse().setStatus(sc);
    }

    @Override
    public void flushBuffer() throws IOException {
        getCorrectResponse().flushBuffer();
    }

    @Override
    public int getBufferSize() {
        return getCorrectResponse().getBufferSize();
    }

    @Override
    public String getCharacterEncoding() {
        return getCorrectResponse().getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return getCorrectResponse().getContentType();
    }

    @Override
    public Locale getLocale() {
        return getCorrectResponse().getLocale();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return getCorrectResponse().getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return getCorrectResponse().getWriter();
    }

    @Override
    public boolean isCommitted() {
        return getCorrectResponse().isCommitted();
    }

    @Override
    public void reset() {
        getCorrectResponse().reset();
    }

    @Override
    public void resetBuffer() {
        getCorrectResponse().resetBuffer();
    }

    @Override
    public void setBufferSize(int size) {
        getCorrectResponse().setBufferSize(size);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        getCorrectResponse().setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        getCorrectResponse().setContentLength(len);
    }

    @Override
    public void setContentType(String type) {
        getCorrectResponse().setContentType(type);
    }

    @Override
    public void setLocale(Locale loc) {
        getCorrectResponse().setLocale(loc);
    }

    static class IllegalHttpServletResponse implements HttpServletResponse {

        public void setLocale(Locale arg0) {
            throw new IllegalStateException();
        }

        public void setContentType(String arg0) {
            throw new IllegalStateException();
        }

        public void setContentLength(int arg0) {
            throw new IllegalStateException();
        }

        public void setCharacterEncoding(String arg0) {
            throw new IllegalStateException();
        }

        public void setBufferSize(int arg0) {
            throw new IllegalStateException();
        }

        public void resetBuffer() {
            throw new IllegalStateException();
        }

        public void reset() {
            throw new IllegalStateException();
        }

        public boolean isCommitted() {
            throw new IllegalStateException();
        }

        public PrintWriter getWriter() throws IOException {
            throw new IllegalStateException();
        }

        public ServletOutputStream getOutputStream() throws IOException {
            throw new IllegalStateException();
        }

        public Locale getLocale() {
            throw new IllegalStateException();
        }

        public String getContentType() {
            throw new IllegalStateException();
        }

        public String getCharacterEncoding() {
            throw new IllegalStateException();
        }

        public int getBufferSize() {
            throw new IllegalStateException();
        }

        public void flushBuffer() throws IOException {
            throw new IllegalStateException();
        }

        public void setStatus(int arg0, String arg1) {
            throw new IllegalStateException();
        }

        public void setStatus(int arg0) {
            throw new IllegalStateException();
        }

        public void setIntHeader(String arg0, int arg1) {
            throw new IllegalStateException();
        }

        public void setHeader(String arg0, String arg1) {
            throw new IllegalStateException();
        }

        public void setDateHeader(String arg0, long arg1) {
            throw new IllegalStateException();
        }

        public void sendRedirect(String arg0) throws IOException {
            throw new IllegalStateException();
        }

        public void sendError(int arg0, String arg1) throws IOException {
            throw new IllegalStateException();
        }

        public void sendError(int arg0) throws IOException {
            throw new IllegalStateException();
        }

        public String encodeUrl(String arg0) {
            throw new IllegalStateException();
        }

        public String encodeURL(String arg0) {
            throw new IllegalStateException();
        }

        public String encodeRedirectUrl(String arg0) {
            throw new IllegalStateException();
        }

        public String encodeRedirectURL(String arg0) {
            throw new IllegalStateException();
        }

        public boolean containsHeader(String arg0) {
            throw new IllegalStateException();
        }

        public void addIntHeader(String arg0, int arg1) {
            throw new IllegalStateException();
        }

        public void addHeader(String arg0, String arg1) {
            throw new IllegalStateException();
        }

        public void addDateHeader(String arg0, long arg1) {
            throw new IllegalStateException();
        }

        public void addCookie(Cookie arg0) {
            throw new IllegalStateException();
        }

    }
}
