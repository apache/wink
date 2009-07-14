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

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path("/decodedparams")
public class AutoDecodeParamResource {

    final private String appVersion;

    public AutoDecodeParamResource(@MatrixParam("appversion") String appVersion) {
        this.appVersion = appVersion;
    }

    @GET
    @Path("country/{location}")
    public String getShopInCountryDecoded(@PathParam("location") String location) {
        return "getShopInCountryDecoded:location=" + location + ";appversion=" + appVersion;
    }

    @GET
    @Path("city")
    public String getShopInCityDecoded(@QueryParam("location") String location) {
        return "getShopInCityDecoded:location=" + location + ";appversion=" + appVersion;
    }

    @GET
    @Path("street")
    public String getShopOnStreetDecoded(@MatrixParam("location") String location) {
        return "getShopOnStreetDecoded:location=" + location + ";appversion=" + appVersion;
    }

    @POST
    @Path("region")
    public String getShopInRegionDecoded(@FormParam("location") String location) {
        return "getShopInRegionDecoded:location=" + location + ";appversion=" + appVersion;
    }
}
