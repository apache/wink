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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

import org.apache.wink.common.internal.http.AcceptEncoding;
import org.apache.wink.common.internal.http.AcceptLanguage;
import org.apache.wink.common.internal.http.EntityTagMatchHeader;
import org.apache.wink.server.handlers.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestImpl implements Request {
    private static final Logger                               logger                =
                                                                                        LoggerFactory
                                                                                            .getLogger(RequestImpl.class);

    private MessageContext                                    msgContext;
    private static final RuntimeDelegate                      delegate              =
                                                                                        RuntimeDelegate
                                                                                            .getInstance();
    private static final HeaderDelegate<EntityTagMatchHeader> ifMatchHeaderDelegate =
                                                                                        delegate
                                                                                            .createHeaderDelegate(EntityTagMatchHeader.class);
    private static final HeaderDelegate<Date>                 dateHeaderDelegate    =
                                                                                        delegate
                                                                                            .createHeaderDelegate(Date.class);

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
        logger.debug("evaluatePreconditions({}) called", tag);
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
        logger.debug("evaluateIfMatch({}, {}) called", tag, headerValue);
        EntityTagMatchHeader ifMatchHeader = null;
        try {
            ifMatchHeader = ifMatchHeaderDelegate.fromString(headerValue);
            logger.debug("ifMatchHeaderDelegate returned {}", ifMatchHeader);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }

        if (!ifMatchHeader.isMatch(tag)) {
            // none of the tags matches the etag
            ResponseBuilder responseBuilder = delegate.createResponseBuilder();
            responseBuilder.status(HttpServletResponse.SC_PRECONDITION_FAILED).tag(tag);
            logger.debug("evaluateIfMatch returning built response because there was no match");
            return responseBuilder;
        }
        logger.debug("evaluateIfMatch returning null because there was a match");
        return null;
    }

    /**
     * returns ResponseBuilder if any of the tags matched
     */
    private ResponseBuilder evaluateIfNoneMatch(EntityTag tag, String headerValue) {
        logger.debug("evaluateIfNoneMatch({}, {}) called", tag, headerValue);
        EntityTagMatchHeader ifNoneMatchHeader = null;
        try {
            ifNoneMatchHeader = ifMatchHeaderDelegate.fromString(headerValue);
            logger.debug("ifMatchHeaderDelegate returned {}", ifNoneMatchHeader);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }

        if (ifNoneMatchHeader.isMatch(tag)) {
            // some tag matched
            ResponseBuilder responseBuilder = delegate.createResponseBuilder();
            String method = getMethod();
            if (method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("HEAD")) {
                logger
                    .debug("evaluateIfNoneMatch returning 304 Not Modified because the {} method matched",
                           method);
                responseBuilder.status(HttpServletResponse.SC_NOT_MODIFIED).tag(tag);
            } else {
                logger
                    .debug("evaluateIfNoneMatch returning 412 Precondition Failed because the {} method matched",
                           method);
                responseBuilder.status(HttpServletResponse.SC_PRECONDITION_FAILED).tag(tag);
            }
            return responseBuilder;
        }
        logger.debug("evaluateIfNoneMatch returning null because there was no match");
        return null;
    }

    public ResponseBuilder evaluatePreconditions(Date lastModified) {
        if (logger.isDebugEnabled()) {
            logger.debug("evaluatePreconditions({}) called with {} date",
                         lastModified,
                         lastModified.getTime());
        }
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
        if (logger.isDebugEnabled()) {
            logger
                .debug("evalueateIfUnmodifiedSince({}, {}) got Date {} from header so comparing {} is after {}",
                       new Object[] {lastModified, headerValue, date, lastModified.getTime(),
                           date.getTime()});
        }
        if (lastModified.after(date)) {
            ResponseBuilder responseBuilder = delegate.createResponseBuilder();
            responseBuilder.status(HttpServletResponse.SC_PRECONDITION_FAILED);
            logger.debug("evalueateIfUnmodifiedSince returning 412 Precondition Failed");
            return responseBuilder;
        }
        logger.debug("evalueateIfUnmodifiedSince returning null");
        return null;
    }

    private ResponseBuilder evaluateIfModifiedSince(Date lastModified, String headerValue) {
        Date date = dateHeaderDelegate.fromString(headerValue);
        if (logger.isDebugEnabled()) {
            logger
                .debug("evaluateIfModifiedSince({}, {}) got Date {} from header so comparing {} is after {}",
                       new Object[] {lastModified, headerValue, date, lastModified.getTime(),
                           date.getTime()});
        }
        if (lastModified.after(date)) {
            logger.debug("evaluateIfModifiedSince returning null");
            return null;
        }
        ResponseBuilder responseBuilder = delegate.createResponseBuilder();
        responseBuilder.status(HttpServletResponse.SC_NOT_MODIFIED);
        logger.debug("evaluateIfModifiedSince returning 304 Not Modified");
        return responseBuilder;
    }

    public ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag tag) {
        if (logger.isDebugEnabled()) {
            logger.debug("evaluatePreconditions({}, {}) called with date {} as a long type",
                         new Object[] {lastModified, tag, lastModified.getTime()});
        }
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
                // although isNoneMatch is not null, but need to proceed because
                // of IfModifiedSince
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
        logger.debug("selectVariant({}) called", variants);
        if (variants == null) {
            throw new IllegalArgumentException();
        }

        if (variants.size() == 0) {
            logger.debug("No variants so returning null");
            return null;
        }

        // algorithm is based on Apache Content Negotiation algorithm with
        // slight modifications
        // http://httpd.apache.org/docs/2.0/content-negotiation.html

        // eliminate all Accept* variants that are not acceptable
        List<MediaType> acceptableMediaTypes =
            msgContext.getHttpHeaders().getAcceptableMediaTypes();

        List<String> acceptableLanguages =
            msgContext.getHttpHeaders().getRequestHeader(HttpHeaders.ACCEPT_LANGUAGE);
        AcceptLanguage languages = null;
        if (acceptableLanguages != null) {
            StringBuilder acceptLanguageTemp = new StringBuilder();
            acceptLanguageTemp.append(acceptableLanguages.get(0));
            for (int c = 1; c < acceptableLanguages.size(); ++c) {
                acceptLanguageTemp.append(",");
                acceptLanguageTemp.append(acceptableLanguages.get(c));
            }
            String acceptLanguage = acceptLanguageTemp.toString();
            languages = AcceptLanguage.valueOf(acceptLanguage);
        }

        List<String> acceptableEncodings =
            msgContext.getHttpHeaders().getRequestHeader(HttpHeaders.ACCEPT_ENCODING);
        AcceptEncoding encodings = null;
        if (acceptableEncodings != null) {
            StringBuilder acceptEncodingsTemp = new StringBuilder();
            acceptEncodingsTemp.append(acceptableEncodings.get(0));
            for (int c = 1; c < acceptableEncodings.size(); ++c) {
                acceptEncodingsTemp.append(",");
                acceptEncodingsTemp.append(acceptableEncodings.get(c));
            }
            String acceptEncodings = acceptEncodingsTemp.toString();
            encodings = AcceptEncoding.valueOf(acceptEncodings);
        }

        VariantQChecked bestVariant = null;
        boolean isIdentityEncodingChecked = false;

        for (Iterator<Variant> iter = variants.iterator(); iter.hasNext();) {
            double acceptQFactor = -1.0d;
            Variant v = iter.next();
            logger.debug("Variant being evaluated is: {}", v);
            MediaType vMediaType = v.getMediaType();
            if (vMediaType != null && acceptableMediaTypes != null) {
                boolean isCompatible = false;
                boolean isAcceptable = true; // explicitly denied by the client
                for (MediaType mt : acceptableMediaTypes) {
                    logger.debug("Checking variant media type {} against Accept media type {}",
                                 vMediaType,
                                 mt);
                    if (mt.isCompatible(vMediaType)) {
                        Map<String, String> params = mt.getParameters();
                        String q = params.get("q");
                        if (q != null) {
                            try {
                                Double qAsDouble = Double.valueOf(q);
                                if (qAsDouble.equals(0.0)) {
                                    isAcceptable = false;
                                    logger
                                        .debug("Accept Media Type: {} is NOT compatible with q-factor {}",
                                               mt,
                                               qAsDouble);
                                    break;
                                }
                                acceptQFactor = qAsDouble;
                            } catch (NumberFormatException e) {
                                logger
                                    .debug("NumberFormatException during MediaType q-factor evaluation: {}",
                                           e);
                            }
                        } else {
                            acceptQFactor = 1.0d;
                        }

                        isCompatible = true;
                        logger.debug("Accept Media Type: {} is compatible with q-factor {}",
                                     mt,
                                     acceptQFactor);
                        break;
                    }
                }
                if (!isCompatible || !isAcceptable) {
                    logger.debug("Variant {} is not compatible or not acceptable", vMediaType);
                    continue;
                }
            }

            if (bestVariant != null) {
                if (acceptQFactor < bestVariant.acceptMediaTypeQFactor) {
                    logger
                        .debug("Best variant's media type {} q-factor {} is greater than current variant {} q-factor {}",
                               new Object[] {bestVariant.variant,
                                   bestVariant.acceptMediaTypeQFactor, vMediaType, acceptQFactor});
                    continue;
                }
            }

            double acceptLanguageQFactor = -1.0d;
            Locale vLocale = v.getLanguage();
            if (vLocale != null && languages != null) {
                boolean isCompatible = false;
                logger.debug("Checking variant locale {}", vLocale);
                if (languages.getBannedLanguages().contains(vLocale)) {
                    logger.debug("Variant locale {} was in unacceptable languages", vLocale);
                    continue;
                }
                for (AcceptLanguage.ValuedLocale locale : languages.getValuedLocales()) {
                    logger
                        .debug("Checking against Accept-Language locale {} with quality factor {}",
                               locale.locale,
                               locale.qValue);
                    if (locale.isWildcard() || vLocale.equals(locale.locale)) {
                        logger.debug("Locale is compatible {}", locale.locale);
                        isCompatible = true;
                        acceptLanguageQFactor = locale.qValue;
                        break;
                    }
                }
                if (!isCompatible) {
                    logger.debug("Variant locale is not compatible {}", vLocale);
                    continue;
                }
            }

            if (bestVariant != null) {
                if (acceptLanguageQFactor < bestVariant.acceptLanguageQFactor) {
                    logger
                        .debug("Best variant's language {} q-factor {} is greater than current variant {} q-factor {}",
                               new Object[] {bestVariant.variant,
                                   bestVariant.acceptLanguageQFactor, v, acceptLanguageQFactor});
                    continue;
                }
            }

            double acceptEncodingQFactor = -1.0d;
            String vEncoding = v.getEncoding();
            if (vEncoding != null) {
                logger.debug("Checking variant encoding {}", vEncoding);
                if (encodings == null || encodings.isAnyEncodingAllowed()) {
                    logger.debug("Accept-Encoding is null or wildcard");
                    if (!v.getEncoding().equalsIgnoreCase("identity")) {
                        logger
                            .debug("Variant encoding {} does not equal identity so not acceptable",
                                   vEncoding);
                        // if there is no Accept Encoding, only identity is
                        // acceptable
                        // mark that identity encoding was checked so that the
                        // Vary header has Accept-Encoding added appropriately
                        isIdentityEncodingChecked = true;
                        continue;
                    }
                } else {
                    boolean isAcceptable = true;
                    for (String encoding : encodings.getBannedEncodings()) {
                        logger.debug("Checking against not acceptable encoding: {}", encoding);
                        if (encoding.equalsIgnoreCase(vEncoding)) {
                            logger.debug("Encoding was not acceptable: {}", vEncoding);
                            isAcceptable = false;
                            break;
                        }
                    }
                    if (!isAcceptable) {
                        continue;
                    }

                    boolean isCompatible = false;
                    for (AcceptEncoding.ValuedEncoding encoding : encodings.getValuedEncodings()) {
                        logger.debug("Checking against acceptable encoding: {}", encoding.encoding);
                        if (encoding.isWildcard() || encoding.encoding.equalsIgnoreCase(vEncoding)) {
                            isCompatible = true;
                            acceptEncodingQFactor = encoding.qValue;
                            logger.debug("Encoding {} was acceptable with q-factor {}",
                                         encoding.encoding,
                                         encoding.qValue);
                            break;
                        }
                    }
                    if (!isCompatible) {
                        logger.debug("Variant encoding {} was not compatible", vEncoding);
                        continue;
                    }
                }
            }

            if (bestVariant != null) {
                if (acceptEncodingQFactor < bestVariant.acceptEncodingQFactor) {
                    logger
                        .debug("Best variant's encoding {} q-factor {} is greater than current variant {} q-factor {}",
                               new Object[] {bestVariant.variant,
                                   bestVariant.acceptEncodingQFactor, v, acceptEncodingQFactor});
                    continue;
                }
            }

            bestVariant =
                new VariantQChecked(v, acceptQFactor, acceptLanguageQFactor, acceptEncodingQFactor);
        }

        if (bestVariant == null) {
            return null;
        }

        StringBuilder varyHeaderValue = new StringBuilder();
        boolean isValueWritten = false;
        if (bestVariant.acceptMediaTypeQFactor > 0) {
            varyHeaderValue.append(HttpHeaders.ACCEPT);
            isValueWritten = true;
        }
        if (bestVariant.acceptLanguageQFactor > 0) {
            if (isValueWritten) {
                varyHeaderValue.append(", ");
            }
            varyHeaderValue.append(HttpHeaders.ACCEPT_LANGUAGE);
            isValueWritten = true;
        }
        if (isIdentityEncodingChecked || bestVariant.acceptEncodingQFactor > 0) {
            if (isValueWritten) {
                varyHeaderValue.append(", ");
            }
            varyHeaderValue.append(HttpHeaders.ACCEPT_ENCODING);
            isValueWritten = true;
        }
        String varyHeaderValueStr = varyHeaderValue.toString().trim();
        logger.debug("Vary Header value should be set to {}", varyHeaderValueStr);
        msgContext.setAttribute(RequestImpl.VaryHeader.class, new VaryHeader(varyHeaderValueStr));
        return bestVariant.variant;
    }

    private static class VariantQChecked {
        final Variant variant;
        final double  acceptMediaTypeQFactor;
        final double  acceptLanguageQFactor;
        final double  acceptEncodingQFactor;

        public VariantQChecked(Variant v,
                               double acceptMediaType,
                               double acceptLanguage,
                               double acceptEncoding) {
            this.variant = v;
            this.acceptMediaTypeQFactor = acceptMediaType;
            this.acceptLanguageQFactor = acceptLanguage;
            this.acceptEncodingQFactor = acceptEncoding;
        }
    }

    /**
     * Stores the Vary header value created from the
     * {@link RequestImpl#selectVariant(List)} method call.
     */
    public static class VaryHeader {
        final private String varyHeaderValue;

        private VaryHeader(String varyHeaderValue) {
            this.varyHeaderValue = varyHeaderValue;
        }

        public String getVaryHeaderValue() {
            return varyHeaderValue;
        }
    }
}
