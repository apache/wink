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

package org.apache.wink.test.filter.root;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/person/{personID}")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
@Encoded
public class Person {

    @QueryParam("p")
    @DefaultValue("defaultQuery")
    private String queryParam;

    @POST
    @DefaultValue("defaultMatrix")
    public String postPerson(String entity,
                             @PathParam("personID") String personID,
                             @MatrixParam("m") String matrixParam) {
        return "Person: " + personID
            + " query parameter: "
            + queryParam
            + " matrix parameter: "
            + matrixParam
            + " entity: "
            + entity;
    }

    @POST
    @Consumes(MediaType.TEXT_XML)
    @Produces(MediaType.TEXT_XML)
    @DefaultValue("defaultMatrix")
    public String postPersonInXML(String entity,
                                  @PathParam("personID") String personID,
                                  @MatrixParam("m") String matrixParam) {
        return "Person: " + personID
            + " query parameter: "
            + queryParam
            + " matrix parameter: "
            + matrixParam
            + " entity: "
            + entity;
    }

}
