/*
 * Copyright (c) 2008-2017 The Aspectran Project
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
package com.aspectran.daemon;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.DaemonConfig;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.util.apon.AponReader;
import com.aspectran.daemon.command.DaemonCommander;
import com.aspectran.daemon.service.DaemonService;

import java.io.File;

/**
 * <p>Created: 2017. 12. 11.</p>
 *
 * @since 5.1.0
 */
public class AbstractDaemon implements Daemon {

    private DaemonService service;

    private DaemonCommander commander;

    public DaemonService getService() {
        return service;
    }

    protected void init(File aspectranConfigFile) throws Exception {
        try {
            AspectranConfig aspectranConfig = new AspectranConfig();
            try {
                AponReader.parse(aspectranConfigFile, aspectranConfig);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to parse aspectran config file: " +
                        aspectranConfigFile, e);
            }

            DaemonConfig daemonConfig = aspectranConfig.touchDaemonConfig();


            service = DaemonService.create(aspectranConfig);
            service.start();

        } catch (Exception e) {
            throw new Exception("Failed to initialize daemon", e);
        }
    }

    protected void destroy() {
        if (service != null) {
            service.stop();
        }
    }

}
