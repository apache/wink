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

package org.apache.wink.server.internal.providers.entity.html;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.wink.common.internal.i18n.Messages;

/**
 * This class represents the response wrapper that will wrap the
 * HttpServletResponse. We need to wrap the HttpServletResponse because in HTML
 * representation we want to write to a provided output stream and not
 * necessarily to the output stream of the HttpServletResponse (happens in Atom
 * case). This class contains its own ServletOutputStream and PrintWriter and
 * override the methods getOutputStream() and getWriter() of
 * HttpServletResponseWrapper.
 */
class OutputStreamHttpServletResponseWrapper extends HttpServletResponseWrapper {
    ServletOutputStreamWrapper servletOutputStreamWrapper = null;
    PrintWriter                printWriter                = null;
    // indicates if OutputStream was requested
    boolean                    isOutputStream             = false;

    /**
     * This constructor sets the response in the super class and creates the
     * output stream wrapper.
     * 
     * @param response the HTTP response
     * @param outputStream the output stream to output the serialization
     */
    public OutputStreamHttpServletResponseWrapper(HttpServletResponse response,
                                                  OutputStream outputStream) {
        super(response);
        this.servletOutputStreamWrapper = new ServletOutputStreamWrapper(outputStream);
    }

    /**
     * This method will override the super implementation in order to return its
     * own ServletOutputStream.
     * 
     * @return the servlet output stream to output the serialization
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        // if printer writer was already requested than output stream cannot be
        // retrieved.
        if (printWriter != null) {
            throw new IllegalStateException(Messages
                .getMessage("cannotGetOutputStreamSinceWriterRequested")); //$NON-NLS-1$
        }
        // indicates that output stream was requested.
        isOutputStream = true;

        return servletOutputStreamWrapper;
    }

    /**
     * This method will override the super implementation in order to return the
     * PrintWriter.
     * 
     * @return the print writer to write the serialization
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        // if output stream was already requested than print writer cannot be
        // retrieved.
        if (isOutputStream) {
            throw new IllegalStateException(Messages
                .getMessage("writerCannotGetWriterSinceOutputStreamRequested")); //$NON-NLS-1$
        }

        // gets the printWriter if exists
        if (printWriter != null) {
            return printWriter;
        }

        // creates the printWriter over the servletOutputStreamWrapper
        OutputStreamWriter outputStreamWriter =
            new OutputStreamWriter(servletOutputStreamWrapper, getCharacterEncoding());
        printWriter = new PrintWriter(outputStreamWriter, true);

        return printWriter;
    }

    /**
     * This method will flush the data that was written through the
     * ServletOutputStream or the PrintWriter.
     * 
     * @throws IOException I/O problem
     */
    void flushOutput() throws IOException {
        if (isOutputStream && servletOutputStreamWrapper != null) {
            servletOutputStreamWrapper.flush();
        } else if (printWriter != null) {
            printWriter.flush();
        }

    }

    /**
     * This class is responsible to wrap the OutputStream received into
     * ServletOutputStream. This class is needed by the response wrapper in
     * order to return ServletOutputStream when the getOutputStream() method is
     * called.
     */
    public class ServletOutputStreamWrapper extends ServletOutputStream {
        OutputStream outputStream;

        /**
         * This constructor operates the super constructor and sets the output
         * stream received.
         * 
         * @param outputStream the output stream to wrap
         */
        public ServletOutputStreamWrapper(OutputStream outputStream) {
            super();
            this.outputStream = outputStream;
        }

        /**
         * This method will implement the super method, writes int value into
         * the output stream.
         * 
         * @param value the value to write
         */
        @Override
        public void write(int value) throws IOException {
            outputStream.write(value);

        }
    }
}
