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

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.ResourceUtils;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.URLResource;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A {@link ResourceManager} implementation that serves resources from the classpath
 * using the class loader of the Aspectran {@link ActivityContext}.
 *
 * <p>Created: 2026-07-17</p>
 */
public class TowClassPathResourceManager implements StaticResourceResolvable, ResourceManager, ActivityContextAware {

    private ActivityContext context;

    private final String prefix;

    /**
     * Instantiates a new TowClassPathResourceManager.
     */
    public TowClassPathResourceManager() {
        this("");
    }

    /**
     * Instantiates a new TowClassPathResourceManager with the specified prefix.
     * @param prefix the prefix to apply to resource paths
     */
    public TowClassPathResourceManager(String prefix) {
        if (prefix == null) {
            this.prefix = "";
        } else if (prefix.endsWith("/")) {
            this.prefix = prefix;
        } else {
            this.prefix = prefix + "/";
        }
    }

    @Override
    public void setActivityContext(@NonNull ActivityContext context) {
        this.context = context;
    }

    @Override
    public Resource getResource(String path) throws IOException {
        if (context == null || path == null) {
            return null;
        }
        String modPath = path;
        if (modPath.startsWith("/")) {
            modPath = modPath.substring(1);
        }
        final String realPath = prefix + modPath;
        final URL resource = context.getClassLoader().getResource(realPath);
        if (resource == null) {
            return null;
        }
        return new URLResource(resource, path);
    }

    @Override
    public boolean isResourceChangeListenerSupported() {
        return false;
    }

    @Override
    public void registerResourceChangeListener(ResourceChangeListener listener) {
        // Classpath resources are static and do not change at runtime.
    }

    @Override
    public void removeResourceChangeListener(ResourceChangeListener listener) {
    }

    @Override
    public void close() throws IOException {
    }

    /**
     * Finds top-level directories and files that are likely to be static resources.
     * @return a set of resource paths
     * @throws IOException if an I/O error occurs
     */
    public Set<String> findStaticResources() throws IOException {
        if (context == null || prefix == null) {
            return Collections.emptySet();
        }
        URL url = context.getClassLoader().getResource(prefix);
        if (url == null) {
            return Collections.emptySet();
        }

        if (ResourceUtils.isFileURL(url)) {
            try {
                Path base = Paths.get(ResourceUtils.toURI(url));
                return findStaticResources(base);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        } else if (ResourceUtils.isJarURL(url)) {
            URL jarUrl = ResourceUtils.extractJarFileURL(url);
            try {
                File file = ResourceUtils.getFile(jarUrl);
                try (JarFile jarFile = new JarFile(file)) {
                    return findStaticResources(jarFile);
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        return Collections.emptySet();
    }

    @NonNull
    private Set<String> findStaticResources(@NonNull JarFile jarFile) {
        Node root = new Node("");
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (entryName.startsWith(prefix) && entryName.length() > prefix.length()) {
                String relPath = entryName.substring(prefix.length());
                String[] parts = relPath.split("/");
                Node current = root;
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    if (part.isEmpty()) {
                        continue;
                    }
                    Node child = current.children.get(part);
                    if (child == null) {
                        child = new Node(part);
                        current.children.put(part, child);
                    }
                    if (i < parts.length - 1 || entry.isDirectory() || entryName.endsWith("/" + part + "/")) {
                        child.isDirectory = true;
                    }
                    current = child;
                }
            }
        }

        Set<String> resources = new HashSet<>();
        for (Node child : root.children.values()) {
            if ("WEB-INF".equalsIgnoreCase(child.name) || "META-INF".equalsIgnoreCase(child.name)) {
                resources.add("/" + child.name + "/");
            } else if (child.isDirectory) {
                findStaticResourceDirsForJar(child, "/" + child.name + "/", resources);
            } else {
                resources.add("/" + child.name);
            }
        }
        return resources;
    }

    private void findStaticResourceDirsForJar(@NonNull Node parent, String prefix, Set<String> resources) {
        Set<Node> children = new HashSet<>();
        boolean found = false;
        for (Node child : parent.children.values()) {
            if (child.isDirectory) {
                children.add(child);
            } else {
                children.clear();
                found = true;
                break;
            }
        }
        if (found) {
            resources.add(prefix);
        } else if (!children.isEmpty()) {
            for (Node child : children) {
                findStaticResourceDirsForJar(child, prefix + child.name + "/", resources);
            }
        }
    }

    private static class Node {
        final String name;
        boolean isDirectory;
        final Map<String, Node> children = new HashMap<>();

        Node(String name) {
            this.name = name;
        }
    }

}
