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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.wink.server.internal.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.internal.log.Requests.LoggedServletInputStream;
import org.apache.wink.server.internal.log.Requests.RequestWrapper;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

public class RequestsTest {

    private Mockery mockContext = new Mockery() {
                                    {
                                        setImposteriser(ClassImposteriser.INSTANCE);
                                    }
                                };

    /**
     * The Requests object should do nothing.
     */
    @Test
    public void testRequestsInitWithNull() {
        Requests r = new Requests();
        r.init(null);
        mockContext.assertIsSatisfied();
    }

    /**
     * The Requests object should do nothing.
     */
    @Test
    public void testRequestsInitWithProperties() {
        Requests r = new Requests();
        final Properties props = mockContext.mock(Properties.class);
        r.init(props);
        mockContext.assertIsSatisfied();
    }

    @Before
    public void setUp() {
        Logger.getLogger("org.apache.wink").setLevel(Level.INFO);
    }

    /**
     * The Requests object should register its wrapper inside of the Requests.
     */
    @Test
    public void testRequestsLogStartupBehaviorWhenNoLoggerAtDebug() {
        Requests r = new Requests();
        r.init(null);
        MessageContext context = mockContext.mock(MessageContext.class);
        r.logStartRequest(context);
        mockContext.assertIsSatisfied();
    }

    /**
     * The Requests object should not log anything with debug not enabled.
     * 
     * @throws Throwable
     */
    @Test
    public void testRequestsHandleRequestWithNoLoggerAtDebug() throws Throwable {
        Requests r = new Requests();
        r.init(null);
        final MessageContext context = mockContext.mock(MessageContext.class);
        final HandlersChain chain = mockContext.mock(HandlersChain.class);

        mockContext.checking(new Expectations() {
            {
                oneOf(chain).doChain(context);
            }
        });
        r.handleRequest(context, chain);
        mockContext.assertIsSatisfied();
    }

    /**
     * The Requests object should register its wrapper inside of the Requests.
     */
    @Test
    public void testRequestsLogStartupBehaviorWhenLoggerAtDebugWithNullWrapper() {
        Logger.getLogger("org.apache.wink").setLevel(Level.FINE);
        Requests r = new Requests();
        r.init(null);
        final MessageContext context = mockContext.mock(MessageContext.class);
        mockContext.checking(new Expectations() {
            {
                oneOf(context).getAttribute(HttpServletRequestWrapper.class);
                will(returnValue(null));
            }
        });
        r.logStartRequest(context);
        mockContext.assertIsSatisfied();
    }

    /**
     * The Requests object should register its wrapper inside of the Requests.
     */
    @Test
    public void testRequestsLogStartupBehaviorWhenLoggerAtDebugWithWrapper() throws Exception {
        Logger.getLogger("org.apache.wink").setLevel(Level.FINE);
        Requests r = new Requests();
        r.init(null);
        final MessageContext context = mockContext.mock(MessageContext.class);
        final HttpServletRequestWrapper requestWrapper =
            mockContext.mock(HttpServletRequestWrapper.class);
        final UriInfo uriInfo = mockContext.mock(UriInfo.class);
        final HttpHeaders headers = mockContext.mock(HttpHeaders.class);
        mockContext.checking(new Expectations() {
            {
                oneOf(context).getAttribute(HttpServletRequestWrapper.class);
                will(returnValue(requestWrapper));
                oneOf(context).getUriInfo();
                will(returnValue(uriInfo));
                oneOf(uriInfo).getRequestUri();
                will(returnValue(new URI("")));
                oneOf(context).getHttpHeaders();
                will(returnValue(headers));
                oneOf(headers).getRequestHeaders();
                will(returnValue(new MultivaluedMapImpl<String, String>()));

                /*
                 * these should actually be all the same objects but no easy way
                 * to capture it
                 */
                oneOf(context).setAttribute(with(RequestWrapper.class),
                                            with(any(RequestWrapper.class)));
                oneOf(context).setAttribute(with(HttpServletRequest.class),
                                            with(any(HttpServletRequest.class)));
                oneOf(context).setAttribute(with(HttpServletRequestWrapper.class),
                                            with(any(HttpServletRequestWrapper.class)));
            }
        });
        r.logStartRequest(context);
        mockContext.assertIsSatisfied();
    }

