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
package com.aspectran.daemon.command.file;

import com.aspectran.daemon.Daemon;
import com.aspectran.daemon.command.CommandExecutor;

/**
 * The command file poller.
 *
 * <p>Created: 2017. 12. 11.</p>
 */
public interface CommandFilePoller {

    Daemon getDaemon();

    CommandExecutor getExecutor();

    void requeue();

    void polling();

    void stop();

    long getPollingInterval();

    void setPollingInterval(long pollingInterval);

    int getMaxThreads();

    boolean isRequeuable();

}
