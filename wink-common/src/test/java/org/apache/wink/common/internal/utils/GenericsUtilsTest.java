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
package org.apache.wink.common.internal.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class GenericsUtilsTest extends TestCase {

    public interface I<T, D> {
    }

    public interface II {
    }
    
    public abstract static class A implements List<String> {

        public List<String>         stringList;
        public Map<Integer, String> map;
        public List<byte[]>         byteArrayList;
        public List<String>[]       stringListArray;

    }

    public abstract static class B extends A implements I<Integer, Double>, II {
    }

    public abstract static class C implements List<byte[]> {
    }

    public abstract static class L implements List<List<byte[]>> {
    }

    public void testGetGenericInterfaceType() {
        assertEquals(String.class, GenericsUtils.getGenericInterfaceParamType(A.class, List.class));
        assertEquals(String.class, GenericsUtils.getGenericInterfaceParamType(B.class, List.class));
        assertEquals(Integer.class, GenericsUtils.getGenericInterfaceParamType(B.class, I.class));
        assertNull(GenericsUtils.getGenericInterfaceParamType(B.class, II.class));
    }

    public void testIsAssignableFrom() throws Exception {
        assertTrue(GenericsUtils.isAssignableFrom(List.class, A.class));
        assertTrue(GenericsUtils.isAssignableFrom(A.class.getField("stringList").getGenericType(),
                                                  List.class));
    }
    
    public void testIsGenericInterfaceAssignableFrom() {
        assertTrue(GenericsUtils
            .isGenericInterfaceAssignableFrom(String.class, B.class, List.class));
        assertTrue(GenericsUtils.isGenericInterfaceAssignableFrom(Integer.class, B.class, I.class));
        // II is not parameterized, but B is still assignableFrom because we
        // assume developer just forgot to parameterize their interface:
        assertTrue(GenericsUtils.isGenericInterfaceAssignableFrom(Integer.class, B.class, II.class));
        assertTrue(GenericsUtils
            .isGenericInterfaceAssignableFrom(byte[].class, C.class, List.class));
        assertTrue(GenericsUtils.isGenericInterfaceAssignableFrom(List.class, L.class, List.class));
        assertTrue(GenericsUtils.isGenericInterfaceAssignableFrom(ArrayList.class,
                                                                  L.class,
                                                                  List.class));
    }

    public void testGetClassType() throws Exception {
        Class<?> clazz = GenericsUtils.getClassType(String.class);
        assertTrue(clazz.equals(String.class));
        clazz = GenericsUtils.getClassType(String[].class);
        assertTrue(clazz.equals(String[].class));
        clazz = GenericsUtils.getClassType(A.class.getField("stringList").getGenericType());
        assertTrue(clazz.equals(List.class));
        clazz = GenericsUtils.getClassType(A.class.getField("map").getGenericType());
        assertTrue(clazz.equals(Map.class));
        clazz = GenericsUtils.getClassType(A.class.getField("byteArrayList").getGenericType());
        assertTrue(clazz.equals(List.class));
        clazz = GenericsUtils.getClassType(A.class.getField("stringListArray").getGenericType());
        assertTrue(clazz.equals(List[].class));
    }

    public void testGetGenericType() throws Exception {
        Class<?> clazz = GenericsUtils.getGenericParamType(String.class);
        assertNull(clazz);
        clazz = GenericsUtils.getGenericParamType(A.class.getField("stringList").getGenericType());
        assertTrue(clazz.equals(String.class));
        clazz = GenericsUtils.getGenericParamType(A.class.getField("map").getGenericType());
        assertTrue(clazz.equals(Integer.class));
    }
}
