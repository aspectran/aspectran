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
        return registry.getDaemon().getService();
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

    protected String failed(String message) {
        log.error(message);
        return "[FAILED] " + message;
    }

    protected String failed(Throwable throwable) {
        log.error(throwable.getMessage(), throwable);
        return "[FAILED] " + ExceptionUtils.getStacktrace(throwable);
    }

}
