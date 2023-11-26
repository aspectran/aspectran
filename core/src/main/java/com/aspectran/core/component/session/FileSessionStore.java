/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.component.session;

import com.aspectran.core.util.MultiException;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * A file-based store of session data.
 */
public class FileSessionStore extends AbstractSessionStore {

    private static final Logger logger = LoggerFactory.getLogger(FileSessionStore.class);

    private final Map<String, String> sessionFileMap = new ConcurrentHashMap<>();

    private File storeDir;

    private boolean deleteUnrestorableFiles = true;

    public File getStoreDir() {
        return storeDir;
    }

    public void setStoreDir(File storeDir) {
        checkAlreadyInitialized();
        this.storeDir = storeDir;
    }

    public boolean isDeleteUnrestorableFiles() {
        return deleteUnrestorableFiles;
    }

    public void setDeleteUnrestorableFiles(boolean deleteUnrestorableFiles) {
        checkAlreadyInitialized();
        this.deleteUnrestorableFiles = deleteUnrestorableFiles;
    }

    @Override
    public SessionData load(String id) throws Exception {
        // load session info from its file
        String filename = sessionFileMap.get(id);
        if (filename == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Session id=" + id + " does not exist in session file map");
            }
            return null;
        }
        File file = new File(storeDir, filename);
        if (!file.exists()) {
            logger.warn("No such file " + filename + " for session id=" + id);
            return null;
        }
        try (FileInputStream in = new FileInputStream(file)) {
            SessionData data = SessionData.deserialize(in);
            data.setLastSaved(file.lastModified());
            return data;
        } catch (Exception e) {
            throw new UnreadableSessionDataException(id, e);
        }
    }

    @Override
    public boolean delete(String id) throws IOException {
        if (storeDir != null) {
            // remove from our map
            String filename = sessionFileMap.remove(id);
            if (filename == null) {
                return false;
            }
            // remove the file
            return deleteFile(filename);
        }
        return false;
    }

    /**
     * Delete the file associated with a session
     * @param filename name of the file containing the session's information
     * @return true if file was deleted, false otherwise
     * @throws IOException if the file associated with the session fails to be deleted
     */
    private boolean deleteFile(String filename) throws IOException {
        if (filename == null) {
            return false;
        }
        File file = new File(storeDir, filename);
        return Files.deleteIfExists(file.toPath());
    }

    @Override
    public boolean exists(String id) {
        String filename = sessionFileMap.get(id);
        if (filename == null) {
            return false;
        }
        // check the expiry
        long expiry = getExpiryFromFilename(filename);
        if (expiry == 0L) {
            return true; // never expires
        } else if (expiry == -1L) {
            return false; // not valid session filename
        } else {
            return (expiry > System.currentTimeMillis()); // hasn't yet expired
        }
    }

    @Override
    public void doSave(String id, SessionData data) throws Exception {
        if (storeDir != null) {
            try {
                delete(id);
            } catch (IOException e) {
                logger.warn("Failed to delete old data file for session " + id);
            }
            // make a fresh file using the latest session expiry
            String filename = getIdWithExpiry(data);
            File file = new File(storeDir, filename);
            try (FileOutputStream fos = new FileOutputStream(file,false)) {
                SessionData.serialize(data, fos, getNonPersistentAttributes());
                sessionFileMap.put(id, filename);
            } catch (Exception e) {
                file.delete(); // No point keeping the file if we didn't save the whole session
                throw new UnwritableSessionDataException(id, e);
            }
        }
    }

    /**
     * Check to see which sessions have expired.
     * @param time the upper limit of expiry times to check
     * @return the complete set of sessions that have expired, including those
     *      that are not currently loaded into the SessionCache
     */
    @Override
    public Set<String> doGetExpired(long time) {
        Set<String> expired = new HashSet<>();
        // iterate over the files and work out which have expired
        for (String filename : sessionFileMap.values()) {
            try {
                long expiry = getExpiryFromFilename(filename);
                if (expiry > 0 && expiry <= time) {
                    expired.add(getIdFromFilename(filename));
                }
            } catch (Exception e) {
                logger.warn("Error finding sessions expired before " + time, e);
            }
        }
        return expired;
    }

    @Override
    public void doCleanOrphans(long time) {
        sweepDisk(time, false);
    }

    @Override
    public Set<String> getAllSessions() {
        return new HashSet<>(sessionFileMap.keySet());
    }

    /**
     * Get the session id with its expiry time.
     * @param data the session data
     * @return the session id plus expiry
     */
    private String getIdWithExpiry(SessionData data) {
        if (data.getExpiry() > 0L) {
            return data.getExpiry() + "_" + data.getId();
        } else {
            return data.getId();
        }
    }

    private long getExpiryFromFilename(String filename) {
        int index = filename.indexOf('_');
        if (index == -1) {
            return 0L; // never expires
        } else {
            try {
                String s = filename.substring(0, index);
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                logger.warn("Not valid session filename: " + filename, e);
                return -1L;
            }
        }
    }

    /**
     * Extract the session id from the filename.
     * @param filename the name of the file to use
     * @return the session id
     */
    private String getIdFromFilename(String filename) {
        int index = filename.indexOf('_');
        if (index == -1) {
            return filename;
        } else {
            return filename.substring(index + 1);
        }
    }

    /**
     * Check if the filename matches our session pattern.
     * @param filename the name of the file to checks
     * @return true if pattern matches
     */
    private boolean isSessionFilename(String filename) {
        return (StringUtils.hasText(filename) && !filename.startsWith("."));
    }

    /**
     * Check all session files and remove any that expired at or before the time limit.
     * @param time the time in msec
     * @param withManaged whether to also include managed sessions
     */
    private void sweepDisk(long time, boolean withManaged) {
        // iterate over the files in the store dir and check expiry times
        if (logger.isTraceEnabled()) {
            logger.trace("Sweeping " + storeDir + " for old session files at " + time);
        }
        try (Stream<Path> stream = Files.walk(storeDir.toPath(), 1, FileVisitOption.FOLLOW_LINKS)) {
            stream
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> isSessionFilename(p.getFileName().toString()))
                .filter(p -> withManaged || !sessionFileMap.containsValue(p.getFileName().toString()))
                .forEach(p -> sweepFile(time, p));
        } catch (Exception e) {
            logger.warn("Unable to walk path " + storeDir, e);
        }
    }

    /**
     * Delete file that expired at or before the given time.
     * @param time the time now in msec
     * @param p the file to check
     */
    private void sweepFile(long time, Path p) {
        if (p != null) {
            String filename = p.getFileName().toString();
            long expiry = getExpiryFromFilename(filename);
            // files with 0 expiry never expire
            if (expiry > 0 && expiry <= time) {
                try {
                    if (Files.deleteIfExists(p)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Sweep expired session file: " + filename);
                        }
                    } else {
                        logger.warn("Could not delete " + filename);
                    }
                } catch (IOException e) {
                    logger.warn("Could not delete " + filename, e);
                }
            } else if (expiry == -1L && isDeleteUnrestorableFiles()) {
                try {
                    if (Files.deleteIfExists(p)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Deleted unrestorable session file: " + p.getFileName());
                        }
                    } else {
                        logger.warn("Could not delete " + filename);
                    }
                } catch (IOException e) {
                    logger.warn("Could not delete " + filename, e);
                }
            }
        }
    }

    @Override
    protected void doInitialize() throws Exception {
        initializeStore();
    }

    @Override
    protected void doDestroy() {
        sessionFileMap.clear();
    }

    private void initializeStore() throws Exception {
        if (storeDir == null) {
            throw new IllegalStateException("No file store directory specified");
        }

        if (!storeDir.exists()) {
            storeDir.mkdirs();
            return;
        }

        if (!(storeDir.isDirectory() && storeDir.canWrite() && storeDir.canRead())) {
            throw new IllegalStateException(storeDir.getAbsolutePath() + " must be readable/writable directory");
        }

        // iterate over files in storeDir and build map of session id to filename
        MultiException me = new MultiException();
        long now = System.currentTimeMillis();

        // build session file map by walking directory
        try (Stream<Path> stream = Files.walk(storeDir.toPath(), 1, FileVisitOption.FOLLOW_LINKS)) {
            stream
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> isSessionFilename(p.getFileName().toString()))
                .forEach(p -> {
                    // first get rid of all ancient files
                    sweepFile(now - TimeUnit.SECONDS.toMillis(getGracePeriodSecs() * 6L), p);

                    // now process it if it wasn't deleted
                    if (Files.exists(p)) {
                        String filename = p.getFileName().toString();
                        String sessionId = getIdFromFilename(filename);
                        // handle multiple session files existing for the same session: remove all
                        // but the file with the most recent expiry time
                        String existing = sessionFileMap.putIfAbsent(sessionId, filename);
                        if (existing != null) {
                            // if there was a prior filename, work out which has the most
                            // recent modify time
                            try {
                                long existingExpiry = getExpiryFromFilename(existing);
                                long thisExpiry = getExpiryFromFilename(filename);
                                if (thisExpiry > existingExpiry) {
                                    // replace with more recent file
                                    Path existingFile = storeDir.toPath().resolve(existing);
                                    // update the file we're keeping
                                    sessionFileMap.put(sessionId, filename);
                                    // delete the old file
                                    Files.delete(existingFile);
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Replaced " + existing + " with " + filename);
                                    }
                                } else {
                                    // we found an older file, delete it
                                    Files.delete(p);
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Deleted expired session file " + filename);
                                    }
                                }
                            } catch (IOException e) {
                                me.add(e);
                            }
                        }
                    }
                });
            me.ifExceptionThrow();
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("storeDir", storeDir);
        tsb.appendForce("deleteUnrestorableFiles", deleteUnrestorableFiles);
        tsb.append("gracePeriodSecs", getGracePeriodSecs());
        tsb.append("savePeriodSecs", getSavePeriodSecs());
        tsb.append("nonPersistentAttributes", getNonPersistentAttributes());
        return tsb.toString();
    }

}
