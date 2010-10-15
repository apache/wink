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

import junit.framework.TestCase;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

/**
 * Tests for the basic Java JSONArray model
 */
public class JSONArrayTest extends TestCase {

    /**
     * Test the noargs contructor.
     */
    public void test_new() {
        JSONArray jObject = new JSONArray();
        assertTrue(jObject != null);
        assertTrue(jObject.length() == 0);
    }

    /**
     * Test the creation of a JSONArray from a string array
     */
    public void test_newFromStringArray() {
        Exception ex = null;
        try {
            String[] strArray = new String[] {"hello", "world", null, "after null"};

            JSONArray jArray = new JSONArray(strArray);
            assertTrue(jArray.length() == 4);
            assertTrue(jArray.getString(0).equals("hello"));
            assertTrue(jArray.optString(2) == null);
            assertTrue(jArray.getString(3).equals("after null"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test the String empty object contructor.
     */
    public void test_newFromEmptyObjectString() {
        JSONArray jObject = null;
        Exception ex = null;
        // Load from empty object string.
        try {
            jObject = new JSONArray("[]");
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
        JSONArray jObject = null;
        Exception ex = null;
        // Load a basic JSON string
        try {
            jObject = new JSONArray("[\"foo\", \"bar\", \"bool\", true]");
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
        assertTrue(jObject != null);
        assertTrue(jObject.length() == 4);
    }

    /**
     * Test the construction from a reader.
     */
    public void test_newFromReader() {
        JSONArray jObject = null;
        Exception ex = null;
        // read in a basic JSON file of a toplevel array that has all the various types in it.
        try {
            Reader rdr = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("utf8_basic_array.json"), "UTF-8");
            jObject = new JSONArray(rdr);
            rdr.close();
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(jObject != null);
        assertTrue(jObject.length() == 7);
        assertTrue(ex == null);
    }

    /**
     * Test the construction from a stream.
     */
    public void test_newFromStream() {
        JSONArray jObject = null;
        Exception ex = null;
        // read in a basic JSON file of a toplevel array that has all the various types in it.
        // Inputstreams are read as UTF-8 by the underlying parser.
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("utf8_basic_array.json");
            jObject = new JSONArray(is);
            is.close();
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(jObject != null);
        assertTrue(jObject.length() == 7);
        assertTrue(ex == null);
    }

    /**
     * Test the String non-empty object contructor parse failure.
     */
    public void test_newFromStringFailure() {
        JSONArray jObject = null;
        Exception ex = null;

        // Load a basic JSON string that's not valid in strict (unquoted string)
        try {
            jObject = new JSONArray("[\"foo\", bar, \"bool\", true]", true);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex != null);
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Array construction and helper 'put' function
     */
    public void test_putLong() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray();
            jArray.put((long)1);
            Long l = (Long)jArray.get(0);
            assertTrue(l != null);
            assertTrue(l instanceof java.lang.Long);
            assertTrue(jArray.getLong(0) == 1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'put' function
     */
    public void test_putInt() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray();
            jArray.put(1);
            Integer i = (Integer)jArray.get(0);
            assertTrue(i != null);
            assertTrue(i instanceof java.lang.Integer);
            assertTrue(jArray.getInt(0) == 1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'put' function
     */
    public void test_putShort() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray();
            jArray.put((short)1);
            Short s = (Short)jArray.get(0);
            assertTrue(s != null);
            assertTrue(s instanceof java.lang.Short);
            assertTrue(jArray.getShort(0) == 1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'put' function
     */
    public void test_putDouble() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray();
            jArray.put((double)1.123);
            Double d = (Double)jArray.get(0);
            assertTrue(d != null);
            assertTrue(d instanceof java.lang.Double);
            assertTrue(jArray.getDouble(0) == 1.123);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'put' function
     */
    public void test_putBoolean() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray();
            jArray.put(true);
            Boolean b = (Boolean)jArray.get(0);
            assertTrue(b != null);
            assertTrue(b instanceof java.lang.Boolean);
            assertTrue(jArray.getBoolean(0) == true);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'put' function
     */
    public void test_putString() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray();
            jArray.put("Hello World.");
            String s = (String)jArray.get(0);
            assertTrue(s != null);
            assertTrue(s instanceof java.lang.String);
            assertTrue(jArray.getString(0).equals("Hello World."));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'put' function
     */
    public void test_putNull() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray();
            jArray.put((Object)null);
            String s = (String)jArray.get(0);
            assertTrue(s == null);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'put' function
     */
    public void test_putJSONObject() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray();
            jArray.put(new JSONObject());
            JSONObject obj = (JSONObject)jArray.get(0);
            assertTrue(obj != null);
            assertTrue(obj instanceof JSONObject);
            assertTrue(((JSONObject)jArray.get(0)).toString().equals("{}"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'put' function
     */
    public void test_putJSONArray() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray();
            jArray.put(new JSONArray());
            JSONArray obj = (JSONArray)jArray.get(0);
            assertTrue(obj != null);
            assertTrue(obj instanceof JSONArray);
            assertTrue(((JSONArray)jArray.get(0)).toString().equals("[]"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getLong() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[1]");
            assertTrue(jArray.getLong(0) == (long)1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getLongNgative() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[-1]");
            assertTrue(jArray.getLong(0) == (long)-1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getInt() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[1]");
            assertTrue(jArray.getInt(0) == 1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getIntNegative() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[-1]");
            assertTrue(jArray.getInt(0) == -1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getDouble() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[1]");
            assertTrue(jArray.getDouble(0) == (double)1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getDoubleNegative() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[-1]");
            assertTrue(jArray.getDouble(0) == (double)-1);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getDoubleWithDecimal() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[100.959]");
            assertTrue(jArray.getDouble(0) == (double)100.959);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getDoubleNegativeWithDecimal() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[-100.959]");
            assertTrue(jArray.getDouble(0) == (double)-100.959);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getDoubleWithExponential() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[100959e-3]");
            assertTrue(jArray.getDouble(0) == (double)100.959);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getDoubleNegativeWithExponential() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[-100959e-3]");
            assertTrue(jArray.getDouble(0) == (double)-100.959);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getString() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[\"some string\"]");
            assertTrue(jArray.getString(0).equals("some string"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getBoolean() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[true]");
            assertTrue(jArray.getBoolean(0));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getBoolean_StringValue() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[\"true\"]");
            assertTrue(jArray.getBoolean(0));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function
     */
    public void test_getNull() {
        Exception ex = null;

        try {
            JSONArray jArray = new JSONArray("[null]");
            assertTrue(jArray.get(0) == null);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /***********************************************************************************/
    /* The following tests array expansion when using indexes > than the current array */
    /***********************************************************************************/

    /**
     * Test a basic JSON Array construction and helper 'put' function
     */
    public void test_putIntPosition() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray();
            // Put the int at the noted position (1)
            jArray.put(5, 1);
            System.out.println(jArray.toString());

            assertTrue(jArray.size() == 6);
            Integer i = (Integer)jArray.get(5);
            assertTrue(i != null);
            assertTrue(i instanceof java.lang.Integer);
            assertTrue(jArray.getInt(5) == 1);

            // Verify that the 0 position is a null (expanded)
            i = (Integer)jArray.get(3);
            assertTrue(i == null);
            System.out.println(jArray.toString());
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
     * Test a basic JSON Array construction and helper 'get' function failure due to type mismatch
     */
    public void test_getLong_typeMisMatch() {
        Exception ex = null;

        try {
            JSONArray jArray = new JSONArray("[\"1\"]");
            assertTrue(jArray.getLong(0) == (long)1);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function failure due to type mismatch
     */
    public void test_getDouble_typeMisMatch() {
        Exception ex = null;

        try {
            JSONArray jArray = new JSONArray("[\"1\"]");
            assertTrue(jArray.getDouble(0) == 1);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function failure due to type mismatch
     */
    public void test_getInt_typeMisMatch() {
        Exception ex = null;

        try {
            JSONArray jArray = new JSONArray("[\"1\"]");
            assertTrue(jArray.getLong(0) == (int)1);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function failure due to type mismatch
     */
    public void test_getString_typeMisMatch() {
        Exception ex = null;

        try {
            JSONArray jArray = new JSONArray("[null]");
            assertTrue(jArray.getString(0) == "null");
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function failure due to type mismatch
     */
    public void test_getBoolean_typeMisMatch() {
        Exception ex = null;

        try {
            JSONArray jArray = new JSONArray("[1]");
            assertTrue(jArray.getBoolean(0) == true);
        } catch (Exception ex1) {
            ex = ex1;
        }
        System.out.println("Error: " + ex);
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function failure due to type mismatch
     */
    public void test_getLong_typeMisMatchNull() {
        Exception ex = null;

        try {
            JSONArray jArray = new JSONArray("[null]");
            assertTrue(jArray.getLong(0) == (long)1);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function failure due to type mismatch
     */
    public void test_getInt_typeMisMatchNull() {
        Exception ex = null;

        try {
            JSONArray jArray = new JSONArray("[null]");
            assertTrue(jArray.getLong(0) == (int)1);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function failure due to type mismatch
     */
    public void test_getDouble_typeMisMatchNull() {
        Exception ex = null;

        try {
            JSONArray jArray = new JSONArray("[null]");
            assertTrue(jArray.getDouble(0) == (double)1);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function failure due to type mismatch
     */
    public void test_getString_typeMisMatchNull() {
        Exception ex = null;

        try {
            JSONArray jArray = new JSONArray("[null]");
            assertTrue(jArray.getString(0) == "1");
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a basic JSON Array construction and helper 'get' function failure due to type mismatch
     */
    public void test_getBoolean_typeMisMatchNull() {
        Exception ex = null;

        try {
            JSONArray jArray = new JSONArray("[null]");
            assertTrue(jArray.getBoolean(0) == true);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof JSONException);
    }

    /**
     * Test a 'join' of a JSONArray
     */
    public void test_JoinNoDelimiter() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[1, true, false, null, \"My String\", [1,2,3], {\"foo\":\"bar\"}]");
            String joined = jArray.join("");
            assertTrue(joined.equals("1truefalsenullMy String[1,2,3]{\"foo\":\"bar\"}"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a 'join' of a JSONArray
     */
    public void test_JoinDelimiter() {
        Exception ex = null;
        try {
            JSONArray jArray = new JSONArray("[1, true, false, null, \"My String\", [1,2,3], {\"foo\":\"bar\"}]");
            String joined = jArray.join("|");
            assertTrue(joined.equals("1|true|false|null|My String|[1,2,3]|{\"foo\":\"bar\"}"));
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /*****************************************************************/
    /* The following tests checks basic 'java beans' convert to JSON */
    /*****************************************************************/

    /**
     * Test that a new Java Date serializes 'bean style' when encountered.
     */
    public void test_Date() {
        Exception ex = null;
        try{
            Date date = new Date();
            JSONArray ja = new JSONArray();
            ja.put(date);
            JSONObject jsonDate = ja.getJSONObject(0);
            assertTrue(jsonDate instanceof JSONObject);
            assertTrue(jsonDate.get("class").equals("java.util.Date"));
            System.out.println(ja.write(3));
        } catch (Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

}
