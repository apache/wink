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

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.apache.wink.common.internal.utils.JAXBUtils;
import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.common.model.synd.SyndEntry;
import org.apache.wink.common.model.synd.SyndFeed;

public class RssFeedToSyndFeedTest extends TestCase {
    // References: http://www.rssboard.org/files/rss-2.0-sample.xml and
    // http://www.rssboard.org/files/sample-rss-2.xml

    private static final String FEED_TITLE              = "Liftoff News";
    private static final String FEED_LINK               = "http://liftoff.msfc.nasa.gov/";
    private static final String FEED_DESCRIPTION        = "Liftoff to Space Exploration.";
    private static final String FEED_LANGUAGE           = "en-us";
    private static final String FEED_COPYRIGHT          = "Copyright 2002, Spartanburg Herald-Journal";
    private static final String FEED_EDITOR             = "editor@example.com";
    private static final String FEED_LASTBUILDDATE      = "Tue, 10 Jun 2003 09:41:01 GMT";
    private static final String FEED_LASTBUILDDATE_ATOM = "2003-06-10T09:41:01Z";
    private static final String FEED_CTG1_DOMAIN        = "http://www.fool.com/cusips";
    private static final String FEED_CTG1_VALUE         = "MSFT";
    private static final String FEED_CTG2_DOMAIN        = "http://www.fool.com/musips";
    private static final String FEED_CTG2_VALUE         = "MOTO";
    private static final String FEED_GENERATOR          = "Weblog Editor 2.0";
    private static final String FEED_IMAGE_URL          = "http://liftoff.msfc.nasa.gov/news.gif";

    private static final String ITEM_TITLE              = "Star City";
    private static final String ITEM_LINK               = "http://liftoff.msfc.nasa.gov/news/2003/news-starcity.asp";
    private static final String ITEM_DESCRIPTION        = "How do Americans get ready to work with Russians aboard the International Space Station? They take a crash course in culture, language and protocol at Russia's Star City.";
    private static final String ITEM_AUTHOR             = "author1@rssboard.org";
    private static final String ITEM_ENCLOSURE_URL      = "http://www.scripting.com/mp3s/weatherReportSuite.mp3";
    private static final String ITEM_ENCLOSURE_LENGTH   = "12216320";
    private static final String ITEM_ENCLOSURE_TYPE     = "audio/mpeg";
    private static final String ITEM_GUID               = "http://liftoff.msfc.nasa.gov/2003/06/03.html#item573";
    private static final String ITEM_PUBDATE            = "Tue, 03 Jun 2003 09:39:21 GMT";
    private static final String ITEM_PUBDATE_ATOM       = "2003-06-03T09:39:21Z";

