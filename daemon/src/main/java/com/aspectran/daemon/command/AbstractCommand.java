/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

public abstract class AbstractCommand implements Command {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final CommandRegistry registry;

    private final boolean isolated;

    public AbstractCommand(CommandRegistry registry) {
        this(registry, false);
    }

    public AbstractCommand(CommandRegistry registry, boolean isolated) {
        this.registry = registry;
        this.isolated = isolated;
    }

    public CommandRegistry getCommandRegistry() {
        return registry;
    }

    @Override
    public boolean isIsolated() {
        return isolated;
    }

    public DaemonService getDaemonService() {
        DaemonService daemonService = registry.getDaemon().getDaemonService();
        if (daemonService == null || !daemonService.getServiceController().isActive()) {
            throw new IllegalStateException("SERVICE NOT AVAILABLE");
        }
        return daemonService;
    }

    public boolean isServiceAvailable() {
        return (registry.getDaemon().getDaemonService() != null &&
                registry.getDaemon().getDaemonService().getServiceController().isActive());
    }

    protected String debug(String message) {
        logger.debug(message);
        return message;
    }

    protected String info(String message) {
        logger.info(message);
        return message;
    }

    protected String warn(String message) {
        logger.warn(message);
        return message;
    }

    protected String error(String message) {
        logger.error(message);
        return message;
    }

    protected CommandResult success(String message) {
        return new CommandResult(true, message);
    }

    protected CommandResult failed(String message) {
        return new CommandResult(false, message);
    }

    protected CommandResult failed(String message, Throwable throwable) {
        logger.error(message, throwable);
        return new CommandResult(false, message + System.lineSeparator() +
                ExceptionUtils.getStacktrace(throwable));
    }

    protected CommandResult failed(Throwable throwable) {
        logger.error(throwable.getMessage(), throwable);
        return new CommandResult(false, ExceptionUtils.getStacktrace(throwable));
    }

}
