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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.wink.common.RestConstants;
import org.apache.wink.common.internal.application.ApplicationFileLoader;
import org.apache.wink.common.internal.application.ApplicationValidator;
import org.apache.wink.common.internal.contexts.ProvidersImpl;
import org.apache.wink.common.internal.lifecycle.LifecycleManagersRegistry;
import org.apache.wink.common.internal.lifecycle.ScopeLifecycleManager;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.common.internal.registry.metadata.ProviderMetadataCollector;
import org.apache.wink.common.internal.runtime.AbstractRuntimeContext;
import org.apache.wink.common.internal.runtime.RuntimeContextTLS;
import org.apache.wink.common.internal.utils.JAXBUtils;
import org.apache.wink.common.model.JAXBNamespacePrefixMapper;
import org.apache.wink.common.model.opensearch.OpenSearchQuery;
import org.apache.wink.test.mock.TestUtils;

public class AtomTest extends TestCase {

    private static final String ATOM_TEXT_TEXT               =
                                                                 "<title type=\"text\" xml:lang=\"en-us\" xml:base=\"http://title/base\" xmlns=\"http://www.w3.org/2005/Atom\">title</title>\n";
    private static final String ATOM_TEXT_HTML               =
                                                                 "<title type=\"html\" xml:lang=\"en-us\" xml:base=\"http://title/base\" xmlns=\"http://www.w3.org/2005/Atom\">&lt;h1&gt;title&lt;/h1&gt;</title>\n";
    private static final String ATOM_TEXT_XHTML              =
                                                                 "<title type=\"xhtml\" xml:lang=\"en-us\" xml:base=\"http://title/base\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <div xmlns=\""
                                                                     + RestConstants.NAMESPACE_XHTML
                                                                     + "\">\n"
                                                                     + "        <h1>title</h1>\n"
                                                                     + "    </div>\n"
                                                                     + "</title>\n";
    private static final String ATOM_TEXT_XHTML_WITH_TEXT    =
                                                                 "<title type=\"xhtml\" xml:lang=\"en-us\" xml:base=\"http://title/base\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <div xmlns=\""
                                                                     + RestConstants.NAMESPACE_XHTML
                                                                     + "\">\n"
                                                                     + "        title\n"
                                                                     + "    </div>\n"
                                                                     + "</title>\n";

    private static final String ATOM_TEXT_XHTML_WITH_DIV     =
                                                                 "<title type=\"xhtml\" xml:lang=\"en-us\" xml:base=\"http://title/base\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <div xmlns=\""
                                                                     + RestConstants.NAMESPACE_XHTML
                                                                     + "\">\n"
                                                                     + "        <h1>\n"
                                                                     + "            <div>title</div>\n"
                                                                     + "        </h1>\n"
                                                                     + "    </div>\n"
                                                                     + "</title>\n";

    private static final String ATOM_CONTENT_TEXT            =
                                                                 "<content type=\"text\" xml:lang=\"en-us\" xml:base=\"http://title/base\" xmlns=\"http://www.w3.org/2005/Atom\">title</content>\n";
    private static final String ATOM_CONTENT_HTML            =
                                                                 "<content type=\"html\" xml:lang=\"en-us\" xml:base=\"http://title/base\" xmlns=\"http://www.w3.org/2005/Atom\">&lt;h1&gt;title&lt;/h1&gt;</content>\n";
    private static final String ATOM_CONTENT_XHTML           =
                                                                 "<content type=\"xhtml\" xml:lang=\"en-us\" xml:base=\"http://title/base\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <div xmlns=\""
                                                                     + RestConstants.NAMESPACE_XHTML
                                                                     + "\">\n"
                                                                     + "        <h1>title</h1>\n"
                                                                     + "    </div>\n"
                                                                     + "</content>\n";
    private static final String ATOM_CONTENT_XHTML_WITH_TEXT =
                                                                 "<content type=\"xhtml\" xml:lang=\"en-us\" xml:base=\"http://title/base\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <div xmlns=\""
                                                                     + RestConstants.NAMESPACE_XHTML
                                                                     + "\">\n"
                                                                     + "        title\n"
                                                                     + "    </div>\n"
                                                                     + "</content>\n";

    private static final String ATOM_CONTENT_XML             =
                                                                 "<content type=\"application/xml\" xml:lang=\"en-us\" xml:base=\"http://title/base\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <x xmlns=\"http://x/\">title</x>\n"
                                                                     + "</content>\n";

