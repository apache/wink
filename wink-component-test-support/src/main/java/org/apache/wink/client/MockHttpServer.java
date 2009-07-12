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
 

package org.apache.wink.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class MockHttpServer extends Thread {

    private static byte[]         NEW_LINE                = "\r\n".getBytes();

    private Thread                serverThread            = null;
    private ServerSocket          serverSocket            = null;
    private boolean               serverStarted           = false;
    private ServerSocketFactory   serverSocketFactory     = null;
    private int                   readTimeOut             = 5000;                         // 5 seconds
    private int                   delayResponseTime       = 0;

    // request data
    private String                requestMethod           = null;
    private String                requestUrl              = null;
    private Map<String, String>   requestHeaders          = new HashMap<String, String>();
    private ByteArrayOutputStream requestContent          = new ByteArrayOutputStream();

    // mock response data
    private int                   mockResponseCode        = 200;
    private Map<String, String>   mockResponseHeaders     = new HashMap<String, String>();
    private byte[]                mockResponseContent     = "".getBytes();
    private String                mockResponseContentType = "text/plain;charset=utf-8";
    private boolean               mockResponseContentEchoRequest;
    private int                   serverPort;

    public MockHttpServer(int serverPort) {
        this(serverPort, false);
    }

    public MockHttpServer(int serverPort, boolean ssl) {
        this.serverPort = serverPort;
        try {
            serverSocketFactory = ServerSocketFactory.getDefault();
            if (ssl) {
                serverSocketFactory = SSLServerSocketFactory.getDefault();
            }
            while (serverSocket == null) {
                try {
                    serverSocket = serverSocketFactory.createServerSocket(++this.serverPort);
                } catch (BindException e) {

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void startServer() {
        if (serverStarted)
            return;

        // start the server thread
        start();
        serverStarted = true;

        // wait for the server thread to start
        waitForServerToStart();
    }

    private synchronized void waitForServerToStart() {
        try {
            wait(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void waitForServerToStop() {
        try {
            wait(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        serverThread = Thread.currentThread();
        executeLoop();
    }

    private void executeLoop() {
        serverStarted();
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                HttpProcessor processor = new HttpProcessor(socket);
                processor.run();
            }
        } catch (IOException e) {
            if (e instanceof SocketException) {
                if (!("Socket closed".equalsIgnoreCase(e.getMessage()) || "Socket is closed".equalsIgnoreCase(e.getMessage()))) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            } else {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } finally {
            // notify that the server was stopped
            serverStopped();
        }
    }

    private synchronized void serverStarted() {
        // notify the waiting thread that the thread started
        notifyAll();
    }

    private synchronized void serverStopped() {
        // notify the waiting thread that the thread started
        notifyAll();
    }

    public synchronized void stopServer() {
        if (!serverStarted)
            return;

        try {
            serverStarted = false;
            // the server may be sleeping somewhere...
            serverThread.interrupt();
            // close the server socket
            serverSocket.close();
            // wait for the server to stop
            waitForServerToStop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class HttpProcessor {

        private Socket socket;

        public HttpProcessor(Socket socket) throws SocketException {
            // set the read timeout (5 seconds by default)
            socket.setSoTimeout(readTimeOut);
            socket.setKeepAlive(false);
            this.socket = socket;
        }

        public void run() {
            try {
                processRequest(socket);
                processResponse(socket);
            } catch (IOException e) {
                if (e instanceof SocketException) {
                    if (!("socket closed".equalsIgnoreCase(e.getMessage()))) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                } else {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            } finally {
                try {
                    socket.shutdownOutput();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void processRequest(Socket socket) throws IOException {
            requestContent.reset();
            BufferedInputStream is = new BufferedInputStream(socket.getInputStream());
            String requestMethodHeader = new String(readLine(is));
            if (requestMethodHeader == null) {
                return;
            }
            processRequestMethod(requestMethodHeader);
            processRequestHeaders(is);
            processRequestContent(is);
        }

        private void processRequestMethod(String requestMethodHeader) {
            String[] parts = requestMethodHeader.split(" ");
            if (parts.length < 2) {
                throw new RuntimeException("illegal http request");
            }
            requestMethod = parts[0];
            requestUrl = parts[1];
        }

        private void processRequestHeaders(InputStream is) throws IOException {
            byte[] line = null;
            while ((line = readLine(is)) != null) {
                String lineStr = new String(line);
                // if there are no more headers
                if ("".equals(lineStr.trim())) {
                    break;
                }
                addRequestHeader(lineStr);
            }
        }

        private void processRequestContent(InputStream is) throws NumberFormatException,
            IOException {
            if (!("PUT".equals(requestMethod) || "POST".equals(requestMethod))) {
                return;
            }

            if ("chunked".equals(requestHeaders.get("Transfer-Encoding"))) {
                processChunkedContent(is);
            } else {
                processRegularContent(is);
            }

            if (mockResponseContentEchoRequest) {
                mockResponseContent = requestContent.toByteArray();
            }
        }

        private void processRegularContent(InputStream is) throws IOException {
            String contentLength = requestHeaders.get("Content-Length");
            if (contentLength == null) {
                return;
            }
            int contentLen = Integer.parseInt(contentLength);
            byte[] bytes = new byte[contentLen];
            is.read(bytes);
            requestContent.write(bytes);
        }

        private void processChunkedContent(InputStream is) throws IOException {
            requestContent.write("".getBytes());
            byte[] chunk = null;
            byte[] line = null;
            boolean lastChunk = false;
            // we should exit this loop only after we get to the end of stream
            while (!lastChunk && (line = readLine(is)) != null) {

                String lineStr = new String(line);
                // a chunk is identified as:
                //  1) not an empty line
                //  2) not 0. 0 means that there are no more chunks
                if ("0".equals(lineStr)) {
                    lastChunk = true;
                }

                if (!lastChunk) {
                    // get the length of the current chunk (it is in hexadecimal form)
                    int chunkLen = Integer.parseInt(lineStr, 16);

                    // get the chunk
                    chunk = getChunk(is, chunkLen);

                    // consume the newline after the chunk that separates between 
                    // the chunk content and the next chunk size
                    readLine(is);

                    requestContent.write(chunk);
                }
            }

            // do one last read to consume the empty line after the last chunk
            if (lastChunk) {
                readLine(is);
            }
        }

        private byte[] readLine(InputStream is) throws IOException {
            int n;
            ByteArrayOutputStream tmpOs = new ByteArrayOutputStream();
            while ((n = is.read()) != -1) {
                if (n == '\r') {
                    n = is.read();
                    if (n == '\n') {
                        return tmpOs.toByteArray();
                    } else {
                        tmpOs.write('\r');
                        if (n != -1) {
                            tmpOs.write(n);
                        } else {
                            return tmpOs.toByteArray();
                        }
                    }
                } else if (n == '\n') {
                    return tmpOs.toByteArray();
                } else {
                    tmpOs.write(n);
                }
            }
            return tmpOs.toByteArray();
        }

        private byte[] getChunk(InputStream is, int len) throws IOException {
            ByteArrayOutputStream chunk = new ByteArrayOutputStream();
            int read = 0;
            int totalRead = 0;
            byte[] bytes = new byte[512];
            // read len bytes as the chunk
            while (totalRead < len) {
                read = is.read(bytes, 0, Math.min(bytes.length, len - totalRead));
                chunk.write(bytes, 0, read);
                totalRead += read;
            }
            return chunk.toByteArray();
        }

        private void addRequestHeader(String line) {
            String[] parts = line.split(": ");
            requestHeaders.put(parts[0], parts[1]);
        }

        private void processResponse(Socket socket) throws IOException {
            // if delaying the response failed (because it was interrupted) 
            // then don't send the response
            if (!delayResponse())
                return;

            OutputStream sos = socket.getOutputStream();
            BufferedOutputStream os = new BufferedOutputStream(sos);
            String reason = "";
            Status statusCode = Response.Status.fromStatusCode(mockResponseCode);
            if(statusCode !=null){
                reason = statusCode.toString();
            }
            os.write(("HTTP/1.1 " + mockResponseCode + " " + reason).getBytes());
            os.write(NEW_LINE);
            processResponseHeaders(os);
            processResponseContent(os);
            os.flush();
        }

        // return:
        //    true - delay was successful
        //    false - delay was unsuccessful
        private boolean delayResponse() {
            // delay the response by delayResponseTime milliseconds
            if (delayResponseTime > 0) {
                try {
                    Thread.sleep(delayResponseTime);
                    return true;
                } catch (InterruptedException e) {
                    return false;
                }
            }
            return true;
        }

        private void processResponseContent(OutputStream os) throws IOException {
            if (mockResponseContent == null) {
                return;
            }

            os.write(mockResponseContent);
        }

        private void processResponseHeaders(OutputStream os) throws IOException {
            addServerResponseHeaders();
            for (String header : mockResponseHeaders.keySet()) {
                os.write((header + ": " + mockResponseHeaders.get(header)).getBytes());
                os.write(NEW_LINE);
            }
            os.write(NEW_LINE);
        }

        private void addServerResponseHeaders() {
            mockResponseHeaders.put("Content-Type", mockResponseContentType);
            mockResponseHeaders.put("Content-Length", mockResponseContent.length + "");
            mockResponseHeaders.put("Server", "Mock HTTP Server v1.0");
            mockResponseHeaders.put("Connection", "closed");
        }
    }

    public void setMockResponseHeaders(Map<String, String> headers) {
        mockResponseHeaders.clear();
        mockResponseHeaders.putAll(headers);
    }

    public void setMockResponseHeader(String name, String value) {
        mockResponseHeaders.put(name, value);
    }

    public void setMockResponseCode(int responseCode) {
        this.mockResponseCode = responseCode;
    }

    public void setMockResponseContent(String content) {
        mockResponseContent = content.getBytes();
    }

    public void setMockResponseContent(byte[] content) {
        mockResponseContent = content;
    }

    public void setMockResponseContentType(String type) {
        mockResponseContentType = type;
    }

    public void setMockResponseContentEchoRequest(boolean echo) {
        mockResponseContentEchoRequest = echo;
    }

    public void setReadTimeout(int milliseconds) {
        readTimeOut = milliseconds;
    }

    public void setDelayResponse(int milliseconds) {
        delayResponseTime = milliseconds;
    }

    public String getRequestContentAsString() {
        return requestContent.toString();
    }

    public byte[] getRequestContent() {
        return requestContent.toByteArray();
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public static void main(String[] args) {
        MockHttpServer server = null;
        try {
            /*
             * StringReader reader = new StringReader("lalalalala\r\n");
             * BufferedReader r = new BufferedReader(reader); String a =
             * r.readLine(); String b = r.readLine(); System.out.println(a);
             * System.out.println(b); server = new MockHttpServer(3334);
             * server.setMockResponseContent("Howdee!");
             * server.setMockResponseContentType("text/plain;charset=UTF-8");
             * //Map<String,String> maps = new HashMap<String,String>();
             * //maps.put("Location", "http://localhost:3333/lalala");
             * //server.setMockResponseHeaders(maps); server.startServer();
             * 
             * URL u = new
             * URL("http://localhost:3334/qadefect-service/rest/defects/2");
             * HttpURLConnection huc = (HttpURLConnection)u.openConnection();
             * huc.setRequestMethod("PUT"); huc.setDoOutput(true);
             * huc.setChunkedStreamingMode(40);
             * huc.addRequestProperty("Content-Type", "application/xml");
             * huc.connect(); OutputStream os = huc.getOutputStream();
             * os.write("martinmartinartinmartinmartin".getBytes());
             * //os.flush();
             * os.write("martinmartinartinmartinmartin".getBytes()); os.flush();
             * os.close(); huc.getResponseCode(); huc.getHeaderField("Server");
             * InputStream is = huc.getInputStream(); byte[] by = new byte[0];
             * byte[] by1 = new byte[5]; int readdd = is.read(by);
             * System.out.println("readdd = " + readdd); readdd = is.read(by1);
             * System.out.println("readdd = " + readdd); readdd = is.read(by);
             * System.out.println("readdd = " + readdd); readdd = is.read(by1);
             * System.out.println("readdd = " + readdd); readdd = is.read(by);
             * System.out.println("readdd = " + readdd); huc.disconnect();
             */

            //            RestClient client = new RestClient();
            //            
            //            // init the resource
            //            String url = "http://localhost:3333/qadefect-service/rest/defects/2";
            //            Resource resource = client.newResource(url);     
            //            
            //            // get defect
            //            Response<TextEntity> response = resource.doGet(TextEntity.class);
            //            TextEntity d = response.getEntity();
            //            System.out.println("GET returned content: " + d.getText());
            //
            //            TextEntity text = new TextEntity();
            //            text.setText("Hello");
            //            response = resource.doPut(text, TextEntity.class);
            //            d = response.getEntity();
            //            System.out.println("PUT sent content: " + server.getRequestContent());
            //            System.out.println("PUT returned content: " + d.getText());
            //            System.out.println(server.getRequestUrl());
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (server != null) {
                server.stopServer();
            }
        }
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getServerPort() {
        return serverPort;
    }
}
