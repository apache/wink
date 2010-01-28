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

package org.apache.wink.common.internal;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.apache.wink.common.internal.uri.UriEncoder;
import org.apache.wink.common.internal.uritemplate.JaxRsUriTemplateProcessor;
import org.apache.wink.common.internal.utils.UriHelper;

public class UriBuilderImpl extends UriBuilder implements Cloneable {

    private String                         scheme;
    private String                         userInfo;
    private String                         host;
    private int                            port;
    private String                         fragment;
    private List<PathSegment>              segments;
    private MultivaluedMap<String, String> query;
    private String                         schemeSpecificPart;

    public UriBuilderImpl() {
        reset();
    }

    public void reset() {
        scheme = null;
        resetSchemeSpecificPart();
        query = null;
        fragment = null;
    }

    private void resetSchemeSpecificPart() {
        schemeSpecificPart = null;
        userInfo = null;
        host = null;
        port = -1;
        segments = null;
    }

    private List<PathSegment> getPathSegments() {
        if (segments == null) {
            segments = new ArrayList<PathSegment>();
        }
        return segments;
    }

    private MultivaluedMap<String, String> getQuery() {
        if (query == null) {
            query = new MultivaluedMapImpl<String, String>();
        }
        return query;
    }

    private String constructPathString() {
        if (segments == null) {
            return null;
        }

        StringBuilder path = new StringBuilder();
        for (PathSegment segment : segments) {
            String segmentStr = segment.toString();
            path.append("/");
            path.append(segmentStr);
        }

        return path.toString();
    }

    private String constructQueryString() {
        if (query == null) {
            return null;
        }
        if (query.size() == 0) {
            return "";
        }
        String queryStr = "?" + MultivaluedMapImpl.toString(query, "&");
        return queryStr;
    }

    private Set<String> getVariableNamesList() {
        String constructedPath = constructPathString();
        String constructedQuery = constructQueryString();
        String uriStr =
            UriHelper.contructUri(scheme,
                                  userInfo,
                                  host,
                                  port,
                                  constructedPath,
                                  constructedQuery,
                                  fragment);
        JaxRsUriTemplateProcessor uriTemplate = new JaxRsUriTemplateProcessor(uriStr);
        return uriTemplate.getVariableNames();
    }

    private URI buildInternal(Map<String, ? extends Object> values)
        throws IllegalArgumentException, UriBuilderException {
        StringBuilder out = new StringBuilder();
        buildScheme(values, out);
        buildAuthority(values, out);
        buildPath(values, out);
        buildQuery(values, out);
        buildFragment(values, out);
        String uriString = out.toString();
        try {
            return new URI(uriString);
        } catch (URISyntaxException e) {
            throw new UriBuilderException(e);
        }
    }

    private void buildScheme(Map<String, ? extends Object> values, StringBuilder out) {
        if (scheme == null) {
            return;
        }
        JaxRsUriTemplateProcessor.expand(scheme,
                                         MultivaluedMapImpl.toMultivaluedMapString(values),
                                         out);
        out.append(':');
    }

    private void buildAuthority(Map<String, ? extends Object> values, StringBuilder out) {
        if (userInfo == null && host == null && port == -1) {
            return;
        }
        out.append("//");
        if (userInfo != null) {
            String eUserInfo =
                JaxRsUriTemplateProcessor.expand(userInfo, MultivaluedMapImpl
                    .toMultivaluedMapString(values));
            eUserInfo = UriEncoder.encodeUserInfo(eUserInfo, true);
            out.append(eUserInfo);
            out.append('@');
        }
        if (host != null) {
            JaxRsUriTemplateProcessor.expand(host, MultivaluedMapImpl
                .toMultivaluedMapString(values), out);
        }
        if (port != -1) {
            out.append(':');
            out.append(port);
        }
    }

    private void buildPath(Map<String, ? extends Object> values, StringBuilder out) {
        if (segments == null || segments.size() == 0) {
            return;
        }

        boolean first = true;
        for (PathSegment segment : segments) {
            // segment
            String segmentPath = segment.getPath();
            String eSegmentPath =
                JaxRsUriTemplateProcessor.expand(segmentPath, MultivaluedMapImpl
                    .toMultivaluedMapString(values));
            eSegmentPath = UriEncoder.encodePathSegment(eSegmentPath, true);

            // we output the path separator if:
            // 1. if we already have some uri built and the last character is
            // not the path separator
            // 2. if the uri is still empty and this is not the first path
            // segment
            if ((out.length() > 0 && out.charAt(out.length() - 1) != '/') || (out.length() == 0 && !first)) {
                out.append('/');
            }
            first = false;

            // output the path segment
            out.append(eSegmentPath);

            // matrix parameters
            MultivaluedMap<String, String> matrixParameters = segment.getMatrixParameters();
            for (String matrix : matrixParameters.keySet()) {
                // matrix parameter
                String eMatrix =
                    JaxRsUriTemplateProcessor.expand(matrix, MultivaluedMapImpl
                        .toMultivaluedMapString(values));
                eMatrix = UriEncoder.encodeMatrix(eMatrix, true);

                // matrix values
                for (String matrixValue : matrixParameters.get(matrix)) {
                    String eValue =
                        JaxRsUriTemplateProcessor.expand(matrixValue, MultivaluedMapImpl
                            .toMultivaluedMapString(values));
                    eValue = UriEncoder.encodeMatrix(eValue, true);
                    out.append(';');
                    out.append(eMatrix);
                    out.append('=');
                    out.append(eValue);
                }
            }
        }
    }

