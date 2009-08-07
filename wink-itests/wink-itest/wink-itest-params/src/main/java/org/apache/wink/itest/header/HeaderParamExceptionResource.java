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

package org.apache.wink.itest.header;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Path("/params/headerparam/exception")
public class HeaderParamExceptionResource {

    public HeaderParamExceptionResource() {
        /* do nothing */
    }

    @HeaderParam("CustomStringConstructorFieldHeader")
    private HeaderStringConstructor customStringConstructorFieldHeader;

    @HeaderParam("CustomValueOfFieldHeader")
    private HeaderValueOf           customValueOfFieldHeader;

    private HeaderValueOf           customPropertyValueOfHeader;

    private HeaderStringConstructor customPropertyStringConstructorHeader;

    @HeaderParam("CustomValueOfPropertyHeader")
    public void setCustomValueOfPropertyHeader(HeaderValueOf param) {
        customPropertyValueOfHeader = param;
    }

    @HeaderParam("CustomStringConstructorPropertyHeader")
    public void setCustomConstructorPropertyHeader(HeaderStringConstructor param) {
        customPropertyStringConstructorHeader = param;
    }

    @GET
    @Path("primitive")
    public Response getHeaderParam(@HeaderParam("CustomNumHeader") int customNumHeader) {
        return Response.ok().header("RespCustomNumHeader", customNumHeader).build();
    }

    @GET
    @Path("constructor")
    public Response getStringConstructorHeaderParam(@HeaderParam("CustomStringHeader") HeaderStringConstructor customStringHeader) {
        return Response.ok().header("RespCustomStringHeader", customStringHeader.getHeader())
            .build();
    }

    public static class HeaderValueOf {
        String header;

        private HeaderValueOf(String aHeader, int num) {
            header = aHeader;
        }

        public String getHeader() {
            return header;
        }

        public static HeaderValueOf valueOf(String v) throws Exception {
            if ("throwWeb".equals(v)) {
                throw new WebApplicationException(Response.status(498)
                    .entity("HeaderValueOfWebAppEx").build());
            } else if ("throwNull".equals(v)) {
                throw new NullPointerException("HeaderValueOf NPE");
            } else if ("throwEx".equals(v)) {
                throw new Exception("HeaderValueOf Exception");
            }
            return new HeaderValueOf(v, 100);
        }
    }

    @GET
    @Path("valueof")
    public Response getValueOfHeaderParam(@HeaderParam("CustomValueOfHeader") HeaderValueOf customValueOfHeader) {
        return Response.ok().header("RespCustomValueOfHeader", customValueOfHeader.getHeader())
            .build();
    }

    @GET
    @Path("listvalueof")
    public Response getValueOfHeaderParam(@HeaderParam("CustomListValueOfHeader") List<HeaderValueOf> customValueOfHeader) {
        if (customValueOfHeader.size() != 1) {
            throw new IllegalArgumentException();
        }
        return Response.ok().header("RespCustomListValueOfHeader",
                                    customValueOfHeader.get(0).getHeader()).build();
    }

    @GET
    @Path("setvalueof")
    public Response getValueOfHeaderParam(@HeaderParam("CustomSetValueOfHeader") Set<HeaderValueOf> customValueOfHeader) {
        if (customValueOfHeader.size() != 1) {
            throw new IllegalArgumentException();
        }
        return Response.ok().header("RespCustomSetValueOfHeader",
                                    new ArrayList<HeaderValueOf>(customValueOfHeader).get(0)
                                        .getHeader()).build();
    }

    @GET
    @Path("sortedsetvalueof")
    public Response getValueOfHeaderParam(@HeaderParam("CustomSortedSetValueOfHeader") SortedSet<HeaderValueOf> customValueOfHeader) {
        if (customValueOfHeader.size() != 1) {
            throw new IllegalArgumentException();
        }
        return Response.ok().header("RespCustomSortedSetValueOfHeader",
                                    customValueOfHeader.first().getHeader()).build();
    }

    @GET
    @Path("fieldstrcstr")
    public Response getFieldStringConstructorHeaderParam() {
        return Response.ok().header("RespCustomStringConstructorFieldHeader",
                                    customStringConstructorFieldHeader.getHeader()).build();
    }

    @GET
    @Path("fieldvalueof")
    public Response getFieldValueOfHeaderParam() {
        return Response.ok().header("RespCustomValueOfFieldHeader",
                                    customValueOfFieldHeader.getHeader()).build();
    }

    @GET
    @Path("propertystrcstr")
    public Response getPropertyStringConstructorHeaderParam() {
        return Response.ok().header("RespCustomStringConstructorPropertyHeader",
                                    customPropertyStringConstructorHeader.getHeader()).build();
    }

    @GET
    @Path("propertyvalueof")
    public Response getPropertyValueOfHeaderParam() {
        return Response.ok().header("RespCustomValueOfPropertyHeader",
                                    customPropertyValueOfHeader.getHeader()).build();
    }

}
