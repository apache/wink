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

package org.apache.wink.jaxrs.test.params.query;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Path("/params/queryparam/exception")
public class QueryParamsExceptionResource {

    public QueryParamsExceptionResource() {
        /* do nothing */
    }

    @QueryParam("CustomStringConstructorFieldQuery")
    private ParamStringConstructor customStringConstructorFieldQuery;

    @QueryParam("CustomValueOfFieldQuery")
    private QueryValueOf           customValueOfFieldQuery;

    private ParamStringConstructor customPropertyStringConstructorQuery;

    private QueryValueOf           customPropertyValueOfQuery;

    @QueryParam("CustomStringConstructorPropertyHeader")
    public void setCustomPropertyStringConstructorQuery(ParamStringConstructor param) {
        customPropertyStringConstructorQuery = param;
    }

    @QueryParam("CustomValueOfPropertyHeader")
    public void setCustomValueOfPropertyHeader(QueryValueOf param) {
        customPropertyValueOfQuery = param;
    }

    @GET
    @Path("primitive")
    public Response getHeaderParam(@QueryParam("CustomNumQuery") int customNumHeader) {
        return Response.ok().header("RespCustomNumQuery", customNumHeader).build();
    }

    // @GET
    // @Path("constructor")
    // public Response getStringConstructorHeaderParam(
    // @HeaderParam("CustomStringHeader") HeaderStringConstructor
    // customStringHeader) {
    // return Response.ok().header("RespCustomStringHeader",
    // customStringHeader.getHeader())
    // .build();
    // }

    public static class QueryValueOf {
        String header;

        private QueryValueOf(String aHeader, int num) {
            header = aHeader;
        }

        public String getParamValue() {
            return header;
        }

        public static QueryValueOf valueOf(String v) throws Exception {
            if ("throwWeb".equals(v)) {
                throw new WebApplicationException(Response.status(498)
                    .entity("ParamValueOfWebAppEx").build());
            } else if ("throwNull".equals(v)) {
                throw new NullPointerException("ParamValueOf NPE");
            } else if ("throwEx".equals(v)) {
                throw new Exception("ParamValueOf Exception");
            }
            return new QueryValueOf(v, 100);
        }
    }

    // @GET
    // @Path("valueof")
    // public Response getValueOfHeaderParam(
    // @QueryParam("CustomValueOfQuery") QueryValueOf customValueOfQuery) {
    // return Response.ok().header("RespCustomValueOfQuery",
    // customValueOfQuery.getParamValue())
    // .build();
    // }
    //
    // @GET
    // @Path("listvalueof")
    // public Response getValueOfHeaderParam(
    // @HeaderParam("CustomListValueOfHeader") List<QueryValueOf>
    // customValueOfHeader) {
    // if (customValueOfHeader.size() != 1) {
    // throw new IllegalArgumentException();
    // }
    // return Response.ok().header("RespCustomListValueOfHeader",
    // customValueOfHeader.get(0).getHeader()).build();
    // }
    //
    // @GET
    // @Path("setvalueof")
    // public Response getValueOfHeaderParam(
    // @HeaderParam("CustomSetValueOfHeader") Set<QueryValueOf>
    // customValueOfHeader) {
    // if (customValueOfHeader.size() != 1) {
    // throw new IllegalArgumentException();
    // }
    // return Response.ok().header("RespCustomSetValueOfHeader",
    // new
    // ArrayList<QueryValueOf>(customValueOfHeader).get(0).getHeader()).build();
    // }
    //
    // @GET
    // @Path("sortedsetvalueof")
    // public Response getValueOfHeaderParam(
    // @HeaderParam("CustomSortedSetValueOfHeader") SortedSet<QueryValueOf>
    // customValueOfHeader) {
    // if (customValueOfHeader.size() != 1) {
    // throw new IllegalArgumentException();
    // }
    // return Response.ok().header("RespCustomSortedSetValueOfHeader",
    // customValueOfHeader.first().getHeader()).build();
    // }
    //
    @GET
    @Path("fieldstrcstr")
    public Response getFieldStringConstructorHeaderParam() {
        return Response.ok().entity(customStringConstructorFieldQuery.getParamValue()).build();
    }

    @GET
    @Path("fieldvalueof")
    public Response getFieldValueOfHeaderParam() {
        return Response.ok().header("RespCustomValueOfFieldHeader",
                                    customValueOfFieldQuery.getParamValue()).build();
    }

    @GET
    @Path("propertystrcstr")
    public Response getPropertyStringConstructorHeaderParam() {
        return Response.ok().header("RespCustomStringConstructorPropertyQuery",
                                    customPropertyStringConstructorQuery.getParamValue()).build();
    }

    @GET
    @Path("propertyvalueof")
    public Response getPropertyValueOfHeaderParam() {
        return Response.ok().header("RespCustomValueOfPropertyQuery",
                                    customPropertyValueOfQuery.getParamValue()).build();
    }

}
