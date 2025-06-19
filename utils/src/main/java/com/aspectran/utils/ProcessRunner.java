/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The ProcessRunner class helps running external processes.
 *
 * <p>Created: 2019-04-12</p>
 */
public class ProcessRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProcessRunner.class);

    private final AtomicBoolean running = new AtomicBoolean();

    private final AtomicBoolean terminated = new AtomicBoolean();

    private final ProcessLogger processLogger;

    private String workingDir;

    private Process process;

    public ProcessRunner() {
        this(null);
    }
    public ProcessRunner(ProcessLogger processLogger) {
        this.processLogger = processLogger;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public int run(String[] command) throws IOException, InterruptedException {
        return run(command, null);
    }

    public int run(String[] command, PrintWriter errOut) throws IOException, InterruptedException {
        Assert.state(!running.get(), "There is already a running process");
        running.set(true);
        terminated.set(false);
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            if (workingDir != null) {
                builder.directory(new File(workingDir));
            }
            process = builder.start();
            if (processLogger != null || logger.isDebugEnabled()) {
                readNormalOutput(process);
            }
            if (errOut != null || processLogger != null || logger.isDebugEnabled()) {
                readErrorOutput(process, errOut);
            }
            return process.waitFor();
        } finally {
            process = null;
            running.set(false);
        }
    }

    public void runInBackground(String[] command) {
        new Thread(() -> {
            try {
                run(command);
            } catch (Exception e) {
                String message = "Error running process in background";
                if (processLogger != null) {
                    processLogger.error(message, e);
                }
                logger.error(message, e);
            }
        }).start();
    }

    public boolean isRunning() {
        return running.get();
    }

    public boolean isTerminated() {
        return terminated.get();
    }

    public void terminate() {
        if (isRunning() && process != null) {
            terminated.set(true);
            process.destroy();
        }
    }

    private void readNormalOutput(Process process) {
        try (InputStream is = process.getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (processLogger != null) {
                    processLogger.info(line);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug(line);
                }
            }
        } catch (IOException e) {
            // ignore
        }
    }

    private void readErrorOutput(Process process, PrintWriter errOut) {
        try (InputStream is = process.getErrorStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (errOut != null) {
                    errOut.println(line);
                }
                if (processLogger != null) {
                    processLogger.warn(line);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug(line);
                }
            }
        } catch (IOException e) {
            // ignore
        }
    }

}
