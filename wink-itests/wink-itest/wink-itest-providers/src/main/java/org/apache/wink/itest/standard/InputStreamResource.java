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

package org.apache.wink.itest.standard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("providers/standard/inputstream")
public class InputStreamResource {

    private byte[] barr = null;

    @GET
    public Response getInputStream() {
        return Response.ok(new ByteArrayInputStream(barr)).build();
    }

    @POST
    public InputStream postInputStream(InputStream is) {
        return is;
    }

    @POST
    @Path("/subclasses/shouldfail")
    public ByteArrayInputStream postInputStream(ByteArrayInputStream is) {
        return is;
    }

    @PUT
    public void putInputStream(InputStream is) throws IOException {
        byte[] buffer = new byte[(is.available() <= 0) ? 1 : is.available()];
        int read = 0;
        int offset = 0;
        while ((read = is.read(buffer, offset, buffer.length - offset)) != -1) {
            offset += read;
            if (offset >= buffer.length) {
                buffer = ArrayUtils.copyOf(buffer, buffer.length * 2);
            }
        }
        barr = ArrayUtils.copyOf(buffer, offset);
    }

    @POST
    @Path("/empty")
    public Response postEmptyInputStream(InputStream is) throws IOException {
        if (is != null && is.read() == -1) {
            return Response.ok("expected").build();
        }
        return Response.serverError().build();
    }

}
