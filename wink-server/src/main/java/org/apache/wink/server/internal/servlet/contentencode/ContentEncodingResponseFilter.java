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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 */
package org.apache.wink.server.internal.servlet.contentencode;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.http.AcceptEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet filter which changes the HttpServletResponse to
 * automatically deflate or GZIP encode an outgoing response if the incoming
 * request has an appropriate Accept-Encoding request header value. Add to your
 * web.xml like: <br/>
 * <code>
 * &lt;filter&gt;<br/>
        &lt;filter-name&gt;ContentEncodingResponseFilter&lt;/filter-name&gt;<br/>
        &lt;filter-class&gt;org.apache.wink.server.internal.servlet.contentencode.ContentEncodingResponseFilter&lt;/filter-class&gt;<br/>
    &lt;/filter&gt;<br/>
    <br/>
    &lt;filter-mapping&gt;<br/>
        &lt;filter-name&gt;ContentEncodingResponseFilter&lt;/filter-name&gt;<br/>
        &lt;url-pattern&gt;/*&lt;/url-pattern&gt;<br/>
    &lt;/filter-mapping&gt;<br/>
 * </code>
 */
public class ContentEncodingResponseFilter implements Filter {

    private final static Logger                         logger                       =
                                                                                         LoggerFactory
                                                                                             .getLogger(ContentEncodingResponseFilter.class);

    private final static HeaderDelegate<AcceptEncoding> acceptEncodingHeaderDelegate =
                                                                                         RuntimeDelegate
                                                                                             .getInstance()
                                                                                             .createHeaderDelegate(AcceptEncoding.class);

    public void init(FilterConfig arg0) throws ServletException {
        logger.debug("init({}) entry", arg0); //$NON-NLS-1$
        /* do nothing */
        logger.debug("init() exit"); //$NON-NLS-1$
    }

