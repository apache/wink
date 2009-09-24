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

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.registry.metadata.MethodMetadata;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.server.handlers.AbstractHandler;
import org.apache.wink.server.handlers.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PopulateResponseMediaTypeHandler extends AbstractHandler {

    private static final Logger    logger           =
                                                        LoggerFactory
                                                            .getLogger(PopulateResponseMediaTypeHandler.class);

    private static final MediaType APPLICATION_TYPE = new MediaType("application", "*");

    private boolean                errorFlow        = false;

    public void handleResponse(MessageContext context) throws Throwable {

        MediaType responseMediaType = null;
        Object result = context.getResponseEntity();

        if (result == null) {
            logger.debug("No entity so no Content-Type needs to be set");
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
            logger.debug("Content-Type was set by application to {}", responseMediaType);
        }

        if (responseMediaType == null) {
            Set<MediaType> producedMime = null;
            SearchResult searchResult = context.getAttribute(SearchResult.class);
            if (searchResult != null && searchResult.isFound()) {
                MethodMetadata methodMetadata = searchResult.getMethod().getMetadata();
                producedMime = methodMetadata.getProduces();
                logger.debug("Determining Content-Type from @Produces on method: {}", producedMime);
            }
            if (producedMime == null || producedMime.isEmpty()) {
                producedMime =
                    context.getAttribute(ProvidersRegistry.class)
                        .getMessageBodyWriterMediaTypes(result.getClass());
                /*
                 * This is to inform the application developer that they should
                 * specify the Content-Type.
                 */
                logger
                    .info(Messages
                        .getMessage("populateResponseMediaTypeHandlerFromCompatibleMessageBodyWriters"));
                logger
                    .debug("Determining Content-Type from compatible generic type to {} from MessageBodyWriters: {}",
                           result.getClass(),
                           producedMime);
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
                    logger.debug("Comparing {} to {}", acceptableMediaType, mediaType);
                    if (mediaType.isCompatible(acceptableMediaType)) {
                        MediaType candidateMediaType = null;
                        if (MediaTypeUtils.compareTo(mediaType, acceptableMediaType) > 0) {
                            candidateMediaType = mediaType;
                        } else {
                            candidateMediaType = acceptableMediaType;
                        }
                        logger.debug("MediaType compatible so using candidate type {}",
                                     candidateMediaType);
                        String q = acceptableMediaType.getParameters().get("q");
                        CandidateMediaType candidate =
                            new CandidateMediaType(candidateMediaType, q);
                        if (Double.compare(candidate.q, 0.0) != 0) {
                            if (logger.isDebugEnabled()) {
                                logger
                                    .debug("Candidate {} has q value {} so adding to possible candidates",
                                           candidate.getMediaType(),
                                           q);
                            }
                            candidates.add(candidate);
                        }
                    }
                }
            }

            // there are no candidates
            if (candidates.isEmpty()) {
                if (isErrorFlow()) {
                    logger.debug("Error flow and no candidates so not going to set a Content-Type");
                    return;
                }
                logger.info(Messages
                    .getMessage("populateResponseMediaTypeHandlerNoAcceptableResponse"));
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
                    if (logger.isDebugEnabled()) {
                        logger.debug("No previous best candidate so using candidate {}", max
                            .getMediaType());
                    }
                } else {
                    // select the more specific media type before a media type
                    // that has a wildcard in it
                    // even if its q value is greater
                    int comparison =
                        MediaTypeUtils.compareTo(candidate.getMediaType(), max.getMediaType());
                    if (comparison > 0) {
                        max = candidate;
                        if (logger.isDebugEnabled()) {
                            logger
                                .debug("Best candidate is now {} because it was a more specific media type",
                                       max.getMediaType());
                        }
                    } else if (comparison == 0 && candidate.getQ() > max.getQ()) {
                        max = candidate;
                        if (logger.isDebugEnabled()) {
                            logger
                                .debug("Best candidate is now {} because it had a higher quality value {} compared to {} with quality value {}",
                                       new Object[] {max.getMediaType(), max.getQ(), candidate,
                                           candidate.getQ()});
                        }
                    }
                }

                if (!useOctetStream && (candidate.getMediaType().equals(MediaType.WILDCARD_TYPE) || candidate
                    .getMediaType().equals(APPLICATION_TYPE))) {
                    logger
                        .debug("If necessary, use an application/octet-stream because there is a wildcard",
                               candidate.getMediaType());
                    useOctetStream = true;
                }
            }

            if (max.getMediaType().isWildcardSubtype() == false) {
                responseMediaType = max.getMediaType();
            } else if (useOctetStream) {
                logger
                    .debug("Content-Type was reset to application/octet-stream because it was either */* or was application/*");
                responseMediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
            } else {
                if (isErrorFlow()) {
                    logger.debug("Error flow so not going to set a response Content-Type");
                    return;
                }
                logger.info(Messages
                    .getMessage("populateResponseMediaTypeHandlerNoAcceptableResponse"));
                throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
            }

        }
        logger.debug("Response Content-Type will be set to {}", responseMediaType);
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
