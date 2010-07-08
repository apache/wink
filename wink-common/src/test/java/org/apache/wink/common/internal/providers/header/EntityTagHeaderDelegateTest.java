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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.junit.Test;

public class EntityTagHeaderDelegateTest {

    @Test
    public void testParseEtag() {
        RuntimeDelegate rd = RuntimeDelegate.getInstance();
        HeaderDelegate<EntityTag> entityTagHeaderDelegate =
            rd.createHeaderDelegate(EntityTag.class);
        if (entityTagHeaderDelegate == null) {
            fail("EntityTag header delegate is not regestered in RuntimeDelegateImpl");
        }

        String expectedEntityTagString = "12321-\"12321-\t123123";
        EntityTag expectedWeekEntityTag = new EntityTag(expectedEntityTagString, true);
        EntityTag expectedStrongEntityTag = new EntityTag(expectedEntityTagString, false);

        String inputEntityTagString = "\"" + "12321-\\\"12321-\t123123" + "\"";

        // Test Weak Entity Tag
        EntityTag parsedWeakEntityTag =
            entityTagHeaderDelegate.fromString("W/" + inputEntityTagString);
        assertEquals(expectedWeekEntityTag, parsedWeakEntityTag);

        // Test Strong Entity Tag
        EntityTag parsedStrongEntityTag = entityTagHeaderDelegate.fromString(inputEntityTagString);
        assertEquals(expectedStrongEntityTag, parsedStrongEntityTag);

        // Negative test - Etag in null
        try {
            entityTagHeaderDelegate.fromString(null);
            fail("EntityTag is null - IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }

        // Negative test - Etag in not valid
        try {
            entityTagHeaderDelegate.fromString("Not quoted Entity Tag");
            fail("Invalid EntityTag - IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testSerializeEtag() {
        RuntimeDelegate rd = RuntimeDelegate.getInstance();
        HeaderDelegate<EntityTag> entityTagHeaderDelegate =
            rd.createHeaderDelegate(EntityTag.class);
        if (entityTagHeaderDelegate == null) {
            fail("EntityTag header delegate is not regestered in RuntimeDelegateImpl");
        }

        String entityTagString = "12321-\"12321-123123";
        String expectedEntityTagString = "\"" + "12321-\\\"12321-123123" + "\"";

        // Test Weak Entity Tag
        EntityTag weakETag = new EntityTag(entityTagString, true);
        String weakETagString = entityTagHeaderDelegate.toString(weakETag);

        assertEquals("W/" + expectedEntityTagString, weakETagString);

        // Test String Entity Tag
        EntityTag strongETag = new EntityTag(entityTagString, false);
        String strongETagString = entityTagHeaderDelegate.toString(strongETag);

        assertEquals(expectedEntityTagString, strongETagString);

        // Negative test - Etag in null
        try {
            EntityTag eTag = null;
            entityTagHeaderDelegate.toString(eTag);
            fail("EntityTag is null - IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }

    }

}
