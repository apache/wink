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

package org.apache.wink.itest.contextresolver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class DepartmentTest extends TestCase {

    public static String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI() + "/departments";
        }
        return ServerEnvironmentInfo.getBaseURI() + "/simplecontextresolver/departments";
    }

    /**
     * This will drive several different requests that interact with the
     * Departments resource class.
     */
    public void testDepartmentsResourceJAXB() throws Exception {
        PostMethod postMethod = null;
        GetMethod getAllMethod = null;
        GetMethod getOneMethod = null;
        HeadMethod headMethod = null;
        DeleteMethod deleteMethod = null;
        try {

            // make sure everything is clear before testing
            DepartmentDatabase.clearEntries();

            // create a new Department
            Department newDepartment = new Department();
            newDepartment.setDepartmentId("1");
            newDepartment.setDepartmentName("Marketing");
            JAXBContext context =
                JAXBContext.newInstance(new Class<?>[] {Department.class,
                    DepartmentListWrapper.class});
            Marshaller marshaller = context.createMarshaller();
            StringWriter sw = new StringWriter();
            marshaller.marshal(newDepartment, sw);
            HttpClient client = new HttpClient();
            postMethod = new PostMethod(getBaseURI());
            RequestEntity reqEntity =
                new ByteArrayRequestEntity(sw.toString().getBytes(), "text/xml");
            postMethod.setRequestEntity(reqEntity);
            client.executeMethod(postMethod);

            newDepartment = new Department();
            newDepartment.setDepartmentId("2");
            newDepartment.setDepartmentName("Sales");
            sw = new StringWriter();
            marshaller.marshal(newDepartment, sw);
            client = new HttpClient();
            postMethod = new PostMethod(getBaseURI());
            reqEntity = new ByteArrayRequestEntity(sw.toString().getBytes(), "text/xml");
            postMethod.setRequestEntity(reqEntity);
            client.executeMethod(postMethod);

            // now let's get the list of Departments that we just created
            // (should be 2)
            client = new HttpClient();
            getAllMethod = new GetMethod(getBaseURI());
            client.executeMethod(getAllMethod);
            byte[] bytes = getAllMethod.getResponseBody();
            assertNotNull(bytes);
            InputStream bais = new ByteArrayInputStream(bytes);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Object obj = unmarshaller.unmarshal(bais);
            assertTrue(obj instanceof DepartmentListWrapper);
            DepartmentListWrapper wrapper = (DepartmentListWrapper)obj;
            List<Department> dptList = wrapper.getDepartmentList();
            assertNotNull(dptList);
            assertEquals(2, dptList.size());

            // now get a specific Department that was created
            client = new HttpClient();
            getOneMethod = new GetMethod(getBaseURI() + "/1");
            client.executeMethod(getOneMethod);
            bytes = getOneMethod.getResponseBody();
            assertNotNull(bytes);
            bais = new ByteArrayInputStream(bytes);
            obj = unmarshaller.unmarshal(bais);
            assertTrue(obj instanceof Department);
            Department dept = (Department)obj;
            assertEquals("1", dept.getDepartmentId());
            assertEquals("Marketing", dept.getDepartmentName());

            // let's send a Head request for both an existent and non-existent
            // resource
            // we are testing to see if header values being set in the resource
            // implementation
            // are sent back appropriately
            client = new HttpClient();
            headMethod = new HeadMethod(getBaseURI() + "/3");
            client.executeMethod(headMethod);
            assertNotNull(headMethod.getResponseHeaders());
            Header header = headMethod.getResponseHeader("unresolved-id");
            assertNotNull(header);
            assertEquals("3", header.getValue());
            headMethod.releaseConnection();

            // now the resource that should exist
            headMethod = new HeadMethod(getBaseURI() + "/1");
            client.executeMethod(headMethod);
            assertNotNull(headMethod.getResponseHeaders());
            header = headMethod.getResponseHeader("resolved-id");
            assertNotNull(header);
            assertEquals("1", header.getValue());

            deleteMethod = new DeleteMethod(getBaseURI() + "/1");
            client.executeMethod(deleteMethod);
            assertEquals(204, deleteMethod.getStatusCode());

            deleteMethod = new DeleteMethod(getBaseURI() + "/2");
            client.executeMethod(deleteMethod);
            assertEquals(204, deleteMethod.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
            if (getAllMethod != null) {
                getAllMethod.releaseConnection();
            }
            if (getOneMethod != null) {
                getOneMethod.releaseConnection();
            }
            if (headMethod != null) {
                headMethod.releaseConnection();
            }
            if (deleteMethod != null) {
                deleteMethod.releaseConnection();
            }
        }
    }
}
