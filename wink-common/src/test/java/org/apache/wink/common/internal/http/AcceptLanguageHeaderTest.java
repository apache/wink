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

package org.apache.wink.common.internal.http;

import java.util.Arrays;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * Unit test of AcceptLanguageHeader.
 */
public class AcceptLanguageHeaderTest extends TestCase {

    public void testNoValue() {
        AcceptLanguage alh = AcceptLanguage.valueOf(null);
        assertTrue("wildcard", alh.isAnyLanguageAllowed());
        assertTrue("empty acceptable", alh.getAcceptableLanguages().isEmpty());
        assertTrue("empty banned", alh.getBannedLanguages().isEmpty());
    }

    public void testEmptyValue() {
        AcceptLanguage alh = AcceptLanguage.valueOf(" ");
        assertFalse("wildcard", alh.isAnyLanguageAllowed());
        assertTrue("empty acceptable", alh.getAcceptableLanguages().isEmpty());
        assertTrue("empty banned", alh.getBannedLanguages().isEmpty());
    }

    public void testTrivialCase() {
        AcceptLanguage alh = AcceptLanguage.valueOf("en-gb-x-y,en");
        assertFalse("wildcard", alh.isAnyLanguageAllowed());
        assertEquals("locales", Arrays.asList(new Locale[] {new Locale("en", "gb", "x-y"),
            new Locale("en")}), alh.getAcceptableLanguages());
        assertTrue("empty banned", alh.getBannedLanguages().isEmpty());
    }

    public void testWithoutWildcard() {
        AcceptLanguage alh = AcceptLanguage.valueOf("da, en;q=0.7,  en-gb;q=0.8");
        assertFalse("wildcard", alh.isAnyLanguageAllowed());
        assertEquals("locales", Arrays.asList(new Locale[] {new Locale("da"),
            new Locale("en", "gb"), new Locale("en")}), alh.getAcceptableLanguages());
        assertTrue("empty banned", alh.getBannedLanguages().isEmpty());
    }

    public void testWithoutZero() {
        AcceptLanguage alh = AcceptLanguage.valueOf("da, en;q=0.7,  en-gb;q=0.8, jp;q=0");
        assertFalse("wildcard", alh.isAnyLanguageAllowed());
        assertEquals("locales", Arrays.asList(new Locale[] {new Locale("da"),
            new Locale("en", "gb"), new Locale("en")}), alh.getAcceptableLanguages());
        assertTrue("empty banned", alh.getBannedLanguages().isEmpty());
    }

    public void testWithWildcardNoBanned() {
        AcceptLanguage alh = AcceptLanguage.valueOf("da, *;q=0.755, en;q=0.7, en-gb;q=0.8");
        assertTrue("wildcard", alh.isAnyLanguageAllowed());
        assertEquals("locales", Arrays.asList(new Locale[] {new Locale("da"),
            new Locale("en", "gb")}), alh.getAcceptableLanguages());
        assertTrue("empty banned", alh.getBannedLanguages().isEmpty());
    }

    public void testWithWildcard() {
        AcceptLanguage alh =
            AcceptLanguage.valueOf("ru;q= 0, da, *;q=0.755, en;q=0.7, en-gb;q=0.8, cz;q=0");
        assertTrue("wildcard", alh.isAnyLanguageAllowed());
        assertEquals("locales", Arrays.asList(new Locale[] {new Locale("da"),
            new Locale("en", "gb")}), alh.getAcceptableLanguages());
        assertEquals("banned",
                     Arrays.asList(new Locale[] {new Locale("ru"), new Locale("cz")}),
                     alh.getBannedLanguages());
    }

    public void testIllegalQ() {
        AcceptLanguage alh = AcceptLanguage.valueOf("da;q=one");
        assertFalse("wildcard", alh.isAnyLanguageAllowed());
        assertEquals("locales", Arrays.asList(new Locale[] {new Locale("da")}), alh
            .getAcceptableLanguages());
        assertTrue("empty banned", alh.getBannedLanguages().isEmpty());
    }

}
