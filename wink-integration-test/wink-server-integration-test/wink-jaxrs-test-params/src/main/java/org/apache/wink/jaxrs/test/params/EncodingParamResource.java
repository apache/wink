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

import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path("/encodingparam")
public class EncodingParamResource {

    final private String appVersion;

    public EncodingParamResource(@Encoded @MatrixParam("appversion") String appVersion) {
        this.appVersion = appVersion;
    }

    //
    // @GET
    // @Path("city/{city}")
    // public String getShopInCity(@Encoded @QueryParam("q") String searchQuery,
    // @PathParam("city") String city) {
    // return "getShopInCity:q=" + searchQuery + ";city=" + city +
    // ";appversion=" + appVersion;
    // }

    // @GET
    // @Path("loc/{location}")
    // @Encoded
    // public String getShopInLocation(@QueryParam("q") String searchQuery,
    // @Encoded @PathParam("location") String location) {
    // return "getShopInLocation:q=" + searchQuery + ";location=" + location +
    // ";appversion=" + appVersion;
    // }

    @GET
    @Path("country/{location}")
    public String getShopInCountry(@Encoded @PathParam("location") String location) {
        return "getShopInCountry:location=" + location + ";appversion=" + appVersion;
    }

    @GET
    @Path("method/country/{location}")
    @Encoded
    public String getShopInCountryMethod(@PathParam("location") String location) {
        return "getShopInCountryMethod:location=" + location + ";appversion=" + appVersion;
    }

    @GET
    @Encoded
    @Path("method/city")
    public String getShopInCityMethod(@QueryParam("location") String location) {
        return "getShopInCityMethod:location=" + location + ";appversion=" + appVersion;
    }

    @GET
    @Path("city")
    public String getShopInCity(@Encoded @QueryParam("location") String location) {
        return "getShopInCity:location=" + location + ";appversion=" + appVersion;
    }

    @GET
    @Encoded
    @Path("method/street")
    public String getShopOnStreetMethod(@MatrixParam("location") String location) {
        return "getShopOnStreetMethod:location=" + location + ";appversion=" + appVersion;
    }

    @GET
    @Path("street")
    public String getShopOnStreet(@Encoded @MatrixParam("location") String location) {
        return "getShopOnStreet:location=" + location + ";appversion=" + appVersion;
    }

    @POST
    @Path("region")
    public String getShopInRegion(@Encoded @FormParam("location") String location) {
        return "getShopInRegion:location=" + location + ";appversion=" + appVersion;
    }

    @POST
    @Encoded
    @Path("method/region")
    public String getShopInRegionMethod(@FormParam("location") String location) {
        return "getShopInRegionMethod:location=" + location + ";appversion=" + appVersion;
    }
}
