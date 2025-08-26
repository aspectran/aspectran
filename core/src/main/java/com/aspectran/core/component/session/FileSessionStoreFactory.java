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

import com.aspectran.utils.SystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * A factory that creates and configures a {@link FileSessionStore}.
 *
 * <p>Created: 2019/12/06</p>
 */
public class FileSessionStoreFactory extends AbstractSessionStoreFactory {

    private String storeDir;

    private boolean deleteUnrestorableFiles = true;

    /**
     * Returns the directory where session data files are stored.
     *
     * @return the session data store directory
     */
    public String getStoreDir() {
        return storeDir;
    }

    /**
     * Sets the directory where session data files are stored.
     *
     * @param storeDir the session data store directory
     */
    public void setStoreDir(String storeDir) {
        this.storeDir = storeDir;
    }

    /**
     * Returns whether to delete session files that cannot be restored.
     *
     * @return true to delete, false otherwise
     */
    public boolean isDeleteUnrestorableFiles() {
        return deleteUnrestorableFiles;
    }

    /**
     * Sets whether to delete session files that cannot be restored.
     *
     * @param deleteUnrestorableFiles true to delete, false otherwise
     */
    public void setDeleteUnrestorableFiles(boolean deleteUnrestorableFiles) {
        this.deleteUnrestorableFiles = deleteUnrestorableFiles;
    }

    /**
     * Creates a new {@link FileSessionStore} instance.
     *
     * @return a new {@link FileSessionStore}
     * @throws IOException if the session store cannot be created
     */
    @Override
    public FileSessionStore createSessionStore() throws IOException {
        File storeDir;
        if (getStoreDir() != null) {
            if (getApplicationAdapter() != null) {
                storeDir = getApplicationAdapter().getRealPath(getStoreDir()).toFile();
            } else {
                storeDir = new File(getStoreDir());
            }
        } else {
            storeDir = new File(SystemUtils.getJavaIoTmpDir());
        }
        FileSessionStore sessionStore = new FileSessionStore(storeDir);
        sessionStore.setDeleteUnrestorableFiles(isDeleteUnrestorableFiles());
        sessionStore.setGracePeriodSecs(getGracePeriodSecs());
        sessionStore.setSavePeriodSecs(getSavePeriodSecs());
        if (getNonPersistentAttributes() != null && getNonPersistentAttributes().length > 0) {
            sessionStore.setNonPersistentAttributes(getNonPersistentAttributes());
        }
        return sessionStore;
    }

}
