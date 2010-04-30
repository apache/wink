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

package org.apache.wink.itest.request;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * A resource for testing the {@link Request} interface. This is treated like a
 * singleton in the application.
 */
@Path("context/request")
public class RequestResource {

    private Date                          date;

    private EntityTag                     etag;

    final private static SimpleDateFormat rfc1123Format =
                                                            new SimpleDateFormat(
                                                                                 "EEE, dd MMM yyyy HH:mm:ss zzz",
                                                                                 Locale.ENGLISH);
    
    @GET
    @Path("timezone")
    public String getTimeZone() {
        boolean dst = TimeZone.getDefault().inDaylightTime(new Date());
        return TimeZone.getDefault().getDisplayName(dst, TimeZone.SHORT);
    }

    @GET
    @Path("date")
    public Response evalDate(@Context Request req) {
        if (!"GET".equals(req.getMethod())) {
            throw new WebApplicationException();
        }
        if (date == null) {
            return Response.serverError().build();
        }
        System.out.println("GET Date: " + date);
        ResponseBuilder respBuilder = req.evaluatePreconditions(date);
        if (respBuilder != null) {
            System.out.println("Returning 304");
            return respBuilder.build();
        }
        System.out.println("Returning 200");
        SimpleDateFormat rfc1123Format =
            new SimpleDateFormat(
                                 "EEE, dd MMM yyyy HH:mm:ss zzz",
                                 Locale.ENGLISH);
        rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return Response.ok("the date: " + rfc1123Format.format(date)).lastModified(date).build();
    }

    @PUT
    @Path("date")
    public Response putDate(String dateSource, @Context Request req) {
        if (!"PUT".equals(req.getMethod())) {
            throw new WebApplicationException();
        }
        try {
            date = new SimpleDateFormat(
                                                   "EEE, dd MMM yyyy HH:mm:ss zzz",
                                                   Locale.ENGLISH).parse(dateSource);
            System.out.println("PUT Date: " + date);
        } catch (ParseException e) {
            throw new WebApplicationException(e);
        }
        return Response.noContent().build();
    }

    @GET
    @Path("etag")
    public Response evalEtag(@Context Request req) {
        if (!"GET".equals(req.getMethod())) {
            throw new WebApplicationException();
        }
        if (etag == null) {
            return Response.serverError().build();
        }
        ResponseBuilder respBuilder = req.evaluatePreconditions(etag);
        if (respBuilder != null) {
            return respBuilder.build();
        }
        return Response.ok("the etag: \"" + etag.getValue() + "\"" + etag.isWeak()).tag(etag)
            .build();
    }

    @POST
    @Path("etag")
    public Response evalPostEtag(@Context Request req) {
        if (!"POST".equals(req.getMethod())) {
            throw new WebApplicationException();
        }
        if (etag == null) {
            return Response.serverError().build();
        }
        ResponseBuilder respBuilder = req.evaluatePreconditions(etag);
        if (respBuilder != null) {
            return respBuilder.build();
        }
        return Response.ok("the etag: \"" + etag.getValue() + "\"" + etag.isWeak()).tag(etag)
            .build();
    }

    @PUT
    @Path("etag")
    public Response putEtag(@Context Request req, String etag) {
        if (!"PUT".equals(req.getMethod())) {
            throw new WebApplicationException();
        }
        this.etag = EntityTag.valueOf(etag);
        return Response.noContent().build();
    }

    @GET
    @Path("variant/acceptonly")
    public Response evalAcceptVariant(@Context Request req) {
        if (!"GET".equals(req.getMethod())) {
            throw new WebApplicationException();
        }
        List<Variant> variants =
            Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE,
                               MediaType.valueOf("text/*"),
                               MediaType.TEXT_XML_TYPE,
                               MediaType.TEXT_HTML_TYPE).add().build();
        Variant targettedVariant = req.selectVariant(variants);
        if (targettedVariant == null) {
            return Response.status(466).build();
        }

        if ("xml".equals(targettedVariant.getMediaType().getSubtype())) {
            org.apache.wink.itest.request.Variant v =
                new org.apache.wink.itest.request.Variant();
            v.setMediatype(targettedVariant.getMediaType().getType() + "/"
                + targettedVariant.getMediaType().getSubtype());
            v.setEncoding(targettedVariant.getEncoding());
            v.setLanguage((targettedVariant.getLanguage() != null) ? targettedVariant.getLanguage()
                .getLanguage() : null);
            return Response.ok(v).build();
        }

