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

import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.apache.wink.common.internal.http.AcceptEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AcceptEncodingHeaderDelegate implements HeaderDelegate<AcceptEncoding> {

    final private static Logger logger =
                                           LoggerFactory
                                               .getLogger(AcceptEncodingHeaderDelegate.class);

    public AcceptEncoding fromString(String value) throws IllegalArgumentException {
        List<String> acceptable = new LinkedList<String>();
        List<String> banned = new LinkedList<String>();
        boolean anyAllowed = (value == null);

        // parse the Accept-Encoding header
        List<AcceptEncoding.ValuedEncoding> vEncodings = parseAcceptEncoding(value);

        for (AcceptEncoding.ValuedEncoding qEncoding : vEncodings) {
            logger.debug("Processing {} with qValue {}", qEncoding.encoding, qEncoding.qValue); //$NON-NLS-1$
            if (anyAllowed) {
                logger.debug("anyAllowed is true"); //$NON-NLS-1$
                if (qEncoding.qValue == 0 && !qEncoding.isWildcard()) {
                    logger.debug("qValue is 0 and qEncoding is not a wildcard so {} is banned", //$NON-NLS-1$
                                 qEncoding.encoding);
                    banned.add(qEncoding.encoding);
                }
            } else {
                logger.debug("anyAllowed is still false"); //$NON-NLS-1$
                if (qEncoding.qValue == 0) {
                    logger.debug("qValue is 0 so breaking out of loop"); //$NON-NLS-1$
                    break; // gone through all acceptable languages
                }
                if (qEncoding.isWildcard()) {
                    logger.debug("qEncoding is a wildcard so everything afterwards is allowable"); //$NON-NLS-1$
                    anyAllowed = true;
                } else {
                    logger.debug("qEncoding is not a wildcard so adding to acceptable list"); //$NON-NLS-1$
                    acceptable.add(qEncoding.encoding);
                }
            }
        }
        return new AcceptEncoding(value, acceptable, banned, anyAllowed, vEncodings);
    }

    private List<AcceptEncoding.ValuedEncoding> parseAcceptEncoding(String acceptableEncodingValue) {
        logger.debug("parseAcceptEncoding({}) entry", acceptableEncodingValue); //$NON-NLS-1$
        List<AcceptEncoding.ValuedEncoding> qEncodings =
            new LinkedList<AcceptEncoding.ValuedEncoding>();
        if (acceptableEncodingValue == null) {
            logger.debug("parseAcceptEncoding() exit - return empty list"); //$NON-NLS-1$
            return qEncodings;
        }

        for (String encodingRange : acceptableEncodingValue.split(",")) { //$NON-NLS-1$
            logger.debug("Parsing encodingRange as {}", encodingRange); //$NON-NLS-1$
            int semicolonIndex = encodingRange.indexOf(';');
            double qValue;
            String encodingSpec;
            if (semicolonIndex == -1) {
                qValue = 1.0d;
                encodingSpec = encodingRange;
            } else {
                encodingSpec = encodingRange.substring(0, semicolonIndex);
                int equalsIndex = encodingRange.indexOf('=', semicolonIndex + 1);
                String qString =
                    encodingRange.substring(equalsIndex != -1 ? equalsIndex + 1 : encodingRange
                        .length());
                try {
                    qValue = Double.parseDouble(qString.trim());
                } catch (NumberFormatException nfe) {
                    // silently ignore incorrect q-specification and assume 1
                    qValue = 1.0d;
                }
            }
            logger.debug("encodingSpec before trim is {}", encodingSpec); //$NON-NLS-1$
            logger.debug("qValue is {}", qValue); //$NON-NLS-1$
            encodingSpec = encodingSpec.trim();
            if (encodingSpec.length() == 0) {
                // ignore empty encoding specifications
                logger.debug("ignoring empty encodingSpec"); //$NON-NLS-1$
                continue;
            } else if (encodingSpec.equals("*")) { //$NON-NLS-1$
                logger.debug("Wildcard spec so adding as wildcard"); //$NON-NLS-1$
                qEncodings.add(new AcceptEncoding.ValuedEncoding(qValue, null));
            } else {
                qEncodings.add(new AcceptEncoding.ValuedEncoding(qValue, encodingSpec));
            }
        }
        Collections.sort(qEncodings, Collections.reverseOrder());
        logger.debug("parseAcceptEncoding exit() returning {}", qEncodings); //$NON-NLS-1$
        return qEncodings;
    }

    public String toString(AcceptEncoding value) {
        return value.getAcceptEncodingHeader();
    }
}
