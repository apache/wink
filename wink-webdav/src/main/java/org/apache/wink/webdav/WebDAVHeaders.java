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

package org.apache.wink.webdav;

public final class WebDAVHeaders {

    public static final String DAV           = "DAV"; //$NON-NLS-1$
    public static final String DEPTH         = "Depth"; //$NON-NLS-1$
    public static final String OVERWRITE     = "Overwrite"; //$NON-NLS-1$
    public static final String DESTINATION   = "Destination"; //$NON-NLS-1$
    public static final String IF            = "If"; //$NON-NLS-1$
    public static final String LOCK_TOKEN    = "Lock-Token"; //$NON-NLS-1$
    public static final String TIMEOUT       = "Timeout"; //$NON-NLS-1$

    /**
     * Microsoft <a
     * href="http://msdn.microsoft.com/en-us/library/cc250217.aspx">
     * authorization header</a>
     */
    public static final String MS_AUTHOR_VIA = "MS-Author-Via"; //$NON-NLS-1$
}
