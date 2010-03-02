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

package org.apache.wink.providers.jackson.internal.pojo.polymorphic;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class Animal {

    private String type;

    @JsonIgnore
    private String ignored;
    
    // type must have some data so Jackson client can properly unmarshal to correct object
    protected Animal(String _type) {
        this.setType(_type);
    }
    
    /**
     * Creator method that can instantiate instances of
     * appropriate polymorphic type
     * 
     * It's pretty ugly to require a supertype to have knowledge
     * of its subtypes, but that's the story of polymorphism in Jackson
     * as of 1.4.0 release.  See:
     * http://archive.codehaus.org/lists/org.codehaus.jackson.user/msg/5f7770581001061754sa3b9a6an67b111c39da8151c@mail.gmail.com
     */
    @JsonCreator
    public static Animal create(@JsonProperty("type") String type)
    {
        if ("dog".equals(type)) {
            return new Dog();
        }
        if ("cat".equals(type)) {
            return new Cat();
        }
        throw new IllegalArgumentException("No such animal type ('"+type+"')");
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @JsonIgnore
    public String getIgnored() {
        return ignored;
    }

    @JsonIgnore
    public void setIgnored(String ignored) {
        this.ignored = ignored;
    }
    
    
    
}
