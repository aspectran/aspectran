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
package com.aspectran.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * A utility to obtain a file-based lock, which can be used to prevent multiple
 * processes or services from accessing a shared resource concurrently.
 * <p>This is particularly useful in scenarios where multiple Aspectran instances might
 * interact with the same persistent store or directory structure.</p>
 *
 * @since 5.1.0
 */
public class FileLocker {

    private static final Logger logger = LoggerFactory.getLogger(FileLocker.class);

    private static final String DEFAULT_LOCK_FILENAME = ".lock";

    private final File lockFile;

    private FileChannel fileChannel;

    private FileLock fileLock;

    /**
     * Creates a new FileLocker for the specified lock file.
     * @param lockFile the file to use for locking
     */
    public FileLocker(File lockFile) {
        if (lockFile == null) {
            throw new IllegalArgumentException("lockFile must not be null");
        }
        this.lockFile = lockFile;
    }

    /**
     * Creates a new FileLocker with a default lock file name (".lock")
     * inside the specified base path.
     * @param basePath the directory path where the lock file will be created
     */
    public FileLocker(String basePath) {
        this(basePath, DEFAULT_LOCK_FILENAME);
    }

    /**
     * Creates a new FileLocker with a specified file name inside the
     * specified base path.
     * @param basePath the directory path where the lock file will be created
     * @param filename the name of the lock file
     */
    public FileLocker(String basePath, String filename) {
        this(new File(basePath, filename));
    }

    /**
     * Attempts to acquire a lock on the file and writes the current process ID (PID)
     * into the lock file.
     * <p>This method is non-blocking. If the lock is already held by another
     * process, it will return immediately.</p>
     * @return {@code true} if the lock was acquired successfully, {@code false} otherwise
     * @throws Exception if the lock is already held by this instance or if an I/O error occurs
     */
    public boolean lock() throws Exception {
        synchronized (this) {
            if (fileLock != null) {
                throw new Exception("Lock is already held");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Acquiring lock on {}", lockFile.getAbsolutePath());
            }
            try {
                fileChannel = new RandomAccessFile(lockFile, "rw").getChannel();
                fileLock = fileChannel.tryLock();
            } catch (IOException e) {
                throw new Exception("Exception occurred while trying to get a lock on file: " +
                        lockFile.getAbsolutePath(), e);
            }
            if (fileLock == null) {
                if (fileChannel != null) {
                    try {
                        fileChannel.close();
                    } catch (IOException ie) {
                        logger.warn(ie.getMessage(), ie);
                    }
                    fileChannel = null;
                }
                return false;
            } else {
                try {
                    long pid = getPid();
                    if (pid != -1L) {
                        fileChannel.truncate(0L);
                        ByteBuffer buffer = ByteBuffer.wrap(String.valueOf(pid).getBytes());
                        fileChannel.write(buffer);
                        fileChannel.force(true);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Successfully wrote PID " + pid + " to " + lockFile.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Unable to write PID to lock file " + lockFile.getAbsolutePath(), e);
                }
                return true;
            }
        }
    }

    /**
     * Releases the file lock.
     * <p>This method releases the lock, closes the file channel, and deletes the lock file.
     * The {@code FileLocker} instance can be used again to acquire a new lock.</p>
     * @throws Exception if an error occurs while releasing the lock
     */
    public void release() throws Exception {
        synchronized (this) {
            if (fileLock != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Releasing lock on {}", lockFile.getAbsolutePath());
                }
                try {
                    if (fileLock.isValid()) {
                        fileLock.release();
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Lock already released: {}", lockFile.getAbsolutePath());
                        }
                    }
                    fileLock = null;
                } catch (Exception e) {
                    throw new Exception("Unable to release locked file: " + lockFile.getAbsolutePath(), e);
                }
                if (fileChannel != null) {
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        logger.warn(e.getMessage(), e);
                    }
                    fileChannel = null;
                }
                if (lockFile.delete()) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Deleted lock file {}", lockFile.getAbsolutePath());
                    }
                } else if (lockFile.exists()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Could not delete lock file {}", lockFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    private long getPid() {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        int index = jvmName.indexOf('@');
        if (index != -1) {
            try {
                return Long.parseLong(jvmName.substring(0, index));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return -1L;
    }

}
