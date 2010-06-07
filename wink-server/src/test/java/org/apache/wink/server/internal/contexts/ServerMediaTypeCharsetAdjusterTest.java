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
package org.apache.wink.server.internal.contexts;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.WinkConfiguration;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.junit.Test;

public class ServerMediaTypeCharsetAdjusterTest extends MockObjectTestCase {

    RuntimeContext context = null;
    DeploymentConfiguration myConfig = new DeploymentConfiguration();
    MultivaluedMap<String, Object> responseHttpHeaders = null;

    
    @SuppressWarnings("unchecked")
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        myConfig.init();
        // default is false in the real config  
        myConfig.setDefaultResponseCharset(true);
        // default is false in the real config  
        myConfig.setUseAcceptCharset(false);
        
        // common expectations
        context = mock(RuntimeContext.class);
        responseHttpHeaders = mock(MultivaluedMap.class);
        checking(new Expectations() {{
            allowing(context).getAttribute(WinkConfiguration.class); will(returnValue(myConfig));
        }});
        
        RuntimeContextTLS.setRuntimeContext(context);
    }
    
    @Override
    public void tearDown() {
        RuntimeContextTLS.setRuntimeContext(null);
    }

    @Test
    public void testSetDefaultCharsetOnMediaTypeHeaderWithDefaultConfig() {
        
        final String expected = "application/xml";
        
        ServerMediaTypeCharsetAdjuster serverMediaTypeCharsetAdjuster = ServerMediaTypeCharsetAdjuster.getInstance();
        
        // null httpHeaders param
        MediaType mediaType = serverMediaTypeCharsetAdjuster.setDefaultCharsetOnMediaTypeHeader(null, MediaType.APPLICATION_XML_TYPE);
        assertEquals(expected, mediaType.toString());
    }
    
    @Test
    public void testSetDefaultCharsetOnMediaTypeHeaderWithConfigTrueNullParam() {
        
        final String expected = "application/xml";
        
        ServerMediaTypeCharsetAdjuster serverMediaTypeCharsetAdjuster = ServerMediaTypeCharsetAdjuster.getInstance();
        
        myConfig.setDefaultResponseCharset(true);
        
        // null httpHeaders param
        MediaType mediaType = serverMediaTypeCharsetAdjuster.setDefaultCharsetOnMediaTypeHeader(null, MediaType.APPLICATION_XML_TYPE);
        assertEquals(expected, mediaType.toString());
        
    }
    
    @Test
    public void testSetDefaultCharsetOnMediaTypeHeaderWithConfigTrueAndNullHeadersAndFalseConfig() {

        final String expected = "application/xml";
        
        ServerMediaTypeCharsetAdjuster serverMediaTypeCharsetAdjuster = ServerMediaTypeCharsetAdjuster.getInstance();
        
        // switch off the default
        myConfig.setDefaultResponseCharset(false);
        
        // empty map
        MediaType mediaType = serverMediaTypeCharsetAdjuster.setDefaultCharsetOnMediaTypeHeader(new MultivaluedMapImpl<String, Object>(), MediaType.APPLICATION_XML_TYPE);
        // no charset attribute due to setDefaultResponseCharset(false)
        assertEquals(expected, mediaType.toString());
        
    }
    
    @Test
    public void testSetDefaultCharsetOnMediaTypeHeaderWithConfigTrueAndNullHeaders() {

        final String expected = "application/xml;charset=UTF-8";
        
        checking(new Expectations() {{
            oneOf(responseHttpHeaders).isEmpty(); will(returnValue(true));
            oneOf(responseHttpHeaders).putSingle(HttpHeaders.CONTENT_TYPE, expected);
        }});
        
        ServerMediaTypeCharsetAdjuster serverMediaTypeCharsetAdjuster = ServerMediaTypeCharsetAdjuster.getInstance();
        
        // empty map
        MediaType mediaType = serverMediaTypeCharsetAdjuster.setDefaultCharsetOnMediaTypeHeader(responseHttpHeaders, MediaType.APPLICATION_XML_TYPE);
        assertEquals(expected, mediaType.toString());
        
    }
    
    @Test
    public void testSetDefaultCharsetOnMediaTypeHeaderWithConfigTrueAndNullAcceptCharsetHeader() {

        final String expected = "application/xml;charset=UTF-8";
        
        final HttpHeaders httpHeaders = mock(HttpHeaders.class);
        checking(new Expectations() {{
            oneOf(responseHttpHeaders).isEmpty(); will(returnValue(true));
            oneOf(responseHttpHeaders).putSingle(HttpHeaders.CONTENT_TYPE, expected);
            oneOf(context).getHttpHeaders(); will(returnValue(httpHeaders));
            oneOf(httpHeaders).getRequestHeader(HttpHeaders.ACCEPT_CHARSET); will(returnValue(null));
        }});
        
        ServerMediaTypeCharsetAdjuster serverMediaTypeCharsetAdjuster = ServerMediaTypeCharsetAdjuster.getInstance();
        
        // exercise code path that reads the Accept-Charset header
        myConfig.setUseAcceptCharset(true);
        
        // empty map
        MediaType mediaType = serverMediaTypeCharsetAdjuster.setDefaultCharsetOnMediaTypeHeader(responseHttpHeaders, MediaType.APPLICATION_XML_TYPE);
        assertEquals(expected, mediaType.toString());
        
    }
    
    @Test
    public void testSetDefaultCharsetOnMediaTypeHeaderWithConfigTrueAndAcceptCharsetHeader() {
        
        final String expected = "application/xml;charset=ISO-8859-1";
        
        final List<String> acceptHeaders = new ArrayList<String>();
        acceptHeaders.add("UTF-16");
        
        final HttpHeaders httpHeaders = mock(HttpHeaders.class);
        checking(new Expectations() {{
            oneOf(responseHttpHeaders).add("nonesense", null);
            oneOf(responseHttpHeaders).isEmpty(); will(returnValue(false));
            oneOf(responseHttpHeaders).get(HttpHeaders.CONTENT_TYPE); will(returnValue(null));
            oneOf(responseHttpHeaders).putSingle(HttpHeaders.CONTENT_TYPE, expected);
            oneOf(context).getHttpHeaders(); will(returnValue(httpHeaders));
            oneOf(httpHeaders).getRequestHeader(HttpHeaders.ACCEPT_CHARSET); will(returnValue(acceptHeaders));
        }});
        
        // non-empty map, just to make sure production code path is as expected
        responseHttpHeaders.add("nonesense", null);
        ServerMediaTypeCharsetAdjuster serverMediaTypeCharsetAdjuster = ServerMediaTypeCharsetAdjuster.getInstance();
        
        // exercise code path that reads the Accept-Charset header
        myConfig.setUseAcceptCharset(true);
        
        // empty map
        MediaType mediaType = serverMediaTypeCharsetAdjuster.setDefaultCharsetOnMediaTypeHeader(responseHttpHeaders, MediaType.APPLICATION_XML_TYPE);
        // still defaults back to ISO-8859-1 because it is silently added as top q-valued charset on the client-originated Accept-Header.  See HTTP spec.
        assertEquals(expected, mediaType.toString());
        
    }
    
    @Test
    public void testSetDefaultCharsetOnMediaTypeHeaderWithConfigTrueAndAcceptCharsetButFalse() {
        
        final String expected = "application/xml;charset=UTF-8";
        
        final List<String> acceptHeaders = new ArrayList<String>();
        acceptHeaders.add("UTF-16;q=1.0");
        acceptHeaders.add("ISO-8859-1;q=0.5");  // re-prioritize silently added charset to lower q-value than UTF-16
        
        checking(new Expectations() {{
            oneOf(responseHttpHeaders).isEmpty(); will(returnValue(true));
            oneOf(responseHttpHeaders).putSingle(HttpHeaders.CONTENT_TYPE, expected);
        }});
        
        ServerMediaTypeCharsetAdjuster serverMediaTypeCharsetAdjuster = ServerMediaTypeCharsetAdjuster.getInstance();
        
        // leave useAcceptHeader to the default of false
        
        MediaType mediaType = serverMediaTypeCharsetAdjuster.setDefaultCharsetOnMediaTypeHeader(responseHttpHeaders, MediaType.APPLICATION_XML_TYPE);
        // UTF-16 has highest q-value, but Accept-Charset is being ignored due to config, so...
        assertEquals(expected, mediaType.toString());
        
    }
    
    @Test
    public void testSetDefaultCharsetOnMediaTypeHeaderWithConfigTrueAndAcceptCharset() {
        
        final String expected = "application/xml;charset=UTF-16";
        
        final List<String> acceptHeaders = new ArrayList<String>();
        acceptHeaders.add("UTF-16;q=1.0");
        acceptHeaders.add("ISO-8859-1;q=0.5");  // re-prioritize silently added charset to lower q-value than UTF-16
        
        final HttpHeaders httpHeaders = mock(HttpHeaders.class);
        checking(new Expectations() {{
            oneOf(responseHttpHeaders).isEmpty(); will(returnValue(true));
            oneOf(responseHttpHeaders).putSingle(HttpHeaders.CONTENT_TYPE, expected);
            oneOf(context).getHttpHeaders(); will(returnValue(httpHeaders));
            oneOf(httpHeaders).getRequestHeader(HttpHeaders.ACCEPT_CHARSET); will(returnValue(acceptHeaders));
        }});
        
        ServerMediaTypeCharsetAdjuster serverMediaTypeCharsetAdjuster = ServerMediaTypeCharsetAdjuster.getInstance();

        // exercise code path that reads the Accept-Charset header
        myConfig.setUseAcceptCharset(true);
        
        MediaType mediaType = serverMediaTypeCharsetAdjuster.setDefaultCharsetOnMediaTypeHeader(responseHttpHeaders, MediaType.APPLICATION_XML_TYPE);
        // UTF-16 has highest q-value
        assertEquals(expected, mediaType.toString());
        
    }
    
    @Test
    public void testSetDefaultCharsetOnMediaTypeHeaderWithConfigFalseAndAcceptCharset() {
        
        final String expected = "application/xml;charset=UTF-16";
        
        final List<String> acceptHeaders = new ArrayList<String>();
        acceptHeaders.add("UTF-16;q=1.0");
        acceptHeaders.add("ISO-8859-1;q=0.5");  // re-prioritize silently added charset to lower q-value than UTF-16
        
        final HttpHeaders httpHeaders = mock(HttpHeaders.class);
        checking(new Expectations() {{
            oneOf(responseHttpHeaders).isEmpty(); will(returnValue(true));
            oneOf(responseHttpHeaders).putSingle(HttpHeaders.CONTENT_TYPE, expected);
            oneOf(context).getHttpHeaders(); will(returnValue(httpHeaders));
            oneOf(httpHeaders).getRequestHeader(HttpHeaders.ACCEPT_CHARSET); will(returnValue(acceptHeaders));
        }});
        
        ServerMediaTypeCharsetAdjuster serverMediaTypeCharsetAdjuster = ServerMediaTypeCharsetAdjuster.getInstance();

        // make sure when user expects to be using Accept-Header, they really do use it
        
        // default is true in the real config.  Resetting to false here to confirm code path is correct
        myConfig.setUseAcceptCharset(false);
        // exercise code path that reads the Accept-Charset header
        myConfig.setUseAcceptCharset(true);
        
        MediaType mediaType = serverMediaTypeCharsetAdjuster.setDefaultCharsetOnMediaTypeHeader(responseHttpHeaders, MediaType.APPLICATION_XML_TYPE);
        // UTF-16 has highest q-value
        assertEquals(expected, mediaType.toString());
        
    }
    

}
