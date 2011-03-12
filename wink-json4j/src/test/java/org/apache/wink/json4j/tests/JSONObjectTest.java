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

package org.apache.wink.json4j.tests;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

/**
 * Tests for the basic Java JSONObject model
 */
public class JSONObjectTest extends TestCase {

    /**
     * Test the noargs contructor.
     */
    public void test_new() {
        JSONObject jObject = new JSONObject();
        assertTrue(jObject != null);
        assertTrue(jObject.length() == 0);
    }

    /**
     * Test the String empty object contructor.
     */
    public void test_newFromEmptyObjectString() {
        JSONObject jObject = null;
        Exception ex = null;
        // Load from empty object string.
        try {
            jObject = new JSONObject("{}");
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
        assertTrue(jObject != null);
        assertTrue(jObject.length() == 0);
    }

    /**
     * Test the String non-empty object contructor.
     */
    public void test_newFromString() {
        JSONObject jObject = null;
        Exception ex = null;
        // Load a basic JSON string
        try {
            jObject = new JSONObject("{\"foo\":\"bar\", \"bool\": true}");
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
        assertTrue(jObject != null);
        assertTrue(jObject.length() == 2);
    }

    /**
     * Test the construction from a reader.
     */
    public void test_newFromReader() {
        JSONObject jObject = null;
        Exception ex = null;
        // read in a basic JSON file that has all the various types in it.
        try {
            Reader rdr = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("utf8_basic.json"), "UTF-8");
            jObject = new JSONObject(rdr);
            rdr.close();
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(jObject != null);
        assertTrue(jObject.length() == 12);
        assertTrue(ex == null);
    }

    /**
     * Test the construction from a stream.
     */
    public void test_newFromStream() {
        JSONObject jObject = null;
        Exception ex = null;
        // read in a basic JSON file that has all the various types in it.
        // Inputstreams are read as UTF-8 by the underlying parser.
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("utf8_basic.json");
            jObject = new JSONObject(is);
            is.close();
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(jObject != null);
        assertTrue(jObject.length() == 12);
        assertTrue(ex == null);
    }

    /**
     * Test the construction from a simple map.
     */
    public void test_newFromMap() {
        JSONObject jObject = null;
        JSONArray jArr = null;
        Exception ex = null;
        HashMap map = new HashMap();
        map.put("string", "This is a string");
        map.put("null", null);
        map.put("integer", new Integer(1));
        map.put("bool", new Boolean(true));
        map.put("strArr", new String[]{"first", "second", "third"});

        // Load a JSON object from a map with JSONable values.
        try {
            jObject = new JSONObject(map);
            jArr = (JSONArray)jObject.get("strArr");
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(jObject != null);
        assertTrue(jObject.length() == 5);
        assertTrue("first".equals((String)jArr.get(0)));
        assertTrue("second".equals((String)jArr.get(1)));
        assertTrue("third".equals((String)jArr.get(2)));

        assertTrue(ex == null);
    }

    /**
     * Test the noargs contructor.
     */
    public void test_newFromBean() {
        Exception ex = null;
        try {
            Date date = new Date();
            JSONObject ja = new JSONObject(date);
            assertTrue(ja.get("class").equals("java.util.Date"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test the noargs contructor.
     */
    public void test_newFromBeanWithSuper() {
        Exception ex = null;
        try {
            Date date = new Date();
            JSONObject ja = new JSONObject(date, true);
            assertTrue(ja.get("class").equals("java.util.Date"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test the noargs contructor.
     */
    public void test_newFromBeanWithOutSuper() {
        Exception ex = null;
        try {
            Date date = new Date();
            JSONObject ja = new JSONObject(date, false);
            assertTrue(ja.opt("class") == null);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction
     */
    public void test_compact() {
        Exception ex = null;
        try {
            JSONObject jObject = new JSONObject("{\"foo\":\"bar\", \"bool\": true}");
            assertTrue(jObject.getBoolean("bool") == true);
            System.out.println("JSON compacted text (jObject):\n");
            System.out.println(jObject.toString());
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction
     */
    public void test_parseFailure() {
        Exception ex = null;

        try {
            // Verify a malformed JSON string (no closure), fails parse.
            JSONObject jObject = new JSONObject("{\"foo\":\"bar\", \"bool\": true");
            assertTrue(jObject.getBoolean("bool") == true);
            System.out.println("JSON compacted text (jObject):\n");
            System.out.println(jObject.toString());
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Object construction
     */
    public void test_noQuotesParseFailure() {
        Exception ex = null;

        try {
            // Verify a malformed JSON string (no quotes on attributes), fails parse in strict mode.
            JSONObject jObject = new JSONObject("{foo:\"bar\", bool: true}", true);
            assertTrue(jObject.getBoolean("bool") == true);
            System.out.println("JSON compacted text (jObject):\n");
            System.out.println(jObject.toString());
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Object construction, with verbose output
     */
    public void test_verbose() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"foo\":\"bar\", \"bool\": true}");
            assertTrue(jObject.getBoolean("bool") == true);
            System.out.println("JSON indented tab space (jObject):\n");
            System.out.println(jObject.toString(true));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction, with verbose output
     */
    public void test_verbose_depth() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"foo\":\"bar\", \"bool\": true}");
            assertTrue(jObject.getBoolean("bool") == true);
            System.out.println("JSON indented 3 space (jObject):\n");
            System.out.println(jObject.toString(3));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test the key check function.
     */
    public void test_has() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"foo\":\"bar\", \"bool\": false, \"null\": null}");
            assertTrue(jObject.has("foo"));
            assertTrue(jObject.has("bool"));
            assertTrue(jObject.has("null"));
            assertTrue(!jObject.has("noKey"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'put' function
     */
    public void test_putLong() {
        Exception ex = null;
        try {
            JSONObject jObject = new JSONObject();
            jObject.put("long", (long)1);
            Long l = (Long)jObject.get("long");
            assertTrue(l != null);
            assertTrue(l instanceof java.lang.Long);
            assertTrue(jObject.getLong("long") == 1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'put' function
     */
    public void test_putInt() {
        Exception ex = null;
        try {
            JSONObject jObject = new JSONObject();
            jObject.put("int", 1);
            Integer i = (Integer)jObject.get("int");
            assertTrue(i != null);
            assertTrue(i instanceof java.lang.Integer);
            assertTrue(jObject.getInt("int") == 1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'put' function
     */
    public void test_putShort() {
        Exception ex = null;
        try {
            JSONObject jObject = new JSONObject();
            jObject.put("short", (short)1);
            Short s = (Short)jObject.get("short");
            assertTrue(s != null);
            assertTrue(s instanceof java.lang.Short);
            assertTrue(jObject.getShort("short") == 1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'put' function
     */
    public void test_putDouble() {
        Exception ex = null;
        try {
            JSONObject jObject = new JSONObject();
            jObject.put("double", (double)1.123);
            Double d = (Double)jObject.get("double");
            assertTrue(d != null);
            assertTrue(d instanceof java.lang.Double);
            assertTrue(jObject.getDouble("double") == 1.123);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'put' function
     */
    public void test_putBoolean() {
        Exception ex = null;
        try {
            JSONObject jObject = new JSONObject();
            jObject.put("bool", true);
            Boolean b = (Boolean)jObject.get("bool");
            assertTrue(b != null);
            assertTrue(b instanceof java.lang.Boolean);
            assertTrue(jObject.getBoolean("bool") == true);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'put' function
     */
    public void test_putString() {
        Exception ex = null;
        try {
            JSONObject jObject = new JSONObject();
            jObject.put("string", "Hello World.");
            String s = (String)jObject.get("string");
            assertTrue(s != null);
            assertTrue(s instanceof java.lang.String);
            assertTrue(jObject.getString("string").equals("Hello World."));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'put' function
     */
    public void test_putJSONObject() {
        Exception ex = null;
        try {
            JSONObject jObject = new JSONObject();
            jObject.put("object", new JSONObject());
            JSONObject obj = (JSONObject)jObject.get("object");
            assertTrue(obj != null);
            assertTrue(obj instanceof JSONObject);
            assertTrue(((JSONObject)jObject.get("object")).toString().equals("{}"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'put' function
     */
    public void test_putJSONArray() {
        Exception ex = null;
        try {
            JSONObject jObject = new JSONObject();
            jObject.put("array", new JSONArray());
            JSONArray obj = (JSONArray)jObject.get("array");
            assertTrue(obj != null);
            assertTrue(obj instanceof JSONArray);
            assertTrue(((JSONArray)jObject.get("array")).toString().equals("[]"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test append function to convert an existing value to an array.
     */
    public void test_append() {
        Exception ex = null;
        try {
            JSONObject jObject = new JSONObject();
            jObject.put("string", "Hello World.");
            String s = (String)jObject.get("string");
            assertTrue(s != null);
            assertTrue(s instanceof java.lang.String);
            jObject.append("string", "Another string.");
            JSONArray array = (JSONArray)jObject.get("string");
            assertTrue(array != null);
            assertTrue(array instanceof JSONArray);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test append function to convert an existing value (though null) to an array.
     */
    public void test_appendtoNull() {
        Exception ex = null;
        try {
            JSONObject jObject = new JSONObject();
            jObject.put("null", (Object)null);
            String s = (String)jObject.get("null");
            assertTrue(s == null);
            assertTrue(jObject.has("null"));
            jObject.append("null", "Another string.");
            JSONArray array = (JSONArray)jObject.get("null");
            assertTrue(array != null);
            assertTrue(array instanceof JSONArray);
            assertTrue(array.size() == 2);
            assertTrue(array.get(0) == null);
            assertTrue(array.get(1).equals("Another string."));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test append function to convert an existing value to an array.
     */
    public void test_appendArray() {
        Exception ex = null;
        try {
            JSONObject jObject = new JSONObject();
            JSONArray array = new JSONArray();
            array.add("Hello World.");
            jObject.put("array", array);
            JSONArray array1 = (JSONArray)jObject.get("array");
            assertTrue(array1 != null);
            assertTrue(array1 instanceof JSONArray);
            assertTrue(array1 == array);
            assertTrue(array1.size() == 1);
            jObject.append("array", "Another string.");
            JSONArray array2 = (JSONArray)jObject.get("array");
            assertTrue(array2 != null);
            assertTrue(array2 instanceof JSONArray);
            assertTrue(array1 == array2);
            assertTrue(array2.size() == 2);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }


    public void test_EmptyAppend() throws Exception {
        Exception ex = null;
        try {
            JSONObject json = new JSONObject();
            json.append("test", "value");
            JSONArray arr = (JSONArray)json.get("test");
            assertTrue(arr.size() == 1);
            assertTrue(arr.get(0).equals("value"));
        }
        catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
       }
       assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function
     */
    public void test_getLong() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"long\":1}");
            assertTrue(jObject.getLong("long") == 1);

            JSONObject json = new JSONObject("{ Date : 1212790800000 }");
    		assertEquals(1212790800000L, json.getLong("Date"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function
     */
    public void test_getLongNgative() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"long\":-1}");
            assertTrue(jObject.getLong("long") == -1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function
     */
    public void test_getInt() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"int\":1}");
            assertTrue(jObject.getInt("int") == 1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function
     */
    public void test_getIntNegative() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"int\":-1}");
            assertTrue(jObject.getInt("int") == -1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function
     */
    public void test_getDouble() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":1}");
            assertTrue(jObject.getDouble("double") == 1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function
     */
    public void test_getDoubleNegative() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":-1}");
            assertTrue(jObject.getDouble("double") == -1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function
     */
    public void test_getDoubleWithDecimal() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":100.959}");
            assertTrue(jObject.getDouble("double") == 100.959);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function
     */
    public void test_getDoubleNegativeWithDecimal() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":-100.959}");
            assertTrue(jObject.getDouble("double") == -100.959);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function
     */
    public void test_getDoubleWithExponential() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":100959e-3}");
            assertTrue(jObject.getDouble("double") == 100.959);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function
     */
    public void test_getDoubleNegativeWithExponential() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":-100959e-3}");
            assertTrue(jObject.getDouble("double") == -100.959);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function
     */
    public void test_getString() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"string\":\"some string\"}");
            assertTrue(jObject.getString("string").equals("some string"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function
     */
    public void test_getBoolean() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"bool\":true}");
            assertTrue(jObject.getBoolean("bool"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function
     */
    public void test_getBoolean_StringValue() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"bool\":\"true\"}");
            assertTrue(jObject.getBoolean("bool"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'opt' function
     */
    public void test_optLong() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"long\":1}");
            assertTrue(jObject.optLong("long") == 1);
            assertTrue(jObject.optLong("long2", 2) == 2);
            assertTrue(jObject.optLong("long2") == 0);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'opt' function
     */
    public void test_optLongNgative() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"long\":-1}");
            assertTrue(jObject.optLong("long") == -1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'opt' function
     */
    public void test_optInt() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"int\":1}");
            assertTrue(jObject.optInt("int") == 1);
            assertTrue(jObject.optInt("int2", 2) == 2);
            assertTrue(jObject.optInt("int2") == 0);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'opt' function
     */
    public void test_optIntNegative() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"int\":-1}");
            assertTrue(jObject.optInt("int") == -1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'opt' function
     */
    public void test_optDouble() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":1}");
            assertTrue(jObject.optDouble("double") == 1);
            assertTrue(jObject.optDouble("double2", 2) == 2);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'opt' function
     */
    public void test_optDoubleNegative() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":-1}");
            assertTrue(jObject.optDouble("double") == -1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'opt' function
     */
    public void test_optDoubleWithDecimal() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":100.959}");
            assertTrue(jObject.optDouble("double") == 100.959);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'opt' function
     */
    public void test_optDoubleNegativeWithDecimal() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":-100.959}");
            assertTrue(jObject.optDouble("double") == -100.959);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'opt' function
     */
    public void test_optDoubleWithExponential() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":100959e-3}");
            assertTrue(jObject.optDouble("double") == 100.959);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'opt' function
     */
    public void test_optDoubleNegativeWithExponential() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":-100959e-3}");
            assertTrue(jObject.optDouble("double") == -100.959);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'opt' function
     */
    public void test_optString() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"string\":\"some string\"}");
            assertTrue(jObject.optString("string").equals("some string"));
            assertTrue(jObject.optString("string2", "string!").equals("string!"));
            assertTrue(jObject.optString("string2") == null);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'opt' function
     */
    public void test_optBoolean() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"bool\":true}");
            assertTrue(jObject.optBoolean("bool"));
            assertTrue(jObject.optBoolean("bool2") == false);
            assertTrue(jObject.optBoolean("bool2", true) == true);

        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction and helper 'opt' function
     */
    public void test_optBoolean_StringValue() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"bool\":\"true\"}");
            assertTrue(jObject.optBoolean("bool"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test returning of Integer object if within Integer range.
     */
    public void test_opt_ReturnsIntegerClass() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"int\" : " + Integer.MAX_VALUE + "}");
            JSONObject jObject2 = new JSONObject("{\"int\" : " + Integer.MIN_VALUE + "}");
            JSONObject jObject3 = new JSONObject("{\"int\" : 4500}");
            JSONObject jObject4 = new JSONObject("{\"int\" : 0}");
            JSONObject jObject5 = new JSONObject("{\"int\" : 0X7fffffff}");
            JSONObject jObject6 = new JSONObject("{\"int\" : 017777777777}");
            assertEquals( jObject.opt("int").getClass(), Integer.class);
            assertEquals( jObject2.opt("int").getClass(), Integer.class);
            assertEquals( jObject3.opt("int").getClass(), Integer.class);
            assertEquals( jObject4.opt("int").getClass(), Integer.class);
            assertEquals( jObject5.opt("int").getClass(), Integer.class);
            assertEquals( jObject6.opt("int").getClass(), Integer.class);

        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test returning of Long object if value out of Integer range.
     */
    public void test_opt_ReturnsLongClass() {
        Exception ex = null;

        try {
        	Long val1 = Long.valueOf(Integer.MAX_VALUE) + 1;
        	Long val2 = Long.valueOf(Integer.MIN_VALUE) - 1;
            JSONObject jObject = new JSONObject("{\"int\" : " + val1 + "}");
            JSONObject jObject2 = new JSONObject("{\"int\" : " + val2 + "}");
            JSONObject jObject3 = new JSONObject("{\"int\" : 0X" + Long.toHexString(val1.longValue()) + "}");
            JSONObject jObject4 = new JSONObject("{\"int\" : 020000000000}");
            assertEquals( jObject.opt("int").getClass(), Long.class);
            assertEquals( jObject2.opt("int").getClass(), Long.class);
            assertEquals( jObject3.opt("int").getClass(), Long.class);
            assertEquals( jObject4.opt("int").getClass(), Long.class);

        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    public void test_optWithHex() {
    	  Exception ex = null;

          try {
            JSONObject jObject3 = new JSONObject("{\"int\" : 0X7f}");
            assertEquals( jObject3.opt("int"), 127);
            jObject3 = new JSONObject("{\"int\" : 0x7f}");
            assertEquals( jObject3.opt("int"), 127);
            jObject3 = new JSONObject("{\"int\" : -0x99e}");
            assertEquals( jObject3.opt("int"), -2462);
            jObject3 = new JSONObject("{\"int\" : -0X99e}");
            assertEquals( jObject3.opt("int"), -2462);

          } catch (Exception ex1) {
              ex = ex1;
              ex.printStackTrace();
          }
          assertTrue(ex == null);

          try {
              JSONObject jObject3 = new JSONObject("{\"int\" : 343g}");
            } catch (Exception ex1) {
                ex = ex1;
                ex.printStackTrace();
            }
            assertTrue(ex instanceof JSONException);
            try {
                JSONObject jObject3 = new JSONObject("{\"int\" : 343a}");
            } catch (Exception ex1) {
                  ex = ex1;
                  ex.printStackTrace();
            }
            assertTrue(ex instanceof JSONException);

    }

    public void test_optNumberReturnsSameException() {
  	  Exception ex = null;
        // Test to make sure same exception is thrown when hex char is included in
  	    // normal identifier.
        try {
          JSONObject jObject3 = new JSONObject("{\"int\" : 343h}");
          assertEquals( jObject3.opt("int"), 127);


        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
        Throwable cause = ex.getCause();
        assertTrue(cause == null);

        try {
            JSONObject jObject3 = new JSONObject("{\"int\" : 343a}");
            assertEquals( jObject3.opt("int"), 127);
          } catch (Exception ex1) {
              ex = ex1;
          }
          assertTrue(ex instanceof JSONException);
          cause = ex.getCause();
          assertTrue(cause == null);

    }

    public void test_ArrayRetrievalFromJavaArrayInsertion() throws Exception {
        JSONObject json = new JSONObject();
        String[] someArray = { "Hello","World!" };
        json.put("somearray", someArray);
        JSONArray array = json.getJSONArray("somearray");
        assertEquals(array.get(0), "Hello");
        assertEquals(array.get(1), "World!");
    }

    /**************************************************************************/
    /* The following tests all test failure scenarios due to type mismatching.*/
    /**************************************************************************/

    /**
     * Test a basic JSON Object construction and helper 'get' function failure due to type mismatch
     */
    public void test_getLong_typeMisMatch() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"long\":\"1\"}");
            assertTrue(jObject.getLong("long") == 1);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function failure due to type mismatch
     */
    public void test_getDouble_typeMisMatch() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":\"1\"}");
            assertTrue(jObject.getDouble("double") == 1);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function failure due to type mismatch
     */
    public void test_getInt_typeMisMatch() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"int\":\"1\"}");
            assertTrue(jObject.getLong("int") == 1);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function failure due to type mismatch
     */
    public void test_getString_typeMisMatch() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"string\":null}");
            assertTrue(jObject.getString("string") == "1");
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function failure due to type mismatch
     */
    public void test_getBoolean_typeMisMatch() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"bool\":1}");
            assertTrue(jObject.getBoolean("bool") == true);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function failure due to type mismatch
     */
    public void test_getLong_typeMisMatchNull() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"long\":null}");
            assertTrue(jObject.getLong("long") == 1);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function failure due to type mismatch
     */
    public void test_getInt_typeMisMatchNull() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"int\":null}");
            assertTrue(jObject.getLong("int") == 1);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function failure due to type mismatch
     */
    public void test_getDouble_typeMisMatchNull() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"double\":null}");
            assertTrue(jObject.getDouble("double") == 1);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function failure due to type mismatch
     */
    public void test_getString_typeMisMatchNull() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"string\":null}");
            assertTrue(jObject.getString("string") == "1");
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Object construction and helper 'get' function failure due to type mismatch
     */
    public void test_getBoolean_typeMisMatchNull() {
        Exception ex = null;

        try {
            JSONObject jObject = new JSONObject("{\"bool\":null}");
            assertTrue(jObject.getBoolean("bool") == true);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test the iterator of the keys.
     */
    public void test_keys() {
        Exception ex = null;
        HashMap map = new HashMap();
        try {
            JSONObject jObject = new JSONObject("{\"foo\": \"bar\", \"number\": 1, \"bool\":null}");
            Iterator keys = jObject.keys();
            while (keys.hasNext()) {
                String key = (String)keys.next();
                map.put(key, key);
            }
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex  == null);
        assertTrue(map.size() == 3);
        assertTrue(map.get("foo") != null);
        assertTrue(map.get("number") != null);
        assertTrue(map.get("bool") != null);
    }

    /**
     * Test the iterator of the sorted keys.
     */
    public void test_sortedKeys() {
        HashMap map = new HashMap();
        JSONObject jObject = null;
        try {
            jObject = new JSONObject("{\"foo\": \"bar\", \"number\": 1, \"bool\":null}");
        } catch (Exception ex) {
            assertTrue(false);
        }
        Iterator keys = jObject.sortedKeys();
        String[] sKeys = new String[] {"bool", "foo", "number"};
        int i = 0;
        while (keys.hasNext()) {
            String key = (String)keys.next();
            String sKey = sKeys[i];
            i++;
            assertTrue(key.equals(sKey));
        }
    }

    /**
     * Test the toString of JSONObject.
     * Use the value to construct a new object and verify contents match.
     */
    public void test_toString() {
        HashMap map = new HashMap();
        JSONObject jObject = null;
        JSONObject jObject2 = null;
        try {
            jObject = new JSONObject("{\"foo\": \"bar\", \"number\": 1, \"bool\":true}");
            jObject2 = new JSONObject(jObject.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
        try {
            assertTrue(jObject != jObject2);
            assertTrue(jObject.length() == jObject2.length());
            assertTrue(jObject.getString("foo").equals(jObject2.getString("foo")));
            assertTrue(jObject.getBoolean("bool") == jObject2.getBoolean("bool"));
            assertTrue(jObject.getInt("number") == jObject2.getInt("number"));
        } catch (JSONException jex) {
            jex.printStackTrace();
            assertTrue(false);
        }
    }
    
    /**
     * Test JSONObject creation does not throw exception if key is missing and 
     * does not add missing value.
     */
    public void test_MissingKeyIsIgnored() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("key1", "key1");
            obj.put("key2", "key2");
            obj.put("key3", "key3");
            String[] keys = {"key1", "key3"};
            JSONObject testObj = new JSONObject(obj, keys);
            assertTrue(testObj.size() == 2);
            assertTrue(testObj.opt("key2") == null);
            assertTrue(testObj.opt("key3").equals("key3"));
            assertTrue(testObj.opt("key1").equals("key1"));
        }
        catch (JSONException jex) {
            jex.printStackTrace();
            assertTrue(false);
        }
    }

    /***********************************************************/
    /* The following tests checks UTF-8 encoded DBCS characters*/
    /***********************************************************/

    /**
     * Verify a standard UTF-8 file with high character codes (Korean), can be read via a reader and parsed.
     */
    public void test_utf8_korean() {
        Exception ex = null;
        try {
            Reader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("utf8_helloworld_ko.json"), "UTF-8");
            JSONObject jObject = new JSONObject(reader);
            reader.close();
            assertTrue(jObject.getString("greet").equals("\uc548\ub155 \uc138\uc0c1\uc544"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Verify a UTF 8 file with character codes in the lower part will parse and
     * serialize correctly in escaped unicode format (which is valid JSON and easier
     * to debug)
     */
    public void test_utf8_lowerchar() {
        Exception ex = null;
        try {
            Reader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("utf8_lowerchar.json"), "UTF-8");
            JSONObject jObject = new JSONObject(reader);
            reader.close();
            assertTrue(jObject.getString("message").equals("\u00c5\u00c5\u00c5\u00c5"));
            assertTrue(jObject.toString().equals("{\"message\":\"\\u00c5\\u00c5\\u00c5\\u00c5\"}"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }


    /*****************************************************************************************/
    /* These tests check for specific 'behaviors' so that the parser is compatible to others */
    /* and allows comments, etc.                                                             */
    /*****************************************************************************************/

    public void test_CStyleComment() throws Exception {
        try {
            JSONObject jObj = new JSONObject("/* comment */ { 'test' : 'value' }");
            assertTrue(jObj.has("test"));
            assertTrue(jObj.getString("test").equals("value"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    public void test_CPPComment() throws Exception {
        try {
            JSONObject jObj = new JSONObject("// test comment\n{'test':'value'}");
            assertTrue(jObj.has("test"));
            assertTrue(jObj.getString("test").equals("value"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    public void test_CStyleCommentWithACommentCharInMiddle() throws Exception {
    	 try {
             JSONObject jObj = new JSONObject("/* * */ { 'test' : 'value' }");
             assertTrue(jObj.has("test"));
             assertTrue(jObj.getString("test").equals("value"));

             JSONObject jObj2 = new JSONObject("/* / */ { 'test' : 'value' }");
             assertTrue(jObj2.has("test"));
             assertTrue(jObj2.getString("test").equals("value"));
         } catch (Exception ex) {
             ex.printStackTrace();
             assertTrue(false);
         }
    }

    public void test_UnquotedObjectKey() throws Exception {
        try {
            JSONObject jObj = new JSONObject("{test:'value'}");
            assertTrue(jObj.has("test"));
            assertTrue(jObj.getString("test").equals("value"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test special characters in unquoted string key.
     */
    public void test_UnquotedObjectKeyWithSpecialChars() throws Exception {
        try {
            JSONObject jObj = new JSONObject("{test-key:'value'}");
            assertTrue(jObj.has("test-key"));
            assertTrue(jObj.getString("test-key").equals("value"));

            jObj = new JSONObject("{test0:'value'}");
            assertTrue(jObj.has("test0"));
            assertTrue(jObj.getString("test0").equals("value"));

            jObj = new JSONObject("{test$:'value'}");
            assertTrue(jObj.has("test$"));
            assertTrue(jObj.getString("test$").equals("value"));

            jObj = new JSONObject("{test_key:'value'}");
            assertTrue(jObj.has("test_key"));
            assertTrue(jObj.getString("test_key").equals("value"));

        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test special characters in unquoted string value.
     */
    public void test_UnquotedObjectValueWithSpecialChars() throws Exception {
        try {
            JSONObject jObj = new JSONObject("{test:@value}");
            assertTrue(jObj.has("test"));
            assertTrue(jObj.getString("test").equals("@value"));

            jObj = new JSONObject("{test:$100}");
            assertTrue(jObj.has("test"));
            assertTrue(jObj.getString("test").equals("$100"));

            jObj = new JSONObject("{test:$value}");
            assertTrue(jObj.has("test"));
            assertTrue(jObj.getString("test").equals("$value"));

        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
    }

    public void test_CStyleCommentFailsStrict() throws Exception {
        Exception ex1 = null;
        try {
            JSONObject jObj = new JSONObject("/* comment */ { 'test' : 'value' }", true);
        } catch (Exception ex) {
            ex1 = ex;
        }
        assertTrue(ex1 != null);
    }

    public void test_CPPCommentFailsStrict() throws Exception {
        Exception ex1 = null;
        try {
            JSONObject jObj = new JSONObject("// test comment\n{'test':'value'}", true);
        } catch (Exception ex) {
            ex1 = ex;
        }

        assertTrue(ex1 != null);
    }

    public void test_UnquotedObjectKeyFailsStrict() throws Exception {
        Exception ex1 = null;
        try {
            JSONObject jObj = new JSONObject("{test:'value'}", true);
        } catch (Exception ex) {
            ex1 = ex;
        }
        assertTrue(ex1 != null);
    }

  
    public void testIsNull() {
        Map jsonMap = new LinkedHashMap(1);
        jsonMap.put("key1", null);
        jsonMap.put("key2", JSONObject.NULL);
        jsonMap.put("key3", "NOT NULL");
        
        
        JSONObject json = new JSONObject(jsonMap);
        
        assertTrue(json.isNull("key1"));
        assertTrue(json.isNull("key2"));
        assertFalse(json.isNull("key3"));
    }
    


    /*****************************************************************/
    /* The following tests checks basic 'java beans' convert to JSON */
    /*****************************************************************/

    /**
     * Test that a new Java Date serializes 'bean style' when encountered.
     */
    public void test_Date() {
        Exception ex = null;
        try {
            Date date = new Date();
            JSONObject ja = new JSONObject();
            ja.put("date", date);
            JSONObject jsonDate = ja.getJSONObject("date");
            assertTrue(jsonDate instanceof JSONObject);
            assertTrue(jsonDate.get("class").equals("java.util.Date"));
            System.out.println(ja.write(3));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

}
