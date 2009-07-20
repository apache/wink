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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * HTML representation of the document. This class is an abstract class that
 * will be used to implement the HTML representation for the entry and for the
 * collection resources.
 * 
 * @param <T>
 */
@Provider
@Produces(MediaType.TEXT_HTML)
public class HtmlProvider implements MessageBodyWriter<HtmlDescriptor> {

    @Context
    protected HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse  httpServletResponse;

    /**
     * This method includes the content of the requested file (includeUrl) into
     * the response wrapper. Since we can get to the HTML representation either
     * when user requested the HTML representation for the resource or when the
     * user requested the Atom representation for the resource, we cannot write
     * the information into the original HttpServletResponse. We need to wrap
     * this response and change it to write through the output stream received
     * and not to the output stream of the HttpServletResponse.
     * 
     * @param stream the output stream to output the serialization
     * @param includeUrl
     * @throws IOException I/O problem
     * @throws IOException in case the included resource throws ServletException
     */
    private void include(OutputStream stream, String includeUrl) throws IOException {
        // create the response wrapper
        OutputStreamHttpServletResponseWrapper httpServletResponseWrapper =
            new OutputStreamHttpServletResponseWrapper(httpServletResponse, stream);
        try {
            // include the file into the response wrapper
            httpServletRequest.getRequestDispatcher(includeUrl).include(httpServletRequest,
                                                                        httpServletResponseWrapper);
        } catch (ServletException e) {
            throw new WebApplicationException(e);
        }
        // flush the result
        httpServletResponseWrapper.flushOutput();
    }

    public long getSize(HtmlDescriptor t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        return HtmlDescriptor.class.isAssignableFrom(type);
    }

    public void writeTo(HtmlDescriptor descriptor,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {

        // set the resource as attribute on the request in order to allow the
        // JSP/Servlet to use this data and present it.
        httpServletRequest.setAttribute(descriptor.getAttributeName(), descriptor.getObject());
        // include the content of the requested file (includeUrl) into the
        // response.
        include(entityStream, descriptor.getIncludeUrl());
        // after the data was written, remove the attribute from the request
        httpServletRequest.removeAttribute(descriptor.getAttributeName());
    }

}