    /**
     * This is a normal flow.
     * 
     * @throws Throwable
     */
    @Test
    public void testRequestsHandleRequestWithLoggerAtDebug() throws Throwable {
        Logger.getLogger("org.apache.wink").setLevel(Level.FINE);
        Requests r = new Requests();
        r.init(null);
        final MessageContext context = mockContext.mock(MessageContext.class);
        final HandlersChain chain = mockContext.mock(HandlersChain.class);

        final HttpServletRequestWrapper requestWrapper =
            mockContext.mock(HttpServletRequestWrapper.class);
        final Sequence normalSequence = mockContext.sequence("normalSequence");
        final RequestWrapper wrapper = mockContext.mock(RequestWrapper.class);
        final LoggedServletInputStream inputStream =
            mockContext.mock(LoggedServletInputStream.class);
        final UriInfo uriInfo = mockContext.mock(UriInfo.class);
        final HttpHeaders headers = mockContext.mock(HttpHeaders.class);

        mockContext.checking(new Expectations() {
            {
                oneOf(context).getAttribute(HttpServletRequestWrapper.class);
                will(returnValue(requestWrapper));
                inSequence(normalSequence);

                oneOf(context).getUriInfo();
                will(returnValue(uriInfo));
                oneOf(uriInfo).getRequestUri();
                will(returnValue(new URI("")));
                oneOf(context).getHttpHeaders();
                will(returnValue(headers));
                oneOf(headers).getRequestHeaders();
                will(returnValue(new MultivaluedMapImpl<String, String>()));

                /*
                 * these should actually be all the same objects but no easy way
                 * to capture it
                 */
                oneOf(context).setAttribute(with(RequestWrapper.class),
                                            with(any(RequestWrapper.class)));
                inSequence(normalSequence);
                oneOf(context).setAttribute(with(HttpServletRequest.class),
                                            with(any(HttpServletRequest.class)));
                inSequence(normalSequence);
                oneOf(context).setAttribute(with(HttpServletRequestWrapper.class),
                                            with(any(HttpServletRequestWrapper.class)));
                inSequence(normalSequence);

                oneOf(chain).doChain(context);
                inSequence(normalSequence);

                oneOf(context).getAttribute(RequestWrapper.class);
                will(returnValue(wrapper));
                inSequence(normalSequence);

                oneOf(wrapper).getLoggedInputStream();
                will(returnValue(inputStream));
                inSequence(normalSequence);

                oneOf(inputStream).getLoggedByteBufferLength();
                will(returnValue(10));
                inSequence(normalSequence);

                oneOf(inputStream).getLoggedByteBuffer();
                will(returnValue(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}));
                inSequence(normalSequence);

                oneOf(inputStream).getLoggedByteBufferLength();
                will(returnValue(10));
                inSequence(normalSequence);

                oneOf(context).setAttribute(RequestWrapper.class, (RequestWrapper)null);
                inSequence(normalSequence);
            }
        });

        r.handleRequest(context, chain);
        mockContext.assertIsSatisfied();
    }

    @Test
    public void testRequestWrapperRandomCall() {
        final HttpServletRequest request = mockContext.mock(HttpServletRequest.class);
        RequestWrapper wrapper = new RequestWrapper(request);

        mockContext.checking(new Expectations() {
            {
                oneOf(request).getContentType();
                will(returnValue("ABCD"));
            }
        });
        assertEquals("ABCD", wrapper.getContentType());

        mockContext.assertIsSatisfied();
    }

    @Test
    public void testRequestWrapperGetInputStream() throws Exception {
        final HttpServletRequest request = mockContext.mock(HttpServletRequest.class);
        final ServletInputStream servletInputStream = mockContext.mock(ServletInputStream.class);
        RequestWrapper wrapper = new RequestWrapper(request);

        mockContext.checking(new Expectations() {
            {
                oneOf(request).getInputStream();
                will(returnValue(servletInputStream));
            }
        });
        ServletInputStream m = wrapper.getInputStream();
        assertTrue(m instanceof LoggedServletInputStream);
        assertSame((LoggedServletInputStream)m, wrapper.getLoggedInputStream());

        mockContext.assertIsSatisfied();
    }
}
