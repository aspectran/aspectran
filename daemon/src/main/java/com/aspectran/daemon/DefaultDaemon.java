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

import java.io.File;

/**
 * <p>Created: 2017. 12. 11.</p>
 *
 * @since 5.1.0
 */
public class DefaultDaemon extends AbstractDaemon implements Runnable {

    public DefaultDaemon() {
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                // do work

                Thread.sleep(getPollingInterval());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

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
            Thread t = new Thread(defaultDaemon);
            t.setName("default");
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
            exitStatus = 1;
        } finally {
            defaultDaemon.destroy();
        }

        System.exit(exitStatus);
    }

}
