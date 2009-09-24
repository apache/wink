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
package org.apache.wink.common.internal.providers;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.registry.ProvidersRegistry;

import junit.framework.TestCase;

public class ProvidersContextResolverTest extends TestCase {

    public static class NotAProvider {
    }

    private static final String  STRING  = "String";
    private static final String  STRING2 = "String2";
    private static final String  STRING3 = "String3";
    private static final String  STRING4 = "String4";
    private static final String  STRING5 = "String5";
    private static final String  STRING6 = "String6";
    private static final String  STRING7 = "String7";

    private static final String  ATOM    = "Atom";
    private static final byte[]  BYTE    = new byte[0];
    private static final Integer _12345  = new Integer(12345);
    private static final MyClass MYCLASS = new MyClass();

    @Provider
    @Produces( {MediaType.TEXT_PLAIN, MediaType.WILDCARD})
    public static class StringContextResolver implements ContextResolver<String> {

        public String getContext(Class<?> type) {
            return STRING;
        }
    }

    @Provider
    @Produces( {MediaType.TEXT_PLAIN})
    public static class StringContextResolver2 implements ContextResolver<String> {

        public String getContext(Class<?> type) {
            return STRING2;
        }
    }

    @Provider
    @Produces("text/*")
    public static class StringContextResolver3 implements ContextResolver<String> {

        public String getContext(Class<?> type) {
            return STRING3;
        }
    }

    @Provider
    @Produces("*/*")
    public static class StringContextResolver4 implements ContextResolver<String> {

        public String getContext(Class<?> type) {
            return STRING4;
        }
    }

    @Provider
    public static class StringContextResolver5 implements ContextResolver<String> {

        public String getContext(Class<?> type) {
            return STRING5;
        }
    }

    @Provider
    public static class StringContextResolver6 implements ContextResolver<String> {

        public String getContext(Class<?> type) {
            return STRING6;
        }
    }

    @Provider
    public static class StringContextResolver7 implements ContextResolver<String> {

        public String getContext(Class<?> type) {
            return STRING7;
        }
    }

    @Provider
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    // intentionally using a MediaType with a '-' to exercise regex code
    public static class MyContextResolver implements ContextResolver<MyClass> {

        public MyClass getContext(Class<?> type) {
            return MYCLASS;
        }
    }

    public static class MyClass {

    }

