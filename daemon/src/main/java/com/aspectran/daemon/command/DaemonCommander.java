package com.aspectran.daemon.command;

import com.aspectran.daemon.Daemon;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DaemonCommander {

    private final Daemon daemon;

    private ExecutorService executorService;

    public DaemonCommander(Daemon daemon) {
        this.daemon = daemon;
    }

    public void setMaxThreads(int maxThreads) {
        executorService = Executors.newFixedThreadPool(maxThreads);
    }

    public void execute(File[] commandFiles) {
        if (executorService == null) {
            setMaxThreads(CommandPoller.DEFAULT_MAX_THREADS);
        }
        for (File file : commandFiles) {
            Runnable runnable = () -> {
                //TODO
            };
            executorService.execute(runnable);
        }
    }

}
