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

package org.apache.wink.common.model.rss;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.apache.wink.common.internal.utils.JAXBUtils;

public class RssTest extends TestCase {
    // References: http://www.rssboard.org/files/rss-2.0-sample.xml and
    // http://www.rssboard.org/files/sample-rss-2.xml

    private static final String RSS_CATEGORY  =
                                                  "\n<category domain=\"Newspapers/Regional/United_States\">Texas</category>\n";
    private static final String RSS_CLOUD     =
                                                  "\n<cloud protocol=\"xml-rpc\" registerProcedure=\"cloud.notify\" path=\"/rpc\" port=\"80\" domain=\"server.example.com\"/>\n";
    private static final String RSS_IMAGE     =
                                                  "\n<image>\n" + "    <url>http://dallas.example.com/masthead.gif</url>\n"
                                                      + "    <title>Dallas Times-Herald</title>\n"
                                                      + "    <link>http://dallas.example.com</link>\n"
                                                      + "    <width>96</width>\n"
                                                      + "    <height>32</height>\n"
                                                      + "    <description>Read the Dallas Times-Herald</description>\n"
                                                      + "</image>\n";
    private static final String RSS_SKIPDAYS  =
                                                  "\n<skipDays>\n" + "    <day>Saturday</day>\n"
                                                      + "    <day>Sunday</day>\n"
                                                      + "</skipDays>\n";
    private static final String RSS_SKIPHOURS =
                                                  "\n<skipHours>\n" + "    <hour>0</hour>\n"
                                                      + "    <hour>1</hour>\n"
                                                      + "    <hour>2</hour>\n"
                                                      + "    <hour>22</hour>\n"
                                                      + "    <hour>23</hour>\n"
                                                      + "</skipHours>\n";
    private static final String RSS_TEXTINPUT =
                                                  "\n<textInput>\n" + "    <title>TextInput Inquiry</title>\n"
                                                      + "    <description>Your aggregator supports the textInput element. What software are you using?</description>\n"
                                                      + "    <name>query</name>\n"
                                                      + "    <link>http://www.cadenhead.org/textinput.php</link>\n"
                                                      + "</textInput>\n";
    private static final String RSS_GUID      =
                                                  "\n<guid isPermaLink=\"false\">tag:dallas.example.com,4131:news</guid>\n";
    private static final String RSS_SOURCE    =
                                                  "\n<source url=\"http://la.example.com/rss.xml\">Los Angeles Herald-Examiner</source>\n";
    private static final String RSS_ENCLOSURE =
                                                  "\n<enclosure type=\"audio/mpeg\" length=\"24986239\" url=\"http://dallas.example.com/joebob_050689.mp3\"/>\n";
    private static final String RSS_ITEM      =
                                                  "\n<item>\n" + "    <title>Joe Bob Goes to the Drive-In</title>\n"
                                                      + "    <link>http://dallas.example.com/1983/05/06/joebob.htm</link>\n"
                                                      + "    <description>I'm headed for France. I wasn't gonna go this year, but then last week &quot;Valley Girl&quot; came out and I said to myself, Joe Bob, you gotta get out of the country for a while.</description>\n"
                                                      + "    <author>jbb@dallas.example.com (Joe Bob Briggs)</author>\n"
                                                      + "    <category>rec.arts.movies.reviews</category>\n"
                                                      + "    <comments>http://dallas.example.com/feedback/1983/06/joebob.htm</comments>\n"
                                                      + "    <enclosure type=\"audio/mpeg\" length=\"24986239\" url=\"http://dallas.example.com/joebob_050689.mp3\"/>\n"
                                                      + "    <guid>http://dallas.example.com/1983/05/06/joebob.htm</guid>\n"
                                                      + "    <pubDate>Fri, 06 May 1983 09:00:00 CST</pubDate>\n"
                                                      + "    <source url=\"http://la.example.com/rss.xml\">Los Angeles Herald-Examiner</source>\n"
                                                      + "</item>\n";
    private static final String RSS_FEED      =
                                                  "\n<rss version=\"2.0\">\n" + "    <channel>\n"
                                                      + "        <title>Liftoff News</title>\n"
                                                      + "        <link>http://liftoff.msfc.nasa.gov/</link>\n"
                                                      + "        <description>Liftoff to Space Exploration.</description>\n"
                                                      + "        <language>en-us</language>\n"
                                                      + "        <managingEditor>editor@example.com</managingEditor>\n"
                                                      + "        <webMaster>webmaster@example.com</webMaster>\n"
                                                      + "        <pubDate>Tue, 10 Jun 2003 04:00:00 GMT</pubDate>\n"
                                                      + "        <lastBuildDate>Tue, 10 Jun 2003 09:41:01 GMT</lastBuildDate>\n"
                                                      + "        <generator>Weblog Editor 2.0</generator>\n"
                                                      + "        <docs>http://blogs.law.harvard.edu/tech/rss</docs>\n"
                                                      + "        <item>\n"
                                                      + "            <title>Star City</title>\n"
                                                      + "            <link>http://liftoff.msfc.nasa.gov/news/2003/news-starcity.asp</link>\n"
                                                      + "            <description>How do Americans get ready to work with Russians aboard the International Space Station? They take a crash course in culture, language and protocol at Russia's &amp;lt;a href=&quot;http://howe.iki.rssi.ru/GCTC/gctc_e.htm&quot;&amp;gt;Star City&amp;lt;/a&amp;gt;.</description>\n"
                                                      + "            <guid>http://liftoff.msfc.nasa.gov/2003/06/03.html#item573</guid>\n"
                                                      + "            <pubDate>Tue, 03 Jun 2003 09:39:21 GMT</pubDate>\n"
                                                      + "        </item>\n"
                                                      + "        <item>\n"
                                                      + "            <description>Sky watchers in Europe, Asia, and parts of Alaska and Canada will experience a &amp;lt;a href=&quot;http://science.nasa.gov/headlines/y2003/30may_solareclipse.htm&quot;&amp;gt;partial eclipse of the Sun&amp;lt;/a&amp;gt; on Saturday, May 31st.</description>\n"
                                                      + "            <guid>http://liftoff.msfc.nasa.gov/2003/05/30.html#item572</guid>\n"
                                                      + "            <pubDate>Fri, 30 May 2003 11:06:42 GMT</pubDate>\n"
                                                      + "        </item>\n"
                                                      + "        <item>\n"
                                                      + "            <title>The Engine That Does More</title>\n"
                                                      + "            <link>http://liftoff.msfc.nasa.gov/news/2003/news-VASIMR.asp</link>\n"
                                                      + "            <description>Before man travels to Mars, NASA hopes to design new engines that will let us fly through the Solar System more quickly.  The proposed VASIMR engine would do that.</description>\n"
                                                      + "            <guid>http://liftoff.msfc.nasa.gov/2003/05/27.html#item571</guid>\n"
                                                      + "            <pubDate>Tue, 27 May 2003 08:37:32 GMT</pubDate>\n"
                                                      + "        </item>\n"
                                                      + "        <item>\n"
                                                      + "            <title>Astronauts' Dirty Laundry</title>\n"
                                                      + "            <link>http://liftoff.msfc.nasa.gov/news/2003/news-laundry.asp</link>\n"
                                                      + "            <description>Compared to earlier spacecraft, the International Space Station has many luxuries, but laundry facilities are not one of them.  Instead, astronauts have other options.</description>\n"
                                                      + "            <guid>http://liftoff.msfc.nasa.gov/2003/05/20.html#item570</guid>\n"
                                                      + "            <pubDate>Tue, 20 May 2003 08:56:02 GMT</pubDate>\n"
                                                      + "        </item>\n"
                                                      + "    </channel>\n"
                                                      + "</rss>\n";

