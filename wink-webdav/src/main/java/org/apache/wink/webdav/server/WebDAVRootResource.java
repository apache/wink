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
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.http.OPTIONS;
import org.apache.wink.common.model.atom.AtomConstants;
import org.apache.wink.common.model.synd.SyndFeed;
import org.apache.wink.common.model.synd.SyndLink;
import org.apache.wink.common.model.synd.SyndText;
import org.apache.wink.server.internal.resources.HtmlServiceDocumentResource;
import org.apache.wink.server.internal.resources.ServiceDocumentCollectionData;
import org.apache.wink.webdav.WebDAVHeaders;
import org.apache.wink.webdav.WebDAVMethod;

@Path("/")
public class WebDAVRootResource extends HtmlServiceDocumentResource {

    @Context
    private UriInfo uriInfo;

    @OPTIONS
    public Response getOptions() {
        return WebDAVUtils.getOptions(uriInfo);
    }

    @WebDAVMethod.PROPFIND
    @Consumes( {MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response propfind(@Context HttpHeaders headers, String body) throws IOException {

        // create a RootResourceCollectionPropertyProvider that can get the sub
        // collection
        WebDAVResponseBuilder.CollectionPropertyHandler provider =
            new RootResourceCollectionPropertyProvider();

        // call the propfind response builder
        return WebDAVResponseBuilder.create(uriInfo).propfind(getSyndFeed(uriInfo.getPath(false)),
                                                              body,
                                                              headers.getRequestHeaders()
                                                                  .getFirst(WebDAVHeaders.DEPTH),
                                                              provider);
    }

    protected SyndFeed getSyndFeed(String path) {
        SyndFeed feed = new SyndFeed();
        feed.setTitle(new SyndText(""));
        feed.addLink(new SyndLink(AtomConstants.ATOM_REL_EDIT, null, path));
        return feed;
    }

    private class RootResourceCollectionPropertyProvider extends
        WebDAVResponseBuilder.CollectionPropertyHandler {

        @Override
        public List<SyndFeed> getSubCollections(WebDAVResponseBuilder builder, SyndFeed feed) {
            List<SyndFeed> collectionsList = new ArrayList<SyndFeed>();
            for (ServiceDocumentCollectionData subCollection : WebDAVRootResource.this
                .getCollections(uriInfo)) {
                // only collection without template URI
                if (!isTemplateUri(subCollection.getUri())) {
                    SyndFeed subFeed = new SyndFeed();
                    subFeed.setTitle(new SyndText(subCollection.getTitle()));
                    subFeed.addLink(new SyndLink(AtomConstants.ATOM_REL_EDIT, null, subCollection
                        .getUri()));
                    collectionsList.add(subFeed);
                }
            }
            return collectionsList;
        }

        private boolean isTemplateUri(String collectionUri) {
            return collectionUri.indexOf('{') > 0;
        }
    }

}
