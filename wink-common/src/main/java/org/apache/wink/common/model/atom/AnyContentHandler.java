/*******************************************************************************
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *     
 *      http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 *******************************************************************************/
package org.apache.wink.common.model.atom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.wink.common.RestConstants;
import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.utils.ProviderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles objects wrapped with XmlWrapper
 */
/* package */class AnyContentHandler implements DomHandler<XmlWrapper, StreamResult> {

    private static final Logger logger = LoggerFactory.getLogger(AnyContentHandler.class);

    public StreamResult createUnmarshaller(ValidationEventHandler errorHandler) {
        return new StreamResult(new ByteArrayOutputStream());
    }

    public XmlWrapper getElement(StreamResult rt) {
        return new XmlWrapper(((ByteArrayOutputStream)rt.getOutputStream()).toByteArray(), null);
    }

    @SuppressWarnings("unchecked")
    public Source marshal(XmlWrapper xmlWrapper, ValidationEventHandler errorHandler) {
        MediaType type = null;

        String contentType = xmlWrapper.getType();
        if (contentType == null) {
            // should never happen
            type = MediaType.APPLICATION_OCTET_STREAM_TYPE;
        } else if (contentType.equals("xhtml")) {
            type = MediaType.APPLICATION_XML_TYPE;
        } else {
            type = MediaType.valueOf(contentType);
        }

        RuntimeContext runtimeContext = RuntimeContextTLS.getRuntimeContext();
        Providers providers = runtimeContext.getProviders();
        Class<? extends Object> cls = xmlWrapper.getValue().getClass();

        // this code ignores possible generic types
        // if in the future we would like to support the generic types
        // should check here if cls is GenericEntity and handle it

        MessageBodyWriter<Object> writer =
            (MessageBodyWriter<Object>)providers.getMessageBodyWriter(cls, cls, null, type);

        if (writer == null) {
            logger.error(Messages.getMessage("noWriterFound"), cls.getName(), type.toString());
            throw new WebApplicationException(500);
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            writer.writeTo(xmlWrapper.getValue(),
                           cls,
                           cls,
                           AtomJAXBUtils.EMPTY_ARRAY,
                           type,
                           AtomJAXBUtils.EMPTY_OBJECT_MAP,
                           os);
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
        byte[] result;
        if (contentType.equals("xhtml")) {
            try {
                result =
                    new StringBuilder().append("<div xmlns=\"")
                        .append(RestConstants.NAMESPACE_XHTML).append("\">").append(os
                            .toString(ProviderUtils.getCharset(type))).append("</div>").toString()
                        .getBytes();
            } catch (UnsupportedEncodingException e) {
                throw new WebApplicationException(e);
            }
        } else {
            result = os.toByteArray();
        }
        return new StreamSource(new ByteArrayInputStream(result));
    }
}
