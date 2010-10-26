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
 */
package org.apache.wink.server.internal.log;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs the incoming requests from client to server. This will log the headers
 * plus the containers.
 */
public class Requests implements RequestHandler {

    final private static Logger  logger             = LoggerFactory.getLogger(Requests.class);

    final private static int     BREAK_POINT        =
                                                        Integer
                                                            .valueOf(System
                                                                .getProperty(Requests.class
                                                                                 .getName() + ".breakPoint",
                                                                             "4096")).intValue();

    final private static boolean IS_LOGGED_AS_BYTES =
                                                        Boolean
                                                            .valueOf(System
                                                                .getProperty(Requests.class
                                                                                 .getName() + ".logAsBytes",
                                                                             "false"))
                                                            .booleanValue();

    final private static int     BUFFER_SIZE        =
                                                        Integer
                                                            .valueOf(System
                                                                .getProperty(Requests.class
                                                                                 .getName() + ".bufferSize",
                                                                             "8192")).intValue();

    public void init(Properties props) {
        /* do nothing */
    }

    public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {
        logger.trace("handleRequest({}, {}) entry", context, chain);
        try {
            if (logger.isDebugEnabled()) {
                logStartRequest(context);
            }
            chain.doChain(context);
        } finally {
            if (logger.isDebugEnabled()) {
                logFinishRequest(context);
            }
        }
        logger.trace("handleRequest({}, {}) exit", context, chain);
    }

    void logStartRequest(MessageContext context) {
        logger.trace("logStartRequest() entry");
        try {
            if (!logger.isDebugEnabled()) {
                logger.trace("logStartRequest() exit"); // how would this ever
                // occur?
                return;
            }
            
            /*
             * start wrapping the InputStream here in the hope that it will be
             * called during logFinishRequest
             */
            HttpServletRequestWrapper request =
                context.getAttribute(HttpServletRequestWrapper.class);
            if (request == null) {
                logger.debug("Could not find the HTTP Servlet Request to wrap.");
                // this is a really bad path that should probably be an error,
                // but just in case
                logger.trace("logStartRequest() exit");
                return;
            }
            
            logger.debug("Request URI is " + context.getUriInfo().getRequestUri().toASCIIString());
            HttpHeaders headers = context.getHttpHeaders();
            MultivaluedMap<String, String> headersMap = headers.getRequestHeaders();
            String headersString = "{";
            for(String key : headersMap.keySet()) {
                headersString += (key + "=" + headersMap.get(key) + ",");
            }
            if(headersString.length() > 1)
                headersString = headersString.substring(0, headersString.length() -1);
            headersString += "}";
            logger.debug("HTTP Headers are " + headersString);
            
            RequestWrapper wrapper = new RequestWrapper(request);
            context.setAttribute(RequestWrapper.class, wrapper);
            context.setAttribute(HttpServletRequest.class, wrapper);
            context.setAttribute(HttpServletRequestWrapper.class, wrapper);
        } catch (Exception e) {
            logger.trace("Could not log the start of the request", e);
        }
        logger.trace("logStartRequest() exit");
    }

    void logFinishRequest(MessageContext context) {
        logger.trace("logFinishRequest() entry");
        try {
            if (!logger.isDebugEnabled()) {
                logger.trace("logFinishRequest() exit"); // how would this ever
                // occur?
                return;
            }
            RequestWrapper requestWrapper = context.getAttribute(RequestWrapper.class);
            if (requestWrapper == null) {
                logger.debug("Did not find the RequestWrapper so will not log the request entity.");
                logger.trace("logStartRequest() exit");
                return;
            }
            LoggedServletInputStream loggedInputStream = requestWrapper.getLoggedInputStream();
            if (loggedInputStream == null || loggedInputStream.getLoggedByteBufferLength() == 0) {
                logger
                    .debug("The request entity was not read from the HttpServletRequest.getInputStream().");
                return;
            }
            byte[] buffer = loggedInputStream.getLoggedByteBuffer();
            final int bufferLength = loggedInputStream.getLoggedByteBufferLength();

            if (IS_LOGGED_AS_BYTES) {
                logger.debug("The request entity as bytes:");
                StringBuffer sb = new StringBuffer();
                int outputCount = 0;

                for (int count = 0; count < bufferLength; ++count) {
                    sb.append(String.format("%#04x ", buffer[count]));
                    sb.append(" ");
                    ++outputCount;
                    if (outputCount > BREAK_POINT) {
                        /*
                         * 100KB increments
                         */
                        logger.debug("{}", sb);
                        sb = new StringBuffer();
                        outputCount = 0;
                    }
                }
                if (outputCount > 0) {
                    logger.debug("{}", sb);
                    sb = new StringBuffer();
                }
            } else {
                logger.debug("The request entity as a String in the default encoding:");
                int offset = 0;
                while (offset < bufferLength) {
                    int length = bufferLength - offset;
                    if (length > BREAK_POINT) {
                        length = BREAK_POINT;
                    }
                    String str = new String(buffer, offset, length);
                    logger.debug("{}", str);
                    offset += length;
                }
            }
            /*
             * remove it in case during a Response chain, an exception is thrown
             * and then a Request needs to be logged again. so only 1 shot at
             * logging a request.
             */
            context.setAttribute(RequestWrapper.class, null);
        } catch (Exception e) {
            logger.debug("Could not log the finishing of the request", e);
        }
        logger.trace("logFinishRequest() exit");
    }

