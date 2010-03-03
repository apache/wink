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
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.ws.rs.ext.Providers;

import org.apache.wink.common.RestException;
import org.apache.wink.common.internal.providers.multipart.MultiPartParser;

/**
 * This class is used to represent inbound MultiPart messages, it implements
 * an Iterator to iterate over the message's parts. A typical usage of
 * InMultiPart looks like this:
 * 
 * @POST
 * @Consumes( MediaTypeUtils.MULTIPART_FORM_DATA) <br>
 *            public String postMultipart(InMultiPart inMP) throws IOException <br>
 *            { <br>
 *            while(inMP.hasNext()) { <br>
 *            InPart part = inMP.next();<br>
 *            MyClass myOject =part.getBody(MyClass.class, null);<br>
 *            // Do somthing<br>
 * <br>
 *            .<br>
 *            .<br>
 * }<br>
 */
public class InMultiPart implements Iterator<InPart> {
    public final static String SEP            = "\n"; //$NON-NLS-1$
    //private String             boundary       = "simple boundary";
    private MultiPartParser    MPParser;
    int                        index          = -1;
    boolean                    moved          = false;
    boolean                    lastMoveResult = true;
    private Providers          providers;

    public Providers getProviders() {
        return providers;
    }

    public void setProviders(Providers providers) {
        this.providers = providers;
    }

    public InMultiPart(MultiPartParser mim) {
        MPParser = mim;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.) <br>
     * NOTICE: calling the has next will cause the inputStream of the previous
     * part to be invalid
     * 
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        if (!moved) {
            try {
                lastMoveResult = MPParser.nextPart();
            } catch (IOException e) {
                throw new RestException(e);
            }
            moved = true;
            index++;
        }
        return lastMoveResult;

    }

    public InPart next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        moved = false;
        InPart ip = new InPart();
        ip.setHeaders(MPParser.getPartHeaders());
        ip.setInputStream(MPParser.getPartBodyStream());
        ip.setProviders(providers);
        return ip;

    }

    /**
     * Not implemented
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
