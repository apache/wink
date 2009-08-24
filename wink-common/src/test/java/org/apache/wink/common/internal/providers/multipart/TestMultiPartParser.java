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

package org.apache.wink.common.internal.providers.multipart;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.internal.providers.multipart.MultiPartParser;

import junit.framework.TestCase;

public class TestMultiPartParser extends TestCase {
	String boundery = "test boundry";
	String NL = "\r";
	String[] messages = new String[] {
			"bla bla bla\n"
					+ "\n--"
					+ boundery
					+ NL
					+ "content-type: message/http;version=1.1;msgtype=request\n"
					+ NL + "bla bla bla\n" + NL + "bla bla bla\n" + NL
					+ "bla bla bla\n" + NL + "--" + boundery + "--" + NL,

			"--"
					+ boundery
					+ ""
					+ NL
					+ "content-type: message/http;version=1.1;msgtype=request"
					+ NL
					+ ""
					+ NL
					+ "PUT /service/business-services/phone-book HTTP/1.1"
					+ NL
					+ "content-type: application/atom+xml;type=entry"
					+ NL
					+ ""
					+ NL
					+ "<entry xmlns=\"http://www.w3.org/2005/Atom\">"
					+ NL
					+ "  ..."
					+ NL
					+ "  <title type=\"text\" xml:lang=\"en\">Phonebook Business Service</title>"
					+ NL
					+ "  ..."
					+ NL
					+ "  <link href=\"slug: implementations/my-new-implementation\" type=\"application/atom+xml\" rel=\"implementation\"/>"
					+ NL
					+ "  ..."
					+ NL
					+ "</entry>"
					+ NL
					+ ""
					+ NL
					+ "--"
					+ boundery
					+ ""
					+ NL
					+ "content-type: message/http;version=1.1;msgtype=request"
					+ NL
					+ ""
					+ NL
					+ "POST /service/implementations HTTP/1.1"
					+ NL
					+ "content-type: application/atom+xml;type=entry"
					+ NL
					+ "slug: implementations/my-new-implementation"
					+ NL
					+ ""
					+ NL
					+ "<entry xmlns=\"http://www.w3.org/2005/Atom\">"
					+ NL
					+ "  ..."
					+ NL
					+ "  <title type=\"text\" xml:lang=\"en\">Phonebook Service Implementation</title>"
					+ NL
					+ "  ..."
					+ NL
					+ "  <link href=\"business-services/phone-book\" type=\"application/atom+xml\" rel=\"business-service\"/>"
					+ NL
					+ "  <link href=\"slug: documentations/my-new-phonebook-reference-guide\" type=\"application/msword\" rel=\"documentation\"/>"
					+ NL
					+ "  ..."
					+ NL
					+ "</entry>"
					+ NL
					+ ""
					+ NL
					+ "--symphony-batch"
					+ NL
					+ "content-type: message/http;version=1.1;msgtype=request"
					+ NL
					+ ""
					+ NL
					+ "DELETE /service/documentations/obsolete-documentation HTTP/1.1"
					+ NL
					+ ""
					+ NL
					+ "--"
					+ boundery
					+ NL
					+ "content-type: message/http;version=1.1;msgtype=request"
					+ NL
					+ ""
					+ NL
					+ "POST /service/documentations HTTP/1.1"
					+ NL
					+ "content-type: application/msword"
					+ NL
					+ "slug: documentations/my-new-phonebook-reference-guide"
					+ NL
					+ ""
					+ NL
					+ "... binary MS WORD media type..."
					+ NL
					+ ""
					+ NL
					+ "--"
					+ boundery
					+ ""
					+ NL
					+ "content-type: message/http;version=1.1;msgtype=request"
					+ NL
					+ ""
					+ NL
					+ "POST /service/ws-policies HTTP/1.1"
					+ NL
					+ "content-type: application/xml"
					+ NL
					+ "slug: ws-policies/my-new-policy"
					+ NL
					+ ""
					+ NL
					+ "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"...>"
					+ NL + "  ..." + NL + "</wsp>" + NL + "" + NL + "--"
					+ boundery + "" + NL
					+ "content-type: message/http;version=1.1;msgtype=request"
					+ NL + "" + NL + "POST /service/ws-policies HTTP/1.1" + NL
					+ "content-type: application/atom+xml" + NL
					+ "slug: ws-policies/my-new-policy" + NL + "" + NL
					+ "<asf:entry>" + NL + "  ..." + NL + "</asf:entry>" + NL
					+ "" + NL + "--" + boundery + "--" + NL + "" + NL

	};

	public void testParser() throws IOException {
		try {
			for (String s : messages) {
				printMessage(s);
			}
		} catch (IOException e) {
			throw e;
		}
	}
	
	public void testMsg() throws IOException {
		try {
			for (String s : messages) {
				printMessage(s);
			}
		} catch (IOException e) {
			throw e;
		}
	}

	public void printMessage(String part) throws IOException {
		System.out.println(part);
		ByteArrayInputStream is = new ByteArrayInputStream(part.getBytes());
		MultiPartParser mpp = new MultiPartParser(is, boundery);
		System.out.println("=========== BEGINE ==================");
		while (mpp.nextPart()) {
			System.out.println("=========== PART ==================");
			printHeaders(mpp.getPartHeaders());
			InputStream partStream = mpp.getPartBodyStream();
			printIS(partStream);
		}
		System.out.println("=========== END ==================");
		int i = 0;
	}

	private void printHeaders(MultivaluedMap<String, String> headers) {
		System.out.println("     ---------- Headers start --------------");
		for (String header : headers.keySet()) {
			List<String> values = headers.get(header);
			System.out.print(header + " :");
			for (String value : values) {
				System.out.println(value);
			}
		}
		System.out.println("     ---------- Headers end --------------");
	}

