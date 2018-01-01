/*
 * Copyright (c) 2008-2017 The Aspectran Project
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
package com.aspectran.daemon.command.polling;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.daemon.Daemon;
import com.aspectran.daemon.command.Command;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CommandExecutor {

    private static final Log log = LogFactory.getLog(CommandExecutor.class);

    private final Daemon daemon;

    private final ExecutorService executorService;

    private final BlockingQueue workQueue;

    public CommandExecutor(Daemon daemon, int maxThreads) {
        this.daemon = daemon;
        this.workQueue = new SynchronousQueue<>();
        this.executorService = new ThreadPoolExecutor(
                1,
                maxThreads,
                180L,
                TimeUnit.SECONDS,
                this.workQueue
        );
    }

    public void execute(final CommandParameters parameters, final Callback callback) {
        Runnable runnable = () -> {
            String commandName = parameters.getCommandName();
            Command command = daemon.getCommandRegistry().getCommand(commandName);
            if (command != null) {
                String result;
                try {
                    result = command.execute(parameters);
                    callback.success();
                } catch (Exception e) {
                    result = e.toString();
                    callback.failure();
                }
                parameters.setOutput(result);
            } else {
                parameters.setOutput("No command mapped to '" + commandName + "'");
                callback.failure();
            }
        };
        executorService.execute(runnable);
    }

    public int getQueueSize() {
        return workQueue.size();
    }

    public void shutdown() {
        log.info("Shutting down executor...");
        executorService.shutdown();
        if (!executorService.isTerminated()) {
            while (true) {
                log.info("Waiting for executor to terminate...");
                if (executorService.isTerminated()) {
                    break;
                }
                try {
                    if (executorService.awaitTermination(5000L, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                } catch (InterruptedException ignored) {
                    // ignore
                }
            }
        }
    }

    public interface Callback {

        void success();

        void failure();

    }

}
