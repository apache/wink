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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONObject;
import org.apache.wink.json4j.utils.XML;

/**
 * Tests for all the basic XML Transform functions.
 */
public class XMLTests extends TestCase {

    /**
     * Test a basic transform of an XML file to a JSON string with verbose emit.
     */
    public void testSimpleXMLDocument_AsFileToStringCompact() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("simple.xml");

        try {
            String JSON = XML.toJson(is);
            is.close();
            System.out.println("JSON compacted text:\n");
            System.out.println(JSON);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic transform of an XML file to a JSON string with compact emit.
     */
    public void testSimpleXMLDocument_AsFileToStringVerbose() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("simple.xml");

        try {
            String JSON = XML.toJson(is, true);
            is.close();
            System.out.println("JSON non-compact text:\n");
            System.out.println(JSON);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }


    /**
     * Test a complex transform of an XML file to a JSON string with verbose emit.
     */
    public void testComplexXMLDocument_AsFileToStringCompact() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("simple.xml");

        try {
            String JSON = XML.toJson(is);
            is.close();
            System.out.println("JSON compacted text:\n");
            System.out.println(JSON);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }

    /**
     * Test a complex transform of an XML file to a JSON string with compact emit.
     */
    public void testComplexXMLDocument_AsFileToStringVerbose() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("complex.xml");

        try {
            String JSON = XML.toJson(is, true);
            is.close();
            System.out.println("JSON non-compact text:\n");
            System.out.println(JSON);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }

    /**
     * Test a complex transform of an XML file to a JSON string with compact emit.
     */
    public void testComplexXMLDocumentWithLongText_AsFileToStringVerbose() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("long-text.xml");

        try {
            String JSON = XML.toJson(is, true);
            is.close();
            System.out.println("JSON non-compact text with LONG string:\n");
            System.out.println(JSON);
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic transform of an XML stream to a JSON string with verbose emit.
     */
    public void testSimpleXMLDocument_AsStreamToStringCompact() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("simple.xml");

        try {
            String JSON = XML.toJson(is);
            is.close();
            System.out.println("JSON compacted text:\n");
            System.out.println(JSON);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }



    /**
     * Test a basic transform of an XML stream to a JSON string with compact emit.
     */
    public void testSimpleXMLDocument_AsStreamToStringVerbose() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("simple.xml");

        try {
            String JSON = XML.toJson(is, true);
            is.close();
            System.out.println("JSON non-compact text:\n");
            System.out.println(JSON);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }


    /**
     * Test a complex transform of an XML stream to a JSON string with verbose emit.
     */
    public void testComplexXMLDocument_AsStreamToStringCompact() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("complex.xml");

        try {
            String JSON = XML.toJson(is);
            is.close();
            System.out.println("JSON compacted text:\n");
            System.out.println(JSON);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }

    /**
     * Test a complex transform of an XML stream to a JSON string with verbose emit.
     */
    public void testComplexXMLDocument_AsStreamToStringVerbose() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("complex.xml");

        try {
            String JSON = XML.toJson(is, true);
            is.close();
            System.out.println("JSON non-compact text:\n");
            System.out.println(JSON);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic transform of an XML stream to a JSON stream with compact emit.
     */
    public void testSimpleXMLDocument_AsStreamToStreamCompact() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("simple.xml");
        File fileOut = new File("target/json_output/simple_json_compact.json");

        try {
            File parent = fileOut.getParentFile();
            parent.mkdirs();

            FileOutputStream fos = new FileOutputStream(fileOut);
            XML.toJson(is, fos);
            is.close();
            fos.flush();
            fos.close();
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }

    /**
     * Test a basic transform of an XML stream to a JSON stream with verbose emit.
     */
    public void testSimpleXMLDocument_AsStreamToStreamVerbose() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("simple.xml");
        File fileOut = new File("target/json_output/simple_json_verbose.json");

        try {
            File parent = fileOut.getParentFile();
            parent.mkdirs();

            FileOutputStream fos = new FileOutputStream(fileOut);
            XML.toJson(is, fos, true);
            is.close();
            fos.flush();
            fos.close();
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }

    /**
     * Test a complex transform of an XML stream to a JSON stream with compact emit.
     */
    public void testComplexXMLDocument_AsStreamToStreamCompact() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("complex.xml");
        File fileOut = new File("target/json_output/complex_json_compact.json");

        try {
            File parent = fileOut.getParentFile();
            parent.mkdirs();

            FileOutputStream fos = new FileOutputStream(fileOut);
            XML.toJson(is, fos);
            is.close();
            fos.flush();
            fos.close();
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }

    /**
     * Test a complex transform of an XML stream to a JSON stream with verbose emit.
     */
    public void testComplexXMLDocument_AsStreamToStreamVerbose() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("complex.xml");
        File fileOut = new File("target/json_output/complex_json_verbose.json");

        try {
            File parent = fileOut.getParentFile();
            parent.mkdirs();

            FileOutputStream fos = new FileOutputStream(fileOut);
            XML.toJson(is, fos, true);
            is.close();
            fos.flush();
            fos.close();
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }

    /**
     * Test a malformed XML document failure.
     */
    public void testSimpleXMLDocument_AsFileToStringCompactFailure() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("simple_broken.xml");

        try {
            String JSON = XML.toJson(is, true);
            System.out.println("JSON compacted text:\n");
            System.out.println(JSON);
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex instanceof org.xml.sax.SAXException);
    }

    /**
     * Test conversions of ATOM feeds to JSON.
     */
    public void testAtomFeedConversion1() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("atom-xml-entry1");
        File oFile   = new File("target/json_output/atomentry-xml-json1.json");

        try {
            File parent = oFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(oFile);
            XML.toJson(is, fos, true);
            is.close();
            fos.flush();
            fos.close();
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test conversions of ATOM feeds to JSON.
     */
    public void testAtomFeedConversion2() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("atom-xml-feed1");
        File oFile   = new File("target/json_output/atomfeed-xml-json2.json");

        try {
            File parent = oFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(oFile);
            XML.toJson(is, fos, true);
            is.close();
            fos.flush();
            fos.close();
        } catch (Exception ex1) {
            ex = ex1;
            ex.printStackTrace();
        }
        assertTrue(ex == null);
    }

    /**
     * Test a simple low character UTF-8 string.
     */
    public void testLowCharacterUTF8String() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("utf8-lowerchar.xml");
        File fileOut = new File("target/json_output/utf8-lowerchar.json");

        try {
            File parent = fileOut.getParentFile();
            parent.mkdirs();

            FileOutputStream fos = new FileOutputStream(fileOut);
            XML.toJson(is, fos, false);
            is.close();
            fos.flush();
            fos.close();

            is = fileOut.toURI().toURL().openStream();// this.getClass().getClassLoader().getResourceAsStream("target/json_output/utf8-lowerchar.json");
            JSONObject jObject = new JSONObject(is);
            is.close();
            String str = (String)jObject.get("hi");
            String expected="\u00C5\u00C5\u00C5\u00C5";
            //Compare this to the string with unicode \u00C5 in it.
            assertTrue(expected.equals(str));

        } catch (Exception ex1) {
            ex1.printStackTrace();
            ex = ex1;
        }
        assertTrue(ex == null);
    }

    /**
     * Test a simple array of UTF-8 strings.
     */
    public void testArrayUTF8String() {
        Exception ex = null;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("utf8-array.xml");
        File fileOut = new File("target/json_output/utf8-array.json");

        try {
            File parent = fileOut.getParentFile();
            parent.mkdirs();

            FileOutputStream fos = new FileOutputStream(fileOut);
            XML.toJson(is, fos, false);
            is.close();
            fos.flush();
            fos.close();

            is = fileOut.toURI().toURL().openStream(); //this.getClass().getClassLoader().getResourceAsStream("utf8-array.json");
            JSONObject jObject = new JSONObject(is);
            is.close();
            String expected="\u592a\u548c\u6bbf";
            JSONObject search = (JSONObject)jObject.get("search");
            JSONObject payload = (JSONObject)search.get("payLoad");
            JSONObject ssug = (JSONObject)payload.get("sSug");
            JSONArray items = (JSONArray)ssug.get("item");

            for (int i = 0; i <items.size(); i++) {
                String str = (String)items.get(i);
                assertTrue(expected.equals(str));
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
            ex = ex1;
        }
        assertTrue(ex == null);
    }

    /************************************
     * Performance tests.
     /***********************************/

    /**
     * Test a complex transform of an XML file to a JSON string with compact emit.
    public void testComplexXMLDocument_AsFileToStringCompactTiming() {
        Exception ex   = null;
        File file      = new File("xmlfiles/complex.xml");
        long endTime   = 0;
        long startTime = 0;

        try {
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                String JSON = XML.toJson(file);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Complex xml timing.  Total time for 10000 transforms: [" + (endTime - startTime) + "ms].  Time per execution: [" + ((endTime - startTime)/10000) + "ms]");
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }

    /**
     * Test a complex transform of an XML file to a JSON string with compact emit.
    public void testSimpleXMLDocument_AsFileToStringCompactTiming() {
        Exception ex   = null;
        File file      = new File("xmlfiles/simple.xml");
        long endTime   = 0;
        long startTime = 0;


        try {
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 10000; i++) {
                String JSON = XML.toJson(file);
            }
            endTime = System.currentTimeMillis();
            System.out.println("Simple xml timing.  Total time for 10000 transforms: [" + (endTime - startTime) + "ms].  Time per execution: [" + ((endTime - startTime)/10000) + "ms]");
        } catch (Exception ex1) {
            ex = ex1;
        }
        assertTrue(ex == null);
    }
    */
}
