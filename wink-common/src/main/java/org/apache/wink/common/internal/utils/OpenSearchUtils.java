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

import java.math.BigInteger;

import javax.ws.rs.core.MediaType;

import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.common.model.opensearch.OpenSearchImage;

public class OpenSearchUtils {

    private static final BigInteger ICON_SIZE = BigInteger.valueOf(16);
    private static final BigInteger IMG_SIZE  = BigInteger.valueOf(64);

    /**
     * Constructor
     * 
     * @param mediaTypeString MimeType
     * @param url Image URL
     */
    public static OpenSearchImage createOpenSearchImage(String mediaTypeString, String url) {
        if (mediaTypeString == null) {
            throw new NullPointerException("mediaType parameter is null");
        }
        OpenSearchImage image = new OpenSearchImage();
        MediaType mediaType = MediaType.valueOf(mediaTypeString);
        if (MediaTypeUtils.equalsIgnoreParameters(MediaTypeUtils.IMAGE_X_ICON, mediaType) || MediaTypeUtils
            .equalsIgnoreParameters(MediaTypeUtils.IMAGE_VND, mediaType)) {
            image.setHeight(ICON_SIZE);
            image.setWidth(ICON_SIZE);
            image.setType(mediaTypeString);
            image.setValue(url);
        } else if (MediaTypeUtils.equalsIgnoreParameters(MediaTypeUtils.IMAGE_PNG, mediaType) || MediaTypeUtils
            .equalsIgnoreParameters(MediaTypeUtils.IMAGE_JPEG_TYPE, mediaType)) {
            image.setHeight(IMG_SIZE);
            image.setWidth(IMG_SIZE);
            image.setType(mediaTypeString);
            image.setValue(url);
        } else {
            image.setHeight(IMG_SIZE);
            image.setWidth(IMG_SIZE);
            image.setType(MediaTypeUtils.IMAGE_PNG.toString());
            image.setValue(url);
        }
        return image;
    }

}
