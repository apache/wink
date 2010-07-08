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

package org.apache.wink.itest.contextresolver;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.wink.itest.contextresolver.jaxb.ObjectFactory;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class ContextTest extends TestCase {

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI() + "/user";
        }
        return ServerEnvironmentInfo.getBaseURI() + "/simplecontextresolver/user";
    }

    public void testUserContextProvider() throws Exception {
        HttpClient httpClient = new HttpClient();

        User user = new User();
        user.setUserName("joedoe@example.com");
        JAXBElement<User> element =
            new JAXBElement<User>(new QName("http://jaxb.context.tests", "user"), User.class, user);
        JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
        StringWriter sw = new StringWriter();
        Marshaller m = context.createMarshaller();
        m.marshal(element, sw);
        PostMethod postMethod = new PostMethod(getBaseURI());
        try {
            postMethod.setRequestEntity(new ByteArrayRequestEntity(sw.toString().getBytes(),
                                                                   "text/xml"));
            httpClient.executeMethod(postMethod);
            assertEquals(204, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }

        GetMethod getMethod = new GetMethod(getBaseURI() + "/joedoe@example.com");
        try {
            httpClient.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            Unmarshaller u = context.createUnmarshaller();
            element =
                u.unmarshal(new StreamSource(getMethod.getResponseBodyAsStream()), User.class);
            assertNotNull(element);
            user = element.getValue();
            assertNotNull(user);
            assertEquals("joedoe@example.com", user.getUserName());
        } finally {
            getMethod.releaseConnection();
        }
    }

}
