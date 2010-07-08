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

import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.ws.rs.core.MultivaluedMap;

import junit.framework.TestCase;

import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.uritemplate.JaxRsUriTemplateProcessor;
import org.apache.wink.common.internal.uritemplate.UriTemplateMatcher;
import org.apache.wink.common.internal.uritemplate.UriTemplateProcessor;
import org.apache.wink.common.internal.uritemplate.JaxRsUriTemplateProcessor.JaxRsCompilationHandler;

public class JaxRsUriTemplateProcessorTest extends TestCase {

    private static class TestCompilationHandler implements JaxRsCompilationHandler {
        int      literalsCounter  = 0;
        int      variablesCounter = 0;
        String   template;
        String[] literals;
        String[] variables;
        String[] regexes;
        boolean  startFired       = false;
        boolean  endFired         = false;

        public TestCompilationHandler(String template,
                                      String[] literals,
                                      String[] variables,
                                      String[] regexes) {
            super();
            this.template = template;
            this.literals = literals;
            this.variables = variables;
            this.regexes = regexes;
        }

        public void startCompile(String uriTemplate) {
            assertEquals(template, uriTemplate);
            startFired = true;
        }

        public void literal(String literal) {
            assertEquals(literals[literalsCounter++], literal);
        }

        public void variable(String name, String regex) {
            assertEquals(variables[variablesCounter], name);
            assertEquals(regexes[variablesCounter++], regex);
        }

        public void endCompile(String literal) {
            assertEquals(literals[literalsCounter++], literal);
            endFired = true;
        }

        public void assertEvents() {
            assertTrue(startFired);
            assertTrue(endFired);
            assertEquals(literalsCounter, literals.length);
            assertEquals(variablesCounter, variables.length);
            assertEquals(variablesCounter, regexes.length);
        }
    }

