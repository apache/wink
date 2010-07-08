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

package org.apache.wink.example.multipart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.providers.multipart.MultiPartParser;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 *
 */
public class MultiPartTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] {MultiPartResource.class};
    }

    private String buildUserXML(String first, String last, String id, String email) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><user><firstName>" + first
            + "</firstName><lastName>"
            + last
            + "</lastName><id>"
            + id
            + "</id><email>"
            + email
            + "</email></user>";

    }

    public void verifyResponse(MockHttpServletResponse response, int expectedPartsNum)
        throws Exception {
        String contentType = response.getContentType();
        String bound = contentType.substring(contentType.indexOf("=") + 1);
        String content = response.getContentAsString();
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
        MultiPartParser mpp = new MultiPartParser(in, bound);
        int numOfParts = 0;
        while (mpp.nextPart()) {
            numOfParts++;
        }
        assertEquals(numOfParts, expectedPartsNum);

    }

    private byte[] obtainByteData(String filename) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(filename);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        byte[] bytes = new byte[512];

        // Read bytes from the input stream in bytes.length-sized chunks and
        // write
        // them into the output stream
        int readBytes;
        while ((readBytes = inputStream.read(bytes)) > 0) {
            outputStream.write(bytes, 0, readBytes);
        }

        // Convert the contents of the output stream into a byte array
        byte[] byteData = outputStream.toByteArray();

        // Close the streams
        inputStream.close();
        outputStream.close();

        return byteData;
    }

    public void testUsers() throws Exception {

        // check the collection
        MockHttpServletRequest request =
            MockRequestConstructor.constructMockRequest("GET",
                                                        "/MP/users",
                                                        MediaTypeUtils.MULTIPART_MIXED);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        verifyResponse(response, 2);

        String boundary = "I am a boundary";
        String requestStr =
            "--" + boundary
                + "\n"
                + "Content-Type: application/xml\n\n"
                + buildUserXML("firstName", "lastName", "9", "myEmail@mail.com")
                + "\n--"
                + boundary
                + "--";

        request =
            MockRequestConstructor
                .constructMockRequest("POST",
                                      "/MP/users",
                                      MediaTypeUtils.MULTIPART_MIXED,
                                      MediaTypeUtils.MULTIPART_MIXED + ";boundary=" + boundary,
                                      requestStr.getBytes());
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        verifyResponse(response, 3);

    }

    /**
     * Upload files and test the the file size as returned by the resource is
     * identical to the actual file length
     * 
     * @throws IOException
     * @throws ServletException
     * @throws UnsupportedEncodingException
     */
    public void testUploadFile() throws IOException, ServletException, UnsupportedEncodingException {
        String boundary = "I am a boundary";
        MockHttpServletRequest request;
        MockHttpServletResponse response;

        String msg1 =
            "--" + boundary
                + "\n"
                + "Content-Disposition: form-data; name=\"datafile\"; filename=\"JDOMAbout\"\n"
                + "Content-Type: application/octet-stream\n\n";
        byte[] bms1 = msg1.getBytes();
        byte[] bms2 = obtainByteData("file1");
        String msg3 = "\n--" + boundary + "--";
        byte[] bms3 = msg3.getBytes();
        int bodyLength = bms2.length;
        byte[] msg = new byte[bms1.length + bms2.length + bms3.length];
        System.arraycopy(bms1, 0, msg, 0, bms1.length);
        System.arraycopy(bms2, 0, msg, bms1.length, bms2.length);
        System.arraycopy(bms3, 0, msg, bms1.length + bms2.length, bms3.length);

        request =
            MockRequestConstructor
                .constructMockRequest("POST",
                                      "/MP/files",
                                      MediaType.TEXT_PLAIN,
                                      MediaTypeUtils.MULTIPART_FORM_DATA + ";boundary=" + boundary,
                                      msg);
        response = invoke(request);
        String content = response.getContentAsString();
        int i = content.indexOf(String.valueOf(bodyLength));
        assertNotSame(i, -1);
    }

}
