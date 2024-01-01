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
package com.aspectran.daemon.command.polling;

import com.aspectran.core.context.config.DaemonConfig;
import com.aspectran.daemon.SimpleDaemon;
import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

class DefaultFileCommanderTest {

    @Test
    void testPolling() throws Exception {
        File root = ResourceUtils.getResourceAsFile(".");
        File incomingDir = new File(root, "cmd/incoming");

        DaemonConfig daemonConfig = new DaemonConfig();
        daemonConfig.addCommand("com.aspectran.daemon.command.builtins.PollingIntervalCommand");

        // The polling interval is not specified, so it is 5 seconds.
        SimpleDaemon daemon = new SimpleDaemon();
        daemon.prepare(root.getCanonicalPath(), daemonConfig);

        // Since the simple daemon has no activity, this command will fail.
        File pollingIntervalCommandFile = new File(incomingDir, "10-polling-interval.apon");
        try (Writer writer = new FileWriter(pollingIntervalCommandFile)) {
            writer.write("command: pollingInterval\n" +
                    "arguments: {\n" +
                    "    item: {\n" +
                    "        value: 4000\n" +
                    "        valueType: long\n" +
                    "    }\n" +
                    "}");
        }

        // The quit command is built in by default.
        File quitCommandFile = new File(incomingDir, "99-quit.apon");
        try (Writer writer = new FileWriter(quitCommandFile)) {
            writer.write("command: quit");
        }

        daemon.start(3000L);
        daemon.destroy();
    }

}
