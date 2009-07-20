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
package org.apache.wink.common.internal.providers.entity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.providers.entity.opensearch.OpenSearchDescriptionProvider;
import org.apache.wink.common.internal.utils.MediaTypeUtils;
import org.apache.wink.common.internal.utils.OpenSearchUtils;
import org.apache.wink.common.model.opensearch.OpenSearchDescription;
import org.apache.wink.common.model.opensearch.OpenSearchImage;
import org.apache.wink.common.model.opensearch.OpenSearchParameter;
import org.apache.wink.common.model.opensearch.OpenSearchQuery;
import org.apache.wink.common.model.opensearch.OpenSearchUrl;
import org.apache.wink.test.diff.DiffIgnoreUpdateWithAttributeQualifier;

import junit.framework.TestCase;

public class OpenSearchProviderTest extends TestCase {

    private static final String TITLE                = "Defect Search";
    private static final String TOTAL_RESULT         = "65000";
    private static final String START_PAGE           = "10";
    private static final String START_INDEX          = "1";
    private static final String COUNT                = "10";
    private static final String SEARCH_TERM          = "All Defects";
    private static final String ENCODING             = "UTF-8";
    private static final String TAGS                 = "defect bug";
    private static final String LANG                 = "en-US";
    private static final String DEVELOPER            = "John Smith";
    private static final String CONTACT              = "john.smith@example.com";
    private static final String DESC                 =
                                                         "You can search defects in HP Defect Manager";
    private static final String LONG_NAME            = "HP Defect Manager search engine";
    private static final String SHORT_NAME           = "HP Defect Manager search engine";

    private String              OPEN_SEARCH_DUCUMENT = "opensearch_descriptor.xml";
    public static final String  SEVERIIY             = "severity";
    public static final String  ASSIGNED_TO          = "assignedTo";
    private static final String URN_ASSIGNED_TO      = "urn:hp:defect:assignedTo";
    private static final String URN_SEVERIIY         = "urn:hp:defect:severity";
    public static final String  FTS                  = "q";
    public static final String  BASE_URL             =
                                                         "http://localhost:8080/QADefectUsingResourceBeans-service/rest/";
    public static final String  SEARCH_URL1          =
                                                         "http://localhost:8080/QADefectUsingResourceBeans-service/rest/defects?q={searchTerms?}";
    public static final String  SEARCH_URL2          =
                                                         "http://localhost:8080/QADefectUsingResourceBeans-service/rest/defects?severity={urn:hp:defect:severity?}&q={searchTerms?}&assignedTo={urn:hp:defect:assignedTo?}";

    public static final String  URL                  = "/defects";

    public void testOpenSearchSerialization() throws Exception {

        OpenSearchDescriptionProvider provider = new OpenSearchDescriptionProvider();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String expectedSerialization = null;
        try {
            provider.writeTo(buildOpenSearchDescriptor(BASE_URL),
                             OpenSearchDescription.class,
                             null,
                             null,
                             MediaTypeUtils.OPENSEARCH_TYPE,
                             null,
                             os);
        } catch (IOException e) {
            fail("Failed to serialize OpenSearchDocument");
        }
        try {
            expectedSerialization = readOpenSearchDocumentFromFile();
        } catch (IOException e) {
            fail("Failed to read " + OPEN_SEARCH_DUCUMENT);
        }
        String resultSerialization = new String(os.toByteArray());

        DiffIgnoreUpdateWithAttributeQualifier diff;
        try {
            diff =
                new DiffIgnoreUpdateWithAttributeQualifier(expectedSerialization,
                                                           resultSerialization);
        } catch (Exception e) {
            fail("Failed to perform diff");
            throw e;
        }

        assertTrue("Expected open search documents to be similar" + " "
            + diff.toString()
            + "\nexpected:\n"
            + expectedSerialization
            + "\nresult:\n"
            + resultSerialization, diff.similar());

    }

