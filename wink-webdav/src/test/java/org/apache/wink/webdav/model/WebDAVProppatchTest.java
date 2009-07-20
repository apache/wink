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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.wink.webdav.model.Prop;
import org.apache.wink.webdav.model.Propertyupdate;
import org.apache.wink.webdav.model.Remove;
import org.apache.wink.webdav.model.WebDAVModelHelper;
import org.custommonkey.xmlunit.Diff;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

public class WebDAVProppatchTest extends AbstractWebDAVModelTest {

    private static final String   NS                        =
                                                                "http://ns.example.com/standards/z39.50/";
    private static final QName    SET_PROPERTY              = new QName(NS, "Authors");
    private static final QName    SET_PROPERTY_CHILD        = new QName(NS, "Author");
    private static final String[] SET_PROPERTY_CHILD_VALUES = {"Jim Whitehead", "Roy Fielding"};
    private static final QName    REMOVE_PROPERTY           = new QName(NS, "Copyright-Owner");

    @Test
    public void test1() throws Exception {

        // test read
        String input = loadStreamToString(getClass().getResourceAsStream("proppatch1.xml"));
        Propertyupdate propupdate = read(input);
        // set properties
        List<Prop> props = propupdate.getPropsToSet();
        Assert.assertEquals(1, props.size());
        Prop prop = props.get(0);
        Set<QName> propertyNames = new HashSet<QName>();
        WebDAVModelHelper.extractPropertyNames(prop, propertyNames);
        Assert.assertArrayEquals(propertyNames.toArray(new QName[propertyNames.size()]),
                                 new QName[] {SET_PROPERTY});

        Element element = prop.getAny().get(0);
        Element child = (Element)element.getFirstChild();
        Assert.assertNotNull(child);
        Assert.assertEquals(SET_PROPERTY_CHILD.getNamespaceURI(), child.getNamespaceURI());
        Assert.assertEquals(SET_PROPERTY_CHILD.getLocalPart(), child.getLocalName());
        Assert.assertEquals(SET_PROPERTY_CHILD_VALUES[0], child.getTextContent());

        child = (Element)child.getNextSibling();
        Assert.assertNotNull(child);
        Assert.assertEquals(SET_PROPERTY_CHILD.getNamespaceURI(), child.getNamespaceURI());
        Assert.assertEquals(SET_PROPERTY_CHILD.getLocalPart(), child.getLocalName());
        Assert.assertEquals(SET_PROPERTY_CHILD_VALUES[1], child.getTextContent());

        // remove properties
        props = propupdate.getPropsToRemove();
        Assert.assertEquals(1, props.size());
        prop = props.get(0);
        propertyNames = new HashSet<QName>();
        WebDAVModelHelper.extractPropertyNames(prop, propertyNames);
        Assert.assertArrayEquals(propertyNames.toArray(new QName[propertyNames.size()]),
                                 new QName[] {REMOVE_PROPERTY});

        // test write
        Diff diff = new Diff(input, write(propupdate));
        Assert.assertTrue(diff.toString(), diff.similar());

        // test runtime creation
        propupdate = new Propertyupdate();
        // set properties
        org.apache.wink.webdav.model.Set set = new org.apache.wink.webdav.model.Set();
        prop = new Prop();
        set.setProp(prop);
        propupdate.getRemoveOrSet().add(set);
        element = prop.setProperty(SET_PROPERTY);
        Element child1 =
            WebDAVModelHelper.createElement(SET_PROPERTY_CHILD, SET_PROPERTY_CHILD_VALUES[0]);
        Element child2 =
            WebDAVModelHelper.createElement(SET_PROPERTY_CHILD, SET_PROPERTY_CHILD_VALUES[1]);
        element.appendChild(child1);
        element.appendChild(child2);
        // remove properties
        Remove remove = new Remove();
        prop = new Prop();
        prop.setProperty(REMOVE_PROPERTY);
        remove.setProp(prop);
        propupdate.getRemoveOrSet().add(remove);
        diff = new Diff(input, write(propupdate));
        Assert.assertTrue(diff.toString(), diff.similar());
    }

    private Propertyupdate read(String input) throws Exception {
        Propertyupdate proppatch = Propertyupdate.unmarshal(new StringReader(input));
        return proppatch;
    }

    private String write(Propertyupdate proppatch) throws Exception {
        StringWriter writer = new StringWriter();
        Propertyupdate.marshal(proppatch, writer);
        return writer.toString();
    }
}
