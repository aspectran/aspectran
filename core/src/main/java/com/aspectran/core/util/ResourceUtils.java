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
package com.aspectran.core.util;

import com.aspectran.core.lang.Nullable;

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

    /** Pseudo URL prefix for loading from the class path: "classpath:". */
    public static final String CLASSPATH_URL_PREFIX = "classpath:";

    /** URL prefix for loading from the file system: "file:". */
    public static final String FILE_URL_PREFIX = "file:";

    /** URL prefix for loading from a jar file: "jar:". */
    public static final String JAR_URL_PREFIX = "jar:";

    /** URL prefix for loading from a war file on Tomcat: "war:". */
    public static final String WAR_URL_PREFIX = "war:";

    /** URL protocol for a file in the file system: "file". */
    public static final String URL_PROTOCOL_FILE = "file";

    /** URL protocol for an entry from a jar file: "jar". */
    public static final String URL_PROTOCOL_JAR = "jar";

    /** URL protocol for an entry from a war file: "war". */
    public static final String URL_PROTOCOL_WAR = "war";

    /** URL protocol for an entry from a zip file: "zip". */
    public static final String URL_PROTOCOL_ZIP = "zip";

    /** URL protocol for an entry from a WebSphere jar file: "wsjar". */
    public static final String URL_PROTOCOL_WSJAR = "wsjar";

    /** URL protocol for an entry from a JBoss jar file: "vfszip". */
    public static final String URL_PROTOCOL_VFSZIP = "vfszip";

    /** URL protocol for a JBoss file system resource: "vfsfile". */
    public static final String URL_PROTOCOL_VFSFILE = "vfsfile";

    /** URL protocol for a general JBoss VFS resource: "vfs". */
    public static final String URL_PROTOCOL_VFS = "vfs";

    /** File extension for a regular jar file: ".jar". */
    public static final String JAR_FILE_EXTENSION = ".jar";

    /** Separator between JAR URL and file path within the JAR: "!/". */
    public static final String JAR_URL_SEPARATOR = "!/";

    /** Special separator between WAR URL and jar part on Tomcat. */
    public static final String WAR_URL_SEPARATOR = "*/";

    public static final String REGULAR_FILE_SEPARATOR = "/";

    public static final char REGULAR_FILE_SEPARATOR_CHAR = '/';

    /**
     * Return whether the given resource location is a URL:
     * either a special "classpath" pseudo URL or a standard URL.
     * @param resourceLocation the location String to check
     * @return whether the location qualifies as a URL
     * @see #CLASSPATH_URL_PREFIX
     * @see java.net.URL
     */
    public static boolean isUrl(@Nullable String resourceLocation) {
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

    /**
     * Resolve the given resource location to a {@code java.net.URL}.
     * <p>Does not check whether the URL actually exists; simply returns
     * the URL that the given location would correspond to.</p>
     * @param resourceLocation the resource location to resolve: either a
     *      "classpath:" pseudo URL, a "file:" URL, or a plain file path
     * @return a corresponding URL object
     * @throws FileNotFoundException if the resource cannot be resolved to a URL
     */
    public static URL getURL(String resourceLocation) throws FileNotFoundException {
        return getURL(resourceLocation, ClassUtils.getDefaultClassLoader());
    }

    /**
     * Resolve the given resource location to a {@code java.net.URL}.
     * <p>Does not check whether the URL actually exists; simply returns
     * the URL that the given location would correspond to.</p>
     * @param resourceLocation the resource location to resolve: either a
     *      "classpath:" pseudo URL, a "file:" URL, or a plain file path
     * @param classLoader the class loader
     * @return a corresponding URL object
     * @throws FileNotFoundException if the resource cannot be resolved to a URL
     */
    public static URL getURL(String resourceLocation, ClassLoader classLoader) throws FileNotFoundException {
        Assert.notNull(resourceLocation, "Resource location must not be null");
        if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
            String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
            URL url = (classLoader != null ? classLoader.getResource(path) : ClassLoader.getSystemResource(path));
            if (url == null) {
                String description = "class path resource [" + path + "]";
                throw new FileNotFoundException(description +
                        " cannot be resolved to URL because it does not exist");
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
                throw new FileNotFoundException("Resource location [" + resourceLocation +
                        "] is neither a URL not a well-formed file path");
            }
        }
    }

    /**
     * Resolve the given resource location to a {@code java.io.File},
     * i.e. to a file in the file system.
     * <p>Does not check whether the file actually exists; simply returns
     * the File that the given location would correspond to.</p>
     * @param resourceLocation the resource location to resolve: either a
     *      "classpath:" pseudo URL, a "file:" URL, or a plain file path
     * @return a corresponding File object
     * @throws FileNotFoundException if the resource cannot be resolved to
     *      a file in the file system
     */
    public static File getFile(String resourceLocation) throws FileNotFoundException {
        return getFile(resourceLocation, ClassUtils.getDefaultClassLoader());
    }

    /**
     * Resolve the given resource location to a {@code java.io.File},
     * i.e. to a file in the file system.
     * <p>Does not check whether the file actually exists; simply returns
     * the File that the given location would correspond to.</p>
     * @param resourceLocation the resource location to resolve: either a
     *      "classpath:" pseudo URL, a "file:" URL, or a plain file path
     * @param classLoader the class loader
     * @return a corresponding File object
     * @throws FileNotFoundException if the resource cannot be resolved to
     *      a file in the file system
     */
    public static File getFile(String resourceLocation, ClassLoader classLoader) throws FileNotFoundException {
        Assert.notNull(resourceLocation, "Resource location must not be null");
        if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
            String path = resourceLocation.substring(CLASSPATH_URL_PREFIX.length());
            String description = "class path resource [" + path + "]";
            URL url = (classLoader != null ? classLoader.getResource(path) : ClassLoader.getSystemResource(path));
            if (url == null) {
                throw new FileNotFoundException(description +
                        " cannot be resolved to absolute file path because it does not exist");
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

    /**
     * Resolve the given resource URL to a {@code java.io.File},
     * i.e. to a file in the file system.
     * @param resourceUrl the resource URL to resolve
     * @return a corresponding File object
     * @throws FileNotFoundException if the URL cannot be resolved to
     *      a file in the file system
     */
    public static File getFile(URL resourceUrl) throws FileNotFoundException {
        return getFile(resourceUrl, "URL");
    }

    /**
     * Resolve the given resource URL to a {@code java.io.File},
     * i.e. to a file in the file system.
     * @param resourceUrl the resource URL to resolve
     * @param description a description of the original resource that
     *      the URL was created for (for example, a class path location)
     * @return a corresponding File object
     * @throws FileNotFoundException if the URL cannot be resolved to
     *      a file in the file system
     */
    public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
        Assert.notNull(resourceUrl, "Resource URL must not be null");
        if (!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
            throw new FileNotFoundException(
                    description + " cannot be resolved to absolute file path " +
                            "because it does not reside in the file system: " + resourceUrl);
        }
        try {
            return new File(toURI(resourceUrl).getSchemeSpecificPart());
        } catch (URISyntaxException ex) {
            // Fallback for URLs that are not valid URIs (should hardly ever happen).
            return new File(resourceUrl.getFile());
        }
    }

    /**
     * Resolve the given resource URI to a {@code java.io.File},
     * i.e. to a file in the file system.
     * @param resourceUri the resource URI to resolve
     * @return a corresponding File object
     * @throws FileNotFoundException if the URL cannot be resolved to
     *      a file in the file system
     */
    public static File getFile(URI resourceUri) throws FileNotFoundException {
        return getFile(resourceUri, "URI");
    }

    /**
     * Resolve the given resource URI to a {@code java.io.File},
     * i.e. to a file in the file system.
     * @param resourceUri the resource URI to resolve
     * @param description a description of the original resource that
     *      the URI was created for (for example, a class path location)
     * @return a corresponding File object
     * @throws FileNotFoundException if the URL cannot be resolved to
     *      a file in the file system
     */
    public static File getFile(URI resourceUri, String description) throws FileNotFoundException {
        Assert.notNull(resourceUri, "Resource URI must not be null");
        if (!URL_PROTOCOL_FILE.equals(resourceUri.getScheme())) {
            throw new FileNotFoundException(
                    description + " cannot be resolved to absolute file path " +
                            "because it does not reside in the file system: " + resourceUri);
        }
        return new File(resourceUri.getSchemeSpecificPart());
    }

    /**
     * Determine whether the given URL points to a resource in the file system,
     * i.e. has protocol "file", "vfsfile" or "vfs".
     * @param url the URL to check
     * @return whether the URL has been identified as a file system URL
     */
    public static boolean isFileURL(URL url) {
        String protocol = url.getProtocol();
        return (URL_PROTOCOL_FILE.equals(protocol) || URL_PROTOCOL_VFSFILE.equals(protocol) ||
                URL_PROTOCOL_VFS.equals(protocol));
    }

    /**
     * Determine whether the given URL points to a resource in a jar file.
     * i.e. has protocol "jar", "war, ""zip", "vfszip" or "wsjar".
     * @param url the URL to check
     * @return whether the URL has been identified as a JAR URL
     */
    public static boolean isJarURL(URL url) {
        String protocol = url.getProtocol();
        return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_WAR.equals(protocol) ||
                URL_PROTOCOL_ZIP.equals(protocol) || URL_PROTOCOL_VFSZIP.equals(protocol) ||
                URL_PROTOCOL_WSJAR.equals(protocol));
    }

    /**
     * Determine whether the given URL points to a jar file itself,
     * that is, has protocol "file" and ends with the ".jar" extension.
     * @param url the URL to check
     * @return whether the URL has been identified as a JAR file URL
     */
    public static boolean isJarFileURL(URL url) {
        return (URL_PROTOCOL_FILE.equals(url.getProtocol()) &&
                url.getPath().toLowerCase().endsWith(JAR_FILE_EXTENSION));
    }

    /**
     * Extract the URL for the actual jar file from the given URL
     * (which may point to a resource in a jar file or to a jar file itself).
     * @param jarUrl the original URL
     * @return the URL for the actual jar file
     * @throws MalformedURLException if no valid jar file URL could be extracted
     */
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
                if (!jarFile.startsWith("/")) {
                    jarFile = "/" + jarFile;
                }
                return new URL(FILE_URL_PREFIX + jarFile);
            }
        } else {
            return jarUrl;
        }
    }

    /**
     * Extract the URL for the outermost archive from the given jar/war URL
     * (which may point to a resource in a jar file or to a jar file itself).
     * <p>In the case of a jar file nested within a war file, this will return
     * a URL to the war file since that is the one resolvable in the file system.</p>
     * @param jarUrl the original URL
     * @return the URL for the actual jar file
     * @throws MalformedURLException if no valid jar file URL could be extracted
     * @see #extractJarFileURL(URL)
     */
    public static URL extractArchiveURL(URL jarUrl) throws MalformedURLException {
        String urlFile = jarUrl.getFile();

        int endIndex = urlFile.indexOf(WAR_URL_SEPARATOR);
        if (endIndex != -1) {
            // Tomcat's "war:file:...mywar.war*/WEB-INF/lib/myjar.jar!/myentry.txt"
            String warFile = urlFile.substring(0, endIndex);
            if (URL_PROTOCOL_WAR.equals(jarUrl.getProtocol())) {
                return new URL(warFile);
            }
            int startIndex = warFile.indexOf(WAR_URL_PREFIX);
            if (startIndex != -1) {
                return new URL(warFile.substring(startIndex + WAR_URL_PREFIX.length()));
            }
        }

        // Regular "jar:file:...myjar.jar!/myentry.txt"
        return extractJarFileURL(jarUrl);
    }

    /**
     * Create a URI instance for the given URL,
     * replacing spaces with "%20" URI encoding first.
     * @param url the URL to convert into a URI instance
     * @return the URI instance
     * @throws URISyntaxException if the URL wasn't a valid URI
     * @see java.net.URL#toURI()
     */
    public static URI toURI(URL url) throws URISyntaxException {
        return toURI(url.toString());
    }

    /**
     * Create a URI instance for the given location String,
     * replacing spaces with "%20" URI encoding first.
     * @param location the location String to convert into a URI instance
     * @return the URI instance
     * @throws URISyntaxException if the location wasn't a valid URI
     */
    public static URI toURI(String location) throws URISyntaxException {
        return new URI(StringUtils.replace(location, " ", "%20"));
    }

    /**
     * Returns the URL of the resource on the classpath.
     * @param resource the resource to find
     * @return {@code URL} object for reading the resource;
     *      {@code null} if the resource could not be found
     * @throws IOException if the resource cannot be found or read
     */
    public static URL getResource(String resource) throws IOException {
        return getResource(resource, ClassUtils.getDefaultClassLoader());
    }

    /**
     * Returns the URL of the resource on the classpath.
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
     * @param resource the resource to find
     * @return an input stream for reading the resource;
     *      {@code null} if the resource could not be found
     * @throws IOException if the resource cannot be found or read
     */
    public static InputStream getResourceAsStream(String resource) throws IOException {
        return getResourceAsStream(resource, ClassUtils.getDefaultClassLoader());
    }

    /**
     * Returns a resource on the classpath as a Stream object.
     * @param resource the resource to find
     * @param classLoader the class loader
     * @return an input stream for reading the resource;
     *      {@code null} if the resource could not be found
     * @throws IOException if the resource cannot be found or read
     */
    public static InputStream getResourceAsStream(String resource, ClassLoader classLoader) throws IOException {
        InputStream stream = null;
        if (classLoader != null) {
            stream = classLoader.getResourceAsStream(resource);
        }
        if (stream == null) {
            stream = ClassLoader.getSystemResourceAsStream(resource);
        }
        if (stream == null) {
            throw new IOException("Could not find resource " + resource);
        }
        return stream;
    }

    /**
     * Returns a Reader for reading the specified file.
     * @param file the file
     * @param encoding the encoding
     * @return the reader instance
     * @throws IOException if an error occurred when reading resources using any I/O operations
     */
    public static Reader getReader(final File file, String encoding) throws IOException {
        InputStream stream;
        try {
            stream = AccessController.doPrivileged((PrivilegedExceptionAction<InputStream>)()
                    -> new FileInputStream(file));
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
     * @param url the url
     * @param encoding the encoding
     * @return the reader instance
     * @throws IOException if an error occurred when reading resources using any I/O operations
     */
    public static Reader getReader(final URL url, String encoding) throws IOException {
        InputStream stream;
        try {
            stream = AccessController.doPrivileged((PrivilegedExceptionAction<InputStream>)() -> {
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
            });
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
