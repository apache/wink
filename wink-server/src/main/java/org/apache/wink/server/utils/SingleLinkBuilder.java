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

package org.apache.wink.server.utils;

import javax.ws.rs.core.MediaType;

/**
 * A builder for generating a single link to a resource. The link is created
 * absolute or relative to the base uri according to the configuration or
 * request settings.
 */
public interface SingleLinkBuilder extends BaseLinksBuilder<SingleLinkBuilder> {

    /**
     * Set the type attribute of the link
     * 
     * @param type media type attribute of the link to generate
     * @return this links builder
     */
    public SingleLinkBuilder type(MediaType type);

    /**
     * Set the rel attribute of the link
     * 
     * @param rel the rel attribute of the link
     * @return this links builder
     */
    public SingleLinkBuilder rel(String rel);

}