    public static class RequestWrapper extends HttpServletRequestWrapper {
        final private HttpServletRequest request;

        private LoggedServletInputStream inputStreamLogger            = null;

        private ServletInputStream       originalStream;
        private boolean                  hasStreamBeenRetrievedBefore = false;

        public RequestWrapper(HttpServletRequest request) {
            super(request);
            this.request = request;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (inputStreamLogger != null) {
                return inputStreamLogger;
            }

            if (!hasStreamBeenRetrievedBefore) {
                originalStream = request.getInputStream();
            }
            if (originalStream == null) {
                /*
                 * this could happen in a weird situation with the web
                 * container. defensive coding for now.
                 */
                logger
                    .debug("The web container did not return a stream from HttpServletRequest.getInputStream()");
                return null;
            }

            inputStreamLogger = new LoggedServletInputStream(originalStream, BUFFER_SIZE);
            return inputStreamLogger;
        }

        public LoggedServletInputStream getLoggedInputStream() {
            return inputStreamLogger;
        }
    }

    /**
     * A wrapper class for a ServletInputStream that also logs the bytes.
     */
    public static class LoggedServletInputStream extends ServletInputStream {

        final private ServletInputStream originalRequest;

        private final byte[]             requestBuffer;

        private int                      offset = 0;

        public LoggedServletInputStream(ServletInputStream originalRequest, int bufferSize) {
            this.originalRequest = originalRequest;
            requestBuffer = new byte[bufferSize];
        }

        @Override
        public int available() throws IOException {
            return originalRequest.available();
        }

        @Override
        public void close() throws IOException {
            originalRequest.close();
        }

        @Override
        public void mark(int readlimit) {
            originalRequest.mark(readlimit);
        }

        @Override
        public boolean markSupported() {
            return originalRequest.markSupported();
        }

        @Override
        public int read() throws IOException {
            int value = originalRequest.read();

            if (value < 0) {
                return value;
            }

            if (offset < requestBuffer.length - 1) {
                requestBuffer[offset] = (byte)value;
                ++offset;
            }
            return value;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int value = originalRequest.read(b);

            if (value < 0) {
                return value;
            }

            int diff = value;
            if (offset + value > requestBuffer.length) {
                diff = requestBuffer.length - offset;
            }

            if (diff > 0) {
                System.arraycopy(b, 0, requestBuffer, offset, diff);
                offset += diff;
            }
            return value;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int value = originalRequest.read(b, off, len);

            if (value < 0) {
                return value;
            }

            int diff = value;
            if (offset + value > requestBuffer.length) {
                diff = requestBuffer.length - offset;
            }

            if (diff > 0) {
                System.arraycopy(b, off, requestBuffer, offset, diff);
                offset += diff;
            }

            return value;
        }

        @Override
        public int readLine(byte[] b, int off, int len) throws IOException {
            int value = originalRequest.readLine(b, off, len);

            if (value < 0) {
                return value;
            }

            int diff = value;
            if (offset + value > requestBuffer.length) {
                diff = requestBuffer.length - offset;
            }

            if (diff > 0) {
                System.arraycopy(b, off, requestBuffer, offset, diff);
                offset += diff;
            }

            return value;
        }

        @Override
        public void reset() throws IOException {
            originalRequest.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            return originalRequest.skip(n);
        }

        public byte[] getLoggedByteBuffer() {
            return requestBuffer;
        }

        public int getLoggedByteBufferLength() {
            return offset;
        }

    }
}