    private static final String RSS_FEED                =
                                                            "<?xml version=\"1.0\"?>" + "<rss version=\"2.0\">\n"
                                                      + "    <channel>\n"
                                                      + "        <title>" + FEED_TITLE + "</title>\n"
                                                      + "        <link>" + FEED_LINK + "</link>\n"
                                                      + "        <description>" + FEED_DESCRIPTION + "</description>\n"
                                                      + "        <language>"+ FEED_LANGUAGE + "</language>\n"
                                                      + "        <copyright>" + FEED_COPYRIGHT + "</copyright>\n"
                                                      + "        <managingEditor>" + FEED_EDITOR + "</managingEditor>\n"
                                                      + "        <webMaster>webmaster@example.com</webMaster>\n"
                                                      + "        <pubDate>Tue, 10 Jun 2003 04:00:00 GMT</pubDate>\n"
                                                      + "        <lastBuildDate>" + FEED_LASTBUILDDATE + "</lastBuildDate>\n"
                                                      + "        <category domain=\"" + FEED_CTG1_DOMAIN + "\">" + FEED_CTG1_VALUE + "</category>\n"
                                                      + "        <category domain=\"" + FEED_CTG2_DOMAIN + "\">" + FEED_CTG2_VALUE + "</category>\n"
                                                      + "        <generator>" + FEED_GENERATOR + "</generator>\n"
                                                      + "        <image>\n"
                                                      + "            <url>" + FEED_IMAGE_URL + "</url>\n"
                                                      + "            <title>Litoff News</title>\n"
                                                      + "            <link>http://liftoff.msfc.nasa.gov/</link>\n"
                                                      + "            <width>100</width>\n"
                                                      + "            <height>100</height>\n"
                                                      + "            <description>News</description>\n"
                                                      + "        </image>\n"
                                                      + "        <docs>http://blogs.law.harvard.edu/tech/rss</docs>\n"
                                                      + "        <item>\n"
                                                      + "            <title>" + ITEM_TITLE + "</title>\n"
                                                      + "            <link>" + ITEM_LINK + "</link>\n"
                                                      + "            <description>" + ITEM_DESCRIPTION + "</description>\n"
                                                      + "            <author>" + ITEM_AUTHOR + "</author>\n"
                                                      + "            <category domain=\"" + FEED_CTG1_DOMAIN + "\">" + FEED_CTG1_VALUE + "</category>\n"
                                                      + "            <category domain=\"" + FEED_CTG2_DOMAIN + "\">" + FEED_CTG2_VALUE + "</category>\n"
                                                      + "            <enclosure url=\"" + ITEM_ENCLOSURE_URL + "\" length=\"" + ITEM_ENCLOSURE_LENGTH + "\" type=\"" + ITEM_ENCLOSURE_TYPE + "\" />\n"
                                                      + "            <guid>" + ITEM_GUID + "</guid>\n"
                                                      + "            <pubDate>" + ITEM_PUBDATE + "</pubDate>\n"
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

