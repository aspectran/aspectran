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
package com.aspectran.core.context.builder.reload;

import com.aspectran.core.context.AspectranRuntimeException;
import com.aspectran.core.service.ServiceController;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import static com.aspectran.core.util.ResourceUtils.JAR_URL_SEPARATOR;
import static com.aspectran.core.util.ResourceUtils.URL_PROTOCOL_JAR;

public class ActivityContextReloadingTimerTask extends TimerTask {

    private final Log log = LogFactory.getLog(ActivityContextReloadingTimerTask.class);

    private final boolean debugEnabled = log.isDebugEnabled();

    private final ServiceController serviceController;

    private Map<String, Long> modifiedTimeMap = new HashMap<>();

    private boolean modified = false;

    public ActivityContextReloadingTimerTask(ServiceController serviceController) {
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
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void run() {
        if (modifiedTimeMap.isEmpty()) {
            return;
        }
        if (modified) {
            if (!serviceController.isBusy()) {
                restartService();
            } else {
                return;
            }
        }
        for (Map.Entry<String, Long> entry : modifiedTimeMap.entrySet()) {
            String filePath = entry.getKey();
            long prevLastModifiedTime = entry.getValue();
            File file = new File(filePath);
            long lastModifiedTime = file.lastModified();
            if (prevLastModifiedTime != lastModifiedTime) {
                modified = true;
                modifiedTimeMap.put(filePath, lastModifiedTime);
                if (debugEnabled) {
                    log.debug("Detected modified file: " + filePath);
                }
            }
        }
        if (modified) {
            if (!serviceController.isBusy()) {
                restartService();
            }
        }
    }

    private void restartService() {
        try {
            String message = "Some resource file changes have been detected.";
            serviceController.restart(message);
        } catch (Exception e) {
            throw new AspectranRuntimeException(e);
        } finally {
            modified = false;
        }
    }

}

