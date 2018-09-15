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
package com.aspectran.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

public class PropertiesLoaderUtils {

    private static final String XML_FILE_EXTENSION = ".xml";

    /**
     * Indicates whether properties should be cached for improved performance.
     * <p>
     * Note that when this class is deployed via a shared classloader in
     * a container, this will affect all webapps. However making this
     * configurable per webapp would mean having a map keyed by context classloader
     * which may introduce memory-leak problems.
     */
    private static volatile boolean cacheEnabled = true;

    /**
     * Stores a cache of Properties in a WeakHashMap.
     * <p>
     * The keys into this map only ever exist as temporary variables within
     * methods of this class, and are never exposed to users of this class.
     * This means that the WeakHashMap is used only as a mechanism for
     * limiting the size of the cache, ie a way to tell the garbage collector
     * that the contents of the cache can be completely garbage-collected
     * whenever it needs the memory. Whether this is a good approach to
     * this problem is doubtful; something like the commons-collections
     * LRUMap may be more appropriate (though of course selecting an
     * appropriate size is an issue).
     */
    private static final Map<String, Reference<Properties>> cache = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Load all properties from the specified class path resource
     * (in ISO-8859-1 encoding), using the given class loader.
     * <p>Merges properties if more than one resource of the same name
     * found in the class path.
     *
     * @param resourceName the name of the class path resource
     * @param classLoader the class loader
     * @return the Properties instance
     * @throws IOException if loading failed
     */
    public static synchronized Properties loadProperties(String resourceName, ClassLoader classLoader) throws IOException {
        Properties props = getCachedProperties(resourceName);
        if (props != null) {
            return props;
        }

        props = new Properties();
        Enumeration<URL> urls = classLoader.getResources(resourceName);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            URLConnection con = url.openConnection();
            try (InputStream is = con.getInputStream()) {
                if (resourceName.endsWith(XML_FILE_EXTENSION)) {
                    props.loadFromXML(is);
                } else {
                    props.load(is);
                }
            }
        }
        cacheMethod(resourceName, props);
        return props;
    }
    
    /**
     * Return the Properties from the cache, if present.
     *
     * @param resourceName the resource name
     * @return the cached Properties
     */
    private static Properties getCachedProperties(String resourceName) {
        if (cacheEnabled) {
            Reference<Properties> ref = cache.get(resourceName);
            if (ref != null) {
                return ref.get();
            }
        }
        return null;
    }

    /**
     * Add a Properties to the cache.
     *
     * @param resourceName the resource name
     * @param props the Properties to cache
     */
    private static void cacheMethod(String resourceName, Properties props) {
        if (cacheEnabled) {
            if (props != null) {
                cache.put(resourceName, new WeakReference<>(props));
            }
        }
    }

    /**
     * Set whether methods should be cached for greater performance or not,
     * default is <code>true</code>.
     *
     * @param cacheEnabling <code>true</code> if methods should be
     * cached for greater performance, otherwise <code>false</code>
     */
    public static synchronized void setCacheEnabled(boolean cacheEnabling) {
        cacheEnabled = cacheEnabling;
        if (!cacheEnabled) {
            clearCache();
        }
    }

    /**
     * Clear the method cache.
     * @return the number of cached methods cleared
     */
    public static synchronized int clearCache() {
        int size = cache.size();
        cache.clear();
        return size;
    }

}
