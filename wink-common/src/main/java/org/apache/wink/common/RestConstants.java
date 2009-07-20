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

package org.apache.wink.common;

/**
 * Constants to be used in REST and REST client components.
 */
public final class RestConstants {

    private RestConstants() {
        // no instances
    }

    // --- namespaces ---
    public static final String NAMESPACE_XML                    =
                                                                    "http://www.w3.org/XML/1998/namespace";

    public static final String NAMESPACE_XHTML                  = "http://www.w3.org/1999/xhtml";
    public static final String XHTML_PREFIX                     = "xhtml";

    public static final String NAMESPACE_ATOM                   = "http://www.w3.org/2005/Atom";
    public static final String ATOM_PREFIX                      = "atom";

    public static final String NAMESPACE_APP                    = "http://www.w3.org/2007/app";
    public static final String APP_PREFIX                       = "app";

    // OpenSearch
    public static final String NAMESPACE_OPENSEARCH             =
                                                                    "http://a9.com/-/spec/opensearch/1.1/";
    public static final String OPENSEARCH_PREFIX                = "opensearch";

    // --- charset ---

    public static final String CHARSET_PARAM                    = "charset";

    // charset encodings
    public static final String CHARACTER_ENCODING_UTF_8         = "UTF-8";

    // --- REST query parameters ---

    /**
     * Overrides Accept-Header with one type.
     */
    public static final String REST_PARAM_MEDIA_TYPE            = "alt";

    /**
     * JSON will be encloses as parameter to the value - method name.
     */
    public static final String REST_PARAM_JSON_CALLBACK         = "callback";

    // paging

    /**
     * The first item to include in the result page.
     */
    public static final String REST_PARAM_PAGING_START          = "start-index";

    /**
     * Number of elements per page.
     */
    public static final String REST_PARAM_PAGING_SIZE           = "page-size";

    /**
     * Special value of page-size.
     */
    public static final String REST_PARAM_PAGING_SIZE_UNLIMITED = "unlimited";

    /**
     * Specify filtering by category.
     */
    public static final String REST_PARAM_CATEGORY              = "category";

    /**
     * Specified collection ordering.
     */
    public static final String REST_PARAM_ORDER_BY              = "order-by";

    /**
     * Filter by author parameter.
     */
    public static final String REST_PARAM_AUTHOR                = "author";

    /**
     * Filter by entry-id.
     */
    public static final String REST_PARAM_ENTRY_ID              = "entry-id";

    /**
     * Filter by custom query.
     */
    public static final String REST_PARAM_QUERY                 = "query";

    // relative / absolute URLs

    /**
     * Links in response should be relative.
     */
    public static final String REST_PARAM_RELATIVE_URLS         = "relative-urls";

    /**
     * Links in response should be absolute.
     */
    public static final String REST_PARAM_ABSOLUTE_URLS         = "absolute-urls";

}