    public void destroy() {
        logger.debug("destroy() entry"); //$NON-NLS-1$
        /* do nothing */
        logger.debug("destroy() exit"); //$NON-NLS-1$
    }

    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        if (logger.isDebugEnabled()) {
            logger.debug("doFilter({}, {}, {}) entry", new Object[] {servletRequest, //$NON-NLS-1$
                servletResponse, chain});
        }
        /*
         * wraps the servlet response if necessary
         */
        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
            final AcceptEncoding acceptEncoding = getAcceptEncodingHeader(httpServletRequest);
            logger.debug("AcceptEncoding header was {}", acceptEncoding); //$NON-NLS-1$
            if (acceptEncoding != null && (acceptEncoding.isAnyEncodingAllowed() || acceptEncoding
                .getAcceptableEncodings().size() > 0)) {
                logger.debug("AcceptEncoding header was set so wrapping HttpServletResponse"); //$NON-NLS-1$
                HttpServletResponseContentEncodingWrapperImpl wrappedServletResponse =
                    new HttpServletResponseContentEncodingWrapperImpl(
                                                                      (HttpServletResponse)servletResponse,
                                                                      acceptEncoding);
                logger.debug("Passing on request and response down the filter chain"); //$NON-NLS-1$
                chain.doFilter(servletRequest, wrappedServletResponse);
                logger.debug("Finished filter chain"); //$NON-NLS-1$
                EncodedOutputStream encodedOutputStream =
                    wrappedServletResponse.getEncodedOutputStream();
                if (encodedOutputStream != null) {
                    logger.debug("Calling encodedOutputStream finish"); //$NON-NLS-1$
                    encodedOutputStream.finish();
                }
                logger.debug("doFilter exit()"); //$NON-NLS-1$
                return;
            }
        }
        logger.debug("AcceptEncoding header not found so processing like normal request"); //$NON-NLS-1$
        chain.doFilter(servletRequest, servletResponse);
        logger.debug("doFilter exit()"); //$NON-NLS-1$
    }

    /**
     * Returns an AcceptEncoding object if there is an Accept Encoding header.
     * 
     * @param httpServletRequest
     * @return
     */
    static AcceptEncoding getAcceptEncodingHeader(HttpServletRequest httpServletRequest) {
        logger.debug("getAcceptEncodingHeader({}) entry", httpServletRequest); //$NON-NLS-1$
        Enumeration<String> acceptEncodingEnum =
            httpServletRequest.getHeaders(HttpHeaders.ACCEPT_ENCODING);
        StringBuilder sb = new StringBuilder();
        if (acceptEncodingEnum.hasMoreElements()) {
            sb.append(acceptEncodingEnum.nextElement());
            while (acceptEncodingEnum.hasMoreElements()) {
                sb.append(","); //$NON-NLS-1$
                sb.append(acceptEncodingEnum.nextElement());
            }
            String acceptEncodingHeader = sb.toString();
            logger.debug("acceptEncodingHeader is {} so returning as AcceptEncodingHeader", //$NON-NLS-1$
                         acceptEncodingHeader);
            return acceptEncodingHeaderDelegate.fromString(acceptEncodingHeader);
        }
        logger.debug("No Accept-Encoding header"); //$NON-NLS-1$
        logger.debug("getAcceptEncodingHeader() exit - returning null"); //$NON-NLS-1$
        return null;
    }

    static abstract class EncodedOutputStream extends ServletOutputStream {

        private boolean              isWritten = false;

        private DeflaterOutputStream outputStream;

        public EncodedOutputStream(DeflaterOutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int b) throws IOException {
            if (!isWritten) {
                isFirstWrite();
                isWritten = true;
            }
            outputStream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (!isWritten) {
                isFirstWrite();
                isWritten = true;
            }
            outputStream.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            if (!isWritten) {
                isFirstWrite();
                isWritten = true;
            }
            outputStream.write(b);
        }

        @Override
        public void flush() throws IOException {
            if (!isWritten) {
                isFirstWrite();
                isWritten = true;
            }
            outputStream.flush();
        }

        @Override
        public void close() throws IOException {
            outputStream.finish();
            outputStream.close();
        }

        public void finish() throws IOException {
            outputStream.finish();
        }

        public abstract void isFirstWrite();
    }

    static class GzipEncoderOutputStream extends EncodedOutputStream {

        final private HttpServletResponse response;

        public GzipEncoderOutputStream(OutputStream outputStream, HttpServletResponse response)
            throws IOException {
            super(new GZIPOutputStream(outputStream));
            this.response = response;
        }

        @Override
        public void isFirstWrite() {
            response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip"); //$NON-NLS-1$
            response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
        }
    }

    static class DeflaterContentEncodedOutputStream extends EncodedOutputStream {

        final private HttpServletResponse response;

        public DeflaterContentEncodedOutputStream(OutputStream outputStream,
                                                  HttpServletResponse response) throws IOException {
            super(new DeflaterOutputStream(outputStream));
            this.response = response;
        }

        @Override
        public void isFirstWrite() {
            response.addHeader(HttpHeaders.CONTENT_ENCODING, "deflate"); //$NON-NLS-1$
            response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
        }
    }

    static class HttpServletResponseContentEncodingWrapperImpl extends HttpServletResponseWrapper {

        private final static Logger  logger          =
                                                         LoggerFactory
                                                             .getLogger(HttpServletResponseContentEncodingWrapperImpl.class);

        final private AcceptEncoding acceptEncoding;

        private ServletOutputStream  outputStream;

        private EncodedOutputStream  encodedOutputStream;

        private int                  varyHeaderCount = 0;

        public EncodedOutputStream getEncodedOutputStream() {
            return encodedOutputStream;
        }

        public HttpServletResponseContentEncodingWrapperImpl(HttpServletResponse response,
                                                             AcceptEncoding acceptEncoding) {
            super(response);
            this.acceptEncoding = acceptEncoding;
        }

        private boolean containsAcceptEncoding(String value) {
            String[] str = value.split(","); //$NON-NLS-1$
            for (String s : str) {
                if (HttpHeaders.ACCEPT_ENCODING.equalsIgnoreCase(s.trim())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void addHeader(String name, String value) {
            logger.debug("addHeader({}, {}) entry", name, value); //$NON-NLS-1$
            /*
             * this logic is added to append Accept-Encoding to the first Vary
             * header value.
             */
            if (HttpHeaders.VARY.equalsIgnoreCase(name)) {
                ++varyHeaderCount;
                logger.debug("Vary header count is now {}", varyHeaderCount); //$NON-NLS-1$
                if (varyHeaderCount == 1) {
                    // add the Accept-Encoding value to the Vary header
                    if (!"*".equals(value) && !containsAcceptEncoding(value)) { //$NON-NLS-1$
                        logger
                            .debug("Vary header did not contain Accept-Encoding so appending to Vary header value"); //$NON-NLS-1$
                        super.addHeader(HttpHeaders.VARY, value + ", " //$NON-NLS-1$
                            + HttpHeaders.ACCEPT_ENCODING);
                        return;
                    }
                } else if (HttpHeaders.ACCEPT_ENCODING.equals(value)) {
                    logger
                        .debug("Skipping Vary header that was only Accept-Encoding since it was already appended to a previous Vary header value"); //$NON-NLS-1$
                    // skip this addition since it has already been appended to
                    // the first Vary value by the "if true" block above
                    return;
                }
            }
            super.addHeader(name, value);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            logger.debug("getOutputStream() entry"); //$NON-NLS-1$
            if (outputStream == null) {
                logger.debug("output stream was null"); //$NON-NLS-1$
                this.outputStream = super.getOutputStream();
                List<String> acceptableEncodings = acceptEncoding.getAcceptableEncodings();
                logger.debug("acceptableEncodings is {}", acceptableEncodings); //$NON-NLS-1$
                for (String encoding : acceptableEncodings) {
                    logger.debug("encoding under test is {}", encoding); //$NON-NLS-1$
                    if ("gzip".equalsIgnoreCase(encoding)) { //$NON-NLS-1$
                        logger.debug("going to use gzip encoding"); //$NON-NLS-1$
                        this.encodedOutputStream = new GzipEncoderOutputStream(outputStream, this);
                        this.outputStream = encodedOutputStream;
                        logger.debug("getOutputStream() exit - returning gzipped encode stream"); //$NON-NLS-1$
                        return outputStream;
                    } else if ("deflate".equalsIgnoreCase(encoding)) { //$NON-NLS-1$
                        logger.debug("going to use deflate encoding"); //$NON-NLS-1$
                        this.encodedOutputStream =
                            new DeflaterContentEncodedOutputStream(outputStream, this);
                        this.outputStream = encodedOutputStream;
                        logger.debug("getOutputStream() exit - returning deflate encode stream"); //$NON-NLS-1$
                        return outputStream;
                    }
                }

                if (acceptEncoding.isAnyEncodingAllowed() && !acceptEncoding.getBannedEncodings()
                    .contains("gzip")) { //$NON-NLS-1$
                    logger.debug("going to use gzip encoding because any encoding is allowed"); //$NON-NLS-1$
                    this.encodedOutputStream = new GzipEncoderOutputStream(outputStream, this);
                    this.outputStream = encodedOutputStream;
                    logger.debug("getOutputStream() exit - returning gzipped encode stream"); //$NON-NLS-1$
                    return outputStream;
                }
            }
            logger.debug("getOutputStream() exit - returning output stream"); //$NON-NLS-1$
            return outputStream;
        }
    }
}
