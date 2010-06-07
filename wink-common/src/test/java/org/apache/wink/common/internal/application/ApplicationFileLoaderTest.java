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
package org.apache.wink.common.internal.application;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.LogRecord;

import junit.framework.TestCase;

import org.apache.wink.logging.WinkLogHandler;

public class ApplicationFileLoaderTest extends TestCase {

    public void testDefault() throws IOException {
        ApplicationFileLoader applicationFileLoader = new ApplicationFileLoader(true);
        Set<Class<?>> classes = applicationFileLoader.getClasses();
        assertTrue(classes
            .contains(org.apache.wink.common.internal.providers.entity.FileProvider.class));
        assertTrue(classes
            .contains(org.apache.wink.common.internal.providers.entity.StringProvider.class));
        assertTrue(classes
            .contains(org.apache.wink.common.internal.providers.entity.SourceProvider.StreamSourceProvider.class));
        assertTrue(classes
            .contains(org.apache.wink.common.internal.providers.entity.SourceProvider.SAXSourceProvider.class));
        assertTrue(classes
            .contains(org.apache.wink.common.internal.providers.entity.ByteArrayProvider.class));
    }

    public void testLogging() throws Exception {
        //WinkLogHandler.turnLoggingCaptureOn();  // WinkLogHandler imported from wink-component-test-support module
        ApplicationFileLoader applicationFileLoader =
            new ApplicationFileLoader("org//apache//wink//common//internal//application//custom.app");
        //WinkLogHandler.turnLoggingCaptureOff();
        //ArrayList<LogRecord> logRecords = WinkLogHandler.getRecords();
        //assertEquals(13, logRecords.size());
        //WinkLogHandler.clearRecords();  // recommend doing this so static ArrayList of LogRecords is clear for next test
    }
    
    public void testFileNotFound() {
        try {
            new ApplicationFileLoader("noSuchFile");
        } catch (FileNotFoundException e) {
            return;
        }
        fail("Should be file not found exception!");
    }

    public void testCustomFile() throws FileNotFoundException {
        ApplicationFileLoader applicationFileLoader =
            new ApplicationFileLoader(
                                      "org//apache//wink//common//internal//application//custom.app");

        Iterator<Class<?>> iterator = applicationFileLoader.getClasses().iterator();
        assertEquals(org.apache.wink.common.internal.providers.entity.FileProvider.class, iterator
            .next());
        assertEquals(org.apache.wink.common.internal.providers.entity.SourceProvider.DOMSourceProvider.class,
                     iterator.next());
        assertFalse(iterator.hasNext());

    }
}