    private static final String ATOM_FEED             = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" 
                                                      + "<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:ns2=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:ns3=\"http://www.w3.org/1999/xhtml\" xml:lang=\"" + FEED_LANGUAGE + "\">\n"
                                                      + "    <title type=\"text\">" + FEED_TITLE + "</title>\n"
                                                      + "    <link href=\"" + FEED_LINK + "\" rel=\"alternate\" />\n"
                                                      + "    <subtitle type=\"text\">" + FEED_DESCRIPTION + "</subtitle>\n"
                                                      + "    <rights type=\"text\">" + FEED_COPYRIGHT + "</rights>\n"
                                                      + "    <author>\n"
                                                      + "        <email>" + FEED_EDITOR + "</email>\n"
                                                      + "        <name>editor</name>\n"
                                                      + "    </author>\n"
                                                      + "    <updated>" + FEED_LASTBUILDDATE_ATOM + "</updated>\n"
                                                      + "    <category label=\"" + FEED_CTG1_VALUE + "\" scheme=\"" + FEED_CTG1_DOMAIN + "\"/>\n"
                                                      + "    <category label=\"" + FEED_CTG2_VALUE + "\" scheme=\"" + FEED_CTG2_DOMAIN + "\"/>\n"
                                                      + "    <generator>" + FEED_GENERATOR + "</generator>\n"
                                                      + "    <logo>" + FEED_IMAGE_URL + "</logo>\n"
                                                      + "    <entry>\n"
                                                      + "        <title type=\"text\">" + ITEM_TITLE + "</title>\n"
                                                      + "        <link href=\"" + ITEM_LINK + "\" rel=\"alternate\"/>\n"
                                                      + "        <summary type=\"text\">" + ITEM_DESCRIPTION + "</summary>\n"
                                                      + "        <author>\n"
                                                      + "            <email>" + ITEM_AUTHOR + "</email>\n"
                                                      + "            <name>author1</name>\n"
                                                      + "        </author>\n"
                                                      + "        <category label=\"" + FEED_CTG1_VALUE + "\" scheme=\"" + FEED_CTG1_DOMAIN + "\"/>\n"
                                                      + "        <category label=\"" + FEED_CTG2_VALUE + "\" scheme=\"" + FEED_CTG2_DOMAIN + "\"/>\n"
                                                      + "        <link href=\"" + ITEM_ENCLOSURE_URL + "\" type=\"" + ITEM_ENCLOSURE_TYPE + "\" rel=\"enclosure\" length=\"" + ITEM_ENCLOSURE_LENGTH + "\"/>\n"
                                                      + "        <id>" + ITEM_GUID + "</id>\n"
                                                      + "        <published>" + ITEM_PUBDATE_ATOM + "</published>\n"
                                                      + "    </entry>\n"
                                                      + "    <entry>\n"
                                                      + "        <id>http://liftoff.msfc.nasa.gov/2003/05/30.html#item572</id>\n"
                                                      + "        <summary type=\"text\">Sky watchers in Europe, Asia, and parts of Alaska and Canada will experience a &lt;a href=&quot;http://science.nasa.gov/headlines/y2003/30may_solareclipse.htm&quot;&gt;partial eclipse of the Sun&lt;/a&gt; on Saturday, May 31st.</summary>\n"
                                                      + "        <published>2003-05-30T16:36:42.000+05:30</published>\n"
                                                      + "    </entry>\n"
                                                      + "    <entry>\n"
                                                      + "        <id>http://liftoff.msfc.nasa.gov/2003/05/27.html#item571</id>\n"
                                                      + "        <title type=\"text\">The Engine That Does More</title>\n"
                                                      + "        <summary type=\"text\">Before man travels to Mars, NASA hopes to design new engines that will let us fly through the Solar System more quickly.  The proposed VASIMR engine would do that.</summary>\n"
                                                      + "        <published>2003-05-27T14:07:32.000+05:30</published>\n"
                                                      + "        <link href=\"http://liftoff.msfc.nasa.gov/news/2003/news-VASIMR.asp\" rel=\"alternate\"/>\n"
                                                      + "    </entry>\n"
                                                      + "    <entry>\n"
                                                      + "        <id>http://liftoff.msfc.nasa.gov/2003/05/20.html#item570</id>\n"
                                                      + "        <title type=\"text\">Astronauts' Dirty Laundry</title>\n"
                                                      + "        <summary type=\"text\">Compared to earlier spacecraft, the International Space Station has many luxuries, but laundry facilities are not one of them.  Instead, astronauts have other options.</summary>\n"
                                                      + "        <published>2003-05-20T14:26:02.000+05:30</published>\n"
                                                      + "        <link href=\"http://liftoff.msfc.nasa.gov/news/2003/news-laundry.asp\" rel=\"alternate\"/>\n"
                                                      + "    </entry>\n"
                                                      + "</feed>\n";

    private static JAXBContext  ctx;

