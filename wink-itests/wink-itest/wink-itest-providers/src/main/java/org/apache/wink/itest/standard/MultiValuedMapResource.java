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

package org.apache.wink.itest.standard;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

@Path("providers/standard/multivaluedmap")
public class MultiValuedMapResource {

    private MultivaluedMap<String, String> formData = null;

    @GET
    public Response getMultivaluedMap() {
        return Response.ok(formData).build();
    }

    @POST
    @Produces("application/x-www-form-urlencoded")
    public MultivaluedMap<String, String> postMultivaluedMap(MultivaluedMap<String, String> map) {
        return map;
    }

    @POST
    @Path("/noproduces")
    public MultivaluedMap<String, String> postMultivaluedMapNoProduces(MultivaluedMap<String, String> map) {
        return map;
    }

    @POST
    @Path("/subclasses/shouldfail")
    public MultivaluedMap<String, Object> postMultivaluedMapWithNotRightTypes(MultivaluedMap<String, Object> map) {
        return map;
    }

    @PUT
    public void putMultivaluedMap(MultivaluedMap<String, String> map) throws IOException {
        formData = map;
    }
}