    private void buildQuery(Map<String, ? extends Object> values, StringBuilder out) {
        if (query == null || query.size() == 0) {
            return;
        }
        char delim = '?';
        for (String queryParam : query.keySet()) {
            // query param name
            String eQueryParam =
                JaxRsUriTemplateProcessor.expand(queryParam, MultivaluedMapImpl
                    .toMultivaluedMapString(values));
            eQueryParam = UriEncoder.encodeQueryParam(eQueryParam, true);

            // query param values
            for (String queryValue : query.get(queryParam)) {
                String eQueryValue =
                    JaxRsUriTemplateProcessor.expand(queryValue, MultivaluedMapImpl
                        .toMultivaluedMapString(values));
                eQueryValue = UriEncoder.encodeQueryParam(eQueryValue, true);
                out.append(delim);
                out.append(eQueryParam);
                if (eQueryValue == null) {
                    continue;
                }
                out.append('=');
                out.append(eQueryValue);
                delim = '&';
            }
        }
    }

    private void buildFragment(Map<String, ? extends Object> values, StringBuilder out) {
        if (fragment == null) {
            return;
        }
        String eFragment =
            JaxRsUriTemplateProcessor.expand(fragment, MultivaluedMapImpl
                .toMultivaluedMapString(values));
        eFragment = UriEncoder.encodeFragment(eFragment, true);
        out.append('#');
        out.append(eFragment);
    }

    @Override
    public URI build(Object... values) throws IllegalArgumentException, UriBuilderException {
        return build(true, values);
    }

    @Override
    public URI buildFromEncoded(Object... values) throws IllegalArgumentException,
        UriBuilderException {
        return build(false, values);
    }

    private URI build(boolean escapePercent, Object... values) throws IllegalArgumentException,
        UriBuilderException {

        if (schemeSpecificPart != null) {
            try {
                // uri templates will be automatically encoded
                return new URI(scheme, schemeSpecificPart, fragment);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("schemeSpecificPart is invalid", e);
            }
        }

        Set<String> names = getVariableNamesList();
        if (names.size() > values.length) {
            throw new IllegalArgumentException("missing variable values");
        }
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        int i = 0;
        for (String name : names) {
            if (values[i] == null) {
                throw new IllegalArgumentException("value for variable " + name + " is null");
            }
            // put only the first occurrence of the value in the map
            if (valuesMap.get(name) == null) {
                String value = values[i].toString();
                if (escapePercent) {
                    value = escapePercent(value);
                }
                valuesMap.put(name, value);
            }
            ++i;
        }
        return buildInternal(valuesMap);
    }

    @Override
    public URI buildFromMap(Map<String, ? extends Object> values) throws IllegalArgumentException,
        UriBuilderException {
        return buildFromMap(true, values);
    }

    @Override
    public URI buildFromEncodedMap(Map<String, ? extends Object> values)
        throws IllegalArgumentException, UriBuilderException {
        return buildFromMap(false, values);
    }

    private URI buildFromMap(boolean escapePercent, Map<String, ? extends Object> values)
        throws IllegalArgumentException, UriBuilderException {
        Set<String> names = getVariableNamesList();
        if (names.size() > values.size()) {
            throw new IllegalArgumentException("missing variable values");
        }
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        for (String name : names) {
            Object value = values.get(name);
            if (value == null) {
                throw new IllegalArgumentException("value for variable " + name + " is null");
            }
            // put only the first occurrence of the value in the map
            if (valuesMap.get(name) == null) {
                String valueToPut = value.toString();
                if (escapePercent) {
                    valueToPut = escapePercent(valueToPut);
                }
                valuesMap.put(name, valueToPut);
            }
        }
        return buildInternal(valuesMap);
    }

