/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import java.io.File;

/**
 * Start and Stop Aspectran Daemon in standalone mode.
 *
 * <p>Created: 2017. 12. 11.</p>
 *
 * @since 5.1.0
 */
public class DefaultDaemon extends AbstractDaemon {

    public static void main(String[] args) {
        String basePath = AspectranConfig.determineBasePath(args);
        File aspectranConfigFile = AspectranConfig.determineAspectranConfigFile(args);
        DefaultDaemon defaultDaemon = new DefaultDaemon();
        int exitStatus = 0;

        try {
            defaultDaemon.prepare(basePath, aspectranConfigFile);
            defaultDaemon.start(true);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            exitStatus = 1;
        } finally {
            defaultDaemon.destroy();
        }

        System.exit(exitStatus);
    }

}
