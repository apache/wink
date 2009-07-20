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

package org.apache.wink.server.internal.resources;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.RestConstants;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.internal.uri.UriEncoder;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.internal.utils.UriHelper;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.ResourceRegistry;

@Path("/")
public class HtmlServiceDocumentResource extends RootResource {

    private static final String APP_SERVICE_DOCUMENT_TYPE =
                                                              MediaTypeUtils
                                                                  .toEncodedString(MediaTypeUtils.ATOM_SERVICE_DOCUMENT_TYPE);

    private static final String TEXT_CSS                  = "text/css";

    private String              serviceDocumentCssPath;

    private static final String START_HTML                = "<html>";
    private static final String END_HTML                  = "</html>";
    private static final String START_HEAD                = "<head>";
    private static final String CSS                       =
                                                              "<style type=\"text/css\" media=\"all\">  h1 {  padding: 4px 4px 4px 24px;  color: #333333;  background-color: #C8C8C8;  font-weight: bold;  font-size: 24px;} h2 {  padding: 4px 4px 4px 24px;  color: #F8F8F8;  background-color: #686868;  font-weight: bold;  font-size: 16px;}    </style>";
    private static final String END_HEAD                  = "</head>";
    private static final String START_TITLE               = "<title>";
    private static final String END_TITLE                 = "</title>";
    private static final String START_LINK                = "<link rel=";
    private static final String END_TAG                   = "/>";
    private static final String CLOSE_TAG                 = ">";
    private static final String START_BODY                = "<body>";
    private static final String END_BODY                  = "</body>";
    private static final String START_HEAD_1              = "<h1>";
    private static final String END_HEAD_1                = "</h1>";
    private static final String START_PARAGRAPH           = "<p>";
    private static final String END_PARAGRAPH             = "</p>";
    private static final String START_A_HREF              = "<a ";
    private static final String END_A_HREF                = "</a>";
    private static final String START_HEAD_2              = "<h2>";
    private static final String END_HEAD_2                = "</h2>";
    private static final String START_TABLE               = "<table>";
    private static final String END_TABLE                 = "</table>";
    private static final String START_T_BODY              = "<tbody>";
    private static final String END_T_BODY                = "</tbody>";
    private static final String START_TR                  = "<tr>";
    private static final String END_TR                    = "</tr>";
    private static final String START_TD                  = "<td>";
    private static final String END_TD                    = "</td>";
    private static final String TYPE                      = "type=";
    private static final String HREF                      = "href=";
    private static final String TITLE                     = "title=";
    private static final String SPACE                     = " ";
    private static final String APOSTROPHE                = "\"";
    private static final String CSS_REL                   = "stylesheet";
    private static final String OPEN_SEARCH_REL           = "search";

    /**
     * Sets the service document CSS path for HTML representation.
     * 
     * @param serviceDocumentCssPath the path to the service document's CSS file
     */
    public void setServiceDocumentCssPath(String serviceDocumentCssPath) {
        this.serviceDocumentCssPath = serviceDocumentCssPath;
    }

    /**
     * Gets the service document CSS path for HTML representation.
     * 
     * @return the path to the service document's CSS file
     */
    public String getServiceDocumentCssPath() {
        return serviceDocumentCssPath;
    }

