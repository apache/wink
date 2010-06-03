/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.wink.itests.contentencode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;

import junit.framework.TestCase;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.internal.handlers.DeflateHandler;
import org.apache.wink.client.internal.handlers.GzipHandler;
import org.apache.wink.common.internal.providers.entity.StringProvider;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class ContentEncodedTest extends TestCase {

    private static String getRepeatedString() {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < 1000; ++c) {
            sb.append("Hello world!  ");
        }
        return sb.toString();
    }

    protected static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/rest";
    }

    private static void verifyResponseNotContentEncodedForRepeatedStrings(ClientResponse response) {
        assertEquals(200, response.getStatusCode());
        assertEquals(getRepeatedString(), response.getEntity(String.class));
        assertNull(response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        assertNull(response.getHeaders().getFirst(HttpHeaders.VARY));
    }

    private static void verifyResponseGZIPContentEncodedForRepeatedStrings(ClientResponse response)
        throws IOException {
        assertEquals(200, response.getStatusCode());
        InputStream is = response.getEntity(InputStream.class);
        GZIPInputStream gzipIS = new GZIPInputStream(is);
        StringProvider sp = new StringProvider();
        String responseEntity =
            sp.readFrom(String.class,
                        String.class,
                        new Annotation[] {},
                        MediaType.TEXT_PLAIN_TYPE,
                        null,
                        gzipIS);
        assertEquals(getRepeatedString(), responseEntity);
        assertEquals("gzip", response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        assertEquals(1, response.getHeaders().get(HttpHeaders.CONTENT_ENCODING).size());
        assertEquals(HttpHeaders.ACCEPT_ENCODING, response.getHeaders().getFirst(HttpHeaders.VARY));
        assertEquals(1, response.getHeaders().get(HttpHeaders.VARY).size());
    }

    private static void verifyResponseDeflateContentEncodedForRepeatedStrings(ClientResponse response)
        throws IOException {
        assertEquals(200, response.getStatusCode());
        InputStream is = response.getEntity(InputStream.class);
        InflaterInputStream inflaterIS = new InflaterInputStream(is);
        StringProvider sp = new StringProvider();
        String responseEntity =
            sp.readFrom(String.class,
                        String.class,
                        new Annotation[] {},
                        MediaType.TEXT_PLAIN_TYPE,
                        null,
                        inflaterIS);
        assertEquals(getRepeatedString(), responseEntity);
        assertEquals("deflate", response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        assertEquals(1, response.getHeaders().get(HttpHeaders.CONTENT_ENCODING).size());
        assertEquals(HttpHeaders.ACCEPT_ENCODING, response.getHeaders().getFirst(HttpHeaders.VARY));
        assertEquals(1, response.getHeaders().get(HttpHeaders.VARY).size());
    }

    /**
     * Tests that a regular encoded input is acceptable to the server.
     */
    public void testContentEncodedInboundRequestRegularOutboundPost() {
        ClientConfig config = new ClientConfig();
        config.handlers();
        RestClient client = new RestClient(config);
        ClientResponse response =
            client.resource(getBaseURI() + "/regular/echo").post(getRepeatedString());
        assertEquals(200, response.getStatusCode());
        assertEquals(getRepeatedString(), response.getEntity(String.class));
        assertNull(response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        assertNull(response.getHeaders().getFirst(HttpHeaders.VARY));
    }

    /**
     * Tests that a GZIP inbound is ok and outbound is also ok.
     */
    public void testGZIPContentEncodedInboundRequestContentEncodedOutboundPost() {
        ClientConfig config = new ClientConfig();
        config.handlers(new DeflateHandler());
        RestClient client = new RestClient(config);
        ClientResponse response =
            client.resource(getBaseURI() + "/contentencode/echo").post(getRepeatedString());

        assertEquals(200, response.getStatusCode());
        assertEquals(getRepeatedString(), response.getEntity(String.class));
        assertEquals("deflate", response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        assertEquals(1, response.getHeaders().get(HttpHeaders.CONTENT_ENCODING).size());
        assertEquals(HttpHeaders.ACCEPT_ENCODING, response.getHeaders().getFirst(HttpHeaders.VARY));
        assertEquals(1, response.getHeaders().get(HttpHeaders.VARY).size());
    }

    /**
     * Tests that a deflate inbound is ok and outbound is also ok.
     */
    public void testDeflatedContentEncodedInboundRequestContentEncodedOutboundPost() {
        ClientConfig config = new ClientConfig();
        config.handlers(new DeflateHandler());
        RestClient client = new RestClient(config);
        ClientResponse response =
            client.resource(getBaseURI() + "/contentencode/echo").post(getRepeatedString());

        assertEquals(200, response.getStatusCode());
        assertEquals(getRepeatedString(), response.getEntity(String.class));
        assertEquals("deflate", response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        assertEquals(1, response.getHeaders().get(HttpHeaders.CONTENT_ENCODING).size());
        assertEquals(HttpHeaders.ACCEPT_ENCODING, response.getHeaders().getFirst(HttpHeaders.VARY));
        assertEquals(1, response.getHeaders().get(HttpHeaders.VARY).size());
    }

    /**
     * Tests that a regular get repeated strings resource is possible. This is
     * not content encoded.
     */
    public void testRegularGetRepeatedStringsResource() {
        RestClient client = new RestClient();
        ClientResponse response = client.resource(getBaseURI() + "/regular/repeatedstring").get();
        verifyResponseNotContentEncodedForRepeatedStrings(response);

        response =
            client.resource(getBaseURI() + "/regular/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip").get();
        verifyResponseNotContentEncodedForRepeatedStrings(response);

        response =
            client.resource(getBaseURI() + "/regular/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "*").get();
        verifyResponseNotContentEncodedForRepeatedStrings(response);

        /*
         * test even with the GZIP Handler on the path
         */
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new GzipHandler());
        client = new RestClient(clientConfig);

        response = client.resource(getBaseURI() + "/regular/repeatedstring").get();
        verifyResponseNotContentEncodedForRepeatedStrings(response);
    }

    /**
     * Tests that a content encoded get repeated strings resource is possible.
     * This is GZIP content encoded. This uses the client handler
     * {@link GzipHandler}.
     */
    public void testGZIPContentEncodedGetRepeatedStringsResource() throws IOException {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.handlers(new GzipHandler());
        RestClient client = new RestClient(clientConfig);

        ClientResponse response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring").get();
        assertEquals(200, response.getStatusCode());
        assertEquals(getRepeatedString(), response.getEntity(String.class));
        assertEquals("gzip", response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        assertEquals(1, response.getHeaders().get(HttpHeaders.CONTENT_ENCODING).size());
        assertEquals(HttpHeaders.ACCEPT_ENCODING, response.getHeaders().getFirst(HttpHeaders.VARY));
        assertEquals(1, response.getHeaders().get(HttpHeaders.VARY).size());
    }

    /**
     * Tests that a content encoded get repeated strings resource is possible.
     * This is GZIP content encoded. Tests a manual GZIP decode (so to make sure
     * that the content was GZIP encoded).
     */
    public void testManualGZIPContentDecodedGetRepeatedStringsResource() throws IOException {
        RestClient client = new RestClient();
        ClientResponse response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip").get();
        verifyResponseGZIPContentEncodedForRepeatedStrings(response);
    }

    /**
     * Tests that a content encoded get repeated strings resource is possible.
     * This is Deflate content encoded.
     */
    public void testManualDeflateContentEncodedGetRepeatedStringsResource() throws IOException {
        RestClient client = new RestClient();
        ClientResponse response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "deflate").get();
        verifyResponseDeflateContentEncodedForRepeatedStrings(response);
    }

    /**
     * Tests that the possible Content Encoded resource respects the
     * Accept-Encoding header and will not Content Encode if the Accept-Encoding
     * header is missing or not available. This is not content encoded.
     */
    public void testContentEncodeRespectAcceptEncodingHeaderForGetRepeatedStringsResource()
        throws IOException {
        RestClient client = new RestClient();
        ClientResponse response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring").get();
        verifyResponseNotContentEncodedForRepeatedStrings(response);

        response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "").get();
        verifyResponseNotContentEncodedForRepeatedStrings(response);

        response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, " ").get();
        verifyResponseNotContentEncodedForRepeatedStrings(response);

        response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "myencoding").get();
        verifyResponseNotContentEncodedForRepeatedStrings(response);

        response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "mycustomencoding;q=0.0").get();
        verifyResponseNotContentEncodedForRepeatedStrings(response);

        response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "mycustomencoding,myothercustomencoding")
                .get();
        verifyResponseNotContentEncodedForRepeatedStrings(response);
    }

    /**
     * Tests that the possible Content Encoded resource respects the
     * Accept-Encoding header for GZIP with wildcards.
     */
    public void testGZIPContentEncodeRespectWildcardAcceptEncodingHeaderForGetRepeatedStringsResource()
        throws IOException {
        RestClient client = new RestClient();
        ClientResponse response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "*").get();
        verifyResponseGZIPContentEncodedForRepeatedStrings(response);

        response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "*,deflate=0.8").get();
        verifyResponseGZIPContentEncodedForRepeatedStrings(response);

        response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip,deflate").get();
        verifyResponseGZIPContentEncodedForRepeatedStrings(response);

        /*
         * tests that gzip encoding is banned which is the default.
         */
        response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "*,gzip;q=0.0").get();
        verifyResponseNotContentEncodedForRepeatedStrings(response);
    }

    /**
     * Tests that the possible Content Encoded resource respects the
     * Accept-Encoding header for Deflate with wildcards.
     */
    public void testDeflateContentEncodeRespectAcceptEncodingQualityValuesHeaderForGetRepeatedStringsResource()
        throws IOException {
        RestClient client = new RestClient();
        ClientResponse response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "*;q=0.6,deflate").get();
        verifyResponseDeflateContentEncodedForRepeatedStrings(response);

        response =
            client.resource(getBaseURI() + "/contentencode/repeatedstring")
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip;q=0.6,deflate;q=0.8").get();
        verifyResponseDeflateContentEncodedForRepeatedStrings(response);
    }

    /**
     * Tests that when a JAX-RS application adds a Vary header, that the Vary
     * header comes out okay.
     */
    public void testRegularVaryHeaderCorrectlyOutput() throws IOException {
        RestClient client = new RestClient();
        ClientResponse response =
            client.resource(getBaseURI() + "/regular/varyheaderwithaccept")
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip").accept(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatusCode());
        assertEquals("Variant[mediaType=text/plain, language=null, encoding=null]", response
            .getEntity(String.class));
        assertEquals(HttpHeaders.ACCEPT, response.getHeaders().getFirst(HttpHeaders.VARY));
        assertEquals(1, response.getHeaders().get(HttpHeaders.VARY).size());
    }

    /**
     * Tests that when a JAX-RS application adds a Vary header, that the Vary
     * header is correctly appended to for the first Vary value.
     */
    public void testContentEncodeVaryHeaderCorrectlyAppended() throws IOException {
        RestClient client = new RestClient();
        ClientResponse response =
            client.resource(getBaseURI() + "/contentencode/varyheaderwithaccept")
                .header(HttpHeaders.ACCEPT_ENCODING, "*").accept(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatusCode());
        assertEquals(HttpHeaders.ACCEPT + ", " + HttpHeaders.ACCEPT_ENCODING, response.getHeaders()
            .getFirst(HttpHeaders.VARY));
        assertEquals(1, response.getHeaders().get(HttpHeaders.VARY).size());
        assertEquals("gzip", response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        assertEquals(1, response.getHeaders().get(HttpHeaders.CONTENT_ENCODING).size());

        InputStream is = response.getEntity(InputStream.class);
        GZIPInputStream gzipIS = new GZIPInputStream(is);
        StringProvider sp = new StringProvider();
        String responseEntity =
            sp.readFrom(String.class,
                        String.class,
                        new Annotation[] {},
                        MediaType.TEXT_PLAIN_TYPE,
                        null,
                        gzipIS);
        assertEquals("Variant[mediaType=text/plain, language=null, encoding=null]", responseEntity);
    }

    /**
     * Tests that when a JAX-RS application adds a Vary header, that the Vary
     * header is set when a {@link Request#selectVariant(java.util.List)} call
     * is made.
     */
    public void testRegularVaryHeaderCorrectlyReturnedByItself() throws IOException {
        RestClient client = new RestClient();
        ClientResponse response =
            client.resource(getBaseURI() + "/regular/varyheaderwithacceptencoding")
                .header(HttpHeaders.ACCEPT_ENCODING, "*").accept(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatusCode());
        assertEquals(HttpHeaders.ACCEPT + ", " + HttpHeaders.ACCEPT_ENCODING, response.getHeaders()
            .getFirst(HttpHeaders.VARY));
        assertEquals(1, response.getHeaders().get(HttpHeaders.VARY).size());
        assertNull(response.getHeaders().get(HttpHeaders.CONTENT_ENCODING));
        assertEquals("Variant[mediaType=text/plain, language=null, encoding=gzip]", response
            .getEntity(String.class));

        response =
            client.resource(getBaseURI() + "/regular/varyheaderwithacceptencoding")
                .accept(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatusCode());
        assertEquals(HttpHeaders.ACCEPT + ", " + HttpHeaders.ACCEPT_ENCODING, response.getHeaders()
            .getFirst(HttpHeaders.VARY));
        assertEquals(1, response.getHeaders().get(HttpHeaders.VARY).size());
        assertNull(response.getHeaders().get(HttpHeaders.CONTENT_ENCODING));
        assertEquals("Variant[mediaType=text/plain, language=null, encoding=identity]", response
            .getEntity(String.class));
    }

    /**
     * Tests that when a JAX-RS application adds a Vary header, that the
     * Accept-Encoding value is correctly appended to the first Vary value (in
     * this case, no append takes place since the Vary header already has
     * Accept-Encoding in it).
     */
    public void testContentEncodeVaryHeaderCorrectlyReturnedByItself() throws IOException {
        RestClient client = new RestClient();
        ClientResponse response =
            client.resource(getBaseURI() + "/contentencode/varyheaderwithacceptencoding")
                .header(HttpHeaders.ACCEPT_ENCODING, "*").accept(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatusCode());
        assertEquals(HttpHeaders.ACCEPT + ", " + HttpHeaders.ACCEPT_ENCODING, response.getHeaders()
            .getFirst(HttpHeaders.VARY));
        assertEquals(1, response.getHeaders().get(HttpHeaders.VARY).size());
        assertEquals("gzip", response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        assertEquals(1, response.getHeaders().get(HttpHeaders.CONTENT_ENCODING).size());

        InputStream is = response.getEntity(InputStream.class);
        GZIPInputStream gzipIS = new GZIPInputStream(is);
        StringProvider sp = new StringProvider();
        String responseEntity =
            sp.readFrom(String.class,
                        String.class,
                        new Annotation[] {},
                        MediaType.TEXT_PLAIN_TYPE,
                        null,
                        gzipIS);
        assertEquals("Variant[mediaType=text/plain, language=null, encoding=gzip]", responseEntity);
    }

    /**
     * Tests that when a user uses a JAX-RS application to add a Vary header
     * that the header is correctly set.
     */
    public void testRegularVaryHeaderCorrectlyReturnedByUserUnmodified() throws IOException {
        RestClient client = new RestClient();
        ClientResponse response =
            client.resource(getBaseURI() + "/regular/varyheaderwithacceptencodingbyuser")
                .header(HttpHeaders.ACCEPT_ENCODING, "*").accept(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatusCode());
        assertEquals(HttpHeaders.ACCEPT_CHARSET + ","
            + HttpHeaders.ACCEPT_ENCODING.toLowerCase()
            + "  , "
            + HttpHeaders.ACCEPT, response.getHeaders().getFirst(HttpHeaders.VARY));
        assertEquals(1, response.getHeaders().get(HttpHeaders.VARY).size());
        assertNull(response.getHeaders().get(HttpHeaders.CONTENT_ENCODING));
        assertEquals("text/plain content", response.getEntity(String.class));

        response =
            client.resource(getBaseURI() + "/regular/varyheaderwithacceptencodingbyuser")
                .accept(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatusCode());
        assertEquals(HttpHeaders.ACCEPT_CHARSET + ","
            + HttpHeaders.ACCEPT_ENCODING.toLowerCase()
            + "  , "
            + HttpHeaders.ACCEPT, response.getHeaders().getFirst(HttpHeaders.VARY));
        assertEquals(1, response.getHeaders().get(HttpHeaders.VARY).size());
        assertNull(response.getHeaders().get(HttpHeaders.CONTENT_ENCODING));
        assertEquals("text/plain content", response.getEntity(String.class));
    }

    /**
     * Tests that when a JAX-RS application adds a Vary header, that the
     * Accept-Encoding value is correctly appended to the first Vary value (in
     * this case, no append takes place since the Vary header already has
     * Accept-Encoding in it).
     */
    public void testGZIPContentEncodeVaryHeaderCorrectlyReturnedByUserUnmodified()
        throws IOException {
        RestClient client = new RestClient();
        ClientResponse response =
            client.resource(getBaseURI() + "/contentencode/varyheaderwithacceptencodingbyuser")
                .header(HttpHeaders.ACCEPT_ENCODING, "*").accept(MediaType.TEXT_PLAIN).get();
        assertEquals(200, response.getStatusCode());
        assertEquals(HttpHeaders.ACCEPT_CHARSET + ","
            + HttpHeaders.ACCEPT_ENCODING.toLowerCase()
            + "  , "
            + HttpHeaders.ACCEPT, response.getHeaders().getFirst(HttpHeaders.VARY));
        assertEquals(1, response.getHeaders().get(HttpHeaders.VARY).size());
        assertEquals("gzip", response.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING));
        assertEquals(1, response.getHeaders().get(HttpHeaders.CONTENT_ENCODING).size());

        InputStream is = response.getEntity(InputStream.class);
        GZIPInputStream gzipIS = new GZIPInputStream(is);
        StringProvider sp = new StringProvider();
        String responseEntity =
            sp.readFrom(String.class,
                        String.class,
                        new Annotation[] {},
                        MediaType.TEXT_PLAIN_TYPE,
                        null,
                        gzipIS);
        assertEquals("text/plain content", responseEntity);
    }
}
