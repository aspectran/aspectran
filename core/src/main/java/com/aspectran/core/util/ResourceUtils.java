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

import com.aspectran.core.context.resource.AspectranClassLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * A class to simplify access to ResourceUtils through the classloader.
 */
public class ResourceUtils {

    public static final String CLASSPATH_URL_PREFIX = "classpath:";

    public static final String FILE_URL_PREFIX = "file:";

    public static final String JAR_URL_PREFIX = "jar:";

    public static final String JAR_FILE_SUFFIX = ".jar";

    public static final String ZIP_FILE_SUFFIX = ".zip";

    public static final String URL_PROTOCOL_FILE = "file";

    public static final String URL_PROTOCOL_JAR = "jar";

    public static final String URL_PROTOCOL_ZIP = "zip";

    public static final String URL_PROTOCOL_VFSZIP = "vfszip";

    public static final String URL_PROTOCOL_WSJAR = "wsjar";

    public static final String URL_PROTOCOL_CODE_SOURCE = "code-source";

    public static final String JAR_URL_SEPARATOR = "!/";

    public static final String REGULAR_FILE_SEPARATOR = "/";

    public static final char REGULAR_FILE_SEPARATOR_CHAR = '/';

    public static boolean isUrl(String resourceLocation) {
        if (resourceLocation == null) {
            return false;
        }
        if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
            return true;
        }
        try {
            new URL(resourceLocation);
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    public static URL getURL(String resourceLocation, ClassLoader classLoader) throws FileNotFoundException {
        if (resourceLocation == null) {
            throw new IllegalArgumentException("Argument 'resourceLocation' must not be null");
        }
        if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
            String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
            URL url = classLoader.getResource(path);
            if (url == null) {
                String description = "class path resource [" + path + "]";
                throw new FileNotFoundException(description + " cannot be resolved to URL because it does not exist");
            }
            return url;
        }
        try {
            // try URL
            return new URL(resourceLocation);
        } catch (MalformedURLException ex) {
            // no URL -> treat as file path
            try {
                return new File(resourceLocation).toURI().toURL();
            } catch (MalformedURLException ex2) {
                throw new FileNotFoundException("Resource location [" + resourceLocation
                        + "] is neither a URL not a well-formed file path");
            }
        }
    }

