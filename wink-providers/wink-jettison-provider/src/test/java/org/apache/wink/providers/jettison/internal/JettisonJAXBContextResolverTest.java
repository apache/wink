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
package org.apache.wink.providers.jettison.internal;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.wink.providers.jettison.JettisonJAXBElementProvider;
import org.apache.wink.providers.jettison.JettisonJAXBProvider;
import org.apache.wink.providers.jettison.internal.jaxb2.AddNumbers;
import org.apache.wink.providers.json.JSONUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JettisonJAXBContextResolverTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class<?>[] {AddNumbersResource.class, MyJAXBResolver.class};
    }
    
    @Override
    protected Object[] getSingletons() {
        JettisonJAXBProvider jaxbProvider = new JettisonJAXBProvider(true, null, null);
        jaxbProvider.setUseAsReader(true);

        JettisonJAXBElementProvider jaxbElementProvider =
            new JettisonJAXBElementProvider(true, null, null);
        jaxbElementProvider.setUseAsReader(true);

        return new Object[] {jaxbProvider, jaxbElementProvider};
    }
    
    @Override
    public String getPropertiesFile() {
        return "META-INF/wink.properties";
    }
    
    @Path("/test/addnumbers")
    public static class AddNumbersResource {

        @POST
        public AddNumbers postAddNumbers(AddNumbers a) {
            return a;
        }
    }
    
    
    @Test
    public void testJAXBUnmarshallingWithAlternateContext1() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("POST", "/test/addnumbers", "application/json");
        request.setContentType("application/json");

        request.setContent(" { \"addNumbers\" : { \"arg0\" : { \"$\":\"1\"}, \"arg1\" : { \"$\":\"2\" } } } "
            .getBytes());
        MockHttpServletResponse response = invoke(request);
        assertEquals(200, response.getStatus());
        assertTrue(JSONUtils
            .equals(new JSONObject("{\"addNumbers\":{\"@xmlns\":{\"ns2\":\"http://org/apache/wink/providers/jettison/internal/jaxb2\"},\"arg0\":{\"$\":\"1\"},\"arg1\":{\"$\":\"2\"}}}"),
                    new JSONObject(response.getContentAsString())));
    }
    
}