    private String escapePercent(String string) {
        StringBuilder out = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c == '%') {
                out.append("%25");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    @Override
    public UriBuilder clone() {
        UriBuilderImpl uriBuilder = new UriBuilderImpl();
        uriBuilder.scheme(this.scheme);
        uriBuilder.userInfo(this.userInfo);
        uriBuilder.host(this.host);
        uriBuilder.port(this.port);
        uriBuilder.fragment(this.fragment);
        uriBuilder.segments(this.segments);
        uriBuilder.query(this.query);
        return this;
    }

    private void query(MultivaluedMap<String, String> query) {
        if (query == null) {
            return;
        }
        this.query = ((MultivaluedMapImpl<String, String>)query).clone();
    }

    private void segments(List<PathSegment> pathSegments) {
        if (pathSegments == null) {
            return;
        }
        this.segments = new ArrayList<PathSegment>();
        for (PathSegment segment : pathSegments) {
            this.segments.add(((PathSegmentImpl)segment).clone());
        }
    }

    @Override
    public UriBuilder fragment(String fragment) {
        this.fragment = fragment;
        return this;
    }

    @Override
    public UriBuilder host(String host) throws IllegalArgumentException {
        // null unsets the host so don't check that
        if ("".equals(host)) {
            throw new IllegalArgumentException();
        }
        this.host = host;
        return this;
    }

    @Override
    public UriBuilder matrixParam(String name, Object... values) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        if (values == null) {
            throw new IllegalArgumentException("values is null");
        }
        PathSegmentImpl lastSegment = getLastPathSegment();
        for (Object value : values) {
            lastSegment.getMatrixParameters().add(name, value.toString());
        }
        return this;
    }

    private PathSegmentImpl getLastPathSegment() {
        List<PathSegment> pathSegments = getPathSegments();
        PathSegmentImpl lastSegment = null;
        int lastSegmentIndex = pathSegments.size() - 1;
        if (lastSegmentIndex >= 0) {
            lastSegment = (PathSegmentImpl)pathSegments.get(lastSegmentIndex);
        } else {
            lastSegment = new PathSegmentImpl("");
            pathSegments.add(lastSegment);
        }
        return lastSegment;
    }

    @Override
    public UriBuilder path(String path) throws IllegalArgumentException {
        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }
        List<PathSegment> list = UriHelper.parsePath(path);
        for (PathSegment segment : list) {
            segment(segment.getPath());
            MultivaluedMap<String, String> matrixParameters = segment.getMatrixParameters();
            for (String matrix : matrixParameters.keySet()) {
                matrixParam(matrix, matrixParameters.get(matrix).toArray());
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public UriBuilder path(Class resource) throws IllegalArgumentException {
        if (resource == null) {
            throw new IllegalArgumentException("resource is null");
        }
        Path pathAnnotation = ((Class<?>)resource).getAnnotation(Path.class);
        if (pathAnnotation == null) {
            throw new IllegalArgumentException("resource is not annotated with Path");
        }
        String path = pathAnnotation.value();
        path(path);
        return this;
    }

    @Override
    public UriBuilder path(Method method) throws IllegalArgumentException {
        if (method == null) {
            throw new IllegalArgumentException("method is null");
        }
        Path pathAnnotation = method.getAnnotation(Path.class);
        if (pathAnnotation == null) {
            throw new IllegalArgumentException("method is not annotated with Path");
        }
        String path = pathAnnotation.value();
        path(path);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public UriBuilder path(Class resource, String method) throws IllegalArgumentException {
        if (resource == null) {
            throw new IllegalArgumentException("resource is null");
        }
        if (method == null) {
            throw new IllegalArgumentException("method is null");
        }

        Method foundMethod = null;
        Method[] methods = resource.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getName().equals(method)) {
                Path pathAnnotation = m.getAnnotation(Path.class);
                if (pathAnnotation != null) {
                    if (foundMethod != null) {
                        throw new IllegalArgumentException(
                                                           "more than one method with Path annotation exists");
                    }
                    foundMethod = m;
                }
            }
        }
        if (foundMethod == null) {
            throw new IllegalArgumentException("no method with Path annotation exists");
        }
        path(foundMethod);
        return this;
    }

    @Override
    public UriBuilder port(int port) throws IllegalArgumentException {
        // -1 unsets the port so don't worry about that
        if (port < -1) {
            throw new IllegalArgumentException();
        }
        this.port = port;
        return this;
    }

    @Override
    public UriBuilder queryParam(String name, Object... values) throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        if (values == null) {
            throw new IllegalArgumentException("values is null");
        }
        MultivaluedMap<String, String> query = getQuery();
        for (Object value : values) {
            query.add(name, value != null ? value.toString() : null);
        }
        return this;
    }

