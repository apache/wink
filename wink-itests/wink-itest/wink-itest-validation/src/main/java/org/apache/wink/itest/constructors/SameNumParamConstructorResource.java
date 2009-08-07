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

package org.apache.wink.itest.constructors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("constructors/samenumparam")
public class SameNumParamConstructorResource {

    /*
     * determines the number of times the constructors have been called in total
     */
    private static int   constructorCallCount = 0;

    final private String whichConstructor;

    public SameNumParamConstructorResource() {
        ++constructorCallCount;
        whichConstructor = "default" + constructorCallCount;
    }

    public SameNumParamConstructorResource(@Context UriInfo uriInfo) {
        /*
         * this constructor may be called
         */
        ++constructorCallCount;
        whichConstructor = "context" + constructorCallCount;
    }

    public SameNumParamConstructorResource(@QueryParam("q") String q) {
        /*
         * this constructor may be called
         */
        ++constructorCallCount;
        whichConstructor = "query" + constructorCallCount;
    }

    @GET
    public String getInfo() {
        return whichConstructor;
    }
}
