/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Used to obtain a lock that can be used to prevent other Aspectran services
 * that use the same persistent store.
 *
 * @since 5.1.0
 */
public class FileLocker {

    private static final Logger logger = LoggerFactory.getLogger(FileLocker.class);

    private static final String DEFAULT_LOCK_FILENAME = ".lock";

    private File lockFile;

    private FileChannel fileChannel;

    private FileLock fileLock;

    /**
     * Instantiates a new FileLocker.
     * @param lockFile the file to lock
     */
    public FileLocker(File lockFile) {
        if (lockFile == null) {
            throw new IllegalArgumentException("lockFile must not be null");
        }
        this.lockFile = lockFile;
    }

    public FileLocker(String basePath) {
        this(basePath, DEFAULT_LOCK_FILENAME);
    }

    public FileLocker(String basePath, String filename) {
        this(new File(basePath, filename));
    }

    /**
     * Try to lock the file and return true if the locking succeeds.
     * @return true if the locking succeeds; false if the lock is already held
     * @throws Exception if the lock could not be obtained for any reason
     */
    public boolean lock() throws Exception {
        synchronized (this) {
            if (fileLock != null) {
                throw new Exception("Lock is already held");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Acquiring lock on " + lockFile.getAbsolutePath());
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
                return true;
            }
        }
    }

    /**
     * Releases the lock.
     * @throws Exception if the lock could not be released  for any reason
     */
    public void release() throws Exception {
        synchronized (this) {
            if (logger.isDebugEnabled()) {
                logger.debug("Releasing lock on " + lockFile.getAbsolutePath());
            }
            if (fileLock != null) {
                try {
                    if (fileLock.isValid()) {
                        fileLock.release();
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Lock already released: " + lockFile.getAbsolutePath());
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
                if (lockFile != null) {
                    if (lockFile.delete()) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Deleted lock file " + lockFile.getAbsolutePath());
                        }
                    } else if (lockFile.exists()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Could not delete lock file " + lockFile.getAbsolutePath());
                        }
                    }
                    lockFile = null;
                }
            }
        }
    }

}
