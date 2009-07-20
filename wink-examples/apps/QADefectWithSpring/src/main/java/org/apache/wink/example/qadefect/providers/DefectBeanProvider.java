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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.apache.wink.common.model.atom.AtomConstants;
import org.apache.wink.common.model.synd.SyndCategory;
import org.apache.wink.common.model.synd.SyndContent;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndPerson;
import org.apache.wink.common.model.synd.SyndText;
import org.apache.wink.example.qadefect.legacy.DefectBean;
import org.apache.wink.example.qadefect.resources.DefectsResource;
import org.apache.wink.server.utils.LinkBuilders;

@Provider
@Produces( {MediaType.APPLICATION_ATOM_XML, MediaType.WILDCARD})
@Consumes( {MediaType.APPLICATION_ATOM_XML, MediaType.WILDCARD})
public class DefectBeanProvider implements MessageBodyReader<DefectBean>,
    MessageBodyWriter<DefectBean> {

    @Context
    private Providers    providers;
    @Context
    private LinkBuilders linkBuilders;
    @Context
    private UriInfo      uriInfo;

    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return type == DefectBean.class;
    }

    public DefectBean readFrom(Class<DefectBean> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders,
                               InputStream entityStream) throws IOException,
        WebApplicationException {

        // deserialize to SyndEntry
        MessageBodyReader<SyndEntry> messageBodyReader =
            providers
                .getMessageBodyReader(SyndEntry.class, SyndEntry.class, annotations, mediaType);
        SyndEntry syndEntry =
            messageBodyReader.readFrom(SyndEntry.class,
                                       SyndEntry.class,
                                       annotations,
                                       mediaType,
                                       httpHeaders,
                                       entityStream);

        // verify that SyndEntry contains xml content
        SyndContent content = syndEntry.getContent();
        if (content == null) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        String contentType = content.getType();
        String value = content.getValue();
        if (value == null || contentType == null
            || contentType.equals(MediaType.APPLICATION_XML) == false) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        // deserialize content to DefectBean
        MessageBodyReader<DefectBean> reader =
            providers.getMessageBodyReader(type,
                                           genericType,
                                           annotations,
                                           MediaType.APPLICATION_XML_TYPE);
        return reader.readFrom(type,
                               genericType,
                               annotations,
                               mediaType,
                               httpHeaders,
                               new ByteArrayInputStream(value.getBytes()));
    }

    public long getSize(DefectBean t,
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
        return type == DefectBean.class;
    }

    public void writeTo(DefectBean bean,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {

        // create content of SyndEntry
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        MessageBodyWriter<DefectBean> writer =
            providers.getMessageBodyWriter(DefectBean.class,
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

        // create SyndEntry
        SyndEntry syndEntry = createSyndEntry(bean);
        // set content
        syndEntry
            .setContent(new SyndContent(os.toString("UTF-8"), MediaType.APPLICATION_XML, false));

        // generate links
        DefectsResource defectsResource = (DefectsResource)uriInfo.getMatchedResources().get(0);
        generateLinksForEntry(syndEntry, linkBuilders, defectsResource.getDefectTestResource());

        // serialize SyndEntry according to the requested media type
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

    public static SyndEntry createSyndEntry(DefectBean bean) {
        SyndEntry syndEntry = new SyndEntry();

        // set basic entry fields in document metadata
        syndEntry.setId("urn:com:hp:qadefects:defect:" + bean.getId());
        syndEntry.setTitle(new SyndText(bean.getName()));
        syndEntry.setSummary(new SyndText(bean.getDescription()));
        syndEntry.getAuthors().add(new SyndPerson(bean.getAuthor()));

        syndEntry.getCategories().add(new SyndCategory("urn:com:hp:qadefects:categories:severity",
                                                       bean.getSeverity(), null));
        syndEntry.getCategories().add(new SyndCategory("urn:com:hp:qadefects:categories:status",
                                                       bean.getStatus(), null));

        if (bean.getCreated() != null) {
            syndEntry.setPublished(bean.getCreated());
        }
        return syndEntry;
    }

    public static void generateLinksForEntry(SyndEntry entry,
                                             LinkBuilders linkBuilders,
                                             Object testsResource) {

        // generate system links
        String defectId = entry.getId().substring(entry.getId().lastIndexOf(':') + 1);
        linkBuilders.createSystemLinksBuilder().subResource(defectId).build(entry.getLinks());

        // generate related links - each defect can access its tests
        linkBuilders.createSingleLinkBuilder().resource(testsResource)
            .rel(AtomConstants.ATOM_REL_RELATED).pathParam(DefectsResource.DEFECT_PARAM, defectId)
            .type(MediaType.APPLICATION_ATOM_XML_TYPE).build(entry.getLinks());
    }

}
