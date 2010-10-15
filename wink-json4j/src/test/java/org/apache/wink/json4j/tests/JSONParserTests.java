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

/**
 * Basic junit imports.
 */
import java.io.InputStream;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.apache.wink.json4j.JSON;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONArtifact;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.json4j.utils.XML;

/**
 * Tests for the basic Java JSON model parser
 */
public class JSONParserTests extends TestCase {

    /**
     * Test a basic transform of an XML file to a JSON string, reparse with generic parser to validate generic parser,
     * then with compact emit for checking.
     */
    public void testJSONGenericObjectParse() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("simple.xml");

        try {
            String json = XML.toJson(is);
            JSONArtifact jsonA = JSON.parse(json);

            assertTrue(jsonA instanceof JSONObject);

            StringWriter strWriter = new StringWriter();
            jsonA.write(strWriter);
            System.out.println("JSON compacted text (jObject):");
            System.out.println(strWriter.toString());
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic parse of a JSONArray in text form
     * then with compact emit for checking.
     */
    public void testJSONGenericArrayParse() {
        Exception ex = null;
        try {
            String json = "[ \"foo\", true, 1, null ]";
            JSONArtifact jsonA = JSON.parse(json);

            assertTrue(jsonA instanceof JSONArray);

            StringWriter strWriter = new StringWriter();
            jsonA.write(strWriter);
            System.out.println("JSON compacted text (jArray):");
            System.out.println(strWriter.toString());
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic parse of a JSONArray that had starting whitespace in text form
     * then with compact emit for checking.
     */
    public void testJSONGenericObjectParse_startingWhitespace() {
        Exception ex = null;
        try {
            String json = "\t\t\t    \b\n\f\r   \t\t  { \"foo\": true, \"bar\": 1, \"noVal\": null }";
            JSONArtifact jsonA = JSON.parse(json);

            assertTrue(jsonA instanceof JSONObject);

            StringWriter strWriter = new StringWriter();
            jsonA.write(strWriter);
            System.out.println("JSON compacted text (jObject):");
            System.out.println(strWriter.toString());
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic parse of a JSONArray that had starting whitespace in text form
     * then with compact emit for checking.
     */
    public void testJSONGenericArrayParse_startingWhitespace() {
        Exception ex = null;
        try {
            String json = "\t\t\t    \b\n\f\r   \t\t  [ \"foo\", true, 1, null ]";
            JSONArtifact jsonA = JSON.parse(json);

            assertTrue(jsonA instanceof JSONArray);

            StringWriter strWriter = new StringWriter();
            jsonA.write(strWriter);
            System.out.println("JSON compacted text (jArray):");
            System.out.println(strWriter.toString());
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

}
