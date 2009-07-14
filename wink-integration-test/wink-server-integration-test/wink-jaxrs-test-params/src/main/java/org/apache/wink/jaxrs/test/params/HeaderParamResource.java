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

package org.apache.wink.jaxrs.test.params;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Resource with<code>HeaderParam</code>.
 * 
 * @see HeaderParam
 */
@Path("header")
public class HeaderParamResource {

    private String cstrHeaderParam;

    @HeaderParam("Accept-Language")
    private String acceptLanguage;

    private String agent;

    static public class HeaderValueOf {
        private HeaderValueOf(String somevalue) {
        }

        public static HeaderValueOf valueOf(String someValue) {
            if ("throwex".equals(someValue)) {
                throw new WebApplicationException(499);
            } else if ("throwruntimeex".equals(someValue)) {
                throw new IllegalArgumentException();
            }
            return new HeaderValueOf(someValue);
        }
    }

    static public class HeaderConstructor {
        public HeaderConstructor(String somevalue) {
            if ("throwex".equals(somevalue)) {
                throw new WebApplicationException(499);
            } else if ("throwruntimeex".equals(somevalue)) {
                throw new IllegalArgumentException();
            }
        }
    }

    public HeaderParamResource(@HeaderParam("customHeaderParam") String cstrHeaderParam) {
        this.cstrHeaderParam = cstrHeaderParam;
    }

    @GET
    public Response getHeaderParam(@HeaderParam("Accept-Language") String methodLanguage) {
        return Response.ok("getHeaderParam:" + cstrHeaderParam
            + ";User-Agent:"
            + agent
            + ";Accept-Language:"
            + acceptLanguage
            + ";language-method:"
            + methodLanguage).header("custResponseHeader", "secret").build();
    }

    @POST
    public Response getHeaderParamPost(@HeaderParam("CustomHeader") HeaderValueOf customHeader,
                                       @HeaderParam("CustomConstructorHeader") HeaderConstructor customHeader2) {
        return Response.ok().entity("made successful call").build();
    }

    @HeaderParam("User-Agent")
    public void setUserAgent(String aUserAgent) {
        agent = aUserAgent;
    }

    public String getUserAgent() {
        return agent;
    }
}
