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

package org.apache.wink.jaxrs.test.headers;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

@Path(value = "/headers")
public class HeadersResource {

    @Context
    HttpHeaders headers;

    @GET
    @Path(value = "/cookie")
    public Response get() {
        Response resp = Response.ok().build();
        resp.getMetadata().putSingle("login",
                headers.getCookies().get("login").getValue());
        return resp;
    }

    @GET
    @Path(value = "/language")
    public Response getLanguage() {
        Response resp = Response.ok().build();
        resp.getMetadata().putSingle("language",
                headers.getLanguage().getLanguage() + ":" + headers.getLanguage().getCountry());
        return resp;
    }

    @GET
    @Path(value = "/content")
    public Response getContent() {
        Response resp = Response.ok().build();
        resp.getMetadata().putSingle("content",
                headers.getMediaType().toString());
        return resp;
    }

    @GET
    @Path(value = "/accept")
    public Response getAccept() {
        Response resp = Response.ok().build();
        StringBuffer sb = new StringBuffer();
        for (MediaType mediaType : headers.getAcceptableMediaTypes()) {
            sb.append(mediaType.toString() + " ");
        }
        resp.getMetadata().putSingle("test-accept", sb.toString().trim());
        return resp;
    }

    @GET
    @Path(value = "/acceptlang")
    public Response getAcceptLanguage() {
        Response resp = Response.ok().build();
        resp.getMetadata().putSingle("acceptlang",
                headers.getAcceptableLanguages().get(0).toString());
        return resp;
    }

    @GET
    @Path(value = "/headercase")
    public Response getHeaderCase(@HeaderParam(value = "custom-header") String param) {
        Response resp = Response.ok().build();
        resp.getMetadata().putSingle("Custom-Header", param);
        return resp;
    }

    @GET
    @Path(value = "/headeraccept")
    public Response getHeaderAccept(@HeaderParam(value = "Accept") String param) {
        Response resp = Response.ok().build();
        resp.getMetadata().putSingle("test-accept", param);
        return resp;
    }

    @GET
    @Path(value = "/headersasarg")
    public Response getHeadersAsArg() {
        Response resp = Response.ok().build();
        StringBuffer sb = new StringBuffer();
        MultivaluedMap<String, String> hdrMap = headers.getRequestHeaders();
        for (String accept : hdrMap.get("ACCEPT")) {
            sb.append(accept + " ");
        }
        String hdr = sb.toString().trim();
        resp.getMetadata().putSingle("test-accept", hdr);
        sb = new StringBuffer();
        for (String ct : hdrMap.get("CONTENT-TYPE")) {
            sb.append(ct + " ");
        }
        hdr = sb.toString().trim();
        resp.getMetadata().putSingle("test-content-type", hdr);
        return resp;
    }

}
