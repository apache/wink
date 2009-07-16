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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.categories.Categories;
import org.apache.wink.common.categories.CollectionCategories;
import org.apache.wink.common.internal.registry.metadata.ClassMetadata;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.internal.uritemplate.UriTemplateProcessor;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.internal.utils.UriHelper;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.ResourceRegistry;


public class ServiceDocumentCollectionData {
    
    private String workspace;

    private String title;

    private String uri;

    private Collection<MediaType> accepts;

    // List of Categories. Each Categories object contains list of CategoryBeans
    private List<Categories> categories;

    // saves the produced media types in which the collection can be retrieved
    private Set<MediaType> produces;

    private static final CollectionComparator COLLECTION_COMPARATOR = new CollectionComparator();

    /**
     * Creates new service document collection.
     * 
     * @param workspace
     *            the collection workspace title
     * @param title
     *            the collection title
     * @param uri
     *            the collection URI
     * @param accepts
     *            the collection accepting media types
     * @param categoriesUris
     *            the collection categories URIs
     * @param produces
     *            the collection produced media types
     */
    public ServiceDocumentCollectionData(String workspace, String title, String uri, Collection<MediaType> accepts,
            List<Categories> categories, Set<MediaType> produces) {

        this.workspace = workspace;
        this.title = title;
        this.uri = uri;
        this.accepts = accepts;
        this.categories = categories;
        this.produces = produces;
    }

    /**
     * Gets the collection workspace title.
     * 
     * @return the collection workspace title
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * Sets the collection workspace title.
     * 
     * @param workspace
     *            the collection workspace title
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     * Gets the collection title.
     * 
     * @return the collection title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the collection title.
     * 
     * @param title
     *            the collection title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the collection URI.
     * 
     * @return the collection URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the collection URI.
     * 
     * @param uri
     *            the collection URI
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Gets the collection accepting media types.
     * 
     * @return the collection accepting media types
     */
    public Collection<MediaType> getAccepts() {
        return accepts;
    }

    /**
     * Sets the collection accepting media types.
     * 
     * @param accepts
     *            the collection accepting media types
     */
    public void setAccepts(Collection<MediaType> accepts) {
        this.accepts = accepts;
    }

    /**
     * Gets list of categories
     * 
     * @return the list of categories
     */
    public List<Categories> getCategories() {
        return categories;
    }

    /**
     * Sets the list of categories
     * 
     * @param categories
     *            the list of categories
     */
    public void setCategories(List<Categories> categories) {
        this.categories = categories;
    }

    /**
     * Gets the produced media types of the collection (the media types in which the collection can
     * be retrieved).
     * 
     * @return Set<MediaType> the produced media types of the collection
     */
    public Set<MediaType> getProduces() {
        return produces;
    }

    /**
     * Sets the produced media types for the collection (the media types in which the collection can
     * be retrieved).
     * 
     * @param produces
     *            set of produced media types for the collection
     */
    public void setProduces(Set<MediaType> produces) {
        this.produces = produces;
    }

    public static List<ServiceDocumentCollectionData> buildServiceDocumentCollectionList(UriInfo uriInfo) {
        ResourceRegistry resourceRegistry = RuntimeContextTLS.getRuntimeContext().getAttribute(ResourceRegistry.class);
        List<ServiceDocumentCollectionData> collections = new ArrayList<ServiceDocumentCollectionData>();
        for (ResourceRecord record : resourceRegistry.getRecords()) {
            ClassMetadata metadata = record.getMetadata();

            // Check if the resource is a collection resource
            if (metadata.getWorkspaceName() != null) {

                // Get Categories that are supported by this collection resource
                List<Categories> collectionCategories = getCollectionCategories(record, resourceRegistry,
                        uriInfo);

                UriTemplateProcessor template = record.getTemplateProcessor();
                Set<MediaType> consumes = getCollectionConsumes(metadata);
                Set<MediaType> produces = getCollectionProduces(metadata);
                ServiceDocumentCollectionData sd = new ServiceDocumentCollectionData(metadata.getWorkspaceName(), metadata
                        .getCollectionTitle(), template.toString(), consumes, collectionCategories, produces);
                collections.add(sd);
            }
        }

        if (collections != null) {
            // Fix order to have stable service document
            Collections.sort(collections, COLLECTION_COMPARATOR);
        }

        return collections;
    }

