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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.annotations.Scope;
import org.apache.wink.common.annotations.Scope.ScopeType;
import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.providers.entity.ByteArrayProvider;
import org.apache.wink.common.internal.providers.entity.FileProvider;
import org.apache.wink.common.internal.providers.entity.InputStreamProvider;
import org.apache.wink.common.internal.providers.entity.StringProvider;
import org.apache.wink.common.internal.registry.ProvidersRegistry;

import junit.framework.TestCase;

public class ProvidersMessageBodyTest extends TestCase {

    @Provider
    @Produces( {MediaType.WILDCARD, MediaType.TEXT_PLAIN})
    @Scope(ScopeType.PROTOTYPE)
    public static class IntegerMessageBodyProvider implements MessageBodyReader<Integer>,
        MessageBodyWriter<Integer> {

        public boolean isReadable(Class<?> type,
                                  Type genericType,
                                  Annotation[] annotations,
                                  MediaType mediaType) {
            return true;
        }

        public Integer readFrom(Class<Integer> type,
                                Type genericType,
                                Annotation[] annotations,
                                MediaType mediaType,
                                MultivaluedMap<String, String> httpHeaders,
                                InputStream entityStream) throws IOException,
            WebApplicationException {
            return null;
        }

        public long getSize(Integer t,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType) {
            return 0;
        }

        public boolean isWriteable(Class<?> type,
                                   Type genericType,
                                   Annotation[] annotations,
                                   MediaType mediaType) {
            return true;
        }

        public void writeTo(Integer t,
                            Class<?> type,
                            Type genericType,
                            Annotation[] annotations,
                            MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders,
                            OutputStream entityStream) throws IOException, WebApplicationException {
        }

    }

    @Provider
    private static class String2Provider extends StringProvider {
    }

    private ProvidersRegistry createProvidersRegistryImpl() {
        ProvidersRegistry providers =
            new ProvidersRegistry(new LifecycleManagersRegistry(), new ApplicationValidator());
        return providers;
    }

    public void testMessageBodyWriters() {
        ProvidersRegistry providers = createProvidersRegistryImpl();

        ByteArrayProvider byteArrayProvider = new ByteArrayProvider();
        InputStreamProvider inputStreamProvider = new InputStreamProvider();
        StringProvider stringProvider = new StringProvider();
        String2Provider string2Provider = new String2Provider();
        FileProvider fileProvider = new FileProvider();
        IntegerMessageBodyProvider objectMessageBodyProvider = new IntegerMessageBodyProvider();

        providers.addProvider(byteArrayProvider);
        providers.addProvider(inputStreamProvider);
        providers.addProvider(string2Provider);
        providers.addProvider(stringProvider);
        providers.addProvider(fileProvider);
        providers.addProvider(objectMessageBodyProvider);

        assertEquals(byteArrayProvider, providers
            .getMessageBodyReader(byte[].class, null, null, MediaType.APPLICATION_JSON_TYPE, null));
        assertEquals(stringProvider, providers.getMessageBodyReader(String.class,
                                                                    null,
                                                                    null,
                                                                    MediaType.WILDCARD_TYPE,
                                                                    null));
        assertEquals(fileProvider, providers
            .getMessageBodyReader(File.class, null, null, MediaType.APPLICATION_SVG_XML_TYPE, null));

        assertEquals(byteArrayProvider, providers
            .getMessageBodyWriter(byte[].class,
                                  null,
                                  null,
                                  MediaType.APPLICATION_ATOM_XML_TYPE,
                                  null));
        assertEquals(stringProvider, providers.getMessageBodyWriter(String.class,
                                                                    null,
                                                                    null,
                                                                    MediaType.WILDCARD_TYPE,
                                                                    null));
        assertEquals(fileProvider, providers
            .getMessageBodyWriter(File.class, null, null, MediaType.APPLICATION_SVG_XML_TYPE, null));

        assertEquals(IntegerMessageBodyProvider.class, providers
            .getMessageBodyWriter(Integer.class, null, null, MediaType.WILDCARD_TYPE, null)
            .getClass());
    }

    public void testMessageBodyWritersMediaTypes() {
        ProvidersRegistry providers = createProvidersRegistryImpl();

        ByteArrayProvider byteArrayProvider = new ByteArrayProvider();
        InputStreamProvider inputStreamProvider = new InputStreamProvider();
        StringProvider stringProvider = new StringProvider();
        String2Provider string2Provider = new String2Provider();
        FileProvider fileProvider = new FileProvider();
        IntegerMessageBodyProvider objectMessageBodyProvider = new IntegerMessageBodyProvider();

        providers.addProvider(byteArrayProvider);
        providers.addProvider(inputStreamProvider);
        providers.addProvider(string2Provider, 0.6);
        providers.addProvider(stringProvider);
        providers.addProvider(fileProvider);
        providers.addProvider(objectMessageBodyProvider);

        Set<MediaType> integerWriterMediaType =
            providers.getMessageBodyWriterMediaTypes(Integer.class);
        assertTrue(integerWriterMediaType.contains(MediaType.WILDCARD_TYPE));
        assertTrue(integerWriterMediaType.contains(MediaType.TEXT_PLAIN_TYPE));
        Set<MediaType> stringWriterMediaTypes =
            providers.getMessageBodyWriterMediaTypes(String.class);
        assertTrue(stringWriterMediaTypes.contains(MediaType.WILDCARD_TYPE));
        Set<MediaType> ioWriterMediaTypes =
            providers.getMessageBodyWriterMediaTypes(InputStream.class);
        assertTrue(ioWriterMediaTypes.contains(MediaType.WILDCARD_TYPE));
        assertTrue(ioWriterMediaTypes.contains(MediaType.APPLICATION_OCTET_STREAM_TYPE));
    }

}
