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

package org.apache.wink.jaxrs.test.returntype;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

@Path("returntypestatus")
public class ReturnTypeStatusResource {

    @Path("/void")
    @GET
    public void getVoidResponse() {
        System.out.println("In void response method");
    }

    @Path("/null")
    @GET
    public Object getNull() {
        System.out.println("In null response method");
        return null;
    }

    @Path("/nullresponse")
    @GET
    public Response getNullResponse() {
        System.out.println("In null response method");
        return null;
    }

    @Path("/responsestatus")
    @GET
    public Response getResponseStatus(@QueryParam("code") String code) {
        Status s = Status.valueOf(code);
        ResponseBuilder respBuilder = Response.status(s);
        respBuilder.entity("Requested status: " + s.getStatusCode() + " "
                + s.name());
        return respBuilder.build();
    }

    @Path("/CustomResponseStatusNotSet")
    @GET
    public Response getCustomResponseStatusNotSet() {
        final MultivaluedMap<String, Object> map = Response.ok().build()
                .getMetadata();
        map.clear();
        return new Response() {
            @Override
            public Object getEntity() {
                return "CustomApplicationResponse";
            }

            @Override
            public MultivaluedMap<String, Object> getMetadata() {
                return map;
            }

            @Override
            public int getStatus() {
                return -1;
            }
        };
    }

    @Path("/CustomNullResponseStatusNotSet")
    @GET
    public Response getCustomNullResponseStatusNotSet() {
        final MultivaluedMap<String, Object> map = Response.ok().build()
                .getMetadata();
        map.clear();
        return new Response() {
            @Override
            public Object getEntity() {
                return null;
            }

            @Override
            public MultivaluedMap<String, Object> getMetadata() {
                return map;
            }

            @Override
            public int getStatus() {
                return -1;
            }
        };
    }
}
