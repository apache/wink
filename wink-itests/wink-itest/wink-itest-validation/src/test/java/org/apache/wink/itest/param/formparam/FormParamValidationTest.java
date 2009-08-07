/*
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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.wink.itest.param.formparam;

import javax.ws.rs.FormParam;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class FormParamValidationTest extends TestCase {

    private static String getBaseURI() {
        return ServerEnvironmentInfo.getBaseURI() + "/param/formparam/";
    }

    /**
     * {@link FormParam} annotated parameters with entity parameters are not
     * supported.
     * 
     * @throws Exception
     */
    public void testFormPropertyNoMultivaluedMapEntityValidation() throws Exception {
        HttpClient httpclient = new HttpClient();

        PostMethod httpMethod =
            new PostMethod(getBaseURI() + "/params/form/validate/paramnotmultivaluedmaparam");
        try {
            StringRequestEntity s =
                new StringRequestEntity("firstkey=somevalue&someothervalue=somethingelse",
                                        "application/x-www-form-urlencoded", null);
            httpMethod.setRequestEntity(s);
            httpclient.executeMethod(httpMethod);
            assertEquals(200, httpMethod.getStatusCode());
            assertEquals("somevalue:", httpMethod.getResponseBodyAsString());
        } finally {
            httpMethod.releaseConnection();
        }
    }
}
