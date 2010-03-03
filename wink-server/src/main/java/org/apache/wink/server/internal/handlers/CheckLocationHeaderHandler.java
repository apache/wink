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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.server.handlers.AbstractHandler;
import org.apache.wink.server.handlers.MessageContext;

/**
 * This response handler verifies that required header LOCATION is present for
 * the following statuses: 201, 301, 302, 303, 305 and 307
 */
public class CheckLocationHeaderHandler extends AbstractHandler {

    public void handleResponse(MessageContext msgContext) throws Throwable {
        int statusCode = msgContext.getResponseStatusCode();
        Object result = msgContext.getResponseEntity();

        if (result instanceof Response && isStatusWithLocation(statusCode)) {
            Response clientResponse = (Response)result;
            if (!clientResponse.getMetadata().containsKey(HttpHeaders.LOCATION)) {
                throw new IllegalStateException(Messages
                    .getMessage("checkLocationHeaderHandlerIllegalArg", statusCode)); //$NON-NLS-1$
            }
        }
    }

    private boolean isStatusWithLocation(int statusCode) {
        return statusCode == HttpStatus.CREATED.getCode() || statusCode == HttpStatus.MOVED_PERMANENTLY
            .getCode()
            || statusCode == HttpStatus.FOUND.getCode()
            || statusCode == HttpStatus.SEE_OTHER.getCode()
            || statusCode == HttpStatus.USE_PROXY.getCode()
            || statusCode == HttpStatus.TEMPORARY_REDIRECT.getCode();
    }

}
