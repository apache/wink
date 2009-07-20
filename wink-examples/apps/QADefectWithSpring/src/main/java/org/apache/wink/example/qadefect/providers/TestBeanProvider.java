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
package org.apache.wink.example.qadefect.providers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.apache.wink.common.model.synd.SyndContent;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndPerson;
import org.apache.wink.common.model.synd.SyndText;
import org.apache.wink.example.qadefect.legacy.DefectBean;
import org.apache.wink.example.qadefect.legacy.TestBean;
import org.apache.wink.example.qadefect.resources.TestsResource;
import org.apache.wink.server.utils.LinkBuilders;

@Provider
@Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON, MediaType.WILDCARD})
@Consumes( {MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_JSON, MediaType.WILDCARD})
public class TestBeanProvider implements MessageBodyWriter<TestBean> {

    @Context
    private Providers    providers;
    @Context
    private LinkBuilders linkBuilders;
    @Context
    private UriInfo      uriInfo;

    public long getSize(TestBean t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        return type == TestBean.class;
    }

    public void writeTo(TestBean bean,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        MessageBodyWriter<TestBean> writer =
            providers.getMessageBodyWriter(TestBean.class,
                                           DefectBean.class,
                                           annotations,
                                           MediaType.APPLICATION_XML_TYPE);
        writer.writeTo(bean,
                       bean.getClass(),
                       bean.getClass(),
                       annotations,
                       MediaType.APPLICATION_XML_TYPE,
                       httpHeaders,
                       os);

        SyndEntry syndEntry = createSyndEntry(bean);
        syndEntry
            .setContent(new SyndContent(os.toString("UTF-8"), MediaType.APPLICATION_XML, false));

        Object testsResource = uriInfo.getMatchedResources().get(0);

        String testId = syndEntry.getId().substring(syndEntry.getId().lastIndexOf(':') + 1);
        // add self and alternate representation links to document metadata
        linkBuilders.createSystemLinksBuilder().resource(testsResource)
            .subResource(TestsResource.TEST_URL).pathParam(TestsResource.TEST_PARAM, testId)
            .build(syndEntry.getLinks());

        MessageBodyWriter<SyndEntry> syndEntryWriter =
            providers
                .getMessageBodyWriter(SyndEntry.class, SyndEntry.class, annotations, mediaType);
        syndEntryWriter.writeTo(syndEntry,
                                syndEntry.getClass(),
                                syndEntry.getClass(),
                                annotations,
                                mediaType,
                                httpHeaders,
                                entityStream);
    }

    public static SyndEntry createSyndEntry(TestBean bean) {
        SyndEntry syndEntry = new SyndEntry();

        // set basic entry fields in document metadata
        syndEntry.setId("urn:com:hp:qadefects:test:" + bean.getId());
        syndEntry.setTitle(new SyndText(bean.getName()));
        syndEntry.setSummary(new SyndText(bean.getDescription()));
        syndEntry.getAuthors().add(new SyndPerson(bean.getAuthor()));
        if (bean.getCreated() != null) {
            syndEntry.setPublished(bean.getCreated());
        }

        return syndEntry;
    }

}
