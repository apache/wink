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
        logger.trace("getAbsolutePath() returning: {}", absolutePath); //$NON-NLS-1$
        return absolutePath;
    }

    public UriBuilder getAbsolutePathBuilder() {
        UriBuilder builder = UriBuilder.fromUri(getAbsolutePath());
        logger.trace("getAbsolutePathBuilder() returning: {}", builder); //$NON-NLS-1$
        return builder;
    }

    public URI getBaseUri() {
        if (baseUri == null) {
            String baseUriString = getBaseUriString();
            try {
                baseUri = new URI(baseUriString);
            } catch (URISyntaxException e) {
                if (logger.isErrorEnabled()) {
                    logger.error(Messages.getMessage("uriBadBaseURI", baseUriString), e); //$NON-NLS-1$
                }
            }
        }
        logger.trace("getBaseUri() returning: {}", baseUri); //$NON-NLS-1$
        return baseUri;
    }

    public UriBuilder getBaseUriBuilder() {
        UriBuilder builder = UriBuilder.fromUri(getBaseUri());
        logger.trace("getBaseUriBuilder() returning: {}", builder); //$NON-NLS-1$
        return builder;
    }

    public List<ResourceInstance> getMatchedResourceInstances() {
        List<ResourceInstance> resources =
            Collections.unmodifiableList(messageContext.getAttribute(SearchResult.class).getData()
                .getMatchedResources());
        logger.trace("getMatchedResourceInstances() returning: {}", resources); //$NON-NLS-1$
        return resources;
    }

    public List<Object> getMatchedResources() {
        List<ResourceInstance> matchedResources =
            messageContext.getAttribute(SearchResult.class).getData().getMatchedResources();
        List<Object> resourceList = new ArrayList<Object>(matchedResources.size());
        for (ResourceInstance resourceInstance : matchedResources) {
            resourceList.add(resourceInstance.getInstance(messageContext));
        }
        logger.trace("getMatchedResources() returning: {}", resourceList); //$NON-NLS-1$
        return Collections.unmodifiableList(resourceList);
    }

    public List<String> getMatchedURIs() {
        return getMatchedURIs(true);
    }

    public List<String> getMatchedURIs(boolean decode) {
        logger.trace("getMatchedURIs({}) called", decode); //$NON-NLS-1$
        List<List<PathSegment>> matchedURIs =
            messageContext.getAttribute(SearchResult.class).getData().getMatchedURIs();
        if (matchedURIsStrings != null && matchedURIsStrings.size() != matchedURIs.size()) {
            matchedURIsStrings = null;
            decodedMatchedURIsStrings = null;
        }

        if (matchedURIsStrings == null) {
            matchedURIsStrings = new ArrayList<String>(matchedURIs.size());
            for (List<PathSegment> segments : matchedURIs) {
                logger.trace("Adding matched URI: {}", segments); //$NON-NLS-1$
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
                    logger.trace("Adding decoded URI: {} from URI: {}", decodedUri, uri); //$NON-NLS-1$
                }
            }
            list = decodedMatchedURIsStrings;
        }
        logger.trace("getMatchedURIs({}) returning {}", decode, list); //$NON-NLS-1$
        return Collections.unmodifiableList(list);
    }

    public String getPath() {
        return getPath(true);
    }

    public String getPath(boolean decode) {
        logger.trace("getPath({}) called", decode); //$NON-NLS-1$
        if (path == null) {
            path = buildRequestPath(messageContext.getAttribute(HttpServletRequest.class));
        }

        if (decode) {
            String decodedPath = UriEncoder.decodeString(path);
            logger.trace("getPath({}) returning {}", decode, decodedPath); //$NON-NLS-1$
            return decodedPath;
        }
        logger.trace("getPath({}) returning {}", decode, path); //$NON-NLS-1$
        return path;
    }

    public MultivaluedMap<String, String> getPathParameters() {
        return getPathParameters(true);
    }

    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        logger.trace("getPathParameters({}) called", decode); //$NON-NLS-1$
        if (pathParameters == null) {
            pathParameters = new MultivaluedMapImpl<String, String>();
            SearchResult searchResult = messageContext.getAttribute(SearchResult.class);
            if (searchResult == null) {
                throw new IllegalStateException(Messages
                    .getMessage("methodCallOutsideScopeOfRequestContext")); //$NON-NLS-1$
            }
            MultivaluedMapImpl.copy(searchResult.getData().getMatchedVariables(), pathParameters);
            logger.trace("getPathParameters({}) encoded path parameters are: {}", //$NON-NLS-1$
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

        logger.trace("getPathParameters({}) returning {}", decode, map); //$NON-NLS-1$
        return map;
    }

    public void resetPathParameters() {
        pathParameters = null;
    }

    public List<PathSegment> getPathSegments() {
        return getPathSegments(true);
    }

    public List<PathSegment> getPathSegments(boolean decode) {
        logger.trace("getPathSegments({}) called", decode); //$NON-NLS-1$
        if (pathSegments == null) {
            pathSegments = UriHelper.parsePath(getPath(false));
            logger.trace("getPathSegments({}) encoded path parameters are: {}", //$NON-NLS-1$
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
        logger.trace("getPathSegments({}) returning {}", decode, list); //$NON-NLS-1$
        return list;
    }

    public MultivaluedMap<String, String> getQueryParameters() {
        return getQueryParameters(true);
    }

    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        logger.trace("getQueryParameters({}) called", decode); //$NON-NLS-1$
        if (queryParameters == null) {
            queryParameters = new MultivaluedMapImpl<String, String>();
            String query = messageContext.getAttribute(HttpServletRequest.class).getQueryString();
            logger.trace("getQueryParameters({}) query string is: {}", decode, query); //$NON-NLS-1$
            queryParameters = UriHelper.parseQuery(query);
            logger.trace("getQueryParameters({}) encoded query parameters are: {}", //$NON-NLS-1$
                         decode,
                         queryParameters);
        }

        MultivaluedMap<String, String> map = queryParameters;
        if (decode) {
            if (decodedQueryParameters == null) {
                if (queryParameters.size() == 0) {
                    /*
                     * shortcut here if the query parameters don't exist
                     */
                    decodedQueryParameters = queryParameters;
                } else {
                    decodedQueryParameters = UriEncoder.decodeMultivaluedMapValues(queryParameters);
                }
            }
            map = decodedQueryParameters;
        }
        logger.trace("getQueryParameters({}) returning {}", decode, map); //$NON-NLS-1$
        return map;
    }

    public URI getRequestUri() {
        logger.trace("getRequestUri() called"); //$NON-NLS-1$
        UriBuilder builder = getAbsolutePathBuilder();
        String query = messageContext.getAttribute(HttpServletRequest.class).getQueryString();
        logger.trace("getRequestUri() query string: {}", query); //$NON-NLS-1$
        builder.replaceQuery(query);
        logger.trace("getRequestUri() build after query replacement: {}", builder); //$NON-NLS-1$
        URI uri = builder.build();
        logger.trace("getRequestUri() returning: {}", uri); //$NON-NLS-1$
        return uri;
    }

    public UriBuilder getRequestUriBuilder() {
        UriBuilder builder = UriBuilder.fromUri(getRequestUri());
        logger.trace("getRequestUriBuilder() returning: {}", builder); //$NON-NLS-1$
        return builder;
    }

    private String getBaseUriString() {
        if (baseUriString == null) {
            baseUriString =
                buildBaseUriString(messageContext.getAttribute(HttpServletRequest.class),
                                   messageContext.getProperties());
        }
        logger.trace("getBaseUriString() returned {}", baseUriString); //$NON-NLS-1$
        return baseUriString;
    }

    private String buildBaseUriString(HttpServletRequest request, Properties properties) {
        String httpURI = getURI(properties, "wink.http.uri"); //$NON-NLS-1$
        String httpsURI = getURI(properties, "wink.https.uri"); //$NON-NLS-1$
        if (httpURI != null || httpsURI != null) {
            if (httpsURI == null) {
                throw new IllegalStateException(Messages
                    .getMessage("parameterHttpsIsEmptyOrNotInitialized")); //$NON-NLS-1$
            }
            if (httpURI == null) {
                throw new IllegalStateException(Messages
                    .getMessage("parameterHttpIsEmptyOrNotInitialized")); //$NON-NLS-1$
            }
        } else {
            logger.trace("Endpoint is not set up in the configuration; using request detection"); //$NON-NLS-1$
        }

        String baseURI = httpURI;
        if (request.isSecure()) {
            logger.trace("buildBaseUriString request is secure"); //$NON-NLS-1$
            baseURI = httpsURI;
        }
        logger.trace("buildBaseUriString baseURI from properties is: {}", baseURI); //$NON-NLS-1$
        if (baseURI == null) {
            baseURI = autodetectBaseUri(request);
            logger.trace("buildBaseUriString baseURI from autodetectBaseUri is: {}", baseURI); //$NON-NLS-1$
        }
        return appendContextAndServletPath(baseURI, request, properties);
    }

    private String getURI(Properties properties, String propertyName) {
        String uri = properties.getProperty(propertyName);
        logger.trace("getURI({}, {}) called", properties, propertyName); //$NON-NLS-1$
        if (uri != null && uri.length() != 0) {
            try {
                URI uriParsed = new URI(uri);
                logger.trace("getURI({}, {}) returning {}", new Object[] {properties, propertyName, //$NON-NLS-1$
                    uriParsed});
                return uriParsed.toString();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(Messages.getMessage("uriInfoInvalidURI"), e); //$NON-NLS-1$
            }
        }
        logger.trace("getURI({}, {}) returning null", properties, propertyName); //$NON-NLS-1$
        return null;
    }

    private static String autodetectBaseUri(HttpServletRequest request) {
        try {
            return new URI(request.getScheme(), null, request.getServerName(), request
                .getServerPort(), "/", null, null).toString(); //$NON-NLS-1$
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private String appendContextAndServletPath(String basePath,
                                               HttpServletRequest request,
                                               Properties properties) {
        logger.trace("appendContextAndServletPath({}, {}, {}) called", new Object[] {basePath, //$NON-NLS-1$
            request, properties});
        StringBuilder builder = new StringBuilder(basePath);
        if (builder.charAt(builder.length() - 1) == '/') {
            builder.deleteCharAt(builder.length() - 1);
        }
        String contextURI = properties.getProperty("wink.context.uri"); //$NON-NLS-1$
        String contextPath =
            ((contextURI != null && contextURI.length() > 0) ? contextURI : request
                .getContextPath());
        if (contextPath != null) {
            builder.append(contextPath);
            logger.trace("appendContextAndServletPath after contextPath called is: {} ", builder); //$NON-NLS-1$
        }

        boolean isServlet =
            RuntimeContextTLS.getRuntimeContext().getAttribute(FilterConfig.class) == null;
        logger.trace("appendContextAndServletPath isServlet: {} ", isServlet); //$NON-NLS-1$
        if (request.getServletPath() != null && isServlet) {
            builder.append(request.getServletPath());
            logger
                .trace("appendContextAndServletPath after getServletPath called is: {} ", builder); //$NON-NLS-1$
        }
        if (builder.charAt(builder.length() - 1) != '/') {
            builder.append('/');
        }
        String builderStr = builder.toString();
        logger.trace("appendContextAndServletPath returning: {} ", builderStr); //$NON-NLS-1$
        return builderStr;
    }

    private static String buildRequestPath(HttpServletRequest request) {
        // we cannot use request.getPathInfo() since it cuts off the ';'
        // parameters on Tomcat
        String requestPath = request.getRequestURI();
        logger.trace("buildRequestPath requestPath is: {}", requestPath); //$NON-NLS-1$
        // Syntax-Based Normalization (RFC 3986, section 6.2.2)
        requestPath = UriHelper.normalize(requestPath);
        logger.trace("buildRequestPath requestPath normalized is: {}", requestPath); //$NON-NLS-1$
        // cut off the context path from the beginning
        if (request.getContextPath() != null) {
            requestPath = requestPath.substring(request.getContextPath().length());
            logger.trace("buildRequestPath after context path removed: {}", requestPath); //$NON-NLS-1$
        }

        // cut off the servlet path from the beginning
        boolean isServlet =
            RuntimeContextTLS.getRuntimeContext().getAttribute(FilterConfig.class) == null;
        logger.trace("buildRequestPath isServlet: {}", isServlet); //$NON-NLS-1$
        if (request.getServletPath() != null && isServlet) {
            requestPath = requestPath.substring(request.getServletPath().length());
            logger
                .trace("buildRequestPath requestPath after servlet path removed: {}", requestPath); //$NON-NLS-1$
        }

        // cut off all leading /
        int index = 0;
        while (index < requestPath.length() && requestPath.charAt(index) == '/') {
            ++index;
        }
        requestPath = requestPath.substring(index);
        logger.trace("buildRequestPath returning requestPath: {}", requestPath); //$NON-NLS-1$
        return requestPath;
    }
}
