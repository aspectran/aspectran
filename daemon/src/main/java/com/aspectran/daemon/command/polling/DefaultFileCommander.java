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

import com.aspectran.core.context.config.DaemonPollingConfig;
import com.aspectran.daemon.command.CommandExecutor;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.service.DaemonService;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.FilenameUtils;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.apon.AponReader;
import com.aspectran.utils.apon.AponWriterCloseable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The default {@link FileCommander} implementation that uses the local filesystem
 * as a command queue.
 * <p>
 * This commander manages a directory structure for processing command files:
 * <ul>
 *   <li>{@code /cmd/incoming}: New command files are placed here to be picked up by the poller.</li>
 *   <li>{@code /cmd/queued}: Command files are moved here while they are being processed.</li>
 *   <li>{@code /cmd/completed}: Successfully processed command files are moved here.</li>
 *   <li>{@code /cmd/failed}: Command files that failed during processing are moved here.</li>
 * </ul>
 * The polling cycle retrieves command files from the incoming directory, moves them
 * to the queued directory, executes them via the {@link CommandExecutor}, and then
 * moves them to either the completed or failed directory.
 * If re-queuing is enabled, commands in the queued directory can be moved back to
 * the incoming directory upon restart.
 * </p>
 *
 * <p>Created: 2017. 12. 11.</p>
 */
public class DefaultFileCommander extends AbstractFileCommander {

    protected final Logger logger = LoggerFactory.getLogger(DefaultFileCommander.class);

    private static final String COMMANDS_PATH = "cmd";

    private static final String QUEUED_PATH = "queued";

    private static final String COMPLETED_PATH = "completed";

    private static final String FAILED_PATH = "failed";

    private static final String DEFAULT_INCOMING_PATH = COMMANDS_PATH + "/incoming";

    private final Path incomingDir;

    private final Path queuedDir;

    private final Path completedDir;

    private final Path failedDir;

    /**
     * Instantiates a new DefaultFileCommander.
     * @param daemonService the daemon service that owns this commander
     * @param pollingConfig the polling configuration
     * @throws Exception if an error occurs while creating the directory structure
     */
    public DefaultFileCommander(DaemonService daemonService, DaemonPollingConfig pollingConfig) throws Exception {
        super(daemonService, pollingConfig);

        try {
            Path basePath = Paths.get(getDaemonService().getBasePath());
            Path cmdDir = basePath.resolve(COMMANDS_PATH);

            this.queuedDir = cmdDir.resolve(QUEUED_PATH);
            Files.createDirectories(this.queuedDir);

            this.completedDir = cmdDir.resolve(COMPLETED_PATH);
            Files.createDirectories(this.completedDir);

            this.failedDir = cmdDir.resolve(FAILED_PATH);
            Files.createDirectories(this.failedDir);

            String incomingPath = pollingConfig.getIncoming(DEFAULT_INCOMING_PATH);
            if (incomingPath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
                // Using url fully qualified paths
                this.incomingDir = Paths.get(URI.create(incomingPath));
            } else {
                // Resolve against basePath, not cmdDir, because incomingPath
                // from config may already contain the '/cmd/' prefix
                if (incomingPath.startsWith("/")) {
                    incomingPath = incomingPath.substring(1);
                }
                this.incomingDir = basePath.resolve(incomingPath);
            }
            Files.createDirectories(this.incomingDir);

            List<Path> incomingFiles = retrieveCommandFiles(this.incomingDir);
            if (incomingFiles != null) {
                for (Path file : incomingFiles) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Deleting old incoming command file: {}", file);
                    }
                    removeCommandFile(file);
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to create directory structure for file commander", e);
        }
    }

    /**
     * Returns the directory where incoming command files are placed.
     * @return the incoming command directory
     */
    public Path getIncomingDir() {
        return incomingDir;
    }

    /**
     * Returns the directory where command files are moved while being processed.
     * @return the queued command directory
     */
    public Path getQueuedDir() {
        return queuedDir;
    }

    /**
     * Returns the directory where successfully processed command files are moved.
     * @return the completed command directory
     */
    public Path getCompletedDir() {
        return completedDir;
    }

    /**
     * Returns the directory where failed command files are moved.
     * @return the failed command directory
     */
    public Path getFailedDir() {
        return failedDir;
    }

