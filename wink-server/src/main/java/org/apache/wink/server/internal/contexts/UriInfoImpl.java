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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.PathSegmentImpl;
import org.apache.wink.common.internal.uri.UriEncoder;
import org.apache.wink.common.internal.utils.UriHelper;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.internal.handlers.SearchResult;
import org.apache.wink.server.internal.registry.ResourceInstance;

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
        return absolutePath;
    }

    public UriBuilder getAbsolutePathBuilder() {
        return UriBuilder.fromUri(getAbsolutePath());
    }

    public URI getBaseUri() {
        if (baseUri == null) {
            String baseUriString = getBaseUriString();
            try {
                baseUri = new URI(baseUriString);
            } catch (URISyntaxException e) {
                logger.error("bad base URI: " + baseUriString, e);
            }
        }
        return baseUri;
    }

    public UriBuilder getBaseUriBuilder() {
        return UriBuilder.fromUri(getBaseUri());
    }

    public List<ResourceInstance> getMatchedResourceInstances() {
        return Collections.unmodifiableList(messageContext.getAttribute(SearchResult.class)
            .getData().getMatchedResources());
    }

    public List<Object> getMatchedResources() {
        List<ResourceInstance> matchedResources =
            messageContext.getAttribute(SearchResult.class).getData().getMatchedResources();
        List<Object> resourceList = new ArrayList<Object>(matchedResources.size());
        for (ResourceInstance resourceInstance : matchedResources) {
            resourceList.add(resourceInstance.getInstance(messageContext));
        }

        return Collections.unmodifiableList(resourceList);
    }

    public List<String> getMatchedURIs() {
        return getMatchedURIs(true);
    }

    public List<String> getMatchedURIs(boolean decode) {
        List<List<PathSegment>> matchedURIs =
            messageContext.getAttribute(SearchResult.class).getData().getMatchedURIs();
        if (matchedURIsStrings != null && matchedURIsStrings.size() != matchedURIs.size()) {
            matchedURIsStrings = null;
            decodedMatchedURIsStrings = null;
        }

        if (matchedURIsStrings == null) {
            matchedURIsStrings = new ArrayList<String>(matchedURIs.size());
            for (List<PathSegment> segments : matchedURIs) {
                matchedURIsStrings.add(PathSegmentImpl.toString(segments));
            }
        }

        List<String> list = matchedURIsStrings;
        if (decode) {
            if (decodedMatchedURIsStrings == null) {
                decodedMatchedURIsStrings = new ArrayList<String>(matchedURIsStrings.size());
                for (String uri : matchedURIsStrings) {
                    decodedMatchedURIsStrings.add(UriEncoder.decodeString(uri));
                }
            }
            list = decodedMatchedURIsStrings;
        }

        return Collections.unmodifiableList(list);
    }

    public String getPath() {
        return getPath(true);
    }

    public String getPath(boolean decode) {
        if (path == null) {
            path = buildRequestPath(messageContext.getAttribute(HttpServletRequest.class));
        }

        if (decode) {
            return UriEncoder.decodeString(path);
        }

        return path;
    }

    public MultivaluedMap<String, String> getPathParameters() {
        return getPathParameters(true);
    }

    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        if (pathParameters == null) {
            pathParameters = new MultivaluedMapImpl<String, String>();
            SearchResult searchResult = messageContext.getAttribute(SearchResult.class);
            if (searchResult == null) {
                throw new IllegalStateException("outside the scope of a request");
            }
            MultivaluedMapImpl.copy(searchResult.getData().getMatchedVariables(), pathParameters);
        }

        MultivaluedMap<String, String> map = pathParameters;
        if (decode) {
            if (decodedPathParameters == null) {
                decodedPathParameters = UriEncoder.decodeMultivaluedMapValues(pathParameters);
            }
            map = decodedPathParameters;
        }

        return map;
    }

    public void resetPathParameters() {
        pathParameters = null;
    }

    public List<PathSegment> getPathSegments() {
        return getPathSegments(true);
    }

    public List<PathSegment> getPathSegments(boolean decode) {
        if (pathSegments == null) {
            pathSegments = UriHelper.parsePath(getPath(false));
        }

        List<PathSegment> list = pathSegments;
        if (decode) {
            if (decodedPathSegments == null) {
                decodedPathSegments = UriHelper.parsePath(getPath(true));
            }
            list = decodedPathSegments;
        }

        return list;
    }

    public MultivaluedMap<String, String> getQueryParameters() {
        return getQueryParameters(true);
    }

    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        if (queryParameters == null) {
            queryParameters = new MultivaluedMapImpl<String, String>();
            String query = messageContext.getAttribute(HttpServletRequest.class).getQueryString();
            queryParameters = UriHelper.parseQuery(query);
        }

        MultivaluedMap<String, String> map = queryParameters;
        if (decode) {
            if (decodedQueryParameters == null) {
                decodedQueryParameters = UriEncoder.decodeMultivaluedMapValues(queryParameters);
            }
            map = decodedQueryParameters;
        }
        return map;
    }

    public URI getRequestUri() {
        UriBuilder builder = getAbsolutePathBuilder();
        String query = messageContext.getAttribute(HttpServletRequest.class).getQueryString();
        builder.replaceQuery(query);
        return builder.build();
    }

    public UriBuilder getRequestUriBuilder() {
        return UriBuilder.fromUri(getRequestUri());
    }

    private String getBaseUriString() {
        if (baseUriString == null) {
            baseUriString =
                buildBaseUriString(messageContext.getAttribute(HttpServletRequest.class),
                                   messageContext.getProperties());
        }
        return baseUriString;
    }

    private String buildBaseUriString(HttpServletRequest request, Properties properties) {
        String httpURI = getURI(properties, "wink.http.uri");
        String httpsURI = getURI(properties, "wink.https.uri");
        if (httpURI != null || httpsURI != null) {
            if (httpsURI == null) {
                throw new IllegalStateException("Parameter httpsURI is empty or not initialized");
            }
            if (httpURI == null) {
                throw new IllegalStateException("Parameter httpsURI is empty or not initialized");
            }
        } else {
            logger.debug("Endpoint is not set up in the configuration; using request detection");
        }

        String baseURI = httpURI;
        if (request.isSecure()) {
            baseURI = httpsURI;
        }
        if (baseURI == null) {
            baseURI = autodetectBaseUri(request);
        }
        return appendContextAndServletPath(baseURI, request, properties);
    }

    private String getURI(Properties properties, String propertyName) {
        String uri = properties.getProperty(propertyName);
        if (uri != null && uri.length() != 0) {
            try {
                URI uriParsed = new URI(uri);
                return uriParsed.toString();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("invalid URI", e);
            }
        }
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
        }
        if (request.getServletPath() != null) {
            builder.append(request.getServletPath());
        }
        if (builder.charAt(builder.length() - 1) != '/') {
            builder.append('/');
        }
        return builder.toString();
    }

    private String buildRequestPath(HttpServletRequest request) {
        // we cannot use request.getPathInfo() since it cuts off the ';'
        // parameters on Tomcat
        String requestPath = request.getRequestURI();

        // Syntax-Based Normalization (RFC 3986, section 6.2.2)
        requestPath = UriHelper.normalize(requestPath);

        // cut off the context path from the beginning
        if (request.getContextPath() != null) {
            requestPath = requestPath.substring(request.getContextPath().length());
        }

        // cut off the servlet path from the beginning
        if (request.getServletPath() != null) {
            requestPath = requestPath.substring(request.getServletPath().length());
        }

        // cut off all leading /
        int index = 0;
        while (index < requestPath.length() && requestPath.charAt(index) == '/') {
            ++index;
        }
        requestPath = requestPath.substring(index);

        return requestPath;
    }
}