	private void printIS(InputStream is) throws IOException {
		System.out.println("     ---------- Body start --------------");
		// ArrayList<Byte> array = new ArrayList<Byte>();
		byte[] array = new byte[100000];
		int b;
		int i = 0;
		while ((b = is.read()) != -1) {
			array[i] = (byte) b;
			i++;
		}
		String s = new String(array, 0, i);

		System.out.print(s);

		System.out.println("\n     ---------- Body end --------------");
	}
	public void testHugeMsg() throws IOException{
		String boundery ="This is my boundery 123";
		String prefix = "bla bla bla\n"
						+ "\n--" + boundery	+ NL
						+ "content-type: message/http;version=1.1;msgtype=request\n"
						+ NL 
						+ "bla bla bla\n" + NL 
						+ "bla bla bla\n" + NL;
		String suffix = "bla bla bla\n" + NL + 
						"--" + boundery + "--" + NL;
		// 100 MG msg
		HugeMsg msg = new HugeMsg(prefix.getBytes(),suffix.getBytes(),100000000);
		MultiPartParser mpp = new MultiPartParser(msg,boundery);
		int parts = 0;
		while (mpp.nextPart())
			parts++;
		assertEquals(parts, 1);		
	}
	
	public void testMsgBodyLength() throws IOException{
		String boundery ="This is my boundery 123";
		String prefix = "bla bla bla\n"
						+ "\n--" + boundery	+ NL
						+ "content-type: message/http;version=1.1;msgtype=request\n"
						+ NL;						
		String suffix = NL + 
						"--" + boundery + "--" + NL;
		int length= 12;
		HugeMsg msg = new HugeMsg(prefix.getBytes(),suffix.getBytes(),length);
		MultiPartParser mpp = new MultiPartParser(msg,boundery);
		int size = 0;
		mpp.nextPart();
		InputStream is = mpp.getPartBodyStream();
		while(is.read()!= -1)
			size++;
		assertEquals(size, length);
			
				
		
	}
	

	public void testMsg01() throws Exception {
		TestMsgInfo msg = new TestMsgInfo("msg01.txt", "boundary");
		checkNumOfParts(msg, 2);
		assertEquals(getHeader(msg, 1, "Content-Id"), "part1");
		
	}
	
	public void testMsg02() throws Exception {
		TestMsgInfo msg = new TestMsgInfo("msg02.txt", "----=_Part_7_10584188.1123489648993");
		checkNumOfParts(msg, 2);
		assertEquals(getHeader(msg, 1, "Content-Type").toLowerCase(), "text/xml; charset=UTF-8".toLowerCase());
	}
	
	public void testMsg03() throws Exception {
		TestMsgInfo msg = new TestMsgInfo("msg03.txt", "----=_Part_7_10584188.1123489648993");
		checkNumOfParts(msg, 2);
		assertEquals(getHeaders(msg, 1).size(),2);				
	}	
	public void testMsg04() throws Exception {
		TestMsgInfo msg = new TestMsgInfo("msg04.txt", "----=_Part_1_2_3_4_5_6");
		checkNumOfParts(msg, 2);
		//assertEquals(getHeaders(msg, 1).size(),2);				
	}
	public void testMsg05() throws Exception {
		TestMsgInfo msg = new TestMsgInfo("msg05.txt", "----=_Part_1_807283631.1066069460327");
		checkNumOfParts(msg, 2);
		//assertEquals(getHeaders(msg, 1).size(),2);				
	}
	
	public void testMsg07() throws Exception {
		TestMsgInfo msg = new TestMsgInfo("msg07.txt", "Boundary1");
		checkNumOfParts(msg, 1);
	}
	

	private void checkNumOfParts(TestMsgInfo msgInfo, int expectedNumParts)
			throws IOException {

		MultiPartParser mpp = msgInfo.createMPParser();
		int parts = 0;
		while (mpp.nextPart())
			parts++;
		assertEquals(parts, expectedNumParts);
	}
	
	private MultivaluedMap<String, String> getHeaders(TestMsgInfo msgInfo, int partNum)
	throws IOException {
		MultiPartParser mpp = msgInfo.createMPParser();
		int sec = 0;
		while (sec <partNum){
			mpp.nextPart();
			sec++;
		}
		return mpp.getPartHeaders();	
	}

	private String getHeader(TestMsgInfo msgInfo, int partNum,String headerName)
			throws IOException {
		String header = getHeaders(msgInfo,partNum).getFirst(headerName);
		return header;
	}

	/***
	 * Helper class to generate a parser out of a file 
	 * @author barame
	 */
	class TestMsgInfo {
		String resource;
		String boundery;

		public TestMsgInfo(String resource, String boudery) {
			this.resource = resource;
			this.boundery = boudery;
		}

		public MultiPartParser createMPParser() {
			InputStream in = getClass().getResourceAsStream(resource);
			return new MultiPartParser(in, boundery);
		}
	}
	
	class HugeMsg extends InputStream{
		private byte[] prefix;
		private byte[] sufix;
		int length;
		int index=0;
		int bodyByte = 'A';
		
		
		HugeMsg(byte[] prefix,byte[] sufix,int length){
			this.prefix = prefix;
			this.sufix = sufix;
			this .length = length;			
		}

		@Override
		public int read() throws IOException {
			if(index< prefix.length){
				int res = prefix[index];
				index++;
				return res;
			}
			if(index< prefix.length + length){
				index++;
				return bodyByte;
			
			}
			if(index< prefix.length + length +sufix.length){
				int res = sufix[index-(length+prefix.length)];
				index++;
				return res;
			}
			return -1;
		}
		
	}
}
