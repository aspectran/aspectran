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
package com.aspectran.core.component.session;

import com.aspectran.core.util.CustomObjectInputStream;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A file-based store of session data.
 */
public class FileSessionDataStore extends AbstractSessionDataStore {

    private final static Log log = LogFactory.getLog(FileSessionDataStore.class);

    private File storeDir;

    private boolean deleteUnrestorableFiles = false;

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
        File file;
        if (storeDir != null) {
            file = getFile(storeDir, id);
            if (file != null && file.exists() && file.getParentFile().equals(storeDir)) {
                return file.delete();
            }
        }
        return false;
    }

    @Override
    public Set<String> doGetExpired(final Set<String> candidates) {
        final long now = System.currentTimeMillis();
        Set<String> expired = new HashSet<>();
        Set<String> ids = new HashSet<>();

        // one pass to get all id
        storeDir.listFiles((dir, name) -> {
            if (dir != storeDir) {
                return false;
            }

            // dir may contain files that don't match our naming pattern
            if (!match(name)) {
                return false;
            }

            String id = getIdFromString(name);
            if (StringUtils.hasText(id)) {
                ids.add(id);
            }
            return true;
        });

        // got the list of all sessionids, remove all old files for each one
        for (String id : ids) {
            deleteOldFiles(storeDir, id);
        }

        // now find sessions that have expired
        storeDir.listFiles((dir, name) -> {
            if (dir != storeDir) {
                return false;
            }

            // dir may contain files that don't match our naming pattern
            if (!match(name)) {
                return false;
            }

            try {
                long expiry = getExpiryFromString(name);
                if (expiry > 0 && expiry < now) {
                    expired.add(getIdFromString(name));
                }
            } catch (Exception e) {
                // ignored
            }

            return false;
        });

        // check candidates that were not found to be expired, perhaps they no
        // longer exist and they should be expired
        for (String c : candidates) {
            if (!expired.contains(c)) {
                // check if the file exists
                File f = getFile(storeDir, c);
                if (f == null || !f.exists()) {
                    expired.add(c);
                }
            }
        }

        return expired;
    }

    @Override
    public SessionData load(String id) throws Exception {
        final AtomicReference<SessionData> reference = new AtomicReference<>();
        final AtomicReference<Exception> exception = new AtomicReference<>();
        Runnable r = () -> {
            // get rid of all but the newest file for a session
            File file = deleteOldFiles(storeDir, id);

            if (file == null || !file.exists()) {
                if (log.isDebugEnabled()) {
                    log.debug("No file: " + file);
                }
                return;
            }

            try (FileInputStream in = new FileInputStream(file)) {
                SessionData data = load(in, id);
                data.setLastSaved(file.lastModified());
                reference.set(data);
            } catch (UnreadableSessionDataException e) {
                if (isDeleteUnrestorableFiles() && file.exists() && file.getParentFile().equals(storeDir)) {
                    file.delete();
                    log.warn("Deleted unrestorable file for session " + id);
                }
                exception.set(e);
            } catch (Exception e) {
                exception.set(e);
            }
        };
        r.run();

        if (exception.get() != null) {
            throw exception.get();
        }

        return reference.get();
    }

    @Override
    public void doStore(String id, SessionData data, long lastSaveTime) throws Exception {
        if (storeDir != null) {
            // remove any existing files for the session
            deleteAllFiles(storeDir, id);

            // make a fresh file using the latest session expiry
            File file = new File(storeDir, getIdWithExpiry(data));

            try(FileOutputStream fos = new FileOutputStream(file,false)) {
                save(fos, id, data);
            } catch (Exception e) {
                e.printStackTrace();
                file.delete(); //  No point keeping the file if we didn't save the whole session
                throw new UnwriteableSessionDataException(id, e);
            }
        }
    }

    @Override
    public boolean isPassivating() {
        return true;
    }

    @Override
    public boolean exists(String id) throws Exception {
        File sessionFile = deleteOldFiles(storeDir, id);
        if (sessionFile == null || !sessionFile.exists()) {
            return false;
        }
        // check the expiry
        long expiry = getExpiryFromFile(sessionFile);
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

    /**
     * Get the expiry time of the session stored in the file.
     *
     * @param file the file from which to extract the expiry time
     * @return the expiry time
     */
    private long getExpiryFromFile(File file) {
        if (file == null) {
            return 0;
        }
        return getExpiryFromString(file.getName());
    }

    private long getExpiryFromString(String filename) {
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
    private String getIdFromString(String filename) {
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
    private boolean match(String filename) {
        if (!StringUtils.hasText(filename)) {
            return false;
        }

        String[] parts = filename.split("_");

        // Need at least 2 parts for a valid filename
        return (parts.length >= 2);
    }

    /**
     * Find a File for the session id.
     *
     * @param storeDir the session storage directory
     * @param id the session id
     * @return the file
     */
    private File getFile (final File storeDir, final String id) {
        File[] files = storeDir.listFiles ((dir, name) -> (dir == storeDir && name.contains(id)));
        if (files == null || files.length < 1) {
            return null;
        }
        return files[0];
    }

    /**
     * Remove all existing session files.
     *
     * @param storeDir where the session files are stored
     * @param id the session id
     */
    private void deleteAllFiles(final File storeDir, final String id) {
        File[] files = storeDir.listFiles ((dir, name) -> (dir == storeDir && (name.contains(id))));
        // no files for that id
        if (files == null || files.length < 1) {
            return;
        }

        // delete all files
        for (File f : files) {
            try {
                Files.deleteIfExists(f.toPath());
            } catch (Exception e) {
                log.warn("Unable to delete session file", e);
            }
        }
    }

    /**
     * Delete all but the most recent file for a given session id.
     *
     * @param storeDir the directory in which sessions are stored
     * @param id the id of the session
     * @return the most recent remaining file for the session, can be null
     */
    private File deleteOldFiles(final File storeDir, final String id) {
        File[] files = storeDir.listFiles ((dir, name) -> {
            if (dir != storeDir) {
                return false;
            }
            if (!match(name)) {
                return false;
            }
            return name.contains(id);
        });

        // no file for that session
        if (files == null || files.length == 0) {
            return null;
        }

        // delete all but the most recent file
        File newest = null;
        for (File f : files) {
            try {
                if (newest == null) {
                    // haven't looked at any files yet
                    newest = f;
                } else {
                    if (f.lastModified() > newest.lastModified()) {
                        // this file is more recent
                        Files.deleteIfExists(newest.toPath());
                        newest = f;
                    } else if (f.lastModified() < newest.lastModified()) {
                        // this file is older
                        Files.deleteIfExists(f.toPath());
                    } else {
                        // files have same last modified times, decide based on latest expiry time
                        long exp1 = getExpiryFromFile(newest);
                        long exp2 = getExpiryFromFile(f);
                        if (exp2 >= exp1) {
                            // this file has a later expiry date
                            Files.deleteIfExists(newest.toPath());
                            newest = f;
                        } else {
                            // this file has an earlier expiry date
                            Files.deleteIfExists(f.toPath());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Unable to delete old session file", e);
            }
        }
        return newest;
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
     * @throws Exception If the session data could not be read from the file
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
        initializeStore();
        super.doInitialize();
    }

    private void initializeStore () {
        if (storeDir == null) {
            throw new IllegalStateException("No file store specified");
        }
        if (!storeDir.exists()) {
            storeDir.mkdirs();
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