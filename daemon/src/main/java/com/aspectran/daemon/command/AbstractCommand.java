/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.util.ExceptionUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.daemon.service.DaemonService;

public abstract class AbstractCommand implements Command {

    private final Log log = LogFactory.getLog(getClass());

    private final CommandRegistry registry;

    public AbstractCommand(CommandRegistry registry) {
        this.registry = registry;
    }

    public DaemonService getService() {
        DaemonService service = registry.getDaemon().getService();
        if (service == null || !service.getServiceController().isActive()) {
            throw new IllegalStateException("SERVICE NOT AVAILABLE");
        }
        return service;
    }

    public CommandRegistry getCommandRegistry() {
        return registry;
    }

    protected String info(String message) {
        log.info(message);
        return message;
    }

    protected String warn(String message) {
        log.warn(message);
        return message;
    }

    protected CommandResult success(String message) {
        return success(message, false);
    }

    protected CommandResult success(String message, boolean noLogging) {
        if (!noLogging) {
            log.info(message);
        }
        return new CommandResult(true, message);
    }

    protected CommandResult failed(String message) {
        return failed(message, false);
    }

    protected CommandResult failed(String message, boolean noLogging) {
        if (!noLogging) {
            log.error(message);
        }
        return new CommandResult(false, message);
    }

    protected CommandResult failed(Throwable throwable) {
        log.error(throwable.getMessage(), throwable);
        return new CommandResult(false, ExceptionUtils.getStacktrace(throwable));
    }

}
