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
package org.apache.wink.server.internal.servlet;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map.Entry;


public class RestServletTest extends MockServletInvocationTest {

    private final static String PROP_COMMON_KEY = "propCommonKey";
    private final static String PROP_DEFAULT_VAL = "propDefaultVal";
    private final static String PROP_CUSTOM_VAL = "propCustomVal";
    
    private final static String OTHER_DEFAULT_KEY = "otherDefaultKey";
    private final static String OTHER_DEFAULT_VAL = "otherDefaultVal";
    private final static String OTHER_CUSTOM_KEY = "otherCustomKey";
    private final static String OTHER_CUSTOM_VAL = "otherCustomVal";
    
    private final static String JVM_COMMON1_KEY = "jvmCommon1Key";
    private final static String JVM_DEFAULT1_VAL = "jvmDefault1Val";
    private final static String JVM_CUSTOM1_VAL = "jvmCustom1Val";
    
    private final static String JVM_COMMON2_KEY = "jvmCommon2Key";
    private final static String JVM_DEFAULT2_VAL = "jvmDefault2Val";
    private final static String JVM_CUSTOM2_VAL = "jvmCustom2Val";
    
    private static File propFileDefault = null;
    private static File propFileCustom = null;
    
    @Override
    protected void setUp() throws Exception {
        Field defaultFileField = RestServlet.class.getDeclaredField("PROPERTIES_DEFAULT_FILE");
        defaultFileField.setAccessible(true);
        String filePath = (String)defaultFileField.get(null);
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        
        /*
         * setup is as such, and should be loaded by Wink in this order:
         * 
         * wink-default.properties:
         *      propCommonKey=propDefaultVal
         *      otherDefaultKey=otherDefaultVal
         *      jvmCommon1Key=jvmDefault1Val
         *      
         * JVM system properties:
         *      jvmCommon1Key=jvmCustom1Val
         *      jvmCommon2Key=jvmDefault2Val
         *      
         * (custom property file):
         *      propCommonKey=propCustomVal
         *      otherCustomKey=otherCustomVal
         *      jvmCommon2Key=jvmCustom2Val
         *      
         * RESULTS should be (we're checking that we don't lose properties, and that they are prioritized correctly):
         *      propCommonKey=propCustomVal
         *      otherDefaultKey=otherDefaultVal
         *      jvmCommon1Key=jvmCustom1Val
         *      jvmCommon2Key=jvmCustom2Val
         *      otherCustomKey=otherCustomVal
         *      
         */

        // create the default property file and write some dummy properties to it so it gets picked up by RestServlet
        Properties propsDefault = new Properties();
        propsDefault.put(PROP_COMMON_KEY, PROP_DEFAULT_VAL);
        propsDefault.put(OTHER_DEFAULT_KEY, OTHER_DEFAULT_VAL);
        propsDefault.put(JVM_COMMON1_KEY, JVM_DEFAULT1_VAL);
        propFileDefault = createFileWithProperties(filePath, propsDefault);
        
        System.setProperty(JVM_COMMON1_KEY, JVM_CUSTOM1_VAL);
        System.setProperty(JVM_COMMON2_KEY, JVM_DEFAULT2_VAL);
        
        // create a custom property file with a single override key/value pair and return it from this.getPropertiesFile()
        Properties propsCustom = new Properties();
        propsCustom.put(PROP_COMMON_KEY, PROP_CUSTOM_VAL);
        propsCustom.put(JVM_COMMON2_KEY, JVM_CUSTOM2_VAL);
        propsCustom.put(OTHER_CUSTOM_KEY, OTHER_CUSTOM_VAL);
        propFileCustom = createFileWithProperties(filePath+"_custom", propsCustom);
        
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        if (!propFileDefault.delete()) {
            fail("failed to delete file " + propFileDefault.getPath());
        }
        if (!propFileCustom.delete()) {
            fail("failed to delete file " + propFileCustom.getPath());
        }
        super.tearDown();
    }

    @Override
    protected String getPropertiesFile() {
        return propFileCustom.getPath();
    }

    public void testProperties() throws Exception {
        RestServlet restServlet = (RestServlet)this.getServlet();
        Properties properties = restServlet.getProperties();

        // make sure properties are not lost
        assertEquals(OTHER_DEFAULT_VAL, properties.getProperty(OTHER_DEFAULT_KEY));
        assertEquals(OTHER_CUSTOM_VAL, properties.getProperty(OTHER_CUSTOM_KEY));
        
        // make sure properties were overridden (in other words, custom property file always wins)
        assertEquals(PROP_CUSTOM_VAL, properties.getProperty(PROP_COMMON_KEY));
        assertEquals(JVM_CUSTOM1_VAL, properties.getProperty(JVM_COMMON1_KEY));
        assertEquals(JVM_CUSTOM2_VAL, properties.getProperty(JVM_COMMON2_KEY));
    }
    
    
    // utility method
    private File createFileWithProperties(String relativeFilePath, Properties props) throws Exception {
        // set up the default properties file in the location where the RestServlet will find it upon test execution
        String classPath = System.getProperty("java.class.path");
        
        StringTokenizer tokenizer = new StringTokenizer(classPath, System.getProperty("path.separator"));
        String pathToUse = null;
        while (tokenizer.hasMoreElements()) {
            String temp = tokenizer.nextToken();
            if (temp.endsWith("test-classes")) {
                pathToUse = temp;
                break;
            }
        }
        
        if (pathToUse == null) {
            fail("failed to find test-classes directory to use for temporary creation of " + relativeFilePath);
        }
        
        File propFile = new File(pathToUse + relativeFilePath);
        FileWriter fileWriter = new FileWriter(propFile);
        for (Iterator<Entry<Object, Object>> it = props.entrySet().iterator(); it.hasNext(); ) {
            Entry<Object, Object> entry = (Entry<Object, Object>)it.next();
            fileWriter.write(entry.getKey() + "=" + entry.getValue());
            fileWriter.write(System.getProperty("line.separator"));
        }
        fileWriter.flush();
        fileWriter.close();
        return propFile;
    }

}