    public void testOpenSearchDescriptor() {
        OpenSearchDescription openSearchDescriptor = buildOpenSearchDescriptor(BASE_URL);
        assertEquals("Expected and actual values of Short Name are not same",
                     SHORT_NAME,
                     openSearchDescriptor.getShortName());
        assertEquals("Expected and actual values of  Description are not same",
                     DESC,
                     openSearchDescriptor.getDescription());
        assertEquals("Expected and actual values of  Long Name are not same",
                     LONG_NAME,
                     openSearchDescriptor.getLongName());
        assertEquals("Expected and actual values of  Contact are not same",
                     CONTACT,
                     openSearchDescriptor.getContact());
        assertEquals("Incorrect Developer", DEVELOPER, openSearchDescriptor.getDeveloper());

        List<String> languageArray = openSearchDescriptor.getLanguage();
        Collections.sort(languageArray);
        int binarySearchResult = Collections.binarySearch(languageArray, LANG);
        if (binarySearchResult < 0) {
            assertFalse("Accept Language " + LANG
                + " does not exist in array of expected languages"
                + languageArray, true);
        }

        assertEquals("Expected and actual values of  Tags are not same", TAGS, openSearchDescriptor
            .getTags());

        List<String> inputEncodingArray = openSearchDescriptor.getInputEncoding();
        Collections.sort(inputEncodingArray);
        int inputEncodingArraySearchResult = Collections.binarySearch(inputEncodingArray, ENCODING);
        if (inputEncodingArraySearchResult < 0) {
            assertFalse("Input Encoding " + ENCODING
                + "  does not exist in array of input incoding values "
                + inputEncodingArray, true);
        }

        List<String> outputEncodingArray = openSearchDescriptor.getOutputEncoding();
        Collections.sort(outputEncodingArray);
        int outputEncodingArraySearchResult =
            Collections.binarySearch(outputEncodingArray, ENCODING);
        if (outputEncodingArraySearchResult < 0) {
            assertFalse("Output Encoding " + ENCODING
                + "  does not exist in array of output incoding values "
                + outputEncodingArray, true);
        }

        List<OpenSearchUrl> urlArray = openSearchDescriptor.getUrl();
        if (urlArray != null && urlArray.size() != 0) {
            for (OpenSearchUrl url : urlArray) {
                String openSearchUrlTemplate = url.getTemplate();
                if (!(openSearchUrlTemplate.equals(SEARCH_URL1) || openSearchUrlTemplate
                    .equals(SEARCH_URL2))) {
                    assertFalse("Expected and actual values of  Url are not same " + " expected:"
                        + SEARCH_URL1
                        + ""
                        + SEARCH_URL2
                        + " actual:"
                        + openSearchUrlTemplate, true);
                }
            }
        }

        List<OpenSearchQuery> queryArray = openSearchDescriptor.getQuery();
        if (urlArray == null || urlArray.size() == 0) {
            assertFalse("Open Search Query is not defined", true);
        } else {
            OpenSearchQuery openSearchQuery = queryArray.get(0);
            assertEquals("Expected and actual values of  Count are not same ",
                         COUNT,
                         openSearchQuery.getCount().toString());
            assertEquals("Expected and actual values of  Input Encoding are not same ",
                         ENCODING,
                         openSearchQuery.getInputEncoding());
            assertEquals("Expected and actual values of  Language are not same ",
                         LANG,
                         openSearchQuery.getLanguage());
            assertEquals("Expected and actual values of  Output Encoding are not same ",
                         ENCODING,
                         openSearchQuery.getOutputEncoding());
            assertEquals("Expected and actual values of  QueryRole are not same ",
                         OpenSearchQuery.QueryRole.example.toString(),
                         openSearchQuery.getRole());
            assertEquals("Expected and actual values of  SearchTerms are not same ",
                         SEARCH_TERM,
                         openSearchQuery.getSearchTerms());
            assertEquals("Expected and actual values of  Start Index are not same ",
                         START_INDEX,
                         openSearchQuery.getStartIndex().toString());
            assertEquals("Expected and actual values of  StartPage are not same ",
                         START_PAGE,
                         openSearchQuery.getStartPage().toString());
            assertEquals("Expected and actual values of  Title are not same ",
                         TITLE,
                         openSearchQuery.getTitle());
            assertEquals("Expected and actual values of  TotalResults are not same ",
                         TOTAL_RESULT,
                         openSearchQuery.getTotalResults().toString());
        }
    }

