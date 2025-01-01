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
package com.aspectran.core.context.config;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

public class SessionFileStoreConfig extends AbstractParameters {

    private static final ParameterKey storeDir;
    private static final ParameterKey gracePeriodSeconds;
    private static final ParameterKey savePeriodSeconds;
    private static final ParameterKey deleteUnrestorableFiles;
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

    public SessionFileStoreConfig() {
        super(parameterKeys);
    }

    public String getStoreDir() {
        return getString(storeDir);
    }

    public SessionFileStoreConfig setStoreDir(String storeDir) {
        putValue(SessionFileStoreConfig.storeDir, storeDir);
        return this;
    }

    public int getGracePeriodSeconds() {
        return getInt(gracePeriodSeconds, 0);
    }

    public SessionFileStoreConfig setGracePeriodSeconds(int gracePeriodSeconds) {
        putValue(SessionFileStoreConfig.gracePeriodSeconds, gracePeriodSeconds);
        return this;
    }

    public boolean hasGracePeriodSeconds() {
        return hasValue(gracePeriodSeconds);
    }

    public int getSavePeriodSeconds() {
        return getInt(savePeriodSeconds, 0);
    }

    public SessionFileStoreConfig setSavePeriodSeconds(int savePeriodSeconds) {
        putValue(SessionFileStoreConfig.savePeriodSeconds, savePeriodSeconds);
        return this;
    }

    public boolean hasSavePeriodSeconds() {
        return hasValue(savePeriodSeconds);
    }

    public boolean isDeleteUnrestorableFiles() {
        return getBoolean(deleteUnrestorableFiles, false);
    }

    public SessionFileStoreConfig setDeleteUnrestorableFiles(boolean deleteUnrestorableFiles) {
        putValue(SessionFileStoreConfig.deleteUnrestorableFiles, deleteUnrestorableFiles);
        return this;
    }

    public boolean hasDeleteUnrestorableFiles() {
        return hasValue(deleteUnrestorableFiles);
    }

    public String[] getNonPersistentAttributes() {
        return getStringArray(nonPersistentAttributes);
    }

    public SessionFileStoreConfig setNonPersistentAttributes(String[] nonPersistentAttributes) {
        removeValue(SessionFileStoreConfig.nonPersistentAttributes);
        putValue(SessionFileStoreConfig.nonPersistentAttributes, nonPersistentAttributes);
        return this;
    }

    public SessionFileStoreConfig addNonPersistentAttributes(String nonPersistentAttribute) {
        putValue(SessionFileStoreConfig.nonPersistentAttributes, nonPersistentAttribute);
        return this;
    }

    public boolean hasNonPersistentAttributes() {
        return hasValue(nonPersistentAttributes);
    }

}
