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

import com.aspectran.core.util.SystemUtils;

import java.io.File;
import java.io.IOException;

public class FileSessionStoreFactory extends AbstractSessionStoreFactory {

    private String storeDir;

    private boolean deleteUnrestorableFiles;

    public void setStoreDir(String storeDir) {
        this.storeDir = storeDir;
    }

    public void setDeleteUnrestorableFiles(boolean deleteUnrestorableFiles) {
        this.deleteUnrestorableFiles = deleteUnrestorableFiles;
    }

    @Override
    public SessionStore getSessionStore() throws IOException {
        FileSessionStore sessionStore = new FileSessionStore();
        if (getNonPersistentAttributes() != null) {
            sessionStore.setNonPersistentAttributes(getNonPersistentAttributes());
        }
        File storeDirFile;
        if (storeDir != null) {
            if (getApplicationAdapter() != null) {
                storeDirFile = getApplicationAdapter().toRealPathAsFile(storeDir);
            } else {
                storeDirFile = new File(storeDir);
            }
        } else {
            storeDirFile = new File(SystemUtils.getJavaIoTmpDir());
        }
        sessionStore.setStoreDir(storeDirFile);
        if (deleteUnrestorableFiles) {
            sessionStore.setDeleteUnrestorableFiles(true);
        }
        return sessionStore;
    }

}
