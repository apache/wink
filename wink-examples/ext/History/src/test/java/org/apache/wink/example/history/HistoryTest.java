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

package org.apache.wink.example.history;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.atom.AtomFeed;
import org.apache.wink.example.history.resources.DefectsResource;
import org.apache.wink.server.internal.servlet.MockServletInvocationTest;
import org.apache.wink.test.diff.DiffIgnoreUpdateWithAttributeQualifier;
import org.apache.wink.test.mock.MockRequestConstructor;
import org.apache.wink.test.mock.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Document;

/**
 *
 */
public class HistoryTest extends MockServletInvocationTest {

    @Override
    protected Class<?>[] getClasses() {
        return new Class[] { DefectsResource.class };
    }

    @Override
    protected String getPropertiesFile() {
        return TestUtils.packageToPath(getClass().getPackage().getName())
            + "\\history.properties";
    }

    public void testAll() throws Exception {

        // check the collection
        MockHttpServletRequest request = MockRequestConstructor.constructMockRequest("GET",
            "/defects", MediaType.APPLICATION_ATOM_XML_TYPE);
        MockHttpServletResponse response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        String diff = diffFeed("initial_atom.xml", response.getContentAsString());
        assertNull(diff);

        // get specific defect
        request = MockRequestConstructor.constructMockRequest("GET", "/defects/1",
            MediaType.APPLICATION_ATOM_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        diff = TestUtils.diffIgnoreUpdateWithAttributeQualifier(
            "initial_defect1_atom.xml", response.getContentAsString().getBytes(), getClass());
        assertNull(diff);

        // get revision of specific defect
        request = MockRequestConstructor.constructMockRequest("GET", "/defects/1;rev=1",
            MediaType.APPLICATION_ATOM_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        diff = TestUtils.diffIgnoreUpdateWithAttributeQualifier(
            "defect1_1_atom.xml", response.getContentAsString().getBytes(), getClass());
        assertNull(diff);

        // get history of specific defect
        request = MockRequestConstructor.constructMockRequest("GET", "/defects/1/history",
            MediaType.APPLICATION_ATOM_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        diff = diffFeed("defect1_history_atom.xml", response.getContentAsString());
        assertNull(diff);

        // try to delete the specific revision, it should fail
        request = MockRequestConstructor.constructMockRequest("DELETE", "/defects/1;rev=1",
            MediaType.APPLICATION_ATOM_XML_TYPE);
        response = invoke(request);
        assertEquals("status", HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.getStatus());

        // try to delete defect, the response should be equal to initial
        request = MockRequestConstructor.constructMockRequest("DELETE", "/defects/1",
            MediaType.APPLICATION_ATOM_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        diff = TestUtils.diffIgnoreUpdateWithAttributeQualifier(
            "initial_defect1_atom.xml", response.getContentAsString().getBytes(), getClass());
        assertNull(diff);

        // try to get the defect again, should return 404
        request = MockRequestConstructor.constructMockRequest("GET", "/defects/1",
            MediaType.APPLICATION_ATOM_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 404, response.getStatus());

        // try to update the defect, should return 404
        request = MockRequestConstructor.constructMockRequest("PUT", "/defects/1",
            MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_ATOM_XML,
            TestUtils.getResourceOfSamePackageAsBytes("defect1_1_atom.xml",
                getClass()));
        response = invoke(request);
        assertEquals("status", 404, response.getStatus());

        // get revision of specific defect after delete
        // the result should not have an edit link
        request = MockRequestConstructor.constructMockRequest("GET", "/defects/1;rev=1",
            MediaType.APPLICATION_ATOM_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        diff = TestUtils.diffIgnoreUpdateWithAttributeQualifier(
            "defect1_1_afterdelete_atom.xml", response.getContentAsString().getBytes(), getClass());
        assertNull(diff);

        // get a history of the specific defect after delete
        // the result should not have edit links and should contain a new "deleted" revision
        request = MockRequestConstructor.constructMockRequest("GET", "/defects/1/history",
            MediaType.APPLICATION_ATOM_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        diff = diffFeed("defect1_history_afterdelete_atom.xml", response.getContentAsString());
        assertNull(diff);

        // undelete
        // should return created
        request = MockRequestConstructor.constructMockRequest("POST", "/defects",
            MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_ATOM_XML,
            TestUtils.getResourceOfSamePackageAsBytes("defect1_1_atom.xml",
                getClass()));
        response = invoke(request);
        assertEquals("status", 201, response.getStatus());
        diff = TestUtils.diffIgnoreUpdateWithAttributeQualifier(
            "defect1_4_atom.xml", response.getContentAsString().getBytes(), getClass());
        assertNull(diff);

        // undelete
        // should fail, since the defect was already undeleted
        request = MockRequestConstructor.constructMockRequest("POST", "/defects",
            MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_ATOM_XML,
            TestUtils.getResourceOfSamePackageAsBytes("defect1_1_atom.xml",
                getClass()));
        response = invoke(request);
        assertEquals("status", 409, response.getStatus());

        // update undeleted defect
        request = MockRequestConstructor.constructMockRequest("PUT", "/defects/1",
            MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_ATOM_XML,
            TestUtils.getResourceOfSamePackageAsBytes("defect1_1_atom.xml",
                getClass()));
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        diff = TestUtils.diffIgnoreUpdateWithAttributeQualifier(
            "defect1_5_atom.xml", response.getContentAsString().getBytes(), getClass());
        assertNull(diff);

        //last final time check the history
        request = MockRequestConstructor.constructMockRequest("GET", "/defects/1/history",
            MediaType.APPLICATION_ATOM_XML_TYPE);
        response = invoke(request);
        assertEquals("status", 200, response.getStatus());
        diff = diffFeed("defect1_history_final_atom.xml", response.getContentAsString());
        assertNull(diff);
    }

    private String diffFeed(String expectedFileName, String actual) throws Exception {
        InputStream expected = TestUtils.getResourceOfSamePackage(
            expectedFileName, getClass());

        // sort feeds
        AtomFeed expectedFeed = AtomFeed.unmarshal(new InputStreamReader(expected));
        AtomFeed actualFeed = AtomFeed.unmarshal(new StringReader(actual));
        Collections.sort(actualFeed.getEntries(), new AtomEntryComparator());
        Collections.sort(expectedFeed.getEntries(), new AtomEntryComparator());

        ByteArrayOutputStream actualOS = new ByteArrayOutputStream();
        ByteArrayOutputStream expectedOS = new ByteArrayOutputStream();
        AtomFeed.marshal(expectedFeed, expectedOS);
        AtomFeed.marshal(actualFeed,  actualOS);

        Document expectedXml = TestUtils.getXML(expectedOS.toByteArray());
        Document actualXml = TestUtils.getXML(actualOS.toByteArray());

        DiffIgnoreUpdateWithAttributeQualifier diff = new DiffIgnoreUpdateWithAttributeQualifier(
            expectedXml, actualXml);
        if (diff.similar()) {
            return null;
        }
        System.err.println("Expected:\r\n"
            + TestUtils.printPrettyXML(expectedXml));
        System.err.println("Actual:\r\n"
            + TestUtils.printPrettyXML(actualXml));
        return diff.toString();
    }

    public class AtomEntryComparator implements Comparator<AtomEntry> {

        public int compare(AtomEntry e1, AtomEntry e2) {
            if (e1 == null) {
                if (e2 == null) {
                    return 0;
                }
                return -1;
            }
            if (e2 == null) {
                return 1;
            }
            return e1.getId().compareTo(e2.getId());
        }

    }
}
