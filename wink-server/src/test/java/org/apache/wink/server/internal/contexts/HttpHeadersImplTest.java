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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.handlers.ServerMessageContext;

public class HttpHeadersImplTest extends TestCase {

    /**
     * Tests that null values returned for header values are ignored. See
     * [WINK-188]
     */
    public void testRequestHeaderReturnsNull() {

        ServerMessageContext context = new ServerMessageContext(new HttpServletRequest() {

            public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {

            }

            public void setAttribute(String arg0, Object arg1) {

            }

            public void removeAttribute(String arg0) {

            }

            public boolean isSecure() {

                return false;
            }

            public int getServerPort() {

                return 0;
            }

            public String getServerName() {

                return null;
            }

            public String getScheme() {

                return null;
            }

            public RequestDispatcher getRequestDispatcher(String arg0) {

                return null;
            }

            public int getRemotePort() {

                return 0;
            }

            public String getRemoteHost() {

                return null;
            }

            public String getRemoteAddr() {

                return null;
            }

            public String getRealPath(String arg0) {

                return null;
            }

            public BufferedReader getReader() throws IOException {

                return null;
            }

            public String getProtocol() {

                return null;
            }

            public String[] getParameterValues(String arg0) {

                return null;
            }

            public Enumeration getParameterNames() {

                return null;
            }

            public Map getParameterMap() {

                return null;
            }

            public String getParameter(String arg0) {

                return null;
            }

            public Enumeration getLocales() {

                return null;
            }

            public Locale getLocale() {

                return null;
            }

            public int getLocalPort() {

                return 0;
            }

            public String getLocalName() {

                return null;
            }

            public String getLocalAddr() {
                return null;
            }

            public ServletInputStream getInputStream() throws IOException {
                return null;
            }

            public String getContentType() {
                return null;
            }

            public int getContentLength() {
                return 0;
            }

            public String getCharacterEncoding() {
                return null;
            }

            public Enumeration getAttributeNames() {
                return null;
            }

            public Object getAttribute(String arg0) {
                return null;
            }

            public boolean isUserInRole(String arg0) {
                return false;
            }

            public boolean isRequestedSessionIdValid() {
                return false;
            }

            public boolean isRequestedSessionIdFromUrl() {
                return false;
            }

            public boolean isRequestedSessionIdFromURL() {
                return false;
            }

            public boolean isRequestedSessionIdFromCookie() {
                return false;
            }

            public Principal getUserPrincipal() {
                return null;
            }

            public HttpSession getSession(boolean arg0) {
                return null;
            }

            public HttpSession getSession() {
                return null;
            }

            public String getServletPath() {
                return null;
            }

            public String getRequestedSessionId() {
                return null;
            }

            public StringBuffer getRequestURL() {
                return null;
            }

            public String getRequestURI() {
                return null;
            }

            public String getRemoteUser() {
                return null;
            }

            public String getQueryString() {
                return null;
            }

            public String getPathTranslated() {
                return null;
            }

            public String getPathInfo() {
                return null;
            }

            public String getMethod() {
                return null;
            }

            public int getIntHeader(String arg0) {
                return 0;
            }

            public Enumeration getHeaders(String arg0) {
                if ("Accept".equals(arg0)) {
                    Vector<String> values = new Vector<String>();
                    values.add("text/xml");
                    values.add(null);
                    return values.elements();
                }
                return null;
            }

            public Enumeration getHeaderNames() {
                Vector<String> headers = new Vector<String>(Collections.singletonList("Accept"));
                return headers.elements();
            }

            public String getHeader(String arg0) {
                return null;
            }

            public long getDateHeader(String arg0) {
                return 0;
            }

            public Cookie[] getCookies() {
                return null;
            }

            public String getContextPath() {
                return null;
            }

            public String getAuthType() {
                return null;
            }
        }, new HttpServletResponse() {

            public void setLocale(Locale arg0) {

            }

            public void setContentType(String arg0) {

            }

            public void setContentLength(int arg0) {

            }

            public void setCharacterEncoding(String arg0) {

            }

            public void setBufferSize(int arg0) {

            }

            public void resetBuffer() {

            }

            public void reset() {

            }

            public boolean isCommitted() {

                return false;
            }

            public PrintWriter getWriter() throws IOException {

                return null;
            }

            public ServletOutputStream getOutputStream() throws IOException {

                return null;
            }

            public Locale getLocale() {

                return null;
            }

            public String getContentType() {

                return null;
            }

            public String getCharacterEncoding() {

                return null;
            }

            public int getBufferSize() {

                return 0;
            }

            public void flushBuffer() throws IOException {

            }

            public void setStatus(int arg0, String arg1) {

            }

            public void setStatus(int arg0) {

            }

            public void setIntHeader(String arg0, int arg1) {

            }

            public void setHeader(String arg0, String arg1) {

            }

            public void setDateHeader(String arg0, long arg1) {

            }

            public void sendRedirect(String arg0) throws IOException {

            }

            public void sendError(int arg0, String arg1) throws IOException {

            }

            public void sendError(int arg0) throws IOException {

            }

            public String encodeUrl(String arg0) {

                return null;
            }

            public String encodeURL(String arg0) {

                return null;
            }

            public String encodeRedirectUrl(String arg0) {

                return null;
            }

            public String encodeRedirectURL(String arg0) {

                return null;
            }

            public boolean containsHeader(String arg0) {

                return false;
            }

            public void addIntHeader(String arg0, int arg1) {

            }

            public void addHeader(String arg0, String arg1) {

            }

            public void addDateHeader(String arg0, long arg1) {

            }

            public void addCookie(Cookie arg0) {

            }
        }, new DeploymentConfiguration() {
        });

        HttpHeadersImpl headers = new HttpHeadersImpl(context);
        List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
        assertEquals(1, mediaTypes.size());
        assertEquals(MediaType.TEXT_XML_TYPE, headers.getAcceptableMediaTypes().get(0));
    }

}
