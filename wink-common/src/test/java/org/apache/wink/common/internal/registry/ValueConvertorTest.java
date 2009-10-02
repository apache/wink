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

import junit.framework.TestCase;

public class ValueConvertorTest extends TestCase {

    /**
     * custom type with constructor, valueOf, and fromString methods
     */
    static class CustomTypeConstructor {

        private String _value = "";

        public CustomTypeConstructor(String value) {
            _value = value;
        }

        static CustomTypeConstructor valueOf(String value) {
            return new CustomTypeConstructor(value + "_valueOf");
        }

        static CustomTypeConstructor fromString(String value) {
            return new CustomTypeConstructor(value + "_fromString");
        }

        public String toString() {
            return _value;
        }
    }

    /**
     * custom type with both valueOf and fromString methods and no constructor
     */
    static class CustomTypeValueOf {

        private String _value = "";

        private CustomTypeValueOf(String value) {
            _value = value;
        }

        static CustomTypeValueOf valueOf(String value) {
            return new CustomTypeValueOf(value + "_valueOf");
        }

        static CustomTypeValueOf fromString(String value) {
            return new CustomTypeValueOf(value + "_fromString");
        }

        public String toString() {
            return _value;
        }
    }

    /**
     * custom type with valueOf that returns incorrect object type and has no
     * constructor
     */
    static class CustomTypeValueOfWrong {

        private String _value = "";

        private CustomTypeValueOfWrong(String value) {
            _value = value;
        }

        static Object valueOf(String value) {
            // intentionally returning incorrect object type
            return new String(value + "_valueOf");
        }

        public String toString() {
            return _value;
        }
    }

    /**
     * custom type with fromString that returns incorrect object type and has no
     * constructor
     */
    static class CustomTypeFromStringWrong {

        private String _value = "";

        private CustomTypeFromStringWrong(String value) {
            _value = value;
        }

        static Object fromString(String value) {
            // intentionally returning incorrect object type
            return new String(value + "_valueOf");
        }

        public String toString() {
            return _value;
        }
    }

    /**
     * custom type with fromString method only
     */
    static class CustomTypeFromString {
        private String _value = "";

        static CustomTypeFromString fromString(String value) {
            CustomTypeFromString ct = new CustomTypeFromString();
            ct._value = value + "_fromString";
            return ct;
        }

        public String toString() {
            return _value;
        }
    }

    /**
     * enum with implicit valueOf method only
     */
    enum MyEnum {
        SUNDAY, SATURDAY;
    }

    /**
     * enum with fromString method only
     */
    enum MyEnumWithFromString {
        SUNDAY, SATURDAY, SUNDAY_fromString, SATURDAY_fromString;

        public static MyEnumWithFromString fromString(String val) {
            return valueOf(val + "_fromString");
        }
    }

    // make sure constructor is favored over "valueOf" and "fromString"
    public void testConvertorPrecedenceConstructor() throws Exception {
        ValueConvertor constructorConvertor =
            ValueConvertor.createConcreteValueConvertor(CustomTypeConstructor.class,
                                                        CustomTypeConstructor.class);
        assertEquals("VALUE", constructorConvertor.convert("VALUE").toString());
    }

    // make sure "valueOf" is favored over "fromString"
    public void testConvertorPrecedenceNoConstructor() throws Exception {
        ValueConvertor valueOfConvertor =
            ValueConvertor.createConcreteValueConvertor(CustomTypeValueOf.class,
                                                        CustomTypeValueOf.class);
        assertEquals("VALUE_valueOf", valueOfConvertor.convert("VALUE").toString());
    }

    // make sure fallback to "fromString" if no "valueOf" method nor constructor
    // is found
    public void testConvertorPrecedenceNoConstructorNoValueOf() throws Exception {
        ValueConvertor fromStringConvertor =
            ValueConvertor.createConcreteValueConvertor(CustomTypeFromString.class,
                                                        CustomTypeFromString.class);
        assertEquals("VALUE_fromString", fromStringConvertor.convert("VALUE").toString());
    }

    // make sure default enum conversion continues to work
    public void testEnumConvertorDefault() throws Exception {
        ValueConvertor fromStringConvertor =
            ValueConvertor.createValueConvertor(MyEnum.class, MyEnum.class);
        assertEquals(MyEnum.SUNDAY, fromStringConvertor.convert("SUNDAY"));
    }

    // make sure "fromString" is favored over "valueOf" for enums
    public void testEnumConvertorPrecedence() throws Exception {
        ValueConvertor fromStringConvertor =
            ValueConvertor.createValueConvertor(MyEnumWithFromString.class,
                                                MyEnumWithFromString.class);
        assertEquals(MyEnumWithFromString.SUNDAY_fromString, fromStringConvertor.convert("SUNDAY"));
    }

    // make sure wrong Object type returned from valueOf and fromString is
    // handled and reported
    public void testConvertorWrongObjectType() throws Exception {
        try {
            ValueConvertor valueOfConvertor =
                ValueConvertor.createValueConvertor(CustomTypeValueOfWrong.class,
                                                    CustomTypeValueOfWrong.class);
            valueOfConvertor.convert("VALUE");
            fail("Should have got an exception.");
        } catch (Exception e) {
        }

        try {
            ValueConvertor valueOfConvertor =
                ValueConvertor.createValueConvertor(CustomTypeFromStringWrong.class,
                                                    CustomTypeFromStringWrong.class);
            valueOfConvertor.convert("VALUE");
            fail("Should have got an exception.");
        } catch (Exception e) {
        }
    }
}
