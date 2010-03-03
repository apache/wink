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

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.wink.common.internal.CaseInsensitiveMultivaluedMap;
import org.apache.wink.common.internal.i18n.Messages;

/*
 * TODO: Add the option to get the preamble
 * TODO: known limitations:the headers length of a part can not exceed the buff.length
 * 
 */

public class MultiPartParser {
    public final static String             SEP                 = "\n";           //$NON-NLS-1$

    private InputStream                    is;
    private byte[]                         boundaryBA;
    static private byte[]                  boundaryDelimiterBA = "--".getBytes(); //$NON-NLS-1$

    private MultivaluedMap<String, String> partHeaders;
    private PartInputStream                partIS;

    private static int                     BOUNDARY_TYPE_START = 0;
    private static int                     BOUNDARY_TYPE_END   = 1;
    // private static int BOUNDARY_TYPE_INVALID = 2;

    private byte[]                         buff;
    // the next byte to return
    private int                            buffIdx             = 0;
    // The number of bytes that were set on the buffer (red from is);
    private int                            buffSize            = 0;
    // the position of the next boundary ("--boundary"), -1 if does not exist in
    // this buffer
    private int                            boundryIdx          = -1;
    // The index of the byte that is suspected of been the boundary
    private int                            saveIdx             = 0;

    // This is a temp array that is used to read stuff into it.
    private byte[]                         temp                = new byte[1024];

    public MultiPartParser(InputStream is, String boundary) {
        this.is = is;
        boundaryBA = ("--" + boundary).getBytes(); //$NON-NLS-1$
        // make sure to allocate a buffer that is at least double then the
        // boundary length
        int buffLength = Math.max(8192, boundaryBA.length * 2);
        buff = new byte[buffLength];

    }

    /**
     * This method shift the bytes to the left and update the indexes it is used
     * to clear room for additional bytes to be read.
     */
    private void shiftBuff() {
        System.arraycopy(buff, buffIdx, buff, 0, buffSize - buffIdx);

        buffSize -= buffIdx;
        saveIdx -= buffIdx;
        if (saveIdx < 0)
            saveIdx = 0;
        /*
         * throw new
         * RuntimeException("This should never happend, we found a bug.");
         */
        boundryIdx = Math.max(-1, boundryIdx - buffIdx);
        buffIdx = 0;

        // for debug purposes
        // Arrays.fill(buff, buffIdx, buff.length-1, (byte)0);

    }

    public boolean nextPart() throws IOException {
        // if this is the first next just get rid of the PREAMBLE
        if (partIS == null) {
            partIS = new PartInputStream();
        }
        // clear the part/preamble bytes that were not read
        digestPartStream();
        if (digestBoundary() == BOUNDARY_TYPE_END)
            return false;
        partIS.setState(PartInputStream.STATE_NOT_ACTIVE);
        partIS = new PartInputStream();
        partHeaders = parseHeaders();
        return partHeaders != null;
    }

    public InputStream getPartBodyStream() {
        return partIS;
    }

    public MultivaluedMap<String, String> getPartHeaders() {
        return partHeaders;
    }

    // read till end of stream (next boundary)
    private void digestPartStream() throws IOException {
        while (partIS.read(temp) != -1) {
        }
    }

    private boolean compareByte(byte[] a, int aOffset, byte[] b, int bOffset, int length) {
        for (int i = 0; i < length; i++) {
            if (a[aOffset + i] != b[bOffset + i])
                return false;
        }
        return true;
    }

    private int digestBoundary() throws IOException {
        // it might be that there is a new line before the boundary
        digestNewLine();

        // promote pointers to the end of the boundary
        buffIdx += boundaryBA.length;
        saveIdx += boundaryBA.length; // DO NOT DELETE

        // check if this is an end boundary
        int unredBytes = verifyByteReadyForRead(2);
        if (unredBytes >= 2) {
            if (compareByte(buff, buffIdx, boundaryDelimiterBA, 0, boundaryDelimiterBA.length))
                return BOUNDARY_TYPE_END;
        }
        // OK
        digestNewLine();
        boundryIdx = -1;
        findBounderyIfNeeded();
        return BOUNDARY_TYPE_START;
    }

    private void findBounderyIfNeeded() {
        if (boundryIdx == -1) {
            boundryIdx = indexOf(buff, saveIdx, buffSize, boundaryBA);
            if (boundryIdx != -1) {
                int nlSize = 0;
                if (boundryIdx > 1) {
                    if (buff[boundryIdx - 2] == '\r' && buff[boundryIdx - 1] == '\n')
                        nlSize = 2;
                    else
                        nlSize = 1;
                }
                if (boundryIdx == 1) {
                    nlSize = 1;
                }

                saveIdx = boundryIdx - nlSize;
            } else {
                // the boundary was not found, but we can promote the save till
                // boundary size + NL size
                saveIdx = Math.max(saveIdx, buffSize - (boundaryBA.length + 2));
            }
        }
    }

    private int verifyByteReadyForRead(int required) throws IOException {
        int unreadBytes = buffSize - buffIdx - 1;
        if (unreadBytes < required) {
            fetch(required - unreadBytes);
            unreadBytes = buffSize - buffIdx;
        }
        return unreadBytes;
    }