    private static final String ATOM_ENTRY_1                 =
                                                                 replaceTimeToken("<entry xml:lang=\"en-us\" xml:base=\"http://entry/base\" anyAttr=\"anyAttrValue\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <id>1</id>\n"
                                                                     + "    <updated>@TIME@</updated>\n"
                                                                     + "    <title type=\"text\">title</title>\n"
                                                                     + "    <summary type=\"text\">summary</summary>\n"
                                                                     + "    <published>@TIME@</published>\n"
                                                                     + "    <link href=\"href\" type=\"text/plain\" rel=\"rel\" hreflang=\"en-us\" title=\"title\" length=\"10\"/>\n"
                                                                     + "    <author>\n"
                                                                     + "        <email>author@hp.com</email>\n"
                                                                     + "        <name>author</name>\n"
                                                                     + "        <uri>http://uri</uri>\n"
                                                                     + "    </author>\n"
                                                                     + "    <contributor>\n"
                                                                     + "        <email>cont@hp.com</email>\n"
                                                                     + "        <name>cont</name>\n"
                                                                     + "        <uri>http://uri</uri>\n"
                                                                     + "    </contributor>\n"
                                                                     + "    <category label=\"label\" scheme=\"scheme\" term=\"term\"/>\n"
                                                                     + "</entry>\n");
    private static final String ATOM_ENTRY_2                 =
                                                                 replaceTimeToken("<entry xml:lang=\"en-us\" xml:base=\"http://entry/base\" anyAttr=\"anyAttrValue\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <id>2</id>\n"
                                                                     + "    <updated>@TIME@</updated>\n"
                                                                     + "    <title type=\"text\">title</title>\n"
                                                                     + "    <summary type=\"text\">summary</summary>\n"
                                                                     + "    <published>@TIME@</published>\n"
                                                                     + "    <link href=\"href\" type=\"text/plain\" rel=\"rel\" hreflang=\"en-us\" title=\"title\" length=\"10\"/>\n"
                                                                     + "    <author>\n"
                                                                     + "        <email>author@hp.com</email>\n"
                                                                     + "        <name>author</name>\n"
                                                                     + "        <uri>http://uri</uri>\n"
                                                                     + "    </author>\n"
                                                                     + "    <contributor>\n"
                                                                     + "        <email>cont@hp.com</email>\n"
                                                                     + "        <name>cont</name>\n"
                                                                     + "        <uri>http://uri</uri>\n"
                                                                     + "    </contributor>\n"
                                                                     + "    <category label=\"label\" scheme=\"scheme\" term=\"term\"/>\n"
                                                                     + "    <content type=\"text/plain\">Gustaf's Knäckebröd</content>\n"
                                                                     + "</entry>\n");
    private static final String ATOM_ENTRY_3                 =
                                                                 replaceTimeToken("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + "<entry xmlns=\"http://www.w3.org/2005/Atom\" xmlns:ns2=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:ns3=\"http://www.w3.org/1999/xhtml\" anyAttr=\"anyAttrValue\" xml:base=\"http://entry/base\" xml:lang=\"en-us\">\r\n"
                                                                     + "    <id>3</id>\r\n"
                                                                     + "    <updated>1970-01-01T02:20:34.567+02:00</updated>\r\n"
                                                                     + "    <title type=\"text\">title</title>\r\n"
                                                                     + "    <summary type=\"text\">summary</summary>\r\n"
                                                                     + "    <published>1970-01-01T02:20:34.567+02:00</published>\r\n"
                                                                     + "    <link href=\"href\" hreflang=\"en-us\" length=\"10\" rel=\"rel\" title=\"title\" type=\"text/plain\"/>\r\n"
                                                                     + "    <author>\r\n"
                                                                     + "        <email>author@hp.com</email>\r\n"
                                                                     + "        <name>author</name>\r\n"
                                                                     + "        <uri>http://uri</uri>\r\n"
                                                                     + "    </author>\r\n"
                                                                     + "    <contributor>\r\n"
                                                                     + "        <email>cont@hp.com</email>\r\n"
                                                                     + "        <name>cont</name>\r\n"
                                                                     + "        <uri>http://uri</uri>\r\n"
                                                                     + "    </contributor>\r\n"
                                                                     + "    <category label=\"label\" scheme=\"scheme\" term=\"term\"/>\r\n"
                                                                     + "    <content type=\"application/xml\">\r\n"
                                                                     + "        <x:x xmlns=\"http://x/\" xmlns:ns2=\"http://www.w3.org/2005/Atom\" xmlns:ns3=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:ns4=\"http://www.w3.org/1999/xhtml\" xmlns:x=\"http://x/\">Gustaf's Knäckebröd</x:x>\r\n"
                                                                     + "    </content>\r\n"
                                                                     + "</entry>");

