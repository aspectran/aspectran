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
package com.aspectran.core.context.builder.reload;

import com.aspectran.core.service.ServiceLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static com.aspectran.utils.ResourceUtils.URL_PROTOCOL_JAR;

/**
 * A {@link Runnable} that detects changes in configuration and resource files
 * to trigger a context reload.
 * <p>It periodically checks the {@code lastModified} timestamp of registered resource files.
 * If a change is detected, it triggers a service restart via the {@link ServiceLifeCycle} interface.</p>
 *
 * @since 6.3.0
 */
public class ContextReloadingTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ContextReloadingTask.class);

    private final ServiceLifeCycle serviceLifeCycle;

    private final Map<String, Long> modifiedTimeMap = new HashMap<>();

    private boolean changeDetectedInPreviousRun;

    private boolean restarting;

    /**
     * Instantiates a new ContextReloadingTask.
     * @param serviceLifeCycle the service life cycle to be controlled for restarts
     */
    public ContextReloadingTask(ServiceLifeCycle serviceLifeCycle) {
        this.serviceLifeCycle = serviceLifeCycle;
    }

    /**
     * Sets the classpath resources to be monitored for changes.
     * @param resources an enumeration of resource URLs, typically from a classloader
     */
    public void setResources(Enumeration<URL> resources) {
        if (resources != null) {
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try {
                    File file;
                    if (URL_PROTOCOL_JAR.equals(url.getProtocol())) {
                        JarURLConnection conn = (JarURLConnection)url.openConnection();
                        URL jarFileUrl = conn.getJarFileURL();
                        file = new File(jarFileUrl.toURI());
                    } else {
                        file = new File(url.toURI());
                    }
                    addResource(file);
                } catch (Exception e) {
                    logger.error("Failed to inspect resource for context reloading: {}", url, e);
                }
            }
        }
    }

    /**
     * Adds a file-system based resource to be monitored for changes.
     * @param file the resource file to monitor
     */
    public void addResource(File file) {
        if (file != null && file.exists()) {
            modifiedTimeMap.put(file.getAbsolutePath(), file.lastModified());
        }
    }

    /**
     * Returns whether there are any resources to monitor for current changes.
     * @return {@code true} if one or more monitored resources exist, otherwise {@code false}.
     */
    public boolean hasResources() {
        return !modifiedTimeMap.isEmpty();
    }

    /**
     * Executes the check for modified resources. If a change is detected,
     * it triggers a service restart.
     */
    @Override
    public void run() {
        if (restarting || modifiedTimeMap.isEmpty()) {
            return;
        }

        boolean changed = hasChanged();

        if (changed) {
            changeDetectedInPreviousRun = true;
        } else {
            if (changeDetectedInPreviousRun) {
                restarting = true;
                changeDetectedInPreviousRun = false;
                restartService();
            }
        }
    }

    private boolean hasChanged() {
        boolean changed = false;
        for (Map.Entry<String, Long> entry : modifiedTimeMap.entrySet()) {
            String filePath = entry.getKey();
            long prevLastModifiedTime = entry.getValue();
            File file = new File(filePath);
            long lastModifiedTime = file.lastModified();
            if (prevLastModifiedTime != lastModifiedTime) {
                entry.setValue(lastModifiedTime);
                changed = true;
                if (logger.isDebugEnabled()) {
                    if (lastModifiedTime > 0) {
                        logger.debug("Detected modified resource: {}", filePath);
                    } else {
                        logger.debug("Detected deleted resource: {}", filePath);
                    }
                }
            }
        }
        return changed;
    }

    /**
     * Restarts the service through the {@link ServiceLifeCycle} interface.
     * This method is called internally when a file modification is detected.
     */
    private void restartService() {
        try {
            String message = "Resource file changes have been detected; restarting after a quiet period.";
            serviceLifeCycle.restart(message);
        } catch (Exception e) {
            // ignore
        } finally {
            restarting = false;
        }
    }

}
