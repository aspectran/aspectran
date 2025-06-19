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
 * The Class ResourceManager.
 *
 * <p>Created: 2014. 12. 18 PM 5:51:13</p>
 */
public class ResourceManager {

    private final ResourceEntries resourceEntries = new ResourceEntries();

    ResourceManager() {
    }

    public URL getResource(String name) {
        return resourceEntries.get(name);
    }

    protected Iterator<URL> getResources() {
        return resourceEntries.values().iterator();
    }

    public int getNumberOfResources() {
        return resourceEntries.size();
    }

    protected void putResource(String resourceName, File file) throws InvalidResourceException {
        resourceEntries.putResource(resourceName, file);
    }

    protected void putResource(File file, JarEntry entry) throws InvalidResourceException {
        resourceEntries.putResource(file, entry);
    }

    public void reset() throws InvalidResourceException {
        release();
    }

    public void release() {
        resourceEntries.clear();
    }

    @NonNull
    public static Enumeration<URL> findResources(final Iterator<SiblingClassLoader> siblings) {
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

    @NonNull
    public static Enumeration<URL> findResources(String name, final Iterator<SiblingClassLoader> siblings) {
        return findResources(name, siblings, null);
    }

    @NonNull
    public static Enumeration<URL> findResources(
            String name, final Iterator<SiblingClassLoader> siblings, final Enumeration<URL> parentResources) {
        if (name == null || siblings == null) {
            return Collections.emptyEnumeration();
        }

        if (StringUtils.endsWith(name, REGULAR_FILE_SEPARATOR_CHAR)) {
            name = name.substring(0, name.length() - 1);
        }

        final String nameToSearch = name;

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

    @NonNull
    public static String resourceNameToClassName(@NonNull String resourceName) {
        String className = resourceName.substring(0, resourceName.length() - CLASS_FILE_SUFFIX.length());
        className = className.replace(REGULAR_FILE_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
        return className;
    }

    @NonNull
    public static String classNameToResourceName(@NonNull String className) {
        return className.replace(PACKAGE_SEPARATOR_CHAR, REGULAR_FILE_SEPARATOR_CHAR)
                + CLASS_FILE_SUFFIX;
    }

    public static String packageNameToResourceName(@NonNull String packageName) {
        String resourceName = packageName.replace(PACKAGE_SEPARATOR_CHAR, REGULAR_FILE_SEPARATOR_CHAR);
        if (StringUtils.endsWith(resourceName, REGULAR_FILE_SEPARATOR_CHAR)) {
            resourceName = resourceName.substring(0, resourceName.length() - 1);
        }
        return resourceName;
    }

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
