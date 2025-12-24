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
import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.Arrays;

/**
 * Utilities for running the Aspectran daemon as a Windows service via Apache Commons Procrun.
 * <p>
 * Provides static entry points that Procrun can bind to (start/stop) and delegates to
 * an internal {@link DefaultDaemon} instance.
 * </p>
 *
 * <p>Created: 2017. 12. 11.</p>
 * @since 5.1.0
 */
public class ProcrunDaemon {

    private static DefaultDaemon defaultDaemon;

    /**
     * Returns the active {@link DefaultDaemon} instance managed by this helper.
     * @return the active DefaultDaemon
     * @throws IllegalStateException if no daemon has been started
     */
    protected static DefaultDaemon getDefaultDaemon() {
        Assert.state(defaultDaemon != null,
            "No DefaultDaemon available");
        return defaultDaemon;
    }

    /**
     * Procrun entry point for starting the daemon.
     * @param params command-line parameters passed by Procrun
     */
    public static void start(String[] params) {
        Thread.currentThread().setName("procrun-start");
        start(params, 0L);
    }

    /**
     * Procrun entry point for stopping the daemon.
     * @param args parameters passed by Procrun (ignored)
     */
    public static void stop(String[] args) {
        Thread.currentThread().setName("procrun-stop");
        stop();
    }

    /**
     * Internal start routine used by the public Procrun start entry point.
     * @param params command-line parameters from Procrun
     * @param waitTimeoutMillis milliseconds to wait before returning; 0 for non-blocking
     */
    protected static void start(String[] params, long waitTimeoutMillis) {
        if (defaultDaemon == null) {
            try {
                String basePath = AspectranConfig.determineBasePath(params);
                File aspectranConfigFile = AspectranConfig.determineAspectranConfigFile(params);

                defaultDaemon = new DefaultDaemon();
                defaultDaemon.prepare(basePath, aspectranConfigFile);
                defaultDaemon.start(waitTimeoutMillis);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                stop();
            }
        }
    }

    /**
     * Internal stop routine used by the public Procrun stop entry point.
     * Destroys the underlying {@link DefaultDaemon} if present.
     */
    protected static void stop() {
        if (defaultDaemon != null) {
            try {
                defaultDaemon.destroy();
                defaultDaemon = null;
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Command-line helper allowing manual start/stop when running outside the service wrapper.
     * <p>
     * Usage:
     * <ul>
     *   <li>start [options]</li>
     *   <li>stop</li>
     * </ul>
     * </p>
     * @param args the arguments where the first token is "start" or "stop"
     */
    public static void main(String @NonNull [] args) {
        if (args.length > 0) {
            if ("start".equals(args[0])) {
                String[] params = Arrays.copyOfRange(args, 1, args.length);
                start(params);
            } else if ("stop".equals(args[0])) {
                stop();
            }
        }
    }

}
