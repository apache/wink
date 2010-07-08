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
package org.apache.wink.server.internal.handlers;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.HttpMethod;

import org.apache.wink.common.RestConstants;
import org.apache.wink.common.http.HttpStatus;
import org.apache.wink.server.handlers.HandlersChain;
import org.apache.wink.server.handlers.MessageContext;
import org.apache.wink.server.handlers.RequestHandler;
import org.apache.wink.server.handlers.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeadMethodHandler implements RequestHandler, ResponseHandler {

    private static final Logger logger                     =
                                                               LoggerFactory
                                                                   .getLogger(HeadMethodHandler.class);

    private static final String ORIGINAL_RESPONSE_ATT_NAME =
                                                               HeadMethodHandler.class.getName() + "_original_response"; //$NON-NLS-1$

    public void handleRequest(MessageContext context, HandlersChain chain) throws Throwable {

        // first thing - proceed the chain
        chain.doChain(context);

        // check the search result.
        // if the request is HEAD and no method was found, try to find a GET
        // method
        // and discard the entity
        SearchResult searchResult = context.getAttribute(SearchResult.class);
        if (searchResult.isError() && searchResult.getError().getResponse().getStatus() == HttpStatus.METHOD_NOT_ALLOWED
            .getCode()
            && context.getHttpMethod().equalsIgnoreCase(HttpMethod.HEAD)) {
            logger
                .trace("No HEAD method so trying GET method while not sending the response entity"); //$NON-NLS-1$
            context.setHttpMethod(HttpMethod.GET);
            HttpServletResponse originalResponse = context.getAttribute(HttpServletResponse.class);
            NoBodyResponse noBodyResponse = new NoBodyResponse(originalResponse);
            context.setAttribute(HttpServletResponse.class, noBodyResponse);
            context.getAttributes().put(ORIGINAL_RESPONSE_ATT_NAME, originalResponse);
            chain.doChain(context);
        }
    }

    public void handleResponse(MessageContext context, HandlersChain chain) throws Throwable {
        HttpServletResponse originalResponse =
            (HttpServletResponse)context.getAttributes().remove(ORIGINAL_RESPONSE_ATT_NAME);
        if (originalResponse != null) {
            HttpServletResponse response = context.getAttribute(HttpServletResponse.class);
            response.flushBuffer();
            response.setContentLength(((NoBodyResponse)response).getContentLengthValue());
            // set the original response on the context
            context.setAttribute(HttpServletResponse.class, originalResponse);
        }
        chain.doChain(context);
    }

    private static final class NoBodyResponse extends HttpServletResponseWrapper {

        private PrintWriter    writer         = null;
        private CountingStream countingStream = null;

        NoBodyResponse(HttpServletResponse servletResponse) {
            super(servletResponse);
        }

        int getContentLengthValue() {
            return countingStream.getByteCount();
        }

        @Override
        public void flushBuffer() {
            if (writer != null) {
                writer.flush();
            }
        }

        public PrintWriter getWriter() throws IOException {
            if (writer == null) {
                String charsetName =
                    getCharacterEncoding() != null ? getCharacterEncoding()
                        : RestConstants.CHARACTER_ENCODING_UTF_8;
                OutputStreamWriter osWriter =
                    new OutputStreamWriter(getOutputStream(), charsetName);
                writer = new PrintWriter(osWriter);
            }
            return writer;
        }

        public ServletOutputStream getOutputStream() throws IOException {
            if (countingStream == null) {
                countingStream = new CountingStream();
            }
            return countingStream;
        }

        private static final class CountingStream extends ServletOutputStream {

            private int byteCount = 0;

            /**
             * @return number of bytes written
             */
            public int getByteCount() {
                return byteCount;
            }

            public void write(int b) throws IOException {
                byteCount++;
            }

            public void write(byte b[], int off, int len) throws IOException {
                byteCount += len;
            }

        } // class CountingStream

    } // class NoBodyResponseWrapper

    public void init(Properties props) {
    }

}