    public static File getFile(String resourceLocation, ClassLoader classLoader) throws FileNotFoundException {
        if (resourceLocation == null) {
            throw new IllegalArgumentException("Argument 'resourceLocation' must not be null");
        }
        if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
            String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
            String description = "class path resource [" + path + "]";
            URL url = classLoader.getResource(path);
            if (url == null) {
                throw new FileNotFoundException(description + " cannot be resolved to absolute file path "
                        + "because it does not reside in the file system");
            }
            return getFile(url, description);
        }
        try {
            // try URL
            return getFile(new URL(resourceLocation));
        } catch (MalformedURLException ex) {
            // no URL -> treat as file path
            return new File(resourceLocation);
        }
    }

    public static File getFile(URL resourceUrl) throws FileNotFoundException {
        return getFile(resourceUrl, "URL");
    }

    public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Argument 'resourceUrl' must not be null");
        }
        if (!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
            throw new FileNotFoundException(description + " cannot be resolved to absolute file path "
                    + "because it does not reside in the file system: " + resourceUrl);
        }
        try {
            return new File(toURI(resourceUrl).getSchemeSpecificPart());
        } catch (URISyntaxException ex) {
            // Fallback for URLs that are not valid URIs (should hardly ever happen).
            return new File(resourceUrl.getFile());
        }
    }

    public static File getFile(URI resourceUri) throws FileNotFoundException {
        return getFile(resourceUri, "URI");
    }

    public static File getFile(URI resourceUri, String description) throws FileNotFoundException {
        if (resourceUri == null) {
            throw new IllegalArgumentException("Argument 'resourceUri' must not be null");
        }
        if (!URL_PROTOCOL_FILE.equals(resourceUri.getScheme())) {
            throw new FileNotFoundException(description + " cannot be resolved to absolute file path "
                    + "because it does not reside in the file system: " + resourceUri);
        }
        return new File(resourceUri.getSchemeSpecificPart());
    }

    public static boolean isJarURL(URL url) {
        return URL_PROTOCOL_JAR.equals(url.getProtocol());
    }

    public static boolean isJarSimilarURL(URL url) {
        String protocol = url.getProtocol();
        return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_ZIP.equals(protocol)
                || URL_PROTOCOL_VFSZIP.equals(protocol) || URL_PROTOCOL_WSJAR.equals(protocol)
                || (URL_PROTOCOL_CODE_SOURCE.equals(protocol) && url.getPath().contains(JAR_URL_SEPARATOR)));
    }

    public static URL extractJarFileURL(URL jarUrl) throws MalformedURLException {
        String urlFile = jarUrl.getFile();
        int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
        if (separatorIndex != -1) {
            String jarFile = urlFile.substring(0, separatorIndex);
            try {
                return new URL(jarFile);
            } catch (MalformedURLException ex) {
                // Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
                // This usually indicates that the jar file resides in the file system.
                if (!jarFile.startsWith(REGULAR_FILE_SEPARATOR)) {
                    jarFile = REGULAR_FILE_SEPARATOR + jarFile;
                }
                return new URL(FILE_URL_PREFIX + jarFile);
            }
        } else {
            return jarUrl;
        }
    }

    public static URI toURI(URL url) throws URISyntaxException {
        return toURI(url.toString());
    }

    public static URI toURI(String location) throws URISyntaxException {
        return new URI(StringUtils.replace(location, " ", "%20"));
    }

    /**
     * Returns the URL of the resource on the classpath.
     *
     * @param resource the resource to find
     * @return {@code URL} object for reading the resource;
     *      {@code null} if the resource could not be found
     * @throws IOException if the resource cannot be found or read
     */
    public static URL getResource(String resource) throws IOException {
        return getResource(resource, AspectranClassLoader.getDefaultClassLoader());
    }

    /**
     * Returns the URL of the resource on the classpath.
     *
     * @param classLoader the class loader used to load the resource
     * @param resource the resource to find
     * @return {@code URL} object for reading the resource;
     *      {@code null} if the resource could not be found
     * @throws IOException if the resource cannot be found or read
     */
    public static URL getResource(String resource, ClassLoader classLoader) throws IOException {
        URL url = null;
        if (classLoader != null) {
            url = classLoader.getResource(resource);
        }
        if (url == null) {
            url = ClassLoader.getSystemResource(resource);
        }
        if (url == null) {
            throw new IOException("Could not find resource '" + resource + "'");
        }
        return url;
    }

    public static File getResourceAsFile(String resource) throws IOException {
        return getFile(getResource(resource));
    }

    public static File getResourceAsFile(String resource, ClassLoader classLoader) throws IOException {
        return getFile(getResource(resource, classLoader));
    }

    /**
     * Returns a resource on the classpath as a Stream object.
     *
     * @param resource the resource to find
     * @return an input stream for reading the resource;
     *      {@code null} if the resource could not be found
     * @throws IOException if the resource cannot be found or read
     */
    public static InputStream getResourceAsStream(String resource) throws IOException {
        return getResourceAsStream(resource, AspectranClassLoader.getDefaultClassLoader());
    }

    /**
     * Returns a resource on the classpath as a Stream object.
     *
     * @param resource the resource to find
     * @param classLoader the class loader
     * @return an input stream for reading the resource;
     *      {@code null} if the resource could not be found
     * @throws IOException if the resource cannot be found or read
     */
    public static InputStream getResourceAsStream(String resource, ClassLoader classLoader) throws IOException {
        InputStream in = null;
        if (classLoader != null) {
            in = classLoader.getResourceAsStream(resource);
        }
        if (in == null) {
            in = ClassLoader.getSystemResourceAsStream(resource);
        }
        if (in == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return in;
    }

    /**
     * Returns a Reader for reading the specified file.
     *
     * @param file the file
     * @param encoding the encoding
     * @return the reader instance
     * @throws IOException if an error occurred when reading resources using any I/O operations
     */
    public static Reader getReader(final File file, String encoding) throws IOException {
        InputStream stream;
        try {
            stream = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<InputStream>() {
                        @Override
                        public InputStream run() throws IOException {
                            return new FileInputStream(file);
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            throw (IOException)e.getException();
        }

        Reader reader;
        if (encoding != null) {
            reader = new InputStreamReader(stream, encoding);
        } else {
            reader = new InputStreamReader(stream);
        }
        return reader;
    }

    /**
     * Returns a Reader for reading the specified url.
     *
     * @param url the url
     * @param encoding the encoding
     * @return the reader instance
     * @throws IOException if an error occurred when reading resources using any I/O operations
     */
    public static Reader getReader(final URL url, String encoding) throws IOException {
        InputStream stream;
        try {
            stream = AccessController.doPrivileged(
                    new PrivilegedExceptionAction<InputStream>() {
                        @Override
                        public InputStream run() throws IOException {
                            InputStream is = null;
                            if (url != null) {
                                URLConnection connection = url.openConnection();
                                if (connection != null) {
                                    // Disable caches to get fresh data for reloading.
                                    connection.setUseCaches(false);
                                    is = connection.getInputStream();
                                }
                            }
                            return is;
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            throw (IOException)e.getException();
        }

        Reader reader;
        if (encoding != null) {
            reader = new InputStreamReader(stream, encoding);
        } else {
            reader = new InputStreamReader(stream);
        }
        return reader;
    }

    /**
     * Returns a string from the specified file.
     *
     * @param file the file
     * @param encoding the encoding
     * @return the reader instance
     * @throws IOException if an error occurred when reading resources using any I/O operations
     */
    public static String read(File file, String encoding) throws IOException {
        Reader reader = getReader(file, encoding);
        String source;
        try {
            source = read(reader);
        } finally {
            reader.close();
        }
        return source;
    }

    /**
     * Returns a string from the specified url.
     *
     * @param url the url
     * @param encoding the encoding
     * @return the string
     * @throws IOException if an error occurred when reading resources using any I/O operations
     */
    public static String read(URL url, String encoding) throws IOException {
        Reader reader = getReader(url, encoding);
        String source;
        try {
            source = read(reader);
        } finally {
            reader.close();
        }
        return source;
    }

    /**
     * Returns a string from the specified Reader object.
     *
     * @param reader the reader
     * @return the string
     * @throws IOException if an error occurred when reading resources using any I/O operations
     */
    public static String read(Reader reader) throws IOException {
        final char[] buffer = new char[1024];
        StringBuilder sb = new StringBuilder();
        int len;
        while ((len = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, len);
        }
        return sb.toString();
    }

}
