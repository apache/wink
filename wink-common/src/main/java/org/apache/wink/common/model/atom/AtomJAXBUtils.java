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

package org.apache.wink.common.model.atom;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.wink.common.RestException;
import org.apache.wink.common.RuntimeContext;
import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.internal.utils.UnmodifiableMultivaluedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

public class AtomJAXBUtils {

    public static final MultivaluedMap<String, Object> EMPTY_OBJECT_MAP =
                                                                            new UnmodifiableMultivaluedMap<String, Object>(
                                                                                                                           new MultivaluedMapImpl<String, Object>());
    public static final MultivaluedMap<String, String> EMPTY_STRING_MAP =
                                                                            new UnmodifiableMultivaluedMap<String, String>(
                                                                                                                           new MultivaluedMapImpl<String, String>());
    public static final Annotation[]                   EMPTY_ARRAY      = new Annotation[0];
    private final static SAXParserFactory              spf;
    private final static DatatypeFactory               datatypeFactory;
    private static final Logger                        logger           =
                                                                            LoggerFactory
                                                                                .getLogger(AtomJAXBUtils.class);

    static {
        try {
            spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setValidating(false);
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (Exception e) {
            throw new RestException("Error setting up Atom JAXB utils", e);
        }
    }

    public static boolean isTypeXml(String type) {

        // remove parameters if they exist
        int index = type.indexOf(';');
        if (index > -1) {
            type = type.substring(0, index).trim();
        }

        // as per RFC3023 and Atom specification
        type = type.toLowerCase();
        if (type.endsWith("/xml") || type.endsWith("+xml")
            || type.equals("xhtml")
            || type.equals("text/xml-external-parsed-entity")
            || type.equals("application/xml-external-parsed-entity")
            || type.equals("application/xml-dtd")) {
            return true;
        }

        return false;
    }

    public static boolean isValueActuallyXml(Object source) {
        if (source instanceof AtomContent) {
            AtomContent content = (AtomContent)source;
            String type = content.getType();
            if (AtomJAXBUtils.isTypeXml(type)) {
                return true;
            }
        } else if (source instanceof AtomText) {
            AtomText text = (AtomText)source;
            AtomTextType type = text.getType();
            if (type == AtomTextType.xhtml) {
                return true;
            }
        }
        return false;
    }

    public static void saxParse(Reader reader, ContentHandler handler, String errorMessage) {
        XMLReader xmlReader;
        try {
            xmlReader = spf.newSAXParser().getXMLReader();
            xmlReader.setContentHandler(handler);
            // setting this property will cause the handler to get lexical
            // events as well
            if (handler instanceof LexicalHandler) {
                xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
            }
            xmlReader.parse(new InputSource(reader));
        } catch (SAXException e) {
            logger.error(errorMessage);
            throw new WebApplicationException(e);
        } catch (ParserConfigurationException e) {
            logger.error(errorMessage);
            throw new WebApplicationException(e);
        } catch (IOException e) {
            logger.error(errorMessage);
            throw new WebApplicationException(e);
        }
    }

    public static Object unmarshal(Unmarshaller unmarshaller, Reader reader) throws IOException {
        Object result = null;
        try {
            result = unmarshaller.unmarshal(reader);
            if (result instanceof JAXBElement<?>) {
                result = ((JAXBElement<?>)result).getValue();
            }
        } catch (IllegalStateException e) {
            throw new WebApplicationException(e);
        } catch (JAXBException e) {
            throw new WebApplicationException(e);
        }
        return result;
    }

    public static void marshal(Marshaller marshaller, Object jaxbObject, OutputStream os)
        throws IOException {
        try {
            marshaller.marshal(jaxbObject, os);
        } catch (JAXBException e) {
            throw new WebApplicationException(e);
        }
    }

    public static XMLGregorianCalendar timeToXmlGregorianCalendar(long time) {
        if (time == -1) {
            return null;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        XMLGregorianCalendar xmlGregCal = datatypeFactory.newXMLGregorianCalendar(calendar);
        return xmlGregCal;
    }

    public static long xmlGregorianCalendarToTime(XMLGregorianCalendar xmlGregCal) {
        if (xmlGregCal == null) {
            return -1;
        }
        Calendar calendar = xmlGregCal.toGregorianCalendar();
        long time = calendar.getTimeInMillis();
        return time;
    }

    @SuppressWarnings("unchecked")
    public static <T> T readValue(List<Object> list,
                                  Class<T> type,
                                  Providers providers,
                                  Type genericType,
                                  Annotation[] annotations,
                                  MultivaluedMap<String, String> httpHeaders,
                                  MediaType mediaType) throws IOException {
        if (list == null || list.isEmpty()) {
            return null;
        }

        Object value = list.get(0);

        if (value == null) {
            return null;
        }

        Class<? extends Object> cls = value.getClass();
        if (type.isAssignableFrom(cls)) {
            return (T)value;
        }

        if (value instanceof JAXBElement<?>) {
            value = ((JAXBElement<?>)value).getValue();
            return readValue(Arrays.asList(value),
                             type,
                             providers,
                             genericType,
                             annotations,
                             httpHeaders,
                             mediaType);
        }

        if (cls == AtomXhtml.class) {
            return readValue(((AtomXhtml)value).getAny(),
                             type,
                             providers,
                             genericType,
                             annotations,
                             httpHeaders,
                             mediaType);
        }

        if (cls == XmlWrapper.class) {
            value = ((XmlWrapper)value).getValue();
            return readValue(Arrays.asList(value),
                             type,
                             providers,
                             genericType,
                             annotations,
                             httpHeaders,
                             mediaType);
        }

        if (value instanceof byte[]) {
            if (providers == null) {
                // try to get Providers from the TLS
                RuntimeContext runtimeContext = RuntimeContextTLS.getRuntimeContext();
                if (runtimeContext != null) {
                    providers = runtimeContext.getProviders();
                }
            }
            MessageBodyReader<T> reader =
                providers.getMessageBodyReader(type, type, EMPTY_ARRAY, mediaType);
            if (reader == null)
                throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
            T read =
                reader.readFrom(type,
                                type,
                                annotations,
                                mediaType,
                                httpHeaders,
                                new ByteArrayInputStream((byte[])value));
            return read;
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getName()
            + " to "
            + type.getName());

    }

    public static MediaType determineMediaType(String type) {
        MediaType mediaType;
        if (type == null || type.equals("text") || type.equals("html")) {
            mediaType = MediaType.TEXT_PLAIN_TYPE;
        } else if (type.equals("xhtml")) {
            mediaType = MediaType.APPLICATION_XML_TYPE;
        } else {
            mediaType = MediaType.valueOf(type);
        }
        return mediaType;
    }

    /**
     * Fixes content of any list.
     * <p>
     * This method provides the solution of wrapping the necessary elements with
     * XmlWrapper in order to invoke AnyContentHandler later.
     * 
     * @param any
     * @param type
     */
    public static void fixAnyContent(List<Object> any, String type) {
        if (any == null || any.isEmpty()) {
            // nothing to handle for null or empty objects
            return;
        }

        // retrieve the value to handle
        Object value = any.get(0);

        if (type == null) {
            // if type not set, use AtomTextType.text
            type = AtomTextType.text.name();
        }

        if (value instanceof XmlWrapper) {
            XmlWrapper xmlWrapper = (XmlWrapper)value;
            if (xmlWrapper.getType() == null) {
                // fixes type on the XmlWrapper in the case it was not set, it
                // happens if the same object was unmarsheled, and now is going
                // to be marsheled back to xml
                xmlWrapper.setType(type);
            }
        } else if (value.getClass() == String.class && !isTypeXml(type)) {
            // Non xml strings should be escaped
            // nothing to do
        } else {
            // wrapping with XmlWrapper will cause the Providers code to run
            // xml content won't be escaped
            any.set(0, new XmlWrapper(value, type));
        }
    }
}
