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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.RestConstants;
import org.apache.wink.common.internal.CaseInsensitiveMultivaluedMap;
import org.apache.wink.common.internal.http.Accept;
import org.apache.wink.common.internal.http.AcceptLanguage;
import org.apache.wink.common.internal.utils.HeaderUtils;
import org.apache.wink.common.internal.utils.StringUtils;
import org.apache.wink.common.internal.utils.UnmodifiableMultivaluedMap;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHeadersImpl implements HttpHeaders {

    private final static Logger            logger = LoggerFactory.getLogger(HttpHeadersImpl.class);

    private MessageContext                 msgContext;
    private MultivaluedMap<String, String> headers;
    private MultivaluedMap<String, String> allHeaders;
    private List<Locale>                   acceptableLanguages;
    private List<MediaType>                acceptableMediaTypes;
    private Map<String, Cookie>            cookies;
    private Locale                         language;
    private MediaType                      mediaType;
    private MultivaluedMap<String, String> allHeadersView;

    public HttpHeadersImpl(MessageContext msgContext) {
        this.msgContext = msgContext;
        headers = new CaseInsensitiveMultivaluedMap<String>();
        acceptableLanguages = null;
        acceptableMediaTypes = null;
        cookies = null;
        language = null;
        mediaType = null;
        allHeaders = null;
        allHeadersView = null;
    }

    public List<Locale> getAcceptableLanguages() {
        if (acceptableLanguages == null) {
            List<String> requestHeader = getRequestHeader(HttpHeaders.ACCEPT_LANGUAGE);
            if (requestHeader == null || requestHeader.isEmpty()) {
                acceptableLanguages = new LinkedList<Locale>();
            } else {
                StringBuilder acceptLanguageTemp = new StringBuilder();
                acceptLanguageTemp.append(requestHeader.get(0));
                for (int c = 1; c < requestHeader.size(); ++c) {
                    acceptLanguageTemp.append(","); //$NON-NLS-1$
                    acceptLanguageTemp.append(requestHeader.get(c));
                }
                String acceptLanguage = acceptLanguageTemp.toString();
                logger.debug("Accept-Language combined header is {}", acceptLanguage); //$NON-NLS-1$
                AcceptLanguage acceptLanguages = AcceptLanguage.valueOf(acceptLanguage);
                acceptableLanguages = acceptLanguages.getAcceptableLanguages();
            }
        }
        logger.debug("getAcceptableLanguages() returns {}", acceptableLanguages); //$NON-NLS-1$
        return acceptableLanguages;
    }

    public List<MediaType> getAcceptableMediaTypes() {
        if (acceptableMediaTypes == null) {
            Accept acceptHeader = getAcceptHeader();
            acceptableMediaTypes = acceptHeader.getSortedMediaTypes();
        }
        logger.debug("getAcceptableMediaTypes() returns {}", acceptableMediaTypes); //$NON-NLS-1$
        return acceptableMediaTypes;
    }

    private Accept getAcceptHeader() {
        String alternateParameter =
            msgContext.getUriInfo().getQueryParameters()
                .getFirst(RestConstants.REST_PARAM_MEDIA_TYPE);
        String acceptValue = null;
        logger.debug("alternateParameter is {}", alternateParameter); //$NON-NLS-1$
        if (alternateParameter != null) {
            // try to map alternate parameter shortcut to a real media type
            DeploymentConfiguration deploymentConfiguration =
                msgContext.getAttribute(DeploymentConfiguration.class);
            Map<String, String> alternateShortcutMap =
                deploymentConfiguration.getAlternateShortcutMap();
            logger.debug("alternateShortcutMap is {}", alternateShortcutMap); //$NON-NLS-1$
            if (alternateShortcutMap != null) {
                acceptValue = alternateShortcutMap.get(alternateParameter);
            }
            if (acceptValue == null) {
                acceptValue = alternateParameter;
            }
            logger.debug("acceptValue set via alternateParameter is {}", acceptValue); //$NON-NLS-1$
        } else {
            List<String> requestHeader = getRequestHeader(HttpHeaders.ACCEPT);
            if (requestHeader == null || requestHeader.isEmpty()) {
                acceptValue = null;
            } else if (requestHeader.size() > 1) {
                StringBuilder acceptValueTemp = new StringBuilder();
                acceptValueTemp.append(requestHeader.get(0));
                for (int c = 1; c < requestHeader.size(); ++c) {
                    acceptValueTemp.append(","); //$NON-NLS-1$
                    acceptValueTemp.append(requestHeader.get(c));
                }
                acceptValue = acceptValueTemp.toString();
            } else {
                acceptValue = requestHeader.get(0);
            }
        }
        try {
            logger.debug("Accept header is: {}", acceptValue); //$NON-NLS-1$
            Accept acceptHeader = Accept.valueOf(acceptValue);
            logger.debug("getAcceptHeader() returns {}", acceptHeader); //$NON-NLS-1$
            return acceptHeader;
        } catch (IllegalArgumentException e) {
            logger.debug("Illegal Accept request header: {}", e); //$NON-NLS-1$
            throw new WebApplicationException(e, 400);
        }
    }

    public Map<String, Cookie> getCookies() {
        if (cookies == null) {
            cookies = new HashMap<String, Cookie>();
            List<String> cookiesHeaders = getRequestHeaderInternal(HttpHeaders.COOKIE);
            if (cookiesHeaders != null) {
                for (String cookieHeader : cookiesHeaders) {
                    Cookie cookie = Cookie.valueOf(cookieHeader);
                    cookies.put(cookie.getName(), cookie);
                }
            }
        }
        logger.debug("Cookies are: {}", cookies); //$NON-NLS-1$
        return cookies;
    }

    public Locale getLanguage() {
        if (language == null) {
            String languageStr = headers.getFirst(HttpHeaders.CONTENT_LANGUAGE);
            if (languageStr == null) {
                List<String> s = getRequestHeaderInternal(HttpHeaders.CONTENT_LANGUAGE);
                if (s == null || s.isEmpty()) {
                    return null;
                } else {
                    languageStr = s.get(0);
                }
            }
            logger.debug("Language string is {}", languageStr); //$NON-NLS-1$
            if (languageStr == null) {
                logger.debug("getLanguage() returning null"); //$NON-NLS-1$
                return null;
            }
            String[] locales = StringUtils.fastSplit(languageStr, ","); //$NON-NLS-1$
            language = HeaderUtils.languageToLocale(locales[0].trim());
        }
        logger.debug("getLanguage() returning {}", language); //$NON-NLS-1$
        return language;
    }

    public MediaType getMediaType() {
        if (mediaType == null) {
            String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
            if (contentType == null) {
                List<String> s = getRequestHeaderInternal(HttpHeaders.CONTENT_TYPE);
                if (s == null || s.isEmpty()) {
                    logger.debug("getMediaType() returning null"); //$NON-NLS-1$
                    return null;
                } else {
                    contentType = s.get(0);
                }
            }
            logger.debug("Content-type is {}", contentType); //$NON-NLS-1$
            mediaType = MediaType.valueOf(contentType);
        }
        logger.debug("getMediaType() returning {}", mediaType); //$NON-NLS-1$
        return mediaType;
    }

    private List<String> getRequestHeaderInternal(String name) {
        if (allHeaders != null) {
            List<String> value = allHeaders.get(name);
            logger.debug("Returning {} header value from allHeaders cache: {}", name, value); //$NON-NLS-1$
            return value;
        }

        List<String> list = headers.get(name);
        if (list == null) {
            Enumeration<?> headerValues =
                msgContext.getAttribute(HttpServletRequest.class).getHeaders(name);
            list = new ArrayList<String>();
            while (headerValues.hasMoreElements()) {
                String val = (String)headerValues.nextElement();
                if (val != null) {
                    list.add(val);
                }
            }
            logger
                .debug("HttpServletRequest.getHeaders({}) returned {} so putting into headers cache", //$NON-NLS-1$
                       name,
                       list);
            headers.put(name, list);
        }
        logger.debug("getRequestHeaderInternal({}) returning {}", name, list); //$NON-NLS-1$
        return list;
    }

    public List<String> getRequestHeader(String name) {
        if (name == null) {
            logger.debug("getRequestHeader({}) returns null", name); //$NON-NLS-1$
            return null;
        }
        List<String> list = getRequestHeaderInternal(name);
        if (list == null || list.isEmpty()) {
            logger.debug("getRequestHeader({}) returns null due to empty or non-existent header", //$NON-NLS-1$
                         name);
            return null;
        }
        logger.debug("getRequestHeader({}) returns {}", name, list); //$NON-NLS-1$
        return Collections.unmodifiableList(list);
    }

    public MultivaluedMap<String, String> getRequestHeaders() {
        if (allHeadersView == null) {
            allHeadersView =
                new UnmodifiableMultivaluedMap<String, String>(
                                                               new MultivaluedRequestHeaderDelegate());
        }
        return allHeadersView;
    }

    private MultivaluedMap<String, String> buildRequestHeaders() {
        MultivaluedMap<String, String> map = new CaseInsensitiveMultivaluedMap<String>();
        Enumeration<?> names = msgContext.getAttribute(HttpServletRequest.class).getHeaderNames();

        if (names == null) {
            return map;
        }

        while (names.hasMoreElements()) {
            String name = (String)names.nextElement();
            Enumeration<?> headerValues =
                msgContext.getAttribute(HttpServletRequest.class).getHeaders(name);
            List<String> values = new ArrayList<String>();
            while (headerValues.hasMoreElements()) {
                String val = (String)headerValues.nextElement();
                if (val != null) {
                    values.add(val);
                }
            }
            logger.debug("buildRequestHeaders() adding {} header with values {}", name, values); //$NON-NLS-1$
            map.put(name, values);
        }
        return new UnmodifiableMultivaluedMap<String, String>(map);
    }

    private class MultivaluedRequestHeaderDelegate implements MultivaluedMap<String, String> {

        public void setupAllHeaders() {
            if (allHeaders == null) {
                allHeaders = buildRequestHeaders();
            }
        }

        public void add(String key, String value) {
            throw new UnsupportedOperationException();
        }

        public String getFirst(String key) {
            List<String> headers = getRequestHeaderInternal(key);
            if(headers == null || headers.isEmpty()) {
                return null;
            }
            return headers.get(0);
        }

        public void putSingle(String key, String value) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(Object key) {
            setupAllHeaders();
            return allHeaders.containsKey(key);
        }

        public boolean containsValue(Object value) {
            setupAllHeaders();
            return allHeaders.containsValue(value);
        }

        public Set<java.util.Map.Entry<String, List<String>>> entrySet() {
            setupAllHeaders();
            return allHeaders.entrySet();
        }

        public List<String> get(Object key) {
            setupAllHeaders();
            return allHeaders.get(key);
        }

        public boolean isEmpty() {
            setupAllHeaders();
            return allHeaders.isEmpty();
        }

        public Set<String> keySet() {
            setupAllHeaders();
            return allHeaders.keySet();
        }

        public List<String> put(String key, List<String> value) {
            throw new UnsupportedOperationException();
        }

        public void putAll(Map<? extends String, ? extends List<String>> t) {
            throw new UnsupportedOperationException();
        }

        public List<String> remove(Object key) {
            throw new UnsupportedOperationException();
        }

        public int size() {
            setupAllHeaders();
            return allHeaders.size();
        }

        public Collection<List<String>> values() {
            setupAllHeaders();
            return allHeaders.values();
        }

    }
}
