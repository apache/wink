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

package org.apache.wink.jaxrs.test.inheritance;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class InheritanceTest extends TestCase {

    protected HttpClient        httpClient         = new HttpClient();

    final private static String PARKING_LOT_URI    =
                                                       ServerEnvironmentInfo.getBaseURI() + "/inheritance/parkinglot";

    final private static String PARKING_GARAGE_URI =
                                                       ServerEnvironmentInfo.getBaseURI() + "/inheritance/parkinggarage";

    final private static String CARPORT_URI        =
                                                       ServerEnvironmentInfo.getBaseURI() + "/inheritance/carport";

    final private static String CARFERRY_URI       =
                                                       ServerEnvironmentInfo.getBaseURI() + "/inheritance/carferry";

    final private static String CLASS_C_URI        =
                                                       ServerEnvironmentInfo.getBaseURI() + "/inheritance/classc";

    final private static String FRUIT_URI          =
                                                       ServerEnvironmentInfo.getBaseURI() + "/inheritance/fruit";

    public void testOverrideInterfaceAnnotations() throws Exception {
        PostMethod postMethod = new PostMethod(PARKING_LOT_URI + "/cars");
        GetMethod getMethod = new GetMethod(PARKING_LOT_URI + "/cars");
        try {
            String licenseNum = "103DIY";
            postMethod.setRequestEntity(new ByteArrayRequestEntity(licenseNum.getBytes(),
                                                                   "text/xml"));
            httpClient.executeMethod(postMethod);
            Header header = postMethod.getResponseHeader("Invoked");
            assertNotNull(header);
            assertEquals("ParkingLot.addCar", header.getValue());
            httpClient.executeMethod(getMethod);
            String resp = getMethod.getResponseBodyAsString();
            assertTrue(resp.contains(licenseNum));
            header = getMethod.getResponseHeader("Invoked");
            assertNotNull(header);
            assertEquals("ParkingLot.getCars", header.getValue());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        } finally {
            postMethod.releaseConnection();
            getMethod.releaseConnection();
        }
    }

    public void testOverrideSuperClassAnnotations() throws Exception {
        PostMethod postMethod = new PostMethod(PARKING_GARAGE_URI + "/cars");
        GetMethod getMethod = new GetMethod(PARKING_GARAGE_URI + "/cars/1");
        try {
            String licenseNum = "103DIY";
            postMethod.setRequestEntity(new ByteArrayRequestEntity(licenseNum.getBytes(),
                                                                   "text/xml"));
            httpClient.executeMethod(postMethod);
            Header header = postMethod.getResponseHeader("Invoked");
            assertNotNull(header);
            assertEquals("ParkingGarage.addCar", header.getValue());
            httpClient.executeMethod(getMethod);
            String resp = getMethod.getResponseBodyAsString();
            assertTrue(resp.contains(licenseNum));
            header = getMethod.getResponseHeader("Invoked");
            assertNotNull(header);
            assertEquals("ParkingGarage.getCars", header.getValue());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        } finally {
            postMethod.releaseConnection();
            getMethod.releaseConnection();
        }
    }

    public void testInheritAnnotationsFromInterface() throws Exception {
        PostMethod postMethod = new PostMethod(CARPORT_URI + "/carstorage");
        GetMethod getMethod = new GetMethod(CARPORT_URI + "/carstorage");
        try {
            String licenseNum = "103DIY";
            postMethod.setRequestEntity(new ByteArrayRequestEntity(licenseNum.getBytes(),
                                                                   "text/xml"));
            httpClient.executeMethod(postMethod);
            Header header = postMethod.getResponseHeader("Invoked");
            assertNotNull(header);
            assertEquals("Carport.addCar", header.getValue());
            httpClient.executeMethod(getMethod);
            String resp = getMethod.getResponseBodyAsString();
            assertTrue(resp.contains(licenseNum));
            header = getMethod.getResponseHeader("Invoked");
            assertNotNull(header);
            assertEquals("Carport.getCars", header.getValue());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        } finally {
            postMethod.releaseConnection();
            getMethod.releaseConnection();
        }
    }

    public void testInheritAnnotationsFromSuperClass() throws Exception {
        PostMethod postMethod = new PostMethod(CARFERRY_URI + "/cars");
        GetMethod getMethod = new GetMethod(CARFERRY_URI + "/cars/1");
        try {
            String licenseNum = "103DIY";
            postMethod.setRequestEntity(new ByteArrayRequestEntity(licenseNum.getBytes(),
                                                                   "text/xml"));
            httpClient.executeMethod(postMethod);
            Header header = postMethod.getResponseHeader("Invoked");
            assertNotNull(header);
            assertEquals("CarFerry.addCar", header.getValue());
            httpClient.executeMethod(getMethod);
            String resp = getMethod.getResponseBodyAsString();
            assertTrue(resp.contains(licenseNum));
            header = getMethod.getResponseHeader("Invoked");
            assertNotNull(header);
            assertEquals("CarFerry.getCars", header.getValue());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        } finally {
            postMethod.releaseConnection();
            getMethod.releaseConnection();
        }
    }

    public void testSuperClassOverInterface() throws Exception {
        GetMethod getMethod = new GetMethod(CLASS_C_URI + "/abstract_method1/encoded%20string");
        try {
            httpClient.executeMethod(getMethod);
            String resp = getMethod.getResponseBodyAsString();
            assertEquals("ClassC Method1;encoded%20string", resp);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        } finally {
            getMethod.releaseConnection();
        }
    }

    public void testOverridePostWithGet() throws Exception {
        GetMethod getMethod = new GetMethod(CLASS_C_URI + "/method2/encoded%20string");
        PostMethod postMethod = new PostMethod(CLASS_C_URI);
        try {
            httpClient.executeMethod(getMethod);
            String resp = getMethod.getResponseBodyAsString();
            assertEquals("ClassC Method2;encoded string", resp);
            httpClient.executeMethod(postMethod);
            assertEquals(405, postMethod.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        } finally {
            getMethod.releaseConnection();
            postMethod.releaseConnection();
        }
    }

    public void testSubResourceLocaterInheritance() throws Exception {
        DeleteMethod deleteMethod = new DeleteMethod(PARKING_LOT_URI + "/cars/remove/ParkingLot");
        GetMethod getMethod = new GetMethod(FRUIT_URI + "/fruit%20suffix");
        PostMethod postMethod = new PostMethod(FRUIT_URI + "/orange%20suffix");
        try {
            // ParkingLot classes. Sub resource classes are potential root
            // resources
            httpClient.executeMethod(deleteMethod);
            Header header = deleteMethod.getResponseHeader("Invoked");
            assertNotNull(header);
            assertEquals("ParkingLot.removeCar", header.getValue());
            deleteMethod.releaseConnection();
            deleteMethod = new DeleteMethod(PARKING_LOT_URI + "/cars/remove/ParkingGarage");
            httpClient.executeMethod(deleteMethod);
            header = deleteMethod.getResponseHeader("Invoked");
            assertNotNull(header);
            assertEquals("ParkingGarage.removeCar", header.getValue());
            deleteMethod.releaseConnection();
            deleteMethod = new DeleteMethod(PARKING_LOT_URI + "/cars/remove/CarFerry");
            httpClient.executeMethod(deleteMethod);
            assertEquals(405, deleteMethod.getStatusCode());

            // Fruit classes. Sub resource classes are not potential root
            // resources
            httpClient.executeMethod(getMethod);
            String response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.jaxrs.test.inheritance.fruits.Fruit;fruit%20suffix",
                         response);
            getMethod.releaseConnection();
            getMethod = new GetMethod(FRUIT_URI + "/apple%20suffix");
            httpClient.executeMethod(getMethod);
            response = getMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.jaxrs.test.inheritance.fruits.Apple;apple suffix", // parameters
                                                                                             // on
                                                                                             // class
                                                                                             // are
                                                                                             // not
                                                                                             // inherited
                         response);
            getMethod.releaseConnection();
            getMethod = new GetMethod(FRUIT_URI + "/orange%20suffix");
            httpClient.executeMethod(getMethod);
            assertEquals(405, getMethod.getStatusCode());
            httpClient.executeMethod(postMethod);
            response = postMethod.getResponseBodyAsString();
            assertEquals("org.apache.wink.jaxrs.test.inheritance.fruits.Orange;orange suffix",
                         response);
            assertEquals(200, postMethod.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        } finally {
            deleteMethod.releaseConnection();
            getMethod.releaseConnection();
        }
    }
}
