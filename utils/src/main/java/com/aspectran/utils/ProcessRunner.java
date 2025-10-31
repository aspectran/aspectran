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
 * A utility class for running external processes and capturing their output.
 * <p>It provides methods to execute commands, manage process lifecycle,
 * and log standard and error output.</p>
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

    /**
     * Creates a new ProcessRunner with no specific process logger.
     */
    public ProcessRunner() {
        this(null);
    }

    /**
     * Creates a new ProcessRunner with a specified process logger.
     * @param processLogger the logger to use for process output
     */
    public ProcessRunner(ProcessLogger processLogger) {
        this.processLogger = processLogger;
    }

    /**
     * Sets the working directory for the process.
     * @param workingDir the working directory path
     */
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * Runs an external command and waits for its completion.
     * Standard output is logged at INFO level, and error output at WARN level.
     * @param command the command and its arguments as an array
     * @return the exit value of the process
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the current thread is interrupted while waiting for the process to terminate
     */
    public int run(String[] command) throws IOException, InterruptedException {
        return run(command, null);
    }

    /**
     * Runs an external command and waits for its completion.
     * Standard output is logged at INFO level, and error output is written to the provided PrintWriter.
     * @param command the command and its arguments as an array
     * @param errOut a PrintWriter to which error output will be written (may be {@code null})
     * @return the exit value of the process
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the current thread is interrupted while waiting for the process to terminate
     */
    public int run(String[] command, PrintWriter errOut) throws IOException, InterruptedException {
        Assert.state(!running.get(), "There is already a running process");
        running.set(true);
        terminated.set(false);

        Thread stdOutReader = null;
        Thread stdErrReader = null;

        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            if (workingDir != null) {
                builder.directory(new File(workingDir));
            }
            process = builder.start();

            if (processLogger != null || logger.isDebugEnabled()) {
                stdOutReader = new Thread(() -> readNormalOutput(process));
                stdOutReader.start();
            }
            if (errOut != null || processLogger != null || logger.isDebugEnabled()) {
                stdErrReader = new Thread(() -> readErrorOutput(process, errOut));
                stdErrReader.start();
            }

            int exitCode = process.waitFor();

            if (stdOutReader != null) {
                stdOutReader.join();
            }
            if (stdErrReader != null) {
                stdErrReader.join();
            }

            return exitCode;
        } finally {
            process = null;
            running.set(false);
        }
    }

    /**
     * Runs an external command in a new background thread.
     * Any exceptions during execution are logged.
     * @param command the command and its arguments as an array
     */
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

    /**
     * Checks if a process is currently running.
     * @return {@code true} if a process is running, {@code false} otherwise
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Checks if the process has been terminated (either naturally or by calling {@link #terminate()}).
     * @return {@code true} if the process is terminated, {@code false} otherwise
     */
    public boolean isTerminated() {
        return terminated.get();
    }

    /**
     * Terminates the currently running process.
     * If no process is running, this method does nothing.
     */
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
            if (!isTerminated()) {
                logger.warn("Error reading process standard output", e);
            }
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
            if (!isTerminated()) {
                logger.warn("Error reading process error output", e);
            }
        }
    }

}
