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

import java.util.List;
import java.util.Properties;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.apache.wink.server.internal.registry.ResourceInstance;
import org.apache.wink.server.internal.registry.ResourceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindRootResourceHandler implements RequestHandler {

    public static final String  SEARCH_POLICY_CONTINUED_SEARCH_KEY =
                                                                       "wink.searchPolicyContinuedSearch";           //$NON-NLS-1$
    private static final Logger logger                             =
                                                                       LoggerFactory
                                                                           .getLogger(FindRootResourceHandler.class);

    private boolean             isContinuedSearchPolicy;

    public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
        ResourceRegistry registry = context.getAttribute(ResourceRegistry.class);

        // create a path stripped from all matrix parameters to use for matching
        List<PathSegment> segments = context.getUriInfo().getPathSegments(false);
        logger.debug("Getting URI Info path segments: {}", segments); //$NON-NLS-1$
        String strippedPath = buildPathForMatching(segments);
        logger.debug("Getting stripped path from segments: {}", strippedPath); //$NON-NLS-1$
        // get a list of root resources that can handle the request

        // JAX-RS specification requires to search only the first matching
        // resource,
        // but the continued search behavior is to continue searching in all
        // matching resources
        List<ResourceInstance> matchedResources =
            registry.getMatchingRootResources(strippedPath, isContinuedSearchPolicy);
        logger.debug("Found resource instances: {}", matchedResources); //$NON-NLS-1$
        if (matchedResources.size() == 0) {
            logger.debug("No resource found matching {}", context.getUriInfo().getPath(false)); //$NON-NLS-1$
            SearchResult result =
                new SearchResult(new WebApplicationException(Response.Status.NOT_FOUND));
            context.setAttribute(SearchResult.class, result);
            return;
        }

        // search through all the matched resources (or just the first one)
        for (ResourceInstance resource : matchedResources) {
            // save the matched variables, resource and uri
            SearchResult result = new SearchResult(resource, context.getUriInfo());
            context.setAttribute(SearchResult.class, result);
            resource.getMatcher().storeVariables(result.getData().getMatchedVariables(), false);
            int headSegmentsCount =
                result.getData().addMatchedURI(resource.getMatcher().getHead(false));
            resource.getMatcher()
                .storeVariablesPathSegments(segments,
                                            0,
                                            headSegmentsCount,
                                            result.getData().getMatchedVariablesPathSegments());

            logger.debug("Using SearchResult: {}", result); //$NON-NLS-1$

            // continue that chain to find the actual resource that will handle
            // the request.
            // it may be the current resource or a sub-resource of the current
            // resource.
            chain.doChain(context);

            // check the result to see if we found a match
            if (result.isFound()) {
                break;
            }

            // if the search result was unsuccessful, should automatically do
            // the release on any root resources created (and any subresource
            // instances used; the subresource is dead)
            List<ResourceInstance> resourceInstances = result.getData().getMatchedResources();
            for (ResourceInstance res : resourceInstances) {
                logger.debug("Releasing resource instance"); //$NON-NLS-1$
                res.releaseInstance(context);
            }
        }
    }

    private String buildPathForMatching(List<PathSegment> segments) {
        StringBuilder strippedPathBuilder = new StringBuilder();
        String delim = ""; //$NON-NLS-1$
        for (PathSegment segment : segments) {
            strippedPathBuilder.append(delim);
            strippedPathBuilder.append(segment.getPath());
            delim = "/"; //$NON-NLS-1$
        }
        String strippedPath = strippedPathBuilder.toString();
        return strippedPath;
    }

    public void init(Properties props) {
        isContinuedSearchPolicy =
            Boolean.valueOf(props.getProperty(SEARCH_POLICY_CONTINUED_SEARCH_KEY));
    }

}
