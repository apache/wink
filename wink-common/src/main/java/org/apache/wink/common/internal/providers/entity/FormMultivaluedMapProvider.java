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
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.uri.UriEncoder;
import org.apache.wink.common.internal.utils.UriHelper;
import org.apache.wink.common.utils.ProviderUtils;

@Provider
@Produces(MediaType.APPLICATION_FORM_URLENCODED)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class FormMultivaluedMapProvider implements
    MessageBodyWriter<MultivaluedMap<String, ? extends Object>>,
    MessageBodyReader<MultivaluedMap<String, String>> {

    public long getSize(MultivaluedMap<String, ? extends Object> t,
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
        return MultivaluedMap.class.isAssignableFrom(type);
    }

    public void writeTo(MultivaluedMap<String, ? extends Object> t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        // StringBuilder builder = new StringBuilder();
        // String delim = "";
        // for (String key : t.keySet()) {
        // for (String value : t.get(key)) {
        // builder.append(delim);
        // String encodedKey = URLEncoder.encode(key, "UTF-8");
        // builder.append(encodedKey);
        // if (value != null) {
        // builder.append('=');
        // String encodedValue = URLEncoder.encode(value, "UTF-8");
        // builder.append(encodedValue);
        // }
        // delim = "&";
        // }
        // }
        String string = MultivaluedMapImpl.toString(t, "&");
        string = UriEncoder.encodeQuery(string, true);
        ProviderUtils.writeToStream(string, entityStream, mediaType);
    }

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        // must be a multivalued map and parameterized with Strings
        if (!(MultivaluedMap.class == type) || !(genericType instanceof ParameterizedType)) {
            return false;
        }

        ParameterizedType pType = (ParameterizedType)genericType;
        Type[] actualTypeArguments = pType.getActualTypeArguments();
        Type type1 = actualTypeArguments[0];
        Type type2 = actualTypeArguments[1];
        if (!(type1 instanceof Class<?>) || !((Class<?>)type1).equals(String.class)
            || !(type2 instanceof Class<?>)
            || !((Class<?>)type2).equals(String.class)) {
            return false;
        }
        return true;

    }

    public MultivaluedMap<String, String> readFrom(Class<MultivaluedMap<String, String>> type,
                                                   Type genericType,
                                                   Annotation[] annotations,
                                                   MediaType mediaType,
                                                   MultivaluedMap<String, String> httpHeaders,
                                                   InputStream entityStream) throws IOException,
        WebApplicationException {

        // the output map
        MultivaluedMap<String, String> map = new MultivaluedMapImpl<String, String>();

        // get the form parameters as a string
        String string = ProviderUtils.readFromStreamAsString(entityStream, mediaType);
        map = UriHelper.parseQuery(string);
        // // split into individual parameters
        // String[] formParams = StringUtils.fastSplit(string, "&");
        // for (int i = 0; i < formParams.length; ++i) {
        // // get the parameter
        // String key = formParams[i];
        // String value = null;
        // if (key.length() == 0) {
        // continue;
        // }
        // // get the value of the parameter, if exists
        // int indexOfEqualsSign = key.indexOf('=');
        // if (indexOfEqualsSign > 0) {
        // value = key.substring(indexOfEqualsSign+1);
        // key = key.substring(0, indexOfEqualsSign);
        // }
        // // decode the key and value
        // String keyDecoded = URLDecoder.decode(key, "UTF-8");
        // String valueDecoded = (value == null ? value :
        // URLDecoder.decode(value, "UTF-8"));
        // // add key and value to the map of form parameters
        // map.add(keyDecoded, valueDecoded);
        // }

        return map;
    }

}
