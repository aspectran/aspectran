/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import com.aspectran.utils.Assert;

import java.net.URI;
import java.nio.file.Path;

import static com.aspectran.utils.ResourceUtils.FILE_URL_PREFIX;

/**
 * The Class AbstractApplicationAdapter.
 *
 * @since 2011. 3. 13.
*/
public abstract class AbstractApplicationAdapter implements ApplicationAdapter {

    private final Path basePath;

    /**
     * Instantiates a new AbstractApplicationAdapter.
     */
    public AbstractApplicationAdapter(String basePath) {
        this.basePath = (basePath != null ? Path.of(basePath).normalize().toAbsolutePath() : null);
    }

    @Override
    public String getBasePath() {
        return (basePath != null ? basePath.toString() : null);
    }

    @Override
    public Path getRealPath(String path) {
        Assert.notNull(path, "path must not be null");
        if (path.startsWith(FILE_URL_PREFIX)) {
            // Using url fully qualified paths
            return Path.of(URI.create(path));
        } else {
            Path pathToUse = Path.of(path).normalize();
            if (basePath != null) {
                if (pathToUse.toAbsolutePath().startsWith(basePath)) {
                    return pathToUse;
                } else {
                    return Path.of(basePath.toString(), pathToUse.toString()).toAbsolutePath();
                }
            } else {
                return pathToUse;
            }
        }
    }

}