    public void testStaticCompile() {
        // 1
        String template = "/path1/{var1}/path2{var2:[ab]*}/tail";
        String[] literals = new String[] {"/path1/", "/path2", "/tail"};
        String[] variables = new String[] {"var1", "var2"};
        String[] regexes = new String[] {null, "[ab]*"};
        TestCompilationHandler handler =
            new TestCompilationHandler(template, literals, variables, regexes);
        JaxRsUriTemplateProcessor.compile(template, handler);
        handler.assertEvents();

        // 2
        template = "/path1/and/tail";
        literals = new String[] {"/path1/and/tail"};
        variables = new String[] {};
        regexes = new String[] {};
        handler = new TestCompilationHandler(template, literals, variables, regexes);
        JaxRsUriTemplateProcessor.compile(template, handler);
        handler.assertEvents();

        // 3
        template = "{var1}{var2:[ab]+}{var3:(a\\{b\\}c)*}";
        literals = new String[] {"", "", "", ""};
        variables = new String[] {"var1", "var2", "var3"};
        regexes = new String[] {null, "[ab]+", "(a\\{b\\}c)*"};
        handler = new TestCompilationHandler(template, literals, variables, regexes);
        JaxRsUriTemplateProcessor.compile(template, handler);
        handler.assertEvents();

        // 4
        template = "";
        literals = new String[] {""};
        variables = new String[] {};
        regexes = new String[] {};
        handler = new TestCompilationHandler(template, literals, variables, regexes);
        JaxRsUriTemplateProcessor.compile(template, handler);
        handler.assertEvents();

        // 5
        try {
            JaxRsUriTemplateProcessor.compile(null, handler);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            JaxRsUriTemplateProcessor.compile(template, null);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    public void testStaticExpandString() {
        // 1
        String template = "/path1/{var1}/path2{var2:[ab]*}/tail";
        MultivaluedMap<String, String> values = new MultivaluedMapImpl<String, String>();
        values.add("var1", "value1");
        values.add("var2", "value2");
        String result = JaxRsUriTemplateProcessor.expand(template, values);
        assertEquals("/path1/value1/path2value2/tail", result);

        // 2
        template = "/path1/and/tail";
        result = JaxRsUriTemplateProcessor.expand(template, null);
        assertEquals("/path1/and/tail", result);

        // 3
        template = "{var1}{var2:[ab]+}{var3:(a\\{b\\}c)*}";
        values = new MultivaluedMapImpl<String, String>();
        values.add("var1", "value1");
        values.add("var2", "value2");
        values.add("var3", "value3");
        result = JaxRsUriTemplateProcessor.expand(template, values);
        assertEquals("value1value2value3", result);

        // 4
        template = "";
        result = JaxRsUriTemplateProcessor.expand(template, values);
        assertEquals("", result);

        // 5
        try {
            template = "{var1}";
            JaxRsUriTemplateProcessor.expand(template, null);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }

        // 5
        try {
            template = "{var1}";
            values = new MultivaluedMapImpl<String, String>();
            values.add("var1", null);
            JaxRsUriTemplateProcessor.expand(template, values);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    public void testStaticExpandStringBuilder() {
        // 1
        StringBuilder builder = new StringBuilder();
        String template = "/path1/{var1}/path2{var2:[ab]*}/tail";
        MultivaluedMap<String, String> values = new MultivaluedMapImpl<String, String>();
        values.add("var1", "value1");
        values.add("var2", "value2");
        JaxRsUriTemplateProcessor.expand(template, values, builder);
        String result = builder.toString();
        assertEquals("/path1/value1/path2value2/tail", result);

        // 2
        template = "/path1/and/tail";
        builder = new StringBuilder();
        JaxRsUriTemplateProcessor.expand(template, null, builder);
        result = builder.toString();
        assertEquals("/path1/and/tail", result);

        // 3
        template = "{var1}{var2:[ab]+}{var3:(a\\{b\\}c)*}";
        values = new MultivaluedMapImpl<String, String>();
        values.add("var1", "value1");
        values.add("var2", "value2");
        values.add("var3", "value3");
        builder = new StringBuilder();
        JaxRsUriTemplateProcessor.expand(template, values, builder);
        result = builder.toString();
        assertEquals("value1value2value3", result);

        // 4
        template = "";
        builder = new StringBuilder();
        JaxRsUriTemplateProcessor.expand(template, values, builder);
        result = builder.toString();
        assertEquals("", result);

        // 5
        try {
            template = "{var1}";
            JaxRsUriTemplateProcessor.expand(template, null, builder);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }

        // 5
        try {
            template = "{var1}";
            values = new MultivaluedMapImpl<String, String>();
            values.add("var1", null);
            JaxRsUriTemplateProcessor.expand(template, values, builder);
            fail("expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    public void testMatches() {
        String template = "/path1/{var1}/path2{var2:[ab]*}/tail";
        JaxRsUriTemplateProcessor processor = new JaxRsUriTemplateProcessor(template);
        UriTemplateMatcher matcher = processor.matcher();
        assertEquals(template, processor.getTemplate());

        boolean matches = matcher.matches("/path1/value1/path2/tail");
        assertTrue(matches);
        matches = matcher.matches("/path1/value1/path2ab/tail");
        assertTrue(matches);
        matches = matcher.matches("/path1/value1/path2ababab/tail");
        assertTrue(matches);
        matches = matcher.matches("/path1/value2/path2/tail");
        assertTrue(matches);

        matches = matcher.matches("/path1/value1/path2c/tail");
        assertFalse(matches);
        matches = matcher.matches("/path2/value1/path2ab/tail");
        assertFalse(matches);
        matches = matcher.matches("/path1/value1/path2c/tailZ");
        assertFalse(matches);
    }

    public void testGetVariables() {
        JaxRsUriTemplateProcessor processor =
            new JaxRsUriTemplateProcessor("/path1/{var1}/path2{var2:[ab]*}/{var1}");
        UriTemplateMatcher matcher = processor.matcher();
        matcher.matches("/path1/value%20a/path2abab/valueB/tail%20part");

        // variable value
        assertEquals("value a", matcher.getVariableValue("var1"));
        assertEquals("value%20a", matcher.getVariableValue("var1", false));
        assertEquals("abab", matcher.getVariableValue("var2"));
        assertEquals("abab", matcher.getVariableValue("var2", false));
        assertNull(matcher.getVariableValue("var3"));

        // variable values as list
        List<String> varValues = matcher.getVariableValues("var1");
        assertEquals(2, varValues.size());
        assertEquals("value a", varValues.get(0));
        assertEquals("valueB", varValues.get(1));
        varValues = matcher.getVariableValues("var1", false);
        assertEquals(2, varValues.size());
        assertEquals("value%20a", varValues.get(0));
        assertEquals("valueB", varValues.get(1));
        varValues = matcher.getVariableValues("var2");
        assertEquals(1, varValues.size());
        assertEquals("abab", varValues.get(0));

        // variable names set
        Set<String> variableNames = processor.getVariableNames();
        assertEquals(2, variableNames.size());
        assertTrue(variableNames.contains("var1"));
        assertTrue(variableNames.contains("var2"));

        // get all variables
        // decoded
        MultivaluedMap<String, String> allVariables = matcher.getVariables(true);
        assertEquals(2, allVariables.size());
        varValues = allVariables.get("var1");
        assertEquals(2, varValues.size());
        assertEquals("value a", varValues.get(0));
        assertEquals("valueB", varValues.get(1));
        varValues = allVariables.get("var2");
        assertEquals(1, varValues.size());
        assertEquals("abab", varValues.get(0));

        // encoded
        allVariables = matcher.getVariables(false);
        assertEquals(2, allVariables.size());
        varValues = allVariables.get("var1");
        assertEquals(2, varValues.size());
        assertEquals("value%20a", varValues.get(0));
        assertEquals("valueB", varValues.get(1));
        varValues = allVariables.get("var2");
        assertEquals(1, varValues.size());
        assertEquals("abab", varValues.get(0));
    }

    public void testTail() {
        JaxRsUriTemplateProcessor processor =
            new JaxRsUriTemplateProcessor("/path1/{var1}/path2{var2:[ab]*}/{var3}");
        UriTemplateMatcher matcher = processor.matcher();
        matcher.matches("/path1/value%20a/path2abab/valueB/tail%20part");

        String tail = matcher.getTail();
        assertEquals("/tail part", tail);
        assertEquals("/tail%20part", matcher.getTail(false));

        matcher.matches("/path1/value%20a/path2abab/valueB/");
        tail = matcher.getTail();
        assertEquals("/", tail);

        matcher.matches("/path1/value%20a/path2abab/valueB");
        tail = matcher.getTail();
        assertEquals("", tail);
    }

    public void testHead() {
        JaxRsUriTemplateProcessor processor =
            new JaxRsUriTemplateProcessor("/path1/{var1}/path2{var2:[ab]*}/{var3}");
        UriTemplateMatcher matcher = processor.matcher();
        matcher.matches("/path1/value%20a/path2abab/valueB/tail%20part");

        String head = matcher.getHead();
        assertEquals("/path1/value a/path2abab/valueB", head);
        assertEquals("/path1/value%20a/path2abab/valueB", matcher.getHead(false));

        matcher.matches("/path1/value%20a/path2abab/valueB/");
        head = matcher.getHead();
        assertEquals("/path1/value a/path2abab/valueB", head);

        matcher.matches("/path1/value%20a/path2abab/valueB");
        head = matcher.getHead();
        assertEquals("/path1/value a/path2abab/valueB", head);
    }

    public void testSameVariableTwice() {
        JaxRsUriTemplateProcessor processor =
            new JaxRsUriTemplateProcessor("/path1/{var1}/path2{var1:[ab]*}/tail");
        UriTemplateMatcher matcher = processor.matcher();
        boolean matches = matcher.matches("/path1/value%20a/path2abab/tail");
        assertTrue(matches);
        assertEquals("value a", matcher.getVariableValue("var1"));
        assertEquals("value%20a", matcher.getVariableValue("var1", false));

        List<String> varValues = matcher.getVariableValues("var1");
        assertEquals(2, varValues.size());
        assertEquals("value a", varValues.get(0));
        assertEquals("abab", varValues.get(1));

        varValues = matcher.getVariableValues("var1", false);
        assertEquals(2, varValues.size());
        assertEquals("value%20a", varValues.get(0));
        assertEquals("abab", varValues.get(1));
    }

    public void testTemplateWithCapturingGroupsInRegex() {
        JaxRsUriTemplateProcessor processor =
            new JaxRsUriTemplateProcessor("/path1/{var1}/{var2:(a)(b)*}/path2/{var3}/tail");
        UriTemplateMatcher matcher = processor.matcher();
        boolean matches = matcher.matches("/path1/cc/abb/path2/dd/tail");
        assertTrue(matches);
        assertEquals("cc", matcher.getVariableValue("var1"));
        assertEquals("abb", matcher.getVariableValue("var2"));
        assertEquals("dd", matcher.getVariableValue("var3"));
        assertNull(matcher.getVariableValue("var4"));

        assertEquals(1, matcher.getVariableValues("var1").size());
        assertEquals(1, matcher.getVariableValues("var2").size());
        assertEquals(1, matcher.getVariableValues("var3").size());

        matches = matcher.matches("/path1/cc/bb/path2/dd/tail");
        assertFalse(matches);
    }

    public void testCompileMatchExpand() {
        JaxRsUriTemplateProcessor processor = new JaxRsUriTemplateProcessor();
        // 1
        processor.compile("/path1/{var1}");
        UriTemplateMatcher matcher = processor.matcher();
        boolean matches = matcher.matches("/path1/abc");
        assertTrue(matches);
        MultivaluedMap<String, String> values = new MultivaluedMapImpl<String, String>();
        values.add("var1", "xyz");
        String expanded = processor.expand(values);
        assertEquals("/path1/xyz", expanded);

        // 2
        processor.compile("/path2/{var1}");
        matcher = processor.matcher();
        matches = matcher.matches("/path1/abc");
        assertFalse(matches);
        matches = matcher.matches("/path2/abc");
        assertTrue(matches);
        values = new MultivaluedMapImpl<String, String>();
        values.add("var1", "xyz");
        expanded = processor.expand(values);
        assertEquals("/path2/xyz", expanded);
    }

    public void testNewNormalizedInstance() {
        UriTemplateProcessor processor =
            JaxRsUriTemplateProcessor.newNormalizedInstance("/path1/path2/./../path3");
        assertEquals("path1/path3", processor.getTemplate());

        processor = (JaxRsUriTemplateProcessor)JaxRsUriTemplateProcessor.newNormalizedInstance("");
        UriTemplateMatcher matcher = processor.matcher();
        boolean matches = matcher.matches("");
        assertTrue(matches);
        assertTrue(matcher.isExactMatch());

        processor = (JaxRsUriTemplateProcessor)JaxRsUriTemplateProcessor.newNormalizedInstance("/");
        matcher = processor.matcher();
        matches = matcher.matches("");
        assertTrue(matches);
        assertTrue(matcher.isExactMatch());

    }

    public void testIllegalStates() {
        JaxRsUriTemplateProcessor processor = new JaxRsUriTemplateProcessor();
        assertNull(processor.getTemplate());

        // not yet compiled
        try {
            processor.matcher();
            fail("expected IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
        }

        try {
            processor.expand(new MultivaluedMapImpl<String, String>());
            fail("expected IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
        }

        try {
            processor.getPatternString();
            fail("expected IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
        }

        try {
            processor.getVariableNames();
            fail("expected IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
        }

        // compiled but not matched
        processor.compile("/path1/{var1}");
        UriTemplateMatcher matcher = processor.matcher();
        assertFalse(matcher.matches("/path2"));

        try {
            matcher.getVariables(false);
            fail("expected IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
        }

        try {
            matcher.getVariableValue("var1");
            fail("expected IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
        }

        try {
            matcher.getVariableValue("var1", false);
            fail("expected IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
        }

        try {
            matcher.getVariableValues("var1");
            fail("expected IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
        }

        try {
            matcher.getVariableValues("var1", false);
            fail("expected IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
        }

        try {
            matcher.getTail();
            fail("expected IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
        }

        try {
            matcher.getTail(false);
            fail("expected IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
        }
    }

    public void testBadUserRegex() {
        try {
            new JaxRsUriTemplateProcessor("path/{var:?.*}");
            fail("expected PatternSyntaxException to be thrown");
        } catch (PatternSyntaxException e) {
        }
    }

    public void testBadTemplateForm() {
        try {
            new JaxRsUriTemplateProcessor("/path{/{var:?.*}");
            fail("expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
        }

        try {
            new JaxRsUriTemplateProcessor("/path{}/{var:?.*}");
            fail("expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCompareTo() {
        JaxRsUriTemplateProcessor p1 = new JaxRsUriTemplateProcessor();
        JaxRsUriTemplateProcessor p2 = new JaxRsUriTemplateProcessor();

        p1.compile("/path1/path2");
        p2.compile("/path1/path2");
        assertTrue(p1.compareTo(p2) == 0);

        p1.compile("/path2/path1");
        p2.compile("/path1/path2");
        assertTrue(p1.compareTo(p2) == 0);

        p1.compile("/path1/path2/path3");
        p2.compile("/path1/path2");
        assertTrue(p1.compareTo(p2) > 0);

        p1.compile("/path1/path2");
        p2.compile("/path1/path2/path3");
        assertTrue(p1.compareTo(p2) < 0);

        p1.compile("/path1/path2/{var1}");
        p2.compile("/path1/path2/{var1}");
        assertTrue(p1.compareTo(p2) == 0);

        p1.compile("/path1/path2/{var1}");
        p2.compile("/path1/path2/{variable1}");
        assertTrue(p1.compareTo(p2) == 0);

        p1.compile("/path1/path2/{var1}");
        p2.compile("/path1/{variable1}/path2");
        assertTrue(p1.compareTo(p2) == 0);

        p1.compile("/path1/path2/path3/{var1}");
        p2.compile("/path1/path2/{var1}");
        assertTrue(p1.compareTo(p2) > 0);

        p1.compile("/path1/path2/{var1}");
        p2.compile("/path1/path2/path3/{var1}");
        assertTrue(p1.compareTo(p2) < 0);

        p1.compile("/path1/path2/{var1}/{var2}");
        p2.compile("/path1/path2/{var1}");
        assertTrue(p1.compareTo(p2) > 0);

        p1.compile("/path1/path2/{var1}");
        p2.compile("/path1/path2/{var1}/{var2}");
        assertTrue(p1.compareTo(p2) < 0);

        p1.compile("/path1/path2/path3/{var1}");
        p2.compile("/path1/path2/{var1}/{var2}");
        assertTrue(p1.compareTo(p2) > 0);

        p1.compile("/path1/path2/{var1}/{var2}");
        p2.compile("/path1/path2/path3/{var1}");
        assertTrue(p1.compareTo(p2) < 0);

        p1.compile("/path1/path2/{var1}/{var2:.*}");
        p2.compile("/path1/path2/{var1}/{var2:.*}");
        assertTrue(p1.compareTo(p2) == 0);

        p1.compile("/path1/path2/{var1}/{var2:.*}");
        p2.compile("/path1/path2/{var1}/{var2}");
        assertTrue(p1.compareTo(p2) > 0);

        p1.compile("/path1/path2/{var1}/{var2}");
        p2.compile("/path1/path2/{var1}/{var2:.*}");
        assertTrue(p1.compareTo(p2) < 0);

        p1.compile("/path1/path2/{var1:.*}/{var2}");
        p2.compile("/path1/path2/{var1}/{var2:.*}");
        assertTrue(p1.compareTo(p2) == 0);
    }
}
