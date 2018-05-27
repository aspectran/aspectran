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

    public static void start(String[] args) {
        if (defaultDaemon == null) {
            try {
                File aspectranConfigFile;
                if (args != null && args.length > 0) {
                    aspectranConfigFile = DaemonService.determineAspectranConfigFile(args[0]);
                } else {
                    aspectranConfigFile = DaemonService.determineAspectranConfigFile(null);
                }

                defaultDaemon = new DefaultDaemon();
                defaultDaemon.init(aspectranConfigFile);
                defaultDaemon.start();
            } catch (Exception e) {
                defaultDaemon = null;
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static void stop(String[] args) {
        if (defaultDaemon != null) {
            try {
                defaultDaemon.destroy();
                defaultDaemon = null;
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            if ("start".equals(args[0])) {
                String[] args2 = Arrays.copyOfRange(args, 1, args.length);
                start(args2);
            } else if ("stop".equals(args[0])) {
                stop(null);
            }
        }
    }

}
