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

public class WebDAVRootResourceTest extends AbstractWebDAVResourcesTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {WebDAVTestCollectionResource.class,
            WebDAVTestDocumentResource.class, WebDAVRootResource.class};
    }

    public void testAllPropertyNames() throws Exception {

        // request
        Propfind propfind = new Propfind();
        propfind.setPropname(new Propname());
        Multistatus multistatus = propfind(propfind, "/", -1);

        // responses - depth is 1 so we have 2 ('/' and '/feed')
        Map<String, Response> responses = multistatus.getResponsesAsMapByHref();
        Assert.assertEquals(2, responses.size());

        // '/'
        Response response = responses.get("/");
        Assert.assertNotNull(response);
        checkCollectionPropertyNames(response);

        // '/feed'
        response = responses.get(WebDAVCollectionResource.PATH);
        Assert.assertNotNull(response);
        checkCollectionPropertyNames(response);
    }

    public void testAllProperties() throws Exception {

        // request
        Propfind propfind = new Propfind();
        propfind.setAllprop(new Allprop());
        Multistatus multistatus = propfind(propfind, "/", -1);

        // responses - depth is 1 so we have 2 ('/' and '/feed')
        Map<String, Response> responses = multistatus.getResponsesAsMapByHref();
        Assert.assertEquals(2, responses.size());

        // '/'
        Response response = responses.get("/");
        Assert.assertNotNull(response);
        checkRootCollectionProperties(response, "");

        // '/feed'
        response = responses.get(WebDAVCollectionResource.PATH);
        Assert.assertNotNull(response);
        checkRootCollectionProperties(response, WebDAVCollectionResource.TITLE);
    }

    public void testNonExistingProperty() throws Exception {

        // request with depth 0
        Propfind propfind = new Propfind();
        QName propertyName = new QName("blah");
        Prop prop = new Prop();
        prop.setProperty(propertyName);
        propfind.setProp(prop);
        Multistatus multistatus = propfind(propfind, "/", 0);

        // 1 response - '/'
        Map<String, Response> responses = multistatus.getResponsesAsMapByHref();
        Assert.assertEquals(1, responses.size());
        Response response = responses.get("/");
        Assert.assertNotNull(response);
        Assert.assertEquals(1, response.getPropstat().size());
        Propstat propstat = response.getPropstat().get(0);
        Assert.assertEquals(HttpStatus.NOT_FOUND.getCode(), propstat.getStatusCode());
        prop = propstat.getProp();
        Assert.assertEquals(1, prop.getAny().size());
        Assert.assertNotNull(prop.getAnyByName(propertyName));
    }

    public void testOptions() throws Exception {
        checkOptions("/", false);
    }
}
