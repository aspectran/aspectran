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
 * Abstract base implementation of the {@link ApplicationAdapter} interface.
 *
 * <p>This adapter provides common facilities for resolving file system paths
 * relative to a specified application base path. It also supports fully
 * qualified file URLs. Subclasses are responsible for attribute management.
 * </p>
 *
 * @author Juho Jeong
 * @since 2011. 3. 13.
 */
public abstract class AbstractApplicationAdapter implements ApplicationAdapter {

    /**
     * The normalized, absolute base path of the application.
     */
    private final Path basePath;

    /**
     * Creates a new adapter with the given application base path.
     * @param basePath the application base path, may be {@code null}
     */
    public AbstractApplicationAdapter(String basePath) {
        this.basePath = (basePath != null ? Path.of(basePath).normalize().toAbsolutePath() : null);
    }

    @Override
    public Path getBasePath() {
        return basePath;
    }

    @Override
    public String getBasePathString() {
        return (basePath != null ? basePath.toString() : null);
    }

    /**
     * Resolves the given path to an absolute, real file system path.
     * <p>The resolution logic is as follows:
     * <ul>
     *   <li>If the path starts with "file:", it is treated as a fully qualified URL.</li>
     *   <li>If the path is absolute, it is used as is.</li>
     *   <li>Otherwise, the path is resolved relative to the application base path.
     *   If no base path is configured, it is resolved relative to the current
     *   working directory.</li>
     * </ul>
     * @param path the path to resolve (must not be {@code null})
     * @return the resolved absolute {@link Path}
     */
    @Override
    public Path getRealPath(String path) {
        Assert.notNull(path, "path must not be null");
        if (path.startsWith(FILE_URL_PREFIX)) {
            // A fully qualified URL
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
