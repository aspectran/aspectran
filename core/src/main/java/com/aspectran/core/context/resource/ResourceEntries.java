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

import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.File;
import java.io.Serial;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.jar.JarEntry;

import static com.aspectran.utils.PathUtils.REGULAR_FILE_SEPARATOR_CHAR;
import static com.aspectran.utils.ResourceUtils.JAR_URL_PREFIX;
import static com.aspectran.utils.ResourceUtils.JAR_URL_SEPARATOR;

/**
 * A specialized map that holds resource names and their corresponding URLs.
 * It extends {@link LinkedHashMap} to maintain the insertion order of resources.
 * Resource names are normalized to use forward slashes and no trailing slashes for consistency.
 *
 * @since 2014. 12. 24
 */
public class ResourceEntries extends LinkedHashMap<String, URL> {

    @Serial
    private static final long serialVersionUID = -6936820061673430782L;

    /**
     * Constructs a new, empty, insertion-ordered {@code ResourceEntries} instance.
     */
    ResourceEntries() {
        super();
    }

    /**
     * Adds a resource entry from a {@link File} object.
     * @param name the name of the resource
     * @param file the resource file
     * @throws InvalidResourceException if the file's URL is malformed
     */
    public void putResource(String name, @NonNull File file) throws InvalidResourceException {
        URL url;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new InvalidResourceException("Invalid resource: " + file, e);
        }
        put(name, url);
    }

    /**
     * Adds a resource entry from a {@link JarEntry} within a JAR file.
     * @param file the JAR file
     * @param entry the entry within the JAR file
     * @throws InvalidResourceException if the resource URL is malformed
     */
    public void putResource(@NonNull File file, @NonNull JarEntry entry) throws InvalidResourceException {
        String resourceName = entry.getName();
        URL url;
        try {
            // e.g., "jar:file:///C:/proj/parser/jar/parser.jar!/test.xml"
            url = new URI(JAR_URL_PREFIX + file.toURI() + JAR_URL_SEPARATOR + resourceName).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new InvalidResourceException("Invalid resource: " + file, e);
        }
        put(resourceName, url);
    }

    /**
     * Overrides the default {@code put} method to normalize the resource name before storing it.
     * Normalization includes replacing backslashes with forward slashes and removing any trailing slash.
     * @param name the resource name to be normalized
     * @param url the resource URL
     * @return the previous value associated with {@code name}, or {@code null} if there was no mapping for {@code name}
     */
    @Override
    public URL put(String name, URL url) {
        name = name.replace(File.separatorChar, REGULAR_FILE_SEPARATOR_CHAR);
        if (StringUtils.endsWith(name, REGULAR_FILE_SEPARATOR_CHAR)) {
            name = name.substring(0, name.length() - 1);
        }
        return super.put(name, url);
    }

}
