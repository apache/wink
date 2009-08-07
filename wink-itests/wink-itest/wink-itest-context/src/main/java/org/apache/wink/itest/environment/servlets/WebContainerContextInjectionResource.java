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

package org.apache.wink.itest.environment.servlets;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("environment/webcontainer/context/")
public class WebContainerContextInjectionResource {

    @Context
    private HttpServletRequest  httpServletRequest;

    @Context
    private HttpServletResponse httpServletResponse;

    @Context
    private ServletConfig       servletConfig;

    @Context
    private ServletContext      servletContext;

    @GET
    public String getHTTPRequestPathInfo() {
        return httpServletRequest.getPathInfo();
    }

    @POST
    public String getHTTPResponse() {
        httpServletResponse.addHeader("responseheadername", "responseheadervalue");
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);

        try {
            PrintWriter pw =
                new PrintWriter(new OutputStreamWriter(httpServletResponse.getOutputStream()));
            /*
             * PrintWriter does not automatically flush so going to flush pw
             * manually. Reminder, cannot just flush HttpServletResponse
             * OutputStream either since decorated class has no idea about
             * PrintWriter.
             */
            pw.write("Hello World");
            pw.flush();
            /*
             * this should always be committed now
             */
            if (httpServletResponse.isCommitted()) {
                pw.write(" -- I was committted");
            }
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not output the servlet response.");
        }

        return "Shouldn't see me";
    }

    @GET
    @Path("servletcontext")
    public void getServletContext() throws IOException, ServletException {
        httpServletRequest.setAttribute("wink", "testing 1-2-3");
        servletContext.getRequestDispatcher("/servlets-test.jsp").include(httpServletRequest,
                                                                          httpServletResponse);
        httpServletRequest.removeAttribute("wink");

        // need to flush buffer so the response is committed
        httpServletResponse.flushBuffer();
    }

    @GET
    @Path("servletconfig")
    public String getServletConfig() {
        return servletConfig.getServletName();
    }
}
