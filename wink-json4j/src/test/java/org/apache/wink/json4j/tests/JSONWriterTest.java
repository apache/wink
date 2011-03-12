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

import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.json4j.JSONWriter;

/**
 * Tests for the basic Java JSONWriter
 */
public class JSONWriterTest extends TestCase {

    /**
     * Test the contructor.
     */
    public void test_new() {
        StringWriter w = new StringWriter();
        JSONWriter jWriter = new JSONWriter(w);
    }

    /**
     * Test the String empty object contructor.
     */
    public void test_WriteEmptyObject() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.endObject();
            String str = w.toString();
            // Verify it parses.
            JSONObject test = new JSONObject(str);
            assertTrue(str.equals("{}"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test the String empty object contructor.
     */
    public void test_WriteEmptyObjectClose() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.close();
            String str = w.toString();
            // Verify it parses.
            JSONObject test = new JSONObject(str);
            assertTrue(str.equals("{}"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test the String empty object contructor.
     */
    public void test_WriteEmptyArray() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.array();
            jWriter.endArray();
            String str = w.toString();
            // Verify it parses.
            JSONArray test = new JSONArray(str);
            assertTrue(str.equals("[]"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test the String empty object contructor.
     */
    public void test_WriteEmptyArrayClose() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.array();
            jWriter.close();

            String str = w.toString();
            // Verify it parses.
            JSONArray test = new JSONArray(str);
            assertTrue(str.equals("[]"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a simple object with a key + value of string
     */
    public void test_WriteObjectAttrString() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.key("foo");
            jWriter.value("bar");
            jWriter.close();
            String str = w.toString();
            // Verify it parses.
            JSONObject test = new JSONObject(str);
            assertTrue(str.equals("{\"foo\":\"bar\"}"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a simple object with a key + value of int
     */
    public void test_WriteObjectAttrInt() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.key("foo");
            jWriter.value(1);
            jWriter.close();
            String str = w.toString();
            // Verify it parses.
            JSONObject test = new JSONObject(str);
            assertTrue(str.equals("{\"foo\":1}"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a simple object with a key + value of long
     */
    public void test_WriteObjectAttrLong() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.key("foo");
            jWriter.value((long)1);
            jWriter.close();
            String str = w.toString();
            // Verify it parses.
            JSONObject test = new JSONObject(str);
            assertTrue(str.equals("{\"foo\":1}"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a simple object with a key + value of short
     */
    public void test_WriteObjectAttrShort() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.key("foo");
            jWriter.value((short)1);
            jWriter.close();
            String str = w.toString();
            // Verify it parses.
            JSONObject test = new JSONObject(str);
            assertTrue(str.equals("{\"foo\":1}"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a simple object with a key + value of Double
     */
    public void test_WriteObjectAttrDouble() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.key("foo");
            jWriter.value((double)100.959);
            jWriter.close();
            String str = w.toString();
            // Verify it parses.
            JSONObject test = new JSONObject(str);
            assertTrue(str.equals("{\"foo\":100.959}"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a simple object with a key + value of boolean
     */
    public void test_WriteObjectAttrBoolean() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.key("foo");
            jWriter.value(true);
            jWriter.close();
            String str = w.toString();
            // Verify it parses.
            JSONObject test = new JSONObject(str);
            assertTrue(str.equals("{\"foo\":true}"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a simple object with a key + value of Object
     */
    public void test_WriteObjectAttrObject() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.key("foo");
            jWriter.object();
            jWriter.key("foo");
            jWriter.value(true);
            jWriter.endObject();
            jWriter.endObject();
            jWriter.close();
            String str = w.toString();
            
            // Verify it parses.
            JSONObject test = new JSONObject(str);
            assertTrue(str.equals("{\"foo\":{\"foo\":true}}"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a simple object with a key + value of Object
     */
    public void test_WriteObjectAttrArray() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.key("foo");
            jWriter.array();
            jWriter.value(true);
            jWriter.endArray();
            jWriter.endObject();
            jWriter.close();
            String str = w.toString();
            
            // Verify it parses.
            JSONObject test = new JSONObject(str);
            assertTrue(str.equals("{\"foo\":[true]}"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a simple object with a key + value of Object
     */
    public void test_WriteObjectAttrJSONObject() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.key("foo");

            // Verify we can put a JSONObject into the stream!
            JSONObject jObj = new JSONObject();
            jObj.put("foo", true);
            jWriter.value(jObj);

            jWriter.endObject();
            jWriter.close();

            String str = w.toString();
            // Verify it parses.
            JSONObject test = new JSONObject(str);
            assertTrue(str.equals("{\"foo\":{\"foo\":true}}"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a simple object with a key + value of array
     */
    public void test_WriteObjectAttrJSONArray() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.key("foo");

            // Verify we can put a JSONObject into the stream!
            JSONArray jArray = new JSONArray();
            jArray.put(true);
            jWriter.value(jArray);

            jWriter.endObject();
            jWriter.close();

            String str = w.toString();
            // Verify it parses.
            JSONObject test = new JSONObject(str);
            assertTrue(str.equals("{\"foo\":[true]}"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }


    /**
     * Test a simple object with multiple keys of varying types
     */
    public void test_WriteObjectComplex() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.key("string");
            jWriter.value("String1");
            jWriter.key("bool");
            jWriter.value(false);
            jWriter.key("number");
            jWriter.value(1);

            // Place an object
            jWriter.key("object");
            jWriter.object();
            jWriter.key("string");
            jWriter.value("String2");
            jWriter.endObject();

            // Place an array
            jWriter.key("array");
            jWriter.array();
            jWriter.value(1);
            jWriter.value((double)2);
            jWriter.value((short)3);
            jWriter.endArray();

            //Close top object.
            jWriter.endObject();

            jWriter.close();

            String str = w.toString();

            // Verify it parses.
            JSONObject test = new JSONObject(str);
            assertTrue(str.equals("{\"string\":\"String1\",\"bool\":false,\"number\":1,\"object\":{\"string\":\"String2\"},\"array\":[1,2.0,3]}"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a simple object with multiple keys of varying types
     */
    public void test_WriteArrayComplex() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.array();
            jWriter.value("String1");
            jWriter.value(false);
            jWriter.value(1);

            // Place an object
            jWriter.object();
            jWriter.key("string");
            jWriter.value("String2");
            jWriter.endObject();

            // Place an array
            jWriter.array();
            jWriter.value(1);
            jWriter.value((double)2);
            jWriter.value((short)3);
            jWriter.endArray();

            //Close top array.
            jWriter.endArray();

            jWriter.close();
            String str = w.toString();

            // Verify it parses.
            JSONArray test = new JSONArray(str);
            assertTrue(str.equals("[\"String1\",false,1,{\"string\":\"String2\"},[1,2.0,3]]"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /*******************************/
    /*All the error condition tests*/
    /*******************************/

    /**
     * Test that setting a value in an object without defining its key fails.
     */
    public void test_ObjectNoKeyValueFail() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.value(true);
            jWriter.endObject();
        }catch(Exception ex1){
            ex = ex1;
        }
        assertTrue(ex instanceof IllegalStateException);
    }


    /**
     * Test that setting a value without a key (after another key/value was set), fails
     */
    public void test_ObjectKeyValueNoKeyValueFail() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.object();
            jWriter.key("foo");
            jWriter.value(true);
            
            // This should die with IllegalStateException...
            jWriter.value(false);
            jWriter.endObject();
        }catch(Exception ex1){
            ex = ex1;
        }
        assertTrue(ex instanceof IllegalStateException);
    }

    /**
     * Test that setting a key while not in an object fails
     */
    public void test_NoObjectKeyFail() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            
            // This should die.
            jWriter.key("foo");
        }catch(Exception ex1){
            ex = ex1;
        }
        assertTrue(ex instanceof IllegalStateException);
    }

    /**
     * Test that setting a value while not in an array or object fails
     */
    public void test_NoObjectValueFail() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            
            // This should die.
            jWriter.value("foo");
        }catch(Exception ex1){
            ex = ex1;
        }
        assertTrue(ex instanceof IllegalStateException);
    }

    /**
     * Test that trying to set keys while in an array fails.
     */
    public void test_ArrayKeyFail() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.array();

            // This should die.
            jWriter.key("foo");
        }catch(Exception ex1){
            ex = ex1;
        }
        assertTrue(ex instanceof IllegalStateException);
    }

    /**
     * Test that trying operations after the writer has been closed fails.
     */
    public void test_OptsAfterCloseFail() {
        Exception ex = null;
        try{
            StringWriter w = new StringWriter();
            JSONWriter jWriter = new JSONWriter(w);
            jWriter.array();
            jWriter.close();

            // This should die.
            jWriter.endArray();
        }catch(Exception ex1){
            ex = ex1;
        }
        assertTrue(ex instanceof IllegalStateException);
    }

}
