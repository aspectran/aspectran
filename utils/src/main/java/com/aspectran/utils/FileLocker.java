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

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * A utility to obtain a file-based lock, which can be used to prevent multiple
 * processes or services from accessing a shared resource concurrently.
 * <p>This is particularly useful in scenarios where multiple Aspectran instances might
 * interact with the same persistent store or directory structure.</p>
 * <p>Implements {@link AutoCloseable} to support {@code try-with-resources} statements.</p>
 *
 * @since 5.1.0
 */
public class FileLocker implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(FileLocker.class);

    private static final String DEFAULT_LOCK_FILENAME = ".lock";

    private final File lockFile;

    private FileChannel fileChannel;

    private FileLock fileLock;

    private long lockedPid = -1L;

    /**
     * Creates a new FileLocker for the specified lock file.
     * @param lockFile the file to use for locking
     */
    public FileLocker(@NonNull File lockFile) {
        Assert.notNull(lockFile, "lockFile must not be null");
        this.lockFile = lockFile;
    }

    /**
     * Creates a new FileLocker with a default lock file name (".lock")
     * inside the specified base path.
     * @param basePath the directory path where the lock file will be created
     */
    public FileLocker(@NonNull String basePath) {
        this(basePath, DEFAULT_LOCK_FILENAME);
    }

    /**
     * Creates a new FileLocker with a specified file name inside the
     * specified base path.
     * @param basePath the directory path where the lock file will be created
     * @param filename the name of the lock file
     */
    public FileLocker(@NonNull String basePath, @NonNull String filename) {
        this(new File(basePath, filename));
    }

    /**
     * Returns the file used for locking.
     * @return the lock file
     */
    @NonNull
    public File getLockFile() {
        return lockFile;
    }

    /**
     * Checks whether this locker currently holds a valid lock.
     * @return {@code true} if a lock is held and valid, {@code false} otherwise
     */
    public synchronized boolean isLocked() {
        return (fileLock != null && fileLock.isValid());
    }

    /**
     * Attempts to acquire a lock on the file and writes the current process ID (PID)
     * into the lock file.
     * <p>This method is non-blocking. If the lock is already held by another
     * process, it will return immediately.</p>
     * @return {@code true} if the lock was acquired successfully, {@code false} otherwise
     * @throws IllegalStateException if the lock is already held by this instance
     * @throws IOException if an I/O error occurs while acquiring the lock
     */
    public synchronized boolean lock() throws IOException {
        if (fileLock != null) {
            throw new IllegalStateException("Lock is already held on " + lockFile.getAbsolutePath());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Acquiring lock on {}", lockFile.getAbsolutePath());
        }

        File parentDir = lockFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        FileChannel channel = null;
        FileLock lock;
        try {
            channel = new RandomAccessFile(lockFile, "rw").getChannel();
            lock = channel.tryLock();
        } catch (OverlappingFileLockException e) {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ie) {
                    logger.warn("Failed to close file channel: {}", ie.getMessage(), ie);
                }
            }
            return false;
        } catch (Exception e) {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ie) {
                    logger.warn("Failed to close file channel after exception: {}", ie.getMessage(), ie);
                }
            }
            if (e instanceof IOException ioe) {
                throw ioe;
            }
            throw new IOException("Failed to acquire lock on file: " + lockFile.getAbsolutePath(), e);
        }

        if (lock == null) {
            try {
                channel.close();
            } catch (IOException ie) {
                logger.warn("Failed to close file channel: {}", ie.getMessage(), ie);
            }
            return false;
        }

        this.fileChannel = channel;
        this.fileLock = lock;

        try {
            long pid = ProcessHandle.current().pid();
            fileChannel.truncate(0L);
            ByteBuffer buffer = ByteBuffer.wrap(String.valueOf(pid).getBytes(StandardCharsets.UTF_8));
            fileChannel.write(buffer);
            fileChannel.force(true);
            this.lockedPid = pid;
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully wrote PID {} to {}", pid, lockFile.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.warn("Unable to write PID to lock file: {}", lockFile.getAbsolutePath(), e);
        }
        return true;
    }

    /**
     * Returns the process ID (PID) recorded in this lock, or {@code -1} if no lock is held.
     * @return the locked PID, or {@code -1}
     */
    public synchronized long getLockedPid() {
        return (isLocked() ? lockedPid : -1L);
    }

    /**
     * Reads the process ID (PID) from the specified lock file.
     * @param lockFile the lock file
     * @return the process ID stored in the lock file, or {@code -1} if unreadable
     */
    public static long readPid(@NonNull File lockFile) {
        Assert.notNull(lockFile, "lockFile must not be null");
        if (!lockFile.exists() || !lockFile.isFile()) {
            return -1L;
        }
        try {
            String content = Files.readString(lockFile.toPath());
            if (!content.trim().isEmpty()) {
                return Long.parseLong(content.trim());
            }
        } catch (Exception ignored) {
        }
        return -1L;
    }

    /**
     * Releases the file lock.
     * <p>This method releases the lock, closes the file channel, and deletes the lock file.
     * The {@code FileLocker} instance can be used again to acquire a new lock.</p>
     * @throws IOException if an I/O error occurs while releasing the lock
     */
    public synchronized void release() throws IOException {
        lockedPid = -1L;
        if (fileLock != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Releasing lock on {}", lockFile.getAbsolutePath());
            }
            try {
                if (fileLock.isValid()) {
                    fileLock.release();
                } else if (logger.isDebugEnabled()) {
                    logger.debug("Lock already invalid/released: {}", lockFile.getAbsolutePath());
                }
            } finally {
                fileLock = null;
            }

            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    logger.warn("Failed to close file channel: {}", e.getMessage(), e);
                } finally {
                    fileChannel = null;
                }
            }

            if (lockFile.delete()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Deleted lock file {}", lockFile.getAbsolutePath());
                }
            } else if (lockFile.exists() && logger.isDebugEnabled()) {
                logger.debug("Could not delete lock file {}", lockFile.getAbsolutePath());
            }
        }
    }

    @Override
    public void close() throws IOException {
        release();
    }

}
