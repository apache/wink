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

package org.apache.wink.common.internal.application;

/**
 * 
 * This class is strictly a holder for debug message(s) that originate
 * from JAX-RS applications, NOT Wink engine, so that we log it once and only
 * once under DEBUG.
 * 
 * This class should be used as a key to RuntimeContext.setAttribute call.
 * Such a call should be made in cases where exceptions are caught at the boundary
 * between Wink engine and application code.
 * 
 * RequestProcessor should send the debugMsg to the logger, only once.
 * 
 * See DebugResourceThrowsExceptionTest.
 *
 */
public class ApplicationExceptionAttribute {

    private String debugMsg;
    
    public ApplicationExceptionAttribute(String debugMsg) {
        this.debugMsg = debugMsg;
    }
    
    public String getDebugMsg() {
        return debugMsg;
    }
    
}
