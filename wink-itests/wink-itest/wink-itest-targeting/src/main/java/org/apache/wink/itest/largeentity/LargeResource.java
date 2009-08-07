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

package org.apache.wink.itest.largeentity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/large")
public class LargeResource {

    @POST
    public Response appendStrings(byte[] input) throws UnsupportedEncodingException {
        final int maxHeaderLength = 100;
        int headerLength = (input.length < maxHeaderLength) ? input.length : maxHeaderLength;
        byte[] headerBytes = new byte[headerLength];
        for (int c = 0; c < headerLength; ++c) {
            headerBytes[c] = input[c];
        }

        StringBuffer sb = new StringBuffer();
        for (int c = 0; c < 50; ++c) {
            sb.append("abcdefghijklmnopqrstuvwxyz");
        }
        // String headerValue = new String(headerBytes, "UTF-8");

        /*
         * use only 2048 characters in header because of Jetty configuration
         * which has a buffer limit of only 4096. give some room for other
         * possible headers
         */
        return Response.status(277).entity(input).header("appendStringsHeader", sb).build();
    }

    @POST
    @Path("zip")
    public Response findFirstEntry(JarInputStream jarInputStream) throws IOException {
        if (jarInputStream == null) {
            return Response.status(Status.BAD_REQUEST).entity("no jar").build();
        }
        JarEntry je = null;
        List<String> l = new ArrayList<String>();
        while ((je = jarInputStream.getNextJarEntry()) != null) {
            l.add(je.getName());
        }
        Collections.sort(l);
        if (l.size() > 0) {
            return Response.status(290).entity(l.get(0)).build();
        }
        return Response.status(Status.NO_CONTENT).build();
    }
}
