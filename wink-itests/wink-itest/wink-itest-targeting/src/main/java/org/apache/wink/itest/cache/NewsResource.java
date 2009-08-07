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

package org.apache.wink.itest.cache;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("/news")
public class NewsResource {

    private static final DateFormat         format  =
                                                        new SimpleDateFormat(
                                                                             "EEE, dd MMM yyyy HH:mm:ss zzz",
                                                                             Locale.ENGLISH);
    static {
        format.setLenient(false);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Context
    private Request                         request;

    private static Map<String, StoryRecord> stories = new HashMap<String, StoryRecord>();

    @GET
    @Produces(value = "text/xml")
    @Path(value = "/{title}")
    public Response getNewsStory(@PathParam(value = "title") String title) {
        StoryRecord record = stories.get(title);

        // if no record, return 404
        if (record == null) {
            return Response.status(404).build();
        }
        NewsStory story = record.getStory();

        EntityTag recordETag =
            EntityTag.valueOf("\"" + String.valueOf(record.getRevision().hashCode()) + "\"");

        String lastModified = story.getUpdatedDate();
        Date date = null;

        try {
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = format.parse(lastModified);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ResponseBuilder builder = null;
        if (date != null) {
            builder = request.evaluatePreconditions(date, recordETag);
        } else {
            builder = request.evaluatePreconditions(recordETag);
        }

        if (builder != null) {
            Response response = builder.build();
            response.getMetadata().putSingle("Content-Length", 0);
            return response;
        }

        // otherwise return the entity
        builder = Response.ok();
        builder.entity(story);
        return builder.build();
    }

    @POST
    public Response addNewsStory(NewsStory story) {
        String date =
            format.format(Calendar.getInstance(TimeZone.getTimeZone("America/Austin")).getTime());
        story.setPostedDate(date);
        story.setUpdatedDate(date);
        StoryRecord record = new StoryRecord(story);
        stories.put(record.getKey(), record);
        ResponseBuilder builder = Response.ok();

        // let's put an ETag in here so clients can send an 'If-Match'
        EntityTag eTag =
            EntityTag.valueOf("\"" + String.valueOf(record.getRevision().hashCode()) + "\"");
        builder.tag(eTag);
        Response resp = builder.build();
        resp.getMetadata().putSingle("Location", "/" + story.getTitle());
        resp.getMetadata().putSingle("Last-Modified", story.getUpdatedDate());
        return resp;
    }

    @PUT
    public Response updateNewsStory(NewsStory story) {
        StoryRecord record = stories.get(story.getTitle());
        if (record == null) {
            record = new StoryRecord(story);
        } else {
            record.updateStory(story);
        }
        ResponseBuilder builder = Response.ok();

        // let's put an ETag in here so clients can send an 'If-Match'
        EntityTag eTag =
            EntityTag.valueOf("\"" + String.valueOf(record.getRevision().hashCode()) + "\"");
        builder.tag(eTag);
        Response response = builder.build();
        response.getMetadata().putSingle("Last-Modified", record.getStory().getUpdatedDate());
        return response;
    }

    @POST
    @Path("/clear")
    public void clearRecords() {
        stories.clear();
    }
}
