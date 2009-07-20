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

package org.apache.wink.common.internal.providers.entity.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * 
 */
public final class CsvReader {

    private Reader  reader;
    private boolean endOfLine = false;
    private boolean endOfFile = false;

    public CsvReader(Reader reader) {
        this.reader = reader;
    }

    public String[] readLine() {
        if (endOfFile) {
            return null;
        }
        ArrayList<String> list = new ArrayList<String>();
        boolean startOfLine = true;
        while (!endOfLine && !endOfFile) {
            String cell = readCell();
            if (startOfLine) {
                // if this is an empty line and the end of the file
                if (endOfFile && cell.length() == 0) {
                    return null;
                }
                startOfLine = false;
            }
            list.add(cell);
        }
        endOfLine = false;
        return list.toArray(new String[list.size()]);
    }

    private String readCell() {
        try {
            StringBuilder stringBuilder = new StringBuilder();

            boolean processThisCharacter = false;
            boolean hadQuote = false;
            int read = -1;
            l: while (processThisCharacter || (read = reader.read()) != -1) {
                processThisCharacter = false;

                char character = (char)read;
                switch (character) {
                    case '"':
                        if (!hadQuote) {
                            // first quote when processing cell, set hadQuote
                            // indicator and continue
                            hadQuote = true;
                            continue l;
                        } else {
                            // middle of the string, and hadQuote.
                            // Let's check the next character:
                            read = reader.read();
                            if (read == -1) {
                                endOfFile = true;
                                break l;
                            }
                            character = (char)read;
                            switch (character) {
                                case '"':
                                    // middle of the cell, add one quote instead
                                    // of two
                                    stringBuilder.append(character);
                                    continue l;
                                default:
                                    // it seems to be a closing quote
                                    hadQuote = false;
                                    // still need to process this character!
                                    processThisCharacter = true;
                                    continue l;
                            }
                        }
                    case '\r':
                        if (hadQuote) {
                            stringBuilder.append(character);
                            continue l;
                        } else {
                            // check the next character
                            read = reader.read();
                            if (read == -1) {
                                endOfFile = true;
                                break l;
                            }
                            character = (char)read;
                            switch (character) {
                                case '\n':
                                    // end of line
                                    endOfLine = true;
                                    break l;
                                default:
                                    // append to string
                                    stringBuilder.append('\r');

                                    // still need to process this character!
                                    processThisCharacter = true;
                                    continue l;
                            }
                        }
                    case '\n':
                        if (hadQuote) {
                            stringBuilder.append(character);
                            continue l;
                        } else {
                            endOfLine = true;
                            break l;
                        }
                    case ',':
                        if (hadQuote) {
                            stringBuilder.append(character);
                            continue l;
                        } else {
                            // end of cell
                            break l;
                        }
                    default:
                        stringBuilder.append(character);
                }
            }
            if (read == -1) {
                endOfFile = true;
            }

            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
