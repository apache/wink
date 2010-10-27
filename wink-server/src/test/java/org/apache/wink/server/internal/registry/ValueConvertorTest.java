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

package org.apache.wink.server.internal.registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ValueConvertorTest extends MockServletInvocationTest {

    private static List<Class<?>> resources = new LinkedList<Class<?>>();

    static {
        for (Class<?> cls : ValueConvertorTest.class.getClasses()) {
            if (cls.getSimpleName().endsWith("Resource")) {
                resources.add(cls);
            }
        }
    }

    @Override
    protected Class<?>[] getClasses() {
        return resources.toArray(new Class<?>[resources.size()]);
    }

    public static class StringConstructorClass implements Comparable<StringConstructorClass> {

        public String value;

        public StringConstructorClass(String value) {
            if (value.equals("3"))
                throw new WebApplicationException(Response.status(499)
                    .entity("WebApplicationException in StringConstructorClass").build());
            else if (value.equals("4"))
                throw new RuntimeException();
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            return value.equals(((StringConstructorClass)obj).value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        public int compareTo(StringConstructorClass o) {
            return value.compareTo(o.value);
        }
    }

    public static class ValueOfClass implements Comparable<ValueOfClass> {

        public String value;

        public ValueOfClass(String value, String dummy) {
            this.value = value;
        }

        public static ValueOfClass valueOf(String value) {
            return new ValueOfClass(value, value);
        }

        @Override
        public boolean equals(Object obj) {
            return value.equals(((ValueOfClass)obj).value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        public int compareTo(ValueOfClass o) {
            return value.compareTo(o.value);
        }
    }

    @Path("pathParam/{p}")
    public static class PathParamResource {

        @PathParam("p")
        public StringConstructorClass fieldPropertyPathParam;

        // Basic types

        @GET
        @Path("byte")
        @Produces
        public void getbyte(@PathParam("p") byte p) {
            assertEquals(1, p);
        }

        @GET
        @Path("byteDefault")
        @Produces
        public void getbyteDefault(@PathParam("k") byte p) {
            assertEquals(0, p);
        }

        @GET
        @Path("Byte")
        @Produces
        public void getByte(@PathParam("p") Byte p) {
            assertEquals(new Byte((byte)1), p);
        }

        @GET
        @Path("ByteDefault")
        @Produces
        public void getByteDefault(@PathParam("k") Byte p) {
            assertEquals(null, p);
        }

        @GET
        @Path("short")
        @Produces
        public void getshort(@PathParam("p") short p) {
            assertEquals(1, p);
        }

        @GET
        @Path("shortDefault")
        @Produces
        public void getshortDefault(@PathParam("k") short p) {
            assertEquals(0, p);
        }

        @GET
        @Path("Short")
        @Produces
        public void getShort(@PathParam("p") Short p) {
            assertEquals(new Short((short)1), p);
        }

        @GET
        @Path("ShortDefault")
        @Produces
        public void getShortDefault(@PathParam("k") Short p) {
            assertEquals(null, p);
        }

        @GET
        @Path("int")
        @Produces
        public void getInt(@PathParam("p") int p) {
            assertEquals(1, p);
        }

        @GET
        @Path("intDefault")
        @Produces
        public void getIntDefault(@PathParam("k") int p) {
            assertEquals(0, p);
        }

        @GET
        @Path("Integer")
        @Produces
        public void getInteger(@PathParam("p") Integer p) {
            assertEquals(new Integer(1), p);
        }

        @GET
        @Path("IntegerDefault")
        @Produces
        public void getIntegerDefault(@PathParam("k") Integer p) {
            assertEquals(null, p);
        }

        @GET
        @Path("long")
        @Produces
        public void getlong(@PathParam("p") long p) {
            assertEquals(1L, p);
        }

        @GET
        @Path("longDefault")
        @Produces
        public void getlongDefault(@PathParam("k") long p) {
            assertEquals(0L, p);
        }

        @GET
        @Path("Long")
        @Produces
        public void getLong(@PathParam("p") Long p) {
            assertEquals(new Long(1L), p);
        }

        @GET
        @Path("LongDefault")
        @Produces
        public void getLongDefault(@PathParam("k") Long p) {
            assertEquals(null, p);
        }

        @GET
        @Path("float")
        @Produces
        public void getfloat(@PathParam("p") float p) {
            assertEquals(1.0f, p);
        }

        @GET
        @Path("floatDefault")
        @Produces
        public void getfloatDefault(@PathParam("k") float p) {
            assertEquals(0.0f, p);
        }

        @GET
        @Path("Float")
        @Produces
        public void getFloat(@PathParam("p") Float p) {
            assertEquals(new Float(1.0f), p);
        }

        @GET
        @Path("FloatDefault")
        @Produces
        public void getFloatDefault(@PathParam("k") Float p) {
            assertEquals(null, p);
        }

        @GET
        @Path("double")
        @Produces
        public void getdouble(@PathParam("p") double p) {
            assertEquals(1.0d, p);
        }

        @GET
        @Path("doubleDefault")
        @Produces
        public void getdoubleDefault(@PathParam("k") double p) {
            assertEquals(0.0d, p);
        }

        @GET
        @Path("Double")
        @Produces
        public void getDouble(@PathParam("p") Double p) {
            assertEquals(new Double(1.0d), p);
        }

        @GET
        @Path("DoubleDefault")
        @Produces
        public void getDoubleDefault(@PathParam("k") Double p) {
            assertEquals(null, p);
        }

        @GET
        @Path("char")
        @Produces
        public void getchar(@PathParam("p") char p) {
            assertEquals('1', p);
        }

        @GET
        @Path("charDefault")
        @Produces
        public void getcharDefault(@PathParam("k") char p) {
            assertEquals('\u0000', p);
        }

        @GET
        @Path("Character")
        @Produces
        public void getCharacter(@PathParam("p") Character p) {
            assertEquals(new Character('1'), p);
        }

        @GET
        @Path("CharacterDefault")
        @Produces
        public void getCharacterDefault(@PathParam("k") Character p) {
            assertEquals(null, p);
        }

        @GET
        @Path("String")
        @Produces
        public void getString(@PathParam("p") String p) {
            assertEquals("1", p);
        }

        @GET
        @Path("StringDefault")
        @Produces
        public void getStringDefault(@PathParam("k") String p) {
            assertEquals(null, p);
        }

        @GET
        @Path("booleanTrue")
        @Produces
        public void getbooleanTrue(@PathParam("p") boolean p) {
            assertEquals(true, p);
        }

        @GET
        @Path("booleanFalse")
        @Produces
        public void getbooleanFalse(@PathParam("p") boolean p) {
            assertEquals(false, p);
        }

        @GET
        @Path("booleanDefault")
        @Produces
        public void getbooleanDefault(@PathParam("k") boolean p) {
            assertEquals(false, p);
        }

        @GET
        @Path("BooleanTrue")
        @Produces
        public void getBooleanTrue(@PathParam("p") Boolean p) {
            assertEquals(Boolean.TRUE, p);
        }

        @GET
        @Path("BooleanFalse")
        @Produces
        public void getBooleanFalse(@PathParam("p") Boolean p) {
            assertEquals(Boolean.FALSE, p);
        }

        @GET
        @Path("BooleanDefault")
        @Produces
        public void getBooleanDefault(@PathParam("k") Boolean p) {
            assertEquals(null, p);
        }

        // Complex types

        @GET
        @Path("StringConstructor")
        @Produces
        public void getStringConstructor(@PathParam("p") StringConstructorClass p) {
            assertEquals(new StringConstructorClass("1"), p);
        }

        @GET
        @Path("StringConstructorDefault")
        @Produces
        public void getStringConstructorDefault(@PathParam("k") StringConstructorClass p) {
            assertEquals(null, p);
        }

        @GET
        @Path("ValueOf")
        @Produces
        public void getValueOf(@PathParam("p") ValueOfClass p) {
            assertEquals(ValueOfClass.valueOf("1"), p);
        }

        @GET
        @Path("ValueOfDefault")
        @Produces
        public void getValueOfDefault(@PathParam("k") ValueOfClass p) {
            assertEquals(null, p);
        }

        // List:  NOTE: very strange to support PathParam as a list, but if people
        // really want it, they have it.  See JavaDoc for PathParam; there is no statement
        // about supporting PathParam as a list.  All other *Param are support as list.

        @GET
        @Path("ListInteger/{p}")
        @Produces
        public void getListInteger(@PathParam("p") List<Integer> p) {
            List<Integer> list = new ArrayList<Integer>();
            list.add(2);
            list.add(1);
            assertEquals(list, p);
        }

        @GET
        @Path("ListIntegerDefault")
        @Produces
        public void getListIntegerDefault(@PathParam("k") List<Integer> p) {
            assertEquals(new ArrayList<Integer>(), p);
        }

        @GET
        @Path("ListString/{p}")
        @Produces
        public void getListString(@PathParam("p") List<String> p) {
            List<String> list = new ArrayList<String>();
            list.add("2");
            list.add("1");
            assertEquals(list, p);
        }

        @GET
        @Path("ListStringDefault")
        @Produces
        public void getListStringDefault(@PathParam("k") List<String> p) {
            assertEquals(new ArrayList<String>(), p);
        }

        @GET
        @Path("ListStringConstructor/{p}")
        @Produces
        public void getListStringConstructor(@PathParam("p") List<StringConstructorClass> p) {
            List<StringConstructorClass> list = new ArrayList<StringConstructorClass>();
            list.add(new StringConstructorClass("2"));
            list.add(new StringConstructorClass("1"));
            assertEquals(list, p);
        }

        @GET
        @Path("ListStringConstructorDefault")
        @Produces
        public void getListStringConstructorDefault(@PathParam("k") List<StringConstructorClass> p) {
            assertEquals(new ArrayList<String>(), p);
        }

        @GET
        @Path("ListValueOf/{p}")
        @Produces
        public void getListValueOf(@PathParam("p") List<ValueOfClass> p) {
            List<ValueOfClass> list = new ArrayList<ValueOfClass>();
            list.add(ValueOfClass.valueOf("2"));
            list.add(ValueOfClass.valueOf("1"));
            assertEquals(list, p);
        }

        @GET
        @Path("ListValueOfDefault")
        @Produces
        public void getListValueOfDefault(@PathParam("k") List<ValueOfClass> p) {
            assertEquals(new ArrayList<String>(), p);
        }

        // Set

        @GET
        @Path("SetInteger/{p}")
        @Produces
        public void getSetInteger(@PathParam("p") Set<Integer> p) {
            Set<Integer> list = new HashSet<Integer>();
            list.add(2);
            list.add(1);
            assertEquals(list, p);
        }

        @GET
        @Path("SetIntegerDefault")
        @Produces
        public void getSetIntegerDefault(@PathParam("k") Set<Integer> p) {
            assertEquals(new HashSet<Integer>(), p);
        }

        @GET
        @Path("SetString/{p}")
        @Produces
        public void getSetString(@PathParam("p") Set<String> p) {
            Set<String> list = new HashSet<String>();
            list.add("2");
            list.add("1");
            assertEquals(list, p);
        }

        @GET
        @Path("SetStringDefault")
        @Produces
        public void getSetStringDefault(@PathParam("k") Set<String> p) {
            assertEquals(new HashSet<String>(), p);
        }

        @GET
        @Path("SetStringConstructor/{p}")
        @Produces
        public void getSetStringConstructor(@PathParam("p") Set<StringConstructorClass> p) {
            Set<StringConstructorClass> list = new HashSet<StringConstructorClass>();
            list.add(new StringConstructorClass("2"));
            list.add(new StringConstructorClass("1"));
            assertEquals(list, p);
        }

        @GET
        @Path("SetStringConstructorDefault")
        @Produces
        public void getSetStringConstructorDefault(@PathParam("k") Set<StringConstructorClass> p) {
            assertEquals(new HashSet<StringConstructorClass>(), p);
        }

        @GET
        @Path("SetValueOf/{p}")
        @Produces
        public void getSetValueOf(@PathParam("p") Set<ValueOfClass> p) {
            Set<ValueOfClass> list = new HashSet<ValueOfClass>();
            list.add(ValueOfClass.valueOf("2"));
            list.add(ValueOfClass.valueOf("1"));
            assertEquals(list, p);
        }

        @GET
        @Path("SetValueOfDefault")
        @Produces
        public void getSetValueOfDefault(@PathParam("k") Set<ValueOfClass> p) {
            assertEquals(new HashSet<ValueOfClass>(), p);
        }

        // SortedSet

        @GET
        @Path("SortedSetInteger/{p}")
        @Produces
        public void getSortedSetInteger(@PathParam("p") SortedSet<Integer> p) {
            SortedSet<Integer> list = new TreeSet<Integer>();
            list.add(2);
            list.add(1);
            assertEquals(list, p);
        }

        @GET
        @Path("SortedSetIntegerDefault")
        @Produces
        public void getSortedSetIntegerDefault(@PathParam("k") SortedSet<Integer> p) {
            assertEquals(new TreeSet<Integer>(), p);
        }

        @GET
        @Path("SortedSetString/{p}")
        @Produces
        public void getSortedSetString(@PathParam("p") SortedSet<String> p) {
            SortedSet<String> list = new TreeSet<String>();
            list.add("2");
            list.add("1");
            assertEquals(list, p);
        }

        @GET
        @Path("SortedSetStringDefault")
        @Produces
        public void getSortedSetStringDefault(@PathParam("k") SortedSet<String> p) {
            assertEquals(new TreeSet<String>(), p);
        }

        @GET
        @Path("SortedSetStringConstructor/{p}")
        @Produces
        public void getSortedSetStringConstructor(@PathParam("p") SortedSet<StringConstructorClass> p) {
            SortedSet<StringConstructorClass> list = new TreeSet<StringConstructorClass>();
            list.add(new StringConstructorClass("2"));
            list.add(new StringConstructorClass("1"));
            assertEquals(list, p);
        }

        @GET
        @Path("SortedSetStringConstructorDefault")
        @Produces
        public void getSortedSetStringConstructorDefault(@PathParam("k") SortedSet<StringConstructorClass> p) {
            assertEquals(new TreeSet<StringConstructorClass>(), p);
        }

        @GET
        @Path("SortedSetValueOf/{p}")
        @Produces
        public void getSortedSetValueOf(@PathParam("p") SortedSet<ValueOfClass> p) {
            SortedSet<ValueOfClass> list = new TreeSet<ValueOfClass>();
            list.add(ValueOfClass.valueOf("2"));
            list.add(ValueOfClass.valueOf("1"));
            assertEquals(list, p);
        }

        @GET
        @Path("SortedSetValueOfDefault")
        @Produces
        public void getSortedSetValueOfDefault(@PathParam("k") SortedSet<ValueOfClass> p) {
            assertEquals(new TreeSet<ValueOfClass>(), p);
        }

        @GET
        @Path("FieldProperty")
        @Produces
        public void getFieldProperty() {
            assertEquals(new StringConstructorClass("1"), this.fieldPropertyPathParam);
        }
    }

    private void assertInvocation(String path) {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", path, MediaType.APPLICATION_XML);
        try {
            MockHttpServletResponse mockResponse = invoke(mockRequest);
            assertEquals(204, mockResponse.getStatus());
        } catch (Exception e) {
            e.printStackTrace(System.out);
            fail("method invocation failed");
        }
    }

    private void assertWebAppException(String path, int status) throws Exception {
        MockHttpServletRequest mockRequest =
            MockRequestConstructor.constructMockRequest("GET", path, MediaType.APPLICATION_XML);
        try {
            MockHttpServletResponse mockResponse = invoke(mockRequest);
            assertEquals(status, mockResponse.getStatus());
        } catch (IOException e) {
            e.printStackTrace(System.out);
            fail("method invocation failed");
        }
    }

    public void testByte() {
        assertInvocation("pathParam/1/byte");
        assertInvocation("pathParam/1/byteDefault");
        assertInvocation("pathParam/1/Byte");
        assertInvocation("pathParam/1/ByteDefault");
    }

    public void testShort() {
        assertInvocation("pathParam/1/short");
        assertInvocation("pathParam/1/shortDefault");
        assertInvocation("pathParam/1/Short");
        assertInvocation("pathParam/1/ShortDefault");
    }

    public void testInteger() {
        assertInvocation("pathParam/1/int");
        assertInvocation("pathParam/1/intDefault");
        assertInvocation("pathParam/1/Integer");
        assertInvocation("pathParam/1/IntegerDefault");
    }

    public void testLong() {
        assertInvocation("pathParam/1/long");
        assertInvocation("pathParam/1/longDefault");
        assertInvocation("pathParam/1/Long");
        assertInvocation("pathParam/1/LongDefault");
    }

    public void testFloat() {
        assertInvocation("pathParam/1/float");
        assertInvocation("pathParam/1.0/float");
        assertInvocation("pathParam/1/floatDefault");
        assertInvocation("pathParam/1/Float");
        assertInvocation("pathParam/1.0/Float");
        assertInvocation("pathParam/1/FloatDefault");
    }

    public void testDouble() {
        assertInvocation("pathParam/1/double");
        assertInvocation("pathParam/1.0/double");
        assertInvocation("pathParam/1/doubleDefault");
        assertInvocation("pathParam/1/Double");
        assertInvocation("pathParam/1.0/Double");
        assertInvocation("pathParam/1/DoubleDefault");
    }

    public void testCharacter() {
        assertInvocation("pathParam/1/char");
        assertInvocation("pathParam/12/char");
        assertInvocation("pathParam/1/charDefault");
        assertInvocation("pathParam/1/Character");
        assertInvocation("pathParam/12/Character");
        assertInvocation("pathParam/1/CharacterDefault");
    }

    public void testString() {
        assertInvocation("pathParam/1/String");
        assertInvocation("pathParam/1/StringDefault");
    }

    public void testBoolean() {
        assertInvocation("pathParam/true/booleanTrue");
        assertInvocation("pathParam/false/booleanFalse");
        assertInvocation("pathParam/ff/booleanDefault");
        assertInvocation("pathParam/true/BooleanTrue");
        assertInvocation("pathParam/false/BooleanFalse");
        assertInvocation("pathParam/ff/BooleanDefault");
    }

    public void testComplex() throws Exception {
        assertInvocation("pathParam/1/StringConstructor");
        assertInvocation("pathParam/1/StringConstructorDefault");
        assertInvocation("pathParam/1/ValueOf");
        assertInvocation("pathParam/1/ValueOfDefault");
        assertWebAppException("pathParam/4/StringConstructor", 404);
        assertWebAppException("pathParam/3/StringConstructor", 499);
    }

    public void testList() {
        assertInvocation("pathParam/1/ListInteger/2");
        assertInvocation("pathParam/1/ListIntegerDefault");
        assertInvocation("pathParam/1/ListString/2");
        assertInvocation("pathParam/1/ListStringDefault");
        assertInvocation("pathParam/1/ListStringConstructor/2");
        assertInvocation("pathParam/1/ListStringConstructorDefault");
        assertInvocation("pathParam/1/ListValueOf/2");
        assertInvocation("pathParam/1/ListValueOfDefault");
    }

    public void testSet() {
        assertInvocation("pathParam/1/SetInteger/2");
        assertInvocation("pathParam/1/SetIntegerDefault");
        assertInvocation("pathParam/1/SetString/2");
        assertInvocation("pathParam/1/SetStringDefault");
        assertInvocation("pathParam/1/SetStringConstructor/2");
        assertInvocation("pathParam/1/SetStringConstructorDefault");
        assertInvocation("pathParam/1/SetValueOf/2");
        assertInvocation("pathParam/1/SetValueOfDefault");
    }

    public void testSortedSet() {
        assertInvocation("pathParam/1/SortedSetInteger/2");
        assertInvocation("pathParam/1/SortedSetIntegerDefault");
        assertInvocation("pathParam/1/SortedSetString/2");
        assertInvocation("pathParam/1/SortedSetStringDefault");
        assertInvocation("pathParam/1/SortedSetStringConstructor/2");
        assertInvocation("pathParam/1/SortedSetStringConstructorDefault");
        assertInvocation("pathParam/1/SortedSetValueOf/2");
        assertInvocation("pathParam/1/SortedSetValueOfDefault");
    }

    public void testFieldPropertyPathParam() throws Exception {
        assertInvocation("pathParam/1/FieldProperty");
        assertWebAppException("pathParam/4/FieldProperty", 404);
        assertWebAppException("pathParam/3/FieldProperty", 499);
    }
}