    /**
     * @param minmum - the minimum number of byte to insist of fetching, the
     *            method might fetch less only in case it get to the end of the
     *            stream
     * @return number of bytes that were fetched, -1 if no more to fetch
     * @throws IOException
     */
    private int fetch(int minmum) throws IOException {
        int res = 0;
        int max2featch = buff.length - buffSize;

        if (max2featch < minmum) {
            shiftBuff();
            max2featch = buff.length - buffSize;
        }

        while (res < minmum && max2featch > 0) {
            max2featch = buff.length - buffSize;

            int read = is.read(buff, buffSize, max2featch);
            if (read == -1) {
                if (res == 0)
                    return -1;
                else
                    break;
            }
            res += read;
            buffSize += read;
        }
        findBounderyIfNeeded();
        return res;

    }

    private void digestNewLine() throws IOException {
        // make sure we have enough byte to read
        int unreadBytes = verifyByteReadyForRead(2);
        int size = 0;

        if (unreadBytes >= 2 && buff[buffIdx] == '\r' && buff[buffIdx + 1] == '\n')
            size = 2;
        else if (buff[buffIdx] == '\r')
            size = 1;
        else if (buff[buffIdx] == '\n')
            size = 1;
        buffIdx += size;
        if (saveIdx < buffIdx)
            saveIdx = buffIdx;
    }

    private int indexOf(byte[] ba, int start, int end, byte[] what) {
        for (int i = start; i < end - what.length + 1; i++) {
            // only if the first byte equals do the compare (to improve
            // performance)
            if (ba[i] == what[0])
                if (compareByte(ba, i, what, 0, what.length))
                    return i;
        }
        return -1;
    }

    /**
     * @return
     * @throws IOException
     */
    private MultivaluedMap<String, String> parseHeaders() throws IOException {

        MultivaluedMap<String, String> headers = new CaseInsensitiveMultivaluedMap<String>();

        String line;
        do {
            line = readLine();
            if (line == null || line.equals("")) //$NON-NLS-1$
                break;
            int semIdx = line.indexOf(":"); //$NON-NLS-1$
            headers.add(line.substring(0, semIdx).trim(), line.substring(semIdx + 1).trim());

        } while (true);
        if (saveIdx < buffIdx)
            saveIdx = buffIdx;
        return headers;
    }

    private String readLine() throws IOException {

        int lineIdx = 0;
        int breakeSize = 0;
        while (lineIdx <= verifyByteReadyForRead(lineIdx)) {
            if (buff[buffIdx + lineIdx] == '\n') {
                breakeSize = 1;
                break;
            }
            if (buff[buffIdx + lineIdx] == '\r') {
                if ((verifyByteReadyForRead(lineIdx + 1) >= lineIdx + 1) && (buff[buffIdx + lineIdx
                    + 1] == '\n')) {
                    breakeSize = 2;
                    break;
                } else {
                    breakeSize = 1;
                    break;
                }
            }
            lineIdx++;
        }

        // got to the end of input without NL
        if (lineIdx == 0) {
            buffIdx += breakeSize;
            return null;
        }

        String hdr = new String(buff, buffIdx, lineIdx);
        buffIdx += lineIdx + breakeSize;
        return hdr;
    }

    public class PartInputStream extends InputStream {
        // The state of the part Stream
        // 0 active
        // 1 not active (the Parser already moved to the next part.)
        private int             state            = 0;
        public final static int STATE_ACTIVE     = 0;
        public final static int STATE_NOT_ACTIVE = 1;

        public void setState(int status) {
            this.state = status;

        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (state == STATE_NOT_ACTIVE) {
                throw new IOException(Messages.getMessage("multiPartStreamAlreadyClosed")); //$NON-NLS-1$
            }
            int available = verifyNumOfByteToReadB4Boundary(len);
            if (available < 1) {
                return available;
            }
            int size2copy = Math.min(len, available);
            System.arraycopy(buff, buffIdx, b, off, size2copy);
            buffIdx += size2copy;
            return size2copy;
        }

        @Override
        public int read() throws IOException {
            if (state == STATE_NOT_ACTIVE) {
                throw new IOException(Messages.getMessage("multiPartStreamAlreadyClosed")); //$NON-NLS-1$
            }
            int i = verifyNumOfByteToReadB4Boundary(1);
            if (i < 1)
                return -1;
            // make sure that the return value is 0 - 255
            int res = buff[buffIdx] & 0xff;
            if (res < 0) {
                int t = 0;
                t++;
            }
            buffIdx++;
            return res;

        }

        /**
         * @param minmum - the minimum number of byte to insist of fetching, the
         *            method might fetch less in case it get to the end of the
         *            stream or in case there minimum exceed the num of byte it
         *            can hold in the buffer
         * @return number of bytes that were fetched, -1 if no more to fetch
         * @throws IOException
         */
        private int verifyNumOfByteToReadB4Boundary(int minmum) throws IOException {
            int availabe = saveIdx - buffIdx;
            if (availabe >= minmum)
                return availabe;
            //
            if (saveIdx <= boundryIdx) {
                if (availabe == 0)
                    return -1;
                return availabe;
            }
            int fetched = fetch(minmum - availabe);
            availabe = saveIdx - buffIdx;
            if (availabe == 0 && fetched == -1)
                return -1;

            return availabe;

        }

        @Override
        public int available() {
            return saveIdx - buffIdx;
        }
    }
}
