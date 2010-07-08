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
package org.apache.wink.common.internal.registry;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Set;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import junit.framework.TestCase;

import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.utils.SoftConcurrentMap;

public class ProvidersRegistryTest extends TestCase {
    
    /**
     * Tests that the providersCache object is and remains instanceof ConcurrentHashMap.  
     * 
     * ProvidersRegistry.MediaTypeMap uses type ConcurrentHashMap on the providersCache object to provide some lock protection on
     * the map when providers are dynamically added.  However, lock protection is already built into the ProvidersRegistry methods:
     * getContextResolver(), getMessageBodyReader(), and getMessageBodyWriter().
     * 
     * However, the second protection (in the ProvidersRegistry methods) is for the cache itself which could be written to by two
     * different threads even if they both were getting a single MessageBodyReader (i.e. a cache value may be dropped and then two
     * threads come back later and try to write a new cache value).  Due to some weird HashMap properties, this can blow up in
     * weird ways.
     * 
     * Thus, we need to ensure the providersCache continues to be instantiated with ConcurrentHashMap.
     */
    public void testProtectionModel() throws Exception {

        // I need the instantiated object providersCache in the abstract private nested class MediaTypeMap, so here we go!
        ProvidersRegistry providersRegistry = new ProvidersRegistry(new LifecycleManagersRegistry(), new ApplicationValidator());
        Field field = providersRegistry.getClass().getDeclaredField("messageBodyReaders");
        field.setAccessible(true);
        Object messageBodyReaders = field.get(providersRegistry);
        Field field2 = messageBodyReaders.getClass().getSuperclass().getDeclaredField("providersCache");
        field2.setAccessible(true);
        Object providersCache = field2.get(messageBodyReaders);
        
        assertTrue(providersCache instanceof SoftConcurrentMap);    
    }
    
    /**
     * Application subclass methods .getClasses and .getSingletons may list provider class or instance that is the same
     * as a default Wink provider to override it to establish a new priority order.
     * 
     * The order that the Wink runtime loads providers is:
     * Application.getSingletons
     * Application.getClasses
     * system (through wink-providers file)
     * extra jars (through each wink-application file)
     */
    @SuppressWarnings("unchecked")
    public void testOverrideSystemProvider() throws Exception {
        ProvidersRegistry providersRegistry = createProvidersRegistryImpl();
        assertTrue(providersRegistry.addProvider(StringReader.class, 0.5, false));  // registered as a user provider
        MessageBodyReader<String> reader1 = providersRegistry.getMessageBodyReader(String.class, null, null, MediaType.TEXT_PLAIN_TYPE, null);

        // registered twice as a custom user provider with higher priority
        // See javadoc for Application.getSingletons to see why we ignore the attempt to add a second StringReader
        assertFalse(providersRegistry.addProvider(StringReader.class, 0.6, false));
        MessageBodyReader<String> reader2 = providersRegistry.getMessageBodyReader(String.class, null, null, MediaType.TEXT_PLAIN_TYPE, null);
        assertTrue(reader1 == reader2);  // object compare to make sure reader2 has been silently ignored
        
        // registered as a system provider
        assertFalse(providersRegistry.addProvider(StringReader.class, 0.1, false));
        MessageBodyReader<String> reader3 = providersRegistry.getMessageBodyReader(String.class, null, null, MediaType.TEXT_PLAIN_TYPE, null);
        assertTrue(reader1 == reader3);  // object compare to make sure reader3 has been silently ignored
        assertTrue(reader2 == reader3);  // object compare to make sure reader3 has been silently ignored
        
        // to confirm that the ignores are indeed happening, I need to get the private field
        // "messageBodyReaders" object, then it's superclass "data" object and inspect it:
        Field field = providersRegistry.getClass().getDeclaredField("messageBodyReaders");
        field.setAccessible(true);
        Object messageBodyReaders = field.get(providersRegistry);
        Field field2 = messageBodyReaders.getClass().getSuperclass().getDeclaredField("data");
        field2.setAccessible(true);
        HashMap data = (HashMap)field2.get(messageBodyReaders);
        Set readers = (Set)data.get(MediaType.WILDCARD_TYPE);
        
        // make there is only one provider in the list to conform to JAX-RS 4.1 first sentence
        assertEquals(1, readers.size());
    }
    
    // TODO:  perhaps future tests should be added to actually exercise the providersCache code, but it would be an involved,
    // multi-threaded test that dynamically adds providers at just the right time to ensure no problems with
    // concurrent writes.
    
    // Utility:
    private ProvidersRegistry createProvidersRegistryImpl() {
        ProvidersRegistry providers =
            new ProvidersRegistry(new LifecycleManagersRegistry(), new ApplicationValidator());
        ;
        return providers;
    }
    
    @Provider
    @Produces( {MediaType.WILDCARD})
    public static class StringReader implements MessageBodyReader<String> {

        public boolean isReadable(Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return true;
        }

        public String readFrom(Class<String> type, Type genericType,
                Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, String> httpHeaders,
                InputStream entityStream) throws IOException {
            return "STRING";
        }
    }
    
}
