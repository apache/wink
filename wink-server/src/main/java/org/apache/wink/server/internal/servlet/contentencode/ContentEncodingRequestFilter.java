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
package org.apache.wink.server.internal.servlet.contentencode;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet filter which changes the HttpServletRequest to
 * automatically inflate or GZIP decode an incoming request that has an
 * appropriate Content-Encoding request header value. Add to your web.xml like: <br/>
 * <code>
 * &lt;filter&gt;<br/>
        &lt;filter-name&gt;ContentEncodingRequestFilter&lt;/filter-name&gt;<br/>
        &lt;filter-class&gt;org.apache.wink.server.internal.servlet.contentencode.ContentEncodingRequestFilter&lt;/filter-class&gt;<br/>
    &lt;/filter&gt;<br/>
    <br/>
    &lt;filter-mapping&gt;<br/>
        &lt;filter-name&gt;ContentEncodingRequestFilter&lt;/filter-name&gt;<br/>
        &lt;url-pattern&gt;/*&lt;/url-pattern&gt;<br/>
    &lt;/filter-mapping&gt;<br/>
 * </code>
 */
public class ContentEncodingRequestFilter implements Filter {

    private static final Logger logger =
                                           LoggerFactory
                                               .getLogger(ContentEncodingRequestFilter.class);

    public void init(FilterConfig arg0) throws ServletException {
        logger.debug("init({}) entry", arg0);
        /* do nothing */
        logger.debug("init() exit");
    }

    public void destroy() {
        logger.debug("destroy() entry");
        /* do nothing */
        logger.debug("destroy() exit");
    }

    private String getContentEncoding(HttpServletRequest httpServletRequest) {
        String contentEncoding = httpServletRequest.getHeader(HttpHeaders.CONTENT_ENCODING);
        if (contentEncoding == null) {
            return null;
        }
        contentEncoding.trim();
        return contentEncoding;
    }

    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        if (logger.isDebugEnabled()) {
            logger.debug("doFilter({}, {}, {}) entry", new Object[] {servletRequest,
                servletResponse, chain});
        }
        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
            String contentEncoding = getContentEncoding(httpServletRequest);
            logger.debug("Content-Encoding was {}", contentEncoding);
            if (contentEncoding != null) {
                if ("gzip".equals(contentEncoding) || "deflate".equals(contentEncoding)) {
                    logger
                        .debug("Wrapping HttpServletRequest because Content-Encoding was set to gzip or deflate");
                    httpServletRequest =
                        new HttpServletRequestContentEncodingWrapperImpl(httpServletRequest,
                                                                         contentEncoding);
                    logger.debug("Invoking chain with wrapped HttpServletRequest");
                    chain.doFilter(httpServletRequest, servletResponse);
                    logger.debug("doFilter exit()");
                    return;
                }
            }
        }
        logger
            .debug("Invoking normal chain since Content-Encoding request header was not understood");
        chain.doFilter(servletRequest, servletResponse);
        logger.debug("doFilter exit()");
    }

    static class DecoderServletInputStream extends ServletInputStream {

        final private InputStream is;

        public DecoderServletInputStream(InputStream is) {
            this.is = is;
        }

        @Override
        public int readLine(byte[] b, int off, int len) throws IOException {
            return is.read(b, off, len);
        }

        @Override
        public int available() throws IOException {
            return is.available();
        }

        @Override
        public void close() throws IOException {
            is.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            is.mark(readlimit);
        }

        @Override
        public boolean markSupported() {
            return is.markSupported();
        }

        @Override
        public int read() throws IOException {
            return is.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return is.read(b, off, len);
        }

        @Override
        public int read(byte[] b) throws IOException {
            return is.read(b);
        }

        @Override
        public synchronized void reset() throws IOException {
            is.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            return is.skip(n);
        }
    }

    static class GZIPDecoderInputStream extends DecoderServletInputStream {

        public GZIPDecoderInputStream(InputStream is) throws IOException {
            super(new GZIPInputStream(is));
        }
    }

    static class InflaterDecoderInputStream extends DecoderServletInputStream {

        public InflaterDecoderInputStream(InputStream is) {
            super(new InflaterInputStream(is));
        }

    }

    static class HttpServletRequestContentEncodingWrapperImpl extends HttpServletRequestWrapper {

        private ServletInputStream inputStream;

        final private String       contentEncoding;

        public HttpServletRequestContentEncodingWrapperImpl(HttpServletRequest request,
                                                            String contentEncoding) {
            super(request);
            this.contentEncoding = contentEncoding;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            logger.debug("getInputStream() entry");
            if (inputStream == null) {
                inputStream = super.getInputStream();
                if ("gzip".equals(contentEncoding)) {
                    logger.debug("Wrapping ServletInputStream with GZIPDecoder");
                    inputStream = new GZIPDecoderInputStream(inputStream);
                } else if ("deflate".equals(contentEncoding)) {
                    logger.debug("Wrapping ServletInputStream with Inflater");
                    inputStream = new InflaterDecoderInputStream(inputStream);
                }
            }
            logger.debug("getInputStream() exit - returning {}", inputStream);
            return inputStream;
        }
    }

}
