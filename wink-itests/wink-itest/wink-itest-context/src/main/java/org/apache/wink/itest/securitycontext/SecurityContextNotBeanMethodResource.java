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

package org.apache.wink.itest.securitycontext;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/context/securitycontext/notbeanmethod")
@Produces(MediaType.APPLICATION_XML)
public class SecurityContextNotBeanMethodResource {

    private SecurityContext s = null;

    @GET
    public Response requestSecurityInfo() {
        if (s == null) {
            return Response.ok("false").build();
        }
        return Response.ok(SecurityContextUtils.securityContextToJSON(s)).build();
    }

    @Context
    public void injectSecurityContext(SecurityContext secContext) {
        /*
         * this method does not start with "set" as its name so it is not
         * expected to be injected.
         */
        this.s = secContext;
    }

    public void setSecurityContext(SecurityContext secContext) {
        /*
         * this method does not have a @Context annotation so it is not expected
         * to be injected.
         */
        this.s = secContext;
    }

    @Context
    public void setSecurityContext(SecurityContext secContext, SecurityContext secContext2) {
        /*
         * this method is not a Java bean method (it has 2 parameters) so it
         * will not be used for injection
         */
        this.s = secContext;
    }
}
