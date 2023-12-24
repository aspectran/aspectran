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
package com.aspectran.core.context.builder.reload;

import com.aspectran.core.service.ServiceController;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import static com.aspectran.utils.ResourceUtils.JAR_URL_SEPARATOR;
import static com.aspectran.utils.ResourceUtils.URL_PROTOCOL_JAR;

public class ContextReloadingTimerTask extends TimerTask {

    private static final Logger logger = LoggerFactory.getLogger(ContextReloadingTimerTask.class);

    private final ServiceController serviceController;

    private final Map<String, Long> modifiedTimeMap = new HashMap<>();

    private boolean modified = false;

    public ContextReloadingTimerTask(ServiceController serviceController) {
        this.serviceController = serviceController;
    }

    public void setResources(Enumeration<URL> resources) {
        if (resources != null) {
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try {
                    File file;
                    if (URL_PROTOCOL_JAR.equals(url.getProtocol())) {
                        URL fileUrl = new URL(url.getFile());
                        String[] parts = StringUtils.split(fileUrl.getFile(), JAR_URL_SEPARATOR);
                        file = new File(parts[0]);
                    } else {
                        file = new File(url.getFile());
                    }
                    String filePath = file.getAbsolutePath();
                    modifiedTimeMap.put(filePath, file.lastModified());
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
    }

    @Override
    public void run() {
        if (modified || modifiedTimeMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Long> entry : modifiedTimeMap.entrySet()) {
            String filePath = entry.getKey();
            long prevLastModifiedTime = entry.getValue();
            File file = new File(filePath);
            long lastModifiedTime = file.lastModified();
            if (prevLastModifiedTime != lastModifiedTime) {
                modified = true;
                modifiedTimeMap.put(filePath, lastModifiedTime);
                if (logger.isDebugEnabled()) {
                    logger.debug("Detected modified resource: " + filePath);
                }
            }
        }
        if (modified) {
            restartService();
        }
    }

    private void restartService() {
        try {
            String message = "Some resource file changes have been detected.";
            serviceController.restart(message);
        } catch (Exception e) {
            // ignore
        } finally {
            modified = false;
        }
    }

}
