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

import static org.junit.Assert.assertArrayEquals;
import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {

    public void testFastSplit() {
        String[] result = StringUtils.fastSplit("", ",");
        assertArrayEquals(new String[] {""}, result);

        result = StringUtils.fastSplit(",", ",");
        assertArrayEquals(new String[] {"", ""}, result);

        result = StringUtils.fastSplit(",", ",", false);
        assertArrayEquals(new String[] {""}, result);

        result = StringUtils.fastSplit(null, ",");
        assertArrayEquals(new String[] {}, result);

        result = StringUtils.fastSplit("a", ",");
        assertArrayEquals(new String[] {"a"}, result);

        result = StringUtils.fastSplit("a,b", ",");
        assertArrayEquals(new String[] {"a", "b"}, result);

        result = StringUtils.fastSplit("a,b,c", ",");
        assertArrayEquals(new String[] {"a", "b", "c"}, result);

        result = StringUtils.fastSplit("a,b,c", ",");
        assertArrayEquals(new String[] {"a", "b", "c"}, result);

        result = StringUtils.fastSplit("a,b,", ",");
        assertArrayEquals(new String[] {"a", "b", ""}, result);

        result = StringUtils.fastSplit("a,b,", ",", true);
        assertArrayEquals(new String[] {"a", "b", ""}, result);

        result = StringUtils.fastSplit("a,b,", ",", false);
        assertArrayEquals(new String[] {"a", "b"}, result);

        result = StringUtils.fastSplit(",a,b", ",");
        assertArrayEquals(new String[] {"", "a", "b"}, result);

        result = StringUtils.fastSplit(",a,b,", ",");
        assertArrayEquals(new String[] {"", "a", "b", ""}, result);
    }

    public void testFastSplitTemplate() {
        String[] result = StringUtils.fastSplitTemplate("", ",");
        assertArrayEquals(new String[] {""}, result);

        result = StringUtils.fastSplitTemplate(",", ",");
        assertArrayEquals(new String[] {"", ""}, result);

        result = StringUtils.fastSplitTemplate(",", ",", false);
        assertArrayEquals(new String[] {""}, result);

        result = StringUtils.fastSplitTemplate(null, ",");
        assertArrayEquals(new String[] {}, result);

        result = StringUtils.fastSplitTemplate("a", ",");
        assertArrayEquals(new String[] {"a"}, result);

        result = StringUtils.fastSplitTemplate("a,b", ",");
        assertArrayEquals(new String[] {"a", "b"}, result);

        result = StringUtils.fastSplitTemplate("a,b,c", ",");
        assertArrayEquals(new String[] {"a", "b", "c"}, result);

        result = StringUtils.fastSplitTemplate("a,b,c", ",");
        assertArrayEquals(new String[] {"a", "b", "c"}, result);

        result = StringUtils.fastSplitTemplate("a,b,", ",");
        assertArrayEquals(new String[] {"a", "b", ""}, result);

        result = StringUtils.fastSplitTemplate("a,b,", ",", true);
        assertArrayEquals(new String[] {"a", "b", ""}, result);

        result = StringUtils.fastSplitTemplate("a,b,", ",", false);
        assertArrayEquals(new String[] {"a", "b"}, result);

        result = StringUtils.fastSplitTemplate(",a,b", ",");
        assertArrayEquals(new String[] {"", "a", "b"}, result);

        result = StringUtils.fastSplitTemplate(",a,b,", ",");
        assertArrayEquals(new String[] {"", "a", "b", ""}, result);

        result = StringUtils.fastSplitTemplate("a,{b,c}", ",");
        assertArrayEquals(new String[] {"a", "{b,c}"}, result);

        result = StringUtils.fastSplitTemplate("a,{b,c},d", ",");
        assertArrayEquals(new String[] {"a", "{b,c}", "d"}, result);

        result = StringUtils.fastSplitTemplate("a,{b,c,d},e", ",");
        assertArrayEquals(new String[] {"a", "{b,c,d}", "e"}, result);

        result = StringUtils.fastSplitTemplate("a,{b},c", ",");
        assertArrayEquals(new String[] {"a", "{b}", "c"}, result);

        result = StringUtils.fastSplitTemplate("a,{},b,c", ",");
        assertArrayEquals(new String[] {"a", "{}", "b", "c"}, result);

        result = StringUtils.fastSplitTemplate("a,{b,c},", ",");
        assertArrayEquals(new String[] {"a", "{b,c}", ""}, result);

        result = StringUtils.fastSplitTemplate("a,{b,c", ",");
        assertArrayEquals(new String[] {"a", "{b,c"}, result);

        result = StringUtils.fastSplitTemplate("a,{{b,c}", ",");
        assertArrayEquals(new String[] {"a", "{{b,c}"}, result);

        result = StringUtils.fastSplitTemplate("a,{{b,c},d}", ",");
        assertArrayEquals(new String[] {"a", "{{b,c},d}"}, result);

        result = StringUtils.fastSplitTemplate("a,{{b,c},d},e", ",");
        assertArrayEquals(new String[] {"a", "{{b,c},d}", "e"}, result);

        result = StringUtils.fastSplitTemplate("a,{b,c},{d", ",");
        assertArrayEquals(new String[] {"a", "{b,c}", "{d"}, result);

        result = StringUtils.fastSplitTemplate("a,{b,c},}d}", ",");
        assertArrayEquals(new String[] {"a", "{b,c}", "}d}"}, result);

        result = StringUtils.fastSplitTemplate("a,{b,c},{}d{}", ",");
        assertArrayEquals(new String[] {"a", "{b,c}", "{}d{}"}, result);

        result = StringUtils.fastSplitTemplate("a,b,c}", ",");
        assertArrayEquals(new String[] {"a", "b", "c}"}, result);

        result = StringUtils.fastSplitTemplate("{a,b,c}", ",");
        assertArrayEquals(new String[] {"{a,b,c}"}, result);

        result = StringUtils.fastSplitTemplate("a,{b,c,,},d,{e,f,},g", ",");
        assertArrayEquals(new String[] {"a", "{b,c,,}", "d", "{e,f,}", "g"}, result);

    }
}