    private static final String ATOM_FEED_1                  =
                                                                 replaceTimeToken("<feed xml:lang=\"en-us\" xml:base=\"http://feed/base\" anyAttr=\"anyAttrValue\" xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <id>id</id>\n"
                                                                     + "    <updated>@TIME@</updated>\n"
                                                                     + "    <title type=\"text\">title</title>\n"
                                                                     + "    <subtitle type=\"text\">subtitle</subtitle>\n"
                                                                     + "    <opensearch:itemsPerPage>5</opensearch:itemsPerPage>\n"
                                                                     + "    <opensearch:startIndex>6</opensearch:startIndex>\n"
                                                                     + "    <opensearch:totalResults>7</opensearch:totalResults>\n"
                                                                     + "    <opensearch:Query searchTerms=\"query 1\"/>\n"
                                                                     + "    <opensearch:Query searchTerms=\"query 2\"/>\n"
                                                                     + "    <link href=\"href\" type=\"text/plain\" rel=\"rel\" hreflang=\"en-us\" title=\"title\" length=\"10\"/>\n"
                                                                     + "    <author>\n"
                                                                     + "        <email>author@hp.com</email>\n"
                                                                     + "        <name>author</name>\n"
                                                                     + "        <uri>http://uri</uri>\n"
                                                                     + "    </author>\n"
                                                                     + "    <contributor>\n"
                                                                     + "        <email>cont@hp.com</email>\n"
                                                                     + "        <name>cont</name>\n"
                                                                     + "        <uri>http://uri</uri>\n"
                                                                     + "    </contributor>\n"
                                                                     + "    <category label=\"label\" scheme=\"scheme\" term=\"term\"/>\n"
                                                                     + "    <generator version=\"1.0\" uri=\"http://generator/uri\" xml:lang=\"en-us\" xml:base=\"http://generator/base\">wink</generator>\n"
                                                                     + "    <icon>icon</icon>\n"
                                                                     + "    <logo>logo</logo>\n"
                                                                     + "    <rights type=\"text\">rights</rights>\n"
                                                                     + "    <entry xml:lang=\"en-us\" xml:base=\"http://entry/base\" anyAttr=\"anyAttrValue\">\n"
                                                                     + "        <id>1</id>\n"
                                                                     + "        <updated>@TIME@</updated>\n"
                                                                     + "        <title type=\"text\">title</title>\n"
                                                                     + "        <summary type=\"text\">summary</summary>\n"
                                                                     + "        <published>@TIME@</published>\n"
                                                                     + "        <link href=\"href\" type=\"text/plain\" rel=\"rel\" hreflang=\"en-us\" title=\"title\" length=\"10\"/>\n"
                                                                     + "        <author>\n"
                                                                     + "            <email>author@hp.com</email>\n"
                                                                     + "            <name>author</name>\n"
                                                                     + "            <uri>http://uri</uri>\n"
                                                                     + "        </author>\n"
                                                                     + "        <contributor>\n"
                                                                     + "            <email>cont@hp.com</email>\n"
                                                                     + "            <name>cont</name>\n"
                                                                     + "            <uri>http://uri</uri>\n"
                                                                     + "        </contributor>\n"
                                                                     + "        <category label=\"label\" scheme=\"scheme\" term=\"term\"/>\n"
                                                                     + "        <content type=\"application/xml\">\n"
                                                                     + "            <x:x xmlns=\"http://x/\" xmlns:ns2=\"http://www.w3.org/2005/Atom\" xmlns:ns3=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:ns4=\"http://www.w3.org/1999/xhtml\" xmlns:x=\"http://x/\">Gustaf's Knäckebröd</x:x>\n"
                                                                     + "        </content>\n"
                                                                     + "    </entry>\n"
                                                                     + "</feed>\n");

    private static final String ATOM_FEED_2                  =
                                                                 replaceTimeToken("<feed xml:lang=\"en-us\" xml:base=\"http://feed/base\" anyAttr=\"anyAttrValue\" xmlns=\"http://www.w3.org/2005/Atom\">\n" + "    <id>id</id>\n"
                                                                     + "    <updated>@TIME@</updated>\n"
                                                                     + "    <title type=\"text\">title</title>\n"
                                                                     + "    <subtitle type=\"text\">subtitle</subtitle>\n"
                                                                     + "    <link href=\"href\" type=\"text/plain\" rel=\"rel\" hreflang=\"en-us\" title=\"title\" length=\"10\"/>\n"
                                                                     + "    <author>\n"
                                                                     + "        <email>author@hp.com</email>\n"
                                                                     + "        <name>author</name>\n"
                                                                     + "        <uri>http://uri</uri>\n"
                                                                     + "    </author>\n"
                                                                     + "    <contributor>\n"
                                                                     + "        <email>cont@hp.com</email>\n"
                                                                     + "        <name>cont</name>\n"
                                                                     + "        <uri>http://uri</uri>\n"
                                                                     + "    </contributor>\n"
                                                                     + "    <category label=\"label\" scheme=\"scheme\" term=\"term\"/>\n"
                                                                     + "    <generator version=\"1.0\" uri=\"http://generator/uri\" xml:lang=\"en-us\" xml:base=\"http://generator/base\">wink</generator>\n"
                                                                     + "    <icon>icon</icon>\n"
                                                                     + "    <logo>logo</logo>\n"
                                                                     + "    <rights type=\"text\">rights</rights>\n"
                                                                     + "    <entry xml:lang=\"en-us\" xml:base=\"http://entry/base\" anyAttr=\"anyAttrValue\">\n"
                                                                     + "        <id>1</id>\n"
                                                                     + "        <updated>@TIME@</updated>\n"
                                                                     + "        <title type=\"text\">title</title>\n"
                                                                     + "        <summary type=\"text\">summary</summary>\n"
                                                                     + "        <published>@TIME@</published>\n"
                                                                     + "        <link href=\"href\" type=\"text/plain\" rel=\"rel\" hreflang=\"en-us\" title=\"title\" length=\"10\"/>\n"
                                                                     + "        <author>\n"
                                                                     + "            <email>author@hp.com</email>\n"
                                                                     + "            <name>author</name>\n"
                                                                     + "            <uri>http://uri</uri>\n"
                                                                     + "        </author>\n"
                                                                     + "        <contributor>\n"
                                                                     + "            <email>cont@hp.com</email>\n"
                                                                     + "            <name>cont</name>\n"
                                                                     + "            <uri>http://uri</uri>\n"
                                                                     + "        </contributor>\n"
                                                                     + "        <category label=\"label\" scheme=\"scheme\" term=\"term\"/>\n"
                                                                     + "        <content type=\"application/xml\">\n"
                                                                     + "            <x1 xmlns=\"xxx\" xmlns:y=\"yyy\">\n"
                                                                     + "                <x2>\n"
                                                                     + "                    <y:y1>Gustaf's Knäckebröd</y:y1>\n"
                                                                     + "                </x2>\n"
                                                                     + "            </x1>\n"
                                                                     + "        </content>\n"
                                                                     + "    </entry>\n"
                                                                     + "</feed>\n");

