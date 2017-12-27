/*
 * Copyright (c) 2008-2017 The Aspectran Project
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

import com.aspectran.core.context.config.DaemonConfig;
import com.aspectran.daemon.Daemon;
import com.aspectran.daemon.service.DaemonService;

import java.io.File;

/**
 * <p>Created: 2017. 12. 11.</p>
 */
public class DaemonCommander {

    private final Daemon daemon;

    private long pollingInterval;

    private File inboundPath;

    private File queuedPath;

    private File completedPath;

    private File failedPath;

    private File trashPath;

    public DaemonCommander(Daemon daemon) {
        this.daemon = daemon;
    }

    public void init(DaemonConfig daemonConfig) {

    }

}
