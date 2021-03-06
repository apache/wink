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

package org.apache.wink.itest.standard;

import java.util.HashSet;
import java.util.Set;

public class Application extends javax.ws.rs.core.Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> clazzes = new HashSet<Class<?>>();
        clazzes.add(FileResource.class);
        return clazzes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objs = new HashSet<Object>();
        objs.add(new BytesArrayResource());
        objs.add(new InputStreamResource());
        objs.add(new ReaderResource());
        objs.add(new StreamingOutputResource());
        objs.add(new MultiValuedMapResource());
        objs.add(new SourceResource());
        objs.add(new DataSourceResource());
        objs.add(new DSResource());
        objs.add(new StringResource());
        objs.add(new JAXBResource());
        return objs;
    }

}
