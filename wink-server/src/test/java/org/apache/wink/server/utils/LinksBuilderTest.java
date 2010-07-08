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

package org.apache.wink.server.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.synd.SyndLink;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.server.utils.SystemLinksBuilder.LinkType;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class LinksBuilderTest extends MockServletInvocationTest {

    private static List<Class<?>> resourceClasses = new LinkedList<Class<?>>();

    static {
        for (Class<?> cls : LinksBuilderTest.class.getClasses()) {
            if (cls.getSimpleName().endsWith("Resource")) {
                resourceClasses.add(cls);
            }
        }
    }

    @Override
    protected Class<?>[] getClasses() {
        return resourceClasses.toArray(new Class<?>[resourceClasses.size()]);
    }

    // -- system links resources

    @Path("systemLinks")
    public static class SystemLinksResource {

        @GET
        @Produces( {"application/xml", "application/json"})
        public void getXmlOrJson() {
        }

        @GET
        @Produces("text/html")
        public void getHtml() {
        }

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {

            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().build(links);
            assertLinks(links);
            return "";
        }

        public static void assertLinks(List<SyndLink> links) {
            assertEquals(5, links.size());
            assertLink(links, "self", null, "systemLinks");
            assertLink(links, "alternate", "application/xml", "systemLinks?alt=application%2Fxml");
            assertLink(links, "alternate", "application/json", "systemLinks?alt=application%2Fjson");
            assertLink(links, "alternate", "text/html", "systemLinks?alt=text%2Fhtml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinks?alt=application%2Fatom%2Bxml");
        }
    }

    @Path("systemLinksWithPut")
    public static class SystemLinksWithPutResource {

        @GET
        @Produces( {"application/xml", "application/json"})
        public void getXmlOrJson() {
        }

        @PUT
        @Produces("application/atom+xml")
        public void postAtom() {
        }

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {

            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().build(links);
            assertEquals(5, links.size());
            assertLink(links, "self", null, "systemLinksWithPut");
            assertLink(links, "edit", null, "systemLinksWithPut");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "systemLinksWithPut?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/json",
                       "systemLinksWithPut?alt=application%2Fjson");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinksWithPut?alt=application%2Fatom%2Bxml");
            return "";
        }
    }

    @Path("systemLinksAltParam")
    public static class SystemLinksAltParamResource {

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {
            List<SyndLink> links = new ArrayList<SyndLink>();
            SystemLinksBuilder systemLinksBuilder = builders.createSystemLinksBuilder();

            systemLinksBuilder.build(links);
            assertEquals(2, links.size());
            assertLink(links, "self", null, "systemLinksAltParam");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinksAltParam?alt=application%2Fatom%2Bxml");

            links.clear();
            systemLinksBuilder.addAltParam(false).build(links);
            assertEquals(2, links.size());
            assertLink(links, "self", null, "systemLinksAltParam");
            assertLink(links, "alternate", "application/atom+xml", "systemLinksAltParam");

            links.clear();
            systemLinksBuilder.addAltParam(true).build(links);
            assertEquals(2, links.size());
            assertLink(links, "self", null, "systemLinksAltParam");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinksAltParam?alt=application%2Fatom%2Bxml");

            links.clear();
            systemLinksBuilder.addAltParam(false).queryParam("alt", "foo").build(links);
            assertEquals(2, links.size());
            assertLink(links, "self", null, "systemLinksAltParam?alt=foo");
            assertLink(links, "alternate", "application/atom+xml", "systemLinksAltParam?alt=foo");

            links.clear();
            systemLinksBuilder.addAltParam(true).build(links);
            assertEquals(2, links.size());
            assertLink(links, "self", null, "systemLinksAltParam?alt=foo");
            assertLink(links, "alternate", "application/atom+xml", "systemLinksAltParam?alt=foo");
            return "";
        }
    }

    @Path("systemLinksSelective")
    public static class SystemLinksSelectiveResource {

        @GET
        @Produces( {"application/xml", "application/json"})
        public void getXmlOrJson() {
        }

        @PUT
        @Produces("application/xml")
        public void putXml() {
        }

        @GET
        @Produces("application/opensearchdescription+xml")
        public void getOpensearch() {
        }

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {
            SystemLinksBuilder builder = builders.createSystemLinksBuilder();

            List<SyndLink> links = new ArrayList<SyndLink>();
            builder.types(LinkType.ALTERNATE).build(links);
            assertEquals(3, links.size());
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "systemLinksSelective?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/json",
                       "systemLinksSelective?alt=application%2Fjson");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinksSelective?alt=application%2Fatom%2Bxml");

            links.clear();
            builder.types(LinkType.SELF).build(links);
            assertEquals(1, links.size());
            assertLink(links, "self", null, "systemLinksSelective");

            links.clear();
            builder.types(LinkType.EDIT).build(links);
            assertEquals(1, links.size());
            assertLink(links, "edit", null, "systemLinksSelective");

            links.clear();
            builder.types(LinkType.OPENSEARCH).build(links);
            assertEquals(1, links.size());
            assertLink(links,
                       "search",
                       "application/opensearchdescription+xml",
                       "systemLinksSelective?alt=application%2Fopensearchdescription%2Bxml");

            links.clear();
            builder.types(LinkType.SELF, LinkType.EDIT).build(links);
            assertEquals(2, links.size());
            assertLink(links, "self", null, "systemLinksSelective");
            assertLink(links, "edit", null, "systemLinksSelective");

            links.clear();
            builder.types(LinkType.EDIT, LinkType.OPENSEARCH).build(links);
            assertEquals(2, links.size());
            assertLink(links, "edit", null, "systemLinksSelective");
            assertLink(links,
                       "search",
                       "application/opensearchdescription+xml",
                       "systemLinksSelective?alt=application%2Fopensearchdescription%2Bxml");

            links.clear();
            builder.types(LinkType.SELF, LinkType.EDIT, LinkType.OPENSEARCH, LinkType.ALTERNATE)
                .build(links);
            assertEquals(6, links.size());
            assertLink(links, "self", null, "systemLinksSelective");
            assertLink(links, "edit", null, "systemLinksSelective");
            assertLink(links,
                       "search",
                       "application/opensearchdescription+xml",
                       "systemLinksSelective?alt=application%2Fopensearchdescription%2Bxml");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "systemLinksSelective?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/json",
                       "systemLinksSelective?alt=application%2Fjson");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinksSelective?alt=application%2Fatom%2Bxml");

            links.clear();
            builder.types().build(links);
            assertEquals(6, links.size());
            assertLink(links, "self", null, "systemLinksSelective");
            assertLink(links, "edit", null, "systemLinksSelective");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "systemLinksSelective?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/json",
                       "systemLinksSelective?alt=application%2Fjson");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinksSelective?alt=application%2Fatom%2Bxml");
            assertLink(links,
                       "search",
                       "application/opensearchdescription+xml",
                       "systemLinksSelective?alt=application%2Fopensearchdescription%2Bxml");

            return "";
        }
    }

    @Path("systemLinksRelativeToAnotherUri")
    public static class SystemLinksRelativeToAnotherUriResource {

        @GET
        @Produces( {"application/xml"})
        public void getXml() {
        }

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {

            List<SyndLink> links = new ArrayList<SyndLink>();
            SystemLinksBuilder builder = builders.createSystemLinksBuilder();
            builder.relativeTo(URI.create("http://localhost:80/foo/bar")).build(links);
            assertEquals(3, links.size());
            assertLink(links, "self", null, "../systemLinksRelativeToAnotherUri");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "../systemLinksRelativeToAnotherUri?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "../systemLinksRelativeToAnotherUri?alt=application%2Fatom%2Bxml");

            links.clear();
            builder.baseUri(URI.create("http://koko:81")).relativeTo(URI
                .create("http://koko:81/foo/bar")).build(links);
            assertEquals(3, links.size());
            assertLink(links, "self", null, "../systemLinksRelativeToAnotherUri");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "../systemLinksRelativeToAnotherUri?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "../systemLinksRelativeToAnotherUri?alt=application%2Fatom%2Bxml");

            links.clear();
            builder.baseUri(URI.create("http://koko:82/")).relativeTo(URI
                .create("http://koko:82/foo/bar/zoo/")).build(links);
            assertEquals(3, links.size());
            assertLink(links, "self", null, "../../../systemLinksRelativeToAnotherUri");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "../../../systemLinksRelativeToAnotherUri?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "../../../systemLinksRelativeToAnotherUri?alt=application%2Fatom%2Bxml");

            links.clear();
            builder.baseUri(URI.create("http://koko:82/")).relativeTo(URI
                .create("http://koko:82/foo/bar/zoo/")).build(links);
            assertEquals(3, links.size());
            assertLink(links, "self", null, "../../../systemLinksRelativeToAnotherUri");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "../../../systemLinksRelativeToAnotherUri?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "../../../systemLinksRelativeToAnotherUri?alt=application%2Fatom%2Bxml");

            return "";
        }
    }

    @Path("systemLinksWithRelativeRequest")
    public static class SystemLinksWithRelativeRequestResource {

        @GET
        @Produces( {"application/xml"})
        public void getXml() {
        }

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {

            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().build(links);
            assertEquals(3, links.size());
            assertLink(links, "self", null, "systemLinksWithRelativeRequest");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "systemLinksWithRelativeRequest?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinksWithRelativeRequest?alt=application%2Fatom%2Bxml");
            return "";
        }
    }

    @Path("systemLinksWithAbsoluteRequest")
    public static class SystemLinksWithAbsoluteRequestResource {

        @GET
        @Produces( {"application/xml"})
        public void getXml() {
        }

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {

            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().build(links);
            assertEquals(3, links.size());
            assertLink(links, "self", null, "http://localhost:80/systemLinksWithAbsoluteRequest");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "http://localhost:80/systemLinksWithAbsoluteRequest?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "http://localhost:80/systemLinksWithAbsoluteRequest?alt=application%2Fatom%2Bxml");
            return "";
        }
    }

    @Path("systemLinksWithRelativeOverride")
    public static class SystemLinksWithRelativeOverrideResource {

        @GET
        @Produces( {"application/xml"})
        public void getXml() {
        }

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {

            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().relativize(true).build(links);
            assertEquals(3, links.size());
            assertLink(links, "self", null, "systemLinksWithRelativeOverride");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "systemLinksWithRelativeOverride?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinksWithRelativeOverride?alt=application%2Fatom%2Bxml");
            return "";
        }
    }

    @Path("systemLinksWithAbsoluteOverride")
    public static class SystemLinksWithAbsoluteOverrideResource {

        @GET
        @Produces( {"application/xml"})
        public void getXml() {
        }

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {

            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().relativize(false).build(links);
            assertEquals(3, links.size());
            assertLink(links, "self", null, "http://localhost:80/systemLinksWithAbsoluteOverride");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "http://localhost:80/systemLinksWithAbsoluteOverride?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "http://localhost:80/systemLinksWithAbsoluteOverride?alt=application%2Fatom%2Bxml");
            return "";
        }
    }

    @Path("systemLinksOfAnotherClass")
    public static class systemLinksOfAnotherClassResource {

        @GET
        @Produces("text/html")
        public void getHtml() {
        }

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {
            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().resource(SystemLinksResource.class).build(links);
            SystemLinksResource.assertLinks(links);

            links.clear();
            builders.createSystemLinksBuilder().resource(this).resource(SystemLinksResource.class)
                .build(links);
            SystemLinksResource.assertLinks(links);
            return "";
        }
    }

    @Path("systemLinksOfAnotherObject")
    public static class systemLinksOfAnotherObjectResource {

        @GET
        @Produces("text/html")
        public void getHtml() {
        }

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {

            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().resource(new SystemLinksResource()).build(links);
            SystemLinksResource.assertLinks(links);

            links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().resource(systemLinksOfAnotherObjectResource.class)
                .resource(new SystemLinksResource()).build(links);
            SystemLinksResource.assertLinks(links);
            return "";
        }
    }

    @Path("systemLinksOfSubResource/{id}")
    public static class systemLinksOfSubResourceResource {

        @Path("koko")
        @GET
        @Produces("application/koko")
        public void getKoko() {
        }

        @Path("{subId}")
        @GET
        @Produces( {"application/xml", "application/json"})
        public void getXmlOrJson() {
        }

        @Path("{subId}")
        @GET
        @Produces("text/html")
        public void getHtml() {
        }

        @Path("{subId}")
        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {

            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().build(links);
            assertEquals(5, links.size());
            assertLink(links, "self", null, "1");
            assertLink(links, "alternate", "application/xml", "1?alt=application%2Fxml");
            assertLink(links, "alternate", "application/json", "1?alt=application%2Fjson");
            assertLink(links, "alternate", "text/html", "1?alt=text%2Fhtml");
            assertLink(links, "alternate", "application/atom+xml", "1?alt=application%2Fatom%2Bxml");
            return "";
        }
    }

    @Path("systemLinksOfSubResourceFromResource/{id}")
    public static class systemLinksOfSubResourceFromResourceResource {

        @Path("koko")
        @GET
        @Produces("application/koko")
        public void getKoko() {
        }

        @Path("{subId}")
        @GET
        @Produces( {"application/xml", "application/json"})
        public void getXmlOrJson() {
        }

        @Path("{subId}")
        @GET
        @Produces("text/html")
        public void getHtml() {
        }

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {

            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().subResource("{subId}").pathParam("subId", "1")
                .build(links);
            assertEquals(4, links.size());
            assertLink(links, "self", null, "a/1");
            assertLink(links, "alternate", "application/xml", "a/1?alt=application%2Fxml");
            assertLink(links, "alternate", "application/json", "a/1?alt=application%2Fjson");
            assertLink(links, "alternate", "text/html", "a/1?alt=text%2Fhtml");
            return "";
        }
    }

    @Path("systemLinksOfAnotherSubResource/{id}")
    public static class systemLinksOfAnotherSubResourceResource {

        @Path("{subId}")
        @GET
        @Produces( {"application/xml", "application/json"})
        public void getXmlOrJson() {
        }

        @Path("{subId}")
        @GET
        @Produces("text/html")
        public void getHtml() {
        }

        @Path("koko")
        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {
            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().subResource("{subId}").pathParam("subId", "1")
                .build(links);
            assertEquals(4, links.size());
            assertLink(links, "self", null, "1");
            assertLink(links, "alternate", "application/xml", "1?alt=application%2Fxml");
            assertLink(links, "alternate", "application/json", "1?alt=application%2Fjson");
            assertLink(links, "alternate", "text/html", "1?alt=text%2Fhtml");
            return "";
        }
    }

    @Path("systemLinksOfAnotherResourceWithSubResource/{id}")
    public static class systemLinksOfAnotherResourceWithSubResourceResource {

        @Path("koko")
        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {

            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().resource(systemLinksOfSubResourceResource.class)
                .subResource("{subId}").pathParam("id", "a").pathParam("subId", "1").build(links);
            assertEquals(5, links.size());
            assertLink(links, "self", null, "../../systemLinksOfSubResource/a/1");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "../../systemLinksOfSubResource/a/1?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/json",
                       "../../systemLinksOfSubResource/a/1?alt=application%2Fjson");
            assertLink(links,
                       "alternate",
                       "text/html",
                       "../../systemLinksOfSubResource/a/1?alt=text%2Fhtml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "../../systemLinksOfSubResource/a/1?alt=application%2Fatom%2Bxml");
            return "";
        }
    }

    @Path("systemLinksThroughSubResourceLocator")
    public static class systemLinksThroughSubResourceLocatorResource {
        @Path("sub1")
        public LocatedResourceWithSystemLinks getLocated() {
            return new LocatedResourceWithSystemLinks();
        }
    }

    public static class LocatedResourceWithSystemLinks {
        @GET
        @Produces("text/html")
        public void getHtml() {
        }

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {
            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().relativize(false).build(links);
            assertEquals(3, links.size());
            assertLink(links,
                       "self",
                       null,
                       "http://localhost:80/systemLinksThroughSubResourceLocator/sub1");
            assertLink(links,
                       "alternate",
                       "text/html",
                       "http://localhost:80/systemLinksThroughSubResourceLocator/sub1?alt=text%2Fhtml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "http://localhost:80/systemLinksThroughSubResourceLocator/sub1?alt=application%2Fatom%2Bxml");
            return "";
        }

        @Path("sub2")
        @GET
        @Produces("text/html")
        public void getHtmlSub() {
        }

        @Path("sub2")
        @GET
        @Produces("application/atom+xml")
        public String getSubAtom(@Context LinkBuilders builders) {
            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().relativize(false).build(links);
            assertEquals(3, links.size());
            assertLink(links,
                       "self",
                       null,
                       "http://localhost:80/systemLinksThroughSubResourceLocator/sub1/sub2");
            assertLink(links,
                       "alternate",
                       "text/html",
                       "http://localhost:80/systemLinksThroughSubResourceLocator/sub1/sub2?alt=text%2Fhtml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "http://localhost:80/systemLinksThroughSubResourceLocator/sub1/sub2?alt=application%2Fatom%2Bxml");
            return "";
        }
    }

    @Path("systemLinksWithQueryParams")
    public static class systemLinksWithQueryParamResource {

        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {

            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSystemLinksBuilder().queryParam("a", "1").queryParam("b", "2")
                .build(links);
            assertEquals(2, links.size());
            assertLink(links, "self", null, "systemLinksWithQueryParams?a=1&b=2");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinksWithQueryParams?a=1&b=2&alt=application%2Fatom%2Bxml");
            return "";
        }
    }

    @Path("systemLinks/ContinuedSearch")
    public static class SystemLinksContinuedSearch1Resource {
        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders, @Context UriInfo uriInfo) {
            List<SyndLink> links = null;

            URI base = uriInfo.getBaseUri();
            SystemLinksBuilder builder = builders.createSystemLinksBuilder().relativeTo(base);

            links = new ArrayList<SyndLink>();
            builder.allResources(true).build(links);
            assertEquals(6, links.size());
            assertLink(links, "self", null, "systemLinks/ContinuedSearch");
            assertLink(links, "edit", null, "systemLinks/ContinuedSearch");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "systemLinks/ContinuedSearch?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/json",
                       "systemLinks/ContinuedSearch?alt=application%2Fjson");
            assertLink(links,
                       "alternate",
                       "text/html",
                       "systemLinks/ContinuedSearch?alt=text%2Fhtml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinks/ContinuedSearch?alt=application%2Fatom%2Bxml");

            links = new ArrayList<SyndLink>();
            builder.allResources(false).build(links);
            assertEquals(2, links.size());
            assertLink(links, "self", null, "systemLinks/ContinuedSearch");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinks/ContinuedSearch?alt=application%2Fatom%2Bxml");

            links = new ArrayList<SyndLink>();
            builder.resource(SystemLinksContinuedSearch3Resource.class)
                .pathParam("v1", "Continued").pathParam("v2", "Search").allResources(true)
                .build(links);
            assertEquals(6, links.size());
            assertLink(links, "self", null, "systemLinks/ContinuedSearch");
            assertLink(links, "edit", null, "systemLinks/ContinuedSearch");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "systemLinks/ContinuedSearch?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/json",
                       "systemLinks/ContinuedSearch?alt=application%2Fjson");
            assertLink(links,
                       "alternate",
                       "text/html",
                       "systemLinks/ContinuedSearch?alt=text%2Fhtml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinks/ContinuedSearch?alt=application%2Fatom%2Bxml");

            links = new ArrayList<SyndLink>();
            builder.allResources(false).build(links);
            assertEquals(3, links.size());
            assertLink(links, "self", null, "systemLinks/ContinuedSearch");
            assertLink(links, "edit", null, "systemLinks/ContinuedSearch");
            assertLink(links,
                       "alternate",
                       "text/html",
                       "systemLinks/ContinuedSearch?alt=text%2Fhtml");

            return "";
        }
    }

    @Path("systemLinks/Continued{v}")
    public static class SystemLinksContinuedSearch2Resource {
        @GET
        @Produces( {"application/xml", "application/json"})
        public void getXmlOrJson() {
        }

        @GET
        @Produces("text/html")
        public void getHtml() {
        }
    }

    @Path("systemLinks/{v1}{v2}")
    public static class SystemLinksContinuedSearch3Resource {
        @GET
        @Produces("text/html")
        public void getHtml() {
        }

        @PUT
        @Produces("text/plain")
        public void postAtom() {
        }
    }

    @Path("systemLinks/ContinuedSearch/SubResources")
    public static class SystemLinksContinuedSearchSubResources1Resource {
        @Path("{id}")
        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders, @Context UriInfo uriInfo) {
            List<SyndLink> links = null;

            URI base = uriInfo.getBaseUri();
            SystemLinksBuilder builder = builders.createSystemLinksBuilder().relativeTo(base);

            links = new ArrayList<SyndLink>();
            builder.allResources(true).build(links);
            assertEquals(6, links.size());
            assertLink(links, "self", null, "systemLinks/ContinuedSearch/SubResources/1");
            assertLink(links, "edit", null, "systemLinks/ContinuedSearch/SubResources/1");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "systemLinks/ContinuedSearch/SubResources/1?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/json",
                       "systemLinks/ContinuedSearch/SubResources/1?alt=application%2Fjson");
            assertLink(links,
                       "alternate",
                       "text/html",
                       "systemLinks/ContinuedSearch/SubResources/1?alt=text%2Fhtml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinks/ContinuedSearch/SubResources/1?alt=application%2Fatom%2Bxml");

            links = new ArrayList<SyndLink>();
            builder.allResources(false).build(links);
            assertEquals(2, links.size());
            assertLink(links, "self", null, "systemLinks/ContinuedSearch/SubResources/1");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinks/ContinuedSearch/SubResources/1?alt=application%2Fatom%2Bxml");

            links = new ArrayList<SyndLink>();
            builder.resource(SystemLinksContinuedSearchSubResources3Resource.class)
                .subResource("{id}").pathParam("v1", "Continued").pathParam("v2", "Search")
                .pathParam("id", "1").allResources(true).build(links);
            assertEquals(6, links.size());
            assertLink(links, "self", null, "systemLinks/ContinuedSearch/SubResources/1");
            assertLink(links, "edit", null, "systemLinks/ContinuedSearch/SubResources/1");
            assertLink(links,
                       "alternate",
                       "application/xml",
                       "systemLinks/ContinuedSearch/SubResources/1?alt=application%2Fxml");
            assertLink(links,
                       "alternate",
                       "application/json",
                       "systemLinks/ContinuedSearch/SubResources/1?alt=application%2Fjson");
            assertLink(links,
                       "alternate",
                       "text/html",
                       "systemLinks/ContinuedSearch/SubResources/1?alt=text%2Fhtml");
            assertLink(links,
                       "alternate",
                       "application/atom+xml",
                       "systemLinks/ContinuedSearch/SubResources/1?alt=application%2Fatom%2Bxml");

            links = new ArrayList<SyndLink>();
            builder.allResources(false).build(links);
            assertEquals(3, links.size());
            assertLink(links, "self", null, "systemLinks/ContinuedSearch/SubResources/1");
            assertLink(links, "edit", null, "systemLinks/ContinuedSearch/SubResources/1");
            assertLink(links,
                       "alternate",
                       "text/html",
                       "systemLinks/ContinuedSearch/SubResources/1?alt=text%2Fhtml");

            return "";
        }
    }

    @Path("systemLinks/Continued{v}/SubResources")
    public static class SystemLinksContinuedSearchSubResources2Resource {
        @Path("{id}")
        @GET
        @Produces( {"application/xml", "application/json"})
        public void getXmlOrJson() {
        }

        @Path("{id}")
        @GET
        @Produces("text/html")
        public void getHtml() {
        }
    }

    @Path("systemLinks/{v1}{v2}/SubResources")
    public static class SystemLinksContinuedSearchSubResources3Resource {
        @Path("{id}")
        @GET
        @Produces("text/html")
        public void getHtml() {
        }

        @Path("{id}")
        @PUT
        @Produces("text/plain")
        public void postAtom() {
        }
    }

    // -- system links tests

    public void testSystemLinks() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinks",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksSelective() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksSelective",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksAltParam() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksAltParam",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksWithPut() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksWithPut",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksRelativeToAnotherUri() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksRelativeToAnotherUri",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksWithRelativeRequest() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksWithRelativeRequest",
                                                        MediaType.APPLICATION_ATOM_XML);
        request.setQueryString("relative-urls=true");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksWithAbsoluteRequest() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksWithAbsoluteRequest",
                                                        MediaType.APPLICATION_ATOM_XML);
        request.setQueryString("relative-urls=false");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksWithRelativeOverride() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksWithRelativeOverride",
                                                        MediaType.APPLICATION_ATOM_XML);
        request.setQueryString("relative-urls=false");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksWithAbsoluteOverride() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksWithAbsoluteOverride",
                                                        MediaType.APPLICATION_ATOM_XML);
        request.setQueryString("relative-urls=true");
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksOfAnotherClass() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksOfAnotherClass",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksOfAnotherObject() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksOfAnotherObject",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksOfSubResource() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksOfSubResource/a/1",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksOfSubResourceFromResource() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksOfSubResourceFromResource/a",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksOfAnotherSubResource() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksOfAnotherSubResource/a/koko",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksOfAnotherResourceWithSubResource() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/systemLinksOfAnotherResourceWithSubResource/a/koko",
                                      MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksThroughSubResourceLocator() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/systemLinksThroughSubResourceLocator/sub1",
                                      MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());

        request =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/systemLinksThroughSubResourceLocator/sub1/sub2",
                                      MediaType.APPLICATION_ATOM_XML);
        response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksWithQueryParams() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinksWithQueryParams",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksWithContinuedSearch() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/systemLinks/ContinuedSearch",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSystemLinksWithContinuedSearchSubResources() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor
                .constructMockRequest("GET",
                                      "/systemLinks/ContinuedSearch/SubResources/1",
                                      MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    // -- single link resources

    @Path("singleLink")
    public static class SingleLinkResource {
        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {
            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSingleLinkBuilder().build(links);
            assertLinks(links);
            return "";
        }

        public static void assertLinks(List<SyndLink> links) {
            assertEquals(1, links.size());
            assertLink(links, null, null, "singleLink");
        }
    }

    @Path("singleLinkToAnotherClass")
    public static class SingleLinkToAnotherClassResource {
        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {
            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSingleLinkBuilder().resource(SingleLinkResource.class).build(links);
            SingleLinkResource.assertLinks(links);
            return "";
        }
    }

    @Path("singleLinkAltParam")
    public static class SingleLinkAltParamResource {
        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {
            List<SyndLink> links = new ArrayList<SyndLink>();
            SingleLinkBuilder singleLinkBuilder = builders.createSingleLinkBuilder();

            singleLinkBuilder.type(MediaType.APPLICATION_ATOM_XML_TYPE).build(links);
            assertEquals(1, links.size());
            assertLink(links,
                       null,
                       MediaType.APPLICATION_ATOM_XML,
                       "singleLinkAltParam?alt=application%2Fatom%2Bxml");

            links.clear();
            singleLinkBuilder.type(MediaType.APPLICATION_ATOM_XML_TYPE).addAltParam(false)
                .build(links);
            assertEquals(1, links.size());
            assertLink(links, null, MediaType.APPLICATION_ATOM_XML, "singleLinkAltParam");

            links.clear();
            singleLinkBuilder.type(MediaType.APPLICATION_ATOM_XML_TYPE).addAltParam(true)
                .build(links);
            assertEquals(1, links.size());
            assertLink(links,
                       null,
                       MediaType.APPLICATION_ATOM_XML,
                       "singleLinkAltParam?alt=application%2Fatom%2Bxml");

            links.clear();
            singleLinkBuilder.type(MediaType.APPLICATION_ATOM_XML_TYPE).queryParam("alt", "foo")
                .build(links);
            assertEquals(1, links.size());
            assertLink(links, null, MediaType.APPLICATION_ATOM_XML, "singleLinkAltParam?alt=foo");

            return "";
        }
    }

    @Path("singleLinkToAnotherObject")
    public static class SingleLinkToAnotherObjectResource {
        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {
            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSingleLinkBuilder().resource(new SingleLinkResource()).build(links);
            SingleLinkResource.assertLinks(links);
            return "";
        }
    }

    @Path("singleLinkWithRelAndType")
    public static class SingleLinkWithRelAndTypeResource {
        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {
            SingleLinkBuilder builder = builders.createSingleLinkBuilder();

            List<SyndLink> links = new ArrayList<SyndLink>();
            builder.rel("stam").build(links);
            assertEquals(1, links.size());
            assertLink(links, "stam", null, "singleLinkWithRelAndType");

            links.clear();
            builder.rel(null).type(MediaType.APPLICATION_JSON_TYPE).build(links);
            assertEquals(1, links.size());
            assertLink(links,
                       null,
                       "application/json",
                       "singleLinkWithRelAndType?alt=application%2Fjson");

            links.clear();
            builder.rel("stam").type(MediaType.APPLICATION_JSON_TYPE).build(links);
            assertEquals(1, links.size());
            assertLink(links,
                       "stam",
                       "application/json",
                       "singleLinkWithRelAndType?alt=application%2Fjson");

            links.clear();
            builder.resource(SingleLinkResource.class).rel("stam")
                .type(MediaType.APPLICATION_JSON_TYPE).build(links);
            assertEquals(1, links.size());
            assertLink(links, "stam", "application/json", "singleLink?alt=application%2Fjson");

            return "";
        }
    }

    @Path("singleLinkWithQuery")
    public static class SingleLinkWithQueryResource {
        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {
            List<SyndLink> links = new ArrayList<SyndLink>();
            builders.createSingleLinkBuilder().queryParam("a", "1").queryParam("b", "2")
                .build(links);
            assertEquals(1, links.size());
            assertLink(links, null, null, "singleLinkWithQuery?a=1&b=2");

            links.clear();
            builders.createSingleLinkBuilder().queryParam("a", "1").queryParam("alt", "stam/stam")
                .build(links);
            assertEquals(1, links.size());
            assertLink(links, null, null, "singleLinkWithQuery?a=1&alt=stam/stam");

            links.clear();
            builders.createSingleLinkBuilder().type(MediaType.APPLICATION_JSON_TYPE)
                .queryParam("a", "1").queryParam("alt", "stam/stam").build(links);
            assertEquals(1, links.size());
            assertLink(links, null, "application/json", "singleLinkWithQuery?a=1&alt=stam/stam");

            return "";
        }
    }

    @Path("singleLinkWithPathParam/{id}")
    public static class SingleLinkWithPathParamResource {
        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {
            List<SyndLink> links = new ArrayList<SyndLink>();

            SingleLinkBuilder builder =
                builders.createSingleLinkBuilder().resource(SingleLinkWithPathParamResource.class);
            builder.pathParam("id", "1").build(links);
            assertEquals(1, links.size());
            assertLink(links, null, null, "1");

            links.clear();
            builder.subResource("a/{subId}/b").pathParam("subId", "2").build(links);
            assertEquals(1, links.size());
            assertLink(links, null, null, "1/a/2/b");

            return "";
        }
    }

    @Path("singleLinkSelfAndEdit")
    public static class SingleLinkSelfAndEditResource {
        @GET
        @Produces("application/atom+xml")
        public String getAtom(@Context LinkBuilders builders) {
            List<SyndLink> links = new ArrayList<SyndLink>();

            builders.createSingleLinkBuilder().rel("self").build(links);
            assertEquals(1, links.size());
            assertLink(links, "self", null, "singleLinkSelfAndEdit");

            // change the self link without clearing the exiting links
            builders.createSingleLinkBuilder().resource(SingleLinkResource.class).rel("self")
                .build(links);
            assertEquals(1, links.size());
            assertLink(links, "self", null, "singleLink");

            links.clear();
            builders.createSingleLinkBuilder().rel("edit").build(links);
            assertEquals(1, links.size());
            assertLink(links, "edit", null, "singleLinkSelfAndEdit");

            // change the edit link without clearing the exiting links
            builders.createSingleLinkBuilder().resource(SingleLinkResource.class).rel("edit")
                .build(links);
            assertEquals(1, links.size());
            assertLink(links, "edit", null, "singleLink");

            return "";
        }
    }

    // -- single link tests

    public void testSingleLink() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/singleLink",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSingleLinkAltParam() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/singleLinkAltParam",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSingleLinkToAnotherClass() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/singleLinkToAnotherClass",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSingleLinkToAnotherObject() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/singleLinkToAnotherObject",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSingleLinkWithRelAndType() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/singleLinkWithRelAndType",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSingleLinkWithQuery() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/singleLinkWithQuery",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSingleLinkWithPathParam() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/singleLinkWithPathParam/1",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    public void testSingleLinkSelfAndEdit() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/singleLinkSelfAndEdit",
                                                        MediaType.APPLICATION_ATOM_XML);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
    }

    // -- helpers

    public static void assertLink(List<SyndLink> links, String rel, String type, String href) {
        SyndLink link = new SyndLink(rel, type, href);
        assertTrue(links.contains(link));
    }

}
