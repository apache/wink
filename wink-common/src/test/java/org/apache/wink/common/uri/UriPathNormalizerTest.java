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

import org.apache.wink.common.internal.uri.UriPathNormalizer;

import junit.framework.TestCase;

/**
 * Unit test of UriPathNormalizer.
 */
public class UriPathNormalizerTest extends TestCase {

    public void testNormalize() {
        assertEquals("./p", "p", UriPathNormalizer.normalize("./p"));
        assertEquals("p/../q", "q", UriPathNormalizer.normalize("p/../q"));
        assertEquals("p/../q/./r/s/..", "q/r/", UriPathNormalizer.normalize("p/../q/./r/s/.."));
        assertEquals("p/q", "p/q", UriPathNormalizer.normalize("p/q"));
        assertEquals("p//q/h", "p/q/h", UriPathNormalizer.normalize("p//q/h"));
        assertEquals("p//", "p/", UriPathNormalizer.normalize("p//"));
        assertEquals("q/../../p", "../p", UriPathNormalizer.normalize("q/../../p"));
        assertEquals("q/../../p/", "../p/", UriPathNormalizer.normalize("q/../../p/"));
        assertEquals("/q/./p/", "/q/p/", UriPathNormalizer.normalize("/q/./p/"));
        assertEquals("../../p", "../../p", UriPathNormalizer.normalize("../../p"));
        assertEquals(".", "", UriPathNormalizer.normalize("."));
        assertEquals("./", "", UriPathNormalizer.normalize("./"));
        assertEquals("..", "..", UriPathNormalizer.normalize(".."));
        assertEquals("../", "../", UriPathNormalizer.normalize("../"));
        assertEquals(".p", ".p", UriPathNormalizer.normalize(".p"));
        assertEquals("p.", "p.", UriPathNormalizer.normalize("p."));
        assertEquals("p.p", "p.p", UriPathNormalizer.normalize("p.p"));
    }
}
