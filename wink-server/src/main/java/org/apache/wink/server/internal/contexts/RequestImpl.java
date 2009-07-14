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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.http.EntityTagMatchHeader;
import org.apache.wink.server.handlers.MessageContext;



public class RequestImpl implements Request {

    private MessageContext                                    msgContext;
    private static final RuntimeDelegate                      delegate              = RuntimeDelegate.getInstance();
    private static final HeaderDelegate<EntityTagMatchHeader> ifMatchHeaderDelegate = delegate.createHeaderDelegate(EntityTagMatchHeader.class);
    private static final HeaderDelegate<Date>                 dateHeaderDelegate    = delegate.createHeaderDelegate(Date.class);

    public RequestImpl(MessageContext msgContext) {
        this.msgContext = msgContext;
    }

    private String getHeaderValue(String header) {
        List<String> headers = msgContext.getHttpHeaders().getRequestHeader(header);
        if (headers != null && headers.size() > 0 && headers.get(0) != null) {
            return headers.get(0);
        }
        return null;
    }

    public ResponseBuilder evaluatePreconditions(EntityTag tag) {
        String ifMatch = getHeaderValue(HttpHeaders.IF_MATCH);
        if (ifMatch != null) {
            return evaluateIfMatch(tag, ifMatch);
        }
        String ifNoneMatch = getHeaderValue(HttpHeaders.IF_NONE_MATCH);
        if (ifNoneMatch != null) {
            return evaluateIfNoneMatch(tag, ifNoneMatch);
        }
        return null;
    }

