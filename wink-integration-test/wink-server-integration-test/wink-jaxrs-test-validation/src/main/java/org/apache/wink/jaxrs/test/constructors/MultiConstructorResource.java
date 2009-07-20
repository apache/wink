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

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("/constructors/multi")
public class MultiConstructorResource {

    /*
     * determines the number of times the constructors have been called in total
     */
    private static int   constructorCallCount = 0;

    final private String whichConstructor;

    public MultiConstructorResource() {
        ++constructorCallCount;
        whichConstructor = "Default" + constructorCallCount;
    }

    public MultiConstructorResource(@QueryParam("q") String query) {
        ++constructorCallCount;
        whichConstructor = "query" + constructorCallCount;
    }

    public MultiConstructorResource(@MatrixParam("m") String matrix, @QueryParam("q") String query) {
        ++constructorCallCount;
        whichConstructor = "matrixAndQuery" + constructorCallCount;
    }

    public MultiConstructorResource(@MatrixParam("m") String matrix,
                                    @QueryParam("q") String query,
                                    @Context UriInfo uriinfo) {
        ++constructorCallCount;
        /*
         * this should be the called constructor
         */
        whichConstructor = "matrixAndQueryAndContext" + constructorCallCount;
    }

    @GET
    public String getInfo() {
        return whichConstructor;
    }

}
