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
package com.aspectran.core.util;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

/**
 * Used to obtain a lock that can be used to prevent other Aspectran services
 * that use the same persistent store.
 *
 * @since 5.1.0
 */
public class FileLocker {

    private static final Log log = LogFactory.getLog(FileLocker.class);

    private File lockFile;

    private FileChannel fileChannel;

    private FileLock fileLock;

    /**
     * Instantiates a new FileLocker.
     *
     * @param lockFile the file to lock
     */
    public FileLocker(File lockFile) {
        if (lockFile == null) {
            throw new IllegalArgumentException("Argument 'lockFile' must not be null");
        }
        this.lockFile = lockFile;
    }

    /**
     * Try to lock the file and return true if the locking succeeds.
     *
     * @return true if the locking succeeds; false if the lock is already held
     * @throws Exception if the lock could not be obtained for any reason
     */
    public boolean lock() throws Exception {
        synchronized (this) {
            if (fileLock != null) {
                throw new Exception("The lock is already held");
            }
            if (log.isDebugEnabled()) {
                log.debug("Acquiring lock on " + lockFile.getAbsolutePath());
            }
            try {
                fileChannel = new RandomAccessFile(lockFile, "rw").getChannel();
                fileLock = fileChannel.tryLock();
            } catch (OverlappingFileLockException | IOException e) {
                throw new Exception("Exception occurred while trying to get a lock on file: " +
                        lockFile.getAbsolutePath(), e);
            }
            if (fileLock == null) {
                if (fileChannel != null) {
                    try {
                        fileChannel.close();
                    } catch (IOException ie) {
                        // ignore
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
     *
     * @throws Exception if the lock could not be released  for any reason
     */
    public void release() throws Exception {
        synchronized (this) {
            if (log.isDebugEnabled()) {
                log.debug("Releasing lock on " + lockFile.getAbsolutePath());
            }
            if (fileLock != null) {
                try {
                    fileLock.release();
                    fileLock = null;
                } catch (Exception e) {
                    throw new Exception("Unable to release locked file: " + lockFile.getAbsolutePath(), e);
                }
                if (fileChannel != null) {
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        // ignore
                    }
                    fileChannel = null;
                }
                if (lockFile != null) {
                    if (lockFile.exists()) {
                        lockFile.delete();
                    }
                    lockFile = null;
                }
            }
        }
    }

}