    /**
     * returns ResponseBuilder if none of the tags matched
     */
    private ResponseBuilder evaluateIfMatch(EntityTag tag, String headerValue) {
        EntityTagMatchHeader ifMatchHeader = null;
        try {
            ifMatchHeader = ifMatchHeaderDelegate.fromString(headerValue);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        
        if (!ifMatchHeader.isMatch(tag)) {
            // none of the tags matches the etag
            ResponseBuilder responseBuilder = delegate.createResponseBuilder();
            responseBuilder.status(HttpServletResponse.SC_PRECONDITION_FAILED).tag(tag);
            return responseBuilder;
        }
        return null;
    }

    /**
     * returns ResponseBuilder if any of the tags matched
     */
    private ResponseBuilder evaluateIfNoneMatch(EntityTag tag, String headerValue) {
        EntityTagMatchHeader ifNoneMatchHeader = null;
        try {
            ifNoneMatchHeader = ifMatchHeaderDelegate.fromString(headerValue);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        
        if (ifNoneMatchHeader.isMatch(tag)) {
            // some tag matched
            ResponseBuilder responseBuilder = delegate.createResponseBuilder();
            String method = getMethod();
            if (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("HEAD")) {
                responseBuilder.status(HttpServletResponse.SC_NOT_MODIFIED).tag(tag);
            } else {
                responseBuilder.status(HttpServletResponse.SC_PRECONDITION_FAILED).tag(tag);
            }
            return responseBuilder;
        }
        return null;
    }

    public ResponseBuilder evaluatePreconditions(Date lastModified) {
        String ifModifiedSince = getHeaderValue(HttpHeaders.IF_MODIFIED_SINCE);
        if (ifModifiedSince != null) {
            return evaluateIfModifiedSince(lastModified, ifModifiedSince);
        }
        String ifUnmodifiedSince = getHeaderValue(HttpHeaders.IF_UNMODIFIED_SINCE);
        if (ifUnmodifiedSince != null) {
            return evalueateIfUnmodifiedSince(lastModified, ifUnmodifiedSince);
        }
        return null;
    }

    private ResponseBuilder evalueateIfUnmodifiedSince(Date lastModified, String headerValue) {

        Date date = dateHeaderDelegate.fromString(headerValue);
        if (lastModified.after(date)) {
            ResponseBuilder responseBuilder = delegate.createResponseBuilder();
            responseBuilder.status(HttpServletResponse.SC_PRECONDITION_FAILED);
            return responseBuilder;
        }
        return null;
    }

    private ResponseBuilder evaluateIfModifiedSince(Date lastModified, String headerValue) {
        Date date = dateHeaderDelegate.fromString(headerValue);
        if (lastModified.after(date)) {
            return null;
        }
        ResponseBuilder responseBuilder = delegate.createResponseBuilder();
        responseBuilder.status(HttpServletResponse.SC_NOT_MODIFIED);
        return responseBuilder;
    }

    public ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag tag) {

        String ifMatch = getHeaderValue(HttpHeaders.IF_MATCH);
        if (ifMatch != null) {
            return evaluateIfMatch(tag, ifMatch);
        }
        String ifNoneMatch = getHeaderValue(HttpHeaders.IF_NONE_MATCH);
        String ifModifiedSince = getHeaderValue(HttpHeaders.IF_MODIFIED_SINCE);
        if (ifNoneMatch != null) {
            ResponseBuilder isNoneMatch = evaluateIfNoneMatch(tag, ifNoneMatch);
            if (isNoneMatch != null && ifModifiedSince != null
                && evaluateIfModifiedSince(lastModified, ifModifiedSince) == null) {
                // although isNoneMatch is not null, but need to proceed because of IfModifiedSince
                // requires to proceed
                return null;
            }
            return isNoneMatch;
        }

        if (ifModifiedSince != null) {
            return evaluateIfModifiedSince(lastModified, ifModifiedSince);
        }
        String ifUnmodifiedSince = getHeaderValue(HttpHeaders.IF_UNMODIFIED_SINCE);
        if (ifUnmodifiedSince != null) {
            return evalueateIfUnmodifiedSince(lastModified, ifUnmodifiedSince);
        }

        return null;
    }

    public String getMethod() {
        return msgContext.getHttpMethod();
    }

    public Variant selectVariant(List<Variant> variants) throws IllegalArgumentException {
        MediaType inputMediaType = msgContext.getHttpHeaders().getMediaType();
        String inputEncoding = msgContext.getAttribute(HttpServletRequest.class).getCharacterEncoding();
        String inputLanguage = getHeaderValue("Content-Language");
        for (Variant variant : variants) {
            String variantEncoding = variant.getEncoding();
            Locale variantLanguage = variant.getLanguage();
            javax.ws.rs.core.MediaType variantMediaType = variant.getMediaType();
            if (isEncodingEqual(inputEncoding, variantEncoding)
                && isLanguageEqual(inputLanguage, variantLanguage)
                && isMediaTypeEqual(inputMediaType, variantMediaType)) {
                return variant;
            }
        }
        return null;
    }

    private boolean isEncodingEqual(String inputEncoding, String variantEncoding) {
        if (inputEncoding == null && variantEncoding == null) {
            return true;
        }

        if ((inputEncoding == null && variantEncoding != null)
            || (inputEncoding != null && variantEncoding == null)) {
            return false;
        }

        return variantEncoding.equals(inputEncoding.toString());
    }

    private boolean isLanguageEqual(String inputLanguage, Locale variantLanguage) {
        if (inputLanguage == null && variantLanguage == null) {
            return true;
        }

        if ((inputLanguage == null && variantLanguage != null)
            || (inputLanguage != null && variantLanguage == null)) {
            return false;
        }

        return variantLanguage.getLanguage().equalsIgnoreCase(inputLanguage.toString());
    }

    private boolean isMediaTypeEqual(MediaType inputMediaType, MediaType variantMediaType) {
        if (inputMediaType == null && variantMediaType == null) {
            return true;
        }

        if ((inputMediaType == null && variantMediaType != null)
            || (inputMediaType != null && variantMediaType == null)) {
            return false;
        }

        return variantMediaType.toString().equals(inputMediaType.toString());
    }

}
