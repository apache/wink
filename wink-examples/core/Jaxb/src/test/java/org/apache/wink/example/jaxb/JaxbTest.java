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

package org.apache.wink.example.jaxb;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.wink.common.model.XmlFormattingOptions;
import org.apache.wink.example.jaxb.JaxbResource;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class JaxbTest extends MockServletInvocationTest {

    private static final String PERSON_1  =
                                              "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><person><firstName>John</firstName><lastName>Smith</lastName><email>john.smith@email.com</email></person>";
    private static final String PERSON_2  =
                                              "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><person><firstName>Jane</firstName><lastName>Smith</lastName><email>jane.smith@email.com</email></person>";
    private static final String PERSON_3  =
                                              "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><person><firstName>Bob</firstName><lastName>Burke</lastName><email>bob.burke@email.com</email></person>";
    private static final String ADDRESS_1 =
                                              "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><address><city>New York</city><street>5th</street><number>64</number></address>";
    private static final String ADDRESS_2 =
                                              "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><address><city>New Orleans</city><street>Burbon</street><number>70</number></address>";
    private static final String ADDRESS_3 =
                                              "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><address><city>New Jersey</city><street>Poppey</street><number>5</number></address>";
    private static final String PHONE_1   =
                                              "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><phone><areaCode>768</areaCode><number>5555678</number></phone>";
    private static final String PHONE_2   =
                                              "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><phone><areaCode>513</areaCode><number>5554321</number></phone>";
    private static final String PHONE_3   =
                                              "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><phone><areaCode>224</areaCode><number>5559876</number></phone>";

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {JaxbResource.class};
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        XmlFormattingOptions.setDefaultXmlFormattingOptions(new XmlFormattingOptions(false, false));
    }

    public void testAllGet() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/info/person/1",
                                                        MediaType.APPLICATION_XML_TYPE);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(PERSON_1, response.getContentAsString());
        assertNull(msg, msg);

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/info/person/2",
                                                        MediaType.APPLICATION_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertEquals(PERSON_2, response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/info/address/1",
                                                        MediaType.APPLICATION_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertEquals(ADDRESS_1, response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/info/address/2",
                                                        MediaType.APPLICATION_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertEquals(ADDRESS_2, response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/info/phone/1",
                                                        MediaType.APPLICATION_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertEquals(PHONE_1, response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/info/phone/2",
                                                        MediaType.APPLICATION_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertEquals(PHONE_2, response.getContentAsString());
    }

    public void testAllPost() throws Exception {
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/info/person/3",
                                                        MediaType.APPLICATION_XML_TYPE);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/info/person/3",
                                                        MediaType.APPLICATION_XML,
                                                        MediaType.APPLICATION_XML,
                                                        PERSON_3.getBytes());
        response = invoke(request);
        assertEquals("status", Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(PERSON_3, response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/info/person/3",
                                                        MediaType.APPLICATION_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertEquals(PERSON_3, response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/info/address/3",
                                                        MediaType.APPLICATION_XML_TYPE);
        response = invoke(request);
        assertEquals("status", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/info/address/3",
                                                        MediaType.APPLICATION_XML,
                                                        MediaType.APPLICATION_XML,
                                                        ADDRESS_3.getBytes());
        response = invoke(request);
        assertEquals("status", Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(ADDRESS_3, response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/info/address/3",
                                                        MediaType.APPLICATION_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertEquals(ADDRESS_3, response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/info/phone/3",
                                                        MediaType.APPLICATION_XML_TYPE);
        response = invoke(request);
        assertEquals("status", Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

        request =
            MockRequestConstructor.constructMockRequest("POST",
                                                        "/info/phone/3",
                                                        MediaType.APPLICATION_XML,
                                                        MediaType.APPLICATION_XML,
                                                        PHONE_3.getBytes());
        response = invoke(request);
        assertEquals("status", Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(PHONE_3, response.getContentAsString());

        request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/info/phone/3",
                                                        MediaType.APPLICATION_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        assertEquals(PHONE_3, response.getContentAsString());
    }

}
