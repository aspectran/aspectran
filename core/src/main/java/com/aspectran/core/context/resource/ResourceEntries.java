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
 * The Class ResourceEntries.
 *
 * <p>Created: 2014. 12. 24 PM 4:54:13</p>
 */
public class ResourceEntries extends LinkedHashMap<String, URL> {

    @Serial
    private static final long serialVersionUID = -6936820061673430782L;

    ResourceEntries() {
        super();
    }

    public void putResource(String name, @NonNull File file) throws InvalidResourceException {
        URL url;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new InvalidResourceException("Invalid resource: " + file, e);
        }
        put(name, url);
    }

    public void putResource(@NonNull File file, @NonNull JarEntry entry) throws InvalidResourceException {
        String resourceName = entry.getName();
        URL url;
        try {
            // "jar:file:///C:/proj/parser/jar/parser.jar!/test.xml"
            url = new URI(JAR_URL_PREFIX + file.toURI() + JAR_URL_SEPARATOR + resourceName).toURL();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new InvalidResourceException("Invalid resource: " + file, e);
        }
        put(resourceName, url);
    }

    @Override
    public URL put(String name, URL url) {
        name = name.replace(File.separatorChar, REGULAR_FILE_SEPARATOR_CHAR);
        if (StringUtils.endsWith(name, REGULAR_FILE_SEPARATOR_CHAR)) {
            name = name.substring(0, name.length() - 1);
        }
        return super.put(name, url);
    }

}
