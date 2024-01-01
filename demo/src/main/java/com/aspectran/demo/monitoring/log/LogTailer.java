/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.utils.lifecycle.AbstractLifeCycle;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogTailer extends AbstractLifeCycle {

    private static final long DEFAULT_SAMPLE_INTERVAL = 500L;

    private final String name;

    /** the log file to tail */
    private final String logFile;

    /** how frequently to check for file changes; defaults to 1 second */
    private final long sampleInterval;

    private LogtailEndpoint endpoint;

    private TailerListener tailerListener;

    private Tailer tailer;

    public LogTailer(String name, String logFile) {
        this(name, logFile, DEFAULT_SAMPLE_INTERVAL);
    }

    public LogTailer(String name, String logFile, long sampleInterval) {
        this.name = name;
        this.logFile = logFile;
        this.sampleInterval = sampleInterval;
    }

    public String getName() {
        return name;
    }

    public void setEndpoint(LogtailEndpoint endpoint) {
        this.endpoint = endpoint;
        this.tailerListener = new LogTailerListener(name, endpoint);
    }

    protected void readLastLines() {
        File file = new File(logFile);
        String[] lines = readLastLines(file, 1000);
        for (String line : lines) {
            endpoint.broadcast(name + ":last:" + line);
        }
    }

    private String[] readLastLines(File file, int lastLines) {
        List<String> list = new ArrayList<>();
        try (ReversedLinesFileReader reversedLinesFileReader = ReversedLinesFileReader.builder()
                .setFile(file)
                .get()) {
            int count = 0;
            while (count++ < lastLines) {
                String line = reversedLinesFileReader.readLine();
                if (line == null) {
                    break;
                }
                list.add(line);
            }
            Collections.reverse(list);
        } catch (IOException e) {
            // ignore
        }
        return list.toArray(new String[0]);
    }

    protected void doStart() throws Exception {
        if (tailerListener == null) {
            throw new IllegalStateException("No TailerListener configured");
        }
        tailer = Tailer.builder()
                .setFile(logFile)
                .setTailerListener(tailerListener)
                .setDelayDuration(Duration.ofMillis(sampleInterval))
                .setTailFromEnd(true)
                .get();
    }

    protected void doStop() throws Exception {
        if (tailer != null) {
            tailer.close();
            tailer = null;
        }
    }

}
