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

package org.apache.wink.itest.uriinfo;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class MatchedResourcesSubResource {

    private String  resourceClassNames;

    private UriInfo uriInfo;

    public MatchedResourcesSubResource(String resourceClassNames, UriInfo uriInfo) {
        this.resourceClassNames = resourceClassNames;
        this.uriInfo = uriInfo;
    }

    @GET
    public Response getUriMatchedResourcesInfo() {
        List<Object> matchedResources = uriInfo.getMatchedResources();
        String subResourceClassNames = "";
        for (Object o : matchedResources) {
            Class<?> c = (Class<?>)o.getClass();
            subResourceClassNames += c.getName() + ":";
        }
        return Response.ok(resourceClassNames + "-" + subResourceClassNames).build();
    }
}
