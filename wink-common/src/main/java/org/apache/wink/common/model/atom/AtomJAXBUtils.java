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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.wink.common.RestConstants;
import org.apache.wink.common.RestException;
import org.apache.wink.common.internal.model.NamespacePrefixMapperProvider;
import org.apache.wink.common.internal.utils.SAXHandlerWrapper;
import org.apache.wink.common.model.JAXBNamespacePrefixMapper;
import org.apache.xml.serialize.EncodingInfo;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

public class AtomJAXBUtils {

    private final static SAXParserFactory spf;
    private final static EncodingInfo     encodingInfo;
    private static final DatatypeFactory  datatypeFactory;

    static {
        try {
            spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setValidating(false);
            encodingInfo = (new OutputFormat(Method.XML, "UTF-8", true)).getEncodingInfo();
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
            throw new RestException(errorMessage, e);
        } catch (ParserConfigurationException e) {
            // shoudln't happen
            throw new RestException(errorMessage, e);
        } catch (IOException e) {
            throw new RestException(errorMessage, e);
        }
    }

    public static Object unmarshal(Unmarshaller unmarshaller, Reader reader) throws IOException {
        Object result = null;
        try {
            UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
            AtomUnmarshallingListener.AtomUnmarshallerHandler handler =
                new AtomUnmarshallingListener.AtomUnmarshallerHandler(unmarshallerHandler);
            AtomUnmarshallingListener listener = new AtomUnmarshallingListener(handler);
            unmarshaller.setListener(listener);
            // here is where the magic begins.
            // SAX will parse the XML document, and our handler will get all the
            // SAX events
            saxParse(reader, handler, "failed to unmarshal object");
            // parsing is done and the JAXB object is ready
            result = handler.getResult();
            if (result instanceof JAXBElement<?>) {
                result = ((JAXBElement<?>)result).getValue();
            }
        } catch (IllegalStateException e) {
            throw new RestException("failed to unmarshal object", e);
        } catch (JAXBException e) {
            throw new RestException("failed to unmarshal object", e);
        }
        return result;
    }

    public static void marshal(Marshaller marshaller, Object jaxbObject, OutputStream os)
        throws IOException {
        marshal(marshaller, jaxbObject, null, null, os);
    }

    public static void marshal(Marshaller marshaller,
                               Object jaxbObject,
                               Map<String, String> processingInstructions,
                               OutputStream os) throws IOException {
        marshal(marshaller, jaxbObject, processingInstructions, null, os);
    }

    public static void marshal(Marshaller marshaller,
                               Object jaxbObject,
                               Map<String, String> processingInstructions,
                               JAXBNamespacePrefixMapper namespacePrefixMapper,
                               OutputStream os) throws IOException {
        try {
            // create a new SAX serializer for creating the XML output.
            // we will invoke the marshal of JAXB to send all its marshaling
            // events to this handler
            AtomMarshallingListener.AtomMarshallerHandler handler =
                new AtomMarshallingListener.AtomMarshallerHandler(processingInstructions,
                                                                  marshaller);
            if (namespacePrefixMapper == null) {
                Object jaxb = jaxbObject;
                if (jaxbObject instanceof JAXBElement<?>) {
                    jaxb = ((JAXBElement<?>)jaxbObject).getValue();
                }
                if (jaxb instanceof NamespacePrefixMapperProvider) {
                    namespacePrefixMapper =
                        ((NamespacePrefixMapperProvider)jaxb).getNamespacePrefixMapper();
                }
            }
            handler.setNamespacePrefixMapper(namespacePrefixMapper);
            handler.setOutputByteStream(os);
            // add our listener to the marshaler so we will receive events from
            // JAXB
            marshaller.setListener(new AtomMarshallingListener(handler));
            // perform the marshaling so that our marshaler (xml serializer)
            // will receive all the
            // SAX events
            ContentHandler xmlSerializer = handler.asContentHandler();
            marshaller.marshal(jaxbObject, xmlSerializer);
        } catch (JAXBException e) {
            throw new RestException("failed to marshal object (" + jaxbObject.getClass().getName()
                + ")", e);
        }
    }

    // ========================== AtomMarshallingListener
    // =================================

