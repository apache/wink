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

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

public class ResponseImpl extends Response {

    private static final int               DEFAULT_STATUS = -1;

    private Object                         entity;
    private int                            status         = DEFAULT_STATUS;
    private MultivaluedMap<String, Object> metadata;

    private ResponseImpl(int status, Object entity, MultivaluedMap<String, Object> metadata) {
        this.status = status;
        this.entity = entity;
        this.metadata = metadata;
    }

    @Override
    public Object getEntity() {
        return entity;
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public int getStatus() {
        return status;
    }

    public static class ResponseBuilderImpl extends ResponseBuilder {

        private Object                         entity;
        private int                            status;
        private MultivaluedMap<String, Object> metadata;

        public ResponseBuilderImpl() {
            super();
            metadata = new MultivaluedMapImpl<String, Object>();
            status = DEFAULT_STATUS;

        }

        private ResponseBuilderImpl(int status, Object entity,
            MultivaluedMap<String, Object> metadata) {
            super();
            this.status = status;
            this.entity = entity;
            this.metadata = metadata;
        }

        @Override
        public Response build() {
            return new ResponseImpl(status, entity, metadata);
        }

        @Override
        public ResponseBuilder cacheControl(CacheControl cacheControl) {
            return putSingleRemoveNull(HttpHeaders.CACHE_CONTROL, cacheControl);
        }

        @Override
        public ResponseBuilder clone() {
            return new ResponseBuilderImpl(status, entity, metadata);
        }

        @Override
        public ResponseBuilder contentLocation(URI location) {
            return putSingleRemoveNull(HttpHeaders.CONTENT_LOCATION, location);
        }

        @Override
        public ResponseBuilder cookie(NewCookie... cookies) {
            if (cookies == null)
                metadata.remove(HttpHeaders.SET_COOKIE);
            else {
                for (NewCookie cooky : cookies)
                    metadata.add(HttpHeaders.SET_COOKIE, cooky);
            }
            return this;
        }

        @Override
        public ResponseBuilder entity(Object entity) {
            this.entity = entity;
            return this;
        }

        @Override
        public ResponseBuilder expires(Date expires) {
            return putSingleRemoveNull(HttpHeaders.EXPIRES, expires);
        }

        @Override
        public ResponseBuilder header(String name, Object value) {
            return putSingleRemoveNull(name, value);
        }

        @Override
        public ResponseBuilder language(String language) {
            return putSingleRemoveNull(HttpHeaders.CONTENT_LANGUAGE, language);
        }

        @Override
        public ResponseBuilder language(Locale language) {
            return putSingleRemoveNull(HttpHeaders.CONTENT_LANGUAGE, language);
        }

        @Override
        public ResponseBuilder lastModified(Date lastModified) {
            return putSingleRemoveNull(HttpHeaders.LAST_MODIFIED, lastModified);
        }

        @Override
        public ResponseBuilder location(URI location) {
            return putSingleRemoveNull(HttpHeaders.LOCATION, location);
        }

        @Override
        public ResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        @Override
        public ResponseBuilder tag(EntityTag tag) {
            return putSingleRemoveNull(HttpHeaders.ETAG, tag);
        }

        @Override
        public ResponseBuilder tag(String tag) {
            return putSingleRemoveNull(HttpHeaders.ETAG, tag);
        }

        @Override
        public ResponseBuilder type(MediaType type) {
            return putSingleRemoveNull(HttpHeaders.CONTENT_TYPE, type);
        }

        @Override
        public ResponseBuilder type(String type) {
            return putSingleRemoveNull(HttpHeaders.CONTENT_TYPE, type);
        }

        @Override
        public ResponseBuilder variant(Variant variant) {
            if (variant != null) {
                language(variant.getLanguage());
                putSingleRemoveNull(HttpHeaders.CONTENT_ENCODING, variant.getEncoding());
                type(variant.getMediaType());
            } else {

                putSingleRemoveNull(HttpHeaders.CONTENT_LANGUAGE, null);
                putSingleRemoveNull(HttpHeaders.CONTENT_ENCODING, null);
                putSingleRemoveNull(HttpHeaders.CONTENT_TYPE, null);
            }
            return this;
        }

        @Override
        public ResponseBuilder variants(List<Variant> variants) {
            if (variants == null) {
                putSingleRemoveNull(HttpHeaders.VARY, null);
                return this;
            }
            boolean encoding = false;
            boolean lang = false;
            boolean medyatype = false;

            for (Variant v : variants) {
                encoding = encoding || (v.getEncoding() != null);
                lang = lang || (v.getLanguage() != null);
                medyatype = medyatype || (v.getMediaType() != null);
            }
            StringBuilder sb = new StringBuilder();
            conditionalAppend(sb, encoding, HttpHeaders.CONTENT_ENCODING);
            conditionalAppend(sb, lang, HttpHeaders.ACCEPT_LANGUAGE);
            conditionalAppend(sb, medyatype, HttpHeaders.CONTENT_TYPE);
            putSingleRemoveNull(HttpHeaders.VARY, sb.toString());
            return this;
        }

        ///////////////////// helper methods ////////////////////////
        public ResponseBuilder putSingleRemoveNull(String key, Object value) {
            if (value == null)
                metadata.remove(key);
            else
                metadata.putSingle(key, value);
            return this;
        }

        private void conditionalAppend(StringBuilder sb, boolean b, String s) {
            if (b) {
                if (sb.length() > 0)
                    sb.append(",");
                sb.append(s);
            }
        }

    }

}