    private static JAXBContext  ctx;

    static {
        try {
            ctx = JAXBContext.newInstance(RssFeed.class, RssChannel.class, RssItem.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public void testRssCategoryMarshal() throws JAXBException {
        RssCategory category = new RssCategory();
        category.setDomain("Newspapers/Regional/United_States");
        category.setContent("Texas");

        Marshaller m = JAXBUtils.createMarshaller(ctx);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.marshal(new ObjectFactory().createRssCategory(category), os);
        assertEquals(RSS_CATEGORY, os.toString());
    }

    public void testRssCategoryUnmarshal() throws JAXBException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = u.unmarshal(new StringReader(RSS_CATEGORY));
        assertNotNull(element);
        if (element instanceof JAXBElement<?>) {
            element = ((JAXBElement<?>)element).getValue();
        }
        assertTrue(element instanceof RssCategory);
        RssCategory category = (RssCategory)element;
        assertNotNull(category);
        assertEquals("Newspapers/Regional/United_States", category.getDomain());
        assertEquals("Texas", category.getContent());
    }

    public void testRssCloudMarshal() throws JAXBException {
        RssCloud cloud = new RssCloud();
        cloud.setDomain("server.example.com");
        cloud.setPath("/rpc");
        cloud.setPort(80);
        cloud.setProtocol("xml-rpc");
        cloud.setRegisterProcedure("cloud.notify");

        Marshaller m = JAXBUtils.createMarshaller(ctx);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.marshal(new ObjectFactory().createRssCloud(cloud), os);
        assertEquals(RSS_CLOUD, os.toString());
    }

    public void testRssCloudUnmarshal() throws JAXBException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = u.unmarshal(new StringReader(RSS_CLOUD));
        assertNotNull(element);
        if (element instanceof JAXBElement<?>) {
            element = ((JAXBElement<?>)element).getValue();
        }
        assertTrue(element instanceof RssCloud);
        RssCloud cloud = (RssCloud)element;
        assertNotNull(cloud);
        assertEquals("server.example.com", cloud.getDomain());
        assertEquals("/rpc", cloud.getPath());
        assertEquals(80, cloud.getPort());
        assertEquals("xml-rpc", cloud.getProtocol());
        assertEquals("cloud.notify", cloud.getRegisterProcedure());
    }

    public void testRssImageMarshal() throws JAXBException {
        RssImage image = new RssImage();
        image.setLink("http://dallas.example.com");
        image.setTitle("Dallas Times-Herald");
        image.setUrl("http://dallas.example.com/masthead.gif");
        image.setDescription("Read the Dallas Times-Herald");
        image.setHeight(32);
        image.setWidth(96);

        Marshaller m = JAXBUtils.createMarshaller(ctx);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.marshal(new ObjectFactory().createRssImage(image), os);
        assertEquals(RSS_IMAGE, os.toString());
    }

    public void testRssImageUnmarshal() throws JAXBException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = u.unmarshal(new StringReader(RSS_IMAGE));
        assertNotNull(element);
        if (element instanceof JAXBElement<?>) {
            element = ((JAXBElement<?>)element).getValue();
        }
        assertTrue(element instanceof RssImage);
        RssImage image = (RssImage)element;
        assertNotNull(image);
        assertEquals("http://dallas.example.com", image.getLink());
        assertEquals("Dallas Times-Herald", image.getTitle());
        assertEquals("http://dallas.example.com/masthead.gif", image.getUrl());
        assertEquals(new Integer(32), image.getHeight());
        assertEquals(new Integer(96), image.getWidth());
    }