    static {
        try {
            ctx = JAXBContext.newInstance(RssFeed.class, RssChannel.class, RssItem.class, AtomFeed.class, AtomEntry.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public void testRssFeedToSyndFeed() throws JAXBException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = u.unmarshal(new StringReader(RSS_FEED));
        assertNotNull(element);
        if (element instanceof JAXBElement<?>) {
            element = ((JAXBElement<?>)element).getValue();
        }
        assertTrue(element instanceof RssFeed);
        RssFeed rssFeed = (RssFeed)element;
        assertNotNull(rssFeed);

        // Convert RssFeed into SyndFeed
        SyndFeed syndFeed = new SyndFeed();
        syndFeed = rssFeed.toSynd(syndFeed);
        assertNotNull(syndFeed);

        assertNotNull(syndFeed.getTitle());
        assertEquals(FEED_TITLE, syndFeed.getTitle().getValue());

        assertNotNull(syndFeed.getLink("alternate"));
        assertEquals(FEED_LINK, syndFeed.getLink("alternate").getHref());

        assertNotNull(syndFeed.getSubtitle());
        assertEquals(FEED_DESCRIPTION, syndFeed.getSubtitle().getValue());

        assertNotNull(syndFeed.getLang());
        assertEquals(FEED_LANGUAGE, syndFeed.getLang());

        assertNotNull(syndFeed.getRights());
        assertEquals(FEED_COPYRIGHT, syndFeed.getRights().getValue());

        assertTrue(syndFeed.getAuthors().size() > 0);
        assertEquals(FEED_EDITOR, syndFeed.getAuthors().get(0).getEmail());

        assertNotNull(syndFeed.getUpdated());
        assertEquals(RssChannel.convertRssDateToJavaDate(FEED_LASTBUILDDATE), syndFeed.getUpdated());

        assertTrue(syndFeed.getCategories().size() == 2);
        assertEquals(FEED_CTG1_DOMAIN, syndFeed.getCategories().get(0).getScheme());
        assertEquals(FEED_CTG1_VALUE, syndFeed.getCategories().get(0).getLabel());
        assertEquals(FEED_CTG2_DOMAIN, syndFeed.getCategories().get(1).getScheme());
        assertEquals(FEED_CTG2_VALUE, syndFeed.getCategories().get(1).getLabel());

        assertNotNull(syndFeed.getGenerator());
        assertEquals(FEED_GENERATOR, syndFeed.getGenerator().getValue());

        assertNotNull(syndFeed.getLogo());
        assertEquals(FEED_IMAGE_URL, syndFeed.getLogo());

        assertTrue(syndFeed.getEntries().size() > 0);
        SyndEntry syndEntry = syndFeed.getEntries().get(0);
        assertNotNull(syndEntry);

        assertNotNull(syndEntry.getTitle());
        assertEquals(ITEM_TITLE, syndEntry.getTitle().getValue());

        assertNotNull(syndEntry.getLink("alternate"));
        assertEquals(ITEM_LINK, syndEntry.getLink("alternate").getHref());

        assertNotNull(syndEntry.getSummary());
        assertEquals(ITEM_DESCRIPTION, syndEntry.getSummary().getValue());

        assertTrue(syndEntry.getAuthors().size() > 0);
        assertEquals(ITEM_AUTHOR, syndEntry.getAuthors().get(0).getEmail());

        assertTrue(syndEntry.getCategories().size() == 2);
        assertEquals(FEED_CTG1_DOMAIN, syndEntry.getCategories().get(0).getScheme());
        assertEquals(FEED_CTG1_VALUE, syndEntry.getCategories().get(0).getLabel());
        assertEquals(FEED_CTG2_DOMAIN, syndEntry.getCategories().get(1).getScheme());
        assertEquals(FEED_CTG2_VALUE, syndEntry.getCategories().get(1).getLabel());

        assertNotNull(syndEntry.getLink("enclosure"));
        assertEquals(ITEM_ENCLOSURE_URL, syndEntry.getLink("enclosure").getHref());
        assertEquals(ITEM_ENCLOSURE_LENGTH, syndEntry.getLink("enclosure").getLength());
        assertEquals(ITEM_ENCLOSURE_TYPE, syndEntry.getLink("enclosure").getType());

        assertNotNull(syndEntry.getId());
        assertEquals(ITEM_GUID, syndEntry.getId());

        assertNotNull(syndEntry.getPublished());
        assertEquals(RssChannel.convertRssDateToJavaDate(ITEM_PUBDATE), syndEntry.getPublished());
    }

    public void testSyndFeedToRss() throws JAXBException {
        Unmarshaller u = JAXBUtils.createUnmarshaller(ctx);
        Object element = u.unmarshal(new StringReader(ATOM_FEED));
        assertNotNull(element);
        if (element instanceof JAXBElement<?>) {
            element = ((JAXBElement<?>)element).getValue();
        }
        assertTrue(element instanceof AtomFeed);
        SyndFeed syndFeed1 = new SyndFeed();
        syndFeed1 = ((AtomFeed)element).toSynd(syndFeed1);
        assertNotNull(syndFeed1);
 
        // Convert SyndFeed into RssFeed
        RssFeed rssFeed1 = new RssFeed(syndFeed1);
        RssChannel rssChannel = rssFeed1.getChannel();
        assertNotNull(rssChannel);

        assertNotNull(rssChannel.getTitle());
        assertEquals(FEED_TITLE, rssChannel.getTitle());

        assertNotNull(rssChannel.getLink());
        assertEquals(FEED_LINK, rssChannel.getLink());

        assertNotNull(rssChannel.getDescription());
        assertEquals(FEED_DESCRIPTION, rssChannel.getDescription());

        assertNotNull(rssChannel.getLanguage());
        assertEquals(FEED_LANGUAGE, rssChannel.getLanguage());

        assertNotNull(rssChannel.getCopyright());
        assertEquals(FEED_COPYRIGHT, rssChannel.getCopyright());

        assertNotNull(rssChannel.getManagingEditor());
        assertEquals(FEED_EDITOR, rssChannel.getManagingEditor());

        assertNotNull(rssChannel.getLastBuildDate());
        assertEquals(RssChannel.convertRssDateToJavaDate(FEED_LASTBUILDDATE), RssChannel
            .convertRssDateToJavaDate(rssChannel.getLastBuildDate()));

        assertTrue(rssChannel.getCategories().size() == 2);
        assertEquals(FEED_CTG1_DOMAIN, rssChannel.getCategories().get(0).getDomain());
        assertEquals(FEED_CTG1_VALUE, rssChannel.getCategories().get(0).getContent());
        assertEquals(FEED_CTG2_DOMAIN, rssChannel.getCategories().get(1).getDomain());
        assertEquals(FEED_CTG2_VALUE, rssChannel.getCategories().get(1).getContent());

        assertNotNull(rssChannel.getGenerator());
        assertEquals(FEED_GENERATOR, rssChannel.getGenerator());

        assertNotNull(rssChannel.getImage());
        assertNotNull(rssChannel.getImage().getUrl());
        assertEquals(FEED_IMAGE_URL, rssChannel.getImage().getUrl());

        assertTrue(rssChannel.getItems().size() > 0);
        RssItem rssItem = rssChannel.getItems().get(0);
        assertNotNull(rssItem);

        assertNotNull(rssItem.getTitle());
        assertEquals(ITEM_TITLE, rssItem.getTitle());

        assertNotNull(rssItem.getLink());
        assertEquals(ITEM_LINK, rssItem.getLink());

        assertNotNull(rssItem.getDescription());
        assertEquals(ITEM_DESCRIPTION, rssItem.getDescription());

        assertNotNull(rssItem.getAuthor());
        assertEquals(ITEM_AUTHOR, rssItem.getAuthor());

        assertTrue(rssItem.getCategories().size() == 2);
        assertEquals(FEED_CTG1_DOMAIN, rssItem.getCategories().get(0).getDomain());
        assertEquals(FEED_CTG1_VALUE, rssItem.getCategories().get(0).getContent());
        assertEquals(FEED_CTG2_DOMAIN, rssItem.getCategories().get(1).getDomain());
        assertEquals(FEED_CTG2_VALUE, rssItem.getCategories().get(1).getContent());

        assertNotNull(rssItem.getEnclosure());
        assertEquals(ITEM_ENCLOSURE_URL, rssItem.getEnclosure().getUrl());
        assertEquals(ITEM_ENCLOSURE_LENGTH, rssItem.getEnclosure().getLength());
        assertEquals(ITEM_ENCLOSURE_TYPE, rssItem.getEnclosure().getType());

        assertNotNull(rssItem.getGuid());
        assertEquals(ITEM_GUID, rssItem.getGuid().getContent());

        assertNotNull(rssItem.getPubDate());
        assertEquals(RssChannel.convertRssDateToJavaDate(ITEM_PUBDATE), RssChannel
            .convertRssDateToJavaDate(rssItem.getPubDate()));
    }
}
