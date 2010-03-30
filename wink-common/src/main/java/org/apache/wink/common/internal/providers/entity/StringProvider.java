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
package org.apache.wink.common.internal.providers.entity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.utils.ProviderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Consumes
@Produces
public class StringProvider implements MessageBodyReader<String>, MessageBodyWriter<String> {

    private static final Logger logger = LoggerFactory.getLogger(StringProvider.class);

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return type.isAssignableFrom(String.class);
    }

    public String readFrom(Class<String> type,
                           Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException, WebApplicationException {
        byte[] bytes = ProviderUtils.readFromStreamAsBytes(entityStream);
        return new String(bytes, ProviderUtils.getCharset(mediaType));
    }

    public long getSize(String t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        String charSet = ProviderUtils.getCharset(mediaType);
        if (charSet != null && !"UTF-8".equals(charSet)) { //$NON-NLS-1$
            try {
                return t.getBytes(charSet).length;
            } catch (UnsupportedEncodingException e) {
                logger.debug("Unsupported character encoding exception: {}", e); //$NON-NLS-1$
                throw new WebApplicationException(e, 500);
            }
        }
        return t.length();
    }

    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        return type == String.class;
    }

    public void writeTo(String t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        if (httpHeaders != null && httpHeaders.get(HttpHeaders.CONTENT_TYPE) == null) {
            // only correct the MediaType if the MediaType was not explicitly
            // set
            logger
                .debug("Media Type not explicitly set on Response so going to correct charset parameter if necessary");//$NON-NLS-1$
            Map<String, String> parameters = mediaType.getParameters();
            if (parameters != null) {
                if (parameters.get("charset") == null) { //$NON-NLS-1$
                    try {
                        Map<String, String> params =
                            new HashMap<String, String>(mediaType.getParameters());
                        params.put("charset", "UTF-8"); //$NON-NLS-1$ $NON-NLS-2$
                        httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, new MediaType(mediaType
                            .getType(), mediaType.getSubtype(), params)); //$NON-NLS-1$
                        logger.debug("Added charset=UTF-8 parameter to Content-Type HttpHeader"); //$NON-NLS-1$
                    } catch (Exception e) {
                        // this can happen in the Atom Model since we pass in an unmodifiable set
                        logger.debug("Caught exception while trying to set the charset", e); //$NON-NLS-1$
                    }
                }
            }
        }

        logger.debug("Writing {} to stream using {}", t, getClass().getName()); //$NON-NLS-1$

        entityStream.write(t.getBytes(ProviderUtils.getCharset(mediaType)));
    }

}
