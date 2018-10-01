/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.util.StringUtils;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.aspectran.core.util.ResourceUtils.REGULAR_FILE_SEPARATOR_CHAR;

/**
 * The Class ResourceManager.
 *
 * <p>Created: 2014. 12. 18 PM 5:51:13</p>
 */
public class ResourceManager {

    protected final ResourceEntries resourceEntries = new ResourceEntries();

    public ResourceManager() {
    }

    protected ResourceEntries getResourceEntries() {
        return resourceEntries;
    }

    public URL getResource(String name) {
        return resourceEntries.get(name);
    }

    public static Enumeration<URL> getResources(final Iterator<AspectranClassLoader> owners) {
        return new Enumeration<URL>() {
            private Iterator<URL> values;
            private URL next;
            private URL current;

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
                current = next;
                next = null;
                return current;
            }
        };
    }

    public static Enumeration<URL> getResources(final Iterator<AspectranClassLoader> owners, String name) {
        return getResources(owners, name, null);
    }

    public static Enumeration<URL> getResources(final Iterator<AspectranClassLoader> owners, String name,
                                                final Enumeration<URL> inherited) {
        if (owners == null || name == null) {
            return Collections.emptyEnumeration();
        }

        if (StringUtils.endsWith(name, REGULAR_FILE_SEPARATOR_CHAR)) {
            name = name.substring(0, name.length() - 1);
        }

        final String filterName = name;

        return new Enumeration<URL>() {
            private URL next;
            private URL current;
            private boolean noMore; //for parent

            private boolean hasNext() {
                do {
                    if (owners.hasNext()) {
                        next = owners.next().getResourceManager().getResource(filterName);
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
                current = next;
                next = null;
                return current;
            }
        };
    }

    public static Enumeration<URL> searchResources(final Iterator<AspectranClassLoader> owners, String name) {
        return searchResources(owners, name, null);
    }

    public static Enumeration<URL> searchResources(final Iterator<AspectranClassLoader> owners, String name,
                                                   final Enumeration<URL> inherited) {
        if (StringUtils.endsWith(name, REGULAR_FILE_SEPARATOR_CHAR)) {
            name = name.substring(0, name.length() - 1);
        }

        final String filterName = name;

        return new Enumeration<URL>() {
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
                        if (entry2.getKey().equals(filterName)) {
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

    public int getResourceEntriesSize() {
        return resourceEntries.size();
    }

    public void reset() throws InvalidResourceException {
        release();
    }

    public void release() {
        resourceEntries.clear();
    }

}
