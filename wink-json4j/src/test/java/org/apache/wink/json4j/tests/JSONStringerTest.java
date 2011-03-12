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

import junit.framework.TestCase;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.json4j.JSONStringer;

/**
 * Tests for the basic Java JSONStringer
 * Since this clase is very similar to writer, only a few basic
 * tests are done.  Generally, f the writer works, so will
 * the stringer.
 */
public class JSONStringerTest extends TestCase {

    /**
     * Test the contructor.
     */
    public void test_new() {
        JSONStringer jStringer = new JSONStringer();
    }

    /**
     * Test the String empty object contructor.
     */
    public void test_WriteEmptyObject() {
        Exception ex = null;
        try{
            JSONStringer jStringer = new JSONStringer();
            jStringer.object();
            jStringer.endObject();
            String str = jStringer.toString();
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
            JSONStringer jStringer = new JSONStringer();
            jStringer.object();
            jStringer.close();
            String str = jStringer.toString();
            System.out.println("STRING: " + str);
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
            JSONStringer jStringer = new JSONStringer();
            jStringer.array();
            jStringer.endArray();
            String str = jStringer.toString();
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
     * Test a simple object with multiple keys of varying types
     */
    public void test_WriteObjectComplex() {
        Exception ex = null;
        try{
            JSONStringer jStringer = new JSONStringer();
            jStringer.object();
            jStringer.key("string");
            jStringer.value("String1");
            jStringer.key("bool");
            jStringer.value(false);
            jStringer.key("number");
            jStringer.value(1);

            // Place an object
            jStringer.key("object");
            jStringer.object();
            jStringer.key("string");
            jStringer.value("String2");
            jStringer.endObject();

            // Place an array
            jStringer.key("array");
            jStringer.array();
            jStringer.value(1);
            jStringer.value((double)2);
            jStringer.value((short)3);
            jStringer.endArray();

            //Close top object.
            jStringer.endObject();

            String str = jStringer.toString();

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
            JSONStringer jStringer = new JSONStringer();
            jStringer.array();
            jStringer.value("String1");
            jStringer.value(false);
            jStringer.value(1);

            // Place an object
            jStringer.object();
            jStringer.key("string");
            jStringer.value("String2");
            jStringer.endObject();

            // Place an array
            jStringer.array();
            jStringer.value(1);
            jStringer.value((double)2);
            jStringer.value((short)3);
            jStringer.endArray();

            //Close top array.
            jStringer.endArray();

            String str = jStringer.toString();

            // Verify it parses.
            JSONArray test = new JSONArray(str);
            assertTrue(str.equals("[\"String1\",false,1,{\"string\":\"String2\"},[1,2.0,3]]"));
        }catch(Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }


    /**
     * Test that trying operations after the stringer was returned that they fail.
     */
    public void test_OptsAfterCloseFail() {
        Exception ex = null;
        try{
            JSONStringer jStringer = new JSONStringer();
            jStringer.array();
            jStringer.toString();

            // This should die.
            jStringer.endArray();
        }catch(Exception ex1){
            ex = ex1;
        }
        assertTrue(ex instanceof IllegalStateException);
    }

}
