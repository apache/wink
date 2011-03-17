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

package org.apache.wink.client.internal.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.handlers.HandlerContext;
import org.apache.wink.client.internal.ClientUtils;
import org.apache.wink.common.internal.WinkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpURLConnectionHandler extends AbstractConnectionHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpURLConnectionHandler.class);

    public ClientResponse handle(ClientRequest request, HandlerContext context) throws Exception {
        try {
            HttpURLConnection connection = processRequest(request, context);
            return processResponse(request, context, connection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getBypassHostnameVerification(ClientRequest request,
                                                  HttpURLConnection connection) {
        return ((ClientConfig)request.getAttribute(WinkConfiguration.class))
            .getBypassHostnameVerification() && (connection instanceof HttpsURLConnection);
    }

    private HostnameVerifier setupHostnameVerificationBypass(HttpsURLConnection connection) {
        HttpsURLConnection https = ((HttpsURLConnection)connection);
        HostnameVerifier hv = https.getHostnameVerifier();
        https.setHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                logger
                    .trace("Bypassing hostname verification: URL host is {}, SSLSession host is {}", urlHostName, //$NON-NLS-1$
                           session.getPeerHost());
                return true;
            }
        });
        return hv;
    }

    private void teardownHostnameVerificationBypass(HttpsURLConnection connection,
                                                    HostnameVerifier hv) {
        connection.setHostnameVerifier(hv);
    }

    private HttpURLConnection processRequest(ClientRequest request, HandlerContext context)
        throws IOException {
        HttpURLConnection connection = openConnection(request);
        NonCloseableOutputStream ncos = new NonCloseableOutputStream();
        OutputStream os = ncos;
        processRequestHeaders(request, connection);
        HostnameVerifier hv = null;
        if (getBypassHostnameVerification(request, connection)) {
            hv = setupHostnameVerificationBypass((HttpsURLConnection)connection);
        }
        try {
            connection.connect();
        } finally {
            if (getBypassHostnameVerification(request, connection)) {
                teardownHostnameVerificationBypass((HttpsURLConnection)connection, hv);
            }
        }
        if (request.getEntity() != null) {
            ncos.setOutputStream(connection.getOutputStream());
            os = adaptOutputStream(ncos, request, context.getOutputStreamAdapters());
            writeEntity(request, os);
        }
        return connection;
    }

    private HttpURLConnection openConnection(ClientRequest request) throws IOException {
        URL url = request.getURI().toURL();
        HttpURLConnection connection = null;
        // we're on the client so this is a safe cast
        ClientConfig config = (ClientConfig)request.getAttribute(WinkConfiguration.class);

        // setup proxy
        if (config.getProxyHost() != null) {
            Proxy proxy =
                new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getProxyHost(), config
                    .getProxyPort()));
            connection = (HttpURLConnection)url.openConnection(proxy);
        } else {
            connection = (HttpURLConnection)url.openConnection();
        }
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod(request.getMethod());

        connection.setConnectTimeout(config.getConnectTimeout());
        connection.setReadTimeout(config.getReadTimeout());
        connection.setInstanceFollowRedirects(config.isFollowRedirects());

        return connection;
    }

    private void processRequestHeaders(ClientRequest request, HttpURLConnection connection) {
        MultivaluedMap<String, String> headers = request.getHeaders();
        for (String header : headers.keySet()) {
            List<String> values = headers.get(header);
            for (String value : values) {
                if (value != null) {
                    connection.addRequestProperty(header, value);
                }
            }
        }
        /*
         * HttpUrlConnection may set an illegal Accept header by default (a
         * "*;q=0.2" without a subtytle) so if there wasn't an Accept header,
         * then set one here.
         */
        if (headers.getFirst(HttpHeaders.ACCEPT) == null) {
            connection.addRequestProperty(HttpHeaders.ACCEPT, MediaType.WILDCARD);
        }
    }

    private ClientResponse processResponse(ClientRequest request,
                                           HandlerContext context,
                                           HttpURLConnection connection) throws IOException {
        ClientResponse response = createResponse(request, connection);
        InputStream is = null;
        if (ClientUtils.isErrorCode(response.getStatusCode())) {
            is = connection.getErrorStream();
        } else {
            is = connection.getInputStream();
        }
        is = adaptInputStream(is, response, context.getInputStreamAdapters());
        response.setEntity(is);
        return response;
    }

    private ClientResponse createResponse(ClientRequest request, HttpURLConnection connection)
        throws IOException {
        HostnameVerifier hv = null;
        if (getBypassHostnameVerification(request, connection)) {
            hv = setupHostnameVerificationBypass((HttpsURLConnection)connection);
        }
        ClientResponse response = new ClientResponseImpl();
        try {
            response.setStatusCode(connection.getResponseCode());
            response.setMessage(connection.getResponseMessage());
            response.getAttributes().putAll(request.getAttributes());
            processResponseHeaders(response, connection);
        } finally {
            if (getBypassHostnameVerification(request, connection)) {
                teardownHostnameVerificationBypass((HttpsURLConnection)connection, hv);
            }
        }
        return response;
    }

    private void processResponseHeaders(ClientResponse response, HttpURLConnection connection) {
        response.getHeaders().putAll(connection.getHeaderFields());
    }

    private static class NonCloseableOutputStream extends OutputStream {
        OutputStream os;

        public NonCloseableOutputStream() {
        }

        public void setOutputStream(OutputStream os) {
            this.os = os;
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }

        @Override
        public void flush() throws IOException {
            os.flush();
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            os.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            os.write(b);
        }

        @Override
        public void write(int b) throws IOException {
            os.write(b);
        }
    }

}
