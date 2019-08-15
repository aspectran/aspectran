/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import org.apache.jasper.servlet.TldScanner;
import org.apache.tomcat.Jar;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.apache.tomcat.util.scan.JarFactory;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

/**
 * Scans for and loads Tag Library Descriptors contained in a web application.
 */
public class TowTldScanner extends TldScanner {

    private final Log log = LogFactory.getLog(TowTldScanner.class);

    private ServletContext context;

    private String[] jarsToScan;

    /**
     * Initialise with the application's ServletContext.
     *
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
        this.context = context;
    }

    public void setJarsToScan(String[] jarsToScan) {
        this.jarsToScan = jarsToScan;
    }

    @Override
    public void scan() throws IOException, SAXException {
        if (jarsToScan != null) {
            scanPlatform();
            scanJspConfig();
            scanResourcePaths("/WEB-INF/");
            for (String file : jarsToScan) {
                String realPath = context.getRealPath(file);
                if (realPath == null) {
                    throw new FileNotFoundException("In TLD scanning, the supplied resource '" + file + "' does not exist");
                }
                URL url = new File(realPath).toURI().toURL();
                Jar jar = JarFactory.newInstance(url);
                scanJar(jar, file);
            }
        } else {
            super.scan();
        }
    }

    protected void scanJar(Jar jar, String webappPath) throws IOException {
        boolean found = false;
        URL jarFileUrl = jar.getJarFileURL();
        jar.nextEntry();
        for (String entryName = jar.getEntryName();
             entryName != null;
             jar.nextEntry(), entryName = jar.getEntryName()) {
            if (!(entryName.startsWith("META-INF/") &&
                    entryName.endsWith(".tld"))) {
                continue;
            }
            found = true;
            TldResourcePath tldResourcePath =
                    new TldResourcePath(jarFileUrl, webappPath, entryName);
            try {
                parseTld(tldResourcePath);
            } catch (SAXException e) {
                throw new IOException(e);
            }
        }
        if (found) {
            if (log.isDebugEnabled()) {
                log.debug("TLD files were found in JAR [" + jarFileUrl + "]");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No TLD files were found in [" + jarFileUrl + "]");
            }
        }
    }

}
