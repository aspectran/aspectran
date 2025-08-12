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

import com.aspectran.core.context.config.AspectranConfig;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

import java.io.File;

/**
 * Adapter for running the Aspectran daemon under Apache Commons Daemon (Jsvc).
 * <p>
 * Delegates lifecycle events from the service wrapper to an internal {@link DefaultDaemon} instance.
 * This enables installing Aspectran as a Unix service via Jsvc with proper start/stop hooks.
 * </p>
 *
 * <p>Created: 2017. 12. 11.</p>
 * @since 5.1.0
 */
public class JsvcDaemon implements Daemon {

    private DefaultDaemon defaultDaemon;

    /**
     * Initializes the internal {@link DefaultDaemon} using arguments from the provided context.
     * <p>
     * Extracts the base path and Aspectran configuration file from {@link DaemonContext#getArguments()} and
     * invokes {@link DefaultDaemon#prepare(String, File)}.
     * </p>
     * @param daemonContext the daemon context supplying command-line arguments
     * @throws Exception if preparation fails
     */
    @Override
    public void init(DaemonContext daemonContext) throws Exception {
        if (defaultDaemon == null) {
            String[] args = daemonContext.getArguments();
            String basePath = AspectranConfig.determineBasePath(args);
            File aspectranConfigFile = AspectranConfig.determineAspectranConfigFile(args);

            try {
                defaultDaemon = new DefaultDaemon();
                defaultDaemon.prepare(basePath, aspectranConfigFile);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                destroy();
                throw e;
            }
        }
    }

    /**
     * Starts the daemon service.
     * @throws Exception if the daemon is already running or cannot start
     */
    @Override
    public void start() throws Exception {
        if (defaultDaemon != null) {
            if (defaultDaemon.isActive()) {
                throw new Exception("Aspectran daemon is already running");
            }
            defaultDaemon.start();
        }
    }

    /**
     * Stops the daemon service.
     * @throws Exception if the daemon is not running or cannot stop
     */
    @Override
    public void stop() throws Exception {
        if (defaultDaemon != null) {
            if (!defaultDaemon.isActive()) {
                throw new Exception("Aspectran daemon is not running, will do nothing");
            }
            defaultDaemon.stop();
        }
    }

    /**
     * Destroys the daemon service and releases resources.
     * Safe to call multiple times.
     */
    @Override
    public void destroy() {
        if (defaultDaemon != null) {
            defaultDaemon.destroy();
            defaultDaemon = null;
        }
    }

}
