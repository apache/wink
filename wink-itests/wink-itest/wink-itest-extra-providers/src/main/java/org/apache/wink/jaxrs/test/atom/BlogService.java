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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.common.model.atom.AtomLink;
import org.apache.wink.common.model.atom.AtomText;
import org.slf4j.LoggerFactory;

@Path("blogservice")
public class BlogService {
    public static final String ID    = "wink-blog-service";

    private static List<Blog>  blogs = new ArrayList<Blog>();

    @Context
    private UriInfo            baseUri;

    static {
        Date date = new Date();
        Blog winkDevBlog = new Blog();
        winkDevBlog.setId(0);
        winkDevBlog.setTitle("wink-developer-blog");
        winkDevBlog.setUpdated(date);
        BlogEntry entry1 = new BlogEntry(winkDevBlog);
        Author author = new Author();
        author.setName("Blog Admin");
        author.setEmail("winkblogadmin@wink.blog.com");
        entry1.setAuthor(author);
        entry1.setId(BlogEntry.getNextId());
        entry1.setPosting("Welcome to the wink developer blog!!");
        entry1.setTitle("welcomePosting");
        entry1.setUpdated(date);
        winkDevBlog.addEntry(entry1);
        BlogEntry entry2 = new BlogEntry(winkDevBlog);
        entry2.setAuthor(author);
        entry2.setId(BlogEntry.getNextId());
        entry2
            .setPosting("Wink developers,\n\nInstructions on how to set up the wink development have been posted to the wink wiki. Happy wink development!\n\nw--inkblogadmin");
        entry2.setTitle("Wink Development Env");
        entry2.setUpdated(date);
        winkDevBlog.addEntry(entry2);
        Comment comment = new Comment();
        Author author2 = new Author();
        author2.setName("Wink Coder");
        author2.setEmail("winkcoder@mybusiness.com");
        comment.setAuthor(author2);
        comment.setContent("Instructions look great! Now I can begin Wink development!");
        comment.setTitle("Great!");
        entry2.addComment(comment);
        BlogService.blogs.add(winkDevBlog);

        Blog winkUserBlog = new Blog();
        winkUserBlog.setId(1);
        winkUserBlog.setTitle("wink-user-blog");
        winkUserBlog.setUpdated(date);
        BlogEntry entry3 = new BlogEntry(winkUserBlog);
        Author author3 = new Author();
        author3.setName("Eager User");
        author3.setEmail("winkuser@wink.blog.com");
        entry3.setAuthor(author3);
        entry3.setId(BlogEntry.getNextId());
        entry3.setPosting("I hear that the 0.1 SNAPSHOT will be available soon! I can't wait!!!");
        entry3.setTitle("0.1 SNAPSHOT");
        entry3.setUpdated(date);
        winkUserBlog.addEntry(entry3);
        Comment comment2 = new Comment();
        Author author4 = new Author();
        author4.setName("Blog Reader");
        author4.setEmail("blogreader@blogreaders.org");
        comment2.setAuthor(author4);
        comment2.setContent("This is good news. I'll be sure to try it out.");
        comment2.setTitle("Good news");
        entry3.addComment(comment2);
        BlogService.blogs.add(winkUserBlog);
    }

    @GET
    @Produces("application/atom+xml")
    public AtomFeed getBlogs() {
        AtomFeed ret = new AtomFeed();
        ret.setId(BlogService.ID);
        ret.setTitle(new AtomText(BlogService.ID));
        AtomLink link = null;
        for(int i = 0; i < BlogService.blogs.size(); ++i) {
            link = new AtomLink();
            link.setHref(baseUri.getAbsolutePath() + "/blogs/" + i);
            link.setTitle(BlogService.blogs.get(i).getTitle());
            ret.getLinks().add(link);
        }
        return ret;
    }

    @Path("blogs/{blogid}")
    public Blog getBlog(@PathParam("blogid") int blogId) {
        Blog blog = BlogService.blogs.get(blogId);
        LoggerFactory.getLogger(BlogService.class).info(blog.getTitle());
        if (blog == null)
            throw new WebApplicationException(404);
        return blog;
    }
}
