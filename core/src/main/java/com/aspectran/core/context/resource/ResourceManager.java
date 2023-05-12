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
package com.aspectran.core.context.resource;

import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.jar.JarEntry;

import static com.aspectran.core.util.ClassUtils.CLASS_FILE_SUFFIX;
import static com.aspectran.core.util.ClassUtils.PACKAGE_SEPARATOR_CHAR;
import static com.aspectran.core.util.ResourceUtils.CLASSPATH_URL_PREFIX;
import static com.aspectran.core.util.ResourceUtils.FILE_URL_PREFIX;
import static com.aspectran.core.util.ResourceUtils.REGULAR_FILE_SEPARATOR_CHAR;

/**
 * The Class ResourceManager.
 *
 * <p>Created: 2014. 12. 18 PM 5:51:13</p>
 */
public class ResourceManager {

    private final ResourceEntries resourceEntries = new ResourceEntries();

    public ResourceManager() {
    }

    public URL getResource(String name) {
        return resourceEntries.get(name);
    }

    public static Enumeration<URL> getResources(final Iterator<SiblingsClassLoader> owners) {
        return new Enumeration<>() {
            private Iterator<URL> values;
            private URL next;

            private boolean hasNext() {
                while (true) {
                    if (values == null) {
                        if (!owners.hasNext()) {
                            return false;
                        }
                        values = owners.next().getResourceManager().getResourceEntries().values().iterator();
                    }
                    if (values.hasNext()) {
                        next = values.next();
                        return true;
                    }
                    values = null;
                }
            }

            @Override
            public synchronized boolean hasMoreElements() {
                return (next != null || hasNext());
            }

            @Override
            public synchronized URL nextElement() {
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

    public static Enumeration<URL> getResources(final Iterator<SiblingsClassLoader> owners, String name) {
        return getResources(owners, name, null);
    }

    public static Enumeration<URL> getResources(final Iterator<SiblingsClassLoader> owners, String name,
                                                final Enumeration<URL> inherited) {
        if (owners == null || name == null) {
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
                    if (owners.hasNext()) {
                        next = owners.next().getResourceManager().getResource(nameToSearch);
                    } else {
                        return false;
                    }
                } while (next == null);
                return true;
            }

            @Override
            public boolean hasMoreElements() {
                if (!noMore) {
                    if (inherited != null && inherited.hasMoreElements()) {
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
                    if (inherited != null && inherited.hasMoreElements()) {
                        return inherited.nextElement();
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

    public static Enumeration<URL> searchResources(final Iterator<SiblingsClassLoader> owners, String name) {
        return searchResources(owners, name, null);
    }

    public static Enumeration<URL> searchResources(final Iterator<SiblingsClassLoader> owners, String name,
                                                   final Enumeration<URL> inherited) {
        if (StringUtils.endsWith(name, REGULAR_FILE_SEPARATOR_CHAR)) {
            name = name.substring(0, name.length() - 1);
        }

        final String nameToSearch = name;

        return new Enumeration<>() {
            private Iterator<Map.Entry<String, URL>> current;
            private Map.Entry<String, URL> entry;
            private boolean noMore; //for parent

            private boolean hasNext() {
                while (true) {
                    if (current == null) {
                        if (!owners.hasNext()) {
                            return false;
                        }
                        current = owners.next().getResourceManager().getResourceEntries().entrySet().iterator();
                    }
                    while (current.hasNext()) {
                        Map.Entry<String, URL> entry2 = current.next();
                        if (entry2.getKey().equals(nameToSearch)) {
                            entry = entry2;
                            return true;
                        }
                    }
                    current = null;
                }
            }

            @Override
            public synchronized boolean hasMoreElements() {
                if (entry != null) {
                    return true;
                }
                if (!noMore) {
                    if (inherited != null && inherited.hasMoreElements()) {
                        return true;
                    } else {
                        noMore = true;
                    }
                }
                return hasNext();
            }

            @Override
            public synchronized URL nextElement() {
                if (entry == null) {
                    if (!noMore) {
                        if (inherited != null && inherited.hasMoreElements()) {
                            return inherited.nextElement();
                        }
                    }
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                }
                URL url = entry.getValue();
                entry = null;
                return url;
            }
        };
    }

    protected ResourceEntries getResourceEntries() {
        return resourceEntries;
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

    public static String resourceNameToClassName(String resourceName) {
        String className = resourceName.substring(0, resourceName.length() - CLASS_FILE_SUFFIX.length());
        className = className.replace(REGULAR_FILE_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
        return className;
    }

    public static String classNameToResourceName(String className) {
        return className.replace(PACKAGE_SEPARATOR_CHAR, REGULAR_FILE_SEPARATOR_CHAR)
                + CLASS_FILE_SUFFIX;
    }

    public static String packageNameToResourceName(String packageName) {
        String resourceName = packageName.replace(PACKAGE_SEPARATOR_CHAR, REGULAR_FILE_SEPARATOR_CHAR);
        if (StringUtils.endsWith(resourceName, REGULAR_FILE_SEPARATOR_CHAR)) {
            resourceName = resourceName.substring(0, resourceName.length() - 1);
        }
        return resourceName;
    }

    public static String[] checkResourceLocations(String[] resourceLocations, String basePath)
            throws InvalidResourceException {
        if (resourceLocations == null) {
            return null;
        }

        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
        for (int i = 0; i < resourceLocations.length; i++) {
            if (resourceLocations[i].startsWith(CLASSPATH_URL_PREFIX)) {
                String path = resourceLocations[i].substring(CLASSPATH_URL_PREFIX.length());
                URL url = classLoader.getResource(path);
                if (url == null) {
                    throw new InvalidResourceException("Class path resource [" + resourceLocations[i] +
                            "] cannot be resolved to URL because it does not exist");
                }
                resourceLocations[i] = url.getFile();
            } else if (resourceLocations[i].startsWith(FILE_URL_PREFIX)) {
                try {
                    URL url = new URL(resourceLocations[i]);
                    resourceLocations[i] = url.getFile();
                } catch (MalformedURLException e) {
                    throw new InvalidResourceException("Resource location [" + resourceLocations[i] +
                            "] is neither a URL not a well-formed file path");
                }
            } else {
                if (basePath != null) {
                    try {
                        File f = new File(basePath, resourceLocations[i]);
                        resourceLocations[i] = f.getCanonicalPath();
                    } catch (IOException e) {
                        throw new InvalidResourceException("Invalid resource location: " + resourceLocations[i], e);
                    }
                }
            }
            resourceLocations[i] = resourceLocations[i].replace(File.separatorChar, REGULAR_FILE_SEPARATOR_CHAR);
            if (StringUtils.endsWith(resourceLocations[i], REGULAR_FILE_SEPARATOR_CHAR)) {
                resourceLocations[i] = resourceLocations[i].substring(0, resourceLocations[i].length() - 1);
            }
        }

        String resourceLocation = null;
        int cleared = 0;

        try {
            for (int i = 0; i < resourceLocations.length - 1; i++) {
                if (resourceLocations[i] != null) {
                    resourceLocation = resourceLocations[i];
                    File f1 = new File(resourceLocations[i]);
                    String l1 = f1.getCanonicalPath();
                    for (int j = i + 1; j < resourceLocations.length; j++) {
                        if (resourceLocations[j] != null) {
                            resourceLocation = resourceLocations[j];
                            File f2 = new File(resourceLocations[j]);
                            String l2 = f2.getCanonicalPath();
                            if (l1.equals(l2)) {
                                resourceLocations[j] = null;
                                cleared++;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new InvalidResourceException("Invalid resource location: " + resourceLocation, e);
        }

        if (cleared > 0) {
            List<String> list = new ArrayList<>(resourceLocations.length);
            for (String r : resourceLocations) {
                if (r != null) {
                    list.add(r);
                }
            }
            return list.toArray(new String[0]);
        } else {
            return resourceLocations;
        }
    }

}
