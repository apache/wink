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

package org.apache.wink.jaxrs.test.providers.standard;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("providers/standard/reader")
public class ReaderResource {

    private char[] carr = null;

    @GET
    public Response getReader() {
        return Response.ok(new BufferedReader(new CharArrayReader(carr))).build();
    }

    @POST
    public Reader postReader(Reader reader) {
        return reader;
    }

    @POST
    @Path("/subclasses/shouldfail")
    public BufferedReader postReader(BufferedReader br) {
        return br;
    }

    @PUT
    public void putReader(Reader is) throws IOException {
        char[] buffer = new char[1];
        int read = 0;
        int offset = 0;
        while ((read = is.read(buffer, offset, buffer.length - offset)) != -1) {
            offset += read;
            if (offset >= buffer.length) {
                buffer = ArrayUtils.copyOf(buffer, buffer.length * 2);
            }
        }
        carr = ArrayUtils.copyOf(buffer, offset);
    }
}
