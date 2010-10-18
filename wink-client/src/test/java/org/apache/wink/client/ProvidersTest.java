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
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

import junit.framework.TestCase;

import org.apache.wink.common.internal.application.ApplicationFileLoader;
import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.contexts.ProvidersImpl;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.lifecycle.ScopeLifecycleManager;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.runtime.AbstractRuntimeContext;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.test.mock.TestUtils;

public class ProvidersTest extends TestCase {

    private static int          SERVER_PORT = 3456;
    private static String       SERVICE_URL = "http://localhost:{0}/some/service";
    private static final String FEED        =

                                                "<feed xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns=\"http://www.w3.org/2005/Atom\">" + "<id>urn:feed:1</id>"
                                                    + "<updated>1970-01-02T11:20:00+02:00</updated>"
                                                    + "<title type=\"text\" xml:lang=\"en\">Title</title>"
                                                    + "<subtitle type=\"text\" xml:lang=\"en\">Subtitle</subtitle>"
                                                    + "<opensearch:itemsPerPage>10</opensearch:itemsPerPage>"
                                                    + "<opensearch:startIndex>0</opensearch:startIndex>"
                                                    + "<opensearch:totalResults>100</opensearch:totalResults>"
                                                    + "<link href=\"http://localhost:8080/rest/service/feed\" rel=\"self\"/>"
                                                    + "<author>"
                                                    + "<name>John Smith</name>"
                                                    + "</author>"
                                                    + "<category label=\"lable\" scheme=\"urn:entity:priority\" term=\"1\"/>"
                                                    + "<entry>"
                                                    + "<id>urn:entry2</id>"
                                                    + "<updated>1970-01-02T11:20:00+02:00</updated>"
                                                    + "<title type=\"text\" xml:lang=\"en\">Title</title>"
                                                    + "<summary type=\"text\" xml:lang=\"en\">Title</summary>"
                                                    + "<published>1970-01-02T11:20:00+02:00</published>"
                                                    + "<link href=\"http://localhost:8080/rest/service/entry/2\" type=\"application/xml\" rel=\"self\"/>"
                                                    + "<author>"
                                                    + "<name>John Smith</name>"
                                                    + "</author>"
                                                    + "<category label=\"lable\" scheme=\"urn:entity:priority\" term=\"1\"/>"
                                                    + "<content type=\"text\">This is entity created by John Smith</content>"
                                                    + "</entry>"
                                                    + "<entry>"
                                                    + "<id>urn:entry3</id>"
                                                    + "<updated>1970-01-02T11:20:00+02:00</updated>"
                                                    + "<title type=\"text\" xml:lang=\"en\">Title</title>"
                                                    + "<summary type=\"text\" xml:lang=\"en\">Title</summary>"
                                                    + "<published>1970-01-02T11:20:00+02:00</published>"
                                                    + "<link href=\"http://localhost:8080/rest/service/entry/3\" type=\"application/xml\" rel=\"self\"/>"
                                                    + "<author>"
                                                    + "<name>John Smith</name>"
                                                    + "</author>"
                                                    + "<category label=\"lable\" scheme=\"urn:entity:priority\" term=\"1\"/>"
                                                    + "<content type=\"text\">This is entity created by John Smith</content>"
                                                    + "</entry>"
                                                    + "</feed>";

