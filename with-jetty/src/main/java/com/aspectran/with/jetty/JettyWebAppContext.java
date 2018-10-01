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
package com.aspectran.with.jetty;

import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.service.WebService;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * The Class JettyWebAppContext.
 *
 * <p>Created: 2017. 1. 27.</p>
 */
public class JettyWebAppContext extends WebAppContext implements ActivityContextAware, InitializableBean {

    private static final Log log = LogFactory.getLog(JettyWebAppContext.class);

    private ActivityContext context;

    private boolean standalone;

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    public void setTempDirectory(String tempDirectory) {
        File tempDir = null;
        try {
            tempDir = new File(tempDirectory);
            if (!tempDir.exists()) {
                if (!tempDir.mkdirs()) {
                    throw new IOException("Unable to create scratch directory: " + tempDir);
                }
            }
            super.setTempDirectory(tempDir);
        } catch (Exception e) {
            log.error("Failed to establish Scratch directory: " + tempDir, e);
        }
    }

    public void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }

    @Override
    public void initialize() throws Exception {
        if (context == null) {
            throw new IllegalStateException();
        }

        ClassLoader parent = context.getEnvironment().getClassLoader();
        WebAppClassLoader webAppClassLoader = new WebAppClassLoader(parent, this);
        setClassLoader(webAppClassLoader);

        /*
         * Configure the application to support the compilation of JSP files.
         * We need a new class loader and some stuff so that Jetty can call the
         * onStartup() methods as required.
         */
        setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
        setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
        addBean(new ServletContainerInitializersStarter(this), true);

        if (!standalone) {
            CoreService rootService = context.getRootService();
            WebService webService = WebService.create(getServletContext(), rootService);
            webService.start();

            setAttribute(WebService.ROOT_WEB_SERVICE_ATTRIBUTE, webService);
        }
    }

    private List<ContainerInitializer> jspInitializers() {
        JettyJasperInitializer sci = new JettyJasperInitializer();
        ContainerInitializer initializer = new ContainerInitializer(sci, null);
        return Collections.singletonList(initializer);
    }

}
