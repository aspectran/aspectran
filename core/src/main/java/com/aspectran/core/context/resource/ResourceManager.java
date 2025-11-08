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
package com.aspectran.core.context.resource;

import com.aspectran.utils.PathUtils;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.jar.JarEntry;

import static com.aspectran.utils.ClassUtils.CLASS_FILE_SUFFIX;
import static com.aspectran.utils.ClassUtils.PACKAGE_SEPARATOR_CHAR;
import static com.aspectran.utils.PathUtils.REGULAR_FILE_SEPARATOR_CHAR;
import static com.aspectran.utils.ResourceUtils.CLASSPATH_URL_PREFIX;
import static com.aspectran.utils.ResourceUtils.FILE_URL_PREFIX;

/**
 * Manages and provides access to application resources.
 * This class serves as a base for resource management, holding discovered resources in a
 * {@link ResourceEntries} map. It also provides static utility methods for finding resources
 * across a group of {@link SiblingClassLoader}s and for converting between class names and
 * resource path names.
 *
 * @since 2014. 12. 18
 */
public class ResourceManager {

    private final ResourceEntries resourceEntries = new ResourceEntries();

    /**
     * Default constructor for use by subclasses.
     */
    ResourceManager() {
    }

    /**
     * Retrieves a single resource URL by its name from the internal cache.
     * @param name the name of the resource
     * @return the resource URL, or {@code null} if not found
     */
    public URL getResource(String name) {
        return resourceEntries.get(name);
    }

    /**
     * Returns an iterator over all cached resource URLs.
     * @return an iterator for the resource URLs
     */
    protected Iterator<URL> getResources() {
        return resourceEntries.values().iterator();
    }

    /**
     * Returns the total count of cached resources.
     * @return the number of resources
     */
    public int getNumberOfResources() {
        return resourceEntries.size();
    }

    /**
     * Adds a file-based resource to the cache.
     * @param resourceName the name of the resource
     * @param file the resource file
     * @throws InvalidResourceException if the resource is invalid
     */
    protected void putResource(String resourceName, File file) throws InvalidResourceException {
        resourceEntries.putResource(resourceName, file);
    }

    /**
     * Adds a JAR-based resource to the cache.
     * @param file the JAR file
     * @param entry the entry within the JAR file
     * @throws InvalidResourceException if the resource is invalid
     */
    protected void putResource(File file, JarEntry entry) throws InvalidResourceException {
        resourceEntries.putResource(file, entry);
    }

    /**
     * Resets the resource manager. The base implementation simply calls {@link #release()}.
     * Subclasses should override this to implement actual reloading logic.
     * @throws InvalidResourceException if an error occurs during reset
     */
    public void reset() throws InvalidResourceException {
        release();
    }

    /**
     * Clears all cached resources.
     */
    public void release() {
        resourceEntries.clear();
    }

    /**
     * Finds all resources from a given group of {@link SiblingClassLoader}s.
     * @param siblings an iterator over the sibling class loaders to search
     * @return an enumeration of {@link URL} objects for the resources
     */
    @NonNull
    public static Enumeration<URL> findResources(Iterator<SiblingClassLoader> siblings) {
        return new Enumeration<>() {
            private Iterator<URL> iter;
            private URL next;

            private boolean hasNext() {
                while (true) {
                    if (iter == null) {
                        if (!siblings.hasNext()) {
                            return false;
                        }
                        iter = siblings.next().getResourceManager().getResources();
                    }
                    if (iter.hasNext()) {
                        next = iter.next();
                        return true;
                    }
                    iter = null;
                }
            }

            @Override
            public boolean hasMoreElements() {
                return (next != null || hasNext());
            }

            @Override
            public URL nextElement() {
                if (next == null) {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                }
                URL current = next;
                next = null;
                return current;
            }
        };
    }

    /**
     * Finds a specific resource by name across a group of {@link SiblingClassLoader}s.
     * @param name the name of the resource to find
     * @param siblings an iterator over the sibling class loaders to search
     * @return an enumeration of {@link URL} objects for the resource
     */
    @NonNull
    public static Enumeration<URL> findResources(String name, Iterator<SiblingClassLoader> siblings) {
        return findResources(name, siblings, null);
    }

    /**
     * Finds a specific resource by name across a group of {@link SiblingClassLoader}s, also including parent resources.
     * @param name the name of the resource to find
     * @param siblings an iterator over the sibling class loaders to search
     * @param parentResources an enumeration of resources from a parent class loader
     * @return a combined enumeration of {@link URL} objects for the resource
     */
    @NonNull
    public static Enumeration<URL> findResources(
            String name, Iterator<SiblingClassLoader> siblings, Enumeration<URL> parentResources) {
        if (name == null || siblings == null) {
            return Collections.emptyEnumeration();
        }

        if (StringUtils.endsWith(name, REGULAR_FILE_SEPARATOR_CHAR)) {
            name = name.substring(0, name.length() - 1);
        }

        String nameToSearch = name;

        return new Enumeration<>() {
            private URL next;
            private boolean noMore; //for parent

            private boolean hasNext() {
                do {
                    if (siblings.hasNext()) {
                        next = siblings.next().getResourceManager().getResource(nameToSearch);
                    } else {
                        return false;
                    }
                } while (next == null);
                return true;
            }

            @Override
            public boolean hasMoreElements() {
                if (!noMore) {
                    if (parentResources != null && parentResources.hasMoreElements()) {
                        return true;
                    } else {
                        noMore = true;
                    }
                }
                return (next != null || hasNext());
            }

            @Override
            public URL nextElement() {
                if (!noMore) {
                    if (parentResources != null && parentResources.hasMoreElements()) {
                        return parentResources.nextElement();
                    }
                }
                if (next == null) {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                }
                URL current = next;
                next = null;
                return current;
            }
        };
    }

