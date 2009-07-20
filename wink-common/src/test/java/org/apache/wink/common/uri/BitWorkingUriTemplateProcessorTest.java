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

package org.apache.wink.common.uri;

import static org.junit.Assert.assertArrayEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.internal.uritemplate.BitWorkingUriTemplateProcessor;
import org.apache.wink.common.internal.uritemplate.UriTemplateMatcher;
import org.apache.wink.common.internal.uritemplate.UriTemplateProcessor;

import junit.framework.TestCase;

/**
 * Unit test of UriTemplate.
 */
public class BitWorkingUriTemplateProcessorTest extends TestCase {// extends
                                                                  // SpringAwareTestCase
                                                                  // {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetVariableNames() {
        UriTemplateProcessor template =
            new BitWorkingUriTemplateProcessor(
                                               "/prefix/{varA}/root/{variableB}/suffix/{Variable345}");
        Set<String> expectedResult = new HashSet<String>();
        expectedResult.add("varA");
        expectedResult.add("variableB");
        expectedResult.add("Variable345");
        assertEquals("variable name", expectedResult, template.getVariableNames());
    }

    public void testMatch() {
        UriTemplateProcessor template =
            new BitWorkingUriTemplateProcessor("/prefix/{varA}/root/{variableB}/suffix");
        UriTemplateMatcher matcher = template.matcher();
        MultivaluedMap<String, String> result = matcher.match("/prefix/aaaaaa/root/BbBbB/suffix");
        assertNotNull("match ok", result);
        assertEquals("match size", 2, result.size());
        assertEquals("varA", "aaaaaa", result.getFirst("varA"));
        assertEquals("variableB", "BbBbB", result.getFirst("variableB"));
    }

    public void testMatchVariableDoubleUsage() {
        UriTemplateProcessor template =
            new BitWorkingUriTemplateProcessor("/prefix/{varA}/root/{varA}/suffix");
        UriTemplateMatcher matcher = template.matcher();
        MultivaluedMap<String, String> result = matcher.match("/prefix/aaaaaa/root/aaaaaa/suffix");
        assertNotNull("match ok", result);
        assertEquals("match size", 1, result.size());
        assertEquals("varA", "aaaaaa", result.getFirst("varA"));
    }

    public void testMatchNegative() {
        UriTemplateProcessor template =
            new BitWorkingUriTemplateProcessor("/prefix/{variable}/suffix");
        UriTemplateMatcher matcher = template.matcher();
        assertNull("not matching", matcher.match("aprefix/value/suffix"));
    }

    public void testInstantiate() {
        UriTemplateProcessor templateA =
            new BitWorkingUriTemplateProcessor("/part1/{variable}/part2");
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("variable", "value");
        assertEquals("instantiate template", "/part1/value/part2", templateA.expand(hashMap));
    }

    public void testInstantiateDefaultValue() {
        UriTemplateProcessor templateA =
            new BitWorkingUriTemplateProcessor("/part1/{variable=default_value}/part2");

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("variable", "my_value");
        assertEquals("instantiate template with some value", "/part1/my_value/part2", templateA
            .expand(hashMap));

        hashMap = new HashMap<String, Object>();
        hashMap.put("variable", (String)null);
        assertEquals("instantiate template with default value if null",
                     "/part1/default_value/part2",
                     templateA.expand(hashMap));

        hashMap = new HashMap<String, Object>();
        assertEquals("instantiate template with default value if not-defined",
                     "/part1/default_value/part2",
                     templateA.expand(hashMap));
    }

    public void testInstantiateNegative() {
        UriTemplateProcessor template =
            new BitWorkingUriTemplateProcessor("/part1/{variableA}/part2/{variableB}");
        try {
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("variableA", "value");
            template.expand(hashMap);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testCreateNegative() {
        try {
            new BitWorkingUriTemplateProcessor(null);
            fail("NPE expected");
        } catch (NullPointerException expected) {
        }
        try {
            new BitWorkingUriTemplateProcessor("{unclosedVariableStill/goes/on");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
        try {
            new BitWorkingUriTemplateProcessor("artifacts{}");
            fail("no variable in expansion");
        } catch (IllegalArgumentException e) {
        } // ok
    }

    public void testPathParameters() {

        UriTemplateProcessor feedTemplate1 =
            new BitWorkingUriTemplateProcessor(
                                               "artifacts{-prefix|;datetime=|datetime}{-prefix|;lc=|lc}{-prefix|;approved=|approved}");
        UriTemplateProcessor feedTemplate2 =
            new BitWorkingUriTemplateProcessor(
                                               "artifacts{-opt|;|datetime,lc,approved}{-join|;|datetime,lc,approved}");
        UriTemplateProcessor entryTemplate1 =
            new BitWorkingUriTemplateProcessor(
                                               feedTemplate1.getTemplate() + "/{artifact}{-prefix|;rev=|revision}");
        UriTemplateProcessor entryTemplate2 =
            new BitWorkingUriTemplateProcessor(
                                               feedTemplate2.getTemplate() + "/{artifact}{-prefix|;rev=|revision}");

        String[][] allNull = { {"datetime", null}, {"lc", null}, {"approved", null}};
        String[][] dateEmpty = { {"datetime", ""}, {"lc", null}, {"approved", null}};
        String[][] date = { {"datetime", "date"}, {"lc", null}, {"approved", null}};
        String[][] all = { {"datetime", "date"}, {"lc", "lc"}, {"approved", "stage"}};
        String[][] wrongOrder =
            { {"datetime", null}, {"lc", "lc"}, {"approved", "stage;datetime=date"}};

        assertMatchTemplate(feedTemplate1, "artifacts/", allNull);
        assertMatchTemplate(feedTemplate2, "artifacts/", allNull);

        assertMatchTemplate(feedTemplate1, "artifacts;", null);
        assertMatchTemplate(feedTemplate2, "artifacts;", allNull);
        assertMatchTemplate(feedTemplate1, "artifacts;datetime", null);
        assertMatchTemplate(feedTemplate2, "artifacts;datetime", null);
        assertMatchTemplate(feedTemplate1, "artifacts;datetime;", null);
        assertMatchTemplate(feedTemplate2, "artifacts;datetime;", null);

        assertMatchTemplate(feedTemplate1, "artifacts", allNull);
        assertMatchTemplate(feedTemplate2, "artifacts", allNull);
        assertMatchTemplate(feedTemplate1, "artifacts;datetime=", dateEmpty);
        assertMatchTemplate(feedTemplate2, "artifacts;datetime=", dateEmpty);
        assertMatchTemplate(feedTemplate1, "artifacts;datetime=date", date);
        assertMatchTemplate(feedTemplate2, "artifacts;datetime=date", date);
        assertMatchTemplate(feedTemplate1, "artifacts;datetime=date;lc=lc;approved=stage", all);
        assertMatchTemplate(feedTemplate2, "artifacts;datetime=date;lc=lc;approved=stage", all);

        // the order of parameters matters with "prefix" operator
        assertMatchTemplate(feedTemplate1,
                            "artifacts;lc=lc;approved=stage;datetime=date",
                            wrongOrder);
        // but it does not matter with "join" operator
        assertMatchTemplate(feedTemplate2, "artifacts;lc=lc;approved=stage;datetime=date", all);

        // ----- entries ------

        String[][] entry =
            { {"datetime", null}, {"lc", null}, {"approved", null}, {"artifact", "13"},
                {"revision", null}};
        String[][] emptyRevision =
            { {"datetime", null}, {"lc", null}, {"approved", null}, {"artifact", "13"},
                {"revision", ""}};
        String[][] revision =
            { {"datetime", null}, {"lc", null}, {"approved", null}, {"artifact", "13"},
                {"revision", "7"}};
        all =
            new String[][] { {"datetime", "date"}, {"lc", "lc"}, {"approved", "stage"},
                {"artifact", "13"}, {"revision", "7"}};

        assertMatchTemplate(entryTemplate1, "artifacts/", null);
        assertMatchTemplate(entryTemplate2, "artifacts/", null);

        assertMatchTemplate(entryTemplate1, "artifacts/13", entry);
        assertMatchTemplate(entryTemplate2, "artifacts/13", entry);
        assertMatchTemplate(entryTemplate1, "artifacts/13;rev=", emptyRevision);
        assertMatchTemplate(entryTemplate2, "artifacts/13;rev=", emptyRevision);
        assertMatchTemplate(entryTemplate1, "artifacts/13;rev=7", revision);
        assertMatchTemplate(entryTemplate2, "artifacts/13;rev=7", revision);

        assertMatchTemplate(entryTemplate1,
                            "artifacts;datetime=date;lc=lc;approved=stage/13;rev=7",
                            all);
        assertMatchTemplate(entryTemplate2,
                            "artifacts;lc=lc;approved=stage;datetime=date/13;rev=7",
                            all);
    }

    public void testListOperator() {
        UriTemplateProcessor template1 =
            new BitWorkingUriTemplateProcessor("locations/{-list|/|var}");
        UriTemplateProcessor template2 =
            new BitWorkingUriTemplateProcessor("locations{-opt|/|var}{-list|/|var}");
        UriTemplateProcessor template3 =
            new BitWorkingUriTemplateProcessor("telegram:{-list|stop|var}");
        String[][] empty = {{"var", ""}};
        String[][] a = {{"var", "a"}};
        String[][] ab = {{"var", "a", "b"}};
        String[][] ab_ = {{"var", "a", "b", ""}};
        String[][] a_c = {{"var", "a", "", "c"}};
        String[][] _bc = {{"var", "", "b", "c"}};
        String[][] message = {{"var", "hello", "baby", "go", "home"}};

        assertMatchTemplate(template1, "locations", null);
        assertMatchTemplate(template2, "locations", empty);
        assertMatchTemplate(template1, "locations/", empty);
        assertMatchTemplate(template2, "locations/", empty);
        assertMatchTemplate(template1, "locations/a", a);
        assertMatchTemplate(template2, "locations/a", a);
        assertMatchTemplate(template1, "locations/a/b", ab);
        assertMatchTemplate(template2, "locations/a/b", ab);
        assertMatchTemplate(template2, "locations/a/b/", ab_);
        assertMatchTemplate(template2, "locations/a//c", a_c);
        assertMatchTemplate(template2, "locations//b/c", _bc);
        assertMatchTemplate(template3, "telegram:hellostopbabystopgostophome", message);

        try {
            new BitWorkingUriTemplateProcessor("locations/{-list|/|var1,var2}");
            fail("only one variable is allowed");
        } catch (IllegalArgumentException expected) {
        }

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {});
        assertEquals("instantiate template1", "locations/", template1.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {});
        assertEquals("instantiate template2", "locations", template2.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"d"});
        assertEquals("instantiate template1", "locations/d", template1.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"d"});
        assertEquals("instantiate template2", "locations/d", template2.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"d", "e"});
        assertEquals("instantiate template1", "locations/d/e", template1.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"d", "e"});
        assertEquals("instantiate template2", "locations/d/e", template2.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"d", "", "e"});
        assertEquals("instantiate template1", "locations/d//e", template1.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"d", "", "e"});
        assertEquals("instantiate template2", "locations/d//e", template2.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"", "d", "e"});
        assertEquals("instantiate template1", "locations//d/e", template1.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"", "d", "e"});
        assertEquals("instantiate template2", "locations//d/e", template2.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"d", "e", ""});
        assertEquals("instantiate template1", "locations/d/e/", template1.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"d", "e", ""});
        assertEquals("instantiate template2", "locations/d/e/", template2.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"hello", "baby", "go", "home"});
        assertEquals("instantiate template3", "telegram:hellostopbabystopgostophome", template3
            .expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", "value");
        assertEquals("instantiate template1", "locations/value", template1.expand(hashMap));
    }

    public void testOptOperator() {
        UriTemplateProcessor template =
            new BitWorkingUriTemplateProcessor("artifacts{-opt|;|var1,var2}");
        String[][] empty = {};

        assertMatchTemplate(template, "artifact/", null);
        assertMatchTemplate(template, "artifacts", empty);
        assertMatchTemplate(template, "artifacts;", empty);
        assertMatchTemplate(template, "artifacts;;", null);

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("var1", "somevalue");
        assertEquals("instantiate template", "artifacts;", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", "somevalue");
        assertEquals("instantiate template", "artifacts;", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var1", "");
        assertEquals("instantiate template", "artifacts;", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", "");
        assertEquals("instantiate template", "artifacts;", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var1", (String)null);
        assertEquals("instantiate template", "artifacts;", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", (String)null);
        assertEquals("instantiate template", "artifacts;", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var1", "");
        hashMap.put("var2", "");
        assertEquals("instantiate template", "artifacts;", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var1", (String)null);
        hashMap.put("var2", (String)null);
        assertEquals("instantiate template", "artifacts;", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        assertEquals("instantiate template", "artifacts", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", new String[] {});
        assertEquals("instantiate template", "artifacts", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", new String[] {null});
        assertEquals("instantiate template", "artifacts;", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", new String[] {""});
        assertEquals("instantiate template", "artifacts;", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", new String[] {"a", "b"});
        assertEquals("instantiate template", "artifacts;", template.expand(hashMap));

        try {
            new BitWorkingUriTemplateProcessor("artifacts{-opt|;|}");
            fail("no variable in operator");
        } catch (IllegalArgumentException e) {
        } // ok
    }

    public void testSuffixOperator() {
        UriTemplateProcessor template = new BitWorkingUriTemplateProcessor("/{-suffix|/|var}cat");
        UriTemplateProcessor template3 =
            new BitWorkingUriTemplateProcessor("telegram:{-suffix|stop|var}");
        String[][] animalsVar = {{"var", "animals"}};
        String[][] noVar = {{"var", null}};
        String[][] emptyVar = {{"var", ""}};
        String[][] domesticAnimalsVar = {{"var", "animals", "domestic"}};
        String[][] message = {{"var", "hello", "baby", "go", "home"}};

        assertMatchTemplate(template, "/animals/cat", animalsVar);
        assertMatchTemplate(template, "/cat", noVar);
        assertMatchTemplate(template, "//cat", emptyVar);
        assertMatchTemplate(template, "/animals;cat", null);
        assertMatchTemplate(template, "animals/cat", null);
        try {
            new BitWorkingUriTemplateProcessor("/{-suffix|/|var1,var2}cat");
            fail("-suffix accepts only one variable");
        } catch (IllegalArgumentException e) {
        } // ok
        assertMatchTemplate(template, "/animals/domestic/cat", domesticAnimalsVar);
        assertMatchTemplate(template3, "telegram:hellostopbabystopgostophomestop", message);

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("var", "animals");
        assertEquals("instantiate template", "/animals/cat", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", "");
        assertEquals("instantiate template", "//cat", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        assertEquals("instantiate template", "/cat", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("", "");
        assertEquals("instantiate template", "/cat", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("", "pets");
        assertEquals("instantiate template", "/cat", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"animals", "domestic"});
        assertEquals("instantiate template", "/animals/domestic/cat", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"hello", "baby", "go", "home"});
        assertEquals("instantiate template3", "telegram:hellostopbabystopgostophomestop", template3
            .expand(hashMap));
    }

    public void testJoinOperator() {
        UriTemplateProcessor template =
            new BitWorkingUriTemplateProcessor("people%3F{-join|&|var1,var2}");
        String[][] allNull = { {"var1", null}, {"var2", null}};
        String[][] firstEmpty = { {"var1", ""}, {"var2", null}};
        String[][] john = { {"var1", "john"}, {"var2", null}};
        String[][] allDefined = { {"var1", "mary"}, {"var2", "kate"}};

        assertMatchTemplate(template, "people%3F/", allNull);
        assertMatchTemplate(template, "people%3F", allNull);
        assertMatchTemplate(template, "people%3Fvar1", null);
        assertMatchTemplate(template, "people%3Fvar1&", null);
        assertMatchTemplate(template, "people%3Fvar1=", firstEmpty);
        assertMatchTemplate(template, "people%3Fvar1=john", john);
        assertMatchTemplate(template, "people%3Fvar1=mary&var2=kate", allDefined);
        assertMatchTemplate(template, "people%3Fvar2=kate&var1=mary", allDefined);
        assertMatchTemplate(template, "people%3Fvar3=jin", null);

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("var1", "");
        assertEquals("instantiate template", "people%3Fvar1=", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var1", "jay");
        assertEquals("instantiate template", "people%3Fvar1=jay", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", "joe");
        assertEquals("instantiate template", "people%3Fvar2=joe", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var1", "mary");
        hashMap.put("var2", "kate");
        assertEquals("instantiate template", "people%3Fvar1=mary&var2=kate", template
            .expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var3", "joe");
        assertEquals("instantiate template", "people%3F", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        assertEquals("instantiate template", "people%3F", template.expand(hashMap));

        try {
            hashMap = new HashMap<String, Object>();
            hashMap.put("var1", new String[] {"joe", "max"});
            template.expand(hashMap);
            fail("variable must not be a list");
        } catch (IllegalArgumentException expected) {
        }

    }

    public void testNegOperator() {
        UriTemplateProcessor template =
            new BitWorkingUriTemplateProcessor("artifacts{-neg|;|var1,var2}");
        String[][] empty = {};

        assertMatchTemplate(template, "artifact/", null);
        assertMatchTemplate(template, "artifacts", empty);
        assertMatchTemplate(template, "artifacts;", empty);
        assertMatchTemplate(template, "artifacts;;", null);

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("var1", (String)null);
        assertEquals("instantiate template", "artifacts", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", (String)null);
        assertEquals("instantiate template", "artifacts", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var1", (String)null);
        hashMap.put("var2", (String)null);
        assertEquals("instantiate template", "artifacts", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var1", "");
        assertEquals("instantiate template", "artifacts", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", "");
        assertEquals("instantiate template", "artifacts", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var1", "");
        hashMap.put("var2", "");
        assertEquals("instantiate template", "artifacts", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        assertEquals("instantiate template", "artifacts;", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var1", "somevalue");
        assertEquals("instantiate template", "artifacts", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", "somevalue");
        assertEquals("instantiate template", "artifacts", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", new String[] {});
        assertEquals("instantiate template", "artifacts;", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", new String[] {null});
        assertEquals("instantiate template", "artifacts", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", new String[] {""});
        assertEquals("instantiate template", "artifacts", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var2", new String[] {"a", "b"});
        assertEquals("instantiate template", "artifacts", template.expand(hashMap));
    }

    public void testPrefixOperator() {
        UriTemplateProcessor template = new BitWorkingUriTemplateProcessor("{-prefix|/|var}/cat");
        UriTemplateProcessor template3 =
            new BitWorkingUriTemplateProcessor("telegram:{-prefix|stop|var}");
        String[][] animalsVar = {{"var", "animals"}};
        String[][] noVar = {{"var", null}};
        String[][] emptyVar = {{"var", ""}};
        String[][] domesticAnimalsVar = {{"var", "animals", "domestic"}};
        String[][] message = {{"var", "hello", "baby", "go", "home"}};

        assertMatchTemplate(template, "/animals/cat", animalsVar);
        assertMatchTemplate(template, "/cat", noVar);
        assertMatchTemplate(template, "//cat", emptyVar);
        assertMatchTemplate(template, "/animals;cat", null);
        assertMatchTemplate(template, "animals/cat", null);
        try {
            new BitWorkingUriTemplateProcessor("/{-prefix|/|var1,var2}cat");
            fail("-prefix accepts only one variable");
        } catch (IllegalArgumentException e) {
        } // ok
        assertMatchTemplate(template, "/animals/domestic/cat", domesticAnimalsVar);
        assertMatchTemplate(template3, "telegram:stophellostopbabystopgostophome", message);

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("var", "animals");
        assertEquals("instantiate template", "/animals/cat", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", "");
        assertEquals("instantiate template", "//cat", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        assertEquals("instantiate template", "/cat", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("", "");
        assertEquals("instantiate template", "/cat", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("", "pets");
        assertEquals("instantiate template", "/cat", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"animals", "domestic"});
        assertEquals("instantiate template", "/animals/domestic/cat", template.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("var", new String[] {"hello", "baby", "go", "home"});
        assertEquals("instantiate template3", "telegram:stophellostopbabystopgostophome", template3
            .expand(hashMap));
    }

    public void testOperatorInvalidSyntax() {
        try {
            new BitWorkingUriTemplateProcessor("/{-prefix}cat");
            fail("no pipe in operator");
        } catch (IllegalArgumentException e) {
        } // ok
        try {
            new BitWorkingUriTemplateProcessor("/{-prefix|/}cat");
            fail("only one pipe in operator");
        } catch (IllegalArgumentException e) {
        } // ok
        try {
            new BitWorkingUriTemplateProcessor("/{}/cat");
            fail("no variable name");
        } catch (IllegalArgumentException e) {
        } // ok
    }

    public void testEncodedUri() {
        UriTemplateProcessor template1 =
            new BitWorkingUriTemplateProcessor("cars(old*new:good)my?{-join|&|car1,car2}");
        String[][] carsDefined = { {"car1", "Ford"}, {"car2", "Opel"}};
        assertMatchTemplate(template1, "cars(old*new:good)my?car1=Ford&car2=Opel", carsDefined);
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("car1", "Ford");
        hashMap.put("car2", "Opel");
        assertEquals("instantiate template1", "cars(old*new:good)my?car1=Ford&car2=Opel", template1
            .expand(hashMap));

        UriTemplateProcessor template2 =
            new BitWorkingUriTemplateProcessor("vegetables|{-list|?|vegs}");
        String[][] vegsDefined = {{"vegs", "carrot", "leek"}};
        assertMatchTemplate(template2, "vegetables|carrot?leek", vegsDefined);
        hashMap = new HashMap<String, Object>();
        hashMap.put("vegs", new String[] {"carrot", "leek"});
        assertEquals("instantiate template2", "vegetables|carrot?leek", template2.expand(hashMap));

        UriTemplateProcessor template3 =
            new BitWorkingUriTemplateProcessor("vegetables{-prefix|?|vegs}");
        assertMatchTemplate(template3, "vegetables?carrot?leek", vegsDefined);
        hashMap = new HashMap<String, Object>();
        hashMap.put("vegs", new String[] {"carrot", "leek"});
        assertEquals("instantiate template3", "vegetables?carrot?leek", template3.expand(hashMap));

        UriTemplateProcessor template4 =
            new BitWorkingUriTemplateProcessor("vegetables|{-suffix|?|vegs}");
        assertMatchTemplate(template4, "vegetables|carrot?leek?", vegsDefined);
        hashMap = new HashMap<String, Object>();
        hashMap.put("vegs", new String[] {"carrot", "leek"});
        assertEquals("instantiate template4", "vegetables|carrot?leek?", template4.expand(hashMap));

        UriTemplateProcessor template5 =
            new BitWorkingUriTemplateProcessor("translator({-join|?|czech,english})");
        String[][] translation = { {"czech", "pes"}, {"english", "dog"}};
        assertMatchTemplate(template5, "translator(english=dog?czech=pes)", translation);
        hashMap = new HashMap<String, Object>();
        hashMap.put("english", "dog");
        hashMap.put("czech", "pes");
        assertEquals("instantiate template5", "translator(czech=pes?english=dog)", template5
            .expand(hashMap));

        UriTemplateProcessor template6 =
            new BitWorkingUriTemplateProcessor("food{-opt|?|meat,milk}");
        String[][] empty = {};
        assertMatchTemplate(template6, "food?", empty);
        hashMap = new HashMap<String, Object>();
        hashMap.put("meat", "poultry");
        assertEquals("instantiate template6", "food?", template6.expand(hashMap));

        UriTemplateProcessor template7 =
            new BitWorkingUriTemplateProcessor("food{-neg|()|meat,milk}");
        assertMatchTemplate(template7, "food()", empty);
        hashMap = new HashMap<String, Object>();
        assertEquals("instantiate template7", "food()", template7.expand(hashMap));
    }

    public void testUnreservedMatch() {
        UriTemplateProcessor template = new BitWorkingUriTemplateProcessor("/prefix/{varA}");
        UriTemplateMatcher matcher = template.matcher();
        MultivaluedMap<String, String> result = matcher.match("/prefix/a.b");
        assertNotNull("match ok .", result);
        assertEquals("match size .", 1, result.size());
        assertEquals("varA", "a.b", result.getFirst("varA"));

        result = matcher.match("/prefix/a_b");
        assertNotNull("match ok _", result);
        assertEquals("match size _", 1, result.size());
        assertEquals("varA", "a_b", result.getFirst("varA"));

        result = matcher.match("/prefix/a-b");
        assertNotNull("match ok -", result);
        assertEquals("match size ,", 1, result.size());
        assertEquals("varA", "a-b", result.getFirst("varA"));

        result = matcher.match("/prefix/a~b");
        assertNotNull("match ok ~", result);
        assertEquals("match size ~", 1, result.size());
        assertEquals("varA", "a~b", result.getFirst("varA"));
    }

    public void testReservedMatch() {
        UriTemplateProcessor template = new BitWorkingUriTemplateProcessor("/prefix/{varA}");
        UriTemplateMatcher matcher = template.matcher();
        MultivaluedMap<String, String> result = matcher.match("/prefix/a%3Ab");
        assertNotNull("match ok :", result);
        assertEquals("match size :", 1, result.size());
        assertEquals("varA", "a%3Ab", result.getFirst("varA"));

        // character ":" is reserved, however hp-soa requires to match it
        result = matcher.match("/prefix/a:b");
        assertNotNull("match ok :", result);
        assertEquals("match size :", 1, result.size());
        assertEquals("varA", "a:b", result.getFirst("varA"));

        result = matcher.match("/prefix/a%2Fb");
        assertNotNull("match ok /", result);
        assertEquals("match size /", 1, result.size());
        assertEquals("varA", "a%2Fb", result.getFirst("varA"));

        result = matcher.match("/prefix/%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D");
        assertNotNull("match ok ?#[]@!$&'()*+,;=", result);
        assertEquals("match size ?#[]@!$&'()*+,;=", 1, result.size());
        assertEquals("varA", "%3F%23%5B%5D%40%21%24%26%27%28%29%2A%2B%2C%3B%3D", result
            .getFirst("varA"));
    }

    public void testDecodedMatchedValues() {
        UriTemplateProcessor templateVar = new BitWorkingUriTemplateProcessor("/var/{var}");
        String[][] var = {{"var", "enc%3Aoded"}};
        assertMatchTemplate(templateVar, "/var/enc%3Aoded", var);

        UriTemplateProcessor templateJoin =
            new BitWorkingUriTemplateProcessor("/join/{-join|;|join}");
        String[][] join = {{"join", "enc%3Aoded"}};
        assertMatchTemplate(templateJoin, "/join/join=enc%3Aoded", join);

        UriTemplateProcessor templateList =
            new BitWorkingUriTemplateProcessor("/list/{-list|/|list}");
        String[][] list1 = {{"list", "enc%3Aoded"}};
        assertMatchTemplate(templateList, "/list/enc%3Aoded", list1);
        String[][] list2 = {{"list", "enc%3Aoded", "enc%3Aoded"}};
        assertMatchTemplate(templateList, "/list/enc%3Aoded/enc%3Aoded", list2);

        UriTemplateProcessor templatePrefix =
            new BitWorkingUriTemplateProcessor("/prefix{-prefix|/|prefix}");
        String[][] prefix1 = {{"prefix", "enc%3Aoded"}};
        assertMatchTemplate(templatePrefix, "/prefix/enc%3Aoded", prefix1);
        String[][] prefix2 = {{"prefix", "enc%3Aoded", "enc%3Aoded"}};
        assertMatchTemplate(templatePrefix, "/prefix/enc%3Aoded/enc%3Aoded", prefix2);

        UriTemplateProcessor templateSuffix =
            new BitWorkingUriTemplateProcessor("/suffix/{-suffix|/|suffix}");
        String[][] suffix1 = {{"suffix", "enc%3Aoded"}};
        assertMatchTemplate(templateSuffix, "/suffix/enc%3Aoded/", suffix1);
        String[][] suffix2 = {{"suffix", "enc%3Aoded", "enc%3Aoded"}};
        assertMatchTemplate(templateSuffix, "/suffix/enc%3Aoded/enc%3Aoded/", suffix2);
    }

    public void testEncodedSubstitutedValues() {
        UriTemplateProcessor templateVar = new BitWorkingUriTemplateProcessor("/var/{var}");
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("var", "enc:oded");
        assertEquals("values encoded", "/var/enc%3Aoded", templateVar.expand(hashMap));

        UriTemplateProcessor templateJoin =
            new BitWorkingUriTemplateProcessor("/join/{-join|;|join}");
        hashMap = new HashMap<String, Object>();
        hashMap.put("join", "enc:oded");
        assertEquals("values encoded", "/join/join=enc%3Aoded", templateJoin.expand(hashMap));

        UriTemplateProcessor templateList =
            new BitWorkingUriTemplateProcessor("/list/{-list|/|list}");
        hashMap = new HashMap<String, Object>();
        hashMap.put("list", new String[] {"enc:oded"});
        assertEquals("values encoded", "/list/enc%3Aoded", templateList.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("list", new String[] {"enc:oded", "enc:oded"});
        assertEquals("values encoded", "/list/enc%3Aoded/enc%3Aoded", templateList.expand(hashMap));

        UriTemplateProcessor templatePrefix =
            new BitWorkingUriTemplateProcessor("/prefix{-prefix|/|prefix}");
        hashMap = new HashMap<String, Object>();
        hashMap.put("prefix", "enc:oded");
        assertEquals("values encoded", "/prefix/enc%3Aoded", templatePrefix.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("prefix", new String[] {"enc:oded", "enc:oded"});
        assertEquals("values encoded", "/prefix/enc%3Aoded/enc%3Aoded", templatePrefix
            .expand(hashMap));

        UriTemplateProcessor templateSuffix =
            new BitWorkingUriTemplateProcessor("/suffix/{-suffix|/|suffix}");
        hashMap = new HashMap<String, Object>();
        hashMap.put("suffix", "enc:oded");
        assertEquals("values encoded", "/suffix/enc%3Aoded/", templateSuffix.expand(hashMap));
        hashMap = new HashMap<String, Object>();
        hashMap.put("suffix", new String[] {"enc:oded", "enc:oded"});
        assertEquals("values encoded", "/suffix/enc%3Aoded/enc%3Aoded/", templateSuffix
            .expand(hashMap));
    }

    /**
     * Test of registration and unregistration of operators. Since operators are
     * registered in a static map, this test must not be executed in the same
     * time as the UriTemplate tests above.
     */
    // public void testRegistration() {
    // UriTemplateOperator[] registererOperators = registrar.getOperators();
    // assertEquals("all operators are in registrar", 6,
    // registererOperators.length);
    // UriTemplateOperator[] templateOperators = UriTemplate.getOperators();
    // assertEquals("all operators are registered", 6,
    // templateOperators.length);
    //
    // // use operators that were registered via Spring
    // new UriTemplate("{-list|/|var}");
    // new UriTemplate("{-join|/|var}");
    // new UriTemplate("{-neg|/|var}");
    // new UriTemplate("{-opt|/|var}");
    // new UriTemplate("{-prefix|/|var}");
    // new UriTemplate("{-suffix|/|var}");
    //
    // // unregister operators
    // registrar.unregister();
    //
    // // all operators were unregistered
    // templateOperators = UriTemplate.getOperators();
    // assertEquals("no operators are registered", 0, templateOperators.length);
    // try {
    // new UriTemplate("{-list|/|var}");
    // fail("no operator is registered");
    // } catch (IllegalArgumentException e) {
    // } // ok
    //
    // // register operators
    // registrar.register();
    // templateOperators = UriTemplate.getOperators();
    // assertEquals("all operators are registered again", 6,
    // templateOperators.length);
    // // use operators that were registered again
    // new UriTemplate("{-list|/|var}");
    // new UriTemplate("{-join|/|var}");
    // new UriTemplate("{-neg|/|var}");
    // new UriTemplate("{-opt|/|var}");
    // new UriTemplate("{-prefix|/|var}");
    // new UriTemplate("{-suffix|/|var}");
    // }
    private static void assertMatchTemplate(UriTemplateProcessor template,
                                            String uri,
                                            String[][] variables) {

        UriTemplateMatcher matcher = template.matcher();
        MultivaluedMap<String, String> varMap = matcher.match(uri);
        if (variables == null) {

            // must not match
            assertNull(varMap);
        } else {

            // number of variables must match
            assertEquals(variables.length, varMap.size());

            // all variable values must match
            for (String[] variable : variables) {
                if (variable.length > 1) {
                    String[] values = new String[variable.length - 1];
                    System.arraycopy(variable, 1, values, 0, variable.length - 1);
                    List<String> valuesList = varMap.get(variable[0]);
                    Object[] matchArray = new String[valuesList.size()];
                    if (valuesList != null) {
                        for (int i = 0; i < valuesList.size(); ++i) {
                            matchArray[i] = valuesList.get(i);
                        }
                    }
                    assertArrayEquals(values, matchArray);
                } else {
                    fail();
                }
            }
        }
    }
}
