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
package com.aspectran.core.adapter;

import com.aspectran.utils.Assert;

import java.net.URI;
import java.nio.file.Path;

import static com.aspectran.utils.ResourceUtils.FILE_URL_PREFIX;

/**
 * Base implementation of {@link ApplicationAdapter} that provides common
 * path resolution facilities relative to an optional application base path.
 * <p>
 * Implementations can delegate to this class to translate virtual paths into
 * real file system paths while supporting fully qualified file URLs.
 * </p>
 *
 * @since 2011. 3. 13.
*/
public abstract class AbstractApplicationAdapter implements ApplicationAdapter {

    /**
     * The normalized, absolute base path of the application, or {@code null} if not set.
     */
    private final Path basePath;

    /**
     * Create a new adapter with the given base path.
     * @param basePath the application base path; may be {@code null}
     */
    public AbstractApplicationAdapter(String basePath) {
        this.basePath = (basePath != null ? Path.of(basePath).normalize().toAbsolutePath() : null);
    }

    /**
     * Returns the application base path as a {@link Path}.
     */
    @Override
    public Path getBasePath() {
        return basePath;
    }

    /**
     * Returns the application base path as a {@link String}.
     */
    @Override
    public String getBasePathString() {
        return (basePath != null ? basePath.toString() : null);
    }

    /**
     * Resolve the given path to an absolute path.
     * <ul>
     *   <li>If the argument starts with the file: URL scheme, it is treated as a fully
     *   qualified URL and converted directly to a Path.</li>
     *   <li>Otherwise, the path is normalized. If a base path is configured and the
     *   normalized path is not already under it, the path is resolved against the base.</li>
     * </ul>
     * @param path the virtual or absolute path to resolve (must not be {@code null})
     * @return the resolved absolute path
     */
    @Override
    public Path getRealPath(String path) {
        Assert.notNull(path, "path must not be null");
        if (path.startsWith(FILE_URL_PREFIX)) {
            // Using url fully qualified paths
            return Path.of(URI.create(path));
        } else {
            Path normalized = Path.of(path).normalize();
            Path absolutePath = normalized.toAbsolutePath();
            if (basePath != null) {
                if (absolutePath.startsWith(basePath)) {
                    return absolutePath;
                } else {
                    return Path.of(basePath.toString(), normalized.toString()).toAbsolutePath();
                }
            } else {
                return absolutePath;
            }
        }
    }

}
