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

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.webdav.model.Allprop;
import org.apache.wink.webdav.model.Multistatus;
import org.apache.wink.webdav.model.Prop;
import org.apache.wink.webdav.model.Propfind;
import org.apache.wink.webdav.model.Propname;
import org.apache.wink.webdav.model.Propstat;
import org.apache.wink.webdav.model.Response;
import org.junit.Assert;

public class WebDAVCollectionResourceTest extends AbstractWebDAVResourcesTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {WebDAVTestCollectionResource.class};
    }

    public void testAllPropertyNames() throws Exception {

        // request
        Propfind propfind = new Propfind();
        propfind.setPropname(new Propname());
        Multistatus multistatus = propfind(propfind, WebDAVCollectionResource.PATH, -1);

        // responses - depth is 1 so we have 1 + number of entries
        Map<String, Response> responses = multistatus.getResponsesAsMapByHref();
        Assert.assertEquals(1 + WebDAVDocumentResource.ENTRY_NUMBER, responses.size());

        // collection
        Response response = responses.get(WebDAVCollectionResource.PATH);
        Assert.assertNotNull(response);
        checkCollectionPropertyNames(response);

        // entries
        for (int i = 1; i <= WebDAVDocumentResource.ENTRY_NUMBER; i++) {
            String path = WebDAVCollectionResource.PATH + "/" + i;
            response = multistatus.getResponseByHref(path);
            Assert.assertNotNull(response);
            checkDocumentPropertyNames(response);
        }
    }

    public void testAllProperties() throws Exception {

        // request
        Propfind propfind = new Propfind();
        propfind.setAllprop(new Allprop());
        Multistatus multistatus = propfind(propfind, WebDAVCollectionResource.PATH, -1);

        // responses - depth is 1 so we have 1 + number of entries
        Map<String, Response> responses = multistatus.getResponsesAsMapByHref();
        Assert.assertEquals(1 + WebDAVDocumentResource.ENTRY_NUMBER, responses.size());

        // collection
        Response response = responses.get(WebDAVCollectionResource.PATH);
        Assert.assertNotNull(response);
        checkCollectionProperties(response, WebDAVCollectionResource.TITLE);

        // entries
        for (int i = 1; i <= WebDAVDocumentResource.ENTRY_NUMBER; i++) {

            String path = WebDAVCollectionResource.PATH + "/" + i;
            String name = "title" + i;
            response = responses.get(path);
            Assert.assertNotNull(response);
            checkDocumentProperties(response, name);
        }
    }

    public void testNonExistingProperty() throws Exception {

        // request with depth 0
        Propfind propfind = new Propfind();
        QName propertyName = new QName("blah");
        Prop prop = new Prop();
        prop.setProperty(propertyName);
        propfind.setProp(prop);
        Multistatus multistatus = propfind(propfind, WebDAVCollectionResource.PATH, 0);

        // 1 response
        Map<String, Response> responses = multistatus.getResponsesAsMapByHref();
        Assert.assertEquals(1, responses.size());
        Response response = responses.get(WebDAVCollectionResource.PATH);
        Assert.assertNotNull(response);
        List<Propstat> propstats = response.getPropstat();
        Assert.assertEquals(1, propstats.size());
        Propstat propstat = propstats.get(0);
        prop = propstat.getProp();
        Assert.assertNotNull(prop.getAnyByName(propertyName));
        Assert.assertEquals(HttpStatus.NOT_FOUND.getCode(), propstat.getStatusCode());
    }

    public void testOptions() throws Exception {
        checkOptions(WebDAVCollectionResource.PATH, false);
    }
}
