/*
 * Copyright 2008-2017 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.component.session;

import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Created: 2017. 6. 24.</p>
 */
public class SessionCache {

    /** The cache of sessions in a hashmap */
    private final ConcurrentHashMap<String, BasicSession> sessions = new ConcurrentHashMap<>();

    public SessionCache() {
    }

    public BasicSession get(String id) {
        if (id == null) {
            return null;
        }
        return sessions.get(id);
    }

    public void put(String id, BasicSession session) {
        sessions.putIfAbsent(id, session);
    }

    public BasicSession remove(String id) {
        return sessions.remove(id);
    }

    public void clear() {
        for (BasicSession session: sessions.values()) {
            if (session.isValid() && session.isResident()) {
                session.invalidate();
            }
        }
        sessions.clear();
    }

}
