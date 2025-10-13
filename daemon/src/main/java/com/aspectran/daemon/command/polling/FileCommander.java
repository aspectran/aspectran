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
package com.aspectran.daemon.command.polling;

import com.aspectran.daemon.Daemon;
import com.aspectran.daemon.command.CommandExecutor;

/**
 * Defines a file-based command polling mechanism.
 * <p>
 * Implementations of this interface watch a filesystem location for command
 * request files, periodically polling for new items and delegating their
 * execution to a {@link CommandExecutor}. Implementations may also support
 * re-queuing of unfinished commands depending on the configuration.
 * </p>
 *
 * <p>Created: 2017. 12. 11.</p>
 */
public interface FileCommander {

    /**
     * Returns the daemon that owns this commander.
     * @return the daemon instance
     */
    Daemon getDaemon();

    /**
     * Returns the command executor used to run commands.
     * @return the command executor
     */
    CommandExecutor getCommandExecutor();

    /**
     * Moves any commands from the 'queued' directory back to the 'incoming'
     * directory to be re-processed.
     * This is typically called on startup to handle commands that were not
     * completed in the previous session.
     */
    void requeue();

    /**
     * Polls the 'incoming' directory for new command files and executes them.
     */
    void polling();

    /**
     * Returns the polling interval in milliseconds.
     * @return the polling interval
     */
    long getPollingInterval();

    /**
     * Sets the polling interval in milliseconds.
     * @param pollingInterval the new polling interval
     */
    void setPollingInterval(long pollingInterval);

    /**
     * Checks if commands can be re-queued.
     * @return {@code true} if re-queuing is enabled, {@code false} otherwise
     */
    boolean isRequeuable();

}
