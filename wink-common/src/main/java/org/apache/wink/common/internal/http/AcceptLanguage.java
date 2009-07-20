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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * Represent HTTP Accept-Language header.
 * <p>
 * This version of the API does not support construction.
 * 
 * @see <a href='http://tools.ietf.org/html/rfc2616#section-14.4'>RFC 2616
 *      14.4</a>
 */
public class AcceptLanguage {

    private static final HeaderDelegate<AcceptLanguage> delegate =
                                                                     RuntimeDelegate
                                                                         .getInstance()
                                                                         .createHeaderDelegate(AcceptLanguage.class);

    private final String                                acceptLanguageHeader;
    private final boolean                               anyAllowed;
    private final List<Locale>                          acceptable;
    private final List<Locale>                          banned;

    public AcceptLanguage(String acceptLanguageValue,
                          List<Locale> acceptableLanguages,
                          List<Locale> bannedLanguages,
                          boolean anyLanguageAllowed) {
        this.acceptLanguageHeader = acceptLanguageValue;
        this.anyAllowed = anyLanguageAllowed;
        this.acceptable = Collections.unmodifiableList(acceptableLanguages);
        this.banned = Collections.unmodifiableList(bannedLanguages);
    }

    // /**
    // * Constructor.
    // * @param acceptLanguageValue value of the header; <code>null</code> is
    // possible
    // * with meaning that the header is not present
    // */
    // public AcceptLanguage(String acceptLanguageValue) {
    // if (acceptLanguageValue == null) {
    // this.anyLanguageAllowed = true;
    // this.acceptableLanguages = Collections.emptyList();
    // this.bannedLanguages = Collections.emptyList();
    // } else {
    // List<Locale> result = new ArrayList<Locale>();
    // List<Locale> bannedLanguages = new ArrayList<Locale>();
    // boolean anyAccepted = false;
    // List<QLocale> qLocales = parseAcceptLanguage(acceptLanguageValue);
    // for (QLocale qLocale : qLocales) {
    // if (anyAccepted) {
    // if (qLocale.qValue.equals(BigDecimal.ZERO) && ! qLocale.isWildcard()) {
    // bannedLanguages.add(qLocale.locale);
    // }
    // } else {
    // if (qLocale.qValue.equals(BigDecimal.ZERO)) {
    // break; // gone through all acceptable languages
    // }
    // if (qLocale.isWildcard()) {
    // anyAccepted = true;
    // } else {
    // result.add(qLocale.locale);
    // }
    // }
    // }
    // this.anyLanguageAllowed = anyAccepted;
    // this.acceptableLanguages = Collections.unmodifiableList(result);
    // this.bannedLanguages = Collections.unmodifiableList(bannedLanguages);
    // }
    // }
    //
    // private List<QLocale> parseAcceptLanguage(String acceptLanguageValue) {
    // List<QLocale> qLocales = new ArrayList<QLocale>();
    // for (String languageRange : acceptLanguageValue.split(",")) {
    // int semicolonIndex = languageRange.indexOf(';');
    // BigDecimal qValue;
    // String languageSpec;
    // if (semicolonIndex == -1) {
    // qValue = BigDecimal.ONE;
    // languageSpec = languageRange;
    // } else {
    // languageSpec = languageRange.substring(0, semicolonIndex);
    // int equalsIndex = languageRange.indexOf('=', semicolonIndex + 1);
    // String qString = languageRange.substring(equalsIndex != -1 ? equalsIndex
    // + 1: languageRange.length());
    // try {
    // qValue = new BigDecimal(qString.trim());
    // } catch (NumberFormatException nfe) {
    // // silently ignore incorrect q-specification and assume 1
    // qValue = BigDecimal.ONE;
    // }
    // }
    // languageSpec = languageSpec.trim();
    // if (languageSpec.length() == 0) {
    // // ignore empty language specifications
    // continue;
    // } else if (languageSpec.equals("*")) {
    // qLocales.add( new QLocale(qValue, null) );
    // } else {
    // String[] languageSplit = languageSpec.split("-", 3);
    // Locale newLocale = new Locale(languageSplit[0].trim(),
    // languageSplit.length > 1 ? languageSplit[1].trim() : "",
    // languageSplit.length > 2 ? languageSplit[2].trim() : "");
    // qLocales.add( new QLocale(qValue, newLocale) );
    // }
    // }
    // Collections.sort(qLocales);
    // return qLocales;
    // }
    //
    // private static final class QLocale implements Comparable<QLocale> {
    //
    // final BigDecimal qValue;
    // final Locale locale;
    //
    // QLocale(BigDecimal qValue, Locale locale) {
    // this.qValue = qValue;
    // this.locale = locale;
    // }
    //
    // public int compareTo(QLocale other) {
    // return -this.qValue.compareTo(other.qValue); // descending
    // }
    //
    // boolean isWildcard() {
    // return locale == null;
    // }
    //        
    // } // class QLocale

    /**
     * Provide a list of languages (locales) which are acceptable for the
     * client. If any language is acceptable with some non-zero priority (see
     * {@link #isAnyLanguageAllowed()}), only languages more preferable than
     * wildcard are listed.
     * 
     * @return unmodifiable list, never <code>null</code>; the list is sorted
     *         starting with the most preferable language
     */
    public List<Locale> getAcceptableLanguages() {
        return acceptable;
    }

    /**
     * Is any language acceptable? Note that expresions are listed by
     * {@link #getBannedLanguages()}. This means that the value contains
     * wildcard (with non-zero priority) of the header is not present at all.
     * 
     * @return <code>true</code> if any language is acceptable
     */
    public boolean isAnyLanguageAllowed() {
        return anyAllowed;
    }

    /**
     * A list of non-acceptable (q-value 0) languages, i.e. exception of
     * {@link #isAnyLanguageAllowed()}.
     * 
     * @return never <code>null</code>; always empty if wildcard is not included
     */
    public List<Locale> getBannedLanguages() {
        return banned;
    }

    /**
     * Creates a new instance of AcceptLanguage by parsing the supplied string.
     * 
     * @param value the Accept-Languages string
     * @return the newly created AcceptLanguages
     * @throws IllegalArgumentException if the supplied string cannot be parsed
     */
    public static AcceptLanguage valueOf(String value) throws IllegalArgumentException {
        return delegate.fromString(value);
    }

    public String getAcceptLanguageHeader() {
        return acceptLanguageHeader;
    }

    @Override
    public String toString() {
        return delegate.toString(this);
    }

}
