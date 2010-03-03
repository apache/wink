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
        List<AcceptLanguage.ValuedLocale> qLocales = parseAcceptLanguage(value);

        for (AcceptLanguage.ValuedLocale qLocale : qLocales) {
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
        return new AcceptLanguage(value, acceptable, banned, anyAllowed, qLocales);
    }

    private List<AcceptLanguage.ValuedLocale> parseAcceptLanguage(String acceptLanguageValue) {
        List<AcceptLanguage.ValuedLocale> qLocales = new LinkedList<AcceptLanguage.ValuedLocale>();
        if (acceptLanguageValue == null) {
            return qLocales;
        }

        for (String languageRange : acceptLanguageValue.split(",")) { //$NON-NLS-1$
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
            } else if (languageSpec.equals("*")) { //$NON-NLS-1$
               qLocales.add(new AcceptLanguage.ValuedLocale(qValue, null));
            } else {
                Locale newLocale = HeaderUtils.languageToLocale(languageSpec);
                qLocales.add(new AcceptLanguage.ValuedLocale(qValue, newLocale));
            }
        }
        Collections.sort(qLocales, Collections.reverseOrder());
        return qLocales;
    }

    public String toString(AcceptLanguage value) {
        return value.getAcceptLanguageHeader();
    }

}
