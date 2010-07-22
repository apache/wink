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
package org.apache.wink.server.internal.registry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.application.ApplicationFileLoader;
import org.apache.wink.common.internal.providers.entity.StringProvider;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.wink.server.internal.RequestProcessor;
import org.apache.wink.server.internal.registry.providers.Provider1;
import org.apache.wink.server.internal.registry.providers.Provider2;
import org.apache.wink.server.internal.registry.providers.Provider3;
import org.apache.wink.server.internal.registry.providers.Provider4;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.server.internal.servlet.RestServlet;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class ProvidersRegistrySystemTest extends MockServletInvocationTest {
    
    private static File winkprovidersFile = null;
    private static File winkapplicationFile = null;
    private static String rootPath = null;
    private static final String backupExt = "_backup";

    // store this instance so we can check that it was loaded from getSingletons after our Application has loaded
    private static Provider1 provider1Singleton = new Provider1();
    
    @Override
    protected void setUp() throws Exception {
        
        Field defaultFileField = ApplicationFileLoader.class.getDeclaredField("CORE_APPLICATION");
        defaultFileField.setAccessible(true);
        String filePath = (String)defaultFileField.get(null);
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        
        // use the path where CORE_APPLICATION is found as the root for the WINK_APPLICATION file
        String classPath = System.getProperty("java.class.path");
        StringTokenizer tokenizer = new StringTokenizer(classPath, System.getProperty("path.separator"));
        while (tokenizer.hasMoreElements()) {
            String temp = tokenizer.nextToken();
            if (temp.contains("test-classes")) {
                rootPath = temp;
                break;
            }
        }
        
        // save a backup of winkprovidersFile if it exists:
        copyfile(rootPath + filePath, rootPath + filePath + backupExt);
        
        // create a custom wink-providers file with known entries for this test
        ArrayList<String> lines = new ArrayList<String>();
        lines.add(Provider1.class.getName());  // should be ignored due to it already being listed in getSingletons
        lines.add(Provider2.class.getName());  // should be ignored due to it already being listed in getClasses
        lines.add(Provider3.class.getName());  // accepted
        lines.add(StringProvider.class.getName());  // accepted, need it to complete end to end test
        winkprovidersFile = createFile(rootPath + filePath, lines.toArray(new String[0]));
        
        defaultFileField = ApplicationFileLoader.class.getDeclaredField("WINK_APPLICATION");
        defaultFileField.setAccessible(true);
        filePath = (String)defaultFileField.get(null);
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        
        // create a custom wink-application file with known entries for this test
        lines = new ArrayList<String>();
        lines.add(Provider1.class.getName());  // should be ignored due to it already being listed in getSingletons
        lines.add(Provider2.class.getName());  // should be ignored due to it already being listed in getClasses
        lines.add(Provider3.class.getName());  // should be ignored due to it already being listed in wink-providers
        lines.add(Provider4.class.getName());  // accepted
        winkapplicationFile = createFile(rootPath + filePath, lines.toArray(new String[0]));
        
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        if (!winkapplicationFile.delete()) {
            fail("failed to delete file " + winkapplicationFile.getPath());
        }
        if (!winkprovidersFile.delete()) {
            fail("failed to delete file " + winkprovidersFile.getPath());
        }
        // restore the backup:
        copyfile(winkprovidersFile.getPath() + backupExt, winkprovidersFile.getPath());
        super.tearDown();
    }
    
    @Override
    protected String getApplicationClassName() {
        return MyApplication.class.getName();
    }
    
    public static class MyApplication extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            HashSet<Class<?>> set = new LinkedHashSet<Class<?>>();
            set.add(Provider1.class);  // Provider1 should be ignored due to it already being listed in getSingletons
            set.add(Provider2.class);  // accepted
            return set;
        }
        
        @Override
        public Set<Object> getSingletons() {
            HashSet<Object> set = new LinkedHashSet<Object>();
            set.add(provider1Singleton);
            set.add(new Provider1());  // should be ignored due to provider1Singleton already being listed
            set.add(new ECHOResource());
            return set;
        }
    
    }
    
    @Path("/test")
    public static class ECHOResource {

        @POST
        @Consumes(MediaType.TEXT_PLAIN)
        @Produces(MediaType.TEXT_PLAIN)
        @Path("priority")
        public String echoString(String string) {
            return string;
        }
    }
    
    /**
     * Test the order of loading and the prioritization of Providers based on that loading order.
     * 
     * NOTE:  this test depends on the MockServletInvocationTest continuing to use LinkedHashSet in its
     * getClasses and getSingletons methods
     * 
     */
    public void testProviderPrioritization() throws Exception {
        
        // make sure all the lists were read and processed by tracking the number of hits to the ctors
        
        Provider1 p1 = new Provider1();
        assertEquals(5, p1.getNumCtorHits());
        
        Provider2 p2 = new Provider2();
        assertEquals(3, p2.getNumCtorHits());
        
        Provider3 p3 = new Provider3();
        assertEquals(2, p3.getNumCtorHits());
        
        Provider4 p4 = new Provider4();
        assertEquals(2, p4.getNumCtorHits());
        
        // to actually inspect the list in the 'data' object in the MessageBodyReaders in the ProvidersRegistry would take a lot of
        // reflection and hacking.  We'll at least confirm that only 5 (due to AssetProvider) are in the ProvidersRegistry.

        RestServlet servlet = (RestServlet)this.getServlet();
        ServletContext context = servlet.getServletContext();
        RequestProcessor processor = (RequestProcessor) context.getAttribute(RequestProcessor.class.getName());
        DeploymentConfiguration config = processor.getConfiguration();
        ProvidersRegistry providersRegistry = config.getProvidersRegistry();
        // to confirm that the ignores are indeed happening, I need to get the private field
        // "messageBodyReaders" object, then it's superclass "data" object and inspect it:
        Field field = providersRegistry.getClass().getDeclaredField("messageBodyReaders");
        field.setAccessible(true);
        Object messageBodyReaders = field.get(providersRegistry);
        Field field2 = messageBodyReaders.getClass().getSuperclass().getDeclaredField("data");
        field2.setAccessible(true);
        HashMap data = (HashMap)field2.get(messageBodyReaders);
        HashSet readers = (HashSet)data.get(MediaType.WILDCARD_TYPE);
        
        assertEquals(6, readers.size());
        
        // under the covers, the "list" is a treeset, so the iterator does not really tell us any info about the sort
        Set<String> expectedSet = new HashSet<String>(6);
        expectedSet.add("Priority: 0.500000, ObjectFactory: SingletonOF: " + org.apache.wink.server.internal.registry.providers.Provider1.class.getName());
        expectedSet.add("Priority: 0.100000, ObjectFactory: ClassMetadataPrototypeOF Class: " + org.apache.wink.common.internal.providers.entity.AssetProvider.class.getName());
        expectedSet.add("Priority: 0.100000, ObjectFactory: SingletonOF: " + org.apache.wink.server.internal.registry.providers.Provider3.class.getName());
        expectedSet.add("Priority: 0.500000, ObjectFactory: SingletonOF: " + org.apache.wink.server.internal.registry.providers.Provider2.class.getName());
        expectedSet.add("Priority: 0.100000, ObjectFactory: SingletonOF: " + org.apache.wink.server.internal.registry.providers.Provider4.class.getName());
        expectedSet.add("Priority: 0.100000, ObjectFactory: SingletonOF: " + StringProvider.class.getName());
        
        // this is obviously not the best way to check this.  If toString() output is changed, or the TreeSet implementation is modified,
        // this test code will also have to be modified.  Also, can't check order of the readers HashSet.  But we're compatible with other
        // locales now.
        int count = 0;
        for (Iterator it = readers.iterator(); it.hasNext();) {
            Object obj = it.next();
            assertTrue(obj.toString(), expectedSet.contains(obj.toString()));
            count++;
        }
        
        // do a real transaction too to confirm that the listing in getClasses took the top spot
        
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/test/priority",
                                                        MediaType.TEXT_PLAIN,
                                                        MediaType.TEXT_PLAIN,
                                                        "".getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        // make sure the first instance processed has top priority, and that it is indeed the first instance of Provider2 (because it was given
        // higher priority due to getClasses being processed after getSingletons)
        assertEquals(Provider2.class.getName() + "_" + p2.getFirstInstanceHashCode(), response.getContentAsString());
        
    }
    
    // utility method
    private File createFile(String filePath, String[] lines) throws Exception {
        
        File propFile = new File(filePath);
        if (!propFile.exists()) {
            File dir = new File(filePath.substring(0, filePath.lastIndexOf("/")));
            dir.mkdirs();
            propFile.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(propFile);
        for (int i = 0; i < lines.length; i++) {
            fileWriter.write(lines[i]);
            fileWriter.write(System.getProperty("line.separator"));
        }
        fileWriter.flush();
        fileWriter.close();
        return propFile;
    }
    
    private static void copyfile(String sourceFile, String destinationFile) throws Exception {
        try {
            File f1 = new File(sourceFile);
            File f2 = new File(destinationFile);
            InputStream in = new FileInputStream(f1);

            OutputStream out = new FileOutputStream(f2);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            // ignore
        }

    }
    
}