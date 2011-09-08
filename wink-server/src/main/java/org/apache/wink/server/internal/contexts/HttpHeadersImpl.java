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
import javax.ws.rs.core.Response.Status;

import org.apache.wink.common.RestConstants;
import org.apache.wink.common.internal.CaseInsensitiveMultivaluedMap;
import org.apache.wink.common.internal.WinkConfiguration;
import org.apache.wink.common.internal.http.Accept;
import org.apache.wink.common.internal.http.AcceptLanguage;
import org.apache.wink.common.internal.i18n.Messages;
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
                logger.trace("Accept-Language combined header is {}", acceptLanguage); //$NON-NLS-1$
                AcceptLanguage acceptLanguages = AcceptLanguage.valueOf(acceptLanguage);
                acceptableLanguages = acceptLanguages.getAcceptableLanguages();
            }
        }
        logger.trace("getAcceptableLanguages() returns {}", acceptableLanguages); //$NON-NLS-1$
        return acceptableLanguages;
    }

    public List<MediaType> getAcceptableMediaTypes() {
        if (acceptableMediaTypes == null) {
            Accept acceptHeader = getAcceptHeader();
            acceptableMediaTypes = acceptHeader.getSortedMediaTypes();
        }
        logger.trace("getAcceptableMediaTypes() returns {}", acceptableMediaTypes); //$NON-NLS-1$
        return acceptableMediaTypes;
    }

    private Accept getAcceptHeader() {
        String alternateParameter =
            msgContext.getUriInfo().getQueryParameters()
                .getFirst(RestConstants.REST_PARAM_MEDIA_TYPE);
        String acceptValue = null;
        logger.trace("alternateParameter is {}", alternateParameter); //$NON-NLS-1$
        if (alternateParameter != null) {
            // try to map alternate parameter shortcut to a real media type
            // we're on the server, so this is a safe cast
            DeploymentConfiguration deploymentConfiguration =
                (DeploymentConfiguration)msgContext.getAttribute(WinkConfiguration.class);
            Map<String, String> alternateShortcutMap =
                deploymentConfiguration.getAlternateShortcutMap();
            logger.trace("alternateShortcutMap is {}", alternateShortcutMap); //$NON-NLS-1$
            if (alternateShortcutMap != null) {
                acceptValue = alternateShortcutMap.get(alternateParameter);
            }
            if (acceptValue == null) {
                acceptValue = alternateParameter;
            }
            logger.trace("acceptValue set via alternateParameter is {}", acceptValue); //$NON-NLS-1$
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
            logger.trace("Accept header is: {}", acceptValue); //$NON-NLS-1$
            Accept acceptHeader = Accept.valueOf(acceptValue);
            logger.trace("getAcceptHeader() returns {}", acceptHeader); //$NON-NLS-1$
            return acceptHeader;
        } catch (IllegalArgumentException e) {
            logger.error(Messages.getMessage("illegalAcceptHeader", acceptValue), e);
            throw new WebApplicationException(e, Status.BAD_REQUEST);
        }
    }

    public Map<String, Cookie> getCookies() {
        if (cookies == null) {
            cookies = new HashMap<String, Cookie>();
            List<String> cookiesHeaders = getRequestHeaderInternal(HttpHeaders.COOKIE);
            if (cookiesHeaders != null) {
                for (String cookieHeader : cookiesHeaders) {
                    List<Cookie> currentCookies = parseCookieHeader(cookieHeader);
                    for (Cookie c : currentCookies) {
                        cookies.put(c.getName(), c);
                    }
                }
            }
        }
        logger.trace("Cookies are: {}", cookies); //$NON-NLS-1$
        return cookies;
    }

    private static final String VERSION    = "$Version";            //$NON-NLS-1$
    private static final String DOMAIN     = "$Domain";             //$NON-NLS-1$
    private static final String PATH       = "$Path";               //$NON-NLS-1$

    private static class ModifiableCookie {
        public String name;
        public String value;
        public int    version = 0;
        public String path;
        public String domain;
    }

    private List<Cookie> parseCookieHeader(String cookieHeader) {
        String tokens[] = cookieHeader.split("[;,]"); //$NON-NLS-1$

        if (tokens.length <= 0) {
            return Collections.emptyList();
        }

        List<Cookie> cookies = new ArrayList<Cookie>();

        ModifiableCookie currentCookie = null;
        int version = 0;

        for (String token : tokens) {
            String[] subTokens = token.trim().split("=", 2); //$NON-NLS-1$
            String name = subTokens.length > 0 ? subTokens[0] : null;
            String value = subTokens.length > 1 ? subTokens[1] : null;
            if (value != null && value.startsWith("\"") //$NON-NLS-1$
                && value.endsWith("\"") //$NON-NLS-1$
                && value.length() > 1) {
                value = value.substring(1, value.length() - 1);
            }

            if (!name.startsWith("$")) { //$NON-NLS-1$
                // this is the start of a new cookie
                if (currentCookie != null) {
                    if (currentCookie.name != null && currentCookie.value != null) {
                        cookies.add(new Cookie(currentCookie.name, currentCookie.value,
                                               currentCookie.path, currentCookie.domain,
                                               currentCookie.version));
                    }
                }
                currentCookie = new ModifiableCookie();
                currentCookie.name = name;
                currentCookie.value = value;
                currentCookie.version = version;
            } else if (name.startsWith(VERSION)) {
                version = Integer.parseInt(value);
            } else if (name.startsWith(PATH) && currentCookie != null) {
                currentCookie.path = value;
            } else if (name.startsWith(DOMAIN) && currentCookie != null) {
                currentCookie.domain = value;
            }
        }

        if (currentCookie != null) {
            if (currentCookie.name != null && currentCookie.value != null) {
                cookies.add(new Cookie(currentCookie.name, currentCookie.value,
                                       currentCookie.path, currentCookie.domain,
                                       currentCookie.version));
            }
        }
        
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
            logger.trace("Language string is {}", languageStr); //$NON-NLS-1$
            if (languageStr == null) {
                logger.trace("getLanguage() returning null"); //$NON-NLS-1$
                return null;
            }
            String[] locales = StringUtils.fastSplit(languageStr, ","); //$NON-NLS-1$
            language = HeaderUtils.languageToLocale(locales[0].trim());
        }
        logger.trace("getLanguage() returning {}", language); //$NON-NLS-1$
        return language;
    }

    public MediaType getMediaType() {
        if (mediaType == null) {
            String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
            if (contentType == null) {
                List<String> s = getRequestHeaderInternal(HttpHeaders.CONTENT_TYPE);
                if (s == null || s.isEmpty()) {
                    logger.trace("getMediaType() returning null"); //$NON-NLS-1$
                    return null;
                } else {
                    contentType = s.get(0);
                }
            }
            logger.trace("Content-type is {}", contentType); //$NON-NLS-1$
            mediaType = MediaType.valueOf(contentType);
        }
        logger.trace("getMediaType() returning {}", mediaType); //$NON-NLS-1$
        return mediaType;
    }

    private List<String> getRequestHeaderInternal(String name) {
        if (allHeaders != null) {
            List<String> value = allHeaders.get(name);
            logger.trace("Returning {} header value from allHeaders cache: {}", name, value); //$NON-NLS-1$
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
                .trace("HttpServletRequest.getHeaders({}) returned {} so putting into headers cache", //$NON-NLS-1$
                       name,
                       list);
            headers.put(name, list);
        }
        logger.trace("getRequestHeaderInternal({}) returning {}", name, list); //$NON-NLS-1$
        return list;
    }

    public List<String> getRequestHeader(String name) {
        if (name == null) {
            logger.trace("getRequestHeader({}) returns null", name); //$NON-NLS-1$
            return null;
        }
        List<String> list = getRequestHeaderInternal(name);
        if (list == null || list.isEmpty()) {
            logger.trace("getRequestHeader({}) returns null due to empty or non-existent header", //$NON-NLS-1$
                         name);
            return null;
        }
        logger.trace("getRequestHeader({}) returns {}", name, list); //$NON-NLS-1$
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
            logger.trace("buildRequestHeaders() adding {} header with values {}", name, values); //$NON-NLS-1$
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
            if (headers == null || headers.isEmpty()) {
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
