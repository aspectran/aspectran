/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.demo.apm.log;

import jakarta.websocket.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogTailerManager {

    private static final String TAILERS_PROPERTY = "tailers";

    private final Map<String, LogTailer> tailers;

    private final LogtailEndpoint endpoint;

    public LogTailerManager(LogtailEndpoint endpoint, LogTailer[] tailers) {
        this.endpoint = endpoint;
        this.tailers = new HashMap<>();
        endpoint.setLogTailerManager(this);
        addLogTailer(tailers);
    }

    public void addLogTailer(LogTailer... tailers) {
        if (tailers != null) {
            for (LogTailer tailer : tailers) {
                tailer.setEndpoint(endpoint);
                this.tailers.put(tailer.getName(), tailer);
            }
        }
    }

    void join(Session session, String[] names) {
        List<String> list = new ArrayList<>();
        String[] existingNames = (String[])session.getUserProperties().get(TAILERS_PROPERTY);
        if (existingNames != null) {
            Collections.addAll(list, existingNames);
        }
        for (String name : names) {
            LogTailer tailer = tailers.get(name);
            if (tailer != null) {
                tailer.readLastLines();
                list.add(name);
                if (!tailer.isRunning()) {
                    try {
                        tailer.start();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
        session.getUserProperties().put(TAILERS_PROPERTY, list.toArray(new String[0]));
    }

    void release(Session session) {
        String[] names = (String[])session.getUserProperties().get(TAILERS_PROPERTY);
        if (names != null) {
            for (String name : names) {
                LogTailer tailer = tailers.get(name);
                if (tailer != null && tailer.isRunning() && !isUsingTailer(name)) {
                    try {
                        tailer.stop();
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
    }

    private boolean isUsingTailer(String name) {
        for (Session session : endpoint.getSessions()) {
            String[] names = (String[])session.getUserProperties().get(TAILERS_PROPERTY);
            if (names != null) {
                for (String name2 : names) {
                    if (name.equals(name2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
