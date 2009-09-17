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
     * custom type with both valueOf and fromString methods
     */
    static class CustomType {

        private String _value = "";

        public CustomType(String value) {
            _value = value;
        }

        static CustomType valueOf(String value) {
            return new CustomType(value + "_valueOf");
        }

        static CustomType fromString(String value) {
            return new CustomType(value + "_fromString");
        }

        public String toString() {
            return _value;
        }
    }

    /**
     * custom type with fromString method only
     */
    static class CustomTypeNoValueOf {
        private String _value = "";

        public CustomTypeNoValueOf(String value) {
            _value = value;
        }

        static CustomTypeNoValueOf fromString(String value) {
            return new CustomTypeNoValueOf(value + "_fromString");
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

    // make sure "valueOf" is favored over "fromString"
    public void testConvertorPrecedence() throws Exception {
        ValueConvertor valueOfConvertor =
            ValueConvertor.createConcreteValueConvertor(CustomType.class, CustomType.class);
        assertEquals("VALUE_valueOf", valueOfConvertor.convert("VALUE").toString());
    }

    // make sure fallback to "fromString" if no "valueOf" method is found
    public void testConvertorPrecedenceNoValueOf() throws Exception {
        ValueConvertor fromStringConvertor =
            ValueConvertor.createConcreteValueConvertor(CustomTypeNoValueOf.class,
                                                        CustomTypeNoValueOf.class);
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

}
