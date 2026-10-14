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
package com.aspectran.undertow.server.servlet;

import com.aspectran.utils.ResourceUtils;
import jakarta.servlet.ServletContext;
import org.apache.jasper.servlet.TldScanner;
import org.apache.tomcat.Jar;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.apache.tomcat.util.scan.JarFactory;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Scans for and loads Tag Library Descriptors contained in a web application.
 */
public class TowTldScanner extends TldScanner {

    private final Logger logger = LoggerFactory.getLogger(TowTldScanner.class);

    private static final String[] ASPECTRAN_TAGLIB_RESOURCES = {
        "com/aspectran/web/servlet/support/tags/aspectran.tld"
    };

    private static final String ASPECTRAN_TAGLIB_URI = "aspectran.tags";

    private static final String LEGACY_ASPECTRAN_TAGLIB_URI = "http://aspectran.com/tags";

    private static final String[] JSTL_TAGLIB_MARKERS = {
        "META-INF/c.tld",
        "META-INF/fmt.tld",
        "META-INF/fn.tld",
        "META-INF/c-1_2.tld",
        "META-INF/fmt-1_1.tld",
        "META-INF/fn-1_1.tld"
    };

    private ClassLoader classLoader;

    private URL[] tldResources;

    /**
     * Initialize with the application's ServletContext.
     * @param context        the application's servletContext
     * @param namespaceAware should the XML parser used to parse TLD files be
     *                       configured to be name space aware
     * @param validation     should the XML parser used to parse TLD files be
     *                       configured to use validation
     * @param blockExternal  should the XML parser used to parse TLD files be
     *                       configured to be block references to external
     */
    public TowTldScanner(ServletContext context, boolean namespaceAware, boolean validation, boolean blockExternal) {
        super(context, namespaceAware, validation, blockExternal);
        this.classLoader = (context != null ? context.getClassLoader() : null);
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        super.setClassLoader(classLoader);
        this.classLoader = classLoader;
    }

    /**
     * Set the TLD resources to scan.
     * @param tldResources the TLD resources
     */
    public void setTldResources(URL[] tldResources) {
        this.tldResources = tldResources;
    }

    @Override
    public void scan() throws IOException, SAXException {
        scanPlatform();
        scanJspConfig();
        scanResourcePaths("/WEB-INF/");
        scanClasspath();
        scanTldResources();

        Map<String, TldResourcePath> result = getUriTldResourcePathMap();
        if (result != null) {
            TldResourcePath aspectranPath = result.get(ASPECTRAN_TAGLIB_URI);
            if (aspectranPath != null) {
                result.putIfAbsent(LEGACY_ASPECTRAN_TAGLIB_URI, aspectranPath);
            } else {
                aspectranPath = result.get(LEGACY_ASPECTRAN_TAGLIB_URI);
                if (aspectranPath != null) {
                    result.putIfAbsent(ASPECTRAN_TAGLIB_URI, aspectranPath);
                }
            }
        }

        if (logger.isDebugEnabled() && result != null) {
            for (Map.Entry<String, TldResourcePath> entry : result.entrySet()) {
                logger.debug("Found TLD: {} [{}]", entry.getKey(), entry.getValue().getUrl());
            }
        }
    }

    /**
     * Scans the classpath for well-known tag libraries like JSTL and Aspectran.
     * @throws IOException if an I/O error occurs
     */
    private void scanClasspath() throws IOException {
        ClassLoader cl = (classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader());
        if (cl == null) {
            cl = getClass().getClassLoader();
        }
        if (cl == null) {
            return;
        }

        // Scan Aspectran Tag Library
        for (String resource : ASPECTRAN_TAGLIB_RESOURCES) {
            URL aspectranTldUrl = cl.getResource(resource);
            if (aspectranTldUrl != null) {
                parseTld(aspectranTldUrl);
                break;
            }
        }

        // Scan JSTL Tag Libraries
        Set<URL> scannedJars = new HashSet<>();
        for (String marker : JSTL_TAGLIB_MARKERS) {
            Enumeration<URL> urls = cl.getResources(marker);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (ResourceUtils.isJarURL(url) || ResourceUtils.isJarFileURL(url)) {
                    URL jarUrl = ResourceUtils.extractJarFileURL(url);
                    if (scannedJars.add(jarUrl)) {
                        try (Jar jar = JarFactory.newInstance(jarUrl)) {
                            scanJar(jar);
                        }
                    }
                } else if (ResourceUtils.isFileURL(url)) {
                    parseTld(url);
                }
            }
        }
    }

    /**
     * Scans the user-specified TLD resources.
     * @throws IOException if an I/O error occurs
     */
    private void scanTldResources() throws IOException {
        if (tldResources != null) {
            for (URL url : tldResources) {
                if (url != null) {
                    if (url.getPath().endsWith(".tld")) {
                        parseTld(url);
                    } else if (ResourceUtils.isJarURL(url) || ResourceUtils.isJarFileURL(url)) {
                        try (Jar jar = JarFactory.newInstance(url)) {
                            scanJar(jar);
                        }
                    } else if (ResourceUtils.isFileURL(url)) {
                        File dir = ResourceUtils.getFile(url);
                        if (dir.isDirectory()) {
                            scanDir(dir);
                        }
                    } else {
                        logger.warn("Unrecognized TLD Resource: {}", url);
                    }
                }
            }
        }
    }

    /**
     * Scans the given directory for JAR files.
     * @param dir the directory to scan
     */
    private void scanDir(@NonNull File dir) {
        dir.listFiles(file -> {
            if (file.isFile()) {
                String filePath = file.getAbsolutePath();
                if (filePath.toLowerCase().endsWith(ResourceUtils.JAR_FILE_EXTENSION)) {
                    try (Jar jar = JarFactory.newInstance(file.toURI().toURL())) {
                        scanJar(jar);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return false;
        });
    }

    /**
     * Parses a TLD file from a URL.
     * @param url the URL of the TLD file
     * @throws IOException if an I/O error occurs
     */
    private void parseTld(URL url) throws IOException {
        TldResourcePath tldResourcePath = new TldResourcePath(url, null);
        try {
            parseTld(tldResourcePath);
        } catch (SAXException e) {
            throw new IOException("Failed to parse TLD: " + url, e);
        }
    }

    /**
     * Scans a JAR file for TLD files.
     * @param jar the JAR file to scan
     * @throws IOException if an I/O error occurs
     */
    private void scanJar(@NonNull Jar jar) throws IOException {
        boolean found = false;
        URL jarFileUrl = jar.getJarFileURL();
        jar.nextEntry();
        for (String entryName = jar.getEntryName();
            entryName != null; jar.nextEntry(), entryName = jar.getEntryName()) {
            if (entryName.startsWith("META-INF/") && entryName.endsWith(".tld")) {
                found = true;
                TldResourcePath tldResourcePath = new TldResourcePath(jarFileUrl, null, entryName);
                try {
                    parseTld(tldResourcePath);
                } catch (SAXException e) {
                    throw new IOException("Failed to parse TLD: ", e);
                }
            }
        }
        if (!found && logger.isDebugEnabled()) {
            logger.debug("No TLDs were found in JAR [{}]", jarFileUrl);
        }
    }

}
