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

package org.apache.wink.webdav.model;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.namespace.QName;

import org.custommonkey.xmlunit.Diff;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

public class WebDAVMultistatusTest extends AbstractWebDAVModelTest {

    private static final String  NS1              = "http://ns.example.com/boxschema/";
    private static final String  RESP1_HREF       = "http://www.example.com/file";
    private static final String  RESP1_DESC       = "There has been an access violation error.";
    private static final QName[] RESP1_PROPS      =
                                                      new QName[] {new QName(NS1, "bigbox", "R"),
        new QName(NS1, "author", "R"), new QName(NS1, "DingALing", "R"),
        new QName(NS1, "Random", "R")                 };
    private static final String  RESP1_PROPS_DESC =
                                                      "The user does not have access to the DingALing property.";

    private static final String  NS2              = "http://ns.example.com/standards/z39.50/";
    private static final String  RESP2_HREF       = "http://www.example.com/bar.html";
    private static final QName[] RESP2_PROPS      =
                                                      new QName[] {
        new QName(NS2, "Copyright-Owner"), new QName(NS2, "Authors")};
    private static final String  RESP2_DESC       = "Copyright Owner cannot be deleted or altered.";

    @Test
    public void test1() throws Exception {

        // test read
        String input = loadStreamToString(getClass().getResourceAsStream("status1.xml"));
        Multistatus multistatus = read(input);
        List<Response> responses = multistatus.getResponse();
        Assert.assertEquals(multistatus.getResponsedescription(), RESP1_DESC);
        Assert.assertEquals(responses.size(), 1); // one response
        Response response = responses.get(0);
        Assert.assertNotNull(response);
        List<Propstat> propstats = response.getPropstat();
        Assert.assertEquals(propstats.size(), 2);

        Propstat propstat = propstats.get(0);
        Assert.assertEquals(propstat.getStatusCode(), 200);
        Prop prop = propstat.getProp();
        Element element = prop.getAnyByName(RESP1_PROPS[0]);
        Assert.assertNotNull(element);
        Assert.assertEquals(element.getLocalName(), RESP1_PROPS[0].getLocalPart());
        Assert.assertEquals(element.getNamespaceURI(), RESP1_PROPS[0].getNamespaceURI());
        element = prop.getAnyByName(RESP1_PROPS[1]);
        Assert.assertNotNull(element);
        Assert.assertEquals(element.getLocalName(), RESP1_PROPS[1].getLocalPart());
        Assert.assertEquals(element.getNamespaceURI(), RESP1_PROPS[1].getNamespaceURI());

        propstat = propstats.get(1);
        Assert.assertEquals(propstat.getStatusCode(), 403);
        Assert.assertEquals(propstat.getResponsedescription(), RESP1_PROPS_DESC);
        prop = propstat.getProp();
        element = prop.getAnyByName(RESP1_PROPS[2]);
        Assert.assertNotNull(element);
        Assert.assertEquals(element.getLocalName(), RESP1_PROPS[2].getLocalPart());
        Assert.assertEquals(element.getNamespaceURI(), RESP1_PROPS[2].getNamespaceURI());
        element = prop.getAnyByName(RESP1_PROPS[3]);
        Assert.assertNotNull(element);
        Assert.assertEquals(element.getLocalName(), RESP1_PROPS[3].getLocalPart());
        Assert.assertEquals(element.getNamespaceURI(), RESP1_PROPS[3].getNamespaceURI());

        // test write
        String write = write(multistatus);
        Diff diff = new Diff(input, write);
        Assert.assertTrue(diff.toString(), diff.similar());

        // test runtime creation
        multistatus = new Multistatus();
        multistatus.setResponsedescription(RESP1_DESC);
        response = new Response(RESP1_HREF);
        multistatus.getResponse().add(response);
        propstat = response.getOrCreatePropstat(200, null, null);
        prop = propstat.getProp();
        prop.setProperty(RESP1_PROPS[0], WebDAVModelHelper.createElement(NS1,
                                                                         "R:BoxType",
                                                                         "Box type A"));
        prop.setProperty(RESP1_PROPS[1], WebDAVModelHelper.createElement(NS1,
                                                                         "R:Name",
                                                                         "J.J. Johnson"));
        propstat = response.getOrCreatePropstat(403, RESP1_PROPS_DESC, null);
        prop = propstat.getProp();
        prop.setProperty(RESP1_PROPS[2]);
        prop.setProperty(RESP1_PROPS[3]);

        write = write(multistatus);
        diff = new Diff(input, write);
        Assert.assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void test2() throws Exception {

        // test read
        String input = loadStreamToString(getClass().getResourceAsStream("status2.xml"));
        Multistatus multistatus = read(input);
        List<Response> responses = multistatus.getResponse();
        Assert.assertEquals(responses.size(), 1); // one response
        Response response = multistatus.getResponseByHref(RESP2_HREF);
        Assert.assertNotNull(response);
        List<Propstat> propstats = response.getPropstat();
        Assert.assertEquals(propstats.size(), 2);

        Propstat propstat = propstats.get(0);
        Assert.assertEquals(propstat.getStatusCode(), 409);
        Assert.assertNull(propstat.getError());
        Assert.assertNull(propstat.getResponsedescription());
        // 1. property
        Prop prop = propstat.getProp();
        Assert.assertEquals(prop.getAny().size(), 1);
        Element element = prop.getAnyByName(RESP2_PROPS[0]);
        Assert.assertNotNull(element);
        Assert.assertEquals(element.getLocalName(), RESP2_PROPS[0].getLocalPart());
        Assert.assertEquals(element.getNamespaceURI(), RESP2_PROPS[0].getNamespaceURI());
        // 2. property
        propstat = propstats.get(1);
        Assert.assertEquals(propstat.getStatusCode(), 424);
        Assert.assertNull(propstat.getError());
        Assert.assertNull(propstat.getResponsedescription());
        prop = propstat.getProp();
        Assert.assertEquals(prop.getAny().size(), 1);
        element = prop.getAnyByName(RESP2_PROPS[1]);
        Assert.assertNotNull(element);
        Assert.assertEquals(element.getLocalName(), RESP2_PROPS[1].getLocalPart());
        Assert.assertEquals(element.getNamespaceURI(), RESP2_PROPS[1].getNamespaceURI());

        // test write
        String write = write(multistatus);
        Diff diff = new Diff(input, write);
        Assert.assertTrue(diff.toString(), diff.similar());

        // test runtime creation
        multistatus = new Multistatus();
        response = new Response(RESP2_HREF);
        multistatus.getResponse().add(response);
        response.setResponsedescription(RESP2_DESC);
        // 1. property
        response.setProperty(WebDAVModelHelper.createElement(RESP2_PROPS[0]), 409, null, null);
        // 2. property
        response.setProperty(WebDAVModelHelper.createElement(RESP2_PROPS[1]), 424, null, null);
        write = write(multistatus);
        diff = new Diff(input, write);
        Assert.assertTrue(diff.toString(), diff.similar());
    }

    private Multistatus read(String input) throws Exception {
        Multistatus multistatus = Multistatus.unmarshal(new StringReader(input));
        return multistatus;
    }

    private String write(Multistatus multistatus) throws Exception {
        StringWriter writer = new StringWriter();
        Multistatus.marshal(multistatus, writer);
        return writer.toString();
    }
}
