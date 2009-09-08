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

package org.apache.wink.itest.echoapp;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;

import org.apache.wink.itest.client.jaxb.Echo;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/echoaccept")
public class EchoResource {

    @Context
    HttpHeaders requestHeaders;

    @GET
    public Response getAcceptHeaderEcho(@Context Request request) throws JSONException {
        StringBuffer sb = new StringBuffer("echo: ");
        List<String> acceptHeader = requestHeaders.getRequestHeader(HttpHeaders.ACCEPT);

        if (acceptHeader != null) {
            for (String s : acceptHeader) {
                sb.append(s);
            }
        }

        if (acceptHeader == null || acceptHeader.isEmpty()
            || MediaType.WILDCARD_TYPE.equals(requestHeaders.getAcceptableMediaTypes().get(0))) {
            return Response.ok(sb.toString()).type(MediaType.TEXT_PLAIN_TYPE).build();
        }

        Variant variant =
            request.selectVariant(VariantListBuilder.newInstance()
                .mediaTypes(MediaType.TEXT_PLAIN_TYPE,
                            MediaType.TEXT_XML_TYPE,
                            MediaType.APPLICATION_JSON_TYPE).add().build());
        if (variant != null) {
            if (MediaType.APPLICATION_JSON_TYPE.isCompatible(variant.getMediaType())) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("value", sb.toString());
                return Response.ok(jsonObject).build();
            } else if (MediaType.TEXT_XML_TYPE.isCompatible(variant.getMediaType())) {
                Echo e = new Echo();
                e.setValue(sb.toString());
                return Response.ok(e).build();
            }
        }

        return Response.ok(sb.toString()).type(MediaType.TEXT_PLAIN_TYPE).build();
    }
}
