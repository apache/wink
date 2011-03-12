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

package org.apache.wink.json4j.compat.tests;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.wink.json4j.compat.JSONArray;
import org.apache.wink.json4j.compat.JSONException;
import org.apache.wink.json4j.compat.JSONFactory;
import org.apache.wink.json4j.compat.JSONObject;

/**
 * Tests for the basic Java JSONObject model
 */
public class ApacheJSONObjectTest extends TestCase {

    /**
     * Test the noargs contructor.
     */
    public void test_new() {
        System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
        JSONFactory factory = JSONFactory.newInstance();
        JSONObject jObject = factory.createJSONObject();
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
        try{
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            jObject = factory.createJSONObject("{}");
        }catch(Exception ex1){
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
        try{
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            jObject = factory.createJSONObject("{\"foo\":\"bar\", \"bool\": true}");
        }catch(Exception ex1){
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
        try{
            Reader rdr = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("utf8_basic.json"), "UTF-8");
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            jObject = factory.createJSONObject(rdr);
            rdr.close();
        }catch(Exception ex1){
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
        Exception ex = null;
        HashMap map = new HashMap();
        map.put("string", "This is a string");
        map.put("null", null);
        map.put("integer", new Integer(1));
        map.put("bool", new Boolean(true));

        // Load a JSON object from a map with JSONable values.
        try{
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            jObject = factory.createJSONObject(map);
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(jObject != null);
        assertTrue(jObject.length() == 4);
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Object construction
     */
    public void test_compact() {
        Exception ex = null;
        try {
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"foo\":\"bar\", \"bool\": true}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"foo\":\"bar\", \"bool\": true");
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
    /*  Allowing this for now.
    public void test_noQuotesParseFailure() {
        Exception ex = null;

        try {
            // Verify a malformed JSON string (no quotes on attributes), fails parse.
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{foo:\"bar\", bool: true}");
            assertTrue(jObject.getBoolean("bool") == true);
            System.out.println("JSON compacted text (jObject):\n");
            System.out.println(jObject.toString());
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    } */

    /**
     * Test a basic JSON Object construction, with verbose output
     */
    public void test_verbose() {
        Exception ex = null;

        try {
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"foo\":\"bar\", \"bool\": true}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"foo\":\"bar\", \"bool\": false, \"null\": null}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject();
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject();
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject();
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject();
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject();
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject();
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject();
            jObject.put("object", factory.createJSONObject());
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject();
            jObject.put("array", factory.createJSONArray());
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject();
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject();
            jObject.put("null", (Object)null);
            String s = (String)jObject.get("null");
            assertTrue(s == null);
            assertTrue(jObject.has("null"));
            jObject.append("null", "Another string.");
            JSONArray array = (JSONArray)jObject.get("null");

            System.out.println("Array size: " + array.length());
            for (int i = 0; i < array.length(); i++) {
                System.out.println("Val at location: [" + i + "] is: " + array.get(i));
            }

            assertTrue(array != null);
            assertTrue(array instanceof JSONArray);
            assertTrue(array.length() == 2);
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
            System.out.println("In: test_appendArray");
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject();
            JSONArray array = factory.createJSONArray();
            array.put("Hello World.");
            jObject.put("array", array);
            JSONArray array1 = (JSONArray)jObject.get("array");
            assertTrue(array1 != null);
            assertTrue(array1 instanceof JSONArray);
            assertTrue(array1.length() == 1);
            jObject.append("array", "Another string.");
            JSONArray array2 = (JSONArray)jObject.get("array");
            assertTrue(array2 != null);
            assertTrue(array2 instanceof JSONArray);
            assertTrue(array2.length() == 2);
        } catch (Exception ex1) {
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"long\":1}");
            assertTrue(jObject.getLong("long") == 1);
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"long\":-1}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"int\":1}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"int\":-1}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"double\":1}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"double\":-1}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"double\":100.959}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"double\":-100.959}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"double\":100959e-3}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"double\":-100959e-3}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"string\":\"some string\"}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"bool\":true}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"bool\":\"true\"}");
            assertTrue(jObject.getBoolean("bool"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"long\":\"1\"}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"double\":\"1\"}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"int\":\"1\"}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"string\":null}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"bool\":1}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"long\":null}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"int\":null}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"double\":null}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"string\":null}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"bool\":null}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject("{\"foo\": \"bar\", \"number\": 1, \"bool\":null}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            jObject = factory.createJSONObject("{\"foo\": \"bar\", \"number\": 1, \"bool\":null}");
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            jObject = factory.createJSONObject("{\"foo\": \"bar\", \"number\": 1, \"bool\":true}");
            jObject2 = factory.createJSONObject(jObject.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }
        try{
            assertTrue(jObject != jObject2);
            assertTrue(jObject.length() == jObject2.length());
            assertTrue(jObject.getString("foo").equals(jObject2.getString("foo")));
            assertTrue(jObject.getBoolean("bool") == jObject2.getBoolean("bool"));
            assertTrue(jObject.getInt("number") == jObject2.getInt("number"));
        }catch(JSONException jex){
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject(reader);
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
            System.setProperty("org.apache.wink.common.model.json.factory.impl", "org.apache.wink.json4j.compat.impl.ApacheJSONFactory");
            JSONFactory factory = JSONFactory.newInstance();
            JSONObject jObject = factory.createJSONObject(reader);
            reader.close();
            assertTrue(jObject.getString("message").equals("\u00c5\u00c5\u00c5\u00c5"));
            assertTrue(jObject.toString().equals("{\"message\":\"\\u00c5\\u00c5\\u00c5\\u00c5\"}"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

}
