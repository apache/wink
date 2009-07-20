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

package org.apache.wink.server.internal.registry;

import org.apache.wink.common.internal.uritemplate.UriTemplateProcessor;

/**
 * Abstract class that is a common base class for all records that maintain a
 * uri template processor
 */
public abstract class TemplatedRecord implements Comparable<TemplatedRecord>, Cloneable {

    private UriTemplateProcessor templateProcessor;

    public TemplatedRecord(UriTemplateProcessor templateProcessor) {
        this.templateProcessor = templateProcessor;
    }

    public UriTemplateProcessor getTemplateProcessor() {
        return templateProcessor;
    }

    public int compareTo(TemplatedRecord o) {
        return templateProcessor.compareTo(o.templateProcessor);
    }

    @Override
    public String toString() {
        return String.valueOf(templateProcessor);
    }

}
