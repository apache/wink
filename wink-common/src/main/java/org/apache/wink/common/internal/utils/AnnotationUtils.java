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
package org.apache.wink.common.internal.utils;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class AnnotationUtils {

    /**
     * Utility method to parse the values array returned from the @Consumes or @Produces
     * annotation. JAX-RS 1.1 E012 allows example syntax:
     * 
     * @Consumes({"text/xml, application/xml", "text/plain"}) The annotation
     *                       value() method will give an array with two strings,
     *                       not the desired three, hence the need for this
     *                       utility method.
     * @param values
     * @return String[] representing the media type values declared in the
     *         annotation
     */
    public static String[] parseConsumesProducesValues(String[] values) {
        ArrayList<String> strings = new ArrayList<String>();
        for (String v : values) {
            StringTokenizer tokenizer = new StringTokenizer(v, ",");
            while (tokenizer.hasMoreTokens()) {
                strings.add(tokenizer.nextToken().trim());
            }
        }
        return (String[])strings.toArray(new String[] {});
    }

}