    @Override
    public void requeue() {
        if (!isRequeuable()) {
            List<Path> files = retrieveCommandFiles(queuedDir);
            if (files != null) {
                for (Path file : files) {
                    removeCommandFile(file);
                }
            }
            return;
        }

        processCommandFiles(queuedDir, -1, (file, parameters) -> {
            if (parameters.isRequeuable()) {
                try {
                    Path target = incomingDir.resolve(file.getFileName());
                    Path moved = moveUnique(file, target);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Re-queued command: {} to {}", moved.getFileName(), incomingDir);
                    }
                } catch (IOException e) {
                    logger.warn("Failed to re-queue command file: {}", file, e);
                    removeCommandFile(file);
                }
            } else {
                removeCommandFile(file);
            }
            return true;
        });
    }

    @Override
    public void polling() {
        int limit = getCommandExecutor().getAvailableThreads();
        processCommandFiles(incomingDir, limit, (file, parameters) -> {
            try {
                Path target = queuedDir.resolve(file.getFileName());
                Path queuedFile = moveUnique(file, target);
                String queuedFileName = queuedFile.getFileName().toString();
                if (logger.isDebugEnabled()) {
                    logger.debug("Queued command: {} to {}", queuedFileName, queuedDir);
                }

                if (!executeQueuedCommand(parameters, queuedFileName)) {
                    // Rollback if executor rejected the command
                    Files.move(queuedFile, file, StandardCopyOption.REPLACE_EXISTING);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Command execution rejected; rolled back to incoming: {}", file.getFileName());
                    }
                }
                return true;
            } catch (IOException e) {
                logger.error("Failed to process incoming command file: {}", file, e);
                return false;
            }
        });
    }

    private void processCommandFiles(Path dir, int limit, CommandFileProcessor processor) {
        List<Path> files = retrieveCommandFiles(dir);
        if (files != null) {
            int count = 0;
            for (Path file : files) {
                if (limit > 0 && count >= limit) {
                    break;
                }
                CommandParameters parameters = readCommandFile(file);
                if (parameters != null) {
                    if (processor.process(file, parameters)) {
                        count++;
                    }
                }
            }
        }
    }

    private Path moveUnique(Path source, Path target) throws IOException {
        while (true) {
            try {
                Path dest = FilenameUtils.generateUniqueFile(target);
                Files.move(source, dest);
                return dest;
            } catch (FileAlreadyExistsException e) {
                // ignore and try again
            }
        }
    }

    private Path createUnique(Path target) throws IOException {
        while (true) {
            try {
                Path file = FilenameUtils.generateUniqueFile(target);
                Files.createFile(file);
                return file;
            } catch (FileAlreadyExistsException e) {
                // ignore and try again
            }
        }
    }

    private boolean executeQueuedCommand(CommandParameters parameters, String fileName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing command: {}{}{}", fileName, System.lineSeparator(), parameters);
        }
        return getCommandExecutor().execute(parameters, new CommandExecutor.Callback() {
            @Override
            public void success() {
                removeCommandFile(queuedDir.resolve(fileName));
                writeCompletedCommand(parameters, makeFileName());
                if (logger.isTraceEnabled()) {
                    logger.trace("Completed command result: {}{}{}", fileName, System.lineSeparator(), parameters);
                }
            }

            @Override
            public void failure() {
                removeCommandFile(queuedDir.resolve(fileName));
                writeFailedCommand(parameters, makeFileName());
                if (logger.isTraceEnabled()) {
                    logger.trace("Failed command result: {}{}{}", fileName, System.lineSeparator(), parameters);
                }
            }

            @NonNull
            private String makeFileName() {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssSSS");
                String datetime = formatter.format(LocalDateTime.now());
                return datetime + "_" + fileName;
            }
        });
    }

    @Nullable
    private List<Path> retrieveCommandFiles(@NonNull Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.apon")) {
            List<Path> files = new ArrayList<>();
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    files.add(path);
                }
            }
            files.sort(Comparator.comparing(Path::getFileName));
            return (files.isEmpty() ? null : files);
        } catch (IOException e) {
            logger.warn("Failed to retrieve command files from directory: {}", dir, e);
            return null;
        }
    }

    @Nullable
    private CommandParameters readCommandFile(@NonNull Path file) {
        if (logger.isTraceEnabled()) {
            logger.trace("Reading command file: {}", file);
        }
        try {
            CommandParameters parameters = new CommandParameters();
            AponReader.read(file.toFile(), parameters);
            return parameters;
        } catch (Exception e) {
            logger.error("Failed to read command file: {}", file, e);
            handleMalformedFile(file, e);
            return null;
        }
    }

    private void handleMalformedFile(@NonNull Path file, Exception e) {
        try {
            String source = Files.readString(file);
            removeCommandFile(file);

            CommandParameters parameters = new CommandParameters();
            parameters.setResult("[FAILED] Malformed command file: " + file.getFileName());
            parameters.setError(ExceptionUtils.getStacktrace(e));
            parameters.setSource(source);

            writeCommandFile(failedDir, file.getFileName().toString(), parameters);
            if (logger.isWarnEnabled()) {
                logger.warn("Malformed command file handled and moved to failed directory: {}", file.getFileName());
            }
        } catch (IOException ioe) {
            logger.error("Failed to handle malformed command file: {}", file, ioe);
            removeCommandFile(file);
        }
    }

    private void writeCompletedCommand(CommandParameters parameters, String fileName) {
        String written = writeCommandFile(completedDir, fileName, parameters);
        if (logger.isDebugEnabled()) {
            logger.debug("Completed command: {} to {}", written, completedDir);
        }
    }

    private void writeFailedCommand(CommandParameters parameters, String fileName) {
        String written = writeCommandFile(failedDir, fileName, parameters);
        if (logger.isDebugEnabled()) {
            logger.debug("Failed command: {} to {}", written, failedDir);
        }
    }

    @Nullable
    private String writeCommandFile(@NonNull Path dir, String fileName, CommandParameters parameters) {
        try {
            Path target = dir.resolve(fileName);
            Path file = createUnique(target);
            if (logger.isTraceEnabled()) {
                logger.trace("Writing command file: {}", file);
            }
            try (AponWriterCloseable aponWriter = new AponWriterCloseable(file.toFile()).nullWritable(false)) {
                aponWriter.write(parameters);
            }
            return file.getFileName().toString();
        } catch (IOException e) {
            Path f = dir.resolve(fileName);
            logger.warn("Failed to write command file: {}", f, e);
            return null;
        }
    }

    private void removeCommandFile(Path file) {
        if (logger.isTraceEnabled()) {
            logger.trace("Deleting command file: {}", file);
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            logger.warn("Failed to delete command file: {}", file, e);
        }
    }

    @FunctionalInterface
    private interface CommandFileProcessor {
        boolean process(Path file, CommandParameters parameters);
    }

}
