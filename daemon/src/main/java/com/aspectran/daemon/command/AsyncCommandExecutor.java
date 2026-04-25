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
package com.aspectran.daemon.command;

import com.aspectran.core.context.config.DaemonExecutorConfig;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Executes daemon commands asynchronously using a thread pool.
 * <p>This class employs a two-stage process for command execution. First, it
 * synchronously reserves the command using {@link CommandExecutor#reserveCommand(CommandParameters)}.
 * This ensures that the executor's state (e.g., active command count, isolation flags)
 * is updated immediately, preventing race conditions during rapid command submission
 * or polling. Second, it submits the command to an internal {@link ThreadPoolExecutor}
 * for background execution.</p>
 * <p>If a command is rejected during the initial reservation check, {@link #execute(CommandParameters, Callback)}
 * returns {@code false} immediately. If accepted, it returns {@code true}, and the
 * provided {@link Callback} is invoked once the background task completes.</p>
 *
 * <p>Created: 2026. 04. 23.</p>
 */
public class AsyncCommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AsyncCommandExecutor.class);

    private static final int DEFAULT_MAX_THREADS = Runtime.getRuntime().availableProcessors();

    private final CommandExecutor commandExecutor;

    private final int maxThreads;

    private final ExecutorService executorService;

    /**
     * Instantiates a new AsyncCommandExecutor.
     * @param commandExecutor the synchronous command executor
     * @param executorConfig the executor configuration
     */
    public AsyncCommandExecutor(CommandExecutor commandExecutor, DaemonExecutorConfig executorConfig) {
        if (commandExecutor == null) {
            throw new IllegalArgumentException("commandExecutor must not be null");
        }

        this.commandExecutor = commandExecutor;

        if (executorConfig != null) {
            this.maxThreads = executorConfig.getMaxThreads(DEFAULT_MAX_THREADS);
        } else {
            this.maxThreads = DEFAULT_MAX_THREADS;
        }

        BlockingQueue<Runnable> workQueue = new SynchronousQueue<>();
        this.executorService = new ThreadPoolExecutor(
                1,
                maxThreads,
                180L,
                TimeUnit.SECONDS,
                workQueue
        );
    }

    /**
     * Executes the specified command without a callback.
     * @param parameters the parameters for the command
     * @return {@code true} if the command was accepted for execution, {@code false} otherwise
     * @see #execute(CommandParameters, Callback)
     */
    public boolean execute(CommandParameters parameters) {
        return execute(parameters, null);
    }

    /**
     * Executes the specified command and triggers the callback on completion.
     * @param parameters the parameters for the command
     * @param callback the callback to be invoked on completion
     * @return {@code true} if the command was accepted for execution, {@code false} otherwise
     */
    public boolean execute(@NonNull CommandParameters parameters, Callback callback) {
        Command command = commandExecutor.reserveCommand(parameters);
        if (command == null) {
            return false;
        }

        String commandName = command.getDescriptor().getName();
        Runnable runnable = () -> {
            Thread currentThread = Thread.currentThread();
            String oldThreadName = currentThread.getName();
            try {
                // Set a descriptive thread name for easier debugging
                String threadName = "cmd-" + commandName;
                currentThread.setName(threadName);

                CommandResult commandResult = commandExecutor.executeNow(command, parameters);
                if (callback != null) {
                    try {
                        if (commandResult.isSuccess()) {
                            callback.success();
                        } else {
                            callback.failure();
                        }
                    } catch (Exception e) {
                        logger.error("Failed to execute callback", e);
                    }
                }
            } finally {
                commandExecutor.releaseCommand(command);
                currentThread.setName(oldThreadName);
            }
        };

        try {
            executorService.execute(runnable);
            return true;
        } catch (RejectedExecutionException e) {
            commandExecutor.releaseCommand(command);
            logger.error("Command '{}' rejected by the executor; no available threads", commandName, e);
            return false;
        }
    }

    /**
     * Shuts down the executor service and waits for running tasks to complete.
     */
    public void shutdown() {
        if (logger.isDebugEnabled()) {
            logger.debug("Shutting down the async command executor...");
        }

        executorService.shutdown();
        if (!executorService.isTerminated()) {
            while (true) {
                logger.info("Waiting for the async command executor to terminate...");
                if (executorService.isTerminated()) {
                    break;
                }
                try {
                    if (executorService.awaitTermination(3000L, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        }
    }

    /**
     * A callback interface for command execution completion.
     */
    public interface Callback {

        /**
         * Invoked when the command completes successfully.
         */
        void success();

        /**
         * Invoked when the command fails or is not executed.
         */
        void failure();

    }

}
