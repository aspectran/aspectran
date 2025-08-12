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
package com.aspectran.daemon;

import com.aspectran.daemon.command.CommandExecutor;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.polling.FileCommander;
import com.aspectran.daemon.service.DaemonService;

/**
 * Contract for an Aspectran daemon process.
 * <p>
 * A daemon wraps an {@link DaemonService}, exposes command facilities, and
 * provides lifecycle control (start/stop/destroy). Implementations such as
 * {@link DefaultDaemon} are used directly, while wrappers like {@link JsvcDaemon}
 * and {@link ProcrunDaemon} integrate with platform-specific service runners.
 * </p>
 */
public interface Daemon {

    /** Returns the daemon name used for logging and identification. */
    String getName();

    /** Sets the daemon name used for logging and identification. */
    void setName(String name);

    /** Returns the base path used to resolve configuration and resources. */
    String getBasePath();

    /** Returns the underlying service driving this daemon. */
    DaemonService getDaemonService();

    /** Returns the command executor used to run administrative commands. */
    CommandExecutor getCommandExecutor();

    /** Returns the file-based commander (polling) if configured. */
    FileCommander getFileCommander();

    /** Returns the registry of available commands. */
    CommandRegistry getCommandRegistry();

    /** Whether the daemon is waiting for completion (blocking start). */
    boolean isWaiting();

    /** Whether the daemon is currently active (started and running). */
    boolean isActive();

    /** Requests a graceful stop of the daemon. */
    void stop();

    /** Destroys and releases all resources held by this daemon. */
    void destroy();

}
