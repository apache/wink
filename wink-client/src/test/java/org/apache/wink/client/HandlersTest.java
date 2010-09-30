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
package org.apache.wink.client;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.wink.client.handlers.ClientHandler;
import org.apache.wink.client.handlers.HandlerContext;
import org.apache.wink.client.handlers.InputStreamAdapter;
import org.apache.wink.client.handlers.OutputStreamAdapter;

public class HandlersTest extends BaseTest {

    private static final String DUMMY_REQUEST_VALUE   = "Dummy request value";
    private static final String DUMMY_REQUEST_HEADER  = "Dummy-Request-Header";
    private static final String DUMMY_RESPONSE_VALUE  = "Dummy response value";
    private static final String DUMMY_RESPONSE_HEADER = "Dummy-Response-Header";

    public void testHandlers() {
        server.getMockHttpServerResponses().get(0).setMockResponseCode(200);
        server.getMockHttpServerResponses().get(0).setMockResponseContent(SENT_MESSAGE);

        ClientConfig config = new ClientConfig();
        config.handlers(new DummyHandler());
        RestClient client = new RestClient(config);
        Resource resource = client.resource(serviceURL + "/testResourcePost");
        ClientResponse response =
            resource.contentType("text/plain").accept("text/plain")
                .post(SENT_MESSAGE.toLowerCase());

        // Check that request filter converted request entity to upper
        assertEquals(SENT_MESSAGE.toUpperCase(), server.getRequestContentAsString());

        // Check that response filter converted response entity back to lower
        assertEquals(SENT_MESSAGE.toLowerCase(), response.getEntity(String.class));

        // Check that handler added Http header
        assertTrue(server.getRequestHeaders().get(DUMMY_REQUEST_HEADER).get(0)
            .equalsIgnoreCase(DUMMY_REQUEST_VALUE));
        assertTrue(response.getHeaders().get(DUMMY_RESPONSE_HEADER).get(0)
            .equalsIgnoreCase(DUMMY_RESPONSE_VALUE));
    }

    public static class DummyHandler implements ClientHandler {

        public ClientResponse handle(ClientRequest request, HandlerContext context)
            throws Exception {
            context.addInputStreamAdapter(new DummyAdapter());
            context.addOutputStreamAdapter(new DummyAdapter());
            request.getHeaders().add(DUMMY_REQUEST_HEADER, DUMMY_REQUEST_VALUE);
            ClientResponse response = context.doChain(request);
            response.getHeaders().add(DUMMY_RESPONSE_HEADER, DUMMY_RESPONSE_VALUE);
            return response;
        }
    }

    private static class DummyAdapter implements InputStreamAdapter, OutputStreamAdapter {

        public static byte toLower(byte b) {
            if (b >= 'A' && b <= 'Z') {
                return (byte)(b + ('a' - 'A'));
            }
            return b;
        }

        public static byte toUpper(byte b) {
            if (b >= 'a' && b <= 'z') {
                return (byte)(b - ('a' - 'A'));
            }
            return b;
        }

        public InputStream adapt(InputStream is, ClientResponse response) {
            return new FilterInputStream(is) {
                @Override
                public int read() throws IOException {
                    int read = super.read();
                    if (read == -1) {
                        return -1;
                    }
                    return toLower((byte)read);
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    int read = super.read(b, off, len);
                    if (read == -1) {
                        return -1;
                    }
                    for (int i = off; i < off + read; i++) {
                        b[i] = toLower(b[i]);
                    }
                    return read;
                }

                @Override
                public int read(byte[] b) throws IOException {
                    return read(b, 0, b.length);
                }

            };
        }

        public OutputStream adapt(OutputStream os, ClientRequest request) {
            return new FilterOutputStream(os) {
                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    for (int i = off; i < len; i++) {
                        b[i] = toUpper(b[i]);
                    }
                    super.write(b, off, len);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    write(b, 0, b.length);
                }

                @Override
                public void write(int b) throws IOException {
                    super.write(toUpper((byte)b));
                }
            };
        }

    }

}
