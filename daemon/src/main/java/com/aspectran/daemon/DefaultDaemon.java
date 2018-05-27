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
package com.aspectran.daemon;

import com.aspectran.daemon.service.DaemonService;

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
        File aspectranConfigFile;
        if (args.length > 0) {
            aspectranConfigFile = DaemonService.determineAspectranConfigFile(args[0]);
        } else {
            aspectranConfigFile = DaemonService.determineAspectranConfigFile(null);
        }

        DefaultDaemon defaultDaemon = new DefaultDaemon();
        int exitStatus = 0;

        try {
            defaultDaemon.init(aspectranConfigFile);
            defaultDaemon.start(true);
        } catch (Exception e) {
            e.printStackTrace();
            exitStatus = 1;
        } finally {
            defaultDaemon.destroy();
        }

        System.exit(exitStatus);
    }

}
