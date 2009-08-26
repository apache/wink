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

package org.apache.wink.common.model.multipart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An extension to the {@link InPart} class that enables calling of the
 * {@link BufferedInPart#getBody(Class, java.lang.reflect.Type)} multiple times and in a non sequential order.
 * 
 * @author elib
 */
public class BufferedInPart extends InPart {
    byte[] content;

    public BufferedInPart(InPart ip) throws IOException {
        super(ip.getHeaders(), ip.getProviders());
        InputStream src = ip.getInputStream();
        ByteArrayOutputStream dst = new ByteArrayOutputStream(1024);
        byte[] bytes = new byte[1024];
        int read = 0;
        while ((read = src.read(bytes)) != -1) {
            dst.write(bytes, 0, read);
        }
        content = dst.toByteArray();
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

}
