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

package org.apache.wink.common.model.opensearch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.wink.common.model.atom.AtomJAXBUtils;

public class OpenSearchTest extends TestCase {

    private static final String OPENSEARCH =
                                               "<OpenSearchDescription ns2:otherAttrName=\"otherAttrValue\" xmlns=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:ns2=\"otherAttrNs\">\n" + "    <ShortName>short name</ShortName>\n"
                                                   + "    <Description>description</Description>\n"
                                                   + "    <Tags>tags</Tags>\n"
                                                   + "    <Contact>contact</Contact>\n"
                                                   + "    <Url type=\"type\" template=\"template\"/>\n"
                                                   + "    <LongName>long name</LongName>\n"
                                                   + "    <Image type=\"image type 1\" width=\"67890\" height=\"12345\">image value 1</Image>\n"
                                                   + "    <Image type=\"image type 2\" width=\"12345\" height=\"67890\">image value 2</Image>\n"
                                                   + "    <Query outputEncoding=\"output encoding\" inputEncoding=\"input encoding\" language=\"language\" startIndex=\"2\" startPage=\"3\" count=\"1\" totalResults=\"4\" title=\"title\" searchTerms=\"search terms\" role=\"role\" ns2:otherAttrName=\"otherAttrValue\"/>\n"
                                                   + "    <Developer>developer</Developer>\n"
                                                   + "    <Attribution>attribution</Attribution>\n"
                                                   + "    <SyndicationRight>syndication right</SyndicationRight>\n"
                                                   + "    <AdultContent>false</AdultContent>\n"
                                                   + "    <Language>language 1</Language>\n"
                                                   + "    <Language>language 2</Language>\n"
                                                   + "    <OutputEncoding>output encoding 1</OutputEncoding>\n"
                                                   + "    <OutputEncoding>output encoding 2</OutputEncoding>\n"
                                                   + "    <InputEncoding>input encoding 1</InputEncoding>\n"
                                                   + "    <InputEncoding>input encoding 2</InputEncoding>\n"
                                                   + "</OpenSearchDescription>\n";

    // private static JAXBContext ctx;
    //    
    // static {
    // try {
    // ctx =
    // JAXBContext.newInstance(OpenSearchDescription.class.getPackage().getName());
    // } catch (JAXBException e) {
    // throw new RuntimeException(e);
    // }
    // }

