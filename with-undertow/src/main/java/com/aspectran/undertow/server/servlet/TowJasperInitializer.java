/*
 * Copyright (c) 2008-2020 The Aspectran Project
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

import com.aspectran.core.component.bean.aware.ClassLoaderAware;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import org.apache.jasper.servlet.JasperInitializer;
import org.apache.jasper.servlet.TldScanner;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializer for the Jasper JSP Engine.
 */
public class TowJasperInitializer extends JasperInitializer implements ClassLoaderAware {

    private static final Logger logger = LoggerFactory.getLogger(TowJasperInitializer.class);

    private ClassLoader classLoader;

    private URL[] tldResources;

    private String[] jarsToTldScan;

    public TowJasperInitializer() {
    }

    @Override
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setTldResources(String[] resourcesToTldScan) throws IOException {
        List<URL> tldResources = new ArrayList<>();
        List<String> jarsToTldScan = new ArrayList<>();
        if (resourcesToTldScan != null) {
            for (String resource : resourcesToTldScan) {
                if (resource != null) {
                    if (resource.toLowerCase().endsWith(".jar")) {
                        jarsToTldScan.add(resource);
                    } else {
                        URL url = classLoader.getResource(resource);
                        if (url == null) {
                            throw new IOException("Resource \"" + resource + "\" not found");
                        }
                        tldResources.add(url);
                    }
                }
            }
        }
        if (!tldResources.isEmpty()) {
            this.tldResources = tldResources.toArray(new URL[0]);
        } else {
            this.tldResources = null;
        }
        if (!jarsToTldScan.isEmpty()) {
            this.jarsToTldScan = jarsToTldScan.toArray(new String[0]);
        } else {
            this.jarsToTldScan = null;
        }
    }

    @Override
    protected TldScanner newTldScanner(ServletContext context, boolean namespaceAware,
                                       boolean validate, boolean blockExternal) {
        TowTldScanner tldScanner = new TowTldScanner(context, namespaceAware, validate, blockExternal);
        if (classLoader != null) {
            tldScanner.setClassLoader(classLoader);
        }
        tldScanner.setTldResources(tldResources);
        tldScanner.setJarsToScan(jarsToTldScan);
        return tldScanner;
    }

}
