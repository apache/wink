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

import java.util.Date;

import junit.framework.TestCase;

import org.apache.wink.json4j.JSONArtifact;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.json4j.internal.BeanSerializer;

/**
 * Tests for the basic Java Bean serializer
 */
public class BeanSerializerTest extends TestCase {

    /**
     * Test the noargs contructor.
     */
    public void test_Date_WithSuper() {
        Exception ex = null;
        try{
            Date date = new Date();
            JSONArtifact ja = BeanSerializer.toJson(date, true);
            assertTrue(((JSONObject)ja).get("class").equals("java.util.Date"));
            System.out.println(ja.write(3));
        } catch (Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test the noargs contructor.
     */
    public void test_Date_WithOutSuper() {
        Exception ex = null;
        try{
            Date date = new Date();
            JSONArtifact ja = BeanSerializer.toJson(date, false);
            assertTrue(((JSONObject)ja).opt("class") == null);
            System.out.println(ja.write(3));
        } catch (Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    public void test_Date_RebuildWithOutSuper() {
        Exception ex = null;
        try{
            Date date = new Date();
            JSONArtifact ja = BeanSerializer.toJson(date, false);
            assertTrue(((JSONObject)ja).opt("class") == null);
            System.out.println(ja.write(3));
            Date date2 = (Date)BeanSerializer.fromJson((JSONObject)ja);
            System.out.println("Date: " + date2.toString());
        } catch (Exception ex1){
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

}
