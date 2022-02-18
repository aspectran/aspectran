/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

import org.jasypt.properties.PropertyValueEncryptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import static com.aspectran.core.util.PBEncryptionUtils.getDefaultEncryptor;

/**
 * Convenient utility methods for loading of java.util.Properties,
 * performing standard handling of input streams.
 */
public class PropertiesLoaderUtils {

    private static final String XML_FILE_EXTENSION = ".xml";

    private static final String ENCRYPTED_RESOURCE_NAME_SUFFIX = "encrypted.properties";

    private static final Map<String, Properties> cache = new ConcurrentReferenceHashMap<>();

    /**
     * Load all properties from the specified class path resource
     * (in ISO-8859-1 encoding), using the default class loader.
     * <p>Merges properties if more than one resource of the same name
     * found in the class path.</p>
     *
     * @param resourceName the name of the class path resource
     * @return the Properties instance
     * @throws IOException if loading failed
     */
    public static Properties loadProperties(String resourceName) throws IOException {
        return loadProperties(resourceName, ClassUtils.getDefaultClassLoader());
    }

    /**
     * Load all properties from the specified class path resource
     * (in ISO-8859-1 encoding), using the given class loader.
     * <p>Merges properties if more than one resource of the same name
     * found in the class path.</p>
     *
     * @param resourceName the name of the class path resource
     * @param classLoader the class loader
     * @return the Properties instance
     * @throws IOException if loading failed
     */
    public static Properties loadProperties(String resourceName, ClassLoader classLoader) throws IOException {
        Properties props = cache.get(resourceName);
        if (props == null) {
            props = new Properties();
            fillProperties(props, resourceName, classLoader);
            Properties existing = cache.putIfAbsent(resourceName, props);
            if (existing != null) {
                props = existing;
            }
        }
        return props;
    }

    /**
     * Fill the given properties from the specified class path resource (in ISO-8859-1 encoding).
     * <p>Merges properties if more than one resource of the same name
     * found in the class path.</p>
     *
     * @param props the Properties instance to load into
     * @param resourceName the name of the class path resource
     * @throws IOException if loading failed
     */
    public static void fillProperties(Properties props, String resourceName)
            throws IOException {
        fillProperties(props, resourceName, ClassUtils.getDefaultClassLoader());
    }

    /**
     * Fill the given properties from the specified class path resource (in ISO-8859-1 encoding).
     * <p>Merges properties if more than one resource of the same name
     * found in the class path.</p>
     *
     * @param props the Properties instance to load into
     * @param resourceName the name of the class path resource
     * @param classLoader the class loader
     * @throws IOException if loading failed
     */
    public static void fillProperties(Properties props, String resourceName, ClassLoader classLoader)
            throws IOException {
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
        if (resourceName.endsWith(ENCRYPTED_RESOURCE_NAME_SUFFIX)) {
            for (Map.Entry<?, ?> entry: props.entrySet()) {
                Object key = entry.getKey();
                Object val = entry.getValue();
                if (val instanceof String) {
                    String value = (String)val;
                    if (PropertyValueEncryptionUtils.isEncryptedValue(value)) {
                        value = PropertyValueEncryptionUtils.decrypt(value, getDefaultEncryptor());
                        props.put(key, value);
                    }
                }
            }
        }
    }

}
