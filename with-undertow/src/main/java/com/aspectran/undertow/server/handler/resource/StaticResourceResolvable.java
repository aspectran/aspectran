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
package com.aspectran.undertow.server.handler.resource;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Interface to be implemented by ResourceManagers that support
 * scanning and resolving their static resources.
 *
 * <p>Created: 2026-07-18</p>
 */
public interface StaticResourceResolvable {

    /**
     * Finds top-level directories and files that are likely to be static resources.
     * @return a set of resource paths
     * @throws IOException if an I/O error occurs
     */
    Set<String> findStaticResources() throws IOException;

    default Set<String> findStaticResources(Path base) throws IOException {
        Set<String> resources = new HashSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(base)) {
            for (Path child : stream) {
                String fileName = child.getFileName().toString();
                if ("WEB-INF".equalsIgnoreCase(fileName) || "META-INF".equalsIgnoreCase(fileName)) {
                    resources.add("/" + fileName + "/");
                } else if (Files.isDirectory(child)) {
                    findStaticResourceDirs(child, "/" + fileName + "/", resources);
                } else {
                    resources.add("/" + fileName);
                }
            }
        }
        return resources;
    }

    default void findStaticResourceDirs(Path parent, String prefix, Set<String> resources) throws IOException {
        Set<Path> children = new HashSet<>();
        boolean found = false;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent)) {
            for (Path child : stream) {
                if (Files.isDirectory(child)) {
                    children.add(child);
                } else {
                    children.clear();
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            resources.add(prefix);
        } else if (!children.isEmpty()) {
            for (Path child : children) {
                findStaticResourceDirs(child, prefix + child.getFileName() + "/", resources);
            }
        }
    }

}
