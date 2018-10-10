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
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;

public class PropertiesLoaderUtils {

    private static final String XML_FILE_EXTENSION = ".xml";

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
        Properties props = new Properties();
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
        return props;
    }

}
