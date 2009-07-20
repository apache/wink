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
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.utils.MediaTypeUtils;

public class ContentDispositionHeader implements Cloneable {

    private String                                                fileName;
    private String                                                defaultExtension;
    private boolean                                               attachment;

    private static final Map<MediaType, ContentDispositionHeader> mediaType2ContentDisposition =
                                                                                                   contructMediaType2ContentDisposition();

    private static Map<MediaType, ContentDispositionHeader> contructMediaType2ContentDisposition() {
        Map<MediaType, ContentDispositionHeader> result =
            new HashMap<MediaType, ContentDispositionHeader>();
        putContentDispositionRecord(result, MediaTypeUtils.CSV, true, "csv");
        putContentDispositionRecord(result, MediaTypeUtils.PDF_TYPE, false, "pdf");
        return Collections.unmodifiableMap(result);
    }

    private static void putContentDispositionRecord(Map<MediaType, ContentDispositionHeader> map,
                                                    MediaType mediaType,
                                                    boolean attachment,
                                                    String extension) {
        map.put(mediaType, new ContentDispositionHeader(attachment, extension));
    }

    public static ContentDispositionHeader createContentDispositionHeader(MediaType mediaType) {
        try {
            ContentDispositionHeader contentDispositionHeader =
                mediaType2ContentDisposition.get(mediaType);
            if (contentDispositionHeader != null) {
                return contentDispositionHeader.clone();
            }
            return new ContentDispositionHeader();
        } catch (CloneNotSupportedException e) {
            // should never happen
            throw new WebApplicationException(e);
        }
    }

    public ContentDispositionHeader() {
    }

    public ContentDispositionHeader(boolean attachment, String defaultExtension) {
        this.defaultExtension = defaultExtension;
        this.attachment = attachment;
    }

    public void setDefaultExtension(String defaultExtension) {
        this.defaultExtension = defaultExtension;
    }

    public String getDefaultExtension() {
        return defaultExtension;
    }

    public void setAttachment(boolean attachment) {
        this.attachment = attachment;
    }

    public boolean isAttachment() {
        return attachment;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    protected ContentDispositionHeader clone() throws CloneNotSupportedException {
        return (ContentDispositionHeader)super.clone();
    }
}
