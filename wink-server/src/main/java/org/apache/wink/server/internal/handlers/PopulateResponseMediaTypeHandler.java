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
package org.apache.wink.server.internal.handlers;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.server.handlers.AbstractHandler;
import org.apache.wink.server.handlers.MessageContext;

public class PopulateResponseMediaTypeHandler extends AbstractHandler {

    private static final MediaType APPLICATION_TYPE = new MediaType("application", "*");

    private boolean errorFlow = false;
    
    public void handleResponse(MessageContext context) throws Throwable {

        MediaType responseMediaType = null;
        Object result = context.getResponseEntity();

        if (result == null) {
            return;
        }

        if (result instanceof Response) {
            Response response = (Response)result;

            Object first = response.getMetadata().getFirst(HttpHeaders.CONTENT_TYPE);

            if (first != null) {
                if (first instanceof MediaType) {
                    responseMediaType = (MediaType)first;
                } else {
                    responseMediaType = MediaType.valueOf(first.toString());
                }
            }
        }

        if (responseMediaType == null) {
            Set<MediaType> producedMime = null;
            SearchResult searchResult = context.getAttribute(SearchResult.class);
            if (searchResult != null && searchResult.isFound()) {
                MethodMetadata methodMetadata = searchResult.getMethod().getMetadata();
                producedMime = methodMetadata.getProduces();
            }
            if (producedMime == null || producedMime.isEmpty()) {
                producedMime =
                    context.getAttribute(ProvidersRegistry.class)
                        .getMessageBodyWriterMediaTypes(result.getClass());
            }
            if (producedMime.isEmpty()) {
                producedMime.add(MediaType.WILDCARD_TYPE);
            }

            List<MediaType> acceptableMediaTypes =
                context.getHttpHeaders().getAcceptableMediaTypes();

            // collect all candidates
            List<CandidateMediaType> candidates = new LinkedList<CandidateMediaType>();
            for (MediaType acceptableMediaType : acceptableMediaTypes) {
                for (MediaType mediaType : producedMime) {
                    if (mediaType.isCompatible(acceptableMediaType)) {
                        MediaType candidateMediaType = null;
                        if (MediaTypeUtils.compareTo(mediaType, acceptableMediaType) > 0) {
                            candidateMediaType = mediaType;
                        } else {
                            candidateMediaType = acceptableMediaType;
                        }
                        String q = acceptableMediaType.getParameters().get("q");
                        CandidateMediaType candidate =
                            new CandidateMediaType(candidateMediaType, q);
                        if (Double.compare(candidate.q, 0.0) != 0) {
                            candidates.add(candidate);
                        }
                    }
                }
            }

            // there are no candidates
            if (candidates.isEmpty()) {
                if (isErrorFlow()) {
                    return;
                }
                throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
            }

            // select the best candidate.
            // we don't need to sort the whole thing, just to select the best
            // one
            CandidateMediaType max = null;
            boolean useOctetStream = false;
            for (CandidateMediaType candidate : candidates) {
                if (max == null) {
                    max = candidate;
                } else {
                    // select the more specific media type before a media type
                    // that has a wildcard in it
                    // even if its q value is greater
                    int comparison =
                        MediaTypeUtils.compareTo(candidate.getMediaType(), max.getMediaType());
                    if (comparison > 0) {
                        max = candidate;
                    } else if (comparison == 0 && candidate.getQ() > max.getQ()) {
                        max = candidate;
                    }
                }

                if (!useOctetStream && (candidate.getMediaType().equals(MediaType.WILDCARD_TYPE) || candidate
                    .getMediaType().equals(APPLICATION_TYPE))) {
                    useOctetStream = true;
                }
            }

            if (max.getMediaType().isWildcardSubtype() == false) {
                responseMediaType = max.getMediaType();
            } else if (useOctetStream) {
                responseMediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
            } else {
                if (isErrorFlow()) {
                    return;
                }
                throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
            }

        }
        context.setResponseMediaType(responseMediaType);
    }



    public void setErrorFlow(boolean errorFlow) {
        this.errorFlow = errorFlow;
    }



    public boolean isErrorFlow() {
        return errorFlow;
    }



    private static class CandidateMediaType {
        private MediaType mediaType;
        private double    q;

        public CandidateMediaType(MediaType mediaType, String q) {
            this.mediaType = mediaType;
            if (q != null) {
                this.q = Double.parseDouble(q);
            } else {
                this.q = 1.0d;
            }
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        public double getQ() {
            return q;
        }
    }

}
