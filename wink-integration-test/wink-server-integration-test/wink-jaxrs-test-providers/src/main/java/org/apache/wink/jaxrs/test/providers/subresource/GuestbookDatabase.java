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

package org.apache.wink.jaxrs.test.providers.subresource;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple in-memory data store.
 */
public class GuestbookDatabase {

    private static GuestbookDatabase guestbook = new GuestbookDatabase();

    private Map<Integer, Comment>    comments  =
                                                   Collections
                                                       .synchronizedMap(new HashMap<Integer, Comment>());

    private int                      counter   = 0;

    private GuestbookDatabase() {
        /* private singleton constructor */
    }

    public static GuestbookDatabase getGuestbook() {
        return guestbook;
    }

    public Comment getComment(Integer id) {
        return comments.get(id);
    }

    public void storeComment(Comment c) {
        comments.put(c.getId(), c);
    }

    public Collection<Integer> getCommentKeys() {
        return comments.keySet();
    }

    public void deleteComment(Integer id) {
        if (id == -99999) {
            throw new Error("Simulated error");
        }

        if (comments.remove(id) == null) {
            throw new NullPointerException("The comment did not previously exist.");
        }
    }

    public synchronized int getAndIncrementCounter() {
        ++counter;
        return counter;
    }
}
