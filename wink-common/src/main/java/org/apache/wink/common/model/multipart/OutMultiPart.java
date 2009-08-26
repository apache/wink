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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.ws.rs.ext.Providers;

/**
 * An abstract class to generate a MultiPart response, the concept behind this
 * class been abstract is that there might be simple implementation over a
 * collection or more complex once like over a database cursor
 * 
 * @author elib
 */
public abstract class OutMultiPart {

    private String             boundary = "simple boundary";
    public final static String SEP      = "\n";

    /**
     * set the boundary to be used to separate between the different parts
     * 
     * @param boundary
     */
    public void setBoundary(String boundary) {
        this.boundary = boundary;
    }

    public String getBoundary() {
        return boundary;
    }

    /**
     * An implementation of this method should return an iterator over the
     * {@link OutPart} of the message, this iterator is used to serialized the
     * message
     * 
     * @return
     */
    protected abstract Iterator<? extends OutPart> getIterator();

    /**
     * This method write the multiPart message to the os stream, it make a usage
     * of the providers for the serialization of the parts
     * 
     * @param os
     * @param providers
     * @throws IOException
     */
    public void write(OutputStream os, Providers providers) throws IOException {
        Iterator<? extends OutPart> it = getIterator();
        while (it.hasNext()) {
            OutPart p = it.next();
            os.write((SEP + "--" + boundary + SEP).getBytes());
            p.writePart(os, providers);
        }
        os.write((SEP + "--" + boundary + "--" + SEP).getBytes());
    }

}
