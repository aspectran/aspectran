/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

public abstract class AbstractCommand implements Command {

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

}
