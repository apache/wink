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
package org.apache.wink.common.internal.providers.error;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Default implementation for ExceptionMapper that maps all Throwables to a
 * Formatted Response
 */
@Provider
public class FormattedExceptionMapper implements ExceptionMapper<Throwable> {

    public Response toResponse(Throwable exception) {

        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

        if (exception instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException)exception;
            if (wae.getResponse().getEntity() != null) {
                return wae.getResponse();
            }
            status = wae.getResponse().getStatus();
        }
        return Response.status(status).entity(exception).build();
    }

}
