package com.aspectran.core.util;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

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

    private static final Log log = LogFactory.getLog(ProcessRunner.class);

    private final AtomicBoolean running = new AtomicBoolean();

    private final AtomicBoolean terminated = new AtomicBoolean();

    private final ProcessLogger logger;

    private String workingDir;

    private Process process;

    public ProcessRunner() {
        this(null);
    }
    public ProcessRunner(ProcessLogger logger) {
        this.logger = logger;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public int run(String[] command) throws IOException, InterruptedException {
        return run(command, null);
    }

    public int run(String[] command, PrintWriter errOut) throws IOException, InterruptedException {
        if (running.get()) {
            throw new IllegalStateException("There is already a running process");
        }
        running.set(true);
        terminated.set(false);
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            if (workingDir != null) {
                builder.directory(new File(workingDir));
            }
            process = builder.start();
            if (logger != null || log.isDebugEnabled()) {
                readNormalOutput(process);
            }
            if (errOut != null || logger != null || log.isDebugEnabled()) {
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
                if (logger != null) {
                    logger.error(message, e);
                }
                log.error(message, e);
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
                if (logger != null) {
                    logger.info(line);
                }
                if (log.isDebugEnabled()) {
                    log.debug(line);
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
                if (logger != null) {
                    logger.warn(line);
                }
                if (log.isDebugEnabled()) {
                    log.debug(line);
                }
            }
        } catch (IOException e) {
            // ignore
        }
    }

}
