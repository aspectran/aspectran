/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.jetty.server.servlet;

import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import jakarta.servlet.ServletContext;
import org.apache.jasper.servlet.TldScanner;
import org.apache.tomcat.Jar;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.apache.tomcat.util.scan.JarFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * Scans for and loads Tag Library Descriptors contained in a web application.
 */
public class JettyTldScanner extends TldScanner {

    private final Logger logger = LoggerFactory.getLogger(JettyTldScanner.class);

    private URL[] tldResources;

    /**
     * Initialise with the application's ServletContext.
     * @param context        the application's servletContext
     * @param namespaceAware should the XML parser used to parse TLD files be
     *                       configured to be name space aware
     * @param validation     should the XML parser used to parse TLD files be
     *                       configured to use validation
     * @param blockExternal  should the XML parser used to parse TLD files be
     *                       configured to be block references to external
     */
    public JettyTldScanner(ServletContext context, boolean namespaceAware, boolean validation, boolean blockExternal) {
        super(context, namespaceAware, validation, blockExternal);
    }

    public void setTldResources(URL[] tldResources) {
        this.tldResources = tldResources;
    }

    @Override
    public void scan() throws IOException, SAXException {
        scanPlatform();
        scanJspConfig();
        scanResourcePaths("/WEB-INF/");
        if (tldResources != null) {
            for (URL url : tldResources) {
                if (url != null) {
                    if (url.getPath().endsWith(".tld")) {
                        parseTld(url);
                    } else if (ResourceUtils.isJarURL(url) || ResourceUtils.isJarFileURL(url)) {
                        Jar jar = JarFactory.newInstance(url);
                        scanJar(jar);
                    } else if (ResourceUtils.isFileURL(url)) {
                        File dir = ResourceUtils.getFile(url);
                        if (dir.isDirectory()) {
                            scanDir(dir);
                        }
                    } else {
                        logger.warn("Unrecognized TLD Resource: " + url);
                    }
                }
            }
        }
        Map<String, TldResourcePath> result = getUriTldResourcePathMap();
        for (Map.Entry<String, TldResourcePath> entry : result.entrySet()) {
            logger.debug("Found TLD: " + entry.getKey() + " [" + entry.getValue().getUrl() + "]");
        }
    }

    private void scanDir(@NonNull File dir) {
        dir.listFiles(file -> {
            if (file.isFile()) {
                String filePath = file.getAbsolutePath();
                if (filePath.toLowerCase().endsWith(ResourceUtils.JAR_FILE_EXTENSION)) {
                    try {
                        Jar jar = JarFactory.newInstance(file.toURI().toURL());
                        scanJar(jar);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return false;
        });
    }

    private void parseTld(URL url) throws IOException {
        TldResourcePath tldResourcePath = new TldResourcePath(url, null);
        try {
            parseTld(tldResourcePath);
        } catch (SAXException e) {
            throw new IOException("Failed to parse TLD: " + url, e);
        }
    }

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
            logger.debug("No TLDs were found in JAR [" + jarFileUrl + "]");
        }
    }

}