    //
    // JAXB marshaling listener which is used to get events from JAXB during the
    // marshaling of an
    // XML
    //
    public static class AtomMarshallingListener extends Marshaller.Listener {

        // this is the AtomMarshallerHandler that will receive all the SAX
        // events from JAXB for
        // creating the
        // XML output.
        private AtomMarshallerHandler handler;

        public AtomMarshallingListener(AtomMarshallerHandler handler) {
            this.handler = handler;
        }

        @Override
        public void beforeMarshal(Object source) {
            if (isValueActuallyXml(source)) {
                // handler.setIsContentXml(true);
                String xmlContent = null;
                if (source instanceof AtomContent) {
                    AtomContent content = (AtomContent)source;
                    xmlContent = content.saveValue();
                    if (content.getType().equals("xhtml")) {
                        xmlContent = surroundWithXhtmlDiv(xmlContent);
                    }
                } else if (source instanceof AtomText) {
                    AtomText text = (AtomText)source;
                    // we know that the type is xhtml, or we wouldn't have
                    // gotten here
                    xmlContent = text.saveValue();
                    xmlContent = surroundWithXhtmlDiv(xmlContent);
                }
                handler.pushXmlContent(xmlContent);
            }
        }

        private String surroundWithXhtmlDiv(String xhtml) {
            if (xhtml == null) {
                return null;
            }
            return "<div xmlns=\"" + RestConstants.NAMESPACE_XHTML + "\">" + xhtml + "</div>";
        }

        @Override
        public void afterMarshal(Object source) {
            if (isValueActuallyXml(source)) {
                // restore original value
                if (source instanceof AtomContent) {
                    AtomContent content = (AtomContent)source;
                    content.revertValue();
                } else if (source instanceof AtomText) {
                    AtomText text = (AtomText)source;
                    text.revertValue();
                }
            }
        }

        /**
         * AtomMarshallerHandler is a SAX ContentHandler which extends
         * XMLSerliazer to create an XML output from SAX events.
         */
        public static class AtomMarshallerHandler extends XMLSerializer {
            // this is used to control the output of the xml that the
            // XMLSerializer will produce
            private OutputFormat              of;
            private int                       indentation = 4;

            private Map<String, String>       processingInstructions;
            private JAXBNamespacePrefixMapper namespacePrefixMapper;

            private String                    xmlContent;

            public AtomMarshallerHandler(Map<String, String> processingInstructions,
                                         Marshaller marshaller) throws JAXBException,
                UnsupportedEncodingException {
                this.processingInstructions = processingInstructions;
                namespacePrefixMapper = null;
                xmlContent = null;

                // prepare the OutputFormat to output the XML as desired
                of = new OutputFormat();
                of.setMethod(Method.XML);
                String encoding = (String)marshaller.getProperty(Marshaller.JAXB_ENCODING);
                of.setEncoding(encoding == null ? encodingInfo : new OutputFormat(Method.XML,
                                                                                  encoding, true)
                    .getEncodingInfo());
                Boolean property =
                    (Boolean)marshaller.getProperty(Marshaller.JAXB_FORMATTED_OUTPUT);
                boolean formattedOutput = false;
                if (property != null) {
                    formattedOutput = property.booleanValue();
                }
                of.setIndenting(formattedOutput);
                if (formattedOutput) {
                    of.setIndent(indentation);
                    of.setLineWidth(256);
                }

                property = (Boolean)marshaller.getProperty(Marshaller.JAXB_FRAGMENT);
                boolean xmlDeclaration = false;
                if (property != null) {
                    xmlDeclaration = property.booleanValue();
                }
                of.setOmitXMLDeclaration(xmlDeclaration);
                super.setOutputFormat(of);
            }

            public void setNamespacePrefixMapper(JAXBNamespacePrefixMapper namespacePrefixMapper) {
                this.namespacePrefixMapper = namespacePrefixMapper;
            }

            public String popXmlContent() {
                String ret = xmlContent;
                xmlContent = null;
                return ret;
            }

            public void pushXmlContent(String xmlContent) {
                this.xmlContent = xmlContent;
            }

            public boolean isXmlContent() {
                return xmlContent != null;
            }

            // ======= ContentHandler methods ============

