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

package org.apache.wink.example.qadefect.resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.wink.common.AbstractDynamicResource;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndFeed;
import org.apache.wink.common.model.synd.SyndPerson;
import org.apache.wink.common.model.synd.SyndText;
import org.apache.wink.example.qadefect.legacy.DefectBean;
import org.apache.wink.example.qadefect.legacy.TestBean;
import org.apache.wink.example.qadefect.providers.TestBeanProvider;
import org.apache.wink.example.qadefect.store.DataStore;
import org.apache.wink.server.utils.LinkBuilders;

public class TestsResource extends AbstractDynamicResource {

    public static final String URL_TO_REDIRECT   = "/applicationJSPs/testCollection.jsp";
    public static final String URL               = "/tests";
    public static final String DEFECT_HTTP_PARAM = "defect";
    public static final String TEST_PARAM        = "test";
    public static final String TEST_URL          = "/{" + TEST_PARAM + "}";

    @Context
    private UriInfo            uriInfo;

    @Context
    private LinkBuilders       linkBuilders;

    @Context
    private HttpServletRequest httpServletRequest;

    private TestsResource      testsResource;

    private final DataStore    store             = DataStore.getInstance();

    /**
     * Handles the GET requests to the collection of Tests
     * 
     * @return
     */
    @GET
    @Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON})
    public Response getTestsCollection(@PathParam(DefectsResource.DEFECT_PARAM) String defectId) {

        // create data object (populated with store data)
        Collection<TestBean> collectionBean = null;
        if (defectId == null) {
            collectionBean = store.getTests();
            if (collectionBean == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
        } else {
            DefectBean bean = store.getDefect(defectId);
            if (bean == null) {
                return Response.status(Status.NOT_FOUND).build();
            }

            collectionBean = bean.getTests();
            if (collectionBean == null || collectionBean.isEmpty()) {
                return Response.status(Status.NOT_FOUND).build();
            }
        }

        SyndFeed syndFeed = buildCollectionAsset(collectionBean, defectId);
        return Response.ok(syndFeed).build();

    }

    private SyndFeed buildCollectionAsset(Collection<TestBean> collectionBean, String defectId) {
        SyndFeed syndFeed = new SyndFeed();

        // set common collection fields
        syndFeed.setId("urn:com:hp:qadefects:tests");
        syndFeed.setTitle(new SyndText("Tests"));
        syndFeed.getAuthors().add(new SyndPerson("admin"));
        syndFeed.setUpdated(new Date());

        // base URL
        syndFeed.setBase(uriInfo.getAbsolutePath().toString());

        for (TestBean testBean : collectionBean) {

            SyndEntry syndEntry = TestBeanProvider.createSyndEntry(testBean);
            syndFeed.getEntries().add(syndEntry);

            if (defectId != null) {
                // these are tests assigned to a defect
                linkBuilders.createSystemLinksBuilder().pathParam(DefectsResource.DEFECT_PARAM,
                                                                  defectId).build(syndEntry
                    .getLinks());
            } else {
                // the whole tests collection
                String tesId = syndEntry.getId().substring(syndEntry.getId().lastIndexOf(':') + 1);
                linkBuilders.createSystemLinksBuilder().subResource(TEST_URL).pathParam(TEST_PARAM,
                                                                                        tesId)
                    .build(syndEntry.getLinks());
            }
        }

        // generate self and alternative representation links in collection
        linkBuilders.createSystemLinksBuilder().build(syndFeed.getLinks());

        return syndFeed;
    }

    /**
     * Handles the GET request to collection of Tests in HTML representation.
     * 
     * @return Response with new Location to redirect
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response redirectToApplication(@Context HttpServletRequest request,
                                          @PathParam(DefectsResource.DEFECT_PARAM) String defectId) {
        return Response.status(Status.SEE_OTHER).location(getHtmlLocation(request, defectId))
            .build();

    }

    /**
     * Subresource - retrieves a test with a specific id
     * 
     * @param testId
     * @return
     */
    @Path(TEST_URL)
    @GET
    @Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML, MediaType.TEXT_HTML})
    public TestBean getDocument(@PathParam(TEST_PARAM) String testId) {

        // create data object (populated with store data)
        TestBean bean = store.getTest(testId);
        if (bean == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        httpServletRequest.setAttribute(getClass().getName(), this);
        return bean;
    }

    /**
     * Build the URL to redirect.
     * 
     * @param defectId
     * @return URL to redirect
     */
    private URI getHtmlLocation(HttpServletRequest request, String defectId) {
        try {
            StringBuilder stringBuilder = new StringBuilder(request.getContextPath());
            stringBuilder.append(URL_TO_REDIRECT);
            if (defectId != null) {
                stringBuilder.append("?");
                stringBuilder.append(DEFECT_HTTP_PARAM);
                stringBuilder.append("=");
                stringBuilder.append(defectId);
            }
            // return the URL to redirect with show all as parameter
            return new URI(stringBuilder.toString());
        } catch (URISyntaxException e) {
            throw new WebApplicationException(e);
        }
    }

    public void setTestsResource(TestsResource testsResource) {
        this.testsResource = testsResource;
    }

    public TestsResource getTestsResource() {
        return testsResource;
    }

}
