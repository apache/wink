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

package org.apache.wink.example.helloworld;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.common.internal.application.ApplicationFileLoader;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test of response from HelloWorld Resource.
 */
public class HelloWorldTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        try {
            Set<Class<?>> classes = new ApplicationFileLoader("application").getClasses();
            Class<?>[] classesArray = new Class[classes.size()];
            return classes.toArray(classesArray);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void testHelloWorld() throws Exception {
        // prepare a mock request and an empty mock response
        MockHttpServletRequest request =
            constructMockRequest("GET", "/world", MediaTypeUtils.ATOM_ENTRY);
        MockHttpServletResponse response = invoke(request);

        // check resulting mock response
        assertEquals("HTTP status", HttpStatus.OK.getCode(), response.getStatus());
        /*
         * avoid a bug in the MockServletResponse#setContentType where it tries
         * to parse the charset there could be a
         * "text/plain;charset=UTF-8;otherParam=otherValue" but
         * MockServletResponse will treat charset as
         * "UTF-8;otherParam=otherValue" instead of just "UTF-8"
         */
        String charset =
            MediaType.valueOf(response.getContentType()).getParameters().get("charset");
        response.setCharacterEncoding(charset);

        AtomEntry entry = AtomEntry.unmarshal(new StringReader(response.getContentAsString()));
        String id = entry.getId();
        assertEquals("entry id", HelloWorld.ID, id);
    }

    // test helper
    private MockHttpServletRequest constructMockRequest(String method,
                                                        String requestURI,
                                                        String acceptHeader) {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest() {

            public String getPathTranslated() {
                return null; // prevent Spring to resolve the file on the
                             // filesystem which fails
            }

        };
        mockRequest.setMethod(method);
        mockRequest.setRequestURI(requestURI);
        mockRequest.addHeader("Accept", acceptHeader);
        return mockRequest;
    }
}
