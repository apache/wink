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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.categories.Categories;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.internal.utils.UriHelper;
import org.apache.wink.common.model.app.AppAccept;
import org.apache.wink.common.model.app.AppCategories;
import org.apache.wink.common.model.app.AppCollection;
import org.apache.wink.common.model.app.AppService;
import org.apache.wink.common.model.app.AppWorkspace;
import org.apache.wink.common.model.app.AppYesNo;
import org.apache.wink.common.model.atom.AtomCategory;
import org.apache.wink.common.model.atom.AtomText;

/**
 * Base class for an Resource handling the root of the REST-style web service
 * namespace. It provides APP service document using introspection of registered
 * collection resources.
 */
@Path("/")
public class RootResource {

    /**
     * Returns the service document.
     * 
     * @return the service document bean
     */
    @GET
    @Produces(MediaTypeUtils.ATOM_SERVICE_DOCUMENT)
    public AppService getServiceDocument(@Context UriInfo uriInfo) {

        List<ServiceDocumentCollectionData> serviceDocument = getCollections(uriInfo);
        return buildAppService(serviceDocument, uriInfo);
    }

    /**
     * Gets all service document collections.
     * 
     * @return the service document collections
     */
    public List<ServiceDocumentCollectionData> getCollections(UriInfo uriInfo) {
        List<ServiceDocumentCollectionData> buildServiceDocumentCollectionList =
            ServiceDocumentCollectionData.buildServiceDocumentCollectionList(uriInfo);
        return buildServiceDocumentCollectionList;
    }

    private AppService buildAppService(List<ServiceDocumentCollectionData> collectionList,
                                       UriInfo uriInfo) {
        if (collectionList == null) {
            // this should never happen
            throw new NullPointerException(Messages.getMessage("rootResourceCollectionListIsNull"));
        }

        // Map of all APP workspaces
        Map<String, AppWorkspace> workspaces = new LinkedHashMap<String, AppWorkspace>();

        // Create APP Service Document
        AppService appService = new AppService();

        // Get list of Workspaces
        List<AppWorkspace> workspaceList = appService.getWorkspace();

        // Loop over the list of Service Document Collections, and add each
        // to relevant Workspace
        for (ServiceDocumentCollectionData serviceDocumentCollection : collectionList) {

            // get workspace
            String workspaceName = serviceDocumentCollection.getWorkspace();
            AppWorkspace workspace = workspaces.get(workspaceName);

            if (workspace == null) {

                // Create new workspace
                workspace = new AppWorkspace();
                AtomText workspaceTitle = new AtomText();

                // Set workspace title
                workspaceTitle.setValue(workspaceName);
                workspace.setTitle(workspaceTitle);
                workspaces.put(workspaceName, workspace);

                // Add workspace to list of workspaces
                workspaceList.add(workspace);
            }

            // Add collection data to workspace
            String baseUri = uriInfo.getBaseUri().toString();
            addCollection(workspace, serviceDocumentCollection, baseUri);

        }

        return appService;
    }

    /**
     * Add Collection to input AppWorkspace
     * 
     * @param workspace App Workspace
     * @param serviceDocumentCollection Service Document Collection
     */
    private void addCollection(AppWorkspace workspace,
                               ServiceDocumentCollectionData serviceDocumentCollection,
                               String baseUri) {

        // new collection
        AppCollection collection = new AppCollection();

        List<AppCollection> workspaceCollectionList = workspace.getCollection();

        // Set URI
        String href = UriHelper.appendPathToBaseUri(baseUri, serviceDocumentCollection.getUri());
        collection.setHref(href);

        // Set Title
        AtomText title = new AtomText();
        // title.setType();
        title.setValue(serviceDocumentCollection.getTitle());
        collection.setTitle(title);

        // Add Accept Media Types
        List<AppAccept> acceptMediaTypes = collection.getAccept();
        if (serviceDocumentCollection.getAccepts().isEmpty()) {
            AppAccept appAccept = new AppAccept();
            acceptMediaTypes.add(appAccept);
        } else {
            for (MediaType acceptMediaType : serviceDocumentCollection.getAccepts()) {
                AppAccept appAccept = new AppAccept();
                String appAcceptString = acceptMediaType.toString();
                appAccept.setValue(appAcceptString);
                acceptMediaTypes.add(appAccept);
            }
        }

        // Add Inline and OutOfline categories
        addCategories(serviceDocumentCollection, collection);
        // Add collection to Workspace Collection List
        workspaceCollectionList.add(collection);
    }

    /**
     * Add Inline and OutOfline categories
     * 
     * @param serviceDocumentCollection Service Document Collection
     * @param collection App Collection
     */
    private void addCategories(ServiceDocumentCollectionData serviceDocumentCollection,
                               AppCollection collection) {

        List<AppCategories> appCategories = collection.getCategories();
        List<Categories> categories = serviceDocumentCollection.getCategories();

        if (categories == null) {
            return;
        }

        // Add all categories to ServiceDocument
        for (Categories cats : categories) {
            AppCategories appCategory = new AppCategories();
            if (cats.isOutOfLine()) {
                String href = cats.getHref();
                appCategory.setHref(href);
            } else {
                addInlineCategories(cats, appCategory);
            }
            appCategories.add(appCategory);
        }
    }

    /**
     * Add Inline Categories to Service Document Collection
     * 
     * @param cats Categories
     * @param collection App Categories
     */
    private void addInlineCategories(Categories cats, AppCategories appCategory) {
        // XML Categories document

        List<AtomCategory> xmlCategoryList = appCategory.getCategory();

        // Categories to serialize
        List<AtomCategory> categoryBeans = cats.getCategories();

        // Loop over a list of Category Beans and add them to Service Document
        // collection
        for (AtomCategory cat : categoryBeans) {
            AtomCategory xmlCategory = new AtomCategory();

            String catLabel = cat.getLabel();
            if (catLabel != null) {
                xmlCategory.setLabel(catLabel);
            }

            String catScheme = cat.getScheme();
            if (catScheme != null) {
                xmlCategory.setScheme(catScheme);
            }

            String catTerm = cat.getTerm();
            if (catTerm != null) {
                xmlCategory.setTerm(catTerm);
            }
            xmlCategoryList.add(xmlCategory);
        }

        String catsScheme = cats.getScheme();
        if (catsScheme != null) {
            appCategory.setScheme(catsScheme);
        }

        boolean fixed = cats.isFixed();
        if (fixed == true) {
            appCategory.setFixed(AppYesNo.YES);
        }

    }
}