    private static JAXBContext  ctx;

    static {
        try {
            ctx = JAXBContext.newInstance(AtomFeed.class, AtomEntry.class, AtomText.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        LifecycleManagersRegistry ofFactoryRegistry = new LifecycleManagersRegistry();
        ofFactoryRegistry.addFactoryFactory(new ScopeLifecycleManager<Object>());
        ProvidersRegistry providersRegistry =
            new ProvidersRegistry(ofFactoryRegistry, new ApplicationValidator());

        Set<Class<?>> classes = new ApplicationFileLoader().getClasses();
        if (classes != null) {
            for (Class<?> cls : classes) {
                if (ProviderMetadataCollector.isProvider(cls)) {
                    providersRegistry.addProvider(cls);
                }
            }
        }
        AbstractRuntimeContext runtimeContext = new AbstractRuntimeContext() {

            public OutputStream getOutputStream() throws IOException {
                return null;
            }

            public InputStream getInputStream() throws IOException {
                return null;
            }
        };
        runtimeContext.setAttribute(Providers.class, new ProvidersImpl(providersRegistry,
                                                                       runtimeContext));
        RuntimeContextTLS.setRuntimeContext(runtimeContext);
    }

    public void testAtomTextMarshal() throws Exception {

        Marshaller m = JAXBUtils.createMarshaller(ctx);

        AtomText text = new AtomText();
        text.setBase("http://title/base");
        text.setLang("en-us");

        JAXBNamespacePrefixMapper mapper =
            new JAXBNamespacePrefixMapper(RestConstants.NAMESPACE_ATOM);
        mapper.omitNamespace(RestConstants.NAMESPACE_OPENSEARCH);

        // test type TEXT
        text.setType(AtomTextType.text);
        text.setValue("title");
        JAXBElement<AtomText> element = (new ObjectFactory()).createTitle(text);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);

        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_TEXT_TEXT, os.toString());
        assertNull(msg, msg);

        // test type HTML
        text.setType(AtomTextType.html);
        text.setValue("<h1>title</h1>");
        element = (new ObjectFactory()).createTitle(text);
        os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);
        msg = TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_TEXT_HTML, os.toString());
        assertNull(msg, msg);

        // test type XHTML
        text.setType(AtomTextType.xhtml);
        text.setValue("<h1>title</h1>");
        element = (new ObjectFactory()).createTitle(text);
        os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);
        msg = TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_TEXT_XHTML, os.toString());
        assertNull(msg, msg);
    }

    public void testAtomTextUnmarshal() throws Exception {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_TEXT_TEXT));
        assertNotNull(element);
        assertTrue(element instanceof AtomText);
        AtomText text = (AtomText)element;
        assertNotNull(text);
        assertEquals("en-us", text.getLang());
        assertEquals("http://title/base", text.getBase());
        assertEquals("title", text.getValue());
        assertEquals(AtomTextType.text, text.getType());

        element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_TEXT_HTML));
        assertNotNull(element);
        assertTrue(element instanceof AtomText);
        text = (AtomText)element;
        assertNotNull(text);
        assertEquals("en-us", text.getLang());
        assertEquals("http://title/base", text.getBase());
        assertEquals("<h1>title</h1>", text.getValue());
        assertEquals(AtomTextType.html, text.getType());

        element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_TEXT_XHTML));
        assertNotNull(element);
        assertTrue(element instanceof AtomText);
        text = (AtomText)element;
        assertNotNull(text);
        assertEquals("en-us", text.getLang());
        assertEquals("http://title/base", text.getBase());
        String msg =
            TestUtils
                .diffIgnoreUpdateWithAttributeQualifier("<h1 xmlns=\"http://www.w3.org/1999/xhtml\">title</h1>",
                                                        text.getValue());
        assertNull(msg, msg);
        assertEquals(AtomTextType.xhtml, text.getType());

        element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_TEXT_XHTML_WITH_TEXT));
        assertNotNull(element);
        assertTrue(element instanceof AtomText);
        text = (AtomText)element;
        assertNotNull(text);
        assertEquals("en-us", text.getLang());
        assertEquals("http://title/base", text.getBase());
        assertEquals("title", text.getValue().trim());
        assertEquals(AtomTextType.xhtml, text.getType());
    }

    public void testAtomTextUnmarshalXhtml() throws Exception {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);

        Object element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_TEXT_XHTML_WITH_DIV));
        assertNotNull(element);
        assertTrue(element instanceof AtomText);
        AtomText text = (AtomText)element;
        assertNotNull(text);
        assertEquals("en-us", text.getLang());
        assertEquals("http://title/base", text.getBase());
        String msg =
            TestUtils
                .diffIgnoreUpdateWithAttributeQualifier("<h1 xmlns=\"" + RestConstants.NAMESPACE_XHTML
                                                            + "\"><div>title</div></h1>",
                                                        text.getValue().replaceAll("\r", "")
                                                            .replaceAll("\n", "")
                                                            .replaceAll("    ", ""));
        assertNull(msg, msg);
        assertEquals(AtomTextType.xhtml, text.getType());
    }

    public void testAtomContentMarshal() throws Exception {
        Marshaller m = JAXBUtils.createMarshaller(ctx);

        JAXBNamespacePrefixMapper mapper =
            new JAXBNamespacePrefixMapper(RestConstants.NAMESPACE_ATOM);
        mapper.omitNamespace(RestConstants.NAMESPACE_OPENSEARCH);

        AtomContent content = new AtomContent();
        content.setBase("http://title/base");
        content.setLang("en-us");

        content.setType("text");
        content.setValue("title");
        JAXBElement<AtomContent> element = (new ObjectFactory()).createContent(content);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_CONTENT_TEXT, os.toString());
        assertNull(msg, msg);

        content.setType("html");
        content.setValue("<h1>title</h1>");
        element = (new ObjectFactory()).createContent(content);
        os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);
        msg = TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_CONTENT_HTML, os.toString());
        assertNull(msg, msg);

        content.setType("xhtml");
        content.setValue("<h1>title</h1>");
        element = (new ObjectFactory()).createContent(content);
        os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);
        msg = TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_CONTENT_XHTML, os.toString());
        assertNull(msg, msg);

        content.setType("application/xml");
        X x = new X();
        x.setTitle("title");
        content.setValue(x);
        element = (new ObjectFactory()).createContent(content);
        os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);
        msg = TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_CONTENT_XML, os.toString());
        assertNull(msg, msg);
        
        content.setValue("<x xmlns=\"http://x/\">title</x>");
        element = (new ObjectFactory()).createContent(content);
        os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);
        msg = TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_CONTENT_XML, os.toString());
        assertNull(msg, msg);
    }

    @XmlRootElement(name = "x", namespace = "http://x/")
    @XmlType(name = "x", propOrder = {"title"})
    protected static class X {

        @XmlMixed
        @XmlAnyElement
        private List<Object> title;

        public void setTitle(String title) {
            this.title = Arrays.asList((Object)title);
        }

    }

    public void testAtomContentUnmarshal() throws Exception {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_CONTENT_TEXT));
        assertNotNull(element);
        assertTrue(element instanceof AtomContent);
        AtomContent content = (AtomContent)element;
        assertNotNull(content);
        assertEquals("en-us", content.getLang());
        assertEquals("http://title/base", content.getBase());
        assertEquals("title", content.getValue());
        assertEquals("text", content.getType());

        element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_CONTENT_HTML));
        assertNotNull(element);
        assertTrue(element instanceof AtomContent);
        content = (AtomContent)element;
        assertNotNull(content);
        assertEquals("en-us", content.getLang());
        assertEquals("http://title/base", content.getBase());
        assertEquals("<h1>title</h1>", content.getValue());
        assertEquals("html", content.getType());

        element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_CONTENT_XHTML));
        assertNotNull(element);
        assertTrue(element instanceof AtomContent);
        content = (AtomContent)element;
        assertNotNull(content);
        assertEquals("en-us", content.getLang());
        assertEquals("http://title/base", content.getBase());
        String msg =
            TestUtils
                .diffIgnoreUpdateWithAttributeQualifier("<h1 xmlns=\"" + RestConstants.NAMESPACE_XHTML
                                                            + "\">title</h1>",
                                                        content.getValue());
        assertNull(msg, msg);
        assertEquals("xhtml", content.getType());

        element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_CONTENT_XHTML_WITH_TEXT));
        assertNotNull(element);
        assertTrue(element instanceof AtomContent);
        content = (AtomContent)element;
        assertNotNull(content);
        assertEquals("en-us", content.getLang());
        assertEquals("http://title/base", content.getBase());
        assertEquals("title", content.getValue().trim());
        assertEquals("xhtml", content.getType());

        element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_CONTENT_XML));
        assertNotNull(element);
        assertTrue(element instanceof AtomContent);
        content = (AtomContent)element;
        assertNotNull(content);
        assertEquals("en-us", content.getLang());
        assertEquals("http://title/base", content.getBase());
        msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier("<x xmlns=\"http://x/\">title</x>",
                                                             content.getValue());
        assertNull(msg, msg);
        assertEquals("application/xml", content.getType());
    }

    public void testAtomEntryMarshal() throws Exception {
        Marshaller m = AtomEntry.getMarshaller();

        AtomEntry entry = getEntryWithoutContent("1");
        JAXBElement<AtomEntry> element = (new ObjectFactory()).createEntry(entry);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);
        String msg = TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_ENTRY_1, os.toString());
        assertNull(msg, msg);

        entry = getEntryWithPlainTextContent("2");
        element = (new ObjectFactory()).createEntry(entry);
        os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);
        msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_ENTRY_2.getBytes("UTF-8"), os
                .toByteArray());
        assertNull(msg, msg);

        entry = getEntryWithXmlContent("3");
        element = (new ObjectFactory()).createEntry(entry);
        os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);
        msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_ENTRY_3.getBytes("UTF-8"), os
                .toByteArray());
        assertNull(msg, msg);
    }

    public void testAtomEntryUnmarshal() throws Exception {
        Unmarshaller u = AtomEntry.getUnmarshaller();
        Object element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_ENTRY_1));
        assertNotNull(element);
        assertTrue(element instanceof AtomEntry);

        AtomEntry entry = AtomEntry.unmarshal(new StringReader(ATOM_ENTRY_1));
        assertEquals("en-us", entry.getLang());
        assertEquals("http://entry/base", entry.getBase());
        assertEquals("1", entry.getId());
        assertEquals("anyAttrValue", entry.getOtherAttributes().get(new QName("anyAttr")));
        assertEquals(getDate(), entry.getPublished());
        assertEquals("summary", entry.getSummary().getValue());
        assertEquals("title", entry.getTitle().getValue());
        assertEquals(1, entry.getAuthors().size());
        assertEquals("author", entry.getAuthors().get(0).getName());
        assertEquals("author@hp.com", entry.getAuthors().get(0).getEmail());
        assertEquals("http://uri", entry.getAuthors().get(0).getUri());
        assertEquals(1, entry.getContributors().size());
        assertEquals("cont", entry.getContributors().get(0).getName());
        assertEquals("cont@hp.com", entry.getContributors().get(0).getEmail());
        assertEquals("http://uri", entry.getContributors().get(0).getUri());
        assertEquals(1, entry.getLinks().size());
        assertEquals("href", entry.getLinks().get(0).getHref());
        assertEquals("rel", entry.getLinks().get(0).getRel());
        assertEquals("text/plain", entry.getLinks().get(0).getType());
        assertEquals(1, entry.getCategories().size());
        assertEquals("label", entry.getCategories().get(0).getLabel());
        assertEquals("scheme", entry.getCategories().get(0).getScheme());
        assertEquals("term", entry.getCategories().get(0).getTerm());

        element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_ENTRY_2));
        assertNotNull(element);
        assertTrue(element instanceof AtomEntry);
        entry = (AtomEntry)element;
        assertNotNull(entry);
        assertEquals("2", entry.getId());
        assertNotNull(entry.getContent());
        assertEquals("text/plain", entry.getContent().getType());
        assertEquals("Gustaf's Knäckebröd", entry.getContent().getValue());

        element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_ENTRY_3));
        assertNotNull(element);
        assertTrue(element instanceof AtomEntry);
        entry = (AtomEntry)element;
        assertNotNull(entry);
        assertEquals("3", entry.getId());
        assertNotNull(entry.getContent());
        assertEquals("application/xml", entry.getContent().getType());
        X x = entry.getContent().getValue(X.class);
        assertEquals("Gustaf's Knäckebröd", x.title.get(0));
    }

    public void testAtomFeedMarshal() throws Exception {
        Marshaller m = AtomFeed.getMarshaller();

        AtomFeed feed = getFeed();
        JAXBElement<AtomFeed> element = (new ObjectFactory()).createFeed(feed);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_FEED_1.getBytes("UTF-8"), os
                .toByteArray());
        assertNull(msg, msg);
    }

    public void testAtomFeedUnmarshal() throws Exception {
        Unmarshaller u = AtomFeed.getUnmarshaller();
        Object element = AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_FEED_1));
        assertNotNull(element);
        assertTrue(element instanceof AtomFeed);

        AtomFeed feed = AtomFeed.unmarshal(new StringReader(ATOM_FEED_1));
        assertEquals("en-us", feed.getLang());
        assertEquals("http://feed/base", feed.getBase());
        assertEquals("id", feed.getId());
        assertEquals("anyAttrValue", feed.getOtherAttributes().get(new QName("anyAttr")));
        assertEquals(getDate(), feed.getUpdated());
        assertEquals("title", feed.getTitle().getValue());
        assertEquals("logo", feed.getLogo());
        assertEquals("rights", feed.getRights().getValue());
        assertEquals("subtitle", feed.getSubtitle().getValue());
        assertEquals(1, feed.getAuthors().size());
        assertEquals("author", feed.getAuthors().get(0).getName());
        assertEquals("author@hp.com", feed.getAuthors().get(0).getEmail());
        assertEquals("http://uri", feed.getAuthors().get(0).getUri());
        assertEquals(1, feed.getContributors().size());
        assertEquals("cont", feed.getContributors().get(0).getName());
        assertEquals("cont@hp.com", feed.getContributors().get(0).getEmail());
        assertEquals("http://uri", feed.getContributors().get(0).getUri());
        assertEquals(1, feed.getLinks().size());
        assertEquals("href", feed.getLinks().get(0).getHref());
        assertEquals("rel", feed.getLinks().get(0).getRel());
        assertEquals("text/plain", feed.getLinks().get(0).getType());
        assertEquals(1, feed.getCategories().size());
        assertEquals("label", feed.getCategories().get(0).getLabel());
        assertEquals("scheme", feed.getCategories().get(0).getScheme());
        assertEquals("term", feed.getCategories().get(0).getTerm());
        assertEquals(1, feed.getEntries().size());
        assertEquals(5, feed.getItemsPerPage());
        assertEquals(6, feed.getStartIndex());
        assertEquals(7, feed.getTotalResults());
        List<OpenSearchQuery> openSearchQuery = feed.getOpenSearchQueries();
        assertEquals(2, openSearchQuery.size());
        assertEquals("query 1", openSearchQuery.get(0).getSearchTerms());
        assertEquals("query 2", openSearchQuery.get(1).getSearchTerms());
    }

    public void testAtomFeedMarshalUnmarshal() throws Exception {
        Marshaller m = AtomFeed.getMarshaller();
        Unmarshaller u = AtomFeed.getUnmarshaller();
        AtomFeed feed = (AtomFeed)AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_FEED_1));
        JAXBElement<AtomFeed> element = (new ObjectFactory()).createFeed(feed);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_FEED_1.getBytes("UTF-8"), os.toByteArray());
        assertNull(msg, msg);
    }

    public void testAtomFeedMarshalUnmarshalWithoutOpenSearch() throws Exception {
        Marshaller m = AtomFeed.getMarshaller();
        Unmarshaller u = AtomFeed.getUnmarshaller();
        AtomFeed feed = (AtomFeed)AtomJAXBUtils.unmarshal(u, new StringReader(ATOM_FEED_2));
        assertEquals(-1, feed.getItemsPerPage());
        assertEquals(-1, feed.getStartIndex());
        assertEquals(-1, feed.getTotalResults());
        assertEquals(0, feed.getOpenSearchQueries().size());
        JAXBElement<AtomFeed> element = (new ObjectFactory()).createFeed(feed);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, os);
        String msg =
            TestUtils.diffIgnoreUpdateWithAttributeQualifier(ATOM_FEED_2.getBytes("UTF-8"), os.toByteArray());
        assertNull(msg, msg);
    }

    public void testGetLinkFromEntry() {
        AtomEntry entry = new AtomEntry();
        AtomLink link1 = new AtomLink();
        link1.setHref("href1");
        link1.setRel("rel1");
        link1.setType("type1");
        entry.getLinks().add(link1);
        AtomLink link2 = new AtomLink();
        link2.setHref("href2");
        link2.setRel("rel2");
        link2.setType("type2");
        entry.getLinks().add(link2);
        AtomLink link3 = new AtomLink();
        link3.setHref("href3");
        link3.setRel("rel2");
        link3.setType("type2");
        entry.getLinks().add(link3);
        AtomLink link4NoRel = new AtomLink();
        link4NoRel.setHref("href3");
        link4NoRel.setType("type2");
        entry.getLinks().add(link4NoRel);
        AtomLink link5NoType = new AtomLink();
        link5NoType.setHref("href3");
        link3.setRel("rel2");
        entry.getLinks().add(link5NoType);

        List<AtomLink> links = entry.getLinks("rel1", "type1");
        assertEquals(1, links.size());
        assertEquals("href1", links.get(0).getHref());

        links = entry.getLinks("rel2", "type2");
        assertEquals(2, links.size());
        assertEquals("href2", links.get(0).getHref());
        assertEquals("href3", links.get(1).getHref());

        links = entry.getLinksByRelation("rel2");
        assertEquals(2, links.size());
        assertEquals("href2", links.get(0).getHref());
        assertEquals("href3", links.get(1).getHref());

        links = entry.getLinksByType("type2");
        assertEquals(3, links.size());
        assertEquals("href2", links.get(0).getHref());
        assertEquals("href3", links.get(1).getHref());

        try {
            entry.getLinks(null, "");
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
            // passed!
        }

        try {
            entry.getLinks("", null);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
            // passed!
        }

        try {
            entry.getLinks("", null);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
            // passed!
        }

        try {
            entry.getLinks("", null);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
            // passed!
        }

    }

    private AtomFeed getFeed() throws Exception {
        AtomFeed feed = new AtomFeed();
        feed.setBase("http://feed/base");
        feed.getOtherAttributes().put(new QName("anyAttr"), "anyAttrValue");
        AtomGenerator generator = new AtomGenerator();
        generator.setBase("http://generator/base");
        generator.setLang("en-us");
        generator.setUri("http://generator/uri");
        generator.setValue("wink");
        generator.setVersion("1.0");
        feed.setGenerator(generator);
        feed.setIcon("icon");
        feed.setId("id");
        feed.setItemsPerPage(5);
        feed.setStartIndex(6);
        feed.setTotalResults(7);

        OpenSearchQuery query = new OpenSearchQuery();
        query.setSearchTerms("query 1");
        feed.addOpenSearchQuery(query);
        query = new OpenSearchQuery();
        query.setSearchTerms("query 2");
        feed.addOpenSearchQuery(query);

        feed.setLang("en-us");
        feed.setLogo("logo");
        AtomText rights = new AtomText();
        rights.setType(AtomTextType.text);
        rights.setValue("rights");
        feed.setRights(rights);
        AtomText subtitle = new AtomText();
        subtitle.setType(AtomTextType.text);
        subtitle.setValue("subtitle");
        feed.setSubtitle(subtitle);
        AtomText title = new AtomText();
        title.setType(AtomTextType.text);
        title.setValue("title");
        feed.setTitle(title);
        feed.setUpdated(getDate());
        AtomPerson author = new AtomPerson();
        author.setEmail("author@hp.com");
        author.setName("author");
        author.setUri("http://uri");
        feed.getAuthors().add(author);
        AtomPerson cont = new AtomPerson();
        cont.setEmail("cont@hp.com");
        cont.setName("cont");
        cont.setUri("http://uri");
        feed.getContributors().add(cont);
        AtomCategory cat = new AtomCategory();
        cat.setLabel("label");
        cat.setScheme("scheme");
        cat.setTerm("term");
        feed.getCategories().add(cat);
        AtomLink link = new AtomLink();
        link.setHref("href");
        link.setRel("rel");
        link.setTitle("title");
        link.setType("text/plain");
        link.setHreflang("en-us");
        link.setLength("10");
        feed.getLinks().add(link);
        feed.getEntries().add(getEntryWithXmlContent("1"));
        return feed;
    }

    private static Date getDate() {
        return getCalendar().toGregorianCalendar().getTime();
    }

    private static XMLGregorianCalendar getCalendar() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(1234567);
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
        XMLGregorianCalendar xmlGregCal = datatypeFactory.newXMLGregorianCalendar(calendar);
        return xmlGregCal;
    }

    private static String replaceTimeToken(String string) {
        XMLGregorianCalendar calendar = getCalendar();
        return string.replace("@TIME@", calendar.toXMLFormat());
    }

    private AtomEntry getEntryWithoutContent(String id) throws Exception {
        AtomEntry entry = new AtomEntry();
        entry.setId(id);
        entry.getOtherAttributes().put(new QName("anyAttr"), "anyAttrValue");
        entry.setBase("http://entry/base");
        entry.setLang("en-us");
        entry.setPublished(getDate());
        AtomText summary = new AtomText();
        summary.setType(AtomTextType.text);
        summary.setValue("summary");
        entry.setSummary(summary);
        AtomText title = new AtomText();
        title.setType(AtomTextType.text);
        title.setValue("title");
        entry.setTitle(title);
        entry.setUpdated(getDate());
        AtomPerson author = new AtomPerson();
        author.setEmail("author@hp.com");
        author.setName("author");
        author.setUri("http://uri");
        entry.getAuthors().add(author);
        AtomPerson cont = new AtomPerson();
        cont.setEmail("cont@hp.com");
        cont.setName("cont");
        cont.setUri("http://uri");
        entry.getContributors().add(cont);
        AtomCategory cat = new AtomCategory();
        cat.setLabel("label");
        cat.setScheme("scheme");
        cat.setTerm("term");
        entry.getCategories().add(cat);
        AtomLink link = new AtomLink();
        link.setHref("href");
        link.setRel("rel");
        link.setTitle("title");
        link.setType("text/plain");
        link.setHreflang("en-us");
        link.setLength("10");
        entry.getLinks().add(link);
        return entry;
    }

    private AtomEntry getEntryWithPlainTextContent(String id) throws Exception {
        AtomEntry entry = getEntryWithoutContent(id);
        AtomContent content = new AtomContent();
        content.setType("text/plain");
        content.setValue("Gustaf's Knäckebröd");
        entry.setContent(content);
        return entry;
    }

    private AtomEntry getEntryWithXmlContent(String id) throws Exception {
        AtomEntry entry = getEntryWithoutContent(id);
        AtomContent content = new AtomContent();
        content.setType("application/xml");
        X x = new X();
        x.setTitle("Gustaf's Knäckebröd");
        content.setValue(x);
        entry.setContent(content);
        return entry;
    }

    public void testEncoding() throws Exception {
        AtomEntry atomEntry = getEntryWithoutContent("1111");

        JAXBElement<AtomEntry> element = (new ObjectFactory()).createEntry(atomEntry);
        Marshaller marshaller = AtomEntry.getMarshaller();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(marshaller, element, os);
        String defaultContent = new String(os.toByteArray(), "UTF-8");

        String[] encodings = {"US-ASCII", "UTF-8", "UTF-16"};
        for (String enc : encodings) {
            element = (new ObjectFactory()).createEntry(atomEntry);
            marshaller = AtomEntry.getMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, enc);
            os = new ByteArrayOutputStream();
            AtomJAXBUtils.marshal(marshaller, element, os);
            String msg =
                TestUtils.diffIgnoreUpdateWithAttributeQualifier(defaultContent, new String(os.toByteArray(), enc));
            assertNull(msg, msg);
        }
    }

}
