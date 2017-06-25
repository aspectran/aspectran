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
 * <p>Created: 2017. 6. 15.</p>
 */
public class DefaultSessionDataStore implements SessionDataStore {

    /** The repository of sessions in a hashmap */
    private final ConcurrentHashMap<String, SessionData> sessionDataMap = new ConcurrentHashMap<>();

    @Override
    public SessionData load(String id) {
        if (id == null) {
            return null;
        }
        return sessionDataMap.get(id);
    }

    @Override
    public void store(String id, SessionData sessionData) {
        sessionDataMap.putIfAbsent(id, sessionData);
    }

    @Override
    public boolean delete(String id) {
        return (sessionDataMap.remove(id) != null);
    }

}
