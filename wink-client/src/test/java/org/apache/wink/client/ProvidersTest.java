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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.wink.client.MockHttpServer;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.common.internal.providers.entity.atom.AtomFeedProvider;
import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.test.mock.TestUtils;

import junit.framework.TestCase;

public class ProvidersTest extends TestCase {

    private static int          SERVER_PORT = 3456;
    private static String       SERVICE_URL = "http://localhost:{0}/some/service";
    private static final String FEED        =

                                                "<feed xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <id>urn:feed:1</id>\n"
                                                    + "    <updated>1970-01-02T11:20:00+02:00</updated>\n"
                                                    + "    <title type=\"text\" xml:lang=\"en\">Title</title>\n"
                                                    + "    <subtitle type=\"text\" xml:lang=\"en\">Subtitle</subtitle>\n"
                                                    + "    <opensearch:itemsPerPage>10</opensearch:itemsPerPage>\n"
                                                    + "    <opensearch:startIndex>0</opensearch:startIndex>\n"
                                                    + "    <opensearch:totalResults>100</opensearch:totalResults>\n"
                                                    + "    <link href=\"http://localhost:8080/rest/service/feed\" rel=\"self\"/>\n"
                                                    + "    <author>\n"
                                                    + "        <name>John Smith</name>\n"
                                                    + "    </author>\n"
                                                    + "    <category label=\"lable\" scheme=\"urn:entity:priority\" term=\"1\"/>\n"
                                                    + "    <entry>\n"
                                                    + "        <id>urn:entry2</id>\n"
                                                    + "        <updated>1970-01-02T11:20:00+02:00</updated>\n"
                                                    + "        <title type=\"text\" xml:lang=\"en\">Title</title>\n"
                                                    + "        <summary type=\"text\" xml:lang=\"en\">Title</summary>\n"
                                                    + "        <published>1970-01-02T11:20:00+02:00</published>\n"
                                                    + "        <link href=\"http://localhost:8080/rest/service/entry/2\" type=\"application/xml\" rel=\"self\"/>\n"
                                                    + "        <author>\n"
                                                    + "            <name>John Smith</name>\n"
                                                    + "        </author>\n"
                                                    + "        <category label=\"lable\" scheme=\"urn:entity:priority\" term=\"1\"/>\n"
                                                    + "        <content type=\"text\">This is entity created by John Smith</content>\n"
                                                    + "    </entry>\n"
                                                    + "    <entry>\n"
                                                    + "        <id>urn:entry3</id>\n"
                                                    + "        <updated>1970-01-02T11:20:00+02:00</updated>\n"
                                                    + "        <title type=\"text\" xml:lang=\"en\">Title</title>\n"
                                                    + "        <summary type=\"text\" xml:lang=\"en\">Title</summary>\n"
                                                    + "        <published>1970-01-02T11:20:00+02:00</published>\n"
                                                    + "        <link href=\"http://localhost:8080/rest/service/entry/3\" type=\"application/xml\" rel=\"self\"/>\n"
                                                    + "        <author>\n"
                                                    + "            <name>John Smith</name>\n"
                                                    + "        </author>\n"
                                                    + "        <category label=\"lable\" scheme=\"urn:entity:priority\" term=\"1\"/>\n"
                                                    + "        <content type=\"text\">This is entity created by John Smith</content>\n"
                                                    + "    </entry>\n"
                                                    + "</feed>\n";

    public void testAtomFeedReadWrite() throws Exception {
        MockHttpServer server = new MockHttpServer(SERVER_PORT);
        server.setMockResponseCode(200);
        server.setMockResponseContentEchoRequest(true);
        server.setMockResponseContentType(MediaType.APPLICATION_ATOM_XML);
        server.startServer();
        try {
            RestClient client = new RestClient();
            Resource resource =
                client.resource(MessageFormat.format(SERVICE_URL, String.valueOf(server
                    .getServerPort())));

            AtomFeedProvider afp = new AtomFeedProvider();
            AtomFeed entryToPost =
                afp.readFrom(AtomFeed.class,
                             null,
                             null,
                             MediaType.APPLICATION_ATOM_XML_TYPE,
                             null,
                             new ByteArrayInputStream(FEED.getBytes()));
            AtomFeed responseEntity =
                resource.accept(MediaType.APPLICATION_ATOM_XML_TYPE)
                    .contentType(MediaType.APPLICATION_ATOM_XML_TYPE).post(AtomFeed.class,
                                                                           entryToPost);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            afp.writeTo(responseEntity,
                        AtomFeed.class,
                        null,
                        null,
                        MediaType.APPLICATION_ATOM_XML_TYPE,
                        null,
                        os);
            String actual = os.toString();

            String msg = TestUtils.diffIgnoreUpdateWithAttributeQualifier(FEED, actual);
            assertNull(msg, msg);
        } finally {
            server.stopServer();
        }
    }

}
