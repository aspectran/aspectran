/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.demo.monitoring.log;

import com.aspectran.utils.annotation.jsr305.NonNull;
import jakarta.websocket.Session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogTailerManager {

    private static final String TAILERS_PROPERTY = "tailers";

    private final Map<String, LogTailer> tailers = new HashMap<>();

    private final LogtailEndpoint endpoint;

    public LogTailerManager(@NonNull LogtailEndpoint endpoint, LogTailer[] tailers) {
        this.endpoint = endpoint;
        endpoint.setLogTailerManager(this);
        addLogTailer(tailers);
    }

    public synchronized void addLogTailer(LogTailer... tailers) {
        if (tailers != null) {
            for (LogTailer tailer : tailers) {
                tailer.setEndpoint(endpoint);
                LogTailer old = this.tailers.put(tailer.getName(), tailer);
                if (old != null) {
                    stopTailer(old);
                }
            }
        }
    }

    void join(@NonNull Session session, String[] names) {
        List<String> list = new ArrayList<>();
        String[] existingNames = (String[])session.getUserProperties().get(TAILERS_PROPERTY);
        if (existingNames != null) {
            Collections.addAll(list, existingNames);
        }
        List<String> newNames = Arrays.asList(names);
        for (Map.Entry<String, LogTailer> entry : tailers.entrySet()) {
            String name = entry.getKey();
            if (newNames.isEmpty() || newNames.contains(name)) {
                endpoint.broadcast("joined:" + name);
                LogTailer tailer = entry.getValue();
                tailer.readLastLines();
                startTailer(tailer);
                list.add(name);
            }
        }
        session.getUserProperties().put(TAILERS_PROPERTY, list.toArray(new String[0]));
    }

    void release(@NonNull Session session) {
        String[] names = (String[])session.getUserProperties().get(TAILERS_PROPERTY);
        if (names != null) {
            for (String name : names) {
                LogTailer tailer = tailers.get(name);
                if (!isUsingTailer(name)) {
                    stopTailer(tailer);
                }
            }
        }
    }

    private void startTailer(LogTailer tailer) {
        if (tailer != null && !tailer.isRunning()) {
            try {
                tailer.start();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private void stopTailer(LogTailer tailer) {
        if (tailer != null && tailer.isRunning()) {
            try {
                tailer.stop();
            } catch (Exception e) {
                // ignore
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
