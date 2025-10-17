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
import com.aspectran.daemon.Daemon;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executes daemon commands asynchronously using a thread pool.
 * <p>
 * This class manages a {@link ThreadPoolExecutor} to run commands. It uses a
 * {@link SynchronousQueue}, which means that if all threads are busy, new tasks
 * are rejected immediately rather than being queued. It also enforces that
 * commands marked as "isolated" run without concurrency.
 * A {@link Callback} can be provided to receive notifications of success or
 * failure after a command completes.
 * </p>
 */
public class CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private static final int DEFAULT_MAX_THREADS = Runtime.getRuntime().availableProcessors();

    private final Daemon daemon;

    private final int maxThreads;

    private final ExecutorService executorService;

    private final AtomicInteger queueSize = new AtomicInteger();

    private final AtomicBoolean isolated = new AtomicBoolean();

    /**
     * Instantiates a new CommandExecutor.
     * @param daemon the daemon that owns this executor
     * @param executorConfig the executor configuration
     */
    public CommandExecutor(Daemon daemon, DaemonExecutorConfig executorConfig) {
        if (daemon == null) {
            throw new IllegalArgumentException("daemon must not be null");
        }

        this.daemon = daemon;

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
     * Returns the number of available threads in the pool.
     * @return the number of available threads
     */
    public int getAvailableThreads() {
        return (maxThreads - queueSize.get());
    }

    /**
     * Executes the specified command without a callback.
     * @param parameters the parameters for the command
     * @return {@code true} if the command was accepted for execution, {@code false} otherwise
     * @see #execute(CommandParameters, Callback)
     */
    public boolean execute(final CommandParameters parameters) {
        return execute(parameters, null);
    }

    /**
     * Executes the specified command and triggers the callback on completion.
     * <p>
     * The command will be rejected under the following conditions:
     * <ul>
     *   <li>If an isolated command is already running.</li>
     *   <li>If the command to be executed is isolated and other commands are already running.</li>
     *   <li>If the command name is not found in the registry.</li>
     *   <li>If the executor has no available threads and rejects the task.</li>
     * </ul>
     * @param parameters the parameters for the command
     * @param callback the callback to be invoked on completion
     * @return {@code true} if the command was accepted for execution, {@code false} otherwise
     */
    public boolean execute(@NonNull final CommandParameters parameters, final Callback callback) {
        final String commandName = parameters.getCommandName();

        if (isolated.get()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Command '{}' rejected because an isolated command is already running.", commandName);
            }
            return false;
        }

        Command command = daemon.getCommandRegistry().getCommand(commandName);
        if (command == null) {
            parameters.setResult("Command not found: " + commandName);
            if (callback != null) {
                try {
                    callback.failure();
                } catch (Exception e) {
                    logger.error("Failed to execute callback", e);
                }
            }
            return false;
        }

        if (command.isIsolated() && queueSize.get() > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Isolated command '{}' rejected because other commands are running.", commandName);
            }
            return false;
        }

        if (daemon.getDaemonService() != null) {
            // DefaultActivity will always be specified here
            parameters.setActivity(daemon.getDaemonService().getActivityContext().getAvailableActivity());
        }

        Runnable runnable = () -> {
            Thread currentThread = Thread.currentThread();
            String oldThreadName = currentThread.getName();
            try {
                // Set a descriptive thread name for easier debugging
                String threadName = "cmd-" + commandName + "-" + queueSize;
                currentThread.setName(threadName);

                if (command.isIsolated()) {
                    isolated.set(true);
                }

                boolean success = execute(command, parameters);
                if (callback != null) {
                    try {
                        if (success) {
                            callback.success();
                        } else {
                            callback.failure();
                        }
                    } catch (Exception e) {
                        logger.error("Failed to execute callback", e);
                    }
                }
            } finally {
                currentThread.setName(oldThreadName);
                isolated.compareAndSet(true, false);
                queueSize.decrementAndGet();
            }
        };

        queueSize.incrementAndGet();
        try {
            executorService.execute(runnable);
            return true;
        } catch (RejectedExecutionException e) {
            logger.error("Command '{}' rejected by the executor; no available threads", commandName, e);
            queueSize.decrementAndGet();
            return false;
        }
    }

    /**
     * Executes the command and captures the result.
     * @param command the command to execute
     * @param parameters the parameters for the command
     * @return {@code true} if the command executed successfully, {@code false} otherwise
     */
    private boolean execute(Command command, CommandParameters parameters) {
        try {
            CommandResult commandResult = command.execute(parameters);
            if (commandResult.isSuccess()) {
                parameters.setResult(commandResult.getResult());
                return true;
            } else {
                parameters.setResult("[FAILED] " + commandResult.getResult());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error executing daemon command {}", command, e);
            parameters.setResult("[FAILED] An unexpected error occurred while executing the command '" +
                    command.getDescriptor().getName() + "'." + System.lineSeparator() +
                    ExceptionUtils.getStacktrace(e));
            return false;
        }
    }

    /**
     * Shuts down the executor service and waits for running tasks to complete.
     */
    public void shutdown() {
        if (logger.isDebugEnabled()) {
            logger.debug("Shutting down the command executor...");
        }

        executorService.shutdown();
        if (!executorService.isTerminated()) {
            while (true) {
                logger.info("Waiting for the command executor to terminate...");
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
