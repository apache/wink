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
package org.apache.wink.providers.json4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.utils.ProviderUtils;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;

@Provider
@Consumes(value = {MediaType.APPLICATION_JSON, "application/javascript"})
@Produces(value = {MediaType.APPLICATION_JSON, "application/javascript"})
public class JSON4JArrayProvider implements MessageBodyWriter<JSONArray>,
    MessageBodyReader<JSONArray> {

    public boolean isReadable(Class<?> clazz,
                              Type type,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return JSONArray.class == clazz;
    }

    public JSONArray readFrom(Class<JSONArray> clazz,
                              Type type,
                              Annotation[] annotations,
                              MediaType mediaType,
                              MultivaluedMap<String, String> headers,
                              InputStream is) throws IOException, WebApplicationException {
        try {
            return new JSONArray(new InputStreamReader(is, ProviderUtils.getCharset(mediaType)),
                                 false);
        } catch (JSONException e) {
            throw new WebApplicationException(e, 400);
        }
    }

    public long getSize(JSONArray obj,
                        Class<?> clazz,
                        Type type,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    public boolean isWriteable(Class<?> clazz,
                               Type type,
                               Annotation[] annotations,
                               MediaType mediaType) {
        return JSONArray.class.isAssignableFrom(clazz);
    }

    public void writeTo(JSONArray arr,
                        Class<?> clazz,
                        Type type,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> headers,
                        OutputStream os) throws IOException, WebApplicationException {

        mediaType = MediaTypeUtils.setDefaultCharsetOnMediaTypeHeader(headers, mediaType);
        OutputStreamWriter writer = new OutputStreamWriter(os, ProviderUtils.getCharset(mediaType));
        try {
            Writer json4jWriter = arr.write(writer);
            json4jWriter.flush();
            writer.flush();
        } catch (JSONException e) {
            throw new WebApplicationException(e);
        }
    }
}
