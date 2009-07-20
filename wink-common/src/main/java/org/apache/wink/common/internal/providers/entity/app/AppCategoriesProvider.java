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
package org.apache.wink.common.internal.providers.entity.app;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.model.app.AppCategories;
import org.apache.wink.common.model.app.ObjectFactory;
import org.apache.wink.common.model.atom.AtomJAXBUtils;
import org.apache.wink.common.utils.ProviderUtils;

/**
 * Representation of Atom Category Document. Category Document is a document
 * that describes the categories allowed in a Collection.
 */
@Provider
@Produces(MediaTypeUtils.ATOM_CATEGORIES_DOCUMENT)
public class AppCategoriesProvider implements MessageBodyWriter<AppCategories> {

    public long getSize(AppCategories t,
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
        return type == AppCategories.class;
    }

    public void writeTo(AppCategories t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            ObjectFactory objectFactory = new ObjectFactory();
            JAXBElement<AppCategories> feedElement = objectFactory.createCategories(t);
            Marshaller marshaller = AppCategories.getMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, ProviderUtils.getCharset(mediaType));
            AtomJAXBUtils.marshal(marshaller, feedElement, entityStream);
        } catch (PropertyException e) {
            throw new WebApplicationException(e);
        }
    }
}
