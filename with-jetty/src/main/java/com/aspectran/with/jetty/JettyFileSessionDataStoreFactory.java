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
package com.aspectran.with.jetty;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.context.AspectranCheckedException;
import com.aspectran.core.util.StringUtils;
import org.eclipse.jetty.server.session.FileSessionDataStoreFactory;

import java.io.File;

/**
 * The Factory to create FileSessionDataStore.
 */
public class JettyFileSessionDataStoreFactory extends FileSessionDataStoreFactory
        implements InitializableBean, DisposableBean {

    private String storeDir;

    private boolean deleteOnExit;

    public JettyFileSessionDataStoreFactory() {
        super();
    }

    public void setStoreDir(String storeDir) {
        this.storeDir = storeDir;
    }

    public boolean isDeleteOnExit() {
        return deleteOnExit;
    }

    public void setDeleteOnExit(boolean deleteOnExit) {
        this.deleteOnExit = deleteOnExit;
    }

    private File resolveStoreDir() {
        File dir;
        if (StringUtils.hasLength(storeDir)) {
            dir = new File(storeDir);
        } else {
            File baseDir = new File(System.getProperty("java.io.tmpdir"));
            dir = new File(baseDir, "jetty-sessions");
        }
        dir.mkdirs();
        if (deleteOnExit) {
            dir.deleteOnExit();
        }
        return dir;
    }

    @Override
    public void initialize() throws Exception {
        try {
            setStoreDir(resolveStoreDir());
        } catch (Exception e) {
            throw new AspectranCheckedException("Failed to initialize JettyFileSessionDataStoreFactory", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (deleteOnExit) {
            try {
                File storeDir = getStoreDir();
                if (storeDir != null && storeDir.exists()) {
                    storeDir.listFiles(file -> {
                        file.deleteOnExit();
                        return false;
                    });
                }
            } catch (Exception e) {
                throw new AspectranCheckedException("Failed to destroy FileSessionHandler", e);
            }
        }
    }

}
