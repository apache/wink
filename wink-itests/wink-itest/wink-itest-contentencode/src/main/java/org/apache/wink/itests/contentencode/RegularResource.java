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
package org.apache.wink.itests.contentencode;

import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;

@Path("regular")
public class RegularResource {

    /**
     * Simple resource that has a string repeated.
     * 
     * @return
     */
    @GET
    @Path("repeatedstring")
    public String getRepeatedContent() {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < 1000; ++c) {
            sb.append("Hello world!  ");
        }
        return sb.toString();
    }

    @POST
    @Path("echo")
    public String echoSomething(String entity) {
        return entity;
    }

    /**
     * Resource method will modify the Vary header with an Accept value.
     * 
     * @return
     */
    @GET
    @Path("varyheaderwithaccept")
    public String getResourceWithVaryAccept(@Context Request request) {
        List<Variant> variants =
            VariantListBuilder.newInstance().mediaTypes(MediaType.TEXT_PLAIN_TYPE,
                                                        MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .add().build();
        Variant bestVariant = request.selectVariant(variants);
        return bestVariant.toString();
    }

    /**
     * Resource method will modify the Vary header with an Accept-Encoding
     * value.
     * 
     * @return
     */
    @GET
    @Path("varyheaderwithacceptencoding")
    public String getResourceWithVaryAcceptEncoding(@Context Request request) {
        List<Variant> variants =
            VariantListBuilder.newInstance().mediaTypes(MediaType.TEXT_PLAIN_TYPE,
                                                        MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .encodings("gzip", "identity", "deflate").add().build();
        Variant bestVariant = request.selectVariant(variants);
        return bestVariant.toString();
    }

    /**
     * Resource method will modify the Vary header with an Accept-Encoding value
     * added by the user.
     * 
     * @return
     */
    @GET
    @Path("varyheaderwithacceptencodingbyuser")
    public Response getResourceWithVaryAcceptEncodingByUser() {
        return Response.ok("text/plain content").header("vary",
                                                        HttpHeaders.ACCEPT_CHARSET + ","
                                                            + HttpHeaders.ACCEPT_ENCODING
                                                                .toLowerCase()
                                                            + "  , "
                                                            + HttpHeaders.ACCEPT).build();
    }

    /**
     * Resource that will return the HTTP header Content-Encoding values.
     * 
     * @return
     */
    @POST
    @Path("httpheadercontentencoding")
    public Response getResourceHttpHeaderValues(String entity,
                                                @Context HttpHeaders httpHeaders,
                                                @Context HttpServletRequest servletRequest) {
        Enumeration headerNames = servletRequest.getHeaderNames();
        boolean isContentEncodingHeaderPresent = false;
        while (headerNames.hasMoreElements()) {
            if (HttpHeaders.CONTENT_ENCODING.equalsIgnoreCase((String)headerNames.nextElement())) {
                isContentEncodingHeaderPresent = true;
            }
        }
        return Response.ok(httpHeaders.getRequestHeader(HttpHeaders.CONTENT_ENCODING) + ":"
            + servletRequest.getHeader(HttpHeaders.CONTENT_ENCODING)
            + ":"
            + servletRequest.getHeaders(HttpHeaders.CONTENT_ENCODING).hasMoreElements()
            + ":"
            + isContentEncodingHeaderPresent).build();
    }
}
