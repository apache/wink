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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.wink.common.AbstractDynamicResource;
import org.apache.wink.common.annotations.Workspace;
import org.apache.wink.common.categories.Categories;
import org.apache.wink.common.categories.CollectionCategories;
import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.internal.utils.OpenSearchUtils;
import org.apache.wink.common.model.opensearch.OpenSearchDescription;
import org.apache.wink.common.model.opensearch.OpenSearchImage;
import org.apache.wink.common.model.opensearch.OpenSearchParameter;
import org.apache.wink.common.model.opensearch.OpenSearchQuery;
import org.apache.wink.common.model.opensearch.OpenSearchUrl;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndFeed;
import org.apache.wink.common.model.synd.SyndPerson;
import org.apache.wink.common.model.synd.SyndText;
import org.apache.wink.example.qadefect.legacy.DefectBean;
import org.apache.wink.example.qadefect.providers.DefectBeanProvider;
import org.apache.wink.example.qadefect.store.DataStore;
import org.apache.wink.example.qadefect.utils.SearchMap;
import org.apache.wink.server.utils.LinkBuilders;

@Workspace(workspaceTitle = "QA Defects", collectionTitle = "Defects")
public class DefectsResource extends AbstractDynamicResource implements CollectionCategories {

    public static final String  CUSTOMIZED_JSP_PATH =
                                                        "/HtmlCustomizedRepresentation/customizedHtmlEntry.jsp";
    public static final String  CUSTOMIZED_JSP_ATTR = "DefectAssetAttr";
    public static final String  URL                 = "/defects";
    public static final String  DEFECT_PARAM        = "defect";
    public static final String  DEFECT_URL          = "/{" + DEFECT_PARAM + "}";
    public static final String  DEFECT_TESTS_URL    = "/{" + DEFECT_PARAM + "}" + TestsResource.URL;
    public static final String  SEVERIIY            = "severity";
    public static final String  ASSIGNED_TO         = "assignedTo";
    private static final String URN_ASSIGNED_TO     = "urn:hp:defect:assignedTo";
    private static final String URN_SEVERIIY        = "urn:hp:defect:severity";
    public static final String  FTS                 = "q";

    private TestsResource       defectTestResource;
    private CategoriesResource  categoriesResource;

    @Context
    private UriInfo             uriInfo;
    @Context
    private LinkBuilders        linkBuilders;

    private final DataStore     store               = DataStore.getInstance();

    @GET
    @Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
    public SyndFeed getCollection(@QueryParam(value = FTS) String query,
                                  @QueryParam(value = SEVERIIY) String severity,
                                  @QueryParam(value = ASSIGNED_TO) String assignedTo) {

        // fill search parameters
        // the parameters may be absent, the SearchMap will do the filtering
        SearchMap searchParameters = new SearchMap();
        searchParameters.put(FTS, query);
        searchParameters.put(SEVERIIY, severity);
        searchParameters.put(ASSIGNED_TO, assignedTo);

        // create data object (populated with store data)
        Collection<DefectBean> collectionBean = store.getDefects(searchParameters);
        if (collectionBean == null || collectionBean.isEmpty()) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        return createSyndFeed(collectionBean);
    }

    private SyndFeed createSyndFeed(Collection<DefectBean> collectionBean) {
        // create a defect collection resource and set its metadata (resource
        // has no data)

        String baseUri = uriInfo.getAbsolutePath().toString();
        SyndFeed syndFeed = new SyndFeed();

        // set common collection fields
        syndFeed.setId("urn:com:hp:qadefects:defects");
        syndFeed.setTitle(new SyndText("Defects"));
        syndFeed.getAuthors().add(new SyndPerson("admin"));
        syndFeed.setUpdated(new Date());

        // base URL
        syndFeed.setBase(baseUri);

        for (DefectBean defectBean : collectionBean) {
            SyndEntry syndEntry = DefectBeanProvider.createSyndEntry(defectBean);
            // generate self and alternative representation links in entries
            DefectBeanProvider.generateLinksForEntry(syndEntry, linkBuilders, defectTestResource);
            syndFeed.getEntries().add(syndEntry);
        }

        // generate self and alternative representation links in collection
        linkBuilders.createSystemLinksBuilder().build(syndFeed.getLinks());

        return syndFeed;
    }

