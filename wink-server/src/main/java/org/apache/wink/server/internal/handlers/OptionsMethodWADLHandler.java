/*
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
 */
package org.apache.wink.server.internal.handlers;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.http.HttpHeadersEx;
import org.apache.wink.common.http.HttpMethodEx;
import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.common.internal.utils.HeaderUtils;
import org.apache.wink.common.model.wadl.Application;
import org.apache.wink.common.model.wadl.WADLGenerator;
import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.apache.wink.server.internal.handlers.SearchResult.MethodType;
import org.apache.wink.server.internal.registry.ResourceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptionsMethodWADLHandler implements RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(OptionsMethodWADLHandler.class);

    public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {

        // first thing - proceed the chain
        chain.doChain(context);

        // check the search result.
        // if the request is OPTIONS and no method was found, generate the
        // response automatically
        SearchResult searchResult = context.getAttribute(SearchResult.class);
        if (searchResult.isError() && searchResult.getError().getResponse().getStatus() == HttpStatus.METHOD_NOT_ALLOWED
            .getCode()
            && context.getHttpMethod().equalsIgnoreCase(HttpMethodEx.OPTIONS)) {
            // get supported HTTP methods
            ResourceRegistry resourceRegistry = context.getAttribute(ResourceRegistry.class);
            Set<Class<?>> c = new HashSet<Class<?>>();
            Class<?> resourceClass = searchResult.getResource().getResourceClass();
            c.add(resourceClass);
            logger.trace("Found the resource classes {}", c);
            Application app = new WADLHandlerWADLGenerator().generate(context.getUriInfo().getBaseUri().toString(), c);
            if(context.getUriInfo().getMatchedResources().size() > 1) {
                // sub-resource locator so lets correct the path
                app.getResources().get(0).getResource().get(0).setPath(context.getUriInfo().getMatchedURIs().get(0));
            }

            Set<String> httpMethods = resourceRegistry.getOptions(searchResult.getResource());

            logger
                .trace("Invoking OPTIONS request handled by runtime with {} resource and {} HTTP methods", //$NON-NLS-1$
                       searchResult.getResource(),
                       httpMethods);
            if (httpMethods.size() > 0) {
                String allowHeader = HeaderUtils.buildOptionsHeader(httpMethods);
                // add 'Allow' header to the response
                context.getAttribute(HttpServletResponse.class).addHeader(HttpHeadersEx.ALLOW,
                                                                          allowHeader);
                // remove an error from the search result
                context.getAttribute(SearchResult.class).setError(null);
                // set result to no-content
                context.setResponseStatusCode(Response.Status.OK.getStatusCode());
                context.setResponseEntity(Response.ok(app).type("application/vnd.sun.wadl+xml")
                    .build());
            }
        }
    }
    
    public void init(Properties props) {
    }

    private class WADLHandlerWADLGenerator extends WADLGenerator {
        
        /*
         * Assume we know what we're doing internally
         */
        @Override
        protected boolean isResource(Class<?> cls) {
            return true;
        }
    }
}
