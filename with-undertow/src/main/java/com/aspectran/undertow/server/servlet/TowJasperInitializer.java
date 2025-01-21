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
package com.aspectran.undertow.server.servlet;

import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import jakarta.servlet.ServletContext;
import org.apache.jasper.servlet.JasperInitializer;
import org.apache.jasper.servlet.TldScanner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.aspectran.utils.ResourceUtils.CLASSPATH_URL_PREFIX;

/**
 * Initializer for the Jasper JSP Engine.
 */
public class TowJasperInitializer extends JasperInitializer implements ActivityContextAware {

    private static final Logger logger = LoggerFactory.getLogger(TowJasperInitializer.class);

    private ActivityContext context;

    private URL[] tldResources;

    public TowJasperInitializer() {
    }

    @Override
    @AvoidAdvice
    public void setActivityContext(@NonNull ActivityContext context) {
        this.context = context;
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
        tldScanner.setClassLoader(context.getClassLoader());
        tldScanner.setTldResources(tldResources);
        return tldScanner;
    }

    private URL getURL(@NonNull String resourceLocation) throws FileNotFoundException {
        try {
            if (resourceLocation.startsWith(CLASSPATH_URL_PREFIX)) {
                return ResourceUtils.getURL(resourceLocation, context.getClassLoader());
            } else {
                return context.getApplicationAdapter().getRealPath(resourceLocation).toUri().toURL();
            }
        } catch (IOException ex) {
            throw new FileNotFoundException("In TLD scanning, the supplied resource '" +
                    resourceLocation + "' does not exist");
        }
    }

}
