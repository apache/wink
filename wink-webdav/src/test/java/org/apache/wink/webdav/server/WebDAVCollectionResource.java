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

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.annotations.Workspace;
import org.apache.wink.common.model.atom.AtomConstants;
import org.apache.wink.common.model.synd.SyndFeed;
import org.apache.wink.common.model.synd.SyndLink;
import org.apache.wink.common.model.synd.SyndText;
import org.apache.wink.webdav.WebDAVHeaders;
import org.apache.wink.webdav.WebDAVMethod;
import org.apache.wink.webdav.server.WebDAVResource;
import org.apache.wink.webdav.server.WebDAVResponseBuilder;

@Workspace(workspaceTitle = "Test Service", collectionTitle = WebDAVCollectionResource.TITLE)
@Path(WebDAVCollectionResource.PATH)
public class WebDAVCollectionResource extends WebDAVResource {

    public static final String PATH  = "/feed";

    public static final String TITLE = "Test feed";

    public static SyndFeed     feed;

    static {
        feed = new SyndFeed(new SyndText(WebDAVCollectionResource.TITLE), null);
        feed.setUpdated(new Date(0));
        feed.addLink(new SyndLink(AtomConstants.ATOM_REL_EDIT, null, PATH));
        feed.getEntries().addAll(WebDAVDocumentResource.entries.values());
    }

    @WebDAVMethod.PROPFIND
    @Consumes( {MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response findProperties(@Context UriInfo uriInfo,
                                   @Context HttpHeaders headers,
                                   String body) throws IOException {
        return WebDAVResponseBuilder.create(uriInfo).propfind(feed,
                                                              body,
                                                              headers.getRequestHeaders()
                                                                  .getFirst(WebDAVHeaders.DEPTH));
    }
}
