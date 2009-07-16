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

package org.apache.wink.jaxrs.test.providers.contextresolver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DepartmentDatabase {

    private static Map<String, Department> departmentDB = new HashMap<String, Department>();

    public static Collection<Department> getDepartments() {
        return departmentDB.values();
    }

    public static void addDepartment(Department department) {
        departmentDB.put(department.getDepartmentId(), department);
    }

    public static Department getDepartment(String departmentId) {
        return departmentDB.get(departmentId);
    }

    public static Department removeDepartment(String departmentId) {
        return departmentDB.remove(departmentId);
    }

    public static void clearEntries() {
        departmentDB.clear();
    }

}
