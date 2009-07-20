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
package org.apache.wink.common.internal;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.junit.Test;

public class ResponseImplTest {

    @Test
    public void testResponse() throws Exception {
        Response r = Response.ok().language("aa").lastModified(new Date()).build();
        assertNotNull(r.getMetadata());
        assertEquals(r.getStatus(), 200);

        URI loc = new URI("/defects/5");
        r =
            Response.created(loc).cookie(new NewCookie("cName", "cValue")).expires(new Date())
                .build();
        assertEquals(loc, r.getMetadata().getFirst(HttpHeaders.LOCATION));
        assertTrue(r.getMetadata().getFirst(HttpHeaders.SET_COOKIE).toString().indexOf("cName") > -1);

        r = Response.serverError().build();
        assertTrue(r.getStatus() >= 500);

        Response r2 = Response.fromResponse(r).build();
        assertEquals(r.getMetadata(), r2.getMetadata());
        assertEquals(r.getStatus(), r2.getStatus());
        assertEquals(r.getEntity(), r2.getEntity());

        r =
            Response.status(201).entity(new String("I'm the Entity")).expires(new Date()).clone()
                .build();
        assertEquals(r.getStatus(), 201);
        assertEquals(r.getEntity(), new String("I'm the Entity"));
        assertNotNull(r.getMetadata().getFirst(HttpHeaders.EXPIRES));

        r = Response.temporaryRedirect(loc).tag("tag").header("headerName", "headerValue").build();
        assertNotNull(r.getMetadata().getFirst("headerName"));
        assertNotNull(r.getMetadata().getFirst(HttpHeaders.LOCATION));
        assertEquals("tag", r.getMetadata().getFirst(HttpHeaders.ETAG));

        r = Response.temporaryRedirect(loc).tag("tag").header("headerName", null).build();
        assertNull(r.getMetadata().getFirst("headerName"));

        r =
            Response.ok().variant(new Variant(new MediaType("application", "atom+xml"),
                                              new Locale("Hebrew"), "UTF8")).build();
        Object o = r.getMetadata().getFirst(HttpHeaders.CONTENT_TYPE);
        assertNotNull(o);

        List<Variant> l = Variant.encodings("UTF8").build();
        l.add(new Variant(null, null, "UTF8"));
        r = Response.noContent().variants(l).build();
        int i = r.getStatus();
        assertEquals(i, 204);
        assertNotNull(r.getMetadata().getFirst(HttpHeaders.VARY));

        l = Variant.encodings("UTF8").build();
        r = Response.noContent().variants(l).variants(null).build();
        assertEquals(i, 204);
        assertNull(r.getMetadata().getFirst(HttpHeaders.VARY));

    }

}
