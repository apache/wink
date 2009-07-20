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
package org.apache.wink.server.internal.factory;

import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class InjectFieldsTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {ResourceWithFieldsExt.class};
    }

    public void testExtResource() throws Exception {
        // tests that all members were populated
        MockHttpServletRequest getRequest =
            MockRequestConstructor.constructMockRequest("GET", "/ext-resource", "*/*");
        getRequest.setQueryString("q=hi&s=bye");
        MockHttpServletResponse response = invoke(getRequest);
        assertEquals(200, response.getStatus());
        assertEquals("hi != bye", response.getContentAsString());

    }
}
