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

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

@Path("providers/standard/source")
public class SourceResource {

    private static Source source = null;

    @GET
    @Produces("text/xml")
    public Response getSource() {
        return Response.ok(source).type("text/xml").build();
    }

    @POST
    public Source postSource(Source src) {
        return src;
    }

    public static class UnsupportedSourceSubclass implements Source {

        public String getSystemId() {
            // TODO Auto-generated method stub
            return null;
        }

        public void setSystemId(String systemId) {
            // TODO Auto-generated method stub

        }

    }

    @POST
    @Path("/subclasses/shouldfail")
    public UnsupportedSourceSubclass postReader(UnsupportedSourceSubclass saxSource) {
        return saxSource;
    }

    @PUT
    public void putSource(DOMSource source) throws IOException {
        this.source = source;
    }
}
