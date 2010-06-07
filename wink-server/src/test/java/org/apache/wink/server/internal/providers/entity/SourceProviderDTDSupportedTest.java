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

package org.apache.wink.server.internal.providers.entity;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.WinkConfiguration;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.server.internal.servlet.RestServlet;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * intent of this test is to ensure the wink.supportDTDEntityExpansion property gets picked up from wink-default.properties on the server
 *
 */
public class SourceProviderDTDSupportedTest extends MockServletInvocationTest {

    private static String path = null;
    static {
        String classpath = System.getProperty("java.class.path");
        StringTokenizer tokenizer = new StringTokenizer(classpath, System.getProperty("path.separator"));
        while (tokenizer.hasMoreTokens()) {
            path = tokenizer.nextToken();
            if (path.endsWith("test-classes")) {
                break;
            }
        }
    }
    private static final String SOURCE =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<!DOCTYPE data [<!ENTITY file SYSTEM \""+ path +"/etc/SourceProviderTest.txt\">]>" +
        "<message>&file;</message>";
    private static final byte[] SOURCE_BYTES = SOURCE.getBytes();
    private File propFileDefault = null;

    @Override
    public void setUp() throws Exception {
        createWinkDefaultPropsFile();
        super.setUp();
        Mockery mockery = new Mockery();
        final RuntimeContext context = mockery.mock(RuntimeContext.class);
        mockery.checking(new Expectations() {{
            allowing(context).getAttribute(WinkConfiguration.class); will(returnValue(null));
        }});
        
        RuntimeContextTLS.setRuntimeContext(context);
    }
    
    @Override
    public void tearDown() {
        RuntimeContextTLS.setRuntimeContext(null);
        deleteWinkDefaultPropsFile();
    }
    
    @Override
    protected String getPropertiesFile() {
        return propFileDefault.getPath();
    }
    
    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {SourceResource.class};
    }

    @Path("source")
    public static class SourceResource {
        
        @POST
        @Path("domwithdtd")
        public String postDomWithDTD(DOMSource source) throws Exception {
            /*
             * we don't want to trigger a parse in this resource method.  We're testing to see what happened
             * with the SAXSource on the way here.
             */
            return source.getNode().getFirstChild().getNextSibling().getTextContent();
        }

    }

    
    public void testDomWithDTDSupported() throws Exception {
        
        final byte[] SOURCE_BYTES = SOURCE.getBytes();
        
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/source/domwithdtd",
                                                        "application/xml",
                                                        "application/xml",
                                                        SOURCE_BYTES);
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertEquals("YOU SHOULD NOT BE ABLE TO SEE THIS", response.getContentAsString().trim());
        
        // as a sanity check, let's make sure our xml is good:
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource( new StringReader(SOURCE) );
        Document d = builder.parse( is );
        assertEquals("xml is bad", "YOU SHOULD NOT BE ABLE TO SEE THIS", d.getElementsByTagName("message").item(0).getTextContent().trim());
    }
    
    
    // utility method, currently only for use by testDomWithDTDSupported
    private void createWinkDefaultPropsFile() {
        try {
            Field defaultFileField = RestServlet.class.getDeclaredField("PROPERTIES_DEFAULT_FILE");
            defaultFileField.setAccessible(true);
            String filePath = (String)defaultFileField.get(null);
            if (!filePath.startsWith("/")) {
                filePath = "/" + filePath;
            }

            // create the default property file and write some dummy properties to it so it gets picked up by RestServlet
            Properties propsDefault = new Properties();
            propsDefault.put("wink.supportDTDEntityExpansion", "true");
            propFileDefault = createFileWithProperties(filePath, propsDefault);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    // utility method, currently only for use by createWinkDefaultPropsFile
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
    
    // utility method, currently only for use by testDomWithDTDSupported
    private void deleteWinkDefaultPropsFile() {
        if (!propFileDefault.delete()) {
            fail("failed to delete file " + propFileDefault.getPath());
        }
        propFileDefault = null;
    }
}
