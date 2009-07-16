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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
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

public class HttpHeadersImpl implements HttpHeaders {

    private MessageContext                 msgContext;
    private MultivaluedMap<String, String> headers;
    private List<Locale>                   acceptableLanguages;
    private List<MediaType>                acceptableMediaTypes;
    private Map<String, Cookie>            cookies;
    private Locale                         language;
    private MediaType                      mediaType;

    public HttpHeadersImpl(MessageContext msgContext) {
        this.msgContext = msgContext;
        headers = buildRequestHeaders();
        acceptableLanguages = null;
        acceptableMediaTypes = null;
        cookies = null;
        language = null;
        mediaType = null;
    }

    public List<Locale> getAcceptableLanguages() {
        if (acceptableLanguages == null) {
            List<String> requestHeader = getRequestHeader(HttpHeaders.ACCEPT_LANGUAGE);
            if (requestHeader == null || requestHeader.isEmpty()) {
                acceptableLanguages = new LinkedList<Locale>();
            } else {
                String acceptLanguage = requestHeader.get(0);
                AcceptLanguage acceptLanguages = AcceptLanguage.valueOf(acceptLanguage);
                acceptableLanguages = acceptLanguages.getAcceptableLanguages();
            }
        }
        return acceptableLanguages;
    }

    public List<MediaType> getAcceptableMediaTypes() {
        if (acceptableMediaTypes == null) {
            Accept acceptHeader = getAcceptHeader();
            acceptableMediaTypes = acceptHeader.getSortedMediaTypes();
        }
        return acceptableMediaTypes;
    }

    private Accept getAcceptHeader() {
        String alternateParameter =
            msgContext.getUriInfo().getQueryParameters()
                .getFirst(RestConstants.REST_PARAM_MEDIA_TYPE);
        String acceptValue = null;
        if (alternateParameter != null) {
            // try to map alternate parameter shortcut to a real media type
            DeploymentConfiguration deploymentConfiguration =
                msgContext.getAttribute(DeploymentConfiguration.class);
            Map<String, String> alternateShortcutMap =
                deploymentConfiguration.getAlternateShortcutMap();
            if (alternateShortcutMap != null) {
                acceptValue = alternateShortcutMap.get(alternateParameter);
            }
            if (acceptValue == null) {
                acceptValue = alternateParameter;
            }
        } else {
            List<String> requestHeader = getRequestHeader(HttpHeaders.ACCEPT);
            if (requestHeader == null || requestHeader.isEmpty()) {
                acceptValue = null;
            } else {
                acceptValue = requestHeader.get(0);
            }
        }
        Accept acceptHeader = Accept.valueOf(acceptValue);
        return acceptHeader;
    }

    public Map<String, Cookie> getCookies() {
        if (cookies == null) {
            cookies = new HashMap<String, Cookie>();
            List<String> cookiesHeaders = headers.get(HttpHeaders.COOKIE);
            if (cookiesHeaders != null) {
                for (String cookieHeader : cookiesHeaders) {
                    Cookie cookie = Cookie.valueOf(cookieHeader);
                    cookies.put(cookie.getName(), cookie);
                }
            }
        }
        return cookies;
    }

    public Locale getLanguage() {
        if (language == null) {
            String languageStr = headers.getFirst(HttpHeaders.CONTENT_LANGUAGE);
            if (languageStr == null) {
                return null;
            }
            String[] locales = StringUtils.fastSplit(languageStr, ",");
            language = HeaderUtils.languageToLocale(locales[0].trim());
        }
        return language;
    }

    public MediaType getMediaType() {
        if (mediaType == null) {
            String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
            if (contentType == null) {
                return null;
            }
            mediaType = MediaType.valueOf(contentType);
        }
        return mediaType;
    }

    public List<String> getRequestHeader(String name) {
        List<String> list = headers.get(name);
        if (list == null) {
            return null;
        }
        return Collections.unmodifiableList(list);
    }

    public MultivaluedMap<String, String> getRequestHeaders() {
        return headers;
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
                values.add((String)headerValues.nextElement());
            }
            map.put(name, values);
        }
        return new UnmodifiableMultivaluedMap<String, String>(map);
    }

}
