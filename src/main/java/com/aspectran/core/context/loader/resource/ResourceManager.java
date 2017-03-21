/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.context.loader.resource;

import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.StringUtils;

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

    public static Enumeration<URL> getResources(final Iterator<AspectranClassLoader> owners, String name, final Enumeration<URL> inherited) {
        if (StringUtils.endsWith(name, ResourceUtils.PATH_SPEPARATOR_CHAR)) {
            name = name.substring(0, name.length() - 1);
        }

        final String filterName = name;

        return new Enumeration<URL>() {
            private URL next;
            private URL current;
            private boolean nomore; //for parent

            private boolean hasNext() {
                do {
                    if (!owners.hasNext()) {
                        return false;
                    }

                    next = owners.next().getResourceManager().getResource(filterName);
                } while (next == null);

                return true;
            }

            @Override
            public synchronized boolean hasMoreElements() {
                if (!nomore) {
                    if (inherited != null && inherited.hasMoreElements()) {
                        return true;
                    } else {
                        nomore = true;
                    }
                }

                return (next != null || hasNext());
            }

            @Override
            public synchronized URL nextElement() {
                if (!nomore) {
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

    public static Enumeration<URL> searchResources(final Iterator<AspectranClassLoader> owners, String name, final Enumeration<URL> inherited) {
        if (StringUtils.endsWith(name, ResourceUtils.PATH_SPEPARATOR_CHAR)) {
            name = name.substring(0, name.length() - 1);
        }

        final String filterName = name;

        return new Enumeration<URL>() {
            private Iterator<Map.Entry<String, URL>> current;
            private Map.Entry<String, URL> entry;
            private boolean nomore; //for parent

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

                if (!nomore) {
                    if (inherited != null && inherited.hasMoreElements()) {
                        return true;
                    } else {
                        nomore = true;
                    }
                }

                return hasNext();
            }

            @Override
            public synchronized URL nextElement() {
                if (entry == null) {
                    if (!nomore) {
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
