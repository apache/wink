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

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;

import org.apache.wink.common.model.atom.AtomContent;
import org.apache.wink.common.model.atom.AtomEntry; /*
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

import org.apache.wink.common.model.atom.AtomText;

public class Comment {

    private String content;
    private Author author;
    private String title;

    @GET
    @Produces("application/atom+xml")
    public AtomEntry getComment() {
        return toAtomEntry();
    }
    
    @PUT
    @Produces("application/atom+xml")
    public AtomEntry updateComment(AtomEntry comment) {
        Author author = getAuthor();
        author.setName(comment.getAuthors().get(0).getName());
        author.setEmail(comment.getAuthors().get(0).getEmail());
        setTitle(comment.getTitle().getValue());
        setContent(comment.getContent().getValue());
        return toAtomEntry();
    }
    
    public AtomEntry toAtomEntry() {
        AtomEntry entry = new AtomEntry();
        AtomContent content = new AtomContent();
        content.setValue(this.content);
        content.setType("text");
        entry.setContent(content);
        entry.getAuthors().add(this.author.toAtomPerson());
        entry.setTitle(new AtomText(this.title));
        return entry;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
