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
package com.aspectran.core.component.session;

import com.aspectran.core.util.CustomObjectInputStream;
import com.aspectran.core.util.MultiException;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A file-based store of session data.
 */
public class FileSessionDataStore extends AbstractSessionDataStore {

    private final static Log log = LogFactory.getLog(FileSessionDataStore.class);

    private final Map<String, String> sessionFileMap = new ConcurrentHashMap<>();

    private File storeDir;

    private boolean deleteUnrestorableFiles;

    private long lastSweepTime;

    public File getStoreDir() {
        return storeDir;
    }

    public void setStoreDir(File storeDir) {
        checkInitialized();
        this.storeDir = storeDir;
    }

    public boolean isDeleteUnrestorableFiles() {
        return deleteUnrestorableFiles;
    }

    public void setDeleteUnrestorableFiles(boolean deleteUnrestorableFiles) {
        checkInitialized();
        this.deleteUnrestorableFiles = deleteUnrestorableFiles;
    }

    @Override
    public boolean delete(String id) throws Exception {
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

    /**
     * Check to see which sessions have expired.
     *
     * @param candidates the set of session ids that the SessionCache believes
     *      have expired
     * @return the complete set of sessions that have expired, including those
     *      that are not currently loaded into the SessionCache
     */
    @Override
    public Set<String> doGetExpired(final Set<String> candidates) {
        final long now = System.currentTimeMillis();
        HashSet<String> expired = new HashSet<>();

        // iterate over the files and work out which have expired
        for (String filename : sessionFileMap.values()) {
            try {
                long expiry = getExpiryFromFilename(filename);
                if (expiry > 0 && expiry < now) {
                    expired.add(getIdFromFilename(filename));
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }

        // check candidates that were not found to be expired, perhaps
        // because they no longer exist and they should be expired
        for (String c : candidates) {
            if (!expired.contains(c)) {
                // if it doesn't have a file then the session doesn't exist
                String filename = sessionFileMap.get(c);
                if (filename == null) {
                    expired.add(c);
                }
            }
        }

        // Infrequently iterate over all files in the store, and delete those
        // that expired a long time ago.
        // If the graceperiod is disabled, don't do the sweep!
        if (gracePeriodSec > 0 &&
                (lastSweepTime == 0L || ((now - lastSweepTime) >= (5 * TimeUnit.SECONDS.toMillis(gracePeriodSec))))) {
            lastSweepTime = now;
            sweepDisk();
        }
        return expired;
    }

    @Override
    public SessionData load(String id) throws Exception {
        // load session info from its file
        String filename = sessionFileMap.get(id);
        if (filename == null) {
            if (log.isDebugEnabled()) {
                log.debug("Unknown session " + id);
            }
            return null;
        }

        File file = new File(storeDir, filename);
        if (!file.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("No such file " + filename);
            }
            return null;
        }

        try (FileInputStream in = new FileInputStream(file)) {
            SessionData data = load(in, id);
            data.setLastSaved(file.lastModified());
            return data;
        } catch (UnreadableSessionDataException e) {
            if (isDeleteUnrestorableFiles() && file.exists() && file.getParentFile().equals(storeDir)) {
                try {
                    delete(id);
                    log.warn("Deleted unrestorable file for session " + id);
                } catch (Exception x) {
                    log.warn("Unable to delete unrestorable file " + filename + " for session " + id, x);
                }
            }
            throw e;
        }
    }

    @Override
    public void doStore(String id, SessionData data, long lastSaveTime) throws Exception {
        if (storeDir != null) {
            delete(id);

            // make a fresh file using the latest session expiry
            String filename = getIdWithExpiry(data);
            File file = new File(storeDir, filename);
            try(FileOutputStream fos = new FileOutputStream(file,false)) {
                save(fos, id, data);
                sessionFileMap.put(id, filename);
            } catch (Exception e) {
                file.delete(); // No point keeping the file if we didn't save the whole session
                throw new UnwritableSessionDataException(id, e);
            }
        }
    }

    @Override
    public boolean isPassivating() {
        return true;
    }

    @Override
    public boolean exists(String id) throws Exception {
        String filename = sessionFileMap.get(id);
        if (filename == null) {
            return false;
        }

        // check the expiry
        long expiry = getExpiryFromFilename(filename);
        if (expiry <= 0) {
            return true; // never expires
        } else {
            return (expiry > System.currentTimeMillis()); // hasn't yet expired
        }
    }

    /**
     * Get the session id with its expiry time.
     *
     * @param data the session data
     * @return the session id plus expiry
     */
    private String getIdWithExpiry(SessionData data) {
        return data.getId() + "_" + data.getExpiryTime();
    }

    private long getExpiryFromFilename(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains("_")) {
            throw new IllegalStateException("Invalid or missing filename");
        }
        String s = filename.substring(filename.lastIndexOf('_') + 1);
        return Long.parseLong(s);
    }

    /**
     * Extract the session id from the filename.
     *
     * @param filename the name of the file to use
     * @return the session id
     */
    private String getIdFromFilename(String filename) {
        if (!StringUtils.hasText(filename) || filename.indexOf('_') < 0) {
            return null;
        }
        return filename.substring(0, filename.lastIndexOf('_'));
    }

    /**
     * Check if the filename matches our session pattern.
     *
     * @param filename the name of the file to checks
     * @return true if pattern matches
     */
    private boolean isSessionFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return false;
        }
        String[] parts = filename.split("_");
        // Need at least 2 parts for a valid filename
        return (parts.length >= 2);
    }

    /**
     * Check all session files that do not belong to this context and
     * remove any that expired long ago (ie at least 5 gracePeriods ago).
     */
    public void sweepDisk() {
        //iterate over the files in the store dir and check expiry times
        long now = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Sweeping " + storeDir + " for old session files");
        }
        try {
            Files.walk(storeDir.toPath(), 1, FileVisitOption.FOLLOW_LINKS)
                    .filter(p -> !Files.isDirectory(p))
                    .filter(p -> isSessionFilename(p.getFileName().toString()))
                    .forEach(p -> {
                        try {
                            sweepFile(now, p);
                        } catch (Exception e) {
                            log.warn(e.getMessage(), e);
                        }

                    });
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
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
    public void sweepFile(long now, Path p) throws Exception {
        if (p == null) {
            return;
        }
        long expiry = getExpiryFromFilename(p.getFileName().toString());
        // files with 0 expiry never expire
        if (expiry > 0 && ((now - expiry) >= (5 * TimeUnit.SECONDS.toMillis(gracePeriodSec)))) {
            Files.deleteIfExists(p);
            if (log.isDebugEnabled()) {
                log.debug("Sweep deleted " + p.getFileName());
            }
        }
    }

    /**
     * Save the session data.
     *
     * @param os the output stream to save to
     * @param id identity of the session
     * @param data the info of the session
     * @throws IOException if an I/O error has occurred
     */
    private void save(OutputStream os, String id, SessionData data) throws IOException {
        DataOutputStream out = new DataOutputStream(os);
        out.writeUTF(id);
        out.writeLong(data.getCreationTime());
        out.writeLong(data.getAccessedTime());
        out.writeLong(data.getLastAccessedTime());
        out.writeLong(data.getExpiryTime());
        out.writeLong(data.getMaxInactiveInterval());

        List<String> keys = new ArrayList<>(data.getKeys());
        out.writeInt(keys.size());
        ObjectOutputStream oos = new ObjectOutputStream(out);
        for (String name : keys) {
            oos.writeUTF(name);
            oos.writeObject(data.getAttribute(name));
        }
    }

    /**
     * Load session data from an input stream that contains session data.
     *
     * @param is the input stream containing session data
     * @param expectedId the id we've been told to load
     * @return the session data
     * @throws Exception if the session data could not be read from the file
     */
    private SessionData load(InputStream is, String expectedId) throws Exception {
        try {
            DataInputStream di = new DataInputStream(is);
            String id = di.readUTF(); // the actual id from inside the file
            long created = di.readLong();
            long accessed = di.readLong();
            long lastAccessed = di.readLong();
            long expiry = di.readLong();
            long maxIdle = di.readLong();

            SessionData data = newSessionData(id, created, accessed, lastAccessed, maxIdle);
            data.setExpiryTime(expiry);
            data.setMaxInactiveInterval(maxIdle);

            //  Attributes
            restoreAttributes(di, di.readInt(), data);

            return data;
        } catch (Exception e) {
            throw new UnreadableSessionDataException(expectedId, e);
        }
    }

    /**
     * Load attributes from an input stream that contains session data.
     *
     * @param is the input stream containing session data
     * @param size number of attributes
     * @param data the data to restore to
     * @throws Exception if the input stream is invalid or fails to read
     */
    private void restoreAttributes(InputStream is, int size, SessionData data) throws Exception {
        if (size > 0) {
            // input stream should not be closed here
            Map<String, Object> attributes = new HashMap<>();
            ObjectInputStream ois =  new CustomObjectInputStream(is);
            for (int i = 0; i < size; i++) {
                String key = ois.readUTF();
                Object value = ois.readObject();
                attributes.put(key, value);
            }
            data.putAllAttributes(attributes);
        }
    }

    @Override
    protected void doInitialize() throws Exception {
        super.doInitialize();
        initializeStore();
    }

    @Override
    protected void doDestroy() {
        sessionFileMap.clear();
        lastSweepTime = 0L;
        super.doDestroy();
    }

    private void initializeStore() throws Exception {
        if (storeDir == null) {
            throw new IllegalStateException("No file store specified");
        }
        if (!storeDir.exists()) {
            storeDir.mkdirs();
        } else {
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
                            if (sessionId != null) {
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
                                            //replace with more recent file
                                            Path existingPath = storeDir.toPath().resolve(existing);
                                            //update the file we're keeping
                                            sessionFileMap.put(sessionId, filename);
                                            //delete the old file
                                            Files.delete(existingPath);
                                            if (log.isDebugEnabled()) {
                                                log.debug("Replaced " + existing + " with " + filename);
                                            }
                                        } else {
                                            // we found an older file, delete it
                                            Files.delete(p);
                                            if (log.isDebugEnabled()) {
                                                log.debug("Deleted expired session file " + filename);
                                            }
                                        }
                                    } catch (IOException e) {
                                        me.add(e);
                                    }
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
        tsb.append("deleteUnrestorableFiles", deleteUnrestorableFiles);
        return tsb.toString();
    }

}