    @Provider
    @Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.WILDCARD})
    public static class AtomContextResolver implements ContextResolver<String> {

        public String getContext(Class<?> type) {
            if (type == null) {
                return ATOM;
            }
            return null;
        }
    }

    @Provider
    @Produces( {MediaType.TEXT_PLAIN, MediaType.WILDCARD})
    public static class ByteContextResolver implements ContextResolver<byte[]> {

        public byte[] getContext(Class<?> type) {
            return BYTE;
        }
    }

    @Provider
    @Produces( {"text/decimal"})
    public static class IntegerContextResolver implements ContextResolver<Integer> {

        public Integer getContext(Class<?> type) {
            return _12345;
        }
    }

    @Provider
    public static class ListContextResolver implements ContextResolver<List<byte[]>> {

        public List<byte[]> getContext(Class<?> type) {
            return Collections.emptyList();
        }
    }

    private ProvidersRegistry createProvidersRegistryImpl() {
        ProvidersRegistry providers =
            new ProvidersRegistry(new LifecycleManagersRegistry(), new ApplicationValidator());
        ;
        return providers;
    }

    public void testContextResolvers() {
        ProvidersRegistry providers = createProvidersRegistryImpl();
        assertTrue(providers.addProvider(new AtomContextResolver()));
        assertTrue(providers.addProvider(new StringContextResolver()));
        assertTrue(providers.addProvider(new IntegerContextResolver()));
        assertTrue(providers.addProvider(new ByteContextResolver()));
        assertTrue(providers.addProvider(new ListContextResolver()));

        try {
            providers.addProvider(new NotAProvider());
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        /*
         * String and text/pain, should invoke StringContextResolver
         */
        assertEquals(STRING, providers.getContextResolver(String.class,
                                                          MediaType.TEXT_PLAIN_TYPE,
                                                          null).getContext(null));

        /*
         * byte[] and text/plain, should invoke ByteContextResolver
         */
        assertEquals(BYTE, providers.getContextResolver(byte[].class,
                                                        MediaType.TEXT_PLAIN_TYPE,
                                                        null).getContext(null));

        /*
         * There is no context resolver that handlers Integer and /
         */
        assertEquals(_12345, providers.getContextResolver(Integer.class,
                                                          MediaType.WILDCARD_TYPE,
                                                          null).getContext(null));

        /*
         * StringContextResolver is registered after AtomContextResolver,
         * therefore it should be invoked
         */
        assertEquals(STRING, providers.getContextResolver(String.class,
                                                          MediaType.WILDCARD_TYPE,
                                                          null).getContext(null));

        /*
         * AtomContextResolver returns null, if the parameter is not null,
         * therefore StringContextResolver should be invoked
         */
        assertEquals(STRING, providers.getContextResolver(String.class,
                                                          MediaType.WILDCARD_TYPE,
                                                          null).getContext(String.class));

        /*
         * test ContextResolver with collections
         */
        assertEquals(Collections.emptyList(), providers.getContextResolver(List.class,
                                                                           MediaType.WILDCARD_TYPE,
                                                                           null).getContext(null));
    }

    public void testContextResolverWildCards() {
        ProvidersRegistry providers = createProvidersRegistryImpl();
        assertTrue(providers.addProvider(new MyContextResolver()));
        assertTrue(providers.addProvider(new StringContextResolver3()));

        /*
         * Check various wildcard permutations
         */
        assertSame(MYCLASS, providers.getContextResolver(MyClass.class,
                                                         MediaType.WILDCARD_TYPE,
                                                         null).getContext(MyClass.class));
        assertSame(MYCLASS, providers.getContextResolver(MyClass.class,
                                                         new MediaType("*", "*"),
                                                         null).getContext(MyClass.class));
        assertSame(MYCLASS, providers.getContextResolver(MyClass.class,
                                                         new MediaType("application", "*"),
                                                         null).getContext(MyClass.class));
        assertSame(MYCLASS, providers.getContextResolver(MyClass.class,
                                                         new MediaType("application",
                                                                       "x-www-form-urlencoded"),
                                                         null).getContext(MyClass.class));
        assertSame(MYCLASS, providers
            .getContextResolver(MyClass.class, new MediaType("*", "x-www-form-urlencoded"), null)
            .getContext(MyClass.class));

        // should hit an exact match when search expands out to "text/*"
        assertSame(STRING3, providers.getContextResolver(String.class,
                                                         new MediaType("text", "blarg"),
                                                         null).getContext(String.class));
    }

    public void testContextResolverSortingAlgorithm() {
        ProvidersRegistry providers = createProvidersRegistryImpl();
        // note: the order these are added is important to the test
        assertTrue(providers.addProvider(new StringContextResolver4()));
        assertTrue(providers.addProvider(new StringContextResolver3()));
        assertTrue(providers.addProvider(new StringContextResolver2()));

        // StringContextResolver2 takes priority over the others due to the
        // media type in @Produces
        assertSame(STRING2, providers.getContextResolver(String.class,
                                                         new MediaType("text", "*"),
                                                         null).getContext(String.class));

        // StringContextResolver2 takes priority over the others due to the
        // media type in @Produces
        assertSame(STRING2, providers.getContextResolver(String.class,
                                                         new MediaType("*", "*"),
                                                         null).getContext(String.class));

        // StringContextResolver2 takes priority over the others due to the
        // media type in @Produces
        assertSame(STRING2, providers.getContextResolver(String.class,
                                                         new MediaType("text", "plain"),
                                                         null).getContext(String.class));
    }

    public void testContextResolverNullMediaType() {
        ProvidersRegistry providers = createProvidersRegistryImpl();
        // note: the order these are added is important to the test
        assertTrue(providers.addProvider(new StringContextResolver4()));
        assertTrue(providers.addProvider(new StringContextResolver3()));
        assertTrue(providers.addProvider(new StringContextResolver2()));

        // StringContextResolver2 takes priority over the others due to the
        // media type in @Produces
        assertSame(STRING2, providers.getContextResolver(String.class, null, null)
            .getContext(String.class));
    }

    public void testContextResolverPrioritySort() {
        ProvidersRegistry providers = createProvidersRegistryImpl();
        // note: the order these are added is important to the test
        assertTrue(providers.addProvider(new StringContextResolver5(), 0.5));
        assertTrue(providers.addProvider(new StringContextResolver6(), 0.6));
        assertTrue(providers.addProvider(new StringContextResolver7(), 0.4));

        // StringContextResolver3 has the highest priority (0.2) even though
        // StringContextResolver2
        // more closely matches based on the media type in @Produces

        assertSame(STRING6, providers.getContextResolver(String.class, null, null)
            .getContext(String.class));
    }
}
