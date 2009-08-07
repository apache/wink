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
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.atom.AtomContent;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomLink;
import org.apache.wink.common.model.atom.AtomText;

public class BlogEntry {

    private static int    nextId   = 0;

    private Blog          parent;
    private String        posting;
    private int           id;
    private String        title;
    private Date          updated;
    private Author        author;
    private List<Comment> comments = new ArrayList<Comment>();
    
    public BlogEntry(Blog parent) {
        this.parent = parent;
    }

    @GET
    @Produces("application/atom+xml")
    public AtomEntry getEntry(@Context UriInfo uriInfo) {
        return toAtomEntry(uriInfo);
    }
    
    @PUT
    @Produces("application/atom+xml")
    public AtomEntry updateBlogEntry(@Context UriInfo uriInfo, AtomEntry updatedEntry) {
        Author author = getAuthor();
        author.setName(updatedEntry.getAuthors().get(0).getName());
        author.setEmail(updatedEntry.getAuthors().get(0).getEmail());
        setPosting(updatedEntry.getContent().getValue());
        setTitle(updatedEntry.getTitle().getValue());
        setUpdated(new Date());
        return toAtomEntry(uriInfo);
    }

    @Path("comments/{commentid}")
    public Comment getComments(@PathParam("commentid") Integer commentId) {
        Comment comment = this.comments.get(commentId);
        if (comment == null)
            throw new WebApplicationException(404);
        return comment;
    }
    
    @POST
    @Path("comments")
    public Response postComment(@Context UriInfo uriInfo, AtomEntry comment) {
        Comment newComment = new Comment();
        Author author = new Author();
        author.setName(comment.getAuthors().get(0).getName());
        author.setEmail(comment.getAuthors().get(0).getEmail());
        newComment.setAuthor(author);
        newComment.setTitle(comment.getTitle().getValue());
        newComment.setContent(comment.getContent().getValue());
        comments.add(newComment);
        try {
            URI uri = new URI(uriInfo.getBaseUri() + "blogservice/blogs/"+this.parent.getId()+"/entries/"+this.id+"/comments/"+(comments.size()-1));
            return Response.created(uri).build();
        } catch (URISyntaxException e) {
            throw new WebApplicationException(e);
        }
    }

    public AtomEntry toAtomEntry(UriInfo uriInfo) {
        AtomEntry entry = new AtomEntry();
        entry.setId("" + this.id);
        entry.setTitle(new AtomText(this.title));
        entry.setUpdated(this.updated);
        entry.getAuthors().add(author.toAtomPerson());
        AtomContent content = new AtomContent();
        content.setType("text");
        content.setValue(this.posting);
        entry.setContent(content);
        AtomLink link = null;
        int i = 0;
        for (Comment comment : comments) {
            link = new AtomLink();
            link.setHref(uriInfo.getBaseUri() + "blogservice/blogs/"
                + parent.getId()
                + "/entries/"
                + this.id
                + "/comments/"
                + i);
            entry.getLinks().add(link);
            ++i;
        }
        return entry;
    }

    public static int getNextId() {
        return BlogEntry.nextId++;
    }

    public String getTitle() {
        return this.title;
    }

    public String getPosting() {
        return posting;
    }

    public void setPosting(String posting) {
        this.posting = posting;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