            @Override
            public void startDocument(String rootTagName) throws IOException {
                // if the user supplied additional processing instructions, add
                // them now
                if (processingInstructions != null) {
                    Set<String> keys = processingInstructions.keySet();
                    String attributes = null;
                    for (String key : keys) {
                        try {
                            attributes = processingInstructions.get(key);
                            super.processingInstruction(key, attributes);
                        } catch (SAXException e) {
                            throw new RestException("failed to add processing instruction '" + key
                                + "' with attributes '"
                                + attributes
                                + "'", e);
                        }
                    }
                }
                super.startDocument(rootTagName);
            }

            @Override
            public void startPrefixMapping(String prefix, String uri) throws SAXException {
                if (namespacePrefixMapper != null) {
                    if (namespacePrefixMapper.isNamespaceOmitted(uri)) {
                        return;
                    }
                    prefix = namespacePrefixMapper.getPreferredPrefix(uri, prefix, true);
                }
                super.startPrefixMapping(prefix, uri);
            }

            @Override
            public void startElement(String namespaceURI,
                                     String localName,
                                     String rawName,
                                     Attributes attrs) throws SAXException {

                if (namespacePrefixMapper != null) {
                    String prefix =
                        namespacePrefixMapper.getPreferredPrefix(namespaceURI, null, false);
                    if (prefix != null) {
                        if (prefix.length() == 0) {
                            rawName = localName;
                        } else {
                            rawName = prefix + ":" + localName;
                        }
                    }
                }
                super.startElement(namespaceURI, localName, rawName, attrs);

                if (isXmlContent()) {
                    parseXmlContent();
                }
            }

            private void parseXmlContent() {
                String xmlContent = popXmlContent();
                JAXBNamespacePrefixMapper mapper = namespacePrefixMapper;
                // disable the prefix mapper
                namespacePrefixMapper = null;
                // parse the xml content and send all events to this handler,
                // except for the startDocument and endDocument events
                saxParse(new StringReader(xmlContent),
                         new XmlContentHandler(this),
                         "Bad XML content in Atom");
                // restore the prefix mapper
                namespacePrefixMapper = mapper;
            }

            /**
             * Delegates all SAX events to the contained handler except for the
             * start and end document events
             */
            private static class XmlContentHandler extends SAXHandlerWrapper {
                public XmlContentHandler(ContentHandler handler) {
                    super(handler);
                }

                @Override
                public void startDocument() throws SAXException {
                    // no op
                }

                @Override
                public void endDocument() throws SAXException {
                    // no op
                }
            }
        }

    }

    // ========================== AtomUnmarshallingListener
    // =================================

    public static class AtomUnmarshallingListener extends Unmarshaller.Listener {

        private AtomUnmarshallerHandler unmarshallerHandler;

        public AtomUnmarshallingListener(AtomUnmarshallerHandler handler) {
            this.unmarshallerHandler = handler;
        }

        @Override
        public void beforeUnmarshal(Object target, Object parent) {
            // if JAXB is just about to unmarshal an AtomContent element
            if (target instanceof AtomContent) {
                unmarshallerHandler.startSpecialContent(target);

                // if JAXB is just about to unmarshal an AtomText construct
                // element
            } else if (target instanceof AtomText) {
                unmarshallerHandler.startSpecialContent(target);
            }
        }

        @Override
        public void afterUnmarshal(Object target, Object parent) {
            if (target instanceof AtomContent) {
                ((AtomContent)target).setValue(unmarshallerHandler.endSpecialContent());
            } else if (target instanceof AtomText) {
                ((AtomText)target).setValue(unmarshallerHandler.endSpecialContent());
            }
        }

        /**
         * This handler is used to receive all SAX events during the parsing of
         * an incoming Atom feed or entry. During the parsing of the XML we will
         * delegate almost all of the events to JAXB so it can do its magic.
         * Events that are related to the contents of a &lt;content> element or
         * to a text construct element will be redirected to an instance of a
         * SpecialContentHandler for converting the contents into a string even
         * if it is xml
         */
        public static class AtomUnmarshallerHandler implements UnmarshallerHandler, LexicalHandler {

            // the JAXB ContentHandler that will receive all the delegated SAX
            // parser events other
            // than the events related to elements whose xml contents need to be
            // treated as text
            private UnmarshallerHandler   jaxbHandler;

