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
package com.aspectran.core.context.config;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;

public class SessionFileStoreConfig extends AbstractParameters {

    private static final ParameterKey storeDir;
    private static final ParameterKey deleteUnrestorableFiles;
    private static final ParameterKey nonPersistentAttributes;

    private static final ParameterKey[] parameterKeys;

    static {
        storeDir = new ParameterKey("storeDir", ValueType.STRING);
        deleteUnrestorableFiles = new ParameterKey("deleteUnrestorableFiles", ValueType.BOOLEAN);
        nonPersistentAttributes = new ParameterKey("nonPersistentAttributes", ValueType.STRING, true);

        parameterKeys = new ParameterKey[] {
                storeDir,
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

    public boolean isDeleteUnrestorableFiles() {
        return getBoolean(deleteUnrestorableFiles, false);
    }

    public SessionFileStoreConfig setDeleteUnrestorableFiles(boolean deleteUnrestorableFiles) {
        putValue(SessionFileStoreConfig.deleteUnrestorableFiles, deleteUnrestorableFiles);
        return this;
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
