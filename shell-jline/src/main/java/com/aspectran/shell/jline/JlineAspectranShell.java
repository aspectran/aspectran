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
package com.aspectran.shell.jline;

import com.aspectran.shell.ConsoleCommand;
import com.aspectran.shell.jline.inout.JlineConsoleInout;
import com.aspectran.shell.service.ShellAspectranService;

/**
 * Main entry point for the Aspectran Shell using JLine.
 *
 * <p>Created: 2017. 10. 21.</p>
 *
 * @since 4.1.0
 */
public class JlineAspectranShell {

    private static final String DEFAULT_ASPECTRAN_CONFIG_FILE = "aspectran-config.apon";

    public static void main(String[] args) {
        String aspectranConfigFile;
        if (args.length > 0) {
            aspectranConfigFile = args[0];
        } else {
            aspectranConfigFile = DEFAULT_ASPECTRAN_CONFIG_FILE;
        }

        ShellAspectranService service = null;
        int exitStatus = 0;

        try {
            service = ShellAspectranService.create(aspectranConfigFile, new JlineConsoleInout());
            service.start();

            ConsoleCommand command = new ConsoleCommand(service);
            command.perform();
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