        return Response.ok(targettedVariant.getMediaType().getType() + "/"
            + targettedVariant.getMediaType().getSubtype()).build();
    }

    @GET
    @Path("variant/acceptlanguageonly")
    public Response evalAcceptLanguageVariant(@Context Request req) {
        if (!"GET".equals(req.getMethod())) {
            throw new WebApplicationException();
        }
        List<Variant> variants =
            Variant.languages(Locale.ENGLISH, Locale.JAPANESE, Locale.CHINESE, Locale.GERMAN).add()
                .build();
        Variant targettedVariant = req.selectVariant(variants);
        if (targettedVariant == null) {
            return Response.status(466).build();
        }

        return Response.ok(targettedVariant.getLanguage().getLanguage()).build();
    }

    @GET
    @Path("variant/acceptencodingonly")
    public Response evalAcceptEncodingVariant(@Context Request req) {
        if (!"GET".equals(req.getMethod())) {
            throw new WebApplicationException();
        }
        List<Variant> variants = Variant.encodings("compress", "gzip", "identity").add().build();
        Variant targettedVariant = req.selectVariant(variants);
        if (targettedVariant == null) {
            return Response.status(466).build();
        }

        return Response.ok(targettedVariant.getEncoding()).build();
    }

    @GET
    @Path("variant/responsebuilder")
    public Response evalResponseBuilderVary(@Context Request req, @QueryParam("type") String type) {
        if (!"GET".equals(req.getMethod())) {
            throw new WebApplicationException();
        }
        List<Variant> variants =
            Variant.languages(Locale.ENGLISH, Locale.JAPANESE, Locale.CHINESE, Locale.GERMAN)
                .mediaTypes(MediaType.APPLICATION_JSON_TYPE,
                            MediaType.valueOf("text/*"),
                            MediaType.TEXT_XML_TYPE,
                            MediaType.TEXT_HTML_TYPE).encodings("compress", "gzip", "identity")
                .add().build();
        Variant targettedVariant = req.selectVariant(variants);
        if ("notacceptable".equals(type)) {
            List<Variant> notAcceptableVariants =
                Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE,
                                   MediaType.valueOf("text/*"),
                                   MediaType.TEXT_XML_TYPE,
                                   MediaType.TEXT_HTML_TYPE).encodings("compress",
                                                                       "gzip",
                                                                       "identity").add().build();
            return Response.notAcceptable(notAcceptableVariants).build();
        } else if ("variants".equals(type)) {
            List<Variant> okVariants =
                Variant.languages(Locale.ENGLISH, Locale.JAPANESE, Locale.CHINESE, Locale.GERMAN)
                    .encodings("compress", "gzip", "identity").add().build();
            return Response.ok().variants(okVariants).build();
        }
        if (targettedVariant == null) {
            return Response.status(466).build();
        }
        return Response.ok(targettedVariant.getMediaType().getType() + "/"
                               + targettedVariant.getMediaType().getSubtype()
                               + "-"
                               + ((targettedVariant.getLanguage() != null) ? targettedVariant
                                   .getLanguage().getLanguage() : "")
                               + "-"
                               + targettedVariant.getEncoding(),
                           new Variant(new MediaType("text", "plain"), Locale.ENGLISH, "identity"))
            .build();
    }

    @GET
    @Path("variant/acceptmultiple")
    public Response evalAcceptMultipleVariant(@Context Request req) {
        if (!"GET".equals(req.getMethod())) {
            throw new WebApplicationException();
        }
        List<Variant> variants =
            Variant.languages(Locale.ENGLISH, Locale.JAPANESE, Locale.CHINESE, Locale.GERMAN)
                .mediaTypes(MediaType.APPLICATION_JSON_TYPE,
                            MediaType.valueOf("text/*"),
                            MediaType.TEXT_XML_TYPE,
                            MediaType.TEXT_HTML_TYPE).encodings("compress", "gzip", "identity")
                .add().build();
        Variant targettedVariant = req.selectVariant(variants);
        if (targettedVariant == null) {
            return Response.status(466).build();
        }

        if ("xml".equals(targettedVariant.getMediaType().getSubtype())) {
            org.apache.wink.itest.request.Variant v =
                new org.apache.wink.itest.request.Variant();
            v.setMediatype(targettedVariant.getMediaType().getType() + "/"
                + targettedVariant.getMediaType().getSubtype());
            v.setEncoding(targettedVariant.getEncoding());
            v.setLanguage((targettedVariant.getLanguage() != null) ? targettedVariant.getLanguage()
                .getLanguage() : null);
            return Response.ok(v).build();
        }

        return Response.ok(targettedVariant.getMediaType().getType() + "/"
            + targettedVariant.getMediaType().getSubtype()
            + "-"
            + ((targettedVariant.getLanguage() != null) ? targettedVariant.getLanguage()
                .getLanguage() : "")
            + "-"
            + targettedVariant.getEncoding()).build();
    }
}
