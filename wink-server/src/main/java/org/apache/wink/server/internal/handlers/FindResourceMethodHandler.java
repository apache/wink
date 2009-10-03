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

package org.apache.wink.server.internal.handlers;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.common.internal.uritemplate.UriTemplateMatcher;
import org.apache.wink.common.internal.uritemplate.UriTemplateProcessor;
import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.apache.wink.server.internal.contexts.UriInfoImpl;
import org.apache.wink.server.internal.handlers.SearchResult.AccumulatedData;
import org.apache.wink.server.internal.registry.MethodRecord;
import org.apache.wink.server.internal.registry.ResourceInstance;
import org.apache.wink.server.internal.registry.ResourceRecord;
import org.apache.wink.server.internal.registry.ResourceRegistry;
import org.apache.wink.server.internal.registry.SubResourceInstance;
import org.apache.wink.server.internal.registry.SubResourceMethodRecord;
import org.apache.wink.server.internal.registry.SubResourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindResourceMethodHandler implements RequestHandler {

    private boolean             isContinuedSearchPolicy;

    private static final Logger logger = LoggerFactory.getLogger(FindResourceMethodHandler.class);

    public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {

        SearchResult result = context.getAttribute(SearchResult.class);
        ResourceInstance resource = result.getResource();

        // resource method
        if (resource.isExactMatch()) {
            logger.debug("Root resource @Path matches exactly so finding root resource method");
            handleResourceMethod(context, chain);
            return;
        }

        // sub-resource method or locator
        UriTemplateMatcher templateMatcher = resource.getMatcher();
        String tail = UriTemplateProcessor.normalizeUri(templateMatcher.getTail(false));
        logger.debug("Unmatched tail to the URI: {}", tail);

        // get a sorted list of all the sub-resources (methods and locators)
        List<SubResourceInstance> subResources = resource.getRecord().getMatchingSubResources(tail);
        logger.debug("Possible subresources found: {}", subResources);
        if (subResources.size() == 0) {
            result.setError(new WebApplicationException(Response.Status.NOT_FOUND));
            return;
        }

        // get all the searchable sub-resources
        List<SubResourceInstance> searchableSubResources = getSearchableSubResources(subResources);
        logger.debug("Possible searchable subresources found: {}", searchableSubResources);
        // save the current data in case we need to role back the information if
        // the search fails and we will need to continue to the next
        // sub-resource
        // (for continued search mode only)
        AccumulatedData originalData = result.getData();

        // iterate through all sub-resources until a match is found.
        // JAX-RS compliance requires to look only at the first sub-resource -
        // this will be the case
        // unless the search policy is specifically set to "continued search"
        for (SubResourceInstance subResourceInstance : searchableSubResources) {
            SubResourceRecord subResourceRecord = subResourceInstance.getRecord();

            // set a clone of the accumulated data before continuing to the next
            // sub-resource
            result.setData(originalData.clone());

            // handle the sub-resource
            if (subResourceRecord instanceof SubResourceMethodRecord) {
                handleSubResourceMethod(subResourceInstance, subResources, context, chain);
                if (result.isFound()) {
                    return;
                }
            } else {
                handleSubResourceLocator(subResourceInstance, subResources, context, chain);
                // check the result of the recursive call
                if (result.isFound()) {
                    return;
                }
            }
        }
    }

    private void handleResourceMethod(MessageContext context, HandlersChain chain) throws Throwable {
        // if the resource is an exact match to the uri, then this is the
        // handling resource,
        // and we need to find the dispatch method.
        // if no method is found then a RequestMatchingException exception is
        // thrown
        ResourceRegistry registry = context.getAttribute(ResourceRegistry.class);
        SearchResult result = context.getAttribute(SearchResult.class);
        ResourceInstance resource = result.getResource();

        MethodRecord method = null;
        try {
            // if no method is found then a RequestMatchingException exception
            // is thrown
            method = registry.findMethod(resource, context);
        } catch (WebApplicationException e) {
            // couldn't find a method
            result.setError(e);
            return;
        }
        result.setFound(true);
        result.setMethod(method);
        // continue the chain to invoke the method
        if (logger.isDebugEnabled()) {
            MethodMetadata metadata = (method == null) ? null : method.getMetadata();
            logger.debug("Found root resource method to invoke: {} ", metadata);
        }
        chain.doChain(context);
    }

    private void handleSubResourceMethod(SubResourceInstance subResourceInstance,
                                         List<SubResourceInstance> subResources,
                                         MessageContext context,
                                         HandlersChain chain) throws Throwable {
        ResourceRegistry registry = context.getAttribute(ResourceRegistry.class);
        SearchResult result = context.getAttribute(SearchResult.class);
        ResourceInstance resource = result.getResource();
        SubResourceRecord subResourceRecord = subResourceInstance.getRecord();
        UriTemplateMatcher matcher = subResourceInstance.getMatcher();
        String pattern = subResourceRecord.getTemplateProcessor().getPatternString();
        // dispatch to one of the sub-resource methods.
        SubResourceInstance method = null;
        try {
            // if no method is found then a RequestMatchingException exception
            // is thrown
            method = registry.findSubResourceMethod(pattern, subResources, resource, context);
        } catch (WebApplicationException e) {
            // couldn't find a method
            result.setError(e);
            return;
        }

        saveFoundMethod(result, matcher, method, context);

        // continue the chain to invoke the method
        if (logger.isDebugEnabled()) {
            MethodMetadata metadata = (method == null) ? null : method.getMetadata();
            logger.debug("Found subresource method to invoke: {} ", metadata);
        }
        chain.doChain(context);
    }

    private void handleSubResourceLocator(SubResourceInstance subResourceInstance,
                                          List<SubResourceInstance> subResources,
                                          MessageContext context,
                                          HandlersChain chain) throws Throwable {
        ResourceRegistry registry = context.getAttribute(ResourceRegistry.class);
        SearchResult result = context.getAttribute(SearchResult.class);
        UriTemplateMatcher matcher = subResourceInstance.getMatcher();

        // // dispatch to the sub-resource locator.
        // result.setFound(true);
        // result.setMethod(subResourceInstance);
        // // save the matched template variables for UriInfo
        // matcher.storeVariables(result.getData().getMatchedVariables(),
        // false);
        // // save the matched uri for UriInfo
        // result.getData().addMatchedUri(matcher.getHead(false));
        saveFoundMethod(result, matcher, subResourceInstance, context);

        // continue the chain to invoke the locator
        if (logger.isDebugEnabled()) {
            MethodMetadata metadata =
                (subResourceInstance == null) ? null : subResourceInstance.getMetadata();
            logger.debug("Found subresource locator to invoke: {} ", metadata);
        }
        chain.doChain(context);

        // the object returned from the locator is a sub-resource so we must
        // continue the search in it
        Object subResource = context.getResponseEntity();
        if (subResource == null) {
            logger.debug("Subresource returned was null so returning a 404 Not Found");
            result.setError(new WebApplicationException(Status.NOT_FOUND));
            return;
        }
        ResourceRecord record = registry.getRecord(subResource, false);
        ResourceInstance resourceInstance = new ResourceInstance(subResource, record, matcher);
        // save the resource for UriInfo
        result.getData().getMatchedResources().addFirst(resourceInstance);

        // call recursively to search in the sub-resource
        result.setFound(false);
        logger
            .debug("Re-invoking the chain (due to hitting a subresource locator method) with the new subresource instance {}",
                   resourceInstance);
        handleRequest(context, chain);
    }

    private List<SubResourceInstance> getSearchableSubResources(List<SubResourceInstance> subResources) {
        // JAX-RS specification requires that if the first matching sub-resource
        // is a method,
        // then we must dispatch to one of the sub-resource methods, otherwise
        // we should invoke the sub-resource locator.
        // but the continued search behavior is to continue searching in all
        // matching
        // sub-resources
        List<SubResourceInstance> searchableSubResources = new LinkedList<SubResourceInstance>();
        if (!isContinuedSearchPolicy) {
            // strict behavior - look only at the first method
            searchableSubResources.add(subResources.iterator().next());
        } else {
            // continued search behavior - search through all sub-resources
            searchableSubResources.addAll(subResources);
        }
        return searchableSubResources;
    }

    private void saveFoundMethod(SearchResult result,
                                 UriTemplateMatcher matcher,
                                 SubResourceInstance method,
                                 MessageContext context) {

        result.setFound(true);
        result.setMethod(method);

        // save the matched template variables for UriInfo
        matcher.storeVariables(result.getData().getMatchedVariables(), false);

        // save the path segments of the matched path variables.
        // the matched "head" is added to the "matched uri's" list to reflect
        // the
        // most recent match. the difference in the number of segments between
        // the uri of the
        // previous match and the uri of the current match reflects the number
        // of segments that
        // the head of the current match contains.
        // this is done in this way (instead of just converting the "head" into
        // path segments)
        // because we want to save the path segments with the matrix parameters,
        // and the "head" matched
        // part was matched without the matrix parameters, but the
        // "matched uri's" list saves the
        // path segments with the matrix parameters.

        // 1. get the number of segments that were matched up until the current
        // match. this will be used as
        // the offset into the full path segments list
        int offset = result.getData().getMatchedURIs().getFirst().size();
        // 2. save the current matched uri - it is added as the first uri in the
        // list of matched uri's
        int headSegmentsCount = result.getData().addMatchedURI(matcher.getHead(false));
        List<PathSegment> segments = context.getUriInfo().getPathSegments(false);
        // 3. save the path segments of the matched variables
        matcher.storeVariablesPathSegments(segments, offset, headSegmentsCount, result.getData()
            .getMatchedVariablesPathSegments());

        // for sub resources with annotated method parameters, we need to reload
        // path parameters so that they are injected when invoked
        UriInfoImpl uriInfoImpl = context.getAttribute(UriInfoImpl.class);
        if (uriInfoImpl != null && matcher.getVariables().size() > 0)
            uriInfoImpl.resetPathParameters();
    }

    public void init(Properties props) {
        String property =
            props.getProperty(FindRootResourceHandler.SEARCH_POLICY_CONTINUED_SEARCH_KEY);
        isContinuedSearchPolicy = Boolean.valueOf(property);
    }

}
