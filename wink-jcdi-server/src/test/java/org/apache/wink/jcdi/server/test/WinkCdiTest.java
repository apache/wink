/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.wink.jcdi.server.test;


import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.common.model.synd.SyndFeed;
import org.apache.wink.jcdi.server.internal.servlet.CdiRestServlet;
import org.apache.wink.jcdi.server.test.controller.TestController;
import org.apache.wink.jcdi.server.test.service.SimpleTestService;
import org.apache.wink.jcdi.server.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@RunWith(Arquillian.class)
public class WinkCdiTest {
    private final static String TEST_NAME = "restTests";
    private final static String TEST_SERVLET_PATH = "wink-test";
    private final static String TEST_APPLICATION_FILE_NAME = "test-application";

    //creating a WebArchive is only a workaround because JavaArchive cannot contain other archives.
    @Deployment
    public static WebArchive deploy() {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, TEST_NAME + ".jar")
                .addPackage(WinkCdiTest.class.getPackage())
                .addPackage(TestController.class.getPackage())
                .addPackage(SimpleTestService.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        Asset webXmlAsset = new StringAsset("<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\"\n" +
                "         version=\"3.0\">\n" +
                "    <servlet>\n" +
                "        <servlet-name>wink-test</servlet-name>\n" +
                "        <servlet-class>" + CdiRestServlet.class.getName() + "</servlet-class>\n" +
                "        <init-param>\n" +
                "            <param-name>applicationConfigLocation</param-name>\n" +
                "            <param-value>/WEB-INF/" + TEST_APPLICATION_FILE_NAME + "</param-value>\n" +
                "        </init-param>\n" +
                "    </servlet>\n" +
                "    <servlet-mapping>\n" +
                "        <servlet-name>wink-test</servlet-name>\n" +
                "        <url-pattern>/" + TEST_SERVLET_PATH + "/*</url-pattern>\n" +
                "    </servlet-mapping>\n" +
                "</web-app>");

        Asset applicationAsset = new StringAsset(TestController.class.getName());

        return ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addAsLibraries(ArchiveUtils.getWinkCoreServerArchive())
                .addAsLibraries(ArchiveUtils.getWinkCdiServerArchive())
                .addAsLibraries(ArchiveUtils.getWinkCommonArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(webXmlAsset, "web.xml")
                .addAsWebInfResource(applicationAsset, TEST_APPLICATION_FILE_NAME);
    }

    @ArquillianResource
    private URL url;

    @Test
    @RunAsClient
    public void simpleClientToServerCall() throws URISyntaxException, MalformedURLException {
        Assert.assertNotNull(url);
        String resourcePath = TEST_SERVLET_PATH + TestController.PATH;

        RestClient restClient = new RestClient(new ClientConfig());

        URL resourceURL = new URL(url.toExternalForm() + resourcePath);
        Resource resource = restClient.resource(resourceURL.toURI());

        // invoke GET on the resource and check the result
        Assert.assertEquals("Hello CDI", resource.get(SyndFeed.class).getTitle().getValue());
    }
}
