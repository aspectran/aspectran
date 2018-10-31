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
package com.aspectran.shell;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.shell.command.ShellCommander;
import com.aspectran.shell.service.AspectranShellService;

import java.io.File;

/**
 * Main entry point for the Aspectran Shell.
 *
 * @since 2016. 1. 17.
 */
public class AspectranShell {

    public static void main(String[] args) {
        File aspectranConfigFile;
        if (args.length > 0) {
            aspectranConfigFile = AspectranConfig.determineAspectranConfigFile(args[0]);
        } else {
            aspectranConfigFile = AspectranConfig.determineAspectranConfigFile(null);
        }

        AspectranShellService service = null;
        int exitStatus = 0;

        try {
            service = AspectranShellService.create(aspectranConfigFile);
            service.start();

            ShellCommander commander = new ShellCommander(service);
            commander.perform();
        } catch (Exception e) {
            e.printStackTrace();
            exitStatus = 1;
        } finally {
            if (service != null) {
                service.stop();
            }
        }

        System.exit(exitStatus);
    }

}
