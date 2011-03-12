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

import com.ning.http.client.AsyncCompletionHandlerBase;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.handlers.HandlerContext;
import org.apache.wink.client.internal.handlers.AbstractConnectionHandler;
import org.apache.wink.client.internal.handlers.ClientResponseImpl;
import org.apache.wink.common.internal.WinkConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Extends {@link AbstractConnectionHandler} and uses {@link AsyncHttpClient} to perform HTTP request execution.
 */
public class AsyncHttpClientConnectionHandler
    extends AbstractConnectionHandler
    implements Closeable
{
    private static final Logger logger = LoggerFactory.getLogger(AsyncHttpClientConnectionHandler.class);

    private AsyncHttpClient asyncHttpClient;

    public AsyncHttpClientConnectionHandler(final AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
    }

    public void close() throws IOException {
        asyncHttpClient.close();
    }

    public ClientResponse handle(final ClientRequest request, final HandlerContext context) throws Exception {
        Response response = processRequest(request, context);
        return processResponse(request, context, response);
    }

    private Response processRequest(final ClientRequest cr, final HandlerContext context) throws IOException {
        AsyncHttpClient asyncHttpClient = openConnection(cr);
        NonCloseableOutputStream ncos = new NonCloseableOutputStream();
        OutputStream os = adaptOutputStream(ncos, cr, context.getOutputStreamAdapters());

        Request request = setupHttpRequest(cr, ncos, os);
        Response response;
        final AtomicReference<Throwable> failureHolder = new AtomicReference<Throwable>();

        try {
            response = asyncHttpClient.executeRequest(request, new AsyncCompletionHandlerBase()
            {
                @Override
                public Response onCompleted(final Response response) throws Exception {
                    logger.trace("Response received: {}", response);
                    return super.onCompleted(response);
                }

                public void onThrowable(Throwable t) {
                    logger.trace("Request failed", t);
                    failureHolder.set(t);
                }
            }).get();
        }
        catch (InterruptedException e) {
            throw (IOException)new IOException().initCause(e);
        }
        catch (ExecutionException e) {
            throw (IOException)new IOException().initCause(e);
        }

        // If a failure occurred, then decode and re-throw
        Throwable failure = failureHolder.get();
        if (failure != null) {
            if (failure instanceof RuntimeException) {
                throw (RuntimeException)failure;
            }
            if (failure instanceof IOException) {
                throw (IOException)failure;
            }
            throw (IOException)new IOException().initCause(failure);
        }

        return response;
    }

    private Request setupHttpRequest(final ClientRequest cr, final NonCloseableOutputStream ncos, final OutputStream adaptedOutputStream) {
        URI uri = cr.getURI();
        String method = cr.getMethod();
        RequestBuilder builder = new RequestBuilder(method);
        builder.setUrl(uri.toString());

        MultivaluedMap<String, String> headers = cr.getHeaders();
        for (String header : headers.keySet()) {
            List<String> values = headers.get(header);
            for (String value : values) {
                if (value != null) {
                    builder.addHeader(header, value);
                }
            }
        }

        if (method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("POST")) {
            builder.setBody(new Request.EntityWriter()
            {
                public void writeEntity(OutputStream os) throws IOException {
                    ncos.setOutputStream(os);
                    AsyncHttpClientConnectionHandler.this.writeEntity(cr, adaptedOutputStream);
                }
            });
        }

        return builder.build();
    }

    private AsyncHttpClient openConnection(final ClientRequest request) {
        if (asyncHttpClient != null) {
            return asyncHttpClient;
        }

        // cast is safe because we're on the client
        ClientConfig config = (ClientConfig) request.getAttribute(WinkConfiguration.class);

        AsyncHttpClientConfig.Builder c = new AsyncHttpClientConfig.Builder();
        c.setConnectionTimeoutInMs(config.getConnectTimeout());
        c.setRequestTimeoutInMs(config.getReadTimeout());
        c.setFollowRedirects(config.isFollowRedirects());

        // setup proxy
        if (config.getProxyHost() != null) {
            c.setProxyServer(new ProxyServer(config.getProxyHost(), config.getProxyPort()));
        }

        return new AsyncHttpClient(c.build());
    }

    /**
     * An empty input stream to simulate an empty message body.
     */
    private static class EmptyInputStream
        extends InputStream
    {
        @Override
        public int read() throws IOException {
            return -1;
        }
    }

    private ClientResponse processResponse(final ClientRequest request, final HandlerContext context, final Response response)
        throws IllegalStateException, IOException
    {
        ClientResponse cr = createResponse(request, response);
        InputStream is;
        if (response.hasResponseBody()) {
            is = response.getResponseBodyAsStream();
        }
        else {
            is = new EmptyInputStream();
        }
        is = adaptInputStream(is, cr, context.getInputStreamAdapters());
        cr.setEntity(is);
        return cr;
    }

    private ClientResponse createResponse(final ClientRequest request, final Response response) {
        final ClientResponseImpl cr = new ClientResponseImpl();
        cr.setStatusCode(response.getStatusCode());
        cr.setMessage(response.getStatusText());
        cr.getAttributes().putAll(request.getAttributes());
        processResponseHeaders(cr, response);
        return cr;
    }

    private void processResponseHeaders(final ClientResponse cr, final Response response) {
        FluentCaseInsensitiveStringsMap headers = response.getHeaders();
        for (Map.Entry<String, List<String>> header : headers) {
            for (String value : header.getValue()) {
                cr.getHeaders().add(header.getKey(), value);
            }
        }
    }

    // TODO: move this class to the base class

    private static class NonCloseableOutputStream
        extends OutputStream
    {
        OutputStream os;

        public NonCloseableOutputStream() {
        }

        public void setOutputStream(final OutputStream os) {
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
