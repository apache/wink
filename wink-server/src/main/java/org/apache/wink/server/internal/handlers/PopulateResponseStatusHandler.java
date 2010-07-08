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

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.apache.wink.server.handlers.AbstractHandler;
import org.apache.wink.server.handlers.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PopulateResponseStatusHandler extends AbstractHandler {

    private static final Logger logger =
                                           LoggerFactory
                                               .getLogger(PopulateResponseStatusHandler.class);

    public void handleResponse(MessageContext context) throws Throwable {
        Object entity = context.getResponseEntity();

        int status = -1;

        if (entity instanceof Response) {
            Response response = (Response)entity;
            status = response.getStatus();
            entity = response.getEntity();
        }

        if (entity instanceof GenericEntity<?>) {
            GenericEntity<?> genericEntity = (GenericEntity<?>)entity;
            entity = genericEntity.getEntity();
        }

        if (status == -1) {
            if (entity == null) {
                logger
                    .trace("No status set and no entity so setting response status to 204 No Content"); //$NON-NLS-1$
                status = HttpServletResponse.SC_NO_CONTENT;
            } else {
                logger.trace("No status set so setting response status to 200 OK"); //$NON-NLS-1$
                status = HttpServletResponse.SC_OK;
            }
        }

        context.setResponseStatusCode(status);
    }

}
