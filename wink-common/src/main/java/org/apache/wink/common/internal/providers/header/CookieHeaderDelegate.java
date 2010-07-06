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
package org.apache.wink.common.internal.providers.header;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.i18n.Messages;

public class CookieHeaderDelegate implements HeaderDelegate<Cookie> {

    private static Pattern      whitespace = Pattern.compile("\\s"); //$NON-NLS-1$
    private static final String VERSION    = "$Version"; //$NON-NLS-1$
    private static final String DOMAIN     = "$Domain"; //$NON-NLS-1$
    private static final String PATH       = "$Path"; //$NON-NLS-1$

    public Cookie fromString(String cookie) throws IllegalArgumentException {

        if (cookie == null) {
            throw new IllegalArgumentException(Messages.getMessage("headerIsNull", "Cookie")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        String tokens[] = cookie.split("[;,]"); //$NON-NLS-1$

        if (tokens.length <= 0) {
            throw new IllegalArgumentException(Messages.getMessage("invalidCookie", cookie)); //$NON-NLS-1$
        }

        ModifiableCookie firstCookie = null;
        int cookieNum = 0;
        boolean versionSet = false;
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
                cookieNum++;
                if (cookieNum > 1) {
                    // Return only first Cookie
                    break;
                }
                if (firstCookie == null) {
                    firstCookie = new ModifiableCookie();
                }
                firstCookie.name = name;
                firstCookie.value = value;
            } else if (name.startsWith(VERSION)) {
                if (firstCookie == null) {
                    firstCookie = new ModifiableCookie();
                } else {
                    if (versionSet) {
                        throw new IllegalArgumentException(
                                                           Messages.getMessage("cookieCannotContainAddlVers", cookie)); //$NON-NLS-1$
                    }
                }
                versionSet = true;
                firstCookie.version = Integer.parseInt(value);
            } else if (name.startsWith(PATH) && cookie != null) {
                if (firstCookie == null) {
                    throw new IllegalArgumentException(Messages.getMessage("cookieMustStartWithVersion", cookie)); //$NON-NLS-1$
                }
                firstCookie.path = value;
            } else if (name.startsWith(DOMAIN) && cookie != null) {
                if (firstCookie == null) {
                    throw new IllegalArgumentException(Messages.getMessage("cookieMustStartWithVersion", cookie)); //$NON-NLS-1$
                }
                firstCookie.domain = value;
            }
        }
        if (cookieNum == 0) {
            throw new IllegalArgumentException(Messages.getMessage("cookieDoesNotContainNAMEVALUE", cookie)); //$NON-NLS-1$
        }

        if (firstCookie != null) {
            return validateAndBuildCookie(firstCookie, cookie);
        } else {
            throw new IllegalArgumentException(Messages.getMessage("failedToParseCookie", cookie)); //$NON-NLS-1$
        }
    }

    private Cookie validateAndBuildCookie(ModifiableCookie firstCookie, String cookie) {
        if (firstCookie.name == null || firstCookie.value == null) {
            throw new IllegalArgumentException(Messages.getMessage("failedToParseCookie", cookie)); //$NON-NLS-1$
        }
        return new Cookie(firstCookie.name, firstCookie.value, firstCookie.path,
                          firstCookie.domain, firstCookie.version);

    }

    public String toString(Cookie cookie) {
        if (cookie == null) {
            throw new IllegalArgumentException(Messages.getMessage("headerIsNull", "Cookie")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        StringBuilder cookieHeader = new StringBuilder();

        cookieHeader.append(VERSION + "=").append(cookie.getVersion()).append(';'); //$NON-NLS-1$
        cookieHeader.append(cookie.getName()).append('=');
        appendValue(cookieHeader, cookie.getValue());

        if (cookie.getDomain() != null) {
            cookieHeader.append(";" + DOMAIN + "="); //$NON-NLS-1$ //$NON-NLS-2$
            appendValue(cookieHeader, cookie.getDomain());
        }
        if (cookie.getPath() != null) {
            cookieHeader.append(";" + PATH + "="); //$NON-NLS-1$ //$NON-NLS-2$
            appendValue(cookieHeader, cookie.getPath());
        }
        return cookieHeader.toString();
    }

    private void appendValue(StringBuilder cookieHeader, String value) {

        Matcher matcher = whitespace.matcher(value);
        boolean isQuote = matcher.find();
        if (isQuote) {
            cookieHeader.append('"');
        }
        escapeQuotes(cookieHeader, value);
        if (isQuote) {
            cookieHeader.append('"');
        }
    }

    private void escapeQuotes(StringBuilder b, String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"')
                b.append('\\');
            b.append(c);
        }
    }

    private static class ModifiableCookie {

        public String name;
        public String value;
        public int    version = 0;
        public String path;
        public String domain;
    }
}
