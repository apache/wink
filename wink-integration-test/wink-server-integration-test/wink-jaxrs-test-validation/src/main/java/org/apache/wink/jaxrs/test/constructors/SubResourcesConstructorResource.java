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

package org.apache.wink.jaxrs.test.constructors;

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("constructors/subresource")
public class SubResourcesConstructorResource {

    @Path("emptypackage")
    public SubResourcePackageConstructor packageEmptyConstructor() {
        return new SubResourcePackageConstructor();
    }

    @Path("stringpackage")
    public SubResourcePackageConstructor packageStringConstructor() {
        return new SubResourcePackageConstructor("packageString");
    }

    @Path("emptypublic")
    public SubResourcePublicConstructor publicEmptyConstructor() {
        return new SubResourcePublicConstructor();
    }

    @Path("stringpublic")
    public SubResourcePublicConstructor publicStringConstructor(@QueryParam("q") String s) {
        return new SubResourcePublicConstructor(s);
    }

    @Path("emptyprivate")
    public Object privateEmptyConstructor() {
        return SubResourcePrivateConstructor.getPrivateInstance(null);
    }

    @Path("stringprivate")
    public Object privateStringConstructor(@QueryParam("q") String s) {
        return SubResourcePrivateConstructor.getPrivateInstance(s);
    }

    @Path("sub")
    public Object subconstructor(@QueryParam("which") String which) {
        if ("package".equals(which)) {
            return new SubResourcePackageConstructor();
        } else if ("public".equals(which)) {
            return new SubResourcePublicConstructor();
        }
        return null;
    }

}