    public void testOpenSearchMarshal() throws IOException {
        // Map<String,String> p2n = new HashMap<String,String>();
        // p2n.put("otherAttrNs", "other");
        // JAXBNamespacePrefixMapper namespacePrefixMapper =
        // new JAXBNamespacePrefixMapper(RestConstants.NAMESPACE_OPENSEARCH,
        // p2n);
        // Marshaller m = AtomJAXBUtils.createMarshaller(ctx,
        // namespacePrefixMapper);
        Marshaller m = OpenSearchDescription.getMarshaller();

        OpenSearchDescription osd = getOpenSearchDescription();
        JAXBElement<OpenSearchDescription> element =
            (new ObjectFactory()).createOpenSearchDescription(osd);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, null, os);
        assertEquals(OPENSEARCH, os.toString());
    }

    public void testOpenSearchUnmarshal() throws IOException {
        // Unmarshaller u = AtomJAXBUtils.createUnmarshaller(ctx);
        Unmarshaller u = OpenSearchDescription.getUnmarshaller();
        Object element = AtomJAXBUtils.unmarshal(u, new StringReader(OPENSEARCH));
        assertNotNull(element);
        assertTrue(element instanceof OpenSearchDescription);

        OpenSearchDescription osd = (OpenSearchDescription)element;
        OpenSearchDescription expectedOsd = getOpenSearchDescription();

        assertOpenSearchDescription(expectedOsd, osd);
    }

    public void testOpenSearchUnmarshalMarshal() throws IOException {
        // Marshaller m = AtomJAXBUtils.createMarshaller(ctx, new
        // JAXBNamespacePrefixMapper(RestConstants.NAMESPACE_OPENSEARCH));
        Marshaller m = OpenSearchDescription.getMarshaller();
        // Unmarshaller u = AtomJAXBUtils.createUnmarshaller(ctx);
        Unmarshaller u = OpenSearchDescription.getUnmarshaller();

        Object service = AtomJAXBUtils.unmarshal(u, new StringReader(OPENSEARCH));
        JAXBElement<OpenSearchDescription> element =
            (new ObjectFactory()).createOpenSearchDescription((OpenSearchDescription)service);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        AtomJAXBUtils.marshal(m, element, null, os);
        assertEquals(OPENSEARCH, os.toString());
    }

    private void assertOpenSearchDescription(OpenSearchDescription expectedOsd,
                                             OpenSearchDescription osd) {
        assertEquals(expectedOsd.getAdultContent(), osd.getAdultContent());
        assertEquals(expectedOsd.getAttribution(), osd.getAttribution());
        assertEquals(expectedOsd.getContact(), osd.getContact());
        assertEquals(expectedOsd.getDescription(), osd.getDescription());
        assertEquals(expectedOsd.getDeveloper(), osd.getDeveloper());
        assertEquals(expectedOsd.getLongName(), osd.getLongName());
        assertEquals(expectedOsd.getShortName(), osd.getShortName());
        assertEquals(expectedOsd.getSyndicationRight(), osd.getSyndicationRight());
        assertEquals(expectedOsd.getTags(), osd.getTags());

        assertEquals(expectedOsd.getInputEncoding().size(), osd.getInputEncoding().size());
        for (int i = 0; i < expectedOsd.getInputEncoding().size(); ++i) {
            assertEquals(expectedOsd.getInputEncoding().get(i), osd.getInputEncoding().get(i));
        }

        assertEquals(expectedOsd.getOutputEncoding().size(), osd.getOutputEncoding().size());
        for (int i = 0; i < expectedOsd.getOutputEncoding().size(); ++i) {
            assertEquals(expectedOsd.getOutputEncoding().get(i), osd.getOutputEncoding().get(i));
        }

        assertEquals(expectedOsd.getLanguage().size(), osd.getLanguage().size());
        for (int i = 0; i < expectedOsd.getLanguage().size(); ++i) {
            assertEquals(expectedOsd.getLanguage().get(i), osd.getLanguage().get(i));
        }

        assertEquals(expectedOsd.getOtherAttributes().size(), osd.getOtherAttributes().size());
        for (int i = 0; i < expectedOsd.getOtherAttributes().size(); ++i) {
            assertEquals(expectedOsd.getOtherAttributes().get(new QName("otherAttrNs",
                                                                        "otherAttrName")), osd
                .getOtherAttributes().get(new QName("otherAttrNs", "otherAttrName")));
        }

        assertEquals(expectedOsd.getImage().size(), osd.getImage().size());
        for (int i = 0; i < expectedOsd.getImage().size(); ++i) {
            OpenSearchImage expected = expectedOsd.getImage().get(i);
            OpenSearchImage actual = osd.getImage().get(i);
            assertEquals(expected.getType(), actual.getType());
            assertEquals(expected.getValue(), actual.getValue());
            assertEquals(expected.getHeight(), actual.getHeight());
            assertEquals(expected.getWidth(), actual.getWidth());
        }

        assertEquals(expectedOsd.getUrl().size(), osd.getUrl().size());
        for (int i = 0; i < expectedOsd.getUrl().size(); ++i) {
            OpenSearchUrl expected = expectedOsd.getUrl().get(i);
            OpenSearchUrl actual = osd.getUrl().get(i);
            assertEquals(expected.getType(), actual.getType());
            assertEquals(expected.getTemplate(), actual.getTemplate());
        }

        assertEquals(expectedOsd.getQuery().size(), osd.getQuery().size());
        for (int i = 0; i < expectedOsd.getQuery().size(); ++i) {
            OpenSearchQuery expected = expectedOsd.getQuery().get(i);
            OpenSearchQuery actual = osd.getQuery().get(i);
            assertEquals(expected.getInputEncoding(), actual.getInputEncoding());
            assertEquals(expected.getLanguage(), actual.getLanguage());
            assertEquals(expected.getOutputEncoding(), actual.getOutputEncoding());
            assertEquals(expected.getRole(), actual.getRole());
            assertEquals(expected.getSearchTerms(), actual.getSearchTerms());
            assertEquals(expected.getTitle(), actual.getTitle());
            assertEquals(expected.getCount(), actual.getCount());
            assertEquals(expected.getStartIndex(), actual.getStartIndex());
            assertEquals(expected.getStartPage(), actual.getStartPage());
            assertEquals(expected.getTotalResults(), actual.getTotalResults());
            assertEquals(expected.getOtherAttributes().size(), actual.getOtherAttributes().size());
            assertEquals(expected.getOtherAttributes()
                .get(new QName("otherAttrNs", "otherAttrName")), actual.getOtherAttributes()
                .get(new QName("otherAttrNs", "otherAttrName")));
        }

    }

    private OpenSearchDescription getOpenSearchDescription() {
        OpenSearchDescription osd = new OpenSearchDescription();

        osd.setAttribution("attribution");
        osd.setContact("contact");
        osd.setDescription("description");
        osd.setDeveloper("developer");
        osd.setLongName("long name");
        osd.setShortName("short name");
        osd.setSyndicationRight("syndication right");
        osd.setAdultContent("false");
        osd.setTags("tags");
        osd.getOtherAttributes().put(new QName("otherAttrNs", "otherAttrName"), "otherAttrValue");

        OpenSearchImage image = new OpenSearchImage();
        image.setHeight(new BigInteger("12345"));
        image.setWidth(new BigInteger("67890"));
        image.setType("image type 1");
        image.setValue("image value 1");
        osd.getImage().add(image);

        image = new OpenSearchImage();
        image.setHeight(new BigInteger("67890"));
        image.setWidth(new BigInteger("12345"));
        image.setType("image type 2");
        image.setValue("image value 2");
        osd.getImage().add(image);

        osd.getInputEncoding().add("input encoding 1");
        osd.getInputEncoding().add("input encoding 2");

        osd.getLanguage().add("language 1");
        osd.getLanguage().add("language 2");

        osd.getOutputEncoding().add("output encoding 1");
        osd.getOutputEncoding().add("output encoding 2");

        OpenSearchQuery query = new OpenSearchQuery();
        query.getOtherAttributes().put(new QName("otherAttrNs", "otherAttrName"), "otherAttrValue");
        query.setCount(new BigInteger("1"));
        query.setInputEncoding("input encoding");
        query.setLanguage("language");
        query.setOutputEncoding("output encoding");
        query.setRole("role");
        query.setSearchTerms("search terms");
        query.setStartIndex(new BigInteger("2"));
        query.setStartPage(new BigInteger("3"));
        query.setTotalResults(new BigInteger("4"));
        query.setTitle("title");
        osd.getQuery().add(query);

        OpenSearchUrl url = new OpenSearchUrl();
        url.setTemplate("template");
        url.setType("type");
        osd.getUrl().add(url);

        return osd;
    }

}
