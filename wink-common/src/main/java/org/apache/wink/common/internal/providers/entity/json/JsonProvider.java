/*******************************************************************************
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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/

package org.apache.wink.common.internal.providers.entity.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wink.common.RestConstants;
import org.apache.wink.common.internal.providers.entity.xml.AbstractJAXBProvider;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.utils.ProviderUtils;
import org.json.JSONObject;
import org.json.JSONException;

@Provider
@Produces( {MediaType.APPLICATION_JSON, MediaTypeUtils.JAVASCRIPT})
public class JsonProvider extends AbstractJAXBProvider implements MessageBodyWriter<JSONObject> {

    private static final Logger logger = LoggerFactory.getLogger(JsonProvider.class);

    @Context
    private UriInfo             uriInfo;

    public long getSize(JSONObject t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        return type == JSONObject.class;
    }

    public void writeTo(JSONObject t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        String jsonString = null;
        try {
            jsonString = t.toString(2);
        } catch (JSONException e) {
            logger.error("Failed to write json object");
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        String callbackParam =
            uriInfo.getQueryParameters().getFirst(RestConstants.REST_PARAM_JSON_CALLBACK);
        OutputStreamWriter writer =
            new OutputStreamWriter(entityStream, ProviderUtils.getCharset(mediaType));
        if (callbackParam != null) {
            writer.write(callbackParam);
            writer.write("(");
        }
        writer.write(jsonString);
        if (callbackParam != null) {
            writer.write(")");
        }
        writer.flush();
    }

}
