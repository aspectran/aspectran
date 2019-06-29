/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ValueType;

public class SessionFileStoreConfig extends AbstractParameters {

    private static final ParameterDefinition path;
    private static final ParameterDefinition deleteUnrestorableFiles;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        path = new ParameterDefinition("path", ValueType.STRING);
        deleteUnrestorableFiles = new ParameterDefinition("deleteUnrestorableFiles", ValueType.BOOLEAN);

        parameterDefinitions = new ParameterDefinition[] {
                path,
                deleteUnrestorableFiles
        };
    }

    public SessionFileStoreConfig() {
        super(parameterDefinitions);
    }

    public String getPath() {
        return getString(path);
    }

    public SessionFileStoreConfig setPath(String path) {
        putValue(SessionFileStoreConfig.path, path);
        return this;
    }

    public boolean isDeleteUnrestorableFiles() {
        return getBoolean(deleteUnrestorableFiles, false);
    }

    public SessionFileStoreConfig setDeleteUnrestorableFiles(boolean deleteUnrestorableFiles) {
        putValue(SessionFileStoreConfig.deleteUnrestorableFiles, deleteUnrestorableFiles);
        return this;
    }

}
