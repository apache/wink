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
package org.apache.wink.common.internal.providers.entity.atom;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomJAXBUtils;
import org.apache.wink.common.model.atom.ObjectFactory;
import org.apache.wink.common.utils.ProviderUtils;



public abstract class AbstractAtomEntryProvider<T> {

    private static final Log logger = LogFactory.getLog(AbstractAtomEntryProvider.class);

    protected ObjectFactory atomObjectFactory = new org.apache.wink.common.model.atom.ObjectFactory();

    public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    protected void writeEntry(AtomEntry entry, Class<?> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String,Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        if (entry == null) {
            return;
        }

        JAXBElement<AtomEntry> entryElement = atomObjectFactory.createEntry(entry);
        Marshaller marshaller = AtomEntry.getMarshaller();
        OutputStreamWriter writer = new OutputStreamWriter(entityStream, ProviderUtils.getCharset(mediaType));
        AtomJAXBUtils.marshal(marshaller, entryElement, null, writer);
        writer.flush();
    }

    protected AtomEntry readEntry(Class<AtomEntry> type, Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String,String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {

        Unmarshaller unmarshaller = AtomEntry.getUnmarshaller();
        InputStreamReader reader = new InputStreamReader(entityStream, ProviderUtils.getCharset(mediaType));
        Object object = AtomJAXBUtils.unmarshal(unmarshaller, reader);
        AtomEntry entry = null;
        if (object instanceof AtomEntry) {
            entry = (AtomEntry)object;
        } else {
            logger.error(String.format("request entity is not an atom entry (it was unmarshalled as %s)", object
                    .getClass()));
            throw new WebApplicationException(Status.BAD_REQUEST);
        }

        return entry;
    }
}