    public void testRssSkipDaysMarshal() throws JAXBException {
        RssSkipDays skipDays = new RssSkipDays();
        skipDays.getDays().add("Saturday");
        skipDays.getDays().add("Sunday");

        Marshaller m = JAXBUtils.createMarshaller(ctx);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.marshal(new ObjectFactory().createRssSkipDays(skipDays), os);
        assertEquals(RSS_SKIPDAYS, os.toString());
    }

    public void testRssSkipDaysUnmarshal() throws JAXBException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = u.unmarshal(new StringReader(RSS_SKIPDAYS));
        assertNotNull(element);
        if (element instanceof JAXBElement<?>) {
            element = ((JAXBElement<?>)element).getValue();
        }
        assertTrue(element instanceof RssSkipDays);
        RssSkipDays skipDays = (RssSkipDays)element;
        assertNotNull(skipDays);
        assertEquals("Saturday", skipDays.getDays().get(0));
        assertEquals("Sunday", skipDays.getDays().get(1));
    }

    public void testRssSkipHoursMarshal() throws JAXBException {
        RssSkipHours skipHours = new RssSkipHours();
        skipHours.getHours().add("0");
        skipHours.getHours().add("1");
        skipHours.getHours().add("2");
        skipHours.getHours().add("22");
        skipHours.getHours().add("23");

        Marshaller m = JAXBUtils.createMarshaller(ctx);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.marshal(new ObjectFactory().createRssSkipHours(skipHours), os);
        assertEquals(RSS_SKIPHOURS, os.toString());
    }

    public void testRssSkipHoursUnmarshal() throws JAXBException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = u.unmarshal(new StringReader(RSS_SKIPHOURS));
        assertNotNull(element);
        if (element instanceof JAXBElement<?>) {
            element = ((JAXBElement<?>)element).getValue();
        }
        assertTrue(element instanceof RssSkipHours);
        RssSkipHours skipHours = (RssSkipHours)element;
        assertNotNull(skipHours);
        assertEquals("0", skipHours.getHours().get(0));
        assertEquals("1", skipHours.getHours().get(1));
        assertEquals("2", skipHours.getHours().get(2));
        assertEquals("22", skipHours.getHours().get(3));
        assertEquals("23", skipHours.getHours().get(4));
    }

    public void testRssTextInputMarshal() throws JAXBException {
        RssTextInput textInput = new RssTextInput();
        textInput
            .setDescription("Your aggregator supports the textInput element. What software are you using?");
        textInput.setLink("http://www.cadenhead.org/textinput.php");
        textInput.setName("query");
        textInput.setTitle("TextInput Inquiry");

        Marshaller m = JAXBUtils.createMarshaller(ctx);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.marshal(new ObjectFactory().createRssTextInput(textInput), os);
        assertEquals(RSS_TEXTINPUT, os.toString());
    }

    public void testRssTextInputUnmarshal() throws JAXBException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = u.unmarshal(new StringReader(RSS_TEXTINPUT));
        assertNotNull(element);
        if (element instanceof JAXBElement<?>) {
            element = ((JAXBElement<?>)element).getValue();
        }
        assertTrue(element instanceof RssTextInput);
        RssTextInput textInput = (RssTextInput)element;
        assertNotNull(textInput);
        assertEquals("Your aggregator supports the textInput element. What software are you using?",
                     textInput.getDescription());
        assertEquals("http://www.cadenhead.org/textinput.php", textInput.getLink());
        assertEquals("query", textInput.getName());
        assertEquals("TextInput Inquiry", textInput.getTitle());
    }

    public void testRssGuidMarshal() throws JAXBException {
        RssGuid guid = new RssGuid();
        guid.setIsPermaLink(false);
        guid.setContent("tag:dallas.example.com,4131:news");

        Marshaller m = JAXBUtils.createMarshaller(ctx);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.marshal(new ObjectFactory().createRssGuid(guid), os);
        assertEquals(RSS_GUID, os.toString());
    }

    public void testRssGuidUnmarshal() throws JAXBException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = u.unmarshal(new StringReader(RSS_GUID));
        assertNotNull(element);
        if (element instanceof JAXBElement<?>) {
            element = ((JAXBElement<?>)element).getValue();
        }
        assertTrue(element instanceof RssGuid);
        RssGuid guid = (RssGuid)element;
        assertNotNull(guid);
        assertFalse(guid.isIsPermaLink());
        assertEquals("tag:dallas.example.com,4131:news", guid.getContent());
    }

    public void testRssSourceMarshal() throws JAXBException {
        RssSource source = new RssSource();
        source.setUrl("http://la.example.com/rss.xml");
        source.setContent("Los Angeles Herald-Examiner");

        Marshaller m = JAXBUtils.createMarshaller(ctx);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.marshal(new ObjectFactory().createRssSource(source), os);
        assertEquals(RSS_SOURCE, os.toString());
    }

    public void testRssSourceUnmarshal() throws JAXBException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = u.unmarshal(new StringReader(RSS_SOURCE));
        assertNotNull(element);
        if (element instanceof JAXBElement<?>) {
            element = ((JAXBElement<?>)element).getValue();
        }
        assertTrue(element instanceof RssSource);
        RssSource source = (RssSource)element;
        assertNotNull(source);
        assertEquals("http://la.example.com/rss.xml", source.getUrl());
        assertEquals("Los Angeles Herald-Examiner", source.getContent());
    }

    public void testRssEnclosureMarshal() throws JAXBException {
        RssEnclosure enclosure = new RssEnclosure();
        enclosure.setLength("24986239");
        enclosure.setType("audio/mpeg");
        enclosure.setUrl("http://dallas.example.com/joebob_050689.mp3");

        Marshaller m = JAXBUtils.createMarshaller(ctx);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.marshal(new ObjectFactory().createRssEnclosure(enclosure), os);
        assertEquals(RSS_ENCLOSURE, os.toString());
    }

    public void testRssEnclosureUnmarshal() throws JAXBException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = u.unmarshal(new StringReader(RSS_ENCLOSURE));
        assertNotNull(element);
        if (element instanceof JAXBElement<?>) {
            element = ((JAXBElement<?>)element).getValue();
        }
        assertTrue(element instanceof RssEnclosure);
        RssEnclosure enclosure = (RssEnclosure)element;
        assertNotNull(enclosure);
        assertEquals("24986239", enclosure.getLength());
        assertEquals("audio/mpeg", enclosure.getType());
        assertEquals("http://dallas.example.com/joebob_050689.mp3", enclosure.getUrl());
    }

    public void testRssItemMarshal() throws JAXBException {
        RssItem item = new RssItem();

        item.setAuthor("jbb@dallas.example.com (Joe Bob Briggs)");

        RssCategory category = new RssCategory();
        category.setContent("rec.arts.movies.reviews");
        item.getCategories().add(category);

        item.setComments("http://dallas.example.com/feedback/1983/06/joebob.htm");

        item
            .setDescription("I'm headed for France. I wasn't gonna go this year, but then last week \"Valley Girl\" came out and I said to myself, Joe Bob, you gotta get out of the country for a while.");

        RssEnclosure enclosure = new RssEnclosure();
        enclosure.setLength("24986239");
        enclosure.setType("audio/mpeg");
        enclosure.setUrl("http://dallas.example.com/joebob_050689.mp3");
        item.setEnclosure(enclosure);

        RssGuid guid = new RssGuid();
        guid.setContent("http://dallas.example.com/1983/05/06/joebob.htm");
        item.setGuid(guid);

        item.setLink("http://dallas.example.com/1983/05/06/joebob.htm");

        item.setPubDate("Fri, 06 May 1983 09:00:00 CST");

        RssSource source = new RssSource();
        source.setUrl("http://la.example.com/rss.xml");
        source.setContent("Los Angeles Herald-Examiner");
        item.setSource(source);

        item.setTitle("Joe Bob Goes to the Drive-In");

        Marshaller m = JAXBUtils.createMarshaller(ctx);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.marshal(new ObjectFactory().createRssItem(item), os);
        assertEquals(RSS_ITEM, os.toString());
    }

    public void testRssItemUnmarshal() throws JAXBException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = u.unmarshal(new StringReader(RSS_ITEM));
        assertNotNull(element);
        if (element instanceof JAXBElement<?>) {
            element = ((JAXBElement<?>)element).getValue();
        }
        assertTrue(element instanceof RssItem);
        RssItem item = (RssItem)element;
        assertNotNull(item);
        assertEquals("jbb@dallas.example.com (Joe Bob Briggs)", item.getAuthor());
        assertNotNull(item.getCategories());
        assertEquals(1, item.getCategories().size());
        assertNotNull(item.getCategories().get(0));
        assertEquals("rec.arts.movies.reviews", item.getCategories().get(0).getContent());
        assertEquals("http://dallas.example.com/feedback/1983/06/joebob.htm", item.getComments());
        assertEquals("I'm headed for France. I wasn't gonna go this year, but then last week \"Valley Girl\" came out and I said to myself, Joe Bob, you gotta get out of the country for a while.",
                     item.getDescription());
        assertNotNull(item.getEnclosure());
        assertEquals("24986239", item.getEnclosure().getLength());
        assertEquals("audio/mpeg", item.getEnclosure().getType());
        assertEquals("http://dallas.example.com/joebob_050689.mp3", item.getEnclosure().getUrl());
        assertNotNull(item.getGuid());
        assertEquals("http://dallas.example.com/1983/05/06/joebob.htm", item.getGuid().getContent());
        assertEquals("http://dallas.example.com/1983/05/06/joebob.htm", item.getLink());
        assertEquals("Fri, 06 May 1983 09:00:00 CST", item.getPubDate());
        assertNotNull(item.getSource());
        assertEquals("http://la.example.com/rss.xml", item.getSource().getUrl());
        assertEquals("Los Angeles Herald-Examiner", item.getSource().getContent());
        assertEquals("Joe Bob Goes to the Drive-In", item.getTitle());
    }

    public void testRssFeedMarshal() throws JAXBException {
        RssFeed rssFeed = new RssFeed();
        rssFeed.setVersion("2.0");

        RssChannel rssChannel = new RssChannel();
        rssChannel.setTitle("Liftoff News");
        rssChannel.setLink("http://liftoff.msfc.nasa.gov/");
        rssChannel.setDescription("Liftoff to Space Exploration.");
        rssChannel.setLanguage("en-us");
        rssChannel.setPubDate("Tue, 10 Jun 2003 04:00:00 GMT");
        rssChannel.setLastBuildDate("Tue, 10 Jun 2003 09:41:01 GMT");
        rssChannel.setDocs("http://blogs.law.harvard.edu/tech/rss");
        rssChannel.setGenerator("Weblog Editor 2.0");
        rssChannel.setManagingEditor("editor@example.com");
        rssChannel.setWebMaster("webmaster@example.com");

        RssItem item1 = new RssItem();
        item1.setTitle("Star City");
        item1.setLink("http://liftoff.msfc.nasa.gov/news/2003/news-starcity.asp");
        item1
            .setDescription("How do Americans get ready to work with Russians aboard the International Space Station? They take a crash course in culture, language and protocol at Russia's &lt;a href=\"http://howe.iki.rssi.ru/GCTC/gctc_e.htm\"&gt;Star City&lt;/a&gt;.");
        item1.setPubDate("Tue, 03 Jun 2003 09:39:21 GMT");
        RssGuid guid1 = new RssGuid();
        guid1.setContent("http://liftoff.msfc.nasa.gov/2003/06/03.html#item573");
        item1.setGuid(guid1);

        RssItem item2 = new RssItem();
        item2
            .setDescription("Sky watchers in Europe, Asia, and parts of Alaska and Canada will experience a &lt;a href=\"http://science.nasa.gov/headlines/y2003/30may_solareclipse.htm\"&gt;partial eclipse of the Sun&lt;/a&gt; on Saturday, May 31st.");
        item2.setPubDate("Fri, 30 May 2003 11:06:42 GMT");
        RssGuid guid2 = new RssGuid();
        guid2.setContent("http://liftoff.msfc.nasa.gov/2003/05/30.html#item572");
        item2.setGuid(guid2);

        RssItem item3 = new RssItem();
        item3.setTitle("The Engine That Does More");
        item3.setLink("http://liftoff.msfc.nasa.gov/news/2003/news-VASIMR.asp");
        item3
            .setDescription("Before man travels to Mars, NASA hopes to design new engines that will let us fly through the Solar System more quickly.  The proposed VASIMR engine would do that.");
        item3.setPubDate("Tue, 27 May 2003 08:37:32 GMT");
        RssGuid guid3 = new RssGuid();
        guid3.setContent("http://liftoff.msfc.nasa.gov/2003/05/27.html#item571");
        item3.setGuid(guid3);

        RssItem item4 = new RssItem();
        item4.setTitle("Astronauts' Dirty Laundry");
        item4.setLink("http://liftoff.msfc.nasa.gov/news/2003/news-laundry.asp");
        item4
            .setDescription("Compared to earlier spacecraft, the International Space Station has many luxuries, but laundry facilities are not one of them.  Instead, astronauts have other options.");
        item4.setPubDate("Tue, 20 May 2003 08:56:02 GMT");
        RssGuid guid4 = new RssGuid();
        guid4.setContent("http://liftoff.msfc.nasa.gov/2003/05/20.html#item570");
        item4.setGuid(guid4);

        rssChannel.getItems().add(item1);
        rssChannel.getItems().add(item2);
        rssChannel.getItems().add(item3);
        rssChannel.getItems().add(item4);
        rssFeed.setChannel(rssChannel);

        Marshaller m = JAXBUtils.createMarshaller(ctx);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.marshal(rssFeed, os);
        assertEquals(RSS_FEED, os.toString());
    }

    public void testRssFeedUnmarshal() throws JAXBException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = u.unmarshal(new StringReader(RSS_FEED));
        assertNotNull(element);
        if (element instanceof JAXBElement<?>) {
            element = ((JAXBElement<?>)element).getValue();
        }
        assertTrue(element instanceof RssFeed);

        RssFeed feed = (RssFeed)element;
        assertNotNull(feed);
        assertEquals("2.0", feed.getVersion());
        RssChannel channel = feed.getChannel();
        assertNotNull(channel);
        assertEquals("Liftoff News", channel.getTitle());
        assertEquals("http://liftoff.msfc.nasa.gov/", channel.getLink());
        assertEquals("Liftoff to Space Exploration.", channel.getDescription());
        assertEquals("en-us", channel.getLanguage());
        assertEquals("Tue, 10 Jun 2003 04:00:00 GMT", channel.getPubDate());
        assertEquals("Tue, 10 Jun 2003 09:41:01 GMT", channel.getLastBuildDate());
        assertEquals("http://blogs.law.harvard.edu/tech/rss", channel.getDocs());
        assertEquals("Weblog Editor 2.0", channel.getGenerator());
        assertEquals("editor@example.com", channel.getManagingEditor());
        assertEquals("webmaster@example.com", channel.getWebMaster());
        assertEquals(4, channel.getItems().size());

        RssItem item1 = channel.getItems().get(0);
        assertEquals("Star City", item1.getTitle());
        assertEquals("http://liftoff.msfc.nasa.gov/news/2003/news-starcity.asp", item1.getLink());
        assertEquals("How do Americans get ready to work with Russians aboard the International Space Station? They take a crash course in culture, language and protocol at Russia's &lt;a href=\"http://howe.iki.rssi.ru/GCTC/gctc_e.htm\"&gt;Star City&lt;/a&gt;.",
                     item1.getDescription());
        assertEquals("Tue, 03 Jun 2003 09:39:21 GMT", item1.getPubDate());
        assertEquals("http://liftoff.msfc.nasa.gov/2003/06/03.html#item573", item1.getGuid()
            .getContent());

        RssItem item2 = channel.getItems().get(1);
        assertEquals("Sky watchers in Europe, Asia, and parts of Alaska and Canada will experience a &lt;a href=\"http://science.nasa.gov/headlines/y2003/30may_solareclipse.htm\"&gt;partial eclipse of the Sun&lt;/a&gt; on Saturday, May 31st.",
                     item2.getDescription());
        assertEquals("Fri, 30 May 2003 11:06:42 GMT", item2.getPubDate());
        assertEquals("http://liftoff.msfc.nasa.gov/2003/05/30.html#item572", item2.getGuid()
            .getContent());

        RssItem item3 = channel.getItems().get(2);
        assertEquals("The Engine That Does More", item3.getTitle());
        assertEquals("http://liftoff.msfc.nasa.gov/news/2003/news-VASIMR.asp", item3.getLink());
        assertEquals("Before man travels to Mars, NASA hopes to design new engines that will let us fly through the Solar System more quickly.  The proposed VASIMR engine would do that.",
                     item3.getDescription());
        assertEquals("Tue, 27 May 2003 08:37:32 GMT", item3.getPubDate());
        assertEquals("http://liftoff.msfc.nasa.gov/2003/05/27.html#item571", item3.getGuid()
            .getContent());

        RssItem item4 = channel.getItems().get(3);
        assertEquals("Astronauts' Dirty Laundry", item4.getTitle());
        assertEquals("http://liftoff.msfc.nasa.gov/news/2003/news-laundry.asp", item4.getLink());
        assertEquals("Compared to earlier spacecraft, the International Space Station has many luxuries, but laundry facilities are not one of them.  Instead, astronauts have other options.",
                     item4.getDescription());
        assertEquals("Tue, 20 May 2003 08:56:02 GMT", item4.getPubDate());
        assertEquals("http://liftoff.msfc.nasa.gov/2003/05/20.html#item570", item4.getGuid()
            .getContent());
    }
}
