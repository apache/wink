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

package org.apache.wink.itest.subresource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("guestbooksubresources")
public class GuestbookResource {
    //
    // @Path("commentdata/{commentid}")
    // @Produces(value = { "text/xml" })
    // public CommentData resolveComment() {
    // return new CommentData(aCommentId, null);
    // }

    @Path("commentdata/{commentid}")
    public Object resolveComment(@PathParam("commentid") String aCommentId) {
        return new CommentData(aCommentId);
    }

    @Path("commentdata")
    public Object resolveComment() {
        return new CommentData(null);
    }

    @GET
    @Path("somecomment")
    @Produces("text/xml")
    public Object getComment(Comment c2) {
        Comment c = new Comment();
        c.setAuthor("Anonymous");
        c.setId(10);
        c.setMessage("Hello World!");
        return c;
    }

}
