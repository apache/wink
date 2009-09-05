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

package org.apache.wink.itest.exceptionmappers.nomapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.wink.itest.exceptionmappers.nomapper.GuestbookDatabase;

/**
 * The main JAX-RS resource.
 */
@Path("guestbooknomap")
public class Guestbook {

    private static class MyWebAppException extends WebApplicationException {

        private static final long serialVersionUID = -2022185988670037226L;

        final private Response    resp;

        public MyWebAppException(int status) {
            CommentError error = new CommentError();
            error.setErrorMessage("Cannot post an invalid message.");
            resp = Response.status(status).type("text/xml").entity(error).build();
        }

        @Override
        public Response getResponse() {
            return resp;
        }
    }

    /**
     * Adds a new message to the database.
     * 
     * @return HTTP status 200
     */
    @POST
    @Consumes( {"text/xml"})
    @Produces( {"text/xml"})
    public Response createMessage(Comment aMessage, @Context UriInfo uriInfo) {
        if (aMessage == null) {
            WebApplicationException webAppException =
                new WebApplicationException(Status.BAD_REQUEST);
            throw webAppException;
        }

        if (aMessage.getMessage() == null && aMessage.getAuthor() == null) {
            throw new WebApplicationException();
        }

        if (aMessage.getMessage() == null) {
            CommentError error = new CommentError();
            error.setErrorMessage("Missing the message in the comment.");
            Response malformedCommentResponse =
                Response.status(Status.BAD_REQUEST).entity(error).type("text/xml").build();
            WebApplicationException webAppException =
                new WebApplicationException(malformedCommentResponse);
            throw webAppException;
        }

        if (aMessage.getAuthor() == null) {
            WebApplicationException webAppException = new WebApplicationException(499);
            throw webAppException;
        }

        if ("".equals(aMessage.getMessage())) {
            throw new MyWebAppException(498);
        }

        /*
         * Set the message id to a server decided message, even if the client
         * set it.
         */
        int id = GuestbookDatabase.getGuestbook().getAndIncrementCounter();
        aMessage.setId(id);

        GuestbookDatabase.getGuestbook().storeComment(aMessage);
        try {
            return Response.created(new URI(uriInfo.getAbsolutePath() + "/" + aMessage.getId()))
                .entity(aMessage).type(MediaType.TEXT_XML).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @PUT
    @Path("{id}")
    public Response updateMessage(Comment aMessage, @PathParam("id") String msgId)
        throws GuestbookException {
        /*
         * If no message data was sent, then return the null request.
         */
        if (aMessage == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        if (aMessage.getId() == null || !aMessage.getId().equals(msgId)) {
            throw new GuestbookException("Unexpected ID.");
        }

        Comment existingComment =
            GuestbookDatabase.getGuestbook().getComment(Integer.valueOf(msgId));
        if (existingComment == null) {
            throw new GuestbookException("Cannot find existing comment to update.");
        }
        GuestbookDatabase.getGuestbook().storeComment(aMessage);
        return Response.ok(aMessage).build();
    }

    @GET
    @Path("/{id}")
    @Produces( {"text/xml"})
    public Response readMessage(@PathParam("id") String msgId) {
        Comment msg = GuestbookDatabase.getGuestbook().getComment(Integer.valueOf(msgId));
        if (msg == null) {
            return Response.status(404).build();
        }

        return Response.ok(msg).build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteMessage(@PathParam("id") String msgId) {
        GuestbookDatabase.getGuestbook().deleteComment(Integer.valueOf(msgId));
        return Response.noContent().build();
    }

    @POST
    @Path("/clear")
    public void clearMessages() {
        Collection<Integer> keys = GuestbookDatabase.getGuestbook().getCommentKeys();
        for (Integer k : keys) {
            GuestbookDatabase.getGuestbook().deleteComment(k);
        }
    }
}
