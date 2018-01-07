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
package com.aspectran.daemon;

import com.aspectran.daemon.service.DaemonService;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

import java.io.File;

/**
 * <p>Created: 2017. 12. 11.</p>
 *
 * @since 5.1.0
 */
public class JsvcDaemon implements Daemon {

    private DefaultDaemon defaultDaemon;

    private File aspectranConfigFile;

    @Override
    public void init(DaemonContext daemonContext) {
        String[] args = daemonContext.getArguments();
        File aspectranConfigFile;
        if (args.length > 0) {
            aspectranConfigFile = DaemonService.determineAspectranConfigFile(args[0]);
        } else {
            aspectranConfigFile = DaemonService.determineAspectranConfigFile(null);
        }
        this.aspectranConfigFile = aspectranConfigFile;
    }

    @Override
    public void start() throws Exception {
        if (defaultDaemon != null) {
            throw new IllegalStateException("Aspectran Daemon is already running");
        }

        defaultDaemon = new DefaultDaemon();
        defaultDaemon.init(aspectranConfigFile);
        defaultDaemon.run();
    }

    @Override
    public void stop() {
        if (defaultDaemon == null) {
            throw new IllegalStateException("Aspectran Daemon not running, will do nothing");
        }

        defaultDaemon.shutdown();
    }

    @Override
    public void destroy() {
        defaultDaemon = null;
    }

}
