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
package org.apache.wink.server.internal.application;

import java.io.InputStream;
import java.util.Set;

import org.apache.wink.common.internal.providers.entity.FileProvider;
import org.apache.wink.common.internal.providers.entity.SourceProvider;
import org.apache.wink.server.internal.application.ServletWinkApplication;
import org.apache.wink.test.mock.ServletContextMock;

import junit.framework.TestCase;

public class ServletWinkApplicationTest extends TestCase {

    static class ServletContextImpl extends ServletContextMock {

        public ServletContextImpl() {
            super(null);
        }

        @Override
        public String getInitParameter(String arg) {
            return null;
        }

        @Override
        public InputStream getResourceAsStream(String arg0) {
            return null;
        }
    }

    public void testSimpleWinkApplication() {
        ServletWinkApplication simpleWinkApplication =
            new ServletWinkApplication(new ServletContextImpl(),
                                       "org//apache//wink//server//internal//application//custom.app");
        Set<Class<?>> classes = simpleWinkApplication.getClasses();
        assertTrue(classes.contains(FileProvider.class));
        assertTrue(classes.contains(SourceProvider.DOMSourceProvider.class));
    }
}
