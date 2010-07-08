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

package org.apache.wink.providers.json;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONUtils {

    public static JSONObject objectForString(final String input) throws JSONException {
        assert input != null;
        JSONTokener tok = new JSONTokener(input);
        return new JSONObject(tok);
    }

    public static boolean equals(JSONObject expected, JSONObject actual) {
        return isJSONEquals(expected, actual);
    }

    public static boolean equals(JSONArray expected, JSONArray actual) {
        return isJSONEquals(expected, actual);
    }

    private static boolean isJSONEquals(Object expected, Object actual) {
        if (expected == actual) {
            return true;
        }
        if (expected == null || actual == null) {
            return false;
        }
        if (!expected.getClass().equals(actual.getClass())) {
            return false;
        }
        if (expected instanceof JSONObject) {
            JSONObject jsonExpected = (JSONObject)expected;
            JSONObject jsonActual = (JSONObject)actual;
            if (jsonExpected.length() != jsonActual.length()) {
                return false;
            }
            Iterator<?> keys = jsonExpected.keys();
            while (keys.hasNext()) {
                String key = (String)keys.next();
                if (!isJSONEquals(jsonExpected.opt(key), jsonActual.opt(key))) {
                    return false;
                }
            }
            return true;
        }
        if (expected instanceof JSONArray) {
            JSONArray jsonExpected = (JSONArray)expected;
            JSONArray jsonActual = (JSONArray)actual;
            if (jsonExpected.length() != jsonActual.length()) {
                return false;
            }
            for (int i = 0; i < jsonExpected.length(); ++i) {
                if (!isJSONEquals(jsonExpected.opt(i), jsonActual.opt(i))) {
                    return false;
                }
            }
            return true;
        }

        // all other objects
        return expected.equals(actual);
    }
}
