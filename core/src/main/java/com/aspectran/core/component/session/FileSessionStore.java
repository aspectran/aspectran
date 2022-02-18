/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

/**
 * A file-based store of session data.
 */
public class FileSessionStore extends AbstractSessionStore {

    private static final Logger logger = LoggerFactory.getLogger(FileSessionStore.class);

    private final Map<String, String> sessionFilenames = new ConcurrentHashMap<>();

    private File storeDir;

    private boolean deleteUnrestorableFiles = true;

    private long lastSweepTime;

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
        String filename = sessionFilenames.get(id);
        if (filename == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Unknown session file: " + id);
            }
            return null;
        }
        File file = new File(storeDir, filename);
        if (!file.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("No such session file: " + filename);
            }
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
    public boolean delete(String id) throws Exception {
        if (storeDir != null) {
            // remove from our map
            String filename = sessionFilenames.remove(id);
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
     *
     * @param filename name of the file containing the session's information
     * @return true if file was deleted, false otherwise
     * @throws Exception if the file associated with the session fails to be deleted
     */
    private boolean deleteFile(String filename) throws Exception {
        if (filename == null) {
            return false;
        }
        File file = new File(storeDir, filename);
        return Files.deleteIfExists(file.toPath());
    }

    @Override
    public boolean exists(String id) {
        String filename = sessionFilenames.get(id);
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
            delete(id);
            // make a fresh file using the latest session expiry
            String filename = getIdWithExpiry(data);
            File file = new File(storeDir, filename);
            try (FileOutputStream fos = new FileOutputStream(file,false)) {
                SessionData.serialize(data, fos, getNonPersistentAttributes());
                sessionFilenames.put(id, filename);
            } catch (Exception e) {
                file.delete(); // No point keeping the file if we didn't save the whole session
                throw new UnwritableSessionDataException(id, e);
            }
        }
    }

    /**
     * Check to see which sessions have expired.
     *
     * @param candidates the set of session ids that the SessionCache believes
     *      have expired
     * @return the complete set of sessions that have expired, including those
     *      that are not currently loaded into the SessionCache
     */
    @Override
    public Set<String> doGetExpired(Set<String> candidates) {
        long now = System.currentTimeMillis();
        Set<String> expired = new HashSet<>();
        // iterate over the files and work out which have expired
        for (String filename : sessionFilenames.values()) {
            try {
                long expiry = getExpiryFromFilename(filename);
                if (expiry > 0 && expiry < now) {
                    expired.add(getIdFromFilename(filename));
                }
            } catch (Exception e) {
                logger.warn(e);
            }
        }
        // check candidates that were not found to be expired, perhaps
        // because they no longer exist and they should be expired
        for (String id : candidates) {
            if (!expired.contains(id)) {
                // if it doesn't have a file then the session doesn't exist
                if (!sessionFilenames.containsKey(id)) {
                    expired.add(id);
                }
            }
        }
        // Infrequently iterate over all files in the store, and delete those
        // that expired a long time ago.
        // If the grace period is disabled, don't do the sweep!
        if (getGracePeriodSecs() > 0 &&
                (lastSweepTime == 0L ||
                        ((now - lastSweepTime) >= (5 * TimeUnit.SECONDS.toMillis(getGracePeriodSecs()))))) {
            lastSweepTime = now;
            sweepDisk();
        }
        return expired;
    }

    /**
     * Get the session id with its expiry time.
     *
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
     *
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
     *
     * @param filename the name of the file to checks
     * @return true if pattern matches
     */
    private boolean isSessionFilename(String filename) {
        return (StringUtils.hasText(filename) && !filename.startsWith("."));
    }

    /**
     * Check all session files that do not belong to this context and
     * remove any that expired long ago (ie at least 5 gracePeriods ago).
     */
    private void sweepDisk() {
        //iterate over the files in the store dir and check expiry times
        long now = System.currentTimeMillis();
        if (logger.isTraceEnabled()) {
            logger.trace("Sweeping " + storeDir + " for old session files");
        }
        try {
            Files.walk(storeDir.toPath(), 1, FileVisitOption.FOLLOW_LINKS)
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> isSessionFilename(p.getFileName().toString()))
                    .forEach(p -> {
                        try {
                            sweepFile(now, p);
                        } catch (Exception e) {
                            logger.warn(e);
                        }
                    });
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    /**
     * Check to see if the expiry on the file is very old, and
     * delete the file if so. "Old" means that it expired at least
     * 5 gracePeriods ago.
     *
     * @param now the time now in msec
     * @param p the file to check
     * @throws Exception indicating error in sweep
     */
    private void sweepFile(long now, Path p) throws Exception {
        if (p != null) {
            String filename = p.getFileName().toString();
            long expiry = getExpiryFromFilename(filename); // files with 0 expiry never expire
            if (expiry > 0 && ((now - expiry) >= (5 * TimeUnit.SECONDS.toMillis(getGracePeriodSecs())))) {
                Files.deleteIfExists(p);
                if (logger.isDebugEnabled()) {
                    logger.debug("Sweep expired session file: " + p.getFileName());
                }
            } else if (expiry == -1L && isDeleteUnrestorableFiles()) {
                Files.deleteIfExists(p);
                if (logger.isDebugEnabled()) {
                    logger.debug("Deleted unrestorable session file: " + p.getFileName());
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
        sessionFilenames.clear();
        lastSweepTime = 0L;
    }

    private void initializeStore() throws Exception {
        if (storeDir == null) {
            throw new IllegalStateException("No file store specified");
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

        Files.walk(storeDir.toPath(), 1, FileVisitOption.FOLLOW_LINKS)
                .filter(p -> !Files.isDirectory(p))
                .filter(p -> isSessionFilename(p.getFileName().toString()))
                .forEach(p -> {
                    // first get rid of all ancient files, regardless of which context they are for
                    try {
                        sweepFile(now, p);
                    } catch (Exception x) {
                        me.add(x);
                    }

                    // now process it if it wasn't deleted
                    if (Files.exists(p)) {
                        String filename = p.getFileName().toString();
                        String sessionId = getIdFromFilename(filename);
                        // handle multiple session files existing for the same session: remove all
                        // but the file with the most recent expiry time
                        String existing = sessionFilenames.putIfAbsent(sessionId, filename);
                        if (existing != null) {
                            // if there was a prior filename, work out which has the most
                            // recent modify time
                            try {
                                long existingExpiry = getExpiryFromFilename(existing);
                                long thisExpiry = getExpiryFromFilename(filename);
                                if (thisExpiry > existingExpiry) {
                                    // replace with more recent file
                                    Path existingPath = storeDir.toPath().resolve(existing);
                                    // update the file we're keeping
                                    sessionFilenames.put(sessionId, filename);
                                    // delete the old file
                                    Files.delete(existingPath);
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

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("storeDir", storeDir);
        tsb.append("deleteUnrestorableFiles", deleteUnrestorableFiles);
        return tsb.toString();
    }

}
