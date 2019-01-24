/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.context.config.DaemonPollerConfig;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.daemon.SimpleDaemon;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

class FileCommandPollerTest {

    @Test
    void testPolling() throws Exception {
        File root = ResourceUtils.getResourceAsFile(".");
        File inboundDir = new File(root, "inbound");

        DaemonConfig daemonConfig = new DaemonConfig();
        DaemonPollerConfig pollerConfig = daemonConfig.touchDaemonPollerConfig();
        pollerConfig.putValue(DaemonPollerConfig.inbound, inboundDir.getCanonicalPath());

        SimpleDaemon daemon = new SimpleDaemon();
        daemon.init(daemonConfig);

        File commandFile = new File(inboundDir, "quit.apon");
        try (Writer writer = new FileWriter(commandFile)) {
            writer.write("command: quit");
        }

        daemon.start(5000L);
        daemon.destroy();
    }

}