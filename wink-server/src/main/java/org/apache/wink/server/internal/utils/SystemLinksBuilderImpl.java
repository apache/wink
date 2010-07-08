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

package org.apache.wink.server.internal.utils;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.wink.common.RestConstants;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.atom.AtomConstants;
import org.apache.wink.common.model.synd.SyndLink;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.internal.handlers.FindRootResourceHandler;
import org.apache.wink.server.internal.registry.ResourceInstance;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.SubResourceInstance;
import org.apache.wink.server.utils.SystemLinksBuilder;

public class SystemLinksBuilderImpl extends AbstractLinksBuilderImpl<SystemLinksBuilder> implements
    SystemLinksBuilder {

    private LinkType[] types;
    private boolean    allResources;

    public SystemLinksBuilderImpl(MessageContext context) {
        super(context);
        types = null;
        allResources = isContinuedSearchMode();
    }

    protected boolean isContinuedSearchMode() {
        return Boolean.valueOf(context.getProperties()
            .getProperty(FindRootResourceHandler.SEARCH_POLICY_CONTINUED_SEARCH_KEY, "false")); //$NON-NLS-1$
    }

    public SystemLinksBuilder types(LinkType... types) {
        this.types = types;
        return this;
    }

    public SystemLinksBuilder allResources(boolean all) {
        allResources = all;
        return this;
    }

    @Override
    public List<SyndLink> build(List<SyndLink> out) {

        Set<SyndLink> set = new HashSet<SyndLink>();

        // if the search mode is "Continued Search" we need to generate links
        // for all
        // of the matching root resources and not just for the current one
        if (allResources) {
            // get the path to use for obtaining the matching root resources
            URI uri = UriBuilder.fromPath(resourcePath).buildFromEncodedMap(pathParams);
            String path = uri.toString();
            List<ResourceInstance> rootResources = registry.getMatchingRootResources(path);
            // go over all the root matching resources and generate links for
            // them
            for (ResourceInstance rootResource : rootResources) {
                UriBuilder uriBuilder = initUriBuilder(path);
                ResourceRecord record = rootResource.getRecord();
                build(set, uriBuilder, record);
            }
        } else {
            // generate links just for this one resource
            UriBuilder uriBuilder = initUriBuilder();
            build(set, uriBuilder, record);
        }

        if (out == null) {
            out = new LinkedList<SyndLink>();
        }

        out.addAll(set);

        return out;
    }

    private Set<SyndLink> build(Set<SyndLink> set, UriBuilder selfUriBuilder, ResourceRecord record) {
        List<MethodMetadata> methods = null;
        if (subResourcePath != null && subResourcePath.length() > 0) {
            // 1) find all the sub-resources that match the sub-resource path
            List<SubResourceInstance> subResources =
                record.getMatchingSubResourceMethods(subResourcePath);
            methods = new LinkedList<MethodMetadata>();
            for (SubResourceInstance sub : subResources) {
                methods.add(sub.getMetadata());
            }
            // 2) add the sub-resource path to the uri builder
            selfUriBuilder.path(subResourcePath);
        } else {
            methods = record.getMetadata().getResourceMethods();
        }
        return build(set, selfUriBuilder, methods);
    }

    private Set<SyndLink> build(Set<SyndLink> set,
                                UriBuilder selfUriBuilder,
                                List<MethodMetadata> methods) {
        // add all query parameters to the self uri
        for (String query : queryParams.keySet()) {
            selfUriBuilder.queryParam(query, queryParams.get(query).toArray());
        }

        // get a set of the required links to generate
        Set<LinkType> systemLinksToGenerate = generateSystemLinksSet();

        // self link; should not be created if wasn't requested.
        // self link; not replace it if some already exists
        URI selfUri = selfUriBuilder.buildFromEncodedMap(pathParams);
        if (systemLinksToGenerate.contains(LinkType.SELF) && getLink(set,
                                                                     AtomConstants.ATOM_REL_SELF) == null) {
            set.add(createLink(AtomConstants.ATOM_REL_SELF, null, selfUri));
        }

        // edit link; should not be created if wasn't requested.
        // edit link if the Resource has an update operation; does not replace
        // it if it already
        // exists
        if (systemLinksToGenerate.contains(LinkType.EDIT) && getLink(set,
                                                                     AtomConstants.ATOM_REL_EDIT) == null) {
            for (MethodMetadata methodRecord : methods) {
                String httpMethod = methodRecord.getHttpMethod();
                if (httpMethod.equalsIgnoreCase(HttpMethod.PUT) || httpMethod
                    .equalsIgnoreCase(HttpMethod.DELETE)) {
                    set.add(createLink(AtomConstants.ATOM_REL_EDIT, null, selfUri));
                    break;
                }
            }
        }

        // alternate links - all GET operations
        Set<MediaType> producedTypesForGet = getProducedTypesForGet(methods);
        for (MediaType mediaType : producedTypesForGet) {
            if (addAltParam && queryParams.get(RestConstants.REST_PARAM_MEDIA_TYPE) == null) {
                selfUriBuilder.replaceQueryParam(RestConstants.REST_PARAM_MEDIA_TYPE,
                                                 MediaTypeUtils.toEncodedString(mediaType));
            }
            URI href = selfUriBuilder.buildFromEncodedMap(pathParams);
            if (MediaTypeUtils.equalsIgnoreParameters(mediaType, MediaTypeUtils.OPENSEARCH_TYPE)) {
                if (systemLinksToGenerate.contains(LinkType.OPENSEARCH)) {
                    // open search link; should not be created if wasn't
                    // requested.
                    set.add(createLink(AtomConstants.ATOM_REL_SEARCH, mediaType, href));
                }
            } else if (systemLinksToGenerate.contains(LinkType.ALTERNATE)) {
                set.add(createLink(AtomConstants.ATOM_REL_ALT, mediaType, href));
            }
        }
        return set;
    }

    private static Set<MediaType> getProducedTypesForGet(List<MethodMetadata> methods) {
        Set<MediaType> getResponses = new LinkedHashSet<MediaType>();
        if (methods != null) {
            for (MethodMetadata methodRecord : methods) {
                if (methodRecord.getHttpMethod().equals(HttpMethod.GET)) {
                    getResponses.addAll(methodRecord.getProduces());
                }
            }
        }
        return getResponses;
    }

    private Set<LinkType> generateSystemLinksSet() {
        Set<LinkType> systemLinksSet = new HashSet<LinkType>();
        if (types == null || types.length == 0) {
            for (LinkType type : LinkType.values()) {
                systemLinksSet.add(type);
            }
        } else {
            for (LinkType type : types) {
                systemLinksSet.add(type);
            }
        }
        return systemLinksSet;
    }

}
