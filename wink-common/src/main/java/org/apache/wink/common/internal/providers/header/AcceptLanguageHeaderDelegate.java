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

package org.apache.wink.common.internal.providers.header;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.http.AcceptLanguage;
import org.apache.wink.common.internal.utils.HeaderUtils;

public class AcceptLanguageHeaderDelegate implements HeaderDelegate<AcceptLanguage> {

    public AcceptLanguage fromString(String value) throws IllegalArgumentException {
        List<Locale> acceptable = new LinkedList<Locale>();
        List<Locale> banned = new LinkedList<Locale>();
        boolean anyAllowed = (value == null);

        // parse the Accept-Language header
        List<ValuedLocale> qLocales = parseAcceptLanguage(value);

        for (ValuedLocale qLocale : qLocales) {
            if (anyAllowed) {
                if (qLocale.qValue == 0 && !qLocale.isWildcard()) {
                    banned.add(qLocale.locale);
                }
            } else {
                if (qLocale.qValue == 0) {
                    break; // gone through all acceptable languages
                }
                if (qLocale.isWildcard()) {
                    anyAllowed = true;
                } else {
                    acceptable.add(qLocale.locale);
                }
            }
        }
        return new AcceptLanguage(value, acceptable, banned, anyAllowed);
    }

    private List<ValuedLocale> parseAcceptLanguage(String acceptLanguageValue) {
        List<ValuedLocale> qLocales = new LinkedList<ValuedLocale>();
        if (acceptLanguageValue == null) {
            return qLocales;
        }

        for (String languageRange : acceptLanguageValue.split(",")) {
            int semicolonIndex = languageRange.indexOf(';');
            double qValue;
            String languageSpec;
            if (semicolonIndex == -1) {
                qValue = 1.0d;
                languageSpec = languageRange;
            } else {
                languageSpec = languageRange.substring(0, semicolonIndex);
                int equalsIndex = languageRange.indexOf('=', semicolonIndex + 1);
                String qString =
                    languageRange.substring(equalsIndex != -1 ? equalsIndex + 1 : languageRange
                        .length());
                try {
                    qValue = Double.parseDouble(qString.trim());
                } catch (NumberFormatException nfe) {
                    // silently ignore incorrect q-specification and assume 1
                    qValue = 1.0d;
                }
            }
            languageSpec = languageSpec.trim();
            if (languageSpec.length() == 0) {
                // ignore empty language specifications
                continue;
            } else if (languageSpec.equals("*")) {
                qLocales.add(new ValuedLocale(qValue, null));
            } else {
                Locale newLocale = HeaderUtils.languageToLocale(languageSpec);
                qLocales.add(new ValuedLocale(qValue, newLocale));
            }
        }
        Collections.sort(qLocales, Collections.reverseOrder());
        return qLocales;
    }

    private static final class ValuedLocale implements Comparable<ValuedLocale> {
        final double qValue;
        final Locale locale;

        ValuedLocale(double qValue, Locale locale) {
            this.qValue = qValue;
            this.locale = locale;
        }

        public int compareTo(ValuedLocale other) {
            return Double.compare(qValue, other.qValue);
        }

        boolean isWildcard() {
            return locale == null;
        }
    }

    public String toString(AcceptLanguage value) {
        return value.getAcceptLanguageHeader();
    }

}
