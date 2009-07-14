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

import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/queryparamnotset")
public class QueryParamNotSetResource {

    @Path("int")
    @GET
    public String getDefault(@QueryParam("count") int count) {
        return Integer.valueOf(count).toString();
    }

    @Path("short")
    @GET
    public String getDefault(@QueryParam("smallCount") short smallCount) {
        return ""+smallCount;
    }

    @Path("long")
    @GET
    public String getDefault(@QueryParam("longCount") long longCount) {
        return ""+longCount;
    }

    @Path("float")
    @GET
    public String getDefault(@QueryParam("floatCount") float floatCount) {
        return ""+floatCount;
    }

    @Path("double")
    @GET
    public String getDefault(@QueryParam("d") double count) {
        return ""+count;
    }

    @Path("byte")
    @GET
    @Produces("text/plain")
    public String getDefault(@QueryParam("b") byte count) {
        Logger logger = LoggerFactory.getLogger(QueryParamNotSetResource.class);
        logger.error(count+"");
        return ""+count;
    }

    @Path("char")
    @GET
    public String getDefault(@QueryParam("letter") char count) {
        return count + "";
    }

    @Path("set")
    @GET
    public String getDefault(@QueryParam("bag") Set<Integer> stuff) {
        return ""+stuff.size();
    }

    @Path("list")
    @GET
    public String getDefault(@QueryParam("letter") List<String> stuff) {
        return ""+stuff.size();
    }
}