            // this is an xml serializer to handle SAX events during the parsing
            // of an element whose xml contents needs to be treated as text,
            // even
            // if it is xml
            private SpecialContentHandler specialContentHandler;

            // flag for indicating that we are currently handing an element
            // whose
            // contents needs to be treated as text, even if it is xml
            private boolean               isSpecialContent;

            public AtomUnmarshallerHandler(UnmarshallerHandler handler) {
                jaxbHandler = handler;
                isSpecialContent = false;
            }

            public void startSpecialContent(Object target) {
                // this method is called from the UnmarshallerListener, who
                // received a notification
                // that a JAXB element is going to be unmarshalled.
                isSpecialContent = true;
                specialContentHandler = new SpecialContentHandler(target);
                try {
                    specialContentHandler.startDocument();
                } catch (SAXException e) {
                    // shouldn't happen
                    throw new RestException("xmlSerializer failed", e);
                }
            }

            public String endSpecialContent() {
                try {
                    specialContentHandler.endDocument();
                } catch (SAXException e) {
                    // shouldn't happen
                    throw new RestException("xmlSerializer failed", e);
                }
                String contentStr = specialContentHandler.getResult();
                return contentStr;
            }

            public Object getResult() throws JAXBException, IllegalStateException {
                return jaxbHandler.getResult();
            }

            // ======= ContentHandler methods ============

            public void endElement(String uri, String localName, String name) throws SAXException {
                if (isSpecialContent) {
                    if (specialContentHandler.isDone()) {
                        isSpecialContent = false;
                    } else {
                        // delegate the event to the special content serializer
                        specialContentHandler.endElement(uri, localName, name);
                        return;
                    }
                }

                // delegate the event to JAXB
                jaxbHandler.endElement(uri, localName, name);
            }

            public void startElement(String uri, String localName, String name, Attributes atts)
                throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.startElement(uri, localName, name, atts);
                    return;
                }
                jaxbHandler.startElement(uri, localName, name, atts);
            }

