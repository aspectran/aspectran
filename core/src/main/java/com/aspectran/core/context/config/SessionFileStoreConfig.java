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
package com.aspectran.core.context.config;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

/**
 * Configuration for a file-based session store.
 * <p>This class holds settings for the directory where session data is stored,
 * save intervals, and other persistence-related options.</p>
 */
public class SessionFileStoreConfig extends AbstractParameters {

    /** The directory where session data is stored. */
    private static final ParameterKey storeDir;

    /** The grace period in seconds before scavenging old session files. */
    private static final ParameterKey gracePeriodSeconds;

    /** The interval in seconds at which to save session data. */
    private static final ParameterKey savePeriodSeconds;

    /** Whether to delete session files that cannot be restored. */
    private static final ParameterKey deleteUnrestorableFiles;

    /** A list of session attributes that should not be persisted. */
    private static final ParameterKey nonPersistentAttributes;

    private static final ParameterKey[] parameterKeys;

    static {
        storeDir = new ParameterKey("storeDir", ValueType.STRING);
        gracePeriodSeconds = new ParameterKey("gracePeriodSeconds", ValueType.INT);
        savePeriodSeconds = new ParameterKey("savePeriodSeconds", ValueType.INT);
        deleteUnrestorableFiles = new ParameterKey("deleteUnrestorableFiles", ValueType.BOOLEAN);
        nonPersistentAttributes = new ParameterKey("nonPersistentAttributes", ValueType.STRING, true);

        parameterKeys = new ParameterKey[] {
                storeDir,
                gracePeriodSeconds,
                deleteUnrestorableFiles,
                nonPersistentAttributes
        };
    }

    /**
     * Instantiates a new SessionFileStoreConfig.
     */
    public SessionFileStoreConfig() {
        super(parameterKeys);
    }

    /**
     * Returns the directory where session data is stored.
     * @return the store directory path
     */
    public String getStoreDir() {
        return getString(storeDir);
    }

    /**
     * Sets the directory where session data is stored.
     * @param storeDir the store directory path
     * @return this {@code SessionFileStoreConfig} instance
     */
    public SessionFileStoreConfig setStoreDir(String storeDir) {
        putValue(SessionFileStoreConfig.storeDir, storeDir);
        return this;
    }

    /**
     * Returns the grace period in seconds before scavenging old session files.
     * @return the grace period in seconds
     */
    public int getGracePeriodSeconds() {
        return getInt(gracePeriodSeconds, 0);
    }

    /**
     * Sets the grace period in seconds before scavenging old session files.
     * @param gracePeriodSeconds the grace period in seconds
     * @return this {@code SessionFileStoreConfig} instance
     */
    public SessionFileStoreConfig setGracePeriodSeconds(int gracePeriodSeconds) {
        putValue(SessionFileStoreConfig.gracePeriodSeconds, gracePeriodSeconds);
        return this;
    }

    /**
     * Returns whether the grace period is set.
     * @return true if the grace period is set, false otherwise
     */
    public boolean hasGracePeriodSeconds() {
        return hasValue(gracePeriodSeconds);
    }

    /**
     * Returns the interval in seconds at which to save session data.
     * @return the save interval in seconds
     */
    public int getSavePeriodSeconds() {
        return getInt(savePeriodSeconds, 0);
    }

    /**
     * Sets the interval in seconds at which to save session data.
     * @param savePeriodSeconds the save interval in seconds
     * @return this {@code SessionFileStoreConfig} instance
     */
    public SessionFileStoreConfig setSavePeriodSeconds(int savePeriodSeconds) {
        putValue(SessionFileStoreConfig.savePeriodSeconds, savePeriodSeconds);
        return this;
    }

    /**
     * Returns whether the save period is set.
     * @return true if the save period is set, false otherwise
     */
    public boolean hasSavePeriodSeconds() {
        return hasValue(savePeriodSeconds);
    }

    /**
     * Returns whether to delete session files that cannot be restored.
     * @return true to delete unrestorable files, false otherwise
     */
    public boolean isDeleteUnrestorableFiles() {
        return getBoolean(deleteUnrestorableFiles, false);
    }

    /**
     * Sets whether to delete session files that cannot be restored.
     * @param deleteUnrestorableFiles true to delete unrestorable files, false otherwise
     * @return this {@code SessionFileStoreConfig} instance
     */
    public SessionFileStoreConfig setDeleteUnrestorableFiles(boolean deleteUnrestorableFiles) {
        putValue(SessionFileStoreConfig.deleteUnrestorableFiles, deleteUnrestorableFiles);
        return this;
    }

    /**
     * Returns whether the delete-unrestorable-files flag is set.
     * @return true if the flag is set, false otherwise
     */
    public boolean hasDeleteUnrestorableFiles() {
        return hasValue(deleteUnrestorableFiles);
    }

    /**
     * Returns the attributes that should not be persisted.
     * @return an array of non-persistent attribute names
     */
    public String[] getNonPersistentAttributes() {
        if (isAssigned(nonPersistentAttributes)) {
            return getStringArray(nonPersistentAttributes);
        } else {
            return null;
        }
    }

    /**
     * Sets the attributes that should not be persisted.
     * @param nonPersistentAttributes an array of non-persistent attribute names
     * @return this {@code SessionFileStoreConfig} instance
     */
    public SessionFileStoreConfig setNonPersistentAttributes(String[] nonPersistentAttributes) {
        removeValue(SessionFileStoreConfig.nonPersistentAttributes);
        putValue(SessionFileStoreConfig.nonPersistentAttributes, nonPersistentAttributes);
        return this;
    }

    /**
     * Adds an attribute that should not be persisted.
     * @param nonPersistentAttribute the non-persistent attribute name to add
     * @return this {@code SessionFileStoreConfig} instance
     */
    public SessionFileStoreConfig addNonPersistentAttributes(String nonPersistentAttribute) {
        putValue(SessionFileStoreConfig.nonPersistentAttributes, nonPersistentAttribute);
        return this;
    }

    /**
     * Returns whether any non-persistent attributes are defined.
     * @return true if non-persistent attributes are defined, false otherwise
     */
    public boolean hasNonPersistentAttributes() {
        return hasValue(nonPersistentAttributes);
    }

}
