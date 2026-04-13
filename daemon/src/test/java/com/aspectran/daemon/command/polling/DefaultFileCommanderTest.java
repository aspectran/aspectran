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
package com.aspectran.daemon.command.polling;

import com.aspectran.core.context.config.DaemonConfig;
import com.aspectran.core.context.config.DaemonExecutorConfig;
import com.aspectran.core.context.config.DaemonPollingConfig;
import com.aspectran.daemon.SimpleDaemon;
import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultFileCommanderTest {

    private SimpleDaemon daemon;
    private File root;
    private Path incomingDir;
    private Path queuedDir;
    private Path completedDir;
    private Path failedDir;

    @BeforeEach
    void setup() throws Exception {
        root = ResourceUtils.getResourceAsFile(".");
        Path cmdDir = root.toPath().resolve("cmd");
        incomingDir = cmdDir.resolve("incoming");
        queuedDir = cmdDir.resolve("queued");
        completedDir = cmdDir.resolve("completed");
        failedDir = cmdDir.resolve("failed");

        deleteDirectory(cmdDir);
        Files.createDirectories(incomingDir);
        Files.createDirectories(queuedDir);
        Files.createDirectories(completedDir);
        Files.createDirectories(failedDir);
    }

    @AfterEach
    void tearDown() {
        if (daemon != null) {
            daemon.destroy();
        }
    }

    @Test
    void testPollingSuccess() throws Exception {
        DaemonConfig daemonConfig = new DaemonConfig();
        daemonConfig.addCommand("com.aspectran.daemon.command.builtins.SysInfoCommand");
        DaemonPollingConfig pollingConfig = daemonConfig.touchPollingConfig();
        pollingConfig.setPollingInterval(100);
        pollingConfig.setEnabled(true);

        daemon = new SimpleDaemon();
        daemon.prepare(root.getCanonicalPath(), daemonConfig);
        daemon.start();

        // Write a valid command using single-line APON
        Path commandFile = incomingDir.resolve("01-success.apon");
        Files.writeString(commandFile, "command: sysinfo");

        // Wait for completion
        for (int i = 0; i < 20; i++) {
            if (countFiles(completedDir) > 0) break;
            Thread.sleep(100);
        }

        assertTrue(Files.notExists(commandFile), "Original file should be removed from incoming");
        assertEquals(1, countFiles(completedDir), "One file should be in completed directory");
    }

    @Test
    void testPollingMalformed() throws Exception {
        DaemonConfig daemonConfig = new DaemonConfig();
        DaemonPollingConfig pollingConfig = daemonConfig.touchPollingConfig();
        pollingConfig.setPollingInterval(100);
        pollingConfig.setEnabled(true);

        daemon = new SimpleDaemon();
        daemon.prepare(root.getCanonicalPath(), daemonConfig);
        daemon.start();

        // Write an invalid APON file
        Path commandFile = incomingDir.resolve("02-malformed.apon");
        Files.writeString(commandFile, "invalid_format_without_colon");

        // Wait for handling
        Thread.sleep(1000L);

        assertTrue(Files.notExists(commandFile), "Malformed file should be removed from incoming");
        // Integrated report should be the only file in failed directory
        assertEquals(1, countFiles(failedDir), "Only one file should be in failed directory");
    }

    @Test
    void testPollingRejectionAndRollback() throws Exception {
        DaemonConfig daemonConfig = new DaemonConfig();
        daemonConfig.addCommand("com.aspectran.daemon.command.builtins.QuitCommand");
        daemonConfig.addCommand("com.aspectran.daemon.command.builtins.SysInfoCommand");
        
        DaemonExecutorConfig executorConfig = daemonConfig.touchExecutorConfig();
        executorConfig.setMaxThreads(2);

        DaemonPollingConfig pollingConfig = daemonConfig.touchPollingConfig();
        pollingConfig.setPollingInterval(100);
        pollingConfig.setEnabled(true);

        daemon = new SimpleDaemon();
        daemon.prepare(root.getCanonicalPath(), daemonConfig);
        daemon.start();

        // 1. Submit a slow command
        Path slowCommandFile = incomingDir.resolve("03-slow.apon");
        Files.writeString(slowCommandFile, "command: sysinfo, arguments: { item: { value: gc } }");
        
        // Wait for it to be queued
        for (int i = 0; i < 20; i++) {
            if (Files.exists(queuedDir.resolve("03-slow.apon"))) break;
            Thread.sleep(100);
        }

        // 2. Submit an isolated command (quit). 
        // It should be rejected because sysinfo is already running.
        Path isolatedCommandFile = incomingDir.resolve("04-isolated.apon");
        Files.writeString(isolatedCommandFile, "command: quit");

        // Wait for polling rollback
        for (int i = 0; i < 20; i++) {
            if (Files.exists(isolatedCommandFile)) break;
            Thread.sleep(100);
        }

        // The isolated command should be rolled back to incoming
        assertTrue(Files.exists(isolatedCommandFile), "Isolated command should be rolled back to incoming");
        assertTrue(Files.notExists(queuedDir.resolve("04-isolated.apon")), "Isolated command should not be in queued");
    }

    @Test
    void testRequeue() throws Exception {
        DaemonConfig daemonConfig = new DaemonConfig();
        DaemonPollingConfig pollingConfig = daemonConfig.touchPollingConfig();
        pollingConfig.setPollingInterval(1000);
        pollingConfig.setRequeuable(true);
        pollingConfig.setEnabled(true);

        // Pre-place a file in the queued directory
        Path unfinishedFile = queuedDir.resolve("05-unfinished.apon");
        Files.writeString(unfinishedFile, "command: sysinfo requeuable: true");

        daemon = new SimpleDaemon();
        daemon.prepare(root.getCanonicalPath(), daemonConfig);
        // Manually trigger requeue for verification
        daemon.getFileCommander().requeue();

        assertTrue(Files.exists(incomingDir.resolve("05-unfinished.apon")), "Unfinished file should be moved back to incoming");
        assertTrue(Files.notExists(unfinishedFile), "Unfinished file should be removed from queued");
    }

    private int countFiles(Path dir) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return (int) files.count();
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            }
        }
    }

}
