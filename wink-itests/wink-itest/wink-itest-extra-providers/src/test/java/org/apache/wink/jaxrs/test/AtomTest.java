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

package org.apache.wink.jaxrs.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.common.model.atom.AtomContent;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.common.model.atom.AtomLink;
import org.apache.wink.common.model.atom.AtomPerson;
import org.apache.wink.common.model.atom.AtomText;
import org.apache.wink.jaxrs.test.atom.BlogService;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class AtomTest extends TestCase {

    private static String BASE_URI =
                                       ServerEnvironmentInfo.getBaseURI() + "/optionalproviders/blogservice";

    static {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            BASE_URI = ServerEnvironmentInfo.getBaseURI() + "/blogservice";
        }
    }

    public void testAtomGETBlogs() throws Exception {
        RestClient client = new RestClient();
        Resource resource = client.resource(BASE_URI);
        AtomFeed feed = resource.accept("application/atom+xml").get(AtomFeed.class);
        assertEquals(BlogService.ID, feed.getId());
        assertEquals(BlogService.ID, feed.getTitle().getValue());

        List<AtomLink> expectedLinks = new ArrayList<AtomLink>();
        AtomLink link = new AtomLink();
        link.setHref(BASE_URI + "/blogs/0");
        expectedLinks.add(link);
        link = new AtomLink();
        link.setHref(BASE_URI + "/blogs/1");
        expectedLinks.add(link);
        List<AtomLink> actual = feed.getLinks();
        assertEquals(expectedLinks.size(), actual.size());
        for (int i = 0; i < actual.size(); ++i)
            assertEquals(expectedLinks.get(i).getHref(), actual.get(i).getHref());
    }

    public void testAtomGETBlog() throws Exception {
        RestClient client = new RestClient();
        Resource resource = client.resource(BASE_URI + "/blogs/0");
        AtomFeed feed = resource.accept("application/atom+xml").get(AtomFeed.class);
        assertEquals("0", feed.getId());
        assertEquals("wink-developer-blog", feed.getTitle().getValue());

        List<AtomLink> expectedLinks = new ArrayList<AtomLink>();
        AtomLink link = new AtomLink();
        link.setHref(BASE_URI + "/blogs/0/entries/0");
        expectedLinks.add(link);
        link = new AtomLink();
        link.setHref(BASE_URI + "/blogs/0/entries/1");
        expectedLinks.add(link);
        List<AtomLink> actual = feed.getLinks();
        assertEquals(expectedLinks.size(), actual.size());
        for (int i = 0; i < actual.size(); ++i)
            assertEquals(expectedLinks.get(i).getHref(), actual.get(i).getHref());

        client = new RestClient();
        resource = client.resource(BASE_URI + "/blogs/1");
        feed = resource.accept("application/atom+xml").get(AtomFeed.class);
        assertEquals("1", feed.getId());
        assertEquals("wink-user-blog", feed.getTitle().getValue());

        expectedLinks = new ArrayList<AtomLink>();
        link = new AtomLink();
        link.setHref(BASE_URI + "/blogs/1/entries/2");
        expectedLinks.add(link);
        actual = feed.getLinks();
        assertEquals(expectedLinks.size(), actual.size());
        for (int i = 0; i < actual.size(); ++i)
            assertEquals(expectedLinks.get(i).getHref(), actual.get(i).getHref());
    }

    public void testAtomGETBlogEntry() throws Exception {
        RestClient client = new RestClient();
        Resource resource = client.resource(BASE_URI + "/blogs/0/entries/0");
        AtomEntry entry = resource.accept("application/atom+xml").get(AtomEntry.class);
        assertNotNull(entry.getAuthors());
        assertEquals(1, entry.getAuthors().size());
        AtomPerson author = entry.getAuthors().get(0);
        assertNotNull(author);
        assertEquals("Blog Admin", author.getName());
        assertEquals("winkblogadmin@wink.blog.com", author.getEmail());
        assertEquals("0", entry.getId());
        assertEquals("Welcome to the wink developer blog!!", entry.getContent().getValue());
        assertEquals("welcomePosting", entry.getTitle().getValue());
        assertEquals(0, entry.getLinks().size());

        resource = client.resource(BASE_URI + "/blogs/0/entries/1");
        entry = resource.accept("application/atom+xml").get(AtomEntry.class);
        assertNotNull(entry.getAuthors());
        assertEquals(1, entry.getAuthors().size());
        author = entry.getAuthors().get(0);
        assertNotNull(author);
        assertEquals("Blog Admin", author.getName());
        assertEquals("winkblogadmin@wink.blog.com", author.getEmail());
        assertEquals("1", entry.getId());
        assertEquals("Wink developers,\n\nInstructions on how to set up the wink development have been posted to the wink wiki. Happy wink development!\n\nw--inkblogadmin",
                     entry.getContent().getValue());
        assertEquals("Wink Development Env", entry.getTitle().getValue());
        assertEquals(1, entry.getLinks().size());
        List<AtomLink> comments = entry.getLinks();
        assertEquals(BASE_URI + "/blogs/0/entries/1/comments/0", comments.get(0).getHref());

        resource = client.resource(BASE_URI + "/blogs/1/entries/2");
        entry = resource.accept("application/atom+xml").get(AtomEntry.class);
        assertNotNull(entry.getAuthors());
        assertEquals(1, entry.getAuthors().size());
        author = entry.getAuthors().get(0);
        assertNotNull(author);
        assertEquals("Eager User", author.getName());
        assertEquals("winkuser@wink.blog.com", author.getEmail());
        assertEquals("2", entry.getId());
        assertEquals("I hear that the 0.1 SNAPSHOT will be available soon! I can't wait!!!", entry
            .getContent().getValue());
        assertEquals("0.1 SNAPSHOT", entry.getTitle().getValue());
        comments = entry.getLinks();
        assertEquals(1, entry.getLinks().size());
        assertEquals(BASE_URI + "/blogs/1/entries/2/comments/0", comments.get(0).getHref());
    }

    public void testAtomGETBlogComments() throws Exception {
        RestClient client = new RestClient();
        Resource resource = client.resource(BASE_URI + "/blogs/0/entries/1/comments/0");
        AtomEntry entry = resource.accept("application/atom+xml").get(AtomEntry.class);
        assertNotNull(entry.getAuthors());
        assertEquals(1, entry.getAuthors().size());
        AtomPerson author = entry.getAuthors().get(0);
        assertNotNull(author);
        assertEquals("Wink Coder", author.getName());
        assertEquals("winkcoder@mybusiness.com", author.getEmail());
        assertEquals("Great!", entry.getTitle().getValue());
        assertEquals("Instructions look great! Now I can begin Wink development!", entry
            .getContent().getValue());

        resource = client.resource(BASE_URI + "/blogs/1/entries/2/comments/0");
        entry = resource.accept("application/atom+xml").get(AtomEntry.class);
        assertNotNull(entry.getAuthors());
        assertEquals(1, entry.getAuthors().size());
        author = entry.getAuthors().get(0);
        assertNotNull(author);
        assertEquals("Blog Reader", author.getName());
        assertEquals("blogreader@blogreaders.org", author.getEmail());
        assertEquals("Good news", entry.getTitle().getValue());
        assertEquals("This is good news. I'll be sure to try it out.", entry.getContent()
            .getValue());
    }

    public void testAtomPOSTBlogEntry() throws Exception {
        RestClient client = new RestClient();
        Resource resource = client.resource(BASE_URI + "/blogs/0/entries");
        AtomEntry entry = new AtomEntry();
        AtomPerson author = new AtomPerson();
        author.setName("Blog Admin");
        author.setEmail("winkblogadmin@wink.blog.com");
        entry.getAuthors().add(author);
        AtomContent content = new AtomContent();
        content.setType("String");
        content.setValue("This is a new entry in the blog");
        entry.setContent(content);
        entry.setTitle(new AtomText(("New blog entry")));

        ClientResponse uri =
            resource.accept("application/atom+xml").contentType("application/atom+xml").post(entry);
        String location = uri.getHeaders().getFirst("Location");
        assertEquals(BASE_URI + "/blogs/0/entries/3", location);

        resource = client.resource(location);
        AtomEntry postedEntry = resource.accept("application/atom+xml").get(AtomEntry.class);
        author = postedEntry.getAuthors().get(0);
        assertNotNull(author);
        assertEquals("Blog Admin", author.getName());
        assertEquals("winkblogadmin@wink.blog.com", author.getEmail());
        assertEquals("3", postedEntry.getId());
        assertEquals("This is a new entry in the blog", entry.getContent().getValue());
        assertEquals("New blog entry", entry.getTitle().getValue());
        assertEquals(0, entry.getLinks().size());

        resource = client.resource(BASE_URI + "/blogs/0");
        AtomFeed feed = resource.accept("application/atom+xml").get(AtomFeed.class);
        List<AtomLink> expectedLinks = new ArrayList<AtomLink>();
        AtomLink link = new AtomLink();
        link.setHref(BASE_URI + "/blogs/0/entries/0");
        expectedLinks.add(link);
        link = new AtomLink();
        link.setHref(BASE_URI + "/blogs/0/entries/1");
        expectedLinks.add(link);
        link = new AtomLink();
        link.setHref(BASE_URI + "/blogs/0/entries/3");
        expectedLinks.add(link);
        List<AtomLink> actual = feed.getLinks();
        assertEquals(expectedLinks.size(), actual.size());
        for (int i = 0; i < actual.size(); ++i)
            assertEquals(expectedLinks.get(i).getHref(), actual.get(i).getHref());
    }

    public void testAtomPostBlogComment() throws Exception {
        RestClient client = new RestClient();
        Resource resource = client.resource(BASE_URI + "/blogs/0/entries/1/comments");
        AtomEntry entry = new AtomEntry();
        AtomPerson author = new AtomPerson();
        author.setName("Wink Coder");
        author.setEmail("winkcoder@mybusiness.com");
        entry.getAuthors().add(author);
        AtomContent content = new AtomContent();
        content.setType("String");
        content.setValue("I was able to set up the Wink development environment!");
        entry.setContent(content);
        entry.setTitle(new AtomText(("Success")));

        ClientResponse uri =
            resource.accept("application/atom+xml").contentType("application/atom+xml").post(entry);
        String location = uri.getHeaders().getFirst("Location");
        assertEquals(BASE_URI + "/blogs/0/entries/1/comments/1", location);

        resource = client.resource(location);
        AtomEntry postedEntry = resource.accept("application/atom+xml").get(AtomEntry.class);
        author = postedEntry.getAuthors().get(0);
        assertNotNull(author);
        assertEquals("Wink Coder", author.getName());
        assertEquals("winkcoder@mybusiness.com", author.getEmail());
        assertEquals("I was able to set up the Wink development environment!", entry.getContent()
            .getValue());
        assertEquals("Success", entry.getTitle().getValue());

        resource = client.resource(BASE_URI + "/blogs/0/entries/1");
        entry = resource.accept("application/atom+xml").get(AtomEntry.class);
        List<AtomLink> expectedLinks = new ArrayList<AtomLink>();
        AtomLink link = new AtomLink();
        link.setHref(BASE_URI + "/blogs/0/entries/1/comments/0");
        expectedLinks.add(link);
        link = new AtomLink();
        link.setHref(BASE_URI + "/blogs/0/entries/1/comments/1");
        expectedLinks.add(link);
        List<AtomLink> actual = entry.getLinks();
        assertEquals(expectedLinks.size(), actual.size());
        for (int i = 0; i < actual.size(); ++i)
            assertEquals(expectedLinks.get(i).getHref(), actual.get(i).getHref());
    }

    public void testAtomPUTBlogEntry() throws Exception {
        RestClient client = new RestClient();
        Resource resource = client.resource(BASE_URI + "/blogs/0/entries/0");
        AtomEntry entry = resource.accept("application/atom+xml").get(AtomEntry.class);
        AtomPerson author = entry.getAuthors().get(0);
        author.setName(author.getName() + "Updated");
        author.setEmail(author.getEmail() + "Updated");
        AtomText title = entry.getTitle();
        title.setValue(title.getValue() + "Updated");
        AtomContent content = entry.getContent();
        content.setValue(content.getValue() + "Updated");

        resource = client.resource(BASE_URI + "/blogs/0/entries/0");
        AtomEntry updated =
            resource.accept("application/atom+xml").contentType("application/atom+xml")
                .put(AtomEntry.class, entry);
        assertNotNull(updated.getAuthors());
        assertEquals(1, updated.getAuthors().size());
        author = updated.getAuthors().get(0);
        assertNotNull(author);
        assertEquals("Blog AdminUpdated", author.getName());
        assertEquals("winkblogadmin@wink.blog.comUpdated", author.getEmail());
        assertEquals("0", updated.getId());
        assertEquals("Welcome to the wink developer blog!!Updated", updated.getContent().getValue());
        assertEquals("welcomePostingUpdated", updated.getTitle().getValue());
    }

    public void testAtomPUTBlogComment() throws Exception {
        RestClient client = new RestClient();
        Resource resource = client.resource(BASE_URI + "/blogs/0/entries/1/comments/0");
        AtomEntry entry = resource.accept("application/atom+xml").get(AtomEntry.class);
        AtomPerson author = entry.getAuthors().get(0);
        author.setName(author.getName() + "Updated");
        author.setEmail(author.getEmail() + "Updated");
        AtomText title = entry.getTitle();
        title.setValue(title.getValue() + "Updated");
        AtomContent content = entry.getContent();
        content.setValue(content.getValue() + "Updated");

        resource = client.resource(BASE_URI + "/blogs/0/entries/1/comments/0");
        AtomEntry updated =
            resource.accept("application/atom+xml").contentType("application/atom+xml")
                .put(AtomEntry.class, entry);
        assertNotNull(updated.getAuthors());
        assertEquals(1, updated.getAuthors().size());
        author = updated.getAuthors().get(0);
        assertNotNull(author);
        assertNotNull(author);
        assertEquals("Wink CoderUpdated", author.getName());
        assertEquals("winkcoder@mybusiness.comUpdated", author.getEmail());
        assertEquals("Great!Updated", entry.getTitle().getValue());
        assertEquals("Instructions look great! Now I can begin Wink development!Updated", entry
            .getContent().getValue());
    }
}