    @Override
    public UriBuilder replaceMatrix(String matrix) throws IllegalArgumentException {
        // clear all matrix parameters from existing last segment
        PathSegmentImpl lastPathSegment = getLastPathSegment();
        lastPathSegment.clearAllMatrixParameters();

        // use a temporary PathSegmentImpl to parse the matrix parameters
        PathSegmentImpl tmpPathSegment = new PathSegmentImpl("", matrix);
        MultivaluedMap<String, String> matrixParameters = tmpPathSegment.getMatrixParameters();
        for (String param : matrixParameters.keySet()) {
            List<String> matrixValues = matrixParameters.get(param);
            // add the matrix parameter and its values
            matrixParam(param, matrixValues.toArray());
        }
        return this;
    }

    @Override
    public UriBuilder replaceMatrixParam(String name, Object... values)
        throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        if (values == null) {
            throw new IllegalArgumentException("values is null");
        }
        PathSegmentImpl lastPathSegment = getLastPathSegment();
        if (values == null || values.length == 0) {
            lastPathSegment.clearMatrixParameter(name);
        } else {
            List<String> valuesList = lastPathSegment.getMatrixParameters().get(name);
            if (valuesList != null) {
                valuesList.clear();
            }
            matrixParam(name, values);
        }
        return this;
    }

    @Override
    public UriBuilder replacePath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }
        getPathSegments().clear();
        if (path != null) {
            path(path);
        }
        return this;
    }

    @Override
    public UriBuilder replaceQuery(String query) throws IllegalArgumentException {
        getQuery().clear();
        if (query != null) {
            MultivaluedMap<String, String> queries = UriHelper.parseQuery(query);
            for (String name : queries.keySet()) {
                queryParam(name, queries.get(name).toArray());
            }
        }
        return this;
    }

    @Override
    public UriBuilder replaceQueryParam(String name, Object... values)
        throws IllegalArgumentException {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        // remove any exiting values
        getQuery().remove(name);

        if (values != null) {
            queryParam(name, values);
        }
        return this;
    }

    @Override
    public UriBuilder scheme(String scheme) throws IllegalArgumentException {
        this.scheme = scheme;
        return this;
    }

    @Override
    public UriBuilder schemeSpecificPart(String ssp) throws IllegalArgumentException {
        if (ssp == null) {
            throw new IllegalArgumentException("schemeSpecificPart is null");
        }

        if (!ssp.startsWith("/")) {
            // An opaque URI is an absolute URI whose scheme-specific part does
            // not begin with a slash character ('/').
            // Opaque URIs are not subject to further parsing.
            schemeSpecificPart = ssp;
            return this;
        }

        URI uri = null;
        try {
            // uri templates will be automatically encoded
            uri = new URI(scheme, ssp, fragment);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("schemeSpecificPart is invalid", e);
        }

        resetSchemeSpecificPart();

        // decode every part before applying
        if (uri.getRawUserInfo() != null) {
            userInfo(UriEncoder.decodeString(uri.getRawUserInfo()));
        }
        if (uri.getHost() != null) {
            host(UriEncoder.decodeString(uri.getHost()));
        }
        if (uri.getPort() != -1) {
            port(uri.getPort());
        }
        if (uri.getRawPath() != null) {
            path(UriEncoder.decodeString(uri.getRawPath()));
        }
        return this;
    }

    @Override
    public UriBuilder segment(String... segments) throws IllegalArgumentException {
        if (segments == null) {
            throw new IllegalArgumentException("segments is null");
        }

        List<PathSegment> pathSegments = getPathSegments();
        for (int i = 0; i < segments.length; ++i) {
            if (segments[i] == null) {
                throw new IllegalArgumentException("segment at index " + i + " is null");
            }
            pathSegments.add(new PathSegmentImpl(segments[i]));
        }
        return this;
    }

    @Override
    public UriBuilder userInfo(String ui) {
        userInfo = ui;
        return this;
    }

    @Override
    public UriBuilder uri(URI uri) throws IllegalArgumentException {
        if (uri == null) {
            throw new IllegalArgumentException("uri is null");
        }

        reset();

        if (uri.getScheme() != null) {
            scheme(uri.getScheme());
        }
        if (uri.getRawUserInfo() != null) {
            userInfo(uri.getRawUserInfo());
        }
        if (uri.getHost() != null) {
            host(uri.getHost());
        }
        if (uri.getPort() != -1) {
            port(uri.getPort());
        }
        if (uri.getRawPath() != null) {
            path(uri.getRawPath());
        }
        if (uri.getRawQuery() != null) {
            replaceQuery(uri.getRawQuery());
        }
        if (uri.getRawFragment() != null) {
            fragment(uri.getRawFragment());
        }
        if (uri.getSchemeSpecificPart() != null) {
            schemeSpecificPart(uri.getSchemeSpecificPart());
        }
        return this;
    }

}
