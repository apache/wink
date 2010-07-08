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

package org.apache.wink.jaxrs.test.atom;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.common.model.atom.AtomLink;
import org.apache.wink.common.model.atom.AtomText;

public class Blog {

    private int                     id;
    private String                  title;
    private Date                    updated;
    private Map<Integer, BlogEntry> entries = new HashMap<Integer, BlogEntry>();

    @GET
    @Produces("application/atom+xml")
    public AtomFeed getBlog(@Context UriInfo uriInfo) {
        return toAtomFeed(uriInfo);
    }

    @Path("entries/{entryid}")
    public BlogEntry getEntry(@PathParam("entryid") Integer entryId) {
        BlogEntry entry = entries.get(entryId);
        if (entry == null)
            throw new WebApplicationException(404);
        return entry;
    }
    
    @POST
    @Path("entries")
    public Response postBlogEntry(@Context UriInfo uriInfo, AtomEntry blogEntry) {
        BlogEntry newEntry = new BlogEntry(this);
        Author author = new Author();
        author.setName(blogEntry.getAuthors().get(0).getName());
        author.setEmail(blogEntry.getAuthors().get(0).getEmail());
        newEntry.setAuthor(author);
        newEntry.setId(BlogEntry.getNextId());
        newEntry.setPosting(blogEntry.getContent().getValue());
        newEntry.setTitle(blogEntry.getTitle().getValue());
        newEntry.setUpdated(new Date());
        entries.put(new Integer(newEntry.getId()), newEntry);
        try {
            URI uri = new URI(uriInfo.getBaseUri() + "blogservice/blogs/"+this.id+"/entries/"+newEntry.getId());
            return Response.created(uri).build();
        } catch (URISyntaxException e) {
            throw new WebApplicationException(e);
        }
    }

    public AtomFeed toAtomFeed(UriInfo uriInfo) {
        AtomFeed feed = new AtomFeed();
        feed.setId(id+"");
        feed.setTitle(new AtomText(title));
        feed.setUpdated(updated);
        Set<Integer> ids = entries.keySet();
        List<Integer> idList = new ArrayList<Integer>(ids);
        Collections.sort(idList);
        AtomLink link = null;
        for (Integer entryId : idList) {
            link = new AtomLink();
            link.setHref(uriInfo.getBaseUri() + "blogservice/blogs/"
                + this.id
                + "/entries/"
                + entries.get(entryId).getId());
            link.setTitle(entries.get(entryId).getTitle());
            feed.getLinks().add(link);
        }
        return feed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Map<Integer, BlogEntry> getEntries() {
        return entries;
    }

    public void addEntry(BlogEntry newEntry) {
        this.entries.put(newEntry.getId(), newEntry);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
