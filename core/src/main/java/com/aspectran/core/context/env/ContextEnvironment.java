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
package com.aspectran.core.context.env;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.resource.AspectranClassLoader;
import com.aspectran.core.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * The Environment for ActivityContext
 */
public class ContextEnvironment extends AbstractEnvironment {

    private final ApplicationAdapter applicationAdapter;

    private ClassLoader classLoader;

    private String basePath;

    public ContextEnvironment(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    @Override
    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    @Override
    public ClassLoader getClassLoader() {
        if (classLoader != null) {
            return classLoader;
        } else {
            return AspectranClassLoader.getDefaultClassLoader();
        }
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    /**
     * Sets the application base path.
     *
     * @param basePath the new application base path
     */
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public String toRealPath(String filePath) throws IOException {
        File file = toRealPathAsFile(filePath);
        return file.getCanonicalPath();
    }

    @Override
    public File toRealPathAsFile(String filePath) throws IOException {
        File file;
        if (filePath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            // Using url fully qualified paths
            URI uri = URI.create(filePath);
            file = new File(uri);
        } else if (filePath.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            // Using classpath relative resources
            URL url = getClassLoader().getResource(filePath);
            if (url == null) {
                throw new IOException("Could not find the resource with the given name: " + filePath);
            }
            file = new File(url.getFile());
        } else {
            if (basePath != null) {
                file = new File(basePath, filePath);
            } else {
                file = new File(filePath);
            }
        }
        return file;
    }

}