            public void characters(char[] ch, int start, int length) throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.characters(ch, start, length);
                    return;
                }
                jaxbHandler.characters(ch, start, length);
            }

            public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.ignorableWhitespace(ch, start, length);
                    return;
                }
                jaxbHandler.ignorableWhitespace(ch, start, length);
            }

            public void processingInstruction(String target, String data) throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.processingInstruction(target, data);
                    return;
                }
                jaxbHandler.processingInstruction(target, data);
            }

            public void setDocumentLocator(Locator locator) {
                if (isSpecialContent) {
                    specialContentHandler.setDocumentLocator(locator);
                    return;
                }
                jaxbHandler.setDocumentLocator(locator);
            }

            public void skippedEntity(String name) throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.skippedEntity(name);
                    return;
                }
                jaxbHandler.skippedEntity(name);
            }

            public void startDocument() throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.startDocument();
                    return;
                }
                jaxbHandler.startDocument();
            }

            public void endDocument() throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.endDocument();
                    return;
                }
                jaxbHandler.endDocument();
            }

            public void startPrefixMapping(String prefix, String uri) throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.startPrefixMapping(prefix, uri);
                    return;
                }
                jaxbHandler.startPrefixMapping(prefix, uri);
            }

            public void endPrefixMapping(String prefix) throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.endPrefixMapping(prefix);
                    return;
                }
                jaxbHandler.endPrefixMapping(prefix);
            }

            // ======= LexicalHandler methods ============

            public void startCDATA() throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.startCDATA();
                }
            }

            public void endCDATA() throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.endCDATA();
                }
            }

            public void comment(char[] ch, int start, int length) throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.comment(ch, start, length);
                }
            }

            public void startDTD(String name, String publicId, String systemId) throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.startDTD(name, publicId, systemId);
                }
            }

            public void endDTD() throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.endDTD();
                }
            }

            public void startEntity(String name) throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.startEntity(name);
                }
            }

            public void endEntity(String name) throws SAXException {
                if (isSpecialContent) {
                    specialContentHandler.endEntity(name);
                }
            }
        }

        private static class SpecialContentHandler extends SAXHandlerWrapper {
            // the JAXB object whose value may be text or xml
            private Object          target;

            private CharArrayWriter writer;
            private int             elementCounter;
            private boolean         isXhtml;
            private boolean         isXmlOpen;

            public SpecialContentHandler(Object target) {
                super(initXmlSerializer());
                this.target = target;
                writer = new CharArrayWriter();
                ((XMLSerializer)getHandler()).setOutputCharStream(writer);
                elementCounter = 0;
                isXhtml = false;
                isXmlOpen = false;
            }

            private static ContentHandler initXmlSerializer() {
                OutputFormat outputFormat = new OutputFormat();
                outputFormat.setMethod(Method.XML);
                outputFormat.setEncoding(encodingInfo);
                outputFormat.setOmitXMLDeclaration(true);
                XMLSerializer xmlSerializer = new XMLSerializer(outputFormat);
                return xmlSerializer;
            }

            public String getResult() {
                String result = writer.toString();
                if (result.length() == 0) {
                    return null;
                }
                if (isXmlOpen) {
                    result = result.trim();
                    if (!result.startsWith("<")) {
                        throw new RuntimeException(
                                                   "Illegal atom content: must contain a single child element");
                    }
                }
                return result;
            }

            public boolean isDone() {
                return elementCounter == 0;
            }

            public void setContentIsXhtml(boolean isContentXhtml) {
                this.isXhtml = isContentXhtml;
            }

            private boolean isFirstElement() {
                return elementCounter == 1;
            }

            private boolean isLastElement() {
                return elementCounter == 0;
            }

            private boolean isRootElement() {
                return elementCounter == 0;
            }

            @Override
            public void startElement(String uri, String localName, String name, Attributes atts)
                throws SAXException {
                if (isRootElement()) {
                    openXml();
                }

                ++elementCounter;
                // if we need to skip processing of the first <div> element
                if (isFirstElement() && isXhtml) {
                    // if we are unmarshalling xhtml, we need to skip processing
                    // of the first <div>
                    // element
                    if (uri.equals(RestConstants.NAMESPACE_XHTML) && localName
                        .equalsIgnoreCase("div")) {
                        // if this is the first div element, then we must ignore
                        // it
                        return;
                    } else {
                        throw new RuntimeException(
                                                   "Illegal content: xhtml content must have a div root element");
                    }
                }

                // delegate the event to the xml serializer
                super.startElement(uri, localName, name, atts);
            }

            private void openXml() {
                if (isXmlOpen) {
                    throw new RuntimeException(
                                               "Illegal atom content: must have only one root element");
                }

                // get the type of contents of the JAXB object
                String type = null;
                if (target instanceof AtomContent) {
                    type = ((AtomContent)target).getType();
                } else if (target instanceof AtomText) {
                    type = ((AtomText)target).getType().name();
                }

                if (AtomTextType.xhtml.name().equals(type)) {
                    isXhtml = true;
                }

                if (!isXhtml && !isTypeXml(type)) {
                    throw new RuntimeException(
                                               "Illegal atom content: must not contain child elements");
                }
                isXmlOpen = true;
            }

            @Override
            public void endElement(String uri, String localName, String name) throws SAXException {
                --elementCounter;
                // if we need to skip processing of the first <div> element
                if (isLastElement() && isXhtml) {
                    if (uri.equals(RestConstants.NAMESPACE_XHTML) && localName
                        .equalsIgnoreCase("div")) {
                        return;
                    }
                }
                // delegate the event to the xml serializer
                super.endElement(uri, localName, name);
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                if (isXmlOpen) {
                    super.characters(ch, start, length);
                } else {
                    writer.write(ch, start, length);
                }
            }
        }
    }

    // /**
    // * Remove xml declaration from XML.
    // *
    // * @param xmlStr
    // * The XML
    // * @return String The XML without xml declaration
    // */
    // public static String stripXmlDecl(String xmlStr) {
    //
    // if (xmlStr == null) {
    // return null;
    // }
    //
    // int startInd;
    // int endInd;
    // StringBuilder xmlStrBuilder = new StringBuilder(xmlStr);
    //
    // while ((startInd = xmlStrBuilder.indexOf("<?")) >= 0) {
    // endInd = xmlStrBuilder.indexOf("?>") + 2;
    // xmlStrBuilder.replace(startInd, endInd, "");
    // }
    // return xmlStrBuilder.toString();
    // }
    //
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
}