    /**
     * Sub-resource - get a single defect
     * 
     * @param defectId
     * @return
     */
    @Path(DEFECT_URL)
    @GET
    @Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML, MediaType.TEXT_HTML, MediaTypeUtils.PDF})
    public Object getDefectDocument(@PathParam(DEFECT_PARAM) String defectId) {

        // create data object (populated with store data)
        DefectBean bean = store.getDefect(defectId);
        if (bean == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return bean;
    }

    @Path(DEFECT_URL)
    @PUT
    @Consumes( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_XML})
    @Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML})
    public DefectBean updateDocument(@PathParam(DEFECT_PARAM) String defectId,
                                     DefectBean updatedBean) throws IOException {

        // obtain data object from the memory store
        DefectBean bean = store.getDefect(defectId);
        if (bean == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        updatedBean.setId(defectId);
        store.putDefect(defectId, updatedBean);

        return updatedBean;
    }

    /**
     * Subresource - delete a defect
     * 
     * @param defectId
     * @return
     */
    @Path(DEFECT_URL)
    @DELETE
    @Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML, MediaType.TEXT_HTML})
    public DefectBean deleteDocument(@PathParam(DEFECT_PARAM) String defectId) {

        // obtain data object from memory store
        DefectBean bean = store.getDefect(defectId);
        if (bean == null) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        return store.removeDefect(defectId);
    }

    /**
     * <p>
     * Method is handling POST requests. POST request creates new resource from
     * data received in request body. Server side decides about the new resource
     * URI (Location header) and returns status code 201 (created). The data can
     * be Atom entry with xml content (application/atom+xml) or just xml
     * (application/xml)
     * 
     * @return response with status code and resource representation of created
     *         resource
     * @throws IOException problems with reading HTTP request content (the
     *             message body)
     * @throws URISyntaxException
     */
    @POST
    @Consumes( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_XML})
    @Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML})
    public Response createDocument(DefectBean bean) throws IOException, URISyntaxException {

        bean.setId(store.getDefectUniqueId());

        store.putDefect(bean.getId(), bean);

        String location = uriInfo.getAbsolutePath().toString() + "/" + bean.getId();
        return Response.status(Status.CREATED).entity(bean).location(new URI(location)).build();
    }

    /**
     * Method is handling GET request for OpenSearch representation.
     * <p/>
     * <em>Handled URI:</em>
     * <code>/defects?alt=application/opensearchdescription+xml</code> - returns
     * OpenSearch representation
     * 
     * @return response with OpenSearch resource representation
     */
    @GET
    @Produces(value = {MediaTypeUtils.OPENSEARCH})
    public OpenSearchDescription getOpenSearch() {
        return getOpenSearchDescription(uriInfo.getBaseUri().toString());
    }

    /**
     * Add OpenSearch representation to this defect collection resource.
     * OpenSearch description document describes search infrastructure
     * interface. (see <a href="http://www.opensearch.org/">OpenSearch</a>)
     * 
     * @param baseUri baseUri to use as the base for all links
     */
    private OpenSearchDescription getOpenSearchDescription(String baseUri) {
        OpenSearchDescription openSearchDescription = new OpenSearchDescription();
        openSearchDescription.setShortName("HP Defect Manager search engine");
        openSearchDescription.setDescription("You can search defects in HP Defect Manager");
        openSearchDescription.setLongName("HP Defect Manager search engine");
        openSearchDescription.setContact("john.smith@example.com");
        openSearchDescription.setDeveloper("John Smith");
        openSearchDescription.addLanguage("en-US");
        openSearchDescription.setTags("defect bug");
        openSearchDescription.addInputEncoding("UTF-8");
        openSearchDescription.addOutputEncoding("UTF-8");

        // set OpenSearch URL parameters
        OpenSearchParameter severityParameter =
            new OpenSearchParameter(SEVERIIY, URN_SEVERIIY, false);
        OpenSearchParameter ftsParameter =
            new OpenSearchParameter(FTS, OpenSearchParameter.OpenSearchParams.searchTerms
                .toString(), false);
        OpenSearchParameter assignedToParameter =
            new OpenSearchParameter(ASSIGNED_TO, URN_ASSIGNED_TO, false);

        // create Search URL & populate search parameters for browsers
        OpenSearchUrl openSearchUrlForBrowsers = new OpenSearchUrl();
        openSearchUrlForBrowsers.addOpenSearchParameter(ftsParameter);
        openSearchUrlForBrowsers.setType(MediaType.TEXT_HTML);

        // create Search URL & populate search parameters
        OpenSearchUrl openSearchUrl = new OpenSearchUrl();
        openSearchUrl.addOpenSearchParameter(severityParameter);
        openSearchUrl.addOpenSearchParameter(ftsParameter);
        openSearchUrl.addOpenSearchParameter(assignedToParameter);
        openSearchUrl.setType(MediaType.TEXT_HTML);

        // create open search base uri
        StringBuilder openSearchUrlBuilder = new StringBuilder(baseUri);
        if (baseUri.endsWith("/") && DefectsResource.URL.startsWith("/")) {
            openSearchUrlBuilder.append(DefectsResource.URL.substring(1));
        } else {
            openSearchUrlBuilder.append(DefectsResource.URL);
        }
        openSearchUrl.setBaseUri(openSearchUrlBuilder.toString());
        openSearchUrlForBrowsers.setBaseUri(openSearchUrlBuilder.toString());

        // add URLs to OpenSearch
        openSearchDescription.addUrl(openSearchUrlForBrowsers);
        openSearchDescription.addUrl(openSearchUrl);

        // add OpenSearch Query element
        OpenSearchQuery openSearchQuery = new OpenSearchQuery();
        openSearchQuery.setRole(OpenSearchQuery.QueryRole.example.toString());
        openSearchQuery.setSearchTerms("Search Terms");
        openSearchDescription.addQuery(openSearchQuery);

        // add OpenSearch Images
        OpenSearchImage openSearchImage;
        openSearchImage =
            OpenSearchUtils.createOpenSearchImage(MediaTypeUtils.IMAGE_JPEG, openSearchUrlBuilder
                .toString() + "splash.jpg");
        openSearchDescription.addNewImage(openSearchImage);

        return openSearchDescription;
    }

    public void setDefectTestResource(TestsResource defectTestResource) {
        this.defectTestResource = defectTestResource;
    }

    public TestsResource getDefectTestResource() {
        return defectTestResource;
    }

    public void setCategoriesResource(CategoriesResource categoriesResource) {
        this.categoriesResource = categoriesResource;
    }

    /**
     * Return the list of Collection Categories
     */
    public List<Categories> getCategories() {

        List<Categories> catsList = new ArrayList<Categories>();

        // Defect severity categories are defined in stand-alone Categories
        // Document
        // created by CategoriesResource
        Categories severityCategories = new Categories();
        MultivaluedMap<String, String> variables = new MultivaluedMapImpl<String, String>();
        variables.add(CategoriesResource.CategoryParamCN, CategoriesResource.SeverityCN);
        severityCategories.setHref(categoriesResource, variables);
        catsList.add(severityCategories);

        // Build defect status categories object for ServiceDocument
        Categories statusCategories = CategoriesResource.buildStatusCategoriesDocument();

        catsList.add(statusCategories);

        return catsList;

    }

}
