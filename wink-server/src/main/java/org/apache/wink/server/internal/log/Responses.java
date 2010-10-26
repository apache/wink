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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Responses implements ResponseHandler {

    private boolean              isErrorFlow        = false;

    final private static Logger  logger             = LoggerFactory.getLogger(Responses.class);

    final private static int     BREAK_POINT        =
                                                        Integer
                                                            .valueOf(System
                                                                .getProperty(Responses.class
                                                                                 .getName() + ".breakPoint",
                                                                             "4096")).intValue();

    final private static boolean IS_LOGGED_AS_BYTES =
                                                        Boolean
                                                            .valueOf(System
                                                                .getProperty(Responses.class
                                                                                 .getName() + ".logAsBytes",
                                                                             "false"))
                                                            .booleanValue();

    final private static int     BUFFER_SIZE        =
                                                        Integer
                                                            .valueOf(System
                                                                .getProperty(Responses.class
                                                                                 .getName() + ".bufferSize",
                                                                             "8192")).intValue();

    public void log() {

    }

    public void init(Properties props) {
        /* do nothing */
    }

    public void setIsErrorFlow(boolean isErrorFlow) {
        this.isErrorFlow = isErrorFlow;
    }

    public boolean isErrorFlow() {
        return isErrorFlow;
    }

    public void handleResponse(MessageContext context, HandlersChain chain) throws Throwable {
        logger.trace("handleRequest({}, {}) entry", context, chain);
        try {
            if (logger.isDebugEnabled()) {
                logStartResponse(context);
            }
            chain.doChain(context);
        } finally {
            if (logger.isDebugEnabled()) {
                logFinishResponse(context);
            }
        }
        logger.trace("handleRequest({}, {}) exit", context, chain);
    }

    void logStartResponse(MessageContext context) {
        logger.trace("logStartResponse({}) entry", context);
        try {
            if (!logger.isDebugEnabled()) {
                logger.trace("logStartResponse() exit"); // how would this ever
                // occur?
                return;
            }

            /*
             * start wrapping the OutputStream here in the hope that it will be
             * called during logFinishResponse
             */
            HttpServletResponseWrapper response =
                context.getAttribute(HttpServletResponseWrapper.class);
            if (response == null) {
                logger.debug("Could not find the HTTP Servlet Response to wrap.");
                // this is a really bad path that should probably be an error,
                // but just in case
                logger.trace("logStartRequest() exit");
                return;
            }
            ResponseWrapper wrapper = new ResponseWrapper(response);
            context.setAttribute(ResponseWrapper.class, wrapper);
            context.setAttribute(HttpServletResponse.class, wrapper);
            context.setAttribute(HttpServletResponseWrapper.class, wrapper);
        } catch (Exception e) {
            logger.trace("Could not log the start of the request", e);
        }
        logger.trace("logStartResponse() exit");
    }

    void logFinishResponse(MessageContext context) {
        logger.trace("logFinishResponse({}) entry", context);
        try {
            if (!logger.isDebugEnabled()) {
                logger.trace("logFinishResponse() exit"); // how would this ever
                // occur?
                return;
            }
            ResponseWrapper responseWrapper = context.getAttribute(ResponseWrapper.class);
            if (responseWrapper == null) {
                logger
                    .debug("Did not find the ResponseWrapper so will not log the response entity.");
                logger.trace("logStartRequest() exit");
                return;
            }

            if (isErrorFlow) {
                logger
                    .debug("An error occurred when handling the initial request/response, so wrote the entity and headers in the error response handlers chain.");
            }

            
            MultivaluedMap<String, String> headers = responseWrapper.getLoggedResponseHeaders();
            if (headers != null && headers.size() > 0) {
                List<String> keys = new ArrayList<String>(headers.keySet());
                Collections.sort(keys);
                StringBuilder sb = new StringBuilder();
                Formatter f = new Formatter(sb);
                for(String k : keys) {
                    List<String> values = headers.get(k);
                    for(String v : values) {
                        f.format("%n%1$-30s%2$s", k, v);
                    }
                }
                logger.debug("The written response headers:{}", sb);
            } else {
                logger.debug("There were no custom headers written on the response.");
            }

            LoggedServletOutputStream loggedOutputStream = responseWrapper.getLoggedOutputStream();
            if (loggedOutputStream == null || loggedOutputStream.getLoggedByteBufferLength() == 0) {
                logger
                    .debug("The response entity was not written to the HttpServletResponse.getOutputStream().");
                return;
            }
            byte[] buffer = loggedOutputStream.getLoggedByteBuffer();
            final int bufferLength = loggedOutputStream.getLoggedByteBufferLength();

            if (IS_LOGGED_AS_BYTES) {
                logger.debug("The response entity as bytes:");
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
                logger.debug("The response entity as a String in the default encoding:");
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
            context.setAttribute(ResponseWrapper.class, null);
        } catch (Exception e) {
            logger.debug("Could not log the finishing of the response", e);
        }
        logger.trace("logFinishResponse() exit");
    }

    public static class ResponseWrapper extends HttpServletResponseWrapper {
        final private HttpServletResponse      response;

        private LoggedServletOutputStream      outputStreamLogger           = null;

        private ServletOutputStream            originalStream;
        private boolean                        hasStreamBeenRetrievedBefore = false;

        private MultivaluedMap<String, String> headers                      =
                                                                                new MultivaluedMapImpl<String, String>();

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
            this.response = response;
        }

        @Override
        public void addHeader(String headerName, String headerValue) {
            /*
             * we override this in case someone manually circumvents the
             * Response object by using a @Context HttpServletResponse.
             */
            headers.add(headerName, headerValue);
            super.addHeader(headerName, headerValue);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (outputStreamLogger != null) {
                return outputStreamLogger;
            }

            if (!hasStreamBeenRetrievedBefore) {
                originalStream = response.getOutputStream();
            }
            if (originalStream == null) {
                /*
                 * this could happen in a weird situation with the web
                 * container. defensive coding for now.
                 */
                logger
                    .debug("The web container did not return a stream from HttpServletResponse.getOutputStream()");
                return null;
            }

            outputStreamLogger = new LoggedServletOutputStream(originalStream, BUFFER_SIZE);
            return outputStreamLogger;
        }

        public LoggedServletOutputStream getLoggedOutputStream() {
            return outputStreamLogger;
        }

        public MultivaluedMap<String, String> getLoggedResponseHeaders() {
            return headers;
        }
    }

    /**
     * A wrapper class for a ServletInputStream that also logs the bytes.
     */
    public static class LoggedServletOutputStream extends ServletOutputStream {

        final private ServletOutputStream originalRequest;

        private final byte[]              responseBuffer;

        private int                       offset = 0;

        public LoggedServletOutputStream(ServletOutputStream originalRequest, int bufferSize) {
            this.originalRequest = originalRequest;
            responseBuffer = new byte[bufferSize];
        }

        @Override
        public void write(int b) throws IOException {
            if (offset < responseBuffer.length) {
                responseBuffer[offset] = (byte)b;
                ++offset;
            }
            originalRequest.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (len > 0) {
                int length = len;
                if (len + offset >= responseBuffer.length) {
                    length = responseBuffer.length - offset;
                }
                System.arraycopy(b, off, responseBuffer, offset, length);
                offset += length;
            }
            originalRequest.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            if (b.length > 0) {
                int length = b.length;
                if (b.length + offset >= responseBuffer.length) {
                    length = responseBuffer.length - offset;
                }
                System.arraycopy(b, 0, responseBuffer, offset, length);
                offset += length;
            }
            originalRequest.write(b);
        }

        public int getLoggedByteBufferLength() {
            return offset;
        }

        public byte[] getLoggedByteBuffer() {
            return responseBuffer;
        }

        @Override
        public void close() throws IOException {
            originalRequest.close();
        }

        @Override
        public void flush() throws IOException {
            originalRequest.flush();
        }
    }
}