    private OpenSearchDescription buildOpenSearchDescriptor(String baseUri) {
        OpenSearchDescription openSearchDescriptor = new OpenSearchDescription();
        openSearchDescriptor.setShortName(SHORT_NAME);
        openSearchDescriptor.setDescription(DESC);
        openSearchDescriptor.setLongName(LONG_NAME);
        openSearchDescriptor.setContact(CONTACT);
        openSearchDescriptor.setDeveloper(DEVELOPER);
        openSearchDescriptor.addLanguage(LANG);
        openSearchDescriptor.setTags(TAGS);
        openSearchDescriptor.addInputEncoding(ENCODING);
        openSearchDescriptor.addOutputEncoding(ENCODING);

        // set OpenSearch URL parameters
        OpenSearchParameter severityParameter =
            new OpenSearchParameter(SEVERIIY, URN_SEVERIIY, false);
        OpenSearchParameter ftsParameter =
            new OpenSearchParameter(FTS, OpenSearchParameter.OpenSearchParams.searchTerms
                .toString(), false);
        OpenSearchParameter assignedToParameter = new OpenSearchParameter();
        assignedToParameter.setMandatory(false);
        assignedToParameter.setParameterKey(ASSIGNED_TO);
        assignedToParameter.setParameter(URN_ASSIGNED_TO);

        // create Search URL & populate search parameters for browsers
        OpenSearchUrl openSearchUrlForBrowsers = new OpenSearchUrl();
        openSearchUrlForBrowsers.addOpenSearchParameter(ftsParameter);
        openSearchUrlForBrowsers.setType(MediaType.TEXT_HTML);

        // create Search URL & populate search parameters
        OpenSearchUrl openSearchUrl = new OpenSearchUrl();
        openSearchUrl.addOpenSearchParameter(severityParameter);
        openSearchUrl.addOpenSearchParameter(ftsParameter);
        openSearchUrl.addOpenSearchParameter(assignedToParameter);
        openSearchUrl.setType(MediaType.TEXT_HTML);

        // create open search base uri
        StringBuilder openSearchUrlBuilder = new StringBuilder(baseUri);
        if (baseUri.endsWith("/") && URL.startsWith("/")) {
            openSearchUrlBuilder.append(URL.substring(1));
        } else {
            openSearchUrlBuilder.append(URL);
        }
        openSearchUrl.setBaseUri(openSearchUrlBuilder.toString());
        openSearchUrlForBrowsers.setBaseUri(openSearchUrlBuilder.toString());

        // add URLs to OpenSearch
        openSearchDescriptor.addUrl(openSearchUrlForBrowsers);
        openSearchDescriptor.addUrl(openSearchUrl);

        // add OpenSearch Query element
        OpenSearchQuery openSearchQuery = new OpenSearchQuery();
        openSearchQuery.setCount(new BigInteger("10"));
        openSearchQuery.setInputEncoding(ENCODING);
        openSearchQuery.setLanguage(LANG);
        openSearchQuery.setOutputEncoding(ENCODING);
        openSearchQuery.setRole(OpenSearchQuery.QueryRole.example.toString());
        openSearchQuery.setSearchTerms(SEARCH_TERM);
        openSearchQuery.setStartIndex(new BigInteger(START_INDEX));
        openSearchQuery.setStartPage(new BigInteger(START_PAGE));
        openSearchQuery.setTitle(TITLE);
        openSearchQuery.setTotalResults(new BigInteger(TOTAL_RESULT));
        openSearchDescriptor.addQuery(openSearchQuery);

        // add OpenSearch Images
        OpenSearchImage openSearchImage;
        openSearchImage =
            OpenSearchUtils.createOpenSearchImage(MediaTypeUtils.IMAGE_JPEG, openSearchUrlBuilder
                .toString() + "splash.jpg");
        openSearchDescriptor.addNewImage(openSearchImage);
        return openSearchDescriptor;
    }

    private String readOpenSearchDocumentFromFile() throws IOException {
        // Read expected Entry from file
        InputStream is = OpenSearchProviderTest.class.getResourceAsStream(OPEN_SEARCH_DUCUMENT);
        byte[] b = new byte[4096];
        int read = is.read(b);
        String expectedSerialization = new String(b, 0, read);
        return expectedSerialization;
    }

}
