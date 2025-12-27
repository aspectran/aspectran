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
package com.aspectran.utils;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A utility class for handling resource paths.
 * <p>Provides methods for normalizing paths and applying relative paths, primarily for
 * internal use within the framework. It standardizes on forward slashes ('/') as separators.</p>
 */
public class PathUtils {

    /**
     * The standard path separator: a forward slash "/".
     */
    public static final String REGULAR_FILE_SEPARATOR = "/";

    /**
     * The standard path separator character: a forward slash '/'.
     */
    public static final char REGULAR_FILE_SEPARATOR_CHAR = '/';

    /**
     * The Windows path separator: a backslash "\".
     */
    public static final String WINDOWS_FILE_SEPARATOR = "\\";

    private static final String TOP_PATH = "..";

    private static final String CURRENT_PATH = ".";

    /**
     * This class cannot be instantiated.
     */
    private PathUtils() {
    }

    /**
     * Applies a relative path to a given base path.
     * @param path the base path (e.g., "/a/b/c.txt")
     * @param relativePath the relative path to apply (e.g., "../d/e.txt")
     * @return the combined path (e.g., "/a/d/e.txt")
     */
    public static String applyRelativePath(String path, String relativePath) {
        Assert.notNull(path, "path must not be null");
        Assert.notNull(relativePath, "relativePath must not be null");
        int separatorIndex = path.lastIndexOf(REGULAR_FILE_SEPARATOR_CHAR);
        if (separatorIndex != -1) {
            String newPath = path.substring(0, separatorIndex);
            if (relativePath.startsWith(REGULAR_FILE_SEPARATOR)) {
                return newPath + relativePath;
            } else {
                return (newPath + REGULAR_FILE_SEPARATOR_CHAR + relativePath);
            }
        } else {
            return relativePath;
        }
    }

    /**
     * Normalizes a path by simplifying sequences like "/./" and "/../".
     * <p>This method standardizes path separators to forward slashes ('/') and resolves
     * relative path elements. For example, "/a/b/../c" will be normalized to "/a/c".</p>
     * <p><strong>NOTE:</strong> This method is intended for path comparison and resource loading,
     * not for security-sensitive contexts. It does not prevent path traversal attacks.
     * Other mechanisms should be used for security validation.</p>
     * @param path the original path to normalize
     * @return the normalized path, or the original path if it is null or empty
     */
    public static String cleanPath(String path) {
        if (!StringUtils.hasLength(path)) {
            return path;
        }

        String normalizedPath = StringUtils.replace(path, WINDOWS_FILE_SEPARATOR, REGULAR_FILE_SEPARATOR);
        String pathToUse = normalizedPath;

        // Shortcut if there is no work to do
        if (pathToUse.indexOf('.') == -1) {
            return pathToUse;
        }

        // Strip prefix from path to analyze, to not treat it as part of the
        // first path element. This is necessary to correctly parse paths like
        // "file:core/../core/io/Resource.class", where the ".." should just
        // strip the first "core" directory while keeping the "file:" prefix.
        int prefixIndex = pathToUse.indexOf(':');
        String prefix = "";
        if (prefixIndex != -1) {
            prefix = pathToUse.substring(0, prefixIndex + 1);
            if (prefix.contains(REGULAR_FILE_SEPARATOR)) {
                prefix = "";
            } else {
                pathToUse = pathToUse.substring(prefixIndex + 1);
            }
        }
        if (pathToUse.startsWith(REGULAR_FILE_SEPARATOR)) {
            prefix = prefix + REGULAR_FILE_SEPARATOR;
            pathToUse = pathToUse.substring(1);
        }

        String[] pathArray = StringUtils.split(pathToUse, REGULAR_FILE_SEPARATOR);
        // we never require more elements than pathArray and in the common case the same number
        Deque<String> pathElements = new ArrayDeque<>(pathArray.length);
        int tops = 0;

        for (int i = pathArray.length - 1; i >= 0; i--) {
            String element = pathArray[i];
            if (CURRENT_PATH.equals(element)) {
                // Points to current directory - drop it.
            } else if (TOP_PATH.equals(element)) {
                // Registering top path found.
                tops++;
            } else {
                if (tops > 0) {
                    // Merging path element with element corresponding to top path.
                    tops--;
                } else {
                    // Normal path element found.
                    pathElements.addFirst(element);
                }
            }
        }

        // All path elements stayed the same - shortcut
        if (pathArray.length == pathElements.size()) {
            return normalizedPath;
        }
        // Remaining top paths need to be retained.
        for (int i = 0; i < tops; i++) {
            pathElements.addFirst(TOP_PATH);
        }
        // If nothing else left, at least explicitly point to current path.
        if (pathElements.size() == 1 && pathElements.getLast().isEmpty() && !prefix.endsWith(REGULAR_FILE_SEPARATOR)) {
            pathElements.addFirst(CURRENT_PATH);
        }

        String joined = StringUtils.join(pathElements, REGULAR_FILE_SEPARATOR);
        // avoid string concatenation with empty prefix
        return prefix.isEmpty() ? joined : prefix + joined;
    }

    /**
     * Compares two paths after normalizing them with {@link #cleanPath(String)}.
     * @param path1 the first path to compare
     * @param path2 the second path to compare
     * @return {@code true} if the two paths are equivalent after normalization, {@code false} otherwise
     */
    public static boolean pathEquals(String path1, String path2) {
        return cleanPath(path1).equals(cleanPath(path2));
    }

}
