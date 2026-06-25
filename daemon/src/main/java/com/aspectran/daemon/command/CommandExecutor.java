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

import com.aspectran.daemon.service.DaemonService;
import com.aspectran.utils.ExceptionUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executes daemon commands synchronously.
 * <p>This class handles the core logic for executing commands, including
 * registry lookup, isolated execution state management, and result capturing.
 * It serves as the primary execution engine for both direct synchronous calls
 * and asynchronous tasks managed by {@link AsyncCommandExecutor}.</p>
 *
 * <p>Created: 2026. 04. 23.</p>
 */
public class CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private final DaemonService daemonService;

    private final AtomicInteger activeCommands = new AtomicInteger();

    private final AtomicBoolean isolatedRunning = new AtomicBoolean();

    /**
     * Instantiates a new CommandExecutor.
     * @param daemonService the daemon service that owns this executor
     */
    public CommandExecutor(DaemonService daemonService) {
        if (daemonService == null) {
            throw new IllegalArgumentException("daemonService must not be null");
        }
        this.daemonService = daemonService;
    }

    /**
     * Retrieves the DaemonService instance associated with this CommandExecutor.
     * @return the DaemonService instance used by this CommandExecutor
     */
    public DaemonService getDaemonService() {
        return daemonService;
    }

    /**
     * Checks whether the executor is currently running in an isolated mode.
     * Isolation mode ensures that certain commands are executed without
     * interference from others to maintain operation integrity.
     * @return true if the executor is running in isolated mode; false otherwise
     */
    public boolean isIsolatedRunning() {
        return isolatedRunning.get();
    }

    /**
     * Retrieves the current count of active commands being executed.
     * @return the number of commands currently marked as active
     */
    public int getActiveCommandCount() {
        return activeCommands.get();
    }

    /**
     * Checks if the specified command is available for execution based on the
     * current state of the executor and the command's isolation requirements.
     * @param parameters the parameters for the command
     * @return the command instance if available, {@code null} otherwise
     */
    public synchronized Command getAvailableCommand(@NonNull CommandParameters parameters) {
        String commandName = parameters.getCommandName();

        if (isolatedRunning.get()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Command '{}' rejected because an isolated command is already running.", commandName);
            }
            return null;
        }

        Command command = daemonService.getCommandRegistry().getCommand(commandName);
        if (command == null) {
            String message = "Command not found: " + commandName;
            parameters.setResult(message);
            return null;
        }

        if (command.isIsolated() && activeCommands.get() > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Isolated command '{}' rejected because other commands are running.", commandName);
            }
            return null;
        }

        return command;
    }

    /**
     * Checks availability and reserves the command for execution by incrementing
     * the active command count and setting the isolation flag if necessary.
     * @param parameters the parameters for the command
     * @return the command instance if reserved, {@code null} otherwise
     */
    public synchronized Command reserveCommand(@NonNull CommandParameters parameters) {
        Command command = getAvailableCommand(parameters);
        if (command != null) {
            activeCommands.incrementAndGet();
            if (command.isIsolated()) {
                isolatedRunning.set(true);
            }
            return command;
        }
        return null;
    }

    /**
     * Releases the reserved command after execution by decrementing the
     * active command count and clearing the isolation flag if necessary.
     * @param command the command to release
     */
    public void releaseCommand(@NonNull Command command) {
        if (command.isIsolated()) {
            isolatedRunning.compareAndSet(true, false);
        }
        activeCommands.decrementAndGet();
    }

    /**
     * Executes the specified command synchronously and returns the result.
     * <p>
     * This method first validates the command's availability using
     * {@link #getAvailableCommand(CommandParameters)}. If the command is rejected,
     * a failed {@link CommandResult} is returned immediately.
     * </p>
     * @param parameters the parameters for the command
     * @return the result of the command execution
     */
    public CommandResult execute(@NonNull CommandParameters parameters) {
        Command command = reserveCommand(parameters);
        if (command == null) {
            return new CommandResult(false, parameters.getResult());
        }

        try {
            return executeNow(command, parameters);
        } finally {
            releaseCommand(command);
        }
    }

    /**
     * Performs the actual execution logic for a command.
     * <p>
     * This method assumes the command has already been reserved. It prepares
     * the activity context and invokes the command's execution logic.
     * </p>
     * @param command the command to execute
     * @param parameters the parameters for the command
     * @return the result of the command execution
     */
    @NonNull
    protected CommandResult executeNow(Command command, @NonNull CommandParameters parameters) {
        // DefaultActivity will always be specified here
        parameters.setActivity(daemonService.getActivityContext().getAvailableActivity());

        try {
            CommandResult commandResult = command.execute(parameters);
            if (commandResult.isSuccess()) {
                parameters.setResult(commandResult.getResult());
            } else {
                parameters.setResult(commandResult.getResult());
                parameters.setError(commandResult.getError());
            }
            return commandResult;
        } catch (Exception e) {
            logger.error("Error executing daemon command {}", command, e);
            String message = "[FAILED] An unexpected error occurred while executing the command '" +
                    command.getDescriptor().getName() + "'.";
            String error = ExceptionUtils.getStacktrace(e);
            parameters.setResult(message);
            parameters.setError(error);
            return new CommandResult(false, message, error);
        }
    }

}
