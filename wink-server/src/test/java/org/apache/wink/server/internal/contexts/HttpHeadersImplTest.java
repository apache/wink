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

import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.handlers.ServerMessageContext;
import org.jmock.Expectations;
import org.jmock.Mockery;

public class HttpHeadersImplTest extends TestCase {

    /**
     * Tests that null values returned for header values are ignored. See
     * [WINK-188]
     */
    public void testRequestHeaderReturnsNull() {

        final Vector<String> values = new Vector<String>();
        values.add("text/xml");
        values.add(null);

        Mockery mockery = new Mockery();
        final HttpServletRequest requestMock = mockery.mock(HttpServletRequest.class);
        final HttpServletResponse responseMock = mockery.mock(HttpServletResponse.class);
        mockery.checking(new Expectations() {
            {
                oneOf(requestMock).getMethod();
                will(returnValue(null));
                oneOf(requestMock).getQueryString();
                will(returnValue(null));
                oneOf(requestMock).getHeaders("Accept");
                will(returnValue(values.elements()));
            }
        });
        ServerMessageContext context =
            new ServerMessageContext(requestMock, responseMock, new DeploymentConfiguration());
        HttpHeadersImpl headers = new HttpHeadersImpl(context);
        List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
        assertEquals(1, mediaTypes.size());
        assertEquals(MediaType.TEXT_XML_TYPE, headers.getAcceptableMediaTypes().get(0));
        mockery.assertIsSatisfied();

    }

    /**
     * See [WINK-317]
     */
    public void testSingleCookieValuesInSingleHeader() {
        final Vector<String> values = new Vector<String>();
        values.add("$Version=\"1\"; MyName=\"MyValue\"; $Path=\"/somewhere\"");

        Mockery mockery = new Mockery();
        final HttpServletRequest requestMock = mockery.mock(HttpServletRequest.class);
        final HttpServletResponse responseMock = mockery.mock(HttpServletResponse.class);
        mockery.checking(new Expectations() {
            {
                oneOf(requestMock).getMethod();
                will(returnValue(null));
                oneOf(requestMock).getHeaders(HttpHeaders.COOKIE);
                will(returnValue(values.elements()));
            }
        });
        ServerMessageContext context =
            new ServerMessageContext(requestMock, responseMock, new DeploymentConfiguration());
        HttpHeadersImpl headers = new HttpHeadersImpl(context);
        Map<String, Cookie> cookies = headers.getCookies();
        assertEquals(new Cookie("MyName", "MyValue", "/somewhere", null), cookies.get("MyName"));
        assertEquals(1, cookies.size());
        mockery.assertIsSatisfied();
    }

    /**
     * See [WINK-317]
     */
    public void testNoCookieValuesInSingleHeader() {
        final Vector<String> values = new Vector<String>();

        Mockery mockery = new Mockery();
        final HttpServletRequest requestMock = mockery.mock(HttpServletRequest.class);
        final HttpServletResponse responseMock = mockery.mock(HttpServletResponse.class);
        mockery.checking(new Expectations() {
            {
                oneOf(requestMock).getMethod();
                will(returnValue(null));
                oneOf(requestMock).getHeaders(HttpHeaders.COOKIE);
                will(returnValue(values.elements()));
            }
        });
        ServerMessageContext context =
            new ServerMessageContext(requestMock, responseMock, new DeploymentConfiguration());
        HttpHeadersImpl headers = new HttpHeadersImpl(context);
        Map<String, Cookie> cookies = headers.getCookies();
        assertNull(cookies.get("MyName"));
        assertEquals(0, cookies.size());
        mockery.assertIsSatisfied();
    }

    /**
     * See [WINK-317]
     */
    public void testMultipleCookieValuesInSingleHeader() {
        final Vector<String> values = new Vector<String>();
        values
            .add("$Version=\"1\";MyOtherName=\"MyOtherValue\"; $Path=\"/else\"; $Domain=\"mydomain.com\"; MyName=\"MyValue\"; $Path=\"/somewhere\"; ");

        Mockery mockery = new Mockery();
        final HttpServletRequest requestMock = mockery.mock(HttpServletRequest.class);
        final HttpServletResponse responseMock = mockery.mock(HttpServletResponse.class);
        mockery.checking(new Expectations() {
            {
                oneOf(requestMock).getMethod();
                will(returnValue(null));
                oneOf(requestMock).getHeaders(HttpHeaders.COOKIE);
                will(returnValue(values.elements()));
            }
        });
        ServerMessageContext context =
            new ServerMessageContext(requestMock, responseMock, new DeploymentConfiguration());
        HttpHeadersImpl headers = new HttpHeadersImpl(context);
        Map<String, Cookie> cookies = headers.getCookies();
        assertEquals(new Cookie("MyName", "MyValue", "/somewhere", null), cookies.get("MyName"));
        assertEquals(new Cookie("MyOtherName", "MyOtherValue", "/else", "mydomain.com"), cookies
            .get("MyOtherName"));
        assertEquals(2, cookies.size());
        mockery.assertIsSatisfied();
    }
}
