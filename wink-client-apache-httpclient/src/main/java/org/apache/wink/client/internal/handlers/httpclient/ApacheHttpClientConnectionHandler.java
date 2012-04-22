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

package org.apache.wink.client.internal.handlers.httpclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.handlers.HandlerContext;
import org.apache.wink.client.httpclient.ApacheHttpClientConfig;
import org.apache.wink.client.internal.handlers.AbstractConnectionHandler;
import org.apache.wink.client.internal.handlers.ClientResponseImpl;
import org.apache.wink.common.internal.WinkConfiguration;

/**
 * Extends AbstractConnectionHandler and uses Apache HttpClient to perform HTTP
 * request execution. Each outgoing Http request is wrapped by EntityWriter.
 */
public class ApacheHttpClientConnectionHandler extends AbstractConnectionHandler {

    private HttpClient httpclient;

    public ApacheHttpClientConnectionHandler() {
        httpclient = null;
    }

    public ApacheHttpClientConnectionHandler(HttpClient httpclient) {
        this.httpclient = httpclient;
    }

    public ClientResponse handle(ClientRequest request, HandlerContext context) throws Exception {
        HttpResponse response = null;
        try {
            response = processRequest(request, context);
            return processResponse(request, context, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse processRequest(ClientRequest request, HandlerContext context)
        throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpClient client = openConnection(request);
        // TODO: move this functionality to the base class
        NonCloseableOutputStream ncos = new NonCloseableOutputStream();
        OutputStream os = ncos;

        EntityWriter entityWriter = null;
        if (request.getEntity() != null) {
            os = adaptOutputStream(ncos, request, context.getOutputStreamAdapters());
            // cast is safe because we're on the client
            ApacheHttpClientConfig config = (ApacheHttpClientConfig)request.getAttribute(WinkConfiguration.class);
            // prepare the entity that will write our entity
            entityWriter = new EntityWriter(this, request, os, ncos, config.isChunked());
        }

        HttpRequestBase entityRequest = setupHttpRequest(request, client, entityWriter);

        try {
            return client.execute(entityRequest);
        } catch (Exception ex) {
            entityRequest.abort();
            throw new RuntimeException(ex);
        }
    }

    private HttpRequestBase setupHttpRequest(ClientRequest request,
                                             HttpClient client,
                                             EntityWriter entityWriter) {
        URI uri = request.getURI();
        String method = request.getMethod();
        HttpRequestBase httpRequest = null;
        if (entityWriter == null) {
            GenericHttpRequestBase entityRequest = new GenericHttpRequestBase(method);
            httpRequest = entityRequest;
        } else {
            // create a new request with the specified method
            HttpEntityEnclosingRequestBase entityRequest =
                new GenericHttpEntityEnclosingRequestBase(method);
            entityRequest.setEntity(entityWriter);
            httpRequest = entityRequest;
        }
        // set the uri
        httpRequest.setURI(uri);
        // add all headers
        MultivaluedMap<String, String> headers = request.getHeaders();
        for (String header : headers.keySet()) {
            List<String> values = headers.get(header);
            for (String value : values) {
                if (value != null) {
                    httpRequest.addHeader(header, value);
                }
            }
        }
        return httpRequest;
    }

    private synchronized HttpClient openConnection(ClientRequest request) throws NoSuchAlgorithmException, KeyManagementException {
        if (this.httpclient != null) {
            return this.httpclient;
        }

        // cast is safe because we're on the client
        ApacheHttpClientConfig config = (ApacheHttpClientConfig)request.getAttribute(WinkConfiguration.class);
        BasicHttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, Integer.valueOf(config
            .getConnectTimeout()));
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, Integer.valueOf(config
            .getReadTimeout()));
        params.setParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.valueOf(config
            .isFollowRedirects()));
        if (config.isFollowRedirects()) {
            params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, Boolean.TRUE);
        }
        // setup proxy
        if (config.getProxyHost() != null) {
            params.setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost(config.getProxyHost(),
                                                                            config.getProxyPort()));
        }

        if (config.getMaxPooledConnections() > 0) {
            SchemeRegistry schemeRegistry = SchemeRegistryFactory.createDefault();
            ThreadSafeClientConnManager httpConnectionManager = new ThreadSafeClientConnManager(schemeRegistry);

            httpConnectionManager.setMaxTotal(config.getMaxPooledConnections());
            httpConnectionManager.setDefaultMaxPerRoute(config.getMaxPooledConnections());

            this.httpclient = new DefaultHttpClient(httpConnectionManager, params);
        } else {
            this.httpclient = new DefaultHttpClient(params);
        }

        if (config.getBypassHostnameVerification()) {
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, null, null);

            SSLSocketFactory sf = new SSLSocketFactory(sslcontext, new X509HostnameVerifier() {

                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }

                public void verify(String host, String[] cns, String[] subjectAlts)
                    throws SSLException {
                }

                public void verify(String host, X509Certificate cert) throws SSLException {
                }

                public void verify(String host, SSLSocket ssl) throws IOException {
                }
            });
            httpclient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, sf));
        }

        return this.httpclient;
    }

    /**
     * An empty input stream to simulate an empty message body.
     */
    private static class EmptyInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            return -1;
        }
    }

    private ClientResponse processResponse(ClientRequest request,
                                           HandlerContext context,
                                           HttpResponse httpResponse) throws IllegalStateException,
        IOException {
        ClientResponse response = createResponse(request, httpResponse);
        HttpEntity entity = httpResponse.getEntity();
        InputStream is = null;
        if (entity == null) {
            is = new EmptyInputStream();
        } else {
            is = entity.getContent();
        }
        is = adaptInputStream(is, response, context.getInputStreamAdapters());
        response.setEntity(is);
        return response;
    }

    private ClientResponse createResponse(ClientRequest request, final HttpResponse httpResponse) {
        final ClientResponseImpl response = new ClientResponseImpl();
        StatusLine statusLine = httpResponse.getStatusLine();
        response.setStatusCode(statusLine.getStatusCode());
        response.setMessage(statusLine.getReasonPhrase());
        response.getAttributes().putAll(request.getAttributes());
        response.setContentConsumer(new Runnable() {
            
            public void run() {
              HttpEntity entity = httpResponse.getEntity();
              if (entity != null) {
                  try {
                    EntityUtils.consume(entity);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
              }
            }
        });
        processResponseHeaders(response, httpResponse);
        return response;
    }

    private void processResponseHeaders(ClientResponse response, HttpResponse httpResponse) {
        Header[] allHeaders = httpResponse.getAllHeaders();
        for (Header header : allHeaders) {
            response.getHeaders().add(header.getName(), header.getValue());
        }
    }

    private static class GenericHttpRequestBase extends HttpRequestBase {
        private String method;

        public GenericHttpRequestBase(String method) {
            this.method = method;
        }

        @Override
        public String getMethod() {
            return method;
        }
    }

    private static class GenericHttpEntityEnclosingRequestBase extends
        HttpEntityEnclosingRequestBase {
        private String method;

        public GenericHttpEntityEnclosingRequestBase(String method) {
            this.method = method;
        }

        @Override
        public String getMethod() {
            return method;
        }
    }

    // TODO: move this class to the base class
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

    private static class EntityWriter implements HttpEntity {

        private ApacheHttpClientConnectionHandler apacheHttpClientHandler;
        private ClientRequest request;
        private OutputStream adaptedOutputStream;
        private NonCloseableOutputStream ncos;
        private boolean chunked;
        private long length = -1l;
        private byte[] content;

        public EntityWriter(ApacheHttpClientConnectionHandler apacheHttpClientHandler,
                            ClientRequest request,
                            OutputStream adaptedOutputStream,
                            NonCloseableOutputStream ncos,
                            boolean chunked) {
            this.apacheHttpClientHandler = apacheHttpClientHandler;
            this.request = request;
            this.adaptedOutputStream = adaptedOutputStream;
            this.ncos = ncos;
            this.chunked = chunked;

            if (!chunked) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    apacheHttpClientHandler.writeEntity(request, bos);
                    content = bos.toByteArray();
                    length = content.length;
                } catch (IOException e) {
                    throw new WebApplicationException(e);
                }
            }
        }

        @Deprecated
        public void consumeContent() throws IOException {
        }

        public InputStream getContent() throws IOException, IllegalStateException {
            return null;
        }

        public Header getContentEncoding() {
            return null;
        }

        public long getContentLength() {
            return length;
        }

        public Header getContentType() {
            return null;
        }

        public boolean isChunked() {
            return chunked;
        }

        public boolean isRepeatable() {
            return true;
        }

        public boolean isStreaming() {
            return content == null;
        }

        public void writeTo(OutputStream os) throws IOException {
            if (!chunked && length > 0 && content != null) {
                os.write(content);
                os.flush();
            } else {
                ncos.setOutputStream(os);
                apacheHttpClientHandler.writeEntity(request, adaptedOutputStream);
            }
        }
    }
}
