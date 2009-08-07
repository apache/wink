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

package org.apache.wink.itest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("defaultvalue")
public class DefaultValueResource {

    private String version;

    @DefaultValue("100")
    @QueryParam("limit")
    private String limit;

    private String sort;

    public DefaultValueResource(@HeaderParam("requestVersion") @DefaultValue("1.0") String version) {
        this.version = version;
    }

    public static class Page {

        private String offset;

        public Page(String offset, int dummy) {
            this.offset = offset;
            System.out.println("Executed constructor");
        }

        public String getOffset() {
            return offset;
        }

        public int getPage() {
            return Integer.valueOf(offset) * 1; // Integer.valueOf(limit);
        }

        public static Page valueOf(String offset) {
            return new Page(offset, 123);
        }
    }

    @GET
    public String getRow(@QueryParam("offset") @DefaultValue("0") Page page) {
        return "getRow:" + "offset="
            + page.getOffset()
            + ";version="
            + version
            + ";limit="
            + limit
            + ";sort="
            + sort;
    }

    @DefaultValue("normal")
    @QueryParam("sort")
    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getSort() {
        return sort;
    }
}
