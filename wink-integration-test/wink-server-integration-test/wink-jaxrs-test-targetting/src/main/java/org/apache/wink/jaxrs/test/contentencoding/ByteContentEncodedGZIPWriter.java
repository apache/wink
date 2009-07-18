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

package org.apache.wink.jaxrs.test.contentencoding;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

@Provider
public class ByteContentEncodedGZIPWriter implements MessageBodyWriter<byte[]> {

    @Context
    private HttpHeaders headers;

    public long getSize(byte[] t,
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
        if (type != byte[].class || !MediaType.TEXT_PLAIN_TYPE.equals(mediaType)) {
            return false;
        }

        List<String> contentEncodingValues = headers.getRequestHeader("Accept-Encoding");
        if (contentEncodingValues != null && contentEncodingValues.contains("gzip")) {
            /*
             * this in real code should check lower and uppercase gzip with
             * quality factors in play
             */
            return true;
        }
        return false;
    }

    @Context
    private Providers providers;

    public void writeTo(byte[] t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        httpHeaders.putSingle("Content-Encoding", "gzip");
        MessageBodyWriter<byte[]> strReader =
            providers.getMessageBodyWriter(byte[].class,
                                           byte[].class,
                                           annotations,
                                           MediaType.APPLICATION_XML_TYPE);
        GZIPOutputStream gzipOut = new GZIPOutputStream(entityStream);
        strReader.writeTo(t,
                          byte[].class,
                          byte[].class,
                          annotations,
                          mediaType,
                          httpHeaders,
                          gzipOut);
        gzipOut.finish();
        gzipOut.flush();
    }

}
