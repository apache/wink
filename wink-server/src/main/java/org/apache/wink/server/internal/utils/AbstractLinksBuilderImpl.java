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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.RestConstants;
import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.uritemplate.UriTemplateProcessor;
import org.apache.wink.common.internal.utils.UriHelper;
import org.apache.wink.common.model.synd.SyndLink;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.internal.handlers.SearchResult;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.ResourceRegistry;
import org.apache.wink.server.utils.BaseLinksBuilder;

/**
 * Base class providing common functionality for links builders.
 */
public abstract class AbstractLinksBuilderImpl<T> implements BaseLinksBuilder<T> {

    protected Map<String, String>            pathParams;
    protected MultivaluedMap<String, String> queryParams;
    protected ResourceRecord                 record;
    protected String                         resourcePath;
    protected String                         subResourcePath;
    protected URI                            baseUri;
    protected URI                            relativeTo;

    protected MessageContext                 context;
    protected ResourceRegistry               registry;
    protected boolean                        relativize;
    protected boolean                        addAltParam;

    protected AbstractLinksBuilderImpl(MessageContext context) {
        this.context = context;
        registry = context.getAttribute(ResourceRegistry.class);
        pathParams = new HashMap<String, String>();
        queryParams = new MultivaluedMapImpl<String, String>();
        record = context.getAttribute(SearchResult.class).getResource().getRecord();
        initPaths();
        initRelativize();
        initAddAltParam();
        baseUri(null);
        relativeTo = null;
    }

    private void initPaths() {
        UriInfo uriInfo = context.getUriInfo();
        resourcePath = null;
        subResourcePath = null;

        // we need to determine the path of the resource, and the path of the
        // sub-resource method
        // (if it exists)
        List<String> matchedURIs = uriInfo.getMatchedURIs(false);
        if (matchedURIs.size() == 1) {
            // if we have only one matched URI, it's a root resource without a
            // sub-resource method
            resourcePath = matchedURIs.get(0);
            subResourcePath = null;
        } else {
            // we have more than one matched URI. It means we went through at
            // least one sub-resource
            // (locator or method).
            // We compare the number of matched resources against the number of
            // matched URI's to
            // determine if the the invoked method is a resource method or a
            // sub-resource method.
            // If the number of matched URI's is the same as the number of
            // matched resources, then
            // we invoked a resource method.
            // If the number of matched URI's is greater than the number of
            // matched resources, then
            // we invoked a sub-resource method.
            List<Object> matchedResources = uriInfo.getMatchedResources();
            if (matchedURIs.size() == matchedResources.size()) {
                // the number of matched URI's is the same as the number of
                // matched resources - this
                // means we invoked a resource method
                resourcePath = matchedURIs.get(0);
                subResourcePath = null;
            } else {
                // the number of matched URI's is greater than the number of
                // matched resources -
                // this means we invoked a sub-resource method
                resourcePath = matchedURIs.get(1);
                subResourcePath = matchedURIs.get(0).substring(resourcePath.length() + 1); // +
                                                                                           // 1
                                                                                           // is
                                                                                           // to
                                                                                           // skip
                                                                                           // the
                                                                                           // '/'
            }
        }
    }

    private void initRelativize() {
        relativize =
            Boolean.parseBoolean(context.getProperties().getProperty("wink.defaultUrisRelative", //$NON-NLS-1$
                                                                     "true")); //$NON-NLS-1$
        String relative =
            context.getUriInfo().getQueryParameters(false)
                .getFirst(RestConstants.REST_PARAM_RELATIVE_URLS);
        if (relative != null) {
            relativize = Boolean.parseBoolean(relative);
        }
    }

    private void initAddAltParam() {
        addAltParam =
            Boolean.parseBoolean(context.getProperties().getProperty("wink.addAltParam", "true")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @SuppressWarnings("unchecked")
    public final T resource(Class<?> resource) {
        record = registry.getRecord(resource);
        resourcePath = record.getTemplateProcessor().getTemplate();
        subResourcePath = null;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public final T resource(Object resource) {
        record = registry.getRecord(resource);
        resourcePath = record.getTemplateProcessor().getTemplate();
        subResourcePath = null;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public final T subResource(String template) {
        subResourcePath = UriTemplateProcessor.normalizeUri(template);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public final T pathParam(String name, String value) {
        pathParams.put(name, value);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public final T queryParam(String name, String value) {
        queryParams.add(name, value);
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public final T baseUri(URI base) {
        baseUri = base;
        if (baseUri == null) {
            baseUri = context.getUriInfo().getBaseUri();
        }
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public final T relativeTo(URI relativeTo) {
        this.relativeTo = relativeTo;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public final T relativize(boolean relativize) {
        this.relativize = relativize;
        return (T)this;
    }

    @SuppressWarnings("unchecked")
    public final T addAltParam(boolean addAltParam) {
        this.addAltParam = addAltParam;
        return (T)this;
    }

    protected UriBuilder initUriBuilder() {
        return initUriBuilder(resourcePath);
    }

    protected UriBuilder initUriBuilder(String path) {
        UriBuilder builder = null;
        if (relativize) {
            builder = UriBuilder.fromPath(path);
        } else {
            builder = UriBuilder.fromUri(baseUri);
            // special treatment if the path resulting from the base uri equals
            // "/"
            if (baseUri.getPath() != null && baseUri.getPath().equals("/")) { //$NON-NLS-1$
                builder.replacePath(path);
            } else {
                builder.path(path);
            }
        }
        return builder;
    }

    protected SyndLink getLink(Collection<SyndLink> links, String relation) {
        for (SyndLink link : links) {
            if (relation.equals(link.getRel())) {
                return link;
            }
        }
        return null;
    }

    protected SyndLink createLink(String rel, MediaType type, URI href) {
        // convert the link to be absolute or relative
        String hrefStr = resolveLink(href);
        return new SyndLink(rel, (type != null ? type.toString() : null), hrefStr);
    }

    protected String resolveLink(URI link) {
        if (!relativize) {
            return link.toString();
        }

        String relative = null;
        if (relativeTo == null) {
            relative = context.getUriInfo().getPath(false);
        } else {
            URI relativeUri = baseUri.relativize(relativeTo);
            relative = relativeUri.toString();
        }
        return UriHelper.relativize(relative, link.toString());
    }

    public abstract List<SyndLink> build(List<SyndLink> out);

}
