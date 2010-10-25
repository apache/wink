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

package org.apache.wink.providers.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.RestConstants;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.utils.ProviderUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Consumes( {MediaType.APPLICATION_JSON, MediaTypeUtils.JAVASCRIPT})
@Produces( {MediaType.APPLICATION_JSON, MediaTypeUtils.JAVASCRIPT})
public class JsonProvider implements MessageBodyWriter<JSONObject>, MessageBodyReader<JSONObject> {

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
        // TODO: use isAssignableFrom instead of == ?
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
            logger.error(Messages.getMessage("jsonFailWriteJSONObject"), e); //$NON-NLS-1$
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }

        String callbackParam = null;
        try {
            callbackParam =
                uriInfo.getQueryParameters().getFirst(RestConstants.REST_PARAM_JSON_CALLBACK);
        } catch (Exception e) {
            logger.trace("Could not get the URI callback param", e); //$NON-NLS-1$
        }
        mediaType = MediaTypeUtils.setDefaultCharsetOnMediaTypeHeader(httpHeaders, mediaType);
        Charset charset = Charset.forName(ProviderUtils.getCharset(mediaType));
        OutputStreamWriter writer =
            new OutputStreamWriter(entityStream, charset);
        if (callbackParam != null) {
            writer.write(callbackParam);
            writer.write("("); //$NON-NLS-1$
        }
        writer.write(jsonString);
        if (callbackParam != null) {
            writer.write(")"); //$NON-NLS-1$
        }
        writer.flush();
    }

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return type == JSONObject.class;
    }

    public JSONObject readFrom(Class<JSONObject> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders,
                               InputStream entityStream) throws IOException,
        WebApplicationException {
        try {
            return new JSONObject(new JSONTokener(ProviderUtils.createReader(entityStream,
                                                                             mediaType)));
        } catch (JSONException e) {
            logger.error(Messages.getMessage("jsonFailReadJSONObject"), e); //$NON-NLS-1$
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
    }

}
