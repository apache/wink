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

package org.apache.wink.webdav.server;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;

import org.apache.wink.common.http.HttpHeadersEx;
import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockHttpServletRequestWrapper;
import org.apache.wink.webdav.WebDAVConstants;
import org.apache.wink.webdav.WebDAVHeaders;
import org.apache.wink.webdav.WebDAVMethod;
import org.apache.wink.webdav.model.Activelock;
import org.apache.wink.webdav.model.Exclusive;
import org.apache.wink.webdav.model.Lockinfo;
import org.apache.wink.webdav.model.Lockscope;
import org.apache.wink.webdav.model.Locktype;
import org.apache.wink.webdav.model.Multistatus;
import org.apache.wink.webdav.model.Prop;
import org.apache.wink.webdav.model.Propfind;
import org.apache.wink.webdav.model.Propstat;
import org.apache.wink.webdav.model.Response;
import org.apache.wink.webdav.model.WebDAVModelHelper;
import org.apache.wink.webdav.model.Write;
import org.junit.Assert;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public abstract class AbstractWebDAVResourcesTest extends MockServletInvocationTest {

    public static final HttpStatus[] COLLECTION_OKS        =
                                                               {HttpStatus.OK, HttpStatus.OK,
        HttpStatus.OK                                          };
    public static final HttpStatus[] DOCUMENT_OKS          =
                                                               {HttpStatus.OK, HttpStatus.OK,
        HttpStatus.OK, HttpStatus.OK                           };
    public static QName[]            PROPERTIES_DOCUMENT   =
                                                               new QName[] {
        WebDAVConstants.PROPERTY_CREATIONDATE, WebDAVConstants.PROPERTY_DISPLAYNAME,
        WebDAVConstants.PROPERTY_GETLASTMODIFIED, WebDAVConstants.PROPERTY_RESOURCETYPE};

    public static QName[]            PROPERTIES_COLLECTION =
                                                               new QName[] {
        WebDAVConstants.PROPERTY_RESOURCETYPE, WebDAVConstants.PROPERTY_DISPLAYNAME,
        WebDAVConstants.PROPERTY_GETLASTMODIFIED               };

    protected static MockHttpServletRequest constructPropfindRequest(Propfind propfind,
                                                                     String requestURI,
                                                                     int depth) throws IOException {

        MockHttpServletRequest mockRequest = new MockHttpServletRequestWrapper() {

            public String getPathTranslated() {
                return null; // prevent Spring to resolve the file on the file
                             // system which fails
            }
        };
        // headers
        mockRequest.setMethod(WebDAVMethod.PROPFIND.name());
        mockRequest.setRequestURI(requestURI);
        mockRequest.setContentType(MediaType.APPLICATION_XML);
        if (depth >= 0) {
            mockRequest.addHeader(WebDAVHeaders.DEPTH, String.valueOf(depth));
        }
        // body
        StringWriter writer = new StringWriter();
        Propfind.marshal(propfind, writer);
        mockRequest.setContent(writer.toString().getBytes());

        return mockRequest;
    }

    protected static Multistatus getMultistatus(MockHttpServletResponse response)
        throws IOException {

        // headers
        Assert.assertEquals(HttpStatus.MULTI_STATUS.getCode(), response.getStatus());
        MediaType mediaType = MediaType.valueOf(response.getContentType());
        // we can't clear MediaType params due to UnsupportedOperationException on the collection, so...
        Assert.assertEquals(MediaType.valueOf(MediaType.APPLICATION_XML_TYPE.toString()), mediaType);
        // body
        String responseContent = response.getContentAsString();
        StringReader reader = new StringReader(responseContent);
        Multistatus multistatus = Multistatus.unmarshal(reader);

        return multistatus;
    }

    protected Multistatus propfind(Propfind propfind, String requestURI, int depth)
        throws Exception {

        MockHttpServletRequest request = constructPropfindRequest(propfind, requestURI, depth);
        MockHttpServletResponse response = invoke(request);
        return getMultistatus(response);
    }

    private static void checkProperties(Response response,
                                        QName[] propertyNames,
                                        HttpStatus[] statuses) {
        int totalCovered = 0;
        for (Propstat propstat : response.getPropstat()) {
            int statusCode = propstat.getStatusCode();

            Set<QName> expectedPropnames = new HashSet<QName>();
            for (int i = 0; i < statuses.length; ++i) {
                if (statuses[i].getCode() == statusCode) {
                    expectedPropnames.add(propertyNames[i]);
                }
            }
            totalCovered += expectedPropnames.size();

            Prop prop = propstat.getProp();
            Set<QName> propnames =
                WebDAVModelHelper.extractPropertyNames(prop, new HashSet<QName>());
            Assert.assertEquals(expectedPropnames.size(), propnames.size());

            for (QName name : expectedPropnames) {
                Assert.assertTrue(propnames.contains(name));
            }
        }
        Assert.assertEquals(propertyNames.length, totalCovered);
    }

    protected static void checkCollectionPropertyNames(Response response) {
        checkProperties(response, PROPERTIES_COLLECTION, COLLECTION_OKS);
    }

    protected static void checkCollectionProperties(Response response, String name) {

        // check it contains all collection properties with OK status
        checkCollectionPropertyNames(response);

        Prop prop = response.getPropstat().get(0).getProp();

        // check name property
        Assert.assertEquals(name, prop.getDisplayname().getValue());

        // check the resource type property
        Assert.assertTrue(prop.getResourcetype().getCollection() != null);
    }

    protected static void checkRootCollectionProperties(Response response, String name) {

        // check it contains the root collection properties with OK status
        HttpStatus[] rootStatuses = {HttpStatus.OK, HttpStatus.OK, HttpStatus.NOT_FOUND};
        checkProperties(response, PROPERTIES_COLLECTION, rootStatuses);

        Prop prop = response.getPropstat().get(0).getProp();
        // check name property
        String displayName = "";
        if (prop.getDisplayname().getValue() != null) {
            displayName = prop.getDisplayname().getValue();
        }
        Assert.assertEquals(name, displayName);

        // check the resource type property
        Assert.assertTrue(prop.getResourcetype().getCollection() != null);
    }

    protected static void checkDocumentPropertyNames(Response response) {
        checkProperties(response, PROPERTIES_DOCUMENT, DOCUMENT_OKS);
    }

    protected static void checkDocumentProperties(Response response, String name) {

        // check it contains all document properties with OK status
        checkDocumentPropertyNames(response);

        Prop prop = response.getPropstat().get(0).getProp();
        // check name property
        Assert.assertEquals(name, prop.getDisplayname().getValue());

        // check the resource type property
        Assert.assertFalse(prop.getResourcetype().getCollection() != null);
    }

    protected void checkOptions(String path, boolean alsoLock) throws Exception {

        // request
        MockHttpServletRequest request = new MockHttpServletRequest() {

            public String getPathTranslated() {
                return null; // prevent Spring to resolve the file on the file
                             // system which fails
            }
        };
        request.setMethod("OPTIONS");
        request.setRequestURI(path);
        MockHttpServletResponse response = invoke(request);

        // response
        Assert.assertEquals(HttpStatus.OK.getCode(), response.getStatus());
        Assert.assertEquals("1", response.getHeader(WebDAVHeaders.DAV));
        Assert.assertEquals("DAV", response.getHeader(WebDAVHeaders.MS_AUTHOR_VIA));
        // check allow - must contain OPTIONS, PROPFIND, LOCK
        String allowStr = (String)response.getHeader(HttpHeadersEx.ALLOW);
        List<?> allows = Arrays.asList(allowStr.split("\\s*,\\s*"));
        Assert.assertTrue(allows.contains("OPTIONS"));
        Assert.assertTrue(allows.contains(WebDAVMethod.PROPFIND.name()));
        if (alsoLock) {
            Assert.assertTrue(allows.contains(WebDAVMethod.LOCK.name()));
        } else {
            Assert.assertFalse(allows.contains(WebDAVMethod.LOCK.name()));
        }
    }

    protected void checkLockAndUnlock(String path) throws Exception {

        // lock request
        MockHttpServletRequest request = new MockHttpServletRequest() {

            public String getPathTranslated() {
                return null; // prevent Spring to resolve the file on the file
                             // system which fails
            }
        };
        request.setMethod(WebDAVMethod.LOCK.name());
        request.setRequestURI(path);
        request.setContentType(MediaType.APPLICATION_XML);
        Lockinfo lockinfo = new Lockinfo();
        Lockscope lockscope = new Lockscope();
        lockscope.setExclusive(new Exclusive());
        lockinfo.setLockscope(lockscope);
        Locktype locktype = new Locktype();
        locktype.setWrite(new Write());
        lockinfo.setLocktype(locktype);
        StringWriter writer = new StringWriter();
        WebDAVModelHelper.marshal(WebDAVModelHelper.createMarshaller(),
                                  lockinfo,
                                  writer,
                                  "lockinfo");
        request.setContent(writer.toString().getBytes());
        MockHttpServletResponse response = invoke(request);

        // lock response
        Assert.assertEquals(HttpStatus.OK.getCode(), response.getStatus());
        MediaType mediaType = MediaType.valueOf(response.getContentType());
        // we can't clear MediaType params due to UnsupportedOperationException on the collection, so...
        Assert.assertEquals(MediaType.valueOf(MediaType.APPLICATION_XML_TYPE.toString()), mediaType);
        StringReader reader = new StringReader(response.getContentAsString());
        Prop prop =
            WebDAVModelHelper.unmarshal(WebDAVModelHelper.createUnmarshaller(),
                                        reader,
                                        Prop.class,
                                        "prop");
        List<Activelock> activelocks = prop.getLockdiscovery().getActivelock();
        Assert.assertNotNull(activelocks);
        Assert.assertEquals(1, activelocks.size());
        Assert.assertNotNull(activelocks.get(0).getLocktype().getWrite());
        Assert.assertNotNull(activelocks.get(0).getLockscope().getExclusive());
        Assert.assertEquals("0", activelocks.get(0).getDepth());

        // unlock request
        request = new MockHttpServletRequest() {

            public String getPathTranslated() {
                return null; // prevent Spring to resolve the file on the file
                             // system which fails
            }
        };
        request.setMethod(WebDAVMethod.UNLOCK.name());
        request.setRequestURI(path);
        response = invoke(request);

        // lock response
        Assert.assertEquals(HttpStatus.NO_CONTENT.getCode(), response.getStatus());
    }
}
