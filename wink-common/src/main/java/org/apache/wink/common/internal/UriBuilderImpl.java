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
import java.util.Arrays;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UriBuilderImpl extends UriBuilder implements Cloneable {

    private static final Logger            logger = LoggerFactory.getLogger(UriBuilderImpl.class);

    private String                         scheme;
    private String                         userInfo;
    private String                         host;
    private int                            port;
    private String                         fragment;
    private List<PathSegment>              segments;
    private MultivaluedMap<String, String> query;
    private String                         schemeSpecificPart;
    private boolean                        isFirstCall;

    public UriBuilderImpl() {
        reset();
        isFirstCall = true;
    }

    public void reset() {
        logger.debug("Entered reset"); //$NON-NLS-1$
        scheme = null;
        resetSchemeSpecificPart();
        query = null;
        fragment = null;
        logger.debug("Exit reset"); //$NON-NLS-1$
    }

    private void resetSchemeSpecificPart() {
        logger.debug("Entered resetSchemeSpecificPart"); //$NON-NLS-1$
        schemeSpecificPart = null;
        userInfo = null;
        host = null;
        port = -1;
        segments = null;
        logger.debug("Exit resetSchemeSpecificPart"); //$NON-NLS-1$
    }

    private List<PathSegment> getPathSegments() {
        if (segments == null) {
            segments = new ArrayList<PathSegment>();
        }
        logger.debug("getPathSegments returning {}", segments); //$NON-NLS-1$
        return segments;
    }

    private MultivaluedMap<String, String> getQuery() {
        if (query == null) {
            query = new MultivaluedMapImpl<String, String>();
        }
        logger.debug("getQuery returning {}", query); //$NON-NLS-1$
        return query;
    }

    private String constructPathString() {
        if (segments == null) {
            logger.debug("constructPathString() returning null because null segments"); //$NON-NLS-1$
            return null;
        }

        StringBuilder path = new StringBuilder();
        for (PathSegment segment : segments) {
            String segmentStr = segment.toString();
            path.append("/"); //$NON-NLS-1$
            path.append(segmentStr);
            logger.debug("appending {} from path segment to path", segmentStr); //$NON-NLS-1$
        }

        String str = path.toString();
        logger.debug("constructPathString() returning {}", str); //$NON-NLS-1$
        return str;
    }

    private String constructQueryString() {
        if (query == null) {
            logger.debug("constructQueryString returning null beause null"); //$NON-NLS-1$
            return null;
        }
        if (query.size() == 0) {
            logger.debug("constructQueryString returning empty string because string size is 0"); //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        String queryStr = "?" + MultivaluedMapImpl.toString(query, "&"); //$NON-NLS-1$ //$NON-NLS-2$
        logger.debug("constructQueryString returning {}", queryStr); //$NON-NLS-1$
        return queryStr;
    }

    private Set<String> getVariableNamesList() {
        logger.debug("getVariableNamesList() entry"); //$NON-NLS-1$
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
        Set<String> ret = uriTemplate.getVariableNames();
        logger.debug("getVariableNamesList() returning {}", ret); //$NON-NLS-1$
        return ret;
    }

    private URI buildInternal(Map<String, ? extends Object> values)
        throws IllegalArgumentException, UriBuilderException {
        if (logger.isDebugEnabled()) {
            logger.debug("buildInternal({}, {}) entry", values //$NON-NLS-1$
                );
        }
        StringBuilder out = new StringBuilder();
        buildScheme(values, out);
        buildAuthority(values, out);
        buildPath(values, out);
        buildQuery(values, out);
        buildFragment(values, out);
        String uriString = out.toString();
        try {
            logger.debug("buildInternal() exit", uriString); //$NON-NLS-1$
            return new URI(uriString);
        } catch (URISyntaxException e) {
            throw new UriBuilderException(e);
        }
    }

    private void buildScheme(Map<String, ? extends Object> values, StringBuilder out) {
        logger.debug("buildScheme({}, {}) entry", values, out); //$NON-NLS-1$
        if (scheme == null) {
            logger.debug("buildScheme() is null so returning"); //$NON-NLS-1$
            return;
        }
        JaxRsUriTemplateProcessor.expand(scheme,
                                         MultivaluedMapImpl.toMultivaluedMapString(values),
                                         out);
        out.append(':');
        logger.debug("buildScheme() exit changed out to {}", out); //$NON-NLS-1$
    }

    private void buildAuthority(Map<String, ? extends Object> values, StringBuilder out) {
        logger.debug("buildAuthority({}, {}) entry", values, out); //$NON-NLS-1$
        if (userInfo == null && host == null && port == -1) {
            logger.debug("buildAuthority() is null so returning"); //$NON-NLS-1$
            return;
        }
        out.append("//"); //$NON-NLS-1$
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
        logger.debug("buildAuthority() exit changed out to {}", out); //$NON-NLS-1$
    }

    private void buildPath(Map<String, ? extends Object> values, StringBuilder out) {
        if (logger.isDebugEnabled()) {
            logger.debug("buildPath({}, {}) entry", new Object[] {values, out, //$NON-NLS-1$
            });
        }
        if (segments == null || segments.size() == 0) {
            logger.debug("buildPath() segments is null or empty so returning"); //$NON-NLS-1$
            return;
        }

        boolean first = true;
        for (PathSegment segment : segments) {
            // segment
            String segmentPath = segment.getPath();
            String eSegmentPath =
                JaxRsUriTemplateProcessor.expand(segmentPath, MultivaluedMapImpl
                    .toMultivaluedMapString(values));
            // note that even though we're encoding inside a loop over segments,
            // we're treating the path string (eSegmentPath) as just a path (so
            // "/" will not be encoded).

            // this allows the "values" map to contain values with "/" which
            // should not be encoded (since "/" is an unreserved character in
            // paths but not path segments). for real path segments specified by
            // the segments() method, the "/" is encoded in that method
            eSegmentPath = UriEncoder.encodePath(eSegmentPath, true);

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
        if (logger.isDebugEnabled()) {
            logger.debug("buildPath() exit changes out to {} ", out); //$NON-NLS-1$
        }
    }

    private void buildQuery(Map<String, ? extends Object> values, StringBuilder out) {
        if (logger.isDebugEnabled()) {
            logger.debug("buildQuery({}, {}) entry", values, out); //$NON-NLS-1$
        }
        if (query == null || query.size() == 0) {
            logger.debug("buildQuery() exit - query is null"); //$NON-NLS-1$
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
        logger.debug("buildQuery() exit - changes out to {}", out); //$NON-NLS-1$
    }

    private void buildFragment(Map<String, ? extends Object> values, StringBuilder out) {
        logger.debug("buildFragment({}, {})", values, out); //$NON-NLS-1$
        if (fragment == null) {
            return;
        }
        String eFragment =
            JaxRsUriTemplateProcessor.expand(fragment, MultivaluedMapImpl
                .toMultivaluedMapString(values));
        eFragment = UriEncoder.encodeFragment(eFragment, true);
        out.append('#');
        out.append(eFragment);
        logger.debug("buildFragment() exit - changes out to {}", out); //$NON-NLS-1$
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
        if (logger.isDebugEnabled()) {
            logger.debug("build({}, {}) enFtry", Boolean.valueOf(escapePercent), Arrays //$NON-NLS-1$
                .asList(values));
        }
        if (schemeSpecificPart != null) {
            try {
                // uri templates will be automatically encoded
                URI uri = new URI(scheme, schemeSpecificPart, fragment);
                if (logger.isDebugEnabled()) {
                    logger.debug("build() returning {}", uri.toString()); //$NON-NLS-1$
                }
                return uri;
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("schemeSpecificPart is invalid", e);
            }
        }

        Set<String> names = getVariableNamesList();
        if (values == null || names.size() > values.length) {
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
                logger.debug("name: {} has value : {}", name, value); //$NON-NLS-1$
            }
            ++i;
        }
        for (; i < values.length; ++i) {
            if (values[i] == null) {
                throw new IllegalArgumentException("value argument at " + i + "index is null");
            }
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
        if (logger.isDebugEnabled()) {
            logger.debug("buildFromMap({}, {})", Boolean.valueOf(escapePercent), values); //$NON-NLS-1$
        }
        Set<String> names = getVariableNamesList();
        if (values == null || (names.size() > values.size())) {
            throw new IllegalArgumentException("missing variable values");
        }
        logger.debug("names are {}", names); //$NON-NLS-1$
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
                logger.debug("name {} set to value {}", name, valueToPut); //$NON-NLS-1$
            }
        }
        return buildInternal(valuesMap);
    }

    private String escapePercent(String string) {
        logger.debug("escapePercent({}) entry", string); //$NON-NLS-1$
        StringBuilder out = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c == '%') {
                out.append("%25"); //$NON-NLS-1$
            } else {
                out.append(c);
            }
        }
        String ret = out.toString();
        logger.debug("escapePercent() return {}", ret); //$NON-NLS-1$
        return ret;
    }

    @Override
    public UriBuilder clone() {
        logger.debug("clone() entry"); //$NON-NLS-1$
        UriBuilderImpl uriBuilder = new UriBuilderImpl();
        uriBuilder.scheme(this.scheme);
        uriBuilder.userInfo(this.userInfo);
        uriBuilder.host(this.host);
        uriBuilder.port(this.port);
        uriBuilder.fragment(this.fragment);
        uriBuilder.segments(this.segments);
        uriBuilder.query(this.query);
        logger.debug("clone() exit"); //$NON-NLS-1$
        return this;
    }

    private void query(MultivaluedMap<String, String> query) {
        logger.debug("query({}) entry", query); //$NON-NLS-1$
        if (query == null) {
            logger.debug("query exit"); //$NON-NLS-1$
            return;
        }
        this.query = ((MultivaluedMapImpl<String, String>)query).clone();
        logger.debug("query exit"); //$NON-NLS-1$
    }

    private void segments(List<PathSegment> pathSegments) {
        logger.debug("segments({}) entry", pathSegments); //$NON-NLS-1$
        if (pathSegments == null) {
            logger.debug("segments() exit"); //$NON-NLS-1$
            return;
        }
        this.segments = new ArrayList<PathSegment>();
        for (PathSegment segment : pathSegments) {
            this.segments.add(((PathSegmentImpl)segment).clone());
        }
        logger.debug("segments() exit"); //$NON-NLS-1$
    }

    @Override
    public UriBuilder fragment(String fragment) {
        logger.debug("fragment({}) entry", fragment); //$NON-NLS-1$
        this.fragment = fragment;
        logger.debug("fragment() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder host(String host) throws IllegalArgumentException {
        logger.debug("host({}) entry", host); //$NON-NLS-1$
        if ("".equals(host)) {
            throw new IllegalArgumentException();
        }
        this.host = host;
        logger.debug("host() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder matrixParam(String name, Object... values) throws IllegalArgumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("matrixParam({}, {}) entry", name, (values == null) ? null : Arrays //$NON-NLS-1$
                .asList(values));
        }
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        if (values == null) {
            throw new IllegalArgumentException("values is null");
        }
        PathSegmentImpl lastSegment = getLastPathSegment();
        for (Object value : values) {
            lastSegment.getMatrixParameters().add(name, value.toString());
            if (logger.isDebugEnabled()) {
                logger.debug("lastSegment add({}, {})", name, value.toString()); //$NON-NLS-1$
            }
        }
        logger.debug("matrixParam exit"); //$NON-NLS-1$
        return this;
    }

    private PathSegmentImpl getLastPathSegment() {
        logger.debug("getLastPathSegment() entry"); //$NON-NLS-1$
        List<PathSegment> pathSegments = getPathSegments();
        logger.debug("getPathSegments() is {}", pathSegments); //$NON-NLS-1$
        PathSegmentImpl lastSegment = null;
        int lastSegmentIndex = pathSegments.size() - 1;
        if (lastSegmentIndex >= 0) {
            lastSegment = (PathSegmentImpl)pathSegments.get(lastSegmentIndex);
        } else {
            lastSegment = new PathSegmentImpl(""); //$NON-NLS-1$
            pathSegments.add(lastSegment);
        }
        logger.debug("getLastPathSegment() returning {}", lastSegment); //$NON-NLS-1$
        return lastSegment;
    }

    @Override
    public UriBuilder path(String path) throws IllegalArgumentException {
        logger.debug("path({}) entry", path); //$NON-NLS-1$
        if (path == null) {
            throw new IllegalArgumentException("path is null");
        }
        if ("".equals(path)) {
            // do nothing if there is an empty path
            return this;
        }

        // strip off the authority prefix if present
        String _path = path;
        if (path.startsWith("//")) {
            if (path.length() > 2) {
                _path = path.substring(2);
                getPathSegments().add(new PathSegmentImpl("/"));
            } else {
                logger.debug("path() exit"); //$NON-NLS-1$
                return this;
            }
        }

        List<PathSegment> list = UriHelper.parsePath(_path);
        logger.debug("path is {}", list); //$NON-NLS-1$
        for (PathSegment segment : list) {
            segment(segment.getPath());
            MultivaluedMap<String, String> matrixParameters = segment.getMatrixParameters();
            for (String matrix : matrixParameters.keySet()) {
                matrixParam(matrix, matrixParameters.get(matrix).toArray());
            }
        }
        logger.debug("path() exit"); //$NON-NLS-1$
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public UriBuilder path(Class resource) throws IllegalArgumentException {
        logger.debug("path({}) entry", resource); //$NON-NLS-1$
        if (resource == null) {
            throw new IllegalArgumentException("resource is null");
        }
        isFirstCall = false;
        Path pathAnnotation = ((Class<?>)resource).getAnnotation(Path.class);
        if (pathAnnotation == null) {
            throw new IllegalArgumentException("resource is not annotated with Path");
        }
        String path = pathAnnotation.value();
        logger.debug("path annotation value is {}", path); //$NON-NLS-1$
        path(path);
        logger.debug("path() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder path(Method method) throws IllegalArgumentException {
        logger.debug("path({}) entry", method); //$NON-NLS-1$
        if (method == null) {
            throw new IllegalArgumentException("method is null");
        }
        Path pathAnnotation = method.getAnnotation(Path.class);
        if (pathAnnotation == null) {
            throw new IllegalArgumentException("method is not annotated with Path");
        }
        String path = pathAnnotation.value();
        logger.debug("path method annotation is {}", path); //$NON-NLS-1$
        path(path);
        logger.debug("path() exit"); //$NON-NLS-1$
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public UriBuilder path(Class resource, String method) throws IllegalArgumentException {
        logger.debug("path({}, {}) entry", resource, method); //$NON-NLS-1$
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
        logger.debug("path() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder port(int port) throws IllegalArgumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("port({}) entry", port); //$NON-NLS-1$
        }
        if (port < -1) {
            throw new IllegalArgumentException();
        }
        this.port = port;
        logger.debug("port() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder queryParam(String name, Object... values) throws IllegalArgumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("queryParam({}, {}) entry", name, (values == null) ? null : Arrays //$NON-NLS-1$
                .asList(values));
        }
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        if (values == null) {
            throw new IllegalArgumentException("values is null");
        }
        MultivaluedMap<String, String> query = getQuery();
        logger.debug("query map is {}", query); //$NON-NLS-1$
        for (Object value : values) {
            if (value == null) {
                throw new IllegalArgumentException(); //$NON-NLS-1$ //$NON-NLS-2$
            }
            query.add(name, value != null ? value.toString() : null);
        }
        logger.debug("queryParam() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder replaceMatrix(String matrix) throws IllegalArgumentException {
        logger.debug("replaceMatrix({}) entry", matrix); //$NON-NLS-1$
        // clear all matrix parameters from existing last segment
        PathSegmentImpl lastPathSegment = getLastPathSegment();
        lastPathSegment.clearAllMatrixParameters();

        // use a temporary PathSegmentImpl to parse the matrix parameters
        PathSegmentImpl tmpPathSegment = new PathSegmentImpl("", matrix); //$NON-NLS-1$
        MultivaluedMap<String, String> matrixParameters = tmpPathSegment.getMatrixParameters();
        for (String param : matrixParameters.keySet()) {
            List<String> matrixValues = matrixParameters.get(param);
            // add the matrix parameter and its values
            matrixParam(param, matrixValues.toArray());
        }
        logger.debug("replaceMatrix() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder replaceMatrixParam(String name, Object... values)
        throws IllegalArgumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("replaceMatrixParam({}, {})", name, (values == null) ? null : Arrays //$NON-NLS-1$
                .asList(values));
        }
        if (name == null) {
            throw new IllegalArgumentException("name is null");
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
        logger.debug("replaceMatrixParam() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder replacePath(String path) {
        logger.debug("replacePath({}) entry", path); //$NON-NLS-1$
        if (isFirstCall) {
            if (path == null) {
                throw new IllegalArgumentException("path is null"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            isFirstCall = false;
        }
        if (path == null) {
            logger.debug("path is null. resetting");
            reset();
            logger.debug("replacePath() exit");
            return this;
        }
        getPathSegments().clear();
        if (path.indexOf(":") != -1) {
            // we need to parse this as scheme:scheme-specific-part#fragment for
            // a hierarchical URI
            // if a non-valid URI is passed in, the path is parsed as normal
            String[] segments = path.split(":", 2);
            if (segments.length == 2 && segments[0].length() > 0) {
                String scheme = segments[0];
                segments = segments[1].split("#", 2);
                if (segments[0].length() > 0) {
                    String schemeSpecificPart = segments[0];
                    String fragment = null;
                    if (segments.length == 2)
                        fragment = segments[1];
                    scheme(scheme);
                    schemeSpecificPart(schemeSpecificPart);
                    fragment(fragment);
                    logger.debug("replacePath() exit");
                    return this;
                }
            }
        }
        if (path != null && !"".equals(path)) {
            path(path);
        }
        logger.debug("replacePath() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder replaceQuery(String query) throws IllegalArgumentException {
        logger.debug("replaceQuery({}) entry", query); //$NON-NLS-1$
        getQuery().clear();
        if (query != null) {
            query = query.replaceAll(" ", "%20");
            MultivaluedMap<String, String> queries = UriHelper.parseQuery(query);
            // should values be URL encoded or query encoded?

            logger.debug("queries after parsing: {}", queries); //$NON-NLS-1$
            MultivaluedMap<String, String> queryValues = getQuery();
            for (String name : queries.keySet()) {
                List<String> values = queries.get(name);
                for (String v : values) {
                    if (v == null) {
                        queryValues.add(name, null);
                    } else {
                        queryParam(name, v);
                    }
                }
            }
        }
        logger.debug("replaceQuery() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder replaceQueryParam(String name, Object... values)
        throws IllegalArgumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("replaceQueryParam({}, {}) entry", name, (values == null) ? null : Arrays //$NON-NLS-1$
                .asList(values));
        }
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        // remove any exiting values
        getQuery().remove(name);

        if (values != null) {
            queryParam(name, values);
        }
        logger.debug("replaceQueryParam() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder scheme(String scheme) throws IllegalArgumentException {
        logger.debug("scheme({}) entry", scheme); //$NON-NLS-1$
        this.scheme = scheme;
        logger.debug("scheme() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder schemeSpecificPart(String ssp) throws IllegalArgumentException {
        logger.debug("schemeSpecificPart({}) entry", ssp); //$NON-NLS-1$
        if (ssp == null) {
            throw new IllegalArgumentException("schemeSpecificPart is null");
        }

        if (!ssp.startsWith("/")) { //$NON-NLS-1$
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
        logger.debug("schemeSpecificPart() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder segment(String... segments) throws IllegalArgumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("segment({}) entry", (segments == null) ? null : Arrays.asList(segments)); //$NON-NLS-1$
        }
        if (segments == null) {
            throw new IllegalArgumentException("segments is null");
        }

        List<PathSegment> pathSegments = getPathSegments();
        for (int i = 0; i < segments.length; ++i) {
            if (segments[i] == null) {
                throw new IllegalArgumentException("segment at index " + i + " is null");
            }
            if (segments[i].contains("/")) {
                String segValue = segments[i].replace("/", "%2F");
                pathSegments.add(new PathSegmentImpl(segValue));
            } else {
                pathSegments.add(new PathSegmentImpl(segments[i]));
            }
        }
        logger.debug("segment() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder userInfo(String ui) {
        logger.debug("userInfo({}) entry", ui); //$NON-NLS-1$
        userInfo = ui;
        logger.debug("userInfo() exit"); //$NON-NLS-1$
        return this;
    }

    @Override
    public UriBuilder uri(URI uri) throws IllegalArgumentException {
        logger.debug("Entering uri({})", uri); //$NON-NLS-1$
        if (uri == null) {
            throw new IllegalArgumentException("uri is null");
        }

        isFirstCall = false;

        if (uri.getScheme() != null) {
            logger.debug("Constructing scheme"); //$NON-NLS-1$
            scheme(uri.getScheme());
        }
        if (uri.getRawUserInfo() != null) {
            logger.debug("Constructing userInfo"); //$NON-NLS-1$
            userInfo(uri.getRawUserInfo());
        }
        if (uri.getHost() != null) {
            logger.debug("Constructing host"); //$NON-NLS-1$
            host(uri.getHost());
        }
        if (uri.getPort() != -1) {
            logger.debug("Constructing port"); //$NON-NLS-1$
            port(uri.getPort());
        }
        if (uri.getRawPath() != null) {
            logger.debug("Constructing rawPath"); //$NON-NLS-1$
            path(uri.getRawPath());
        }
        if (uri.getRawQuery() != null) {
            logger.debug("Constructing rawQuery"); //$NON-NLS-1$
            replaceQuery(uri.getRawQuery());
        }
        if (uri.getRawFragment() != null) {
            logger.debug("Constructing fragment"); //$NON-NLS-1$
            fragment(uri.getRawFragment());
        }
        if (uri.getSchemeSpecificPart() != null) {
            logger.debug("Constructing schemeSpecificPart"); //$NON-NLS-1$
            schemeSpecificPart(uri.getSchemeSpecificPart());
        }
        logger.debug("uri() exit"); //$NON-NLS-1$
        return this;
    }

}
