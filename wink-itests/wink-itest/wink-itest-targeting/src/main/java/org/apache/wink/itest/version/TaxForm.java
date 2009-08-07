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

package org.apache.wink.itest.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

@Path(value = "/taxform")
public class TaxForm {

    @Context
    private HttpHeaders    httpHeaders;

    @Context
    private ServletContext context;

    @GET
    public InputStream getWithAcceptHeader(@QueryParam(value = "form") String form)
        throws FileNotFoundException {
        List<MediaType> mediaTypes = httpHeaders.getAcceptableMediaTypes();
        if (mediaTypes == null || mediaTypes.isEmpty()) {
            throw new IllegalArgumentException("Accept values not found");
        }
        MediaType mediaType = mediaTypes.get(0);
        String subtype = mediaType.getSubtype();
        String version = subtype.substring(subtype.indexOf("+") + 1);
        String formPath = form + "_" + version;
        String path =
            "tests" + java.io.File.separator
                + "test-resources"
                + java.io.File.separator
                + formPath
                + ".xml";
        InputStream stream = context.getResourceAsStream("/WEB-INF/classes/" + path);
        if (stream != null) {
            return stream;
        }
        return new FileInputStream(new File(path));
    }

    @GET
    @Path(value = "/{form}")
    public InputStream getWithQueryString(@PathParam(value = "form") String form,
                                          @QueryParam(value = "version") String version)
        throws FileNotFoundException {
        String formPath = form + "_" + version;
        String path =
            "tests" + java.io.File.separator
                + "test-resources"
                + java.io.File.separator
                + formPath
                + ".xml";
        InputStream stream = context.getResourceAsStream("/WEB-INF/classes/" + path);
        if (stream != null) {
            return stream;
        }
        return new FileInputStream(new File(path));
    }

    @GET
    @Path(value = "/{form}/{version}")
    public InputStream getWithPathInfo(@PathParam(value = "form") String form,
                                       @PathParam(value = "version") String version)
        throws FileNotFoundException {
        String formPath = form + "_" + version;
        String path =
            "tests" + java.io.File.separator
                + "test-resources"
                + java.io.File.separator
                + formPath
                + ".xml";
        InputStream stream = context.getResourceAsStream("/WEB-INF/classes/" + path);
        if (stream != null) {
            return stream;
        }
        return new FileInputStream(new File(path));
    }

}