    /**
     * Converts a resource path name (e.g., "com/example/MyClass.class") to a fully qualified class name.
     * @param resourceName the resource name to convert
     * @return the corresponding class name (e.g., "com.example.MyClass")
     */
    @NonNull
    public static String resourceNameToClassName(@NonNull String resourceName) {
        String className = resourceName.substring(0, resourceName.length() - CLASS_FILE_SUFFIX.length());
        className = className.replace(REGULAR_FILE_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
        return className;
    }

    /**
     * Converts a fully qualified class name (e.g., "com.example.MyClass") to a resource path name.
     * @param className the class name to convert
     * @return the corresponding resource name (e.g., "com/example/MyClass.class")
     */
    @NonNull
    public static String classNameToResourceName(@NonNull String className) {
        return className.replace(PACKAGE_SEPARATOR_CHAR, REGULAR_FILE_SEPARATOR_CHAR)
                + CLASS_FILE_SUFFIX;
    }

    /**
     * Converts a package name (e.g., "com.example") to a resource path name.
     * @param packageName the package name to convert
     * @return the corresponding resource path (e.g., "com/example")
     */
    public static String packageNameToResourceName(@NonNull String packageName) {
        String resourceName = packageName.replace(PACKAGE_SEPARATOR_CHAR, REGULAR_FILE_SEPARATOR_CHAR);
        if (StringUtils.endsWith(resourceName, REGULAR_FILE_SEPARATOR_CHAR)) {
            resourceName = resourceName.substring(0, resourceName.length() - 1);
        }
        return resourceName;
    }

    /**
     * Resolves and sanitizes an array of resource location strings.
     * This method cleans paths, resolves special prefixes like "classpath:" and "file:",
     * resolves relative paths against a base path, and removes duplicates.
     * @param resourceLocations the raw resource locations to check
     * @param basePath the base path to resolve relative paths against
     * @return a sanitized array of unique, absolute resource locations
     * @throws InvalidResourceException if a location is invalid or cannot be resolved
     */
    public static String[] checkResourceLocations(String[] resourceLocations, String basePath)
            throws InvalidResourceException {
        if (resourceLocations == null || resourceLocations.length == 0) {
            return null;
        }

        String[] resourceLocationsToUse = resourceLocations.clone();
        for (int i = 0; i < resourceLocationsToUse.length; i++) {
            String tempLocation = resourceLocationsToUse[i];
            if (tempLocation != null) {
                tempLocation = PathUtils.cleanPath(tempLocation);
                if (StringUtils.endsWith(tempLocation, REGULAR_FILE_SEPARATOR_CHAR)) {
                    tempLocation = tempLocation.substring(0, tempLocation.length() - 1);
                }
                if (tempLocation.startsWith(CLASSPATH_URL_PREFIX)) {
                    String path = tempLocation.substring(CLASSPATH_URL_PREFIX.length());
                    try {
                        URL url = ResourceUtils.getResource(path);
                        tempLocation = url.getPath();
                    } catch (IOException e) {
                        throw new InvalidResourceException("Class path resource [" + tempLocation +
                                "] cannot be resolved to URL", e);
                    }
                } else if (tempLocation.startsWith(FILE_URL_PREFIX)) {
                    try {
                        URL url = ResourceUtils.toURL(tempLocation);
                        tempLocation = url.getFile();
                    } catch (Exception e) {
                        throw new InvalidResourceException("Resource location [" + tempLocation +
                                "] is neither a URL not a well-formed file path", e);
                    }
                } else {
                    try {
                        File f;
                        if (StringUtils.hasText(basePath)) {
                            f = new File(basePath, tempLocation);
                            if (!f.exists()) {
                                File f2 = new File(tempLocation);
                                if (f2.exists()) {
                                    f = f2;
                                }
                            }
                        } else {
                            f = new File(tempLocation);
                        }
                        tempLocation = f.getCanonicalPath();
                    } catch (IOException e) {
                        throw new InvalidResourceException("Invalid resource location: " + tempLocation, e);
                    }
                }
                resourceLocationsToUse[i] = tempLocation;
            }
        }

        for (int i = 0; i < resourceLocationsToUse.length - 1; i++) {
            String tempLocation1 = resourceLocationsToUse[i];
            if (tempLocation1 != null) {
                for (int j = i + 1; j < resourceLocationsToUse.length; j++) {
                    String tempLocation2 = resourceLocationsToUse[j];
                    if (tempLocation2 != null) {
                        if (tempLocation1.equals(tempLocation2)) {
                            resourceLocationsToUse[j] = null;
                        }
                    }
                }
            }
        }

        return Arrays.stream(resourceLocationsToUse).filter(Objects::nonNull).toArray(String[]::new);
    }

}