    /**
     * This method returns the HTML view of the Service Document.
     * 
     * @return a complete HTML page
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getServiceDocumentHtml(@Context UriInfo uriInfo) {
        final String baseUri = uriInfo.getBaseUri().toString();
        final String titleValue = "Service Document"; // use
        // Accept-Langugage
        StringBuilder htmlDocStrBldr = new StringBuilder(START_HTML);

        // head
        htmlDocStrBldr.append(START_HEAD).append(START_TITLE).append(titleValue).append(END_TITLE);
        addCssLink(htmlDocStrBldr);
        addOpenSearchLinks(htmlDocStrBldr, baseUri);
        htmlDocStrBldr.append(END_HEAD);

        // body
        htmlDocStrBldr.append(START_BODY).append(START_HEAD_1).append(titleValue)
            .append(END_HEAD_1);

        // add a link to ATOM view - user can click on the link and view the
        // Service Document in
        // ATOM.
        String linkToAdd =
            uriInfo.getAbsolutePathBuilder().queryParam(RestConstants.REST_PARAM_MEDIA_TYPE,
                                                        APP_SERVICE_DOCUMENT_TYPE).build()
                .toString();
        htmlDocStrBldr.append(START_PARAGRAPH).append(START_A_HREF).append(HREF).append(APOSTROPHE)
            .append(linkToAdd).append(APOSTROPHE).append(CLOSE_TAG)
            .append("View Service Document in XML format").append(END_A_HREF).append(END_PARAGRAPH);

        // go over each workspace and print its collections
        Map<String, List<ServiceDocumentCollectionData>> workspaces =
            new LinkedHashMap<String, List<ServiceDocumentCollectionData>>();
        List<ServiceDocumentCollectionData> serviceDocumentCollectionList = getCollections(uriInfo);

        for (ServiceDocumentCollectionData collection : serviceDocumentCollectionList) {

            List<ServiceDocumentCollectionData> serviceDocumentCollections =
                workspaces.get(collection.getWorkspace());
            if (serviceDocumentCollections == null) {
                serviceDocumentCollections = new ArrayList<ServiceDocumentCollectionData>(1);
                workspaces.put(collection.getWorkspace(), serviceDocumentCollections);
            }
            serviceDocumentCollections.add(collection);
        }
        for (Map.Entry<String, List<ServiceDocumentCollectionData>> workspace : workspaces
            .entrySet()) {

            String workspaceTitleValue = workspace.getKey();
            List<ServiceDocumentCollectionData> collections = workspace.getValue();

            if (collections != null) {
                htmlDocStrBldr.append(START_HEAD_2).append(workspaceTitleValue).append(END_HEAD_2);

                // go over each collection and display its html URI (if exists),
                // if html URI doesn't
                // exist --> display its default URI.
                htmlDocStrBldr.append(START_TABLE).append(START_T_BODY);
                for (ServiceDocumentCollectionData collection : collections) {
                    String collectionName = collection.getTitle();
                    String template = collection.getUri();
                    String colUri = UriHelper.appendPathToBaseUri(baseUri, template);

                    // if the collection can be produced as HTML, add the
                    // ?alt="text/html" to the
                    // collection URI
                    if (collection.getProduces() != null && collection.getProduces()
                        .contains(MediaType.TEXT_HTML_TYPE)) {
                        colUri = UriHelper.appendAltToPath(colUri, MediaType.TEXT_HTML_TYPE);
                    }

                    // check if there is parameter on the path of the resource,
                    // if yes --> don't
                    // display it as a link
                    boolean isParameterOnUri = collectionHasTemplateHref(template);

                    htmlDocStrBldr.append(START_TR).append(START_TD).append(collectionName)
                        .append(":").append(END_TD);
                    htmlDocStrBldr.append(START_TD);
                    if (!isParameterOnUri) { // only if parameter isn't
                                             // included, display the path
                        // as link
                        htmlDocStrBldr.append("&nbsp;&nbsp;").append(START_A_HREF).append(HREF)
                            .append(APOSTROPHE).append(colUri).append(APOSTROPHE).append(CLOSE_TAG)
                            .append(UriEncoder.decodeString(colUri)).append(END_A_HREF);

                    } else {
                        htmlDocStrBldr.append("&nbsp;&nbsp;").append(UriEncoder
                            .decodeString(colUri));
                    }
                    htmlDocStrBldr.append(END_TD).append(END_TR);
                }
                htmlDocStrBldr.append(END_T_BODY).append(END_TABLE);
            }
        }

        htmlDocStrBldr.append(END_BODY).append(END_HTML);

        return htmlDocStrBldr.toString();
    }

    private boolean collectionHasTemplateHref(String collectionUri) {
        return collectionUri.indexOf('{') > 0;
    }

    private void addCssLink(StringBuilder htmlDocStrBldr) {
        if (serviceDocumentCssPath != null && !serviceDocumentCssPath.equals("")) {
            htmlDocStrBldr.append(START_LINK).append(APOSTROPHE).append(CSS_REL).append(APOSTROPHE)
                .append(SPACE).append(TYPE).append(APOSTROPHE).append(TEXT_CSS).append(APOSTROPHE)
                .append(SPACE).append(HREF).append(APOSTROPHE).append(serviceDocumentCssPath)
                .append(APOSTROPHE).append(END_TAG);
        } else {
            htmlDocStrBldr.append(CSS);
        }

    }

    /**
     * append links to OpenSearch descriptors. This will enable to add the open
     * search as a search engine in the browser.
     * 
     * @param msgContext
     * @param head append to this
     * @param baseUri base service uri
     */
    private void addOpenSearchLinks(StringBuilder htmlDocStrBldr, String baseUri) {
        ResourceRegistry registry =
            RuntimeContextTLS.getRuntimeContext().getAttribute(ResourceRegistry.class);

        for (ResourceRecord record : registry.getRecords()) {
            if (providesOpenSearch(record) && !collectionHasTemplateHref(record
                .getTemplateProcessor().getTemplate())) {

                htmlDocStrBldr.append(START_LINK).append(APOSTROPHE).append(OPEN_SEARCH_REL)
                    .append(APOSTROPHE).append(SPACE).append(TYPE).append(APOSTROPHE)
                    .append(MediaTypeUtils.OPENSEARCH).append(APOSTROPHE);

                String template = record.getTemplateProcessor().getTemplate();

                htmlDocStrBldr.append(SPACE).append(HREF).append(APOSTROPHE).append(UriHelper
                    .appendAltToPath(UriHelper.appendPathToBaseUri(baseUri, template),
                                     MediaTypeUtils.OPENSEARCH_TYPE)).append(APOSTROPHE);

                htmlDocStrBldr.append(SPACE).append(TITLE).append(APOSTROPHE).append(record
                    .getMetadata().getCollectionTitle()).append(SPACE).append("Search")
                    .append(APOSTROPHE).append(END_TAG);
            }
        }
    }

    private boolean providesOpenSearch(ResourceRecord record) {
        for (MethodMetadata method : record.getMetadata().getResourceMethods()) {
            if (providesOpenSearch(method)) {
                return true;
            }
        }
        for (MethodMetadata method : record.getMetadata().getSubResourceMethods()) {
            if (providesOpenSearch(method)) {
                return true;
            }
        }
        return false;
    }

    private boolean providesOpenSearch(MethodMetadata method) {
        if (method.getHttpMethod().equals(HttpMethod.GET) && method.getProduces()
            .equals(MediaTypeUtils.OPENSEARCH_TYPE)) {
            return true;
        }
        return false;
    }
}
