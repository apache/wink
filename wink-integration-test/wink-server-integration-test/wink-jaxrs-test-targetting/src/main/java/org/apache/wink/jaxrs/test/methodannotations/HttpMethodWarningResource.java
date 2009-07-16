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

package org.apache.wink.jaxrs.test.methodannotations;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * Resource with several different type of warnings.
 */
@Path("httpmethodwarning")
public class HttpMethodWarningResource {

    public HttpMethodWarningResource() {

    }

    
    @PUT
    @POST
    public String multiHttpMethodBadBehavior() {
        /*
         * This should not be allowed and violates RESTful principles even if
         * some browsers can't do a PUT.
         */
        return "Should not see me";
    }

    @SuppressWarnings("unused")
    @GET
    @Path("/{id}")
    private String nonPublicGETMethod(@PathParam("id") String id, @QueryParam("detailed") String isDetailed) {
        return "Should not be able to GET me.";
    }

    @DELETE
    protected String nonPublicDeleteMethod() {
        return "Should not be able to DELETE me.";
    }

}
