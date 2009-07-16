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
package org.apache.wink.common.internal.utils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.uri.UriEncoder;

public class MediaTypeUtils {

    public static final String         ATOM_SERVICE_DOCUMENT         = "application/atomsvc+xml";
    public static final MediaType      ATOM_SERVICE_DOCUMENT_TYPE    =
                                                                         new MediaType(
                                                                                       "application",
                                                                                       "atomsvc+xml");
    public static final String         ATOM_CATEGORIES_DOCUMENT      = "application/atomcat+xml";
    public static final MediaType      ATOM_CATEGORIES_DOCUMENT_TYPE =
                                                                         new MediaType(
                                                                                       "application",
                                                                                       "atomcat+xml");
    public static final String         ATOM_ENTRY                    =
                                                                         "application/atom+xml;type=entry";
    public static final MediaType      ATOM_ENTRY_TYPE               =
                                                                         new MediaType(
                                                                                       "application",
                                                                                       "atom+xml",
                                                                                       Collections
                                                                                           .singletonMap("type",
                                                                                                         "entry"));

    public static final String         ATOM_FEED                     =
                                                                         "application/atom+xml;type=feed";
    public static final MediaType      ATOM_FEED_TYPE                =
                                                                         new MediaType(
                                                                                       "application",
                                                                                       "atom+xml",
                                                                                       Collections
                                                                                           .singletonMap("type",
                                                                                                         "feed"));

    public static final MediaType      IMAGE_X_ICON                  =
                                                                         new MediaType("image",
                                                                                       "x-icon");
    public static final MediaType      IMAGE_PNG                     =
                                                                         new MediaType("image",
                                                                                       "png");
    public static final MediaType      IMAGE_VND                     =
                                                                         new MediaType("image",
                                                                                       "vnd.microsoft.icon");
    public static final String         IMAGE_JPEG                    = "image/jpeg";
    public static final MediaType      IMAGE_JPEG_TYPE               =
                                                                         new MediaType("image",
                                                                                       "jpeg");

    public static final String         UNKNOWN                       = "x-internal/unknown";

    public static final MediaType      UNKNOWN_TYPE                  =
                                                                         new MediaType(
                                                                                       "x-internal",
                                                                                       "unknown");

    public static final String         OPENSEARCH                    =
                                                                         "application/opensearchdescription+xml";
    public static final MediaType      OPENSEARCH_TYPE               =
                                                                         new MediaType(
                                                                                       "application",
                                                                                       "opensearchdescription+xml");

    public static final String         JAVASCRIPT                    = "application/javascript";

    public static final MediaType      JAVASCRIPT_TYPE               =
                                                                         new MediaType(
                                                                                       "application",
                                                                                       "javascript");

    public static final String         ECMASCRIPT                    = "application/ecmascript";
    public static final MediaType      ECMASCRIPT_TYPE               =
                                                                         new MediaType(
                                                                                       "application",
                                                                                       "ecmascript");

    public static final String         TEXT_ECMASCRIPT               = "text/ecmascript";
    public static final MediaType      TEXT_ECMASCRIPT_TYPE          =
                                                                         new MediaType("text",
                                                                                       "ecmascript");
    public static final String         TEXT_JAVASCRIPT               = "text/javascript";

    public static final MediaType      TEXT_JAVASCRIPT_TYPE          =
                                                                         new MediaType("text",
                                                                                       "javascript");

    public static final MediaType      CSV                           = new MediaType("text", "csv");
    public static final String         PDF                           = "application/pdf";
    public static final MediaType      PDF_TYPE                      =
                                                                         new MediaType(
                                                                                       "application",
                                                                                       "pdf");
    public static final String         ZIP                           = "application/zip";
    public static final MediaType      ZIP_TYPE                      =
                                                                         new MediaType(
                                                                                       "application",
                                                                                       "zip");

