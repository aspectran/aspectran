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
package com.aspectran.netty.server;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.InputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for HTTP idle timeout in {@link DefaultNettyServer}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NettyIdleTimeoutTest {

    private EmbeddedAspectran aspectran;

    private DefaultNettyServer nettyServer;

    private int port;

    @BeforeAll
    void ready() throws Exception {
        File configFile = ResourceUtils.getResourceAsFile("config/aspectran-config.apon");
        AspectranConfig aspectranConfig = new AspectranConfig(configFile);

        aspectran = EmbeddedAspectran.run(aspectranConfig);
        nettyServer = aspectran.getBean("netty.server");
        nettyServer.setIdleTimeout(1000); // 1 second idle timeout
        nettyServer.start();
        port = nettyServer.getActivePort();
        assertTrue(port > 0);
    }

    @AfterAll
    void finish() throws Exception {
        if (nettyServer != null && nettyServer.isRunning()) {
            nettyServer.stop();
        }
        if (aspectran != null) {
            aspectran.destroy();
        }
    }

    @Test
    void testIdleConnectionClosed() throws Exception {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            socket.setSoTimeout(4000);
            InputStream in = socket.getInputStream();
            // Read blocks until server closes the idle connection (returns -1)
            int readByte = in.read();
            assertEquals(-1, readByte, "Server should close connection upon idle timeout");
        }
    }

}
