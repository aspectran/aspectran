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
 * Abstraction for a file-based command polling mechanism.
 * <p>
 * Implementations watch a filesystem location for serialized command requests,
 * periodically polling for new items and delegating their execution to a
 * {@link CommandExecutor}. Implementations may support re-queuing unfinished
 * commands depending on configuration.
 * </p>
 *
 * <p>Created: 2017. 12. 11.</p>
 */
public interface FileCommander {

    Daemon getDaemon();

    CommandExecutor getCommandExecutor();

    void requeue();

    void polling();

    long getPollingInterval();

    void setPollingInterval(long pollingInterval);

    boolean isRequeuable();

}
