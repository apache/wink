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

package org.apache.wink.common.model.atom;

/**
 * Set of constants useful for ATOM related code.
 */
public final class AtomConstants {

    private AtomConstants() {
    } // no instances

    public static final String ATOM_REL_SELF       = "self"; //$NON-NLS-1$
    public static final String ATOM_REL_ALT        = "alternate"; //$NON-NLS-1$
    public static final String ATOM_REL_RELATED    = "related"; //$NON-NLS-1$
    public static final String ATOM_REL_ENCLOSURE  = "enclosure"; //$NON-NLS-1$
    public static final String ATOM_REL_VIA        = "via"; //$NON-NLS-1$
    public static final String ATOM_REL_EDIT       = "edit"; //$NON-NLS-1$
    public static final String ATOM_REL_EDIT_MEDIA = "edit-media"; //$NON-NLS-1$
    public static final String ATOM_REL_SEARCH     = "search"; //$NON-NLS-1$
    public static final String ATOM_REL_HISTORY    = "history"; //$NON-NLS-1$

    // type
    public static final String ATOM_XHTML          = "xhtml"; //$NON-NLS-1$
    public static final String ATOM_HTML           = "html"; //$NON-NLS-1$
    public static final String ATOM_TXT            = "text"; //$NON-NLS-1$

    // paging
    public static final String ATOM_REL_FIRST      = "first"; //$NON-NLS-1$
    public static final String ATOM_REL_LAST       = "last"; //$NON-NLS-1$
    public static final String ATOM_REL_PREVIOUS   = "previous"; //$NON-NLS-1$
    public static final String ATOM_REL_NEXT       = "next"; //$NON-NLS-1$
}
