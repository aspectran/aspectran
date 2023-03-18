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
package com.aspectran.core.adapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static com.aspectran.core.util.ResourceUtils.CLASSPATH_URL_PREFIX;
import static com.aspectran.core.util.ResourceUtils.FILE_URL_PREFIX;
import static com.aspectran.core.util.ResourceUtils.URL_PROTOCOL_FILE;

/**
 * The Class AbstractApplicationAdapter.
 *
 * @since 2011. 3. 13.
*/
public abstract class AbstractApplicationAdapter implements ApplicationAdapter {

    private final String basePath;

    private final ClassLoader classLoader;

    /**
     * Instantiates a new AbstractApplicationAdapter.
     */
    public AbstractApplicationAdapter(String basePath, ClassLoader classLoader) {
        this.basePath = basePath;
        this.classLoader = classLoader;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public String toRealPath(String filePath) throws IOException {
        return toRealPathAsFile(filePath).getCanonicalPath();
    }

    @Override
    public File toRealPathAsFile(String filePath) throws IOException {
        File file;
        if (filePath.startsWith(FILE_URL_PREFIX)) {
            // Using url fully qualified paths
            URI uri = URI.create(filePath);
            file = new File(uri);
        } else if (filePath.startsWith(CLASSPATH_URL_PREFIX)) {
            // Using classpath relative resources
            String path = filePath.substring(CLASSPATH_URL_PREFIX.length());
            URL url = getClassLoader().getResource(path);
            if (url == null || !URL_PROTOCOL_FILE.equals(url.getProtocol())) {
                throw new FileNotFoundException("Could not find resource file for given classpath: " + path);
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
