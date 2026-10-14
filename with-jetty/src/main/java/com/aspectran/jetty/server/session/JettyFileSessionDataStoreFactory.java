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
package com.aspectran.jetty.server.session;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.utils.Assert;
import org.eclipse.jetty.session.FileSessionDataStoreFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link FileSessionDataStoreFactory} that is aware of the Aspectran
 * {@link ApplicationAdapter}.
 *
 * <p>Created: 2025-01-22</p>
 */
public class JettyFileSessionDataStoreFactory extends FileSessionDataStoreFactory implements ApplicationAdapterAware {

    private ApplicationAdapter applicationAdapter;

    /**
     * Retrieves the {@link ApplicationAdapter} that is aware of the Aspectran.
     * @return the {@link ApplicationAdapter}
     */
    public ApplicationAdapter getApplicationAdapter() {
        Assert.state(applicationAdapter != null, "ApplicationAdapter is not set");
        return applicationAdapter;
    }

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    /**
     * Sets the directory path where session data will be stored.
     * This method resolves the given directory path to a real file system path
     * using the {@link ApplicationAdapter}, ensuring that the directory exists
     * and is writable. If the directory cannot be created or is not writable,
     * an {@link IOException} is thrown.
     * @param storeDir the directory path to store session data, relative to the application's base path
     * @throws IOException if the directory cannot be created or is not writable
     */
    public void setStoreDir(String storeDir) throws IOException {
        Path path = getApplicationAdapter().getRealPath(storeDir);
        Files.createDirectories(path);
        if (!Files.isDirectory(path) || !Files.isWritable(path)) {
            throw new IOException("Unable to create session store directory: " + path);
        }
        super.setStoreDir(path.toFile());
    }

}
