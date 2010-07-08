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

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import junit.framework.TestCase;

import org.apache.wink.common.internal.http.ContentDispositionHeader;

/**
 * Unit test of ContentDispositionHelper
 */
public class ContentDispositionHeaderDelegateTest extends TestCase {

    public void testDispositionAttachmentFilename() {
        ContentDispositionHeader contentDispositionHeader = new ContentDispositionHeader();
        contentDispositionHeader.setAttachment(true);
        contentDispositionHeader.setFileName("fileName.xml");
        HeaderDelegate<ContentDispositionHeader> headerDelegate =
            RuntimeDelegate.getInstance().createHeaderDelegate(ContentDispositionHeader.class);
        String header = headerDelegate.toString(contentDispositionHeader);

        assertEquals("header value", "attachment; filename=\"fileName.xml\"", header);
    }

    public void testDispositionInlineFilename() {
        ContentDispositionHeader contentDispositionHeader = new ContentDispositionHeader();
        contentDispositionHeader.setAttachment(false);
        contentDispositionHeader.setFileName("fileName.xml");
        HeaderDelegate<ContentDispositionHeader> headerDelegate =
            RuntimeDelegate.getInstance().createHeaderDelegate(ContentDispositionHeader.class);
        String header = headerDelegate.toString(contentDispositionHeader);

        assertEquals("header value", "inline; filename=\"fileName.xml\"", header);
    }

    public void testDispositionInlineNoFilename() {
        ContentDispositionHeader contentDispositionHeader = new ContentDispositionHeader();
        contentDispositionHeader.setAttachment(false);
        HeaderDelegate<ContentDispositionHeader> headerDelegate =
            RuntimeDelegate.getInstance().createHeaderDelegate(ContentDispositionHeader.class);
        String header = headerDelegate.toString(contentDispositionHeader);
        assertEquals("header value", "inline", header);
    }

    // public void testNegativeContentTypeNotKnown() {
    // Response response =
    // RuntimeDelegate.getInstance().createResponseBuilder().build();
    // final String unknownContentType = "unknown/unknown";
    // try {
    // ContentDispositionHelper.setContentDisposition(response,
    // unknownContentType,
    // "contentName");
    // fail("IllegalArgumentException expected");
    // } catch (IllegalArgumentException iae) {
    // assertTrue("caused by unknown media type",
    // iae.getMessage().contains(unknownContentType));
    // }
    // }

}
