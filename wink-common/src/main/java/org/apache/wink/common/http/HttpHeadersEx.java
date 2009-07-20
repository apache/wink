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

package org.apache.wink.common.http;

/**
 * List of HTTP headers.
 */
public final class HttpHeadersEx {

    private HttpHeadersEx() {
        // no instances
    }

    public static final String ALLOW                  = "Allow";

    // public static final String ACCEPT = "Accept";
    // public static final String ACCEPT_LANGUAGE = "Accept-Language";
    // public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String ACCEPT_RANGES          = "Accept-Ranges";
    public static final String CONTENT_DISPOSITION    = "Content-Disposition";
    // public static final String CONTENT_LANG = "Content-Language";
    // public static final String CONTENT_TYPE = "Content-Type";
    // public static final String ETAG = "ETag";
    // public static final String EXPIRES = "Expires";
    // public static final String IF_MATCH = "If-Match";
    // public static final String IF_NONE_MATCH = "If-None-Match";
    // public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    // public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    // public static final String LAST_MODIFIED = "Last-Modified";
    // public static final String LOCATION = "Location";
    // public static final String USER_AGENT = "User-Agent";
    // public static final String VARY = "Vary";

    // --- non-standards ---

    /**
     * Non RFC 2616 - Atom Publishing Protocol
     */
    public static final String SLUG                   = "Slug";

    /**
     * Non RFC 2616 - Google Doc convention
     */
    public static final String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";

    /**
     * Non RFC 2616 - Some client implementing Google Doc
     */
    public static final String X_METHOD_OVERRIDE      = "X-Method-Override";

}
