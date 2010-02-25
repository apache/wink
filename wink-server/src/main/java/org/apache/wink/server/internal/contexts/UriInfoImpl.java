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

package org.apache.wink.server.internal.contexts;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.PathSegmentImpl;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.internal.uri.UriEncoder;
import org.apache.wink.common.internal.utils.UriHelper;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.internal.handlers.SearchResult;
import org.apache.wink.server.internal.registry.ResourceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UriInfoImpl implements UriInfo {

    private static final Logger            logger = LoggerFactory.getLogger(UriInfoImpl.class);

    private MessageContext                 messageContext;

    private URI                            absolutePath;
    private URI                            baseUri;
    private String                         baseUriString;
    private String                         path;

    private MultivaluedMap<String, String> pathParameters;
    private MultivaluedMap<String, String> decodedPathParameters;

    private MultivaluedMap<String, String> queryParameters;
    private MultivaluedMap<String, String> decodedQueryParameters;

    private List<PathSegment>              pathSegments;
    private List<PathSegment>              decodedPathSegments;

    private List<String>                   matchedURIsStrings;
    private List<String>                   decodedMatchedURIsStrings;

    public UriInfoImpl(MessageContext msgContext) {
        messageContext = msgContext;
        absolutePath = null;
        baseUri = null;
        baseUriString = null;
        path = null;
        pathParameters = null;
        pathSegments = null;
        decodedPathSegments = null;
        matchedURIsStrings = null;
        decodedMatchedURIsStrings = null;
    }

    public URI getAbsolutePath() {
        if (absolutePath == null) {
            String requestPath = getPath(false);
            absolutePath = getBaseUri().resolve(requestPath);
        }
        logger.debug("getAbsolutePath() returning: {}", absolutePath);
        return absolutePath;
    }

    public UriBuilder getAbsolutePathBuilder() {
        UriBuilder builder = UriBuilder.fromUri(getAbsolutePath());
        logger.debug("getAbsolutePathBuilder() returning: {}", builder);
        return builder;
    }

    public URI getBaseUri() {
        if (baseUri == null) {
            String baseUriString = getBaseUriString();
            try {
                baseUri = new URI(baseUriString);
            } catch (URISyntaxException e) {
                logger.error(Messages.getMessage("uriBadBaseURI") + baseUriString, e);
            }
        }
        logger.debug("getBaseUri() returning: {}", baseUri);
        return baseUri;
    }

    public UriBuilder getBaseUriBuilder() {
        UriBuilder builder = UriBuilder.fromUri(getBaseUri());
        logger.debug("getBaseUriBuilder() returning: {}", builder);
        return builder;
    }

    public List<ResourceInstance> getMatchedResourceInstances() {
        List<ResourceInstance> resources =
            Collections.unmodifiableList(messageContext.getAttribute(SearchResult.class).getData()
                .getMatchedResources());
        logger.debug("getMatchedResourceInstances() returning: {}", resources);
        return resources;
    }

    public List<Object> getMatchedResources() {
        List<ResourceInstance> matchedResources =
            messageContext.getAttribute(SearchResult.class).getData().getMatchedResources();
        List<Object> resourceList = new ArrayList<Object>(matchedResources.size());
        for (ResourceInstance resourceInstance : matchedResources) {
            resourceList.add(resourceInstance.getInstance(messageContext));
        }
        logger.debug("getMatchedResources() returning: {}", resourceList);
        return Collections.unmodifiableList(resourceList);
    }

    public List<String> getMatchedURIs() {
        return getMatchedURIs(true);
    }

    public List<String> getMatchedURIs(boolean decode) {
        logger.debug("getMatchedURIs({}) called", decode);
        List<List<PathSegment>> matchedURIs =
            messageContext.getAttribute(SearchResult.class).getData().getMatchedURIs();
        if (matchedURIsStrings != null && matchedURIsStrings.size() != matchedURIs.size()) {
            matchedURIsStrings = null;
            decodedMatchedURIsStrings = null;
        }

        if (matchedURIsStrings == null) {
            matchedURIsStrings = new ArrayList<String>(matchedURIs.size());
            for (List<PathSegment> segments : matchedURIs) {
                logger.debug("Adding matched URI: {}", segments);
                matchedURIsStrings.add(PathSegmentImpl.toString(segments));
            }
        }

        List<String> list = matchedURIsStrings;
        if (decode) {
            if (decodedMatchedURIsStrings == null) {
                decodedMatchedURIsStrings = new ArrayList<String>(matchedURIsStrings.size());
                for (String uri : matchedURIsStrings) {
                    String decodedUri = UriEncoder.decodeString(uri);
                    decodedMatchedURIsStrings.add(decodedUri);
                    logger.debug("Adding decoded URI: {} from URI: {}", decodedUri, uri);
                }
            }
            list = decodedMatchedURIsStrings;
        }
        logger.debug("getMatchedURIs({}) returning {}", decode, list);
        return Collections.unmodifiableList(list);
    }

    public String getPath() {
        return getPath(true);
    }

    public String getPath(boolean decode) {
        logger.debug("getPath({}) called", decode);
        if (path == null) {
            path = buildRequestPath(messageContext.getAttribute(HttpServletRequest.class));
        }

        if (decode) {
            String decodedPath = UriEncoder.decodeString(path);
            logger.debug("getPath({}) returning {}", decode, decodedPath);
            return decodedPath;
        }
        logger.debug("getPath({}) returning {}", decode, path);
        return path;
    }

    public MultivaluedMap<String, String> getPathParameters() {
        return getPathParameters(true);
    }

    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        logger.debug("getPathParameters({}) called", decode);
        if (pathParameters == null) {
            pathParameters = new MultivaluedMapImpl<String, String>();
            SearchResult searchResult = messageContext.getAttribute(SearchResult.class);
            if (searchResult == null) {
                throw new IllegalStateException(Messages
                    .getMessage("methodCallOutsideScopeOfRequestContext"));
            }
            MultivaluedMapImpl.copy(searchResult.getData().getMatchedVariables(), pathParameters);
            logger.debug("getPathParameters({}) encoded path parameters are: {}",
                         decode,
                         pathParameters);
        }

        MultivaluedMap<String, String> map = pathParameters;
        if (decode) {
            if (decodedPathParameters == null) {
                decodedPathParameters = UriEncoder.decodeMultivaluedMapValues(pathParameters);
            }
            map = decodedPathParameters;
        }

        logger.debug("getPathParameters({}) returning {}", decode, map);
        return map;
    }

    public void resetPathParameters() {
        pathParameters = null;
    }

    public List<PathSegment> getPathSegments() {
        return getPathSegments(true);
    }

    public List<PathSegment> getPathSegments(boolean decode) {
        logger.debug("getPathSegments({}) called", decode);
        if (pathSegments == null) {
            pathSegments = UriHelper.parsePath(getPath(false));
            logger.debug("getPathSegments({}) encoded path parameters are: {}",
                         decode,
                         pathSegments);
        }

        List<PathSegment> list = pathSegments;
        if (decode) {
            if (decodedPathSegments == null) {
                decodedPathSegments = UriHelper.parsePath(getPath(true));
            }
            list = decodedPathSegments;
        }
        logger.debug("getPathSegments({}) returning {}", decode, list);
        return list;
    }

    public MultivaluedMap<String, String> getQueryParameters() {
        return getQueryParameters(true);
    }

    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        logger.debug("getQueryParameters({}) called", decode);
        if (queryParameters == null) {
            queryParameters = new MultivaluedMapImpl<String, String>();
            String query = messageContext.getAttribute(HttpServletRequest.class).getQueryString();
            logger.debug("getQueryParameters({}) query string is: {}", decode, query);
            queryParameters = UriHelper.parseQuery(query);
            logger.debug("getQueryParameters({}) encoded query parameters are: {}",
                         decode,
                         queryParameters);
        }

        MultivaluedMap<String, String> map = queryParameters;
        if (decode) {
            if (decodedQueryParameters == null) {
                decodedQueryParameters = UriEncoder.decodeMultivaluedMapValues(queryParameters);
            }
            map = decodedQueryParameters;
        }
        logger.debug("getQueryParameters({}) returning {}", decode, map);
        return map;
    }

    public URI getRequestUri() {
        logger.debug("getRequestUri() called");
        UriBuilder builder = getAbsolutePathBuilder();
        String query = messageContext.getAttribute(HttpServletRequest.class).getQueryString();
        logger.debug("getRequestUri() query string: {}", query);
        builder.replaceQuery(query);
        logger.debug("getRequestUri() build after query replacement: {}", builder);
        URI uri = builder.build();
        logger.debug("getRequestUri() returning: {}", uri);
        return uri;
    }

    public UriBuilder getRequestUriBuilder() {
        UriBuilder builder = UriBuilder.fromUri(getRequestUri());
        logger.debug("getRequestUriBuilder() returning: {}", builder);
        return builder;
    }

    private String getBaseUriString() {
        if (baseUriString == null) {
            baseUriString =
                buildBaseUriString(messageContext.getAttribute(HttpServletRequest.class),
                                   messageContext.getProperties());
        }
        logger.debug("getBaseUriString() returned {}", baseUriString);
        return baseUriString;
    }

    private String buildBaseUriString(HttpServletRequest request, Properties properties) {
        String httpURI = getURI(properties, "wink.http.uri");
        String httpsURI = getURI(properties, "wink.https.uri");
        if (httpURI != null || httpsURI != null) {
            if (httpsURI == null) {
                throw new IllegalStateException(Messages
                    .getMessage("parameterHttpsIsEmptyOrNotInitialized"));
            }
            if (httpURI == null) {
                throw new IllegalStateException(Messages
                    .getMessage("parameterHttpIsEmptyOrNotInitialized"));
            }
        } else {
            logger.debug("Endpoint is not set up in the configuration; using request detection");
        }

        String baseURI = httpURI;
        if (request.isSecure()) {
            logger.debug("buildBaseUriString request is secure");
            baseURI = httpsURI;
        }
        logger.debug("buildBaseUriString baseURI from properties is: {}", baseURI);
        if (baseURI == null) {
            baseURI = autodetectBaseUri(request);
            logger.debug("buildBaseUriString baseURI from autodetectBaseUri is: {}", baseURI);
        }
        return appendContextAndServletPath(baseURI, request, properties);
    }

    private String getURI(Properties properties, String propertyName) {
        String uri = properties.getProperty(propertyName);
        logger.debug("getURI({}, {}) called", properties, propertyName);
        if (uri != null && uri.length() != 0) {
            try {
                URI uriParsed = new URI(uri);
                logger.debug("getURI({}, {}) returning {}", new Object[] {properties, propertyName,
                    uriParsed});
                return uriParsed.toString();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(Messages.getMessage("uriInfoInvalidURI"), e);
            }
        }
        logger.debug("getURI({}, {}) returning null", properties, propertyName);
        return null;
    }

    private static String autodetectBaseUri(HttpServletRequest request) {
        try {
            return new URI(request.getScheme(), null, request.getServerName(), request
                .getServerPort(), "/", null, null).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private String appendContextAndServletPath(String basePath,
                                               HttpServletRequest request,
                                               Properties properties) {
        logger.debug("appendContextAndServletPath({}, {}, {}) called", new Object[] {basePath,
            request, properties});
        StringBuilder builder = new StringBuilder(basePath);
        if (builder.charAt(builder.length() - 1) == '/') {
            builder.deleteCharAt(builder.length() - 1);
        }
        String contextURI = properties.getProperty("wink.context.uri");
        String contextPath =
            ((contextURI != null && contextURI.length() > 0) ? contextURI : request
                .getContextPath());
        if (contextPath != null) {
            builder.append(contextPath);
            logger.debug("appendContextAndServletPath after contextPath called is: {} ", builder);
        }

        boolean isServlet =
            RuntimeContextTLS.getRuntimeContext().getAttribute(FilterConfig.class) == null;
        logger.debug("appendContextAndServletPath isServlet: {} ", isServlet);
        if (request.getServletPath() != null && isServlet) {
            builder.append(request.getServletPath());
            logger
                .debug("appendContextAndServletPath after getServletPath called is: {} ", builder);
        }
        if (builder.charAt(builder.length() - 1) != '/') {
            builder.append('/');
        }
        String builderStr = builder.toString();
        logger.debug("appendContextAndServletPath returning: {} ", builderStr);
        return builderStr;
    }

    private static String buildRequestPath(HttpServletRequest request) {
        // we cannot use request.getPathInfo() since it cuts off the ';'
        // parameters on Tomcat
        String requestPath = request.getRequestURI();
        logger.debug("buildRequestPath requestPath is: {}", requestPath);
        // Syntax-Based Normalization (RFC 3986, section 6.2.2)
        requestPath = UriHelper.normalize(requestPath);
        logger.debug("buildRequestPath requestPath normalized is: {}", requestPath);
        // cut off the context path from the beginning
        if (request.getContextPath() != null) {
            requestPath = requestPath.substring(request.getContextPath().length());
            logger.debug("buildRequestPath after context path removed: {}", requestPath);
        }

        // cut off the servlet path from the beginning
        boolean isServlet =
            RuntimeContextTLS.getRuntimeContext().getAttribute(FilterConfig.class) == null;
        logger.debug("buildRequestPath isServlet: {}", isServlet);
        if (request.getServletPath() != null && isServlet) {
            requestPath = requestPath.substring(request.getServletPath().length());
            logger
                .debug("buildRequestPath requestPath after servlet path removed: {}", requestPath);
        }

        // cut off all leading /
        int index = 0;
        while (index < requestPath.length() && requestPath.charAt(index) == '/') {
            ++index;
        }
        requestPath = requestPath.substring(index);
        logger.debug("buildRequestPath returning requestPath: {}", requestPath);
        return requestPath;
    }
}
