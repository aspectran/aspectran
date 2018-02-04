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

    private final URL[] resources;

    private Map<String, Long> modifiedTimeMap = new HashMap<>();

    private boolean modified = false;

    private int cycle;

    public ActivityContextReloadingTimerTask(ServiceController serviceController, URL[] resources) {
        this.serviceController = serviceController;
        this.resources = resources;
    }

    @Override
    public void run() {
        if (resources == null || modified) {
            return;
        }

        for (URL url : resources) {
            try {
                File file = new File(url.toURI());
                String filePath = file.getAbsolutePath();
                long modifiedTime = file.lastModified();

                if (cycle == 0) {
                    modifiedTimeMap.put(filePath, modifiedTime);
                } else {
                    Long modifiedTime2 = modifiedTimeMap.get(filePath);
                    if (modifiedTime2 != null) {
                        if (modifiedTime != modifiedTime2) {
                            modified = true;
                            if (debugEnabled) {
                                log.debug("Detected modified file: " + url);
                            }
                            break;
                        }
                    }
                }
            } catch (URISyntaxException e) {
                log.error(e.getMessage(), e);
            }

            cycle++;
        }

        if (modified) {
            try {
                serviceController.restart();
            } catch (Exception e) {
                throw new AspectranRuntimeException(e);
            }
            modified = false;
        }
    }

}

