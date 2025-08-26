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
package com.aspectran.core.component.session;

import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;

/**
 * A {@link com.aspectran.core.component.bean.ablility.FactoryBean} that creates and
 * configures a {@link FileSessionStore}.
 *
 * <p>Created: 2024. 12. 26.</p>
 *
 * @see FileSessionStore
 * @see FileSessionStoreFactory
 */
public class FileSessionStoreFactoryBean
        extends FileSessionStoreFactory implements InitializableFactoryBean<FileSessionStore> {

    private FileSessionStore fileSessionStore;

    /**
     * Initializes the factory by creating a {@link FileSessionStore} instance.
     * @throws Exception if an error occurs during initialization
     */
    @Override
    public void initialize() throws Exception {
        if (fileSessionStore == null) {
            fileSessionStore = createSessionStore();
        }
    }

    /**
     * Returns the singleton {@link FileSessionStore} instance.
     * @return the session store instance
     * @throws Exception if an error occurs
     */
    @Override
    public FileSessionStore getObject() throws Exception {
        return fileSessionStore;
    }

}
