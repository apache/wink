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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;

import junit.framework.TestCase;

import org.apache.wink.common.internal.MultivaluedMapImpl;
import org.apache.wink.common.internal.providers.multipart.InMultiPartProvider;
import org.apache.wink.common.internal.providers.multipart.OutMultiPartProvider;
import org.apache.wink.common.model.multipart.InMultiPart;
import org.apache.wink.common.model.multipart.InPart;
import org.apache.wink.common.model.multipart.OutMultiPart;
import org.apache.wink.common.model.multipart.OutPart;


public class TestMultiPartProvider extends TestCase{
	public void testPassFile() throws IOException{
		//String boundery ="This is my boundery 123";
		ArrayList<String> resources = new ArrayList<String>();
		resources.add("msg01.txt");
		//resources.add("msg02.txt");		
		OutMultiPart omp = new FileOutMultiPart(resources);
		String bounary ="This is the boundary lalala"; 
		omp.setBoundary(bounary);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutMultiPartProvider ompp = new OutMultiPartProvider();
		MultivaluedMapImpl<String, Object> headers = new MultivaluedMapImpl<String, Object>();
		MediaType mt = MediaType.valueOf("multipart/mixed; boundary="+bounary);
		ompp.writeTo(omp, FileOutMultiPart.class, null, null, mt, headers, baos);
		byte[] result = baos.toByteArray();
		MultivaluedMapImpl<String, String> headers2 = convertHeaders(headers);
		String s = new String(result);
		
		
		ByteArrayInputStream bais = new ByteArrayInputStream(result);
		InMultiPartProvider inProvider = new InMultiPartProvider();		
		InMultiPart inMP = inProvider.readFrom(InMultiPart.class, null, null,mt, headers2, bais);
		int i=0;
		while(inMP.hasNext()){
			InPart ip = inMP.next();
			verifyStreamsCompare(ip.getInputStream(),getClass().getResourceAsStream(resources.get(i)));
			i++;
			
		}
		 
	}
	private void verifyStreamsCompare(InputStream is1,InputStream is2) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is1));
//		String s1 = Stream2String(is1);
//		String s2 = Stream2String(is2);
		
		int b1,b2;
		while((b1=is1.read())!=-1){
			b2=is2.read();
			assertEquals(b1,b2);
		}
		assertEquals(is1.read(),-1);
		assertEquals(is2.read(),-1);
		
	}
	private MultivaluedMapImpl<String, String> convertHeaders(MultivaluedMapImpl<String, Object> inh){
		MultivaluedMapImpl<String, String> headers2 = new MultivaluedMapImpl<String, String>();
		Iterator<String> it =inh.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			headers2.add(key, inh.get(key).toString());
		}
		return headers2;
		
	}
	
	public class FileOutMultiPart extends OutMultiPart{
		List<FileOutPart> resources ;
		public FileOutMultiPart(List<String> files){
			resources = new ArrayList<FileOutPart>();
			for(String s:files){
				resources.add(new FileOutPart(s));
			}			
		}
		
		@Override
		public Iterator<? extends OutPart> getIterator() {
			return resources.iterator();
			
		}		
	}
	public class FileOutPart extends OutPart{
		String resource;
		public FileOutPart (String resource){
			this.resource = resource;
		}
		
		@Override
		public void writeBody(OutputStream os,Providers providers) throws IOException {
			InputStream in = getClass().getResourceAsStream(resource);
			int b;
			while ((b = in.read())!= -1) {
				os.write(b);
			}
			
		}
	}

	public static String Stream2String (InputStream in) throws IOException {
	    StringBuffer out = new StringBuffer();
	    byte[] b = new byte[4096];
	    for (int n; (n = in.read(b)) != -1;) {
	        out.append(new String(b, 0, n));
	    }
	    return out.toString();
	}
}
