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
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to represent inbound MultiPart messages, unlike the {@link InMultiPart} 
 * the {@link BufferedInMultiPart} enable non sequential access to the message's parts   
 *  <code>
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
 *            }<br>
 *            
 */
public class BufferedInMultiPart {
    private List<InPart> parts = new ArrayList<InPart>();

    public BufferedInMultiPart(InMultiPart imp) throws IOException {
        while (imp.hasNext()) {
            BufferedInPart cip = new BufferedInPart(imp.next());
            parts.add(cip);
        }
    }

    /**
     * return the number of parts in the multipart message
     */
    public int getSize() {
        return parts.size();
    }

    public List<InPart> getParts() {
        return parts;
    }

}
