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
package com.aspectran.daemon;

import com.aspectran.daemon.service.DaemonService;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import java.io.File;

/**
 * Start and Stop Aspectran Daemon using Jsvc.
 *
 * <p>Created: 2017. 12. 11.</p>
 *
 * @since 5.1.0
 */
public class JsvcDaemon implements Daemon {

    private DefaultDaemon defaultDaemon;

    @Override
    public void init(DaemonContext daemonContext) throws DaemonInitException {
        if (defaultDaemon == null) {
            String[] args = daemonContext.getArguments();
            File aspectranConfigFile;
            if (args != null && args.length > 0) {
                aspectranConfigFile = DaemonService.determineAspectranConfigFile(args[0]);
            } else {
                aspectranConfigFile = DaemonService.determineAspectranConfigFile(null);
            }

            try {
                defaultDaemon = new DefaultDaemon();
                defaultDaemon.init(aspectranConfigFile);
            } catch (Exception e) {
                e.printStackTrace();
                throw new DaemonInitException("Aspectran Daemon failed to initialize", e);
            }
        }
    }

    @Override
    public void start() throws Exception {
        if (defaultDaemon != null) {
            defaultDaemon.start();
        }
    }

    @Override
    public void stop() {
        if (defaultDaemon != null) {
            defaultDaemon.stop();
        }
    }

    @Override
    public void destroy() {
        if (defaultDaemon != null) {
            defaultDaemon.destroy();
            defaultDaemon = null;
        }
    }

}