    private static Set<MediaType> getCollectionConsumes(ClassMetadata metadata) {
        Set<MediaType> consumes = new HashSet<MediaType>();
        for (MethodMetadata method : metadata.getResourceMethods()) {
            if (method.getHttpMethod().equals(HttpMethod.POST)) {
                consumes.addAll(method.getConsumes());
            }
        }
        for (MethodMetadata method : metadata.getSubResourceMethods()) {
            if (method.getHttpMethod().equals(HttpMethod.POST)) {
                consumes.addAll(method.getConsumes());
            }
        }
        return consumes;
    }

    private static Set<MediaType> getCollectionProduces(ClassMetadata metadata) {
        Set<MediaType> produces = new HashSet<MediaType>();
        for (MethodMetadata method : metadata.getResourceMethods()) {
            produces.addAll(method.getProduces());
        }
        for (MethodMetadata method : metadata.getSubResourceMethods()) {
            produces.addAll(method.getProduces());
        }
        return produces;
    }

    /**
     * Get a list of Collection Categories
     * 
     * @param record
     *            ResourceRecord
     * @return List Collection Categories
     */
    private static List<Categories> getCollectionCategories(ResourceRecord record,
            ResourceRegistry resourceRegistry, UriInfo uriInfo) {

        List<Categories> collectionCategories = null;

        // Check if Resource exposes Categories (implements CollectionCategories)
        if (CollectionCategories.class.isAssignableFrom(record.getMetadata().getResourceClass())) {
            Object instance = record.getObjectFactory().getInstance(null);
            collectionCategories = ((CollectionCategories)instance).getCategories();
        }

        if (collectionCategories != null) {
            // Resolve Href of all OutOfline Categories provided by another Resource
            resolveOutOfLineCategoriesListHref(collectionCategories, resourceRegistry, uriInfo);
        }

        return collectionCategories;
    }

    /**
     * Resolve All Outofline Categories href
     */
    private static void resolveOutOfLineCategoriesListHref(List<Categories> collectionCategories,
            ResourceRegistry resourceRegistry, UriInfo uriInfo) {

        for (Categories categories : collectionCategories) {
            if (categories.isOutOfLine()) {
                resolveOutOfLineCategoriesHref(categories, resourceRegistry, uriInfo);
            }
        }
    }

    /**
     * Resolve Outofline Categories href
     */
    private static void resolveOutOfLineCategoriesHref(Categories categories, ResourceRegistry resourceRegistry,
            UriInfo uriInfo) {
        String categoriesDocUri = null;

        if (categories.getHref() != null) {
            // Nothing to do. Href was set explicitly.
            return;
        } else if (categories.getHandlingClass() != null) {
            ResourceRecord record = resourceRegistry.getRecord(categories.getHandlingClass());
            categoriesDocUri = getCategoriesDocBaseUri(categories, record);
        } else if (categories.getHandlingInstance() != null) {
            ResourceRecord record = resourceRegistry.getRecord(categories.getHandlingInstance());
            categoriesDocUri = getCategoriesDocBaseUri(categories, record);
        }

        // Update Categories href with resolved value
        if (categoriesDocUri != null) {
            categoriesDocUri = UriHelper.appendPathToBaseUri(uriInfo.getBaseUri().toString(),
                    categoriesDocUri);
            categoriesDocUri = UriHelper
                    .appendAltToPath(categoriesDocUri, MediaTypeUtils.ATOM_CATEGORIES_DOCUMENT_TYPE);
            categories.setHref(categoriesDocUri);
        }
    }

    /**
     * Get Categories Document base URI
     * 
     * @param <Categories>
     *            List of <CategoryBean>
     * @param <ResourceRecord>
     *            Resource record
     * @return String Categories Document base URI
     */
    private static String getCategoriesDocBaseUri(Categories cats, ResourceRecord record) {
        String categoriesDocBaseUri = null;
        MultivaluedMap<String,String> templateParams = cats.getTemplateParameters();
        UriTemplateProcessor template = record.getTemplateProcessor();
        categoriesDocBaseUri = template.expand(templateParams);
        return categoriesDocBaseUri;
    }

    /**
     * Order by workspace, then by title. Workspace value must be always non-null.
     */
    private static final class CollectionComparator implements Comparator<ServiceDocumentCollectionData> {

        public int compare(ServiceDocumentCollectionData o1, ServiceDocumentCollectionData o2) {
            int workspaceCompare = o1.getWorkspace().compareTo(o2.getWorkspace());
            if (workspaceCompare != 0) {
                return workspaceCompare;
            } else {
                String title1 = o1.getTitle();
                String title2 = o2.getTitle();
                if (title1 == null && title2 == null)
                    return 0;
                if (title1 == null)
                    return 1;
                if (title2 == null)
                    return -1;
                return title1.compareTo(title2);
            }
        }
    } // class CollectionComparator

}
