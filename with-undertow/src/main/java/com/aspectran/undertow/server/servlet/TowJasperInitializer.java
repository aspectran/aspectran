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
package com.aspectran.undertow.server.servlet;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import org.apache.jasper.servlet.JasperInitializer;
import org.apache.jasper.servlet.TldScanner;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.aspectran.core.util.ResourceUtils.CLASSPATH_URL_PREFIX;

/**
 * Initializer for the Jasper JSP Engine.
 */
public class TowJasperInitializer extends JasperInitializer implements ApplicationAdapterAware {

    private static final Logger logger = LoggerFactory.getLogger(TowJasperInitializer.class);

    private ApplicationAdapter applicationAdapter;

    private URL[] tldResources;

    public TowJasperInitializer() {
    }

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    public void setTldResources(String[] resourcesToTldScan) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Specified TLD resources: " + Arrays.toString(resourcesToTldScan));
        }
        List<URL> tldResources = new ArrayList<>();
        if (resourcesToTldScan != null) {
            for (String resource : resourcesToTldScan) {
                if (resource != null) {
                    tldResources.add(getURL(resource));
                }
            }
        }
        if (!tldResources.isEmpty()) {
            this.tldResources = tldResources.toArray(new URL[0]);
        } else {
            this.tldResources = null;
        }
    }

    @Override
    protected TldScanner newTldScanner(ServletContext context, boolean namespaceAware,
                                       boolean validate, boolean blockExternal) {
        TowTldScanner tldScanner = new TowTldScanner(context, namespaceAware, validate, blockExternal);
        tldScanner.setClassLoader(applicationAdapter.getClassLoader());
        tldScanner.setTldResources(tldResources);
        return tldScanner;
    }

    private URL getURL(String resourceLocation) throws FileNotFoundException {
        try {
            if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
                return ResourceUtils.getURL(resourceLocation, applicationAdapter.getClassLoader());
            } else {
                File file = applicationAdapter.toRealPathAsFile(resourceLocation);
                return file.toURI().toURL();
            }
        } catch (IOException ex) {
            throw new FileNotFoundException("In TLD scanning, the supplied resource '" +
                    resourceLocation + "' does not exist");
        }
    }

}
