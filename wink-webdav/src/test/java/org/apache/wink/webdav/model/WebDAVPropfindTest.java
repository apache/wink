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

import javax.xml.namespace.QName;

import org.apache.wink.webdav.model.Allprop;
import org.apache.wink.webdav.model.Prop;
import org.apache.wink.webdav.model.Propfind;
import org.apache.wink.webdav.model.Propname;
import org.custommonkey.xmlunit.Diff;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

public class WebDAVPropfindTest extends AbstractWebDAVModelTest {

    private static final String  NS         = "http://ns.example.com/boxschema/";
    private static final QName[] PROPERTIES =
                                                new QName[] {new QName(NS, "author"),
        new QName(NS, "DingALing"), new QName(NS, "Random"), new QName(NS, "bigbox")};

    @Test
    public void test1() throws Exception {

        // test read
        String input = loadStreamToString(getClass().getResourceAsStream("propfind1.xml"));
        Propfind propfind = read(input);
        Prop prop = propfind.getProp();
        Assert.assertEquals(PROPERTIES.length, prop.getAny().size());
        for (QName qname : PROPERTIES) {
            Element element = prop.getAnyByName(qname);
            Assert.assertNotNull(element);
            Assert.assertEquals(qname.getNamespaceURI(), element.getNamespaceURI());
            Assert.assertEquals(qname.getLocalPart(), element.getLocalName());
        }
        Assert.assertFalse(propfind.isAllprop());
        Assert.assertFalse(propfind.isPropname());

        // test write
        Diff diff = new Diff(input, write(propfind));
        Assert.assertTrue(diff.toString(), diff.similar());

        // test runtime creation
        propfind = new Propfind();
        prop = new Prop();
        propfind.setProp(prop);
        prop.setProperty(PROPERTIES[0]);
        prop.setProperty(PROPERTIES[1]);
        prop.setProperty(PROPERTIES[2]);
        prop.setProperty(PROPERTIES[3]);
        diff = new Diff(input, write(propfind));
        Assert.assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void test2() throws Exception {

        // test read
        String input = loadStreamToString(getClass().getResourceAsStream("propfind2.xml"));
        Propfind propfind = read(input);
        Assert.assertNull(propfind.getProp());
        Assert.assertFalse(propfind.isAllprop());
        Assert.assertTrue(propfind.isPropname());

        // test write
        Diff diff = new Diff(input, write(propfind));
        Assert.assertTrue(diff.toString(), diff.similar());

        // test runtime creation
        propfind = new Propfind();
        propfind.setPropname(new Propname());
        diff = new Diff(input, write(propfind));
        Assert.assertTrue(diff.toString(), diff.similar());
    }

    @Test
    public void test3() throws Exception {

        // test read
        String input = loadStreamToString(getClass().getResourceAsStream("propfind3.xml"));
        Propfind propfind = read(input);
        Assert.assertNull(propfind.getProp());
        Assert.assertTrue(propfind.isAllprop());
        Assert.assertFalse(propfind.isPropname());

        // test write
        Diff diff = new Diff(input, write(propfind));
        Assert.assertTrue(diff.toString(), diff.similar());

        // test runtime creation
        propfind = new Propfind();
        propfind.setAllprop(new Allprop());
        diff = new Diff(input, write(propfind));
        Assert.assertTrue(diff.toString(), diff.similar());
    }

    private Propfind read(String input) throws Exception {
        Propfind propfind = Propfind.unmarshal(new StringReader(input));
        return propfind;
    }

    private String write(Propfind propfind) throws Exception {
        StringWriter writer = new StringWriter();
        Propfind.marshal(propfind, writer);
        return writer.toString();
    }
}
