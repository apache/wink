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

package org.apache.wink.itest;

import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;

@Path("/matrixparamnotset")
public class MatrixParamNotSetResource {

    @Path("int")
    @GET
    public String getDefault(@MatrixParam("count") int count) {
        return count + "";
    }

    @Path("short")
    @GET
    public String getDefault(@MatrixParam("smallCount") short smallCount) {
        return smallCount + "";
    }

    @Path("long")
    @GET
    public String getDefault(@MatrixParam("longCount") long longCount) {
        return longCount + "";
    }

    @Path("float")
    @GET
    public String getDefault(@MatrixParam("floatCount") float floatCount) {
        return floatCount + "";
    }

    @Path("double")
    @GET
    public String getDefault(@MatrixParam("count") double count) {
        return count + "";
    }

    @Path("byte")
    @GET
    public String getDefault(@MatrixParam("b") byte count) {
        return count + "";
    }

    @Path("char")
    @GET
    public String getDefault(@MatrixParam("letter") char letter) {
        return letter + "";
    }

    @Path("set")
    @GET
    public String getDefault(@MatrixParam("bag") Set<Integer> stuff) {
        return stuff.size() + "";
    }

    @Path("list")
    @GET
    public String getDefault(@MatrixParam("letter") List<String> stuff) {
        return stuff.size() + "";
    }
}
