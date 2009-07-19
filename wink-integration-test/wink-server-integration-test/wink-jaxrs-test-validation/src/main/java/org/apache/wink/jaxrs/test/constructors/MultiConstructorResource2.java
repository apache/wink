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

package org.apache.wink.jaxrs.test.constructors;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/constructors/multi2/{path}")
public class MultiConstructorResource2 {

    /**
     * counts the number of times the constructor has been called
     */
    private static int constructorCallCount = 0;

    final private String whichConstructor;

    public MultiConstructorResource2(@HeaderParam("header1") String header1, @CookieParam("cookie1") String cookie1) {
        ++constructorCallCount;
        whichConstructor = "headerAndCookieAndPath" + constructorCallCount;
    }

    public MultiConstructorResource2(@HeaderParam("header1") int header1, @PathParam("cookie1") String cookie1) {
        ++constructorCallCount;
        whichConstructor = "headerAndCookieAndPath" + constructorCallCount;
    }

    public MultiConstructorResource2(@Context UriInfo uriInfo, @HeaderParam("header1") String header1, @CookieParam("cookie1") String cookie1, @PathParam("path") String path1) {
        /*
         * this should be the called constructor
         */
        ++constructorCallCount;
        whichConstructor = "contextAndHeaderAndCookieAndPath"
                + constructorCallCount;
    }

    @GET
    public String getInfo() {
        return whichConstructor;
    }

}
