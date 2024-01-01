/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import java.io.File;
import java.util.Arrays;

/**
 * Start and Stop Aspectran Daemon using Procrun.
 *
 * <p>Created: 2017. 12. 11.</p>
 *
 * @since 5.1.0
 */
public class ProcrunDaemon {

    private static DefaultDaemon defaultDaemon;

    protected static DefaultDaemon getDefaultDaemon() {
        Assert.state(defaultDaemon != null,
            "No DefaultDaemon available");
        return defaultDaemon;
    }

    public static void start(String[] params) {
        Thread.currentThread().setName("procrun-start");
        start(params, 0L);
    }

    public static void stop(String[] args) {
        Thread.currentThread().setName("procrun-stop");
        stop();
    }

    protected static void start(String[] params, long waitTimeoutMillis) {
        if (defaultDaemon == null) {
            try {
                String basePath = AspectranConfig.determineBasePath(params);
                File aspectranConfigFile = AspectranConfig.determineAspectranConfigFile(params);

                defaultDaemon = new DefaultDaemon();
                defaultDaemon.prepare(basePath, aspectranConfigFile);
                defaultDaemon.start(waitTimeoutMillis);
            } catch (Exception e) {
                defaultDaemon = null;
                e.printStackTrace(System.err);
            }
        }
    }

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

    public static void main(String[] args) {
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
