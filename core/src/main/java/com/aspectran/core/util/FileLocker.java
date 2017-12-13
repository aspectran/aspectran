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
package com.aspectran.core.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

/**
 * Used to obtain a lock that can be used to prevent other Aspectran services
 * that use the same persistent store.
 *
 * @since 5.1.0
 */
public class FileLocker {

    private File lockFile;

    private RandomAccessFile file;

    private FileLock fileLock;

    /**
     * Creates an NIO FileLock on the specified file.
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
     * Creates an NIO FileLock on the specified file.
     *
     * @throws Exception if the lock could not be obtained for any reason
     */
    public boolean lock() throws Exception {
        try {
            this.file = new RandomAccessFile(lockFile,"rw");
            this.fileLock = this.file.getChannel().tryLock();
        } catch (IOException e) {
            throw new Exception("Exception occurred while trying to get a lock on file: " +
                    lockFile.getAbsolutePath(), e);
        }
        if (fileLock == null) {
            release();
            return false;
        }
        return true;
    }

    /**
     * Releases the lock.
     *
     * @throws Exception if the lock could not be released  for any reason
     */
    public void release() throws Exception {
        if (fileLock != null) {
            try {
                fileLock.release();
                fileLock =  null;
            } catch (Exception e) {
                throw new Exception("Unable to release locked file: " +
                        lockFile.getAbsolutePath(), e);
            }
        }
        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
                // ignore
            }
            file = null;
        }
        if (lockFile != null && lockFile.exists()) {
            lockFile.delete();
        }
        lockFile = null;
    }

}
