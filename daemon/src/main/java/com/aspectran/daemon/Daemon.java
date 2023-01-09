/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.file.CommandFilePoller;
import com.aspectran.daemon.service.DaemonService;

/**
 * The Interface Daemon.
 */
public interface Daemon {

    String getName();

    void setName(String name);

    String getBasePath();

    DaemonService getDaemonService();

    CommandFilePoller getCommandFilePoller();

    CommandRegistry getCommandRegistry();

    boolean isWait();

    boolean isActive();

    void stop();

    void destroy();

}
