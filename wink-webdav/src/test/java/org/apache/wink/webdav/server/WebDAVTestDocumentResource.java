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

package org.apache.wink.webdav.server;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.atom.AtomConstants;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndLink;
import org.apache.wink.common.model.synd.SyndText;
import org.apache.wink.webdav.WebDAVMethod;

@Path("/feed/{entry}")
public class WebDAVTestDocumentResource extends WebDAVLockableResource {

    public static int                    ENTRY_NUMBER = 3;

    public static Map<String, SyndEntry> entries      =
                                                          new HashMap<String, SyndEntry>(
                                                                                         ENTRY_NUMBER);

    static {
        for (int i = 0; i < ENTRY_NUMBER; i++) {
            String id = "" + (i + 1);
            String title = "title" + (i + 1);
            String path = "/feed/" + (i + 1);
            SyndEntry entry = new SyndEntry(new SyndText(title), id);
            entry.setUpdated(new Date(0));
            entry.setPublished(new Date(0));
            entry.addLink(new SyndLink(AtomConstants.ATOM_REL_EDIT, null, path));
            entries.put(id, entry);
        }
    }

    @WebDAVMethod.PROPFIND
    @Consumes( {MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response findProperties(@Context UriInfo uriInfo,
                                   @PathParam("entry") String id,
                                   String body) throws IOException {
        SyndEntry entry = entries.get(id);
        if (entry != null) {
            return WebDAVResponseBuilder.create(uriInfo).propfind(entry, body);
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

}
