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

package org.apache.wink.jaxrs.test.providers.exceptionmappers.mapped;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapProvider implements ExceptionMapper<WebApplicationException> {

    @Context
    private UriInfo uri;

    public Response toResponse(WebApplicationException arg0) {
        int oldStatus = arg0.getResponse().getStatus();
        Response.ResponseBuilder builder = Response.fromResponse(
                arg0.getResponse()).header("ExceptionPage",
                uri.getAbsolutePath().toASCIIString());

        if (oldStatus == 499) {
            builder.status(497);
        } else if (oldStatus == Response.Status.BAD_REQUEST.getStatusCode()) {
            System.out.println("SETTING 496");
            builder.status(496);
        } else if (oldStatus == 481) {
            builder.status(491);
            CommentError error = new CommentError();
            error.setErrorMessage("WebApplicationExceptionMapProvider set message");
            builder.entity(error);
        }

        return builder.build();
    }

}
