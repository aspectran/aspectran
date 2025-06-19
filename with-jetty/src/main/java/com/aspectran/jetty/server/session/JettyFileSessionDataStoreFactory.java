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
 * <p>Created: 2025-01-22</p>
 */
public class JettyFileSessionDataStoreFactory extends FileSessionDataStoreFactory implements ApplicationAdapterAware {

    private ApplicationAdapter applicationAdapter;

    public ApplicationAdapter getApplicationAdapter() {
        Assert.state(applicationAdapter != null, "ApplicationAdapter is not set");
        return applicationAdapter;
    }

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    public void setStoreDir(String storeDir) throws IOException {
        Path path = getApplicationAdapter().getRealPath(storeDir);
        Files.createDirectories(path);
        if (!Files.isDirectory(path) || !Files.isWritable(path)) {
            throw new IOException("Unable to create session store directory: " + path);
        }
        super.setStoreDir(path.toFile());
    }

}
