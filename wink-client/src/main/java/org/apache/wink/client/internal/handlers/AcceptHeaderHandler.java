/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.wink.client.internal.handlers;

import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.handlers.ClientHandler;
import org.apache.wink.client.handlers.HandlerContext;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides support for setting the Accept header automatically.
 */
public class AcceptHeaderHandler implements ClientHandler {

    private static final Logger logger = LoggerFactory.getLogger(AcceptHeaderHandler.class);

    public ClientResponse handle(ClientRequest request, HandlerContext context) throws Exception {
        MultivaluedMap<String, String> requestHeaders = request.getHeaders();
        if (requestHeaders.getFirst(HttpHeaders.ACCEPT) == null) {
            Object responseEntityClassType =
                request.getAttributes().get(ClientRequestImpl.RESPONSE_ENTITY_CLASS_TYPE);
            if (responseEntityClassType != null) {
                Class<?> classType = (Class<?>)responseEntityClassType;
                logger.debug("Response entity class is: {}", classType);
                Set<MediaType> mediaTypes =
                    request.getAttribute(ProvidersRegistry.class)
                        .getMessageBodyReaderMediaTypesLimitByIsReadable(classType,
                                                                         RuntimeContextTLS
                                                                             .getRuntimeContext());
                logger.debug("Found media types: {}", mediaTypes);
                StringBuffer acceptHeader = new StringBuffer();
                boolean isFirst = true;
                for (MediaType mt : mediaTypes) {
                    if (!isFirst) {
                        acceptHeader.append(",");
                    }
                    acceptHeader.append(mt.toString());
                    isFirst = false;
                }
                if (acceptHeader.length() > 0) {
                    String acceptValue = acceptHeader.toString();
                    requestHeaders.add(HttpHeaders.ACCEPT, acceptValue);
                    logger.info(Messages.getMessage("clientAcceptHeaderHandlerSetAccept"),
                                acceptValue);
                }
            }
        }
        return context.doChain(request);
    }
}
