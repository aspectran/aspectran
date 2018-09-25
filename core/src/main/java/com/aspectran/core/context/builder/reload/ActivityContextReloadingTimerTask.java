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
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class ActivityContextReloadingTimerTask extends TimerTask {

    private final Log log = LogFactory.getLog(ActivityContextReloadingTimerTask.class);

    private final boolean debugEnabled = log.isDebugEnabled();

    private final ServiceController serviceController;

    private URL[] resources;

    private Map<String, Long> modifiedTimeMap = new HashMap<>();

    private boolean modified = false;

    public ActivityContextReloadingTimerTask(ServiceController serviceController) {
        this.serviceController = serviceController;
    }

    public void setResources(URL[] resources) {
        this.resources = resources;
        if (resources != null) {
            for (URL url : resources) {
                try {
                    File file = new File(url.toURI());
                    String filePath = file.getAbsolutePath();
                    modifiedTimeMap.put(filePath, file.lastModified());
                } catch (URISyntaxException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void run() {
        if (resources == null) {
            return;
        }
        if (modified) {
            if (!serviceController.isBusy()) {
                restartService();
            } else {
                return;
            }
        }
        for (URL url : resources) {
            try {
                File file = new File(url.toURI());
                String filePath = file.getAbsolutePath();
                long modifiedTime = file.lastModified();
                Long modifiedTime2 = modifiedTimeMap.get(filePath);
                if (modifiedTime2 != null && modifiedTime2 != modifiedTime) {
                    modified = true;
                    modifiedTimeMap.put(filePath, modifiedTime);
                    if (debugEnabled) {
                        log.debug("Detected modified file: " + url);
                    }
                }
            } catch (URISyntaxException e) {
                log.error(e.getMessage(), e);
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

