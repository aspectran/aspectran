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
package com.aspectran.daemon.service;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.CoreService;
import com.aspectran.daemon.Daemon;
import com.aspectran.daemon.command.CommandExecutor;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.daemon.command.polling.FileCommander;

import java.util.Map;

/**
 * The main interface for the Aspectran Daemon service.
 * <p>This service provides a way to run Aspectran in a standalone, non-web environment,
 * allowing for the programmatic execution of translets. It extends {@link CoreService}
 * to provide access to the core application context and lifecycle management.
 *
 * <p>The primary method for interacting with this service is {@link #translate},
 * which triggers the execution of a specified translet.
 *
 * @since 5.1.0
 */
public interface DaemonService extends CoreService {

    /**
     * Returns the daemon that owns this service.
     * @return the daemon instance, or {@code null} if not associated with a daemon
     */
    Daemon getDaemon();

    /**
     * Sets the daemon that owns this service.
     * @param daemon the daemon instance
     */
    void setDaemon(Daemon daemon);

    /**
     * Creates and returns a new session adapter for the daemon environment.
     * This allows for session management in a non-web context.
     * @return a new {@link SessionAdapter}
     */
    SessionAdapter newSessionAdapter();

    /**
     * Executes the translet with the given name.
     * <p>The request method is determined by parsing the name; if the name is prefixed
     * with a method type (e.g., "POST /path/to/translet"), that method is used.
     * Otherwise, it defaults to {@link MethodType#GET}.
     * @param name the name of the translet to execute, optionally prefixed with a method
     * @param attributeMap a map of attributes to be passed to the activity
     * @param parameterMap a map of parameters to be passed to the activity
     * @return the result of the translet execution
     */
    Translet translate(String name, Map<String, Object> attributeMap, ParameterMap parameterMap);

    /**
     * Executes the translet with the given name and request method.
     * @param name the name of the translet to execute
     * @param method the request method to use
     * @param attributeMap a map of attributes to be passed to the activity
     * @param parameterMap a map of parameters to be passed to the activity
     * @return the result of the translet execution
     */
    Translet translate(String name, MethodType method, Map<String, Object> attributeMap, ParameterMap parameterMap);

    /**
     * Executes the command with the given parameters.
     * <p>
     * Unlike {@link #translate}, which handles business logic and may be
     * restricted when the service is paused, this method is intended for
     * administrative control and remains available regardless of the
     * service's current pause state.
     * </p>
     * @param parameters the parameters for the command
     * @return the result of the command execution
     */
    CommandResult execute(CommandParameters parameters);

    /**
     * Executes the command with the given APON string.
     * <p>
     * This is a convenience method that parses the APON string into
     * {@link CommandParameters} and then calls {@link #execute(CommandParameters)}.
     * </p>
     * @param apon the APON string representing the command and its parameters
     * @return the result of the command execution
     */
    CommandResult execute(String apon);

    /**
     * Returns the command executor used to run administrative commands.
     * @return the command executor
     */
    CommandExecutor getCommandExecutor();

    /**
     * Returns the file-based commander (polling) if configured.
     * @return the file-based commander
     */
    FileCommander getFileCommander();

    /**
     * Returns the registry of available commands.
     * @return the command registry
     */
    CommandRegistry getCommandRegistry();

}
