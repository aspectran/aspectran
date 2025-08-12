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

import java.io.File;

/**
 * Entry point for running the Aspectran daemon in standalone mode.
 * <p>
 * Resolves the base path and configuration file from command-line arguments,
 * prepares an {@link DefaultDaemon} (via {@link AbstractDaemon#prepare(String, java.io.File)}),
 * starts it in blocking mode, and ensures resources are released on exit.
 * </p>
 *
 * <p>Created: 2017. 12. 11.</p>
 * @since 5.1.0
 */
public class DefaultDaemon extends AbstractDaemon {

    /**
     * Starts the daemon using the given command-line arguments.
     * <p>
     * Recognized arguments are parsed by {@link AspectranConfig#determineBasePath(String[])} and
     * {@link AspectranConfig#determineAspectranConfigFile(String[])} to locate the base path and
     * Aspectran configuration file.
     * </p>
     * @param args command-line arguments used to resolve base path and configuration file
     */
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