    // apparently JAXB 2.2 reads JAXB bean fields in a different order, and replaces namespace prefixes.  This XML is equivalent to above.
    private static final String FEED_JAXB_22 = "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:ns2=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:ns3=\"http://www.w3.org/1999/xhtml\">"
        + "<id>urn:feed:1</id>"
        + "<updated>1970-01-02T11:20:00+02:00</updated>"
        + "<title type=\"text\" xml:lang=\"en\">Title</title>"
        + "<subtitle type=\"text\" xml:lang=\"en\">Subtitle</subtitle>"
        + "<link rel=\"self\" href=\"http://localhost:8080/rest/service/feed\"/>"
        + "<author>"
        + "<name>John Smith</name>"
        + "</author>"
        + "<category term=\"1\" scheme=\"urn:entity:priority\" label=\"lable\"/>"
        + "<entry>"
        + "<id>urn:entry2</id>"
        + "<updated>1970-01-02T11:20:00+02:00</updated>"
        + "<title type=\"text\" xml:lang=\"en\">Title</title>"
        + "<summary type=\"text\" xml:lang=\"en\">Title</summary>"
        + "<published>1970-01-02T11:20:00+02:00</published>"
        + "<link rel=\"self\" type=\"application/xml\" href=\"http://localhost:8080/rest/service/entry/2\"/>"
        + "<author>"
        + "<name>John Smith</name>"
        + "</author>"
        + "<category term=\"1\" scheme=\"urn:entity:priority\" label=\"lable\"/>"
        + "<content type=\"text\">This is entity created by John Smith</content>"
        + "</entry>"
        + "<entry>"
        + "<id>urn:entry3</id>"
        + "<updated>1970-01-02T11:20:00+02:00</updated>"
        + "<title type=\"text\" xml:lang=\"en\">Title</title>"
        + "<summary type=\"text\" xml:lang=\"en\">Title</summary>"
        + "<published>1970-01-02T11:20:00+02:00</published>"
        + "<link rel=\"self\" type=\"application/xml\" href=\"http://localhost:8080/rest/service/entry/3\"/>"
        + "<author>"
        + "<name>John Smith</name>"
        + "</author>"
        + "<category term=\"1\" scheme=\"urn:entity:priority\" label=\"lable\"/>"
        + "<content type=\"text\">This is entity created by John Smith</content>"
        + "</entry>"
        + "<ns2:totalResults>100</ns2:totalResults>"
        + "<ns2:itemsPerPage>10</ns2:itemsPerPage>"
        + "<ns2:startIndex>0</ns2:startIndex>"
        + "</feed>";
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        LifecycleManagersRegistry ofFactoryRegistry = new LifecycleManagersRegistry();
        ofFactoryRegistry.addFactoryFactory(new ScopeLifecycleManager<Object>());
        ProvidersRegistry providersRegistry =
            new ProvidersRegistry(ofFactoryRegistry, new ApplicationValidator());

        Set<Class<?>> classes = new ApplicationFileLoader(true).getClasses();
        if (classes != null) {
            for (Class<?> cls : classes) {
                if (ProviderMetadataCollector.isProvider(cls)) {
                    providersRegistry.addProvider(cls);
                }
            }
        }
        AbstractRuntimeContext runtimeContext = new AbstractRuntimeContext() {

            public OutputStream getOutputStream() throws IOException {
                return null;
            }

            public InputStream getInputStream() throws IOException {
                return null;
            }
        };
        runtimeContext.setAttribute(Providers.class, new ProvidersImpl(providersRegistry,
                                                                       runtimeContext));
        RuntimeContextTLS.setRuntimeContext(runtimeContext);
    }

    public void testAtomFeedReadWrite() throws Exception {
        MockHttpServer server = new MockHttpServer(SERVER_PORT);
        server.getMockHttpServerResponses().get(0).setMockResponseCode(200);
        server.getMockHttpServerResponses().get(0).setMockResponseContentEchoRequest(true);
        server.getMockHttpServerResponses().get(0).setMockResponseContentType(MediaType.APPLICATION_ATOM_XML);
        server.startServer();
        try {
            RestClient client = new RestClient();
            Resource resource =
                client.resource(MessageFormat.format(SERVICE_URL, String.valueOf(server
                    .getServerPort())));
            Providers providers = RuntimeContextTLS.getRuntimeContext().getProviders();
            MessageBodyReader<AtomFeed> afp =
                providers.getMessageBodyReader(AtomFeed.class,
                                               AtomFeed.class,
                                               null,
                                               MediaType.APPLICATION_ATOM_XML_TYPE);
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
            MessageBodyWriter<AtomFeed> writer = 
                providers.getMessageBodyWriter(AtomFeed.class,
                                               AtomFeed.class,
                                               null,
                                               MediaType.APPLICATION_ATOM_XML_TYPE);
            writer.writeTo(responseEntity,
                        AtomFeed.class,
                        null,
                        null,
                        MediaType.APPLICATION_ATOM_XML_TYPE,
                        null,
                        os);
            String actual = os.toString();

            String msg = TestUtils.diffIgnoreUpdateWithAttributeQualifier(FEED, actual);
            if (msg != null) {
                msg = TestUtils.diffIgnoreUpdateWithAttributeQualifier(FEED_JAXB_22, actual);
            }
            assertNull(msg, msg);
        } finally {
            server.stopServer();
        }
    }

}