    public static final MediaType      MS_WORD                       =
                                                                         new MediaType(
                                                                                       "application",
                                                                                       "msword");
    public static final MediaType      MS_EXCEL                      =
                                                                         new MediaType(
                                                                                       "application",
                                                                                       "vnd.ms-excel");
    public static final MediaType      MS_PPT                        =
                                                                         new MediaType(
                                                                                       "application",
                                                                                       "vnd.ms-powerpoint");

    public static final Set<MediaType> JSON_TYPES                    = createJsonTypes();

    private static Set<MediaType> createJsonTypes() {
        Set<MediaType> result = new LinkedHashSet<MediaType>();
        result.add(MediaType.APPLICATION_JSON_TYPE);
        result.add(JAVASCRIPT_TYPE);
        result.add(ECMASCRIPT_TYPE);
        result.add(TEXT_JAVASCRIPT_TYPE);
        result.add(TEXT_ECMASCRIPT_TYPE);
        return Collections.unmodifiableSet(result);
    }

    public static boolean isXmlType(MediaType mediaType) {
        return (MediaTypeUtils.equalsIgnoreParameters(mediaType, MediaType.TEXT_XML_TYPE) || MediaTypeUtils
            .equalsIgnoreParameters(mediaType, MediaType.APPLICATION_XML_TYPE) || (mediaType
            .getType().equals("application") && mediaType.getSubtype().endsWith("+xml")));
    }

    public static MediaType clone(MediaType mt) {
        return new MediaType(mt.getType(), mt.getSubtype(), mt.getParameters());
    }

    public static String toEncodedString(MediaType mediaType) {
        // TODO: optimize with cache?
        return UriEncoder.encodeString(mediaType.toString());
    }

    /**
     * The method returns true if m1 is compatible to m2.
     * However, the method doesn't check that m2 is compatible to m1.
     * 
     * For example: isCompatibleNonCommutative("text/*", "text/plain") returns true,
     * but isCompatibleNonCommutative("text/plain", "text/*") returns false
     * @param m1
     * @param m2
     * @return
     */
    public static boolean isCompatibleNonCommutative(MediaType m1, MediaType m2) {
        if (m1 == m2) {
            return true;
        }
        if (m1 == null || m2 == null) {
            return false;
        }
        if (m1.getType().equals(MediaType.MEDIA_TYPE_WILDCARD))
            return true;
        else if (m1.getType().equalsIgnoreCase(m2.getType()) && (m1.getSubtype().equals(MediaType.MEDIA_TYPE_WILDCARD)))
            return true;
        else
            return m1.getType().equalsIgnoreCase(m2.getType()) && m1.getSubtype()
                .equalsIgnoreCase(m2.getSubtype());
    }

    public static boolean equalsIgnoreParameters(MediaType m1, MediaType m2) {
        if (m1 == m2) {
            return true;
        }
        if (m1 == null || m2 == null) {
            return false;
        }
        return (m1.getType().equalsIgnoreCase(m2.getType()) && m1.getSubtype().equalsIgnoreCase(m2
            .getSubtype()));
    }

    /**
     * compares according to the rule: n/m > n/* > /*
     */
    public static int compareTo(MediaType m1, MediaType m2) {
        if (m1 == m2) {
            return 0;
        }
        return compareTypeAndSubType(m1, m2);
    }

    private static int compareTypeAndSubType(MediaType m1, MediaType m2) {
        int ret = compareSubType(m1, m2);
        if (ret != 0) {
            return ret;
        }
        return compareType(m1, m2);
    }

    private static int compareType(MediaType m1, MediaType m2) {
        return compareMediaPart(m1.getType(), m2.getType());
    }

    private static int compareSubType(MediaType m1, MediaType m2) {
        return compareMediaPart(m1.getSubtype(), m2.getSubtype());
    }

    private static int compareMediaPart(String part1, String part2) {
        if (part1.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
            if (part2.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
                return 0;
            }
            return -1;
        } else if (part2.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
            return 1;
        }
        return 0;
    }

}
