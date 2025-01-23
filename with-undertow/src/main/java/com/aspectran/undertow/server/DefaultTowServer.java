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
package com.aspectran.undertow.server;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import io.undertow.Undertow;

/**
 * The Undertow Server managed by Aspectran.
 *
 * @see <a href="http://undertow.io">Undertow</a>
 * @since 6.3.0
 */
public class DefaultTowServer extends AbstractTowServer implements InitializableBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTowServer.class);

    private Undertow server;

    @Override
    public void doStart() throws Exception {
        try {
            server = buildServer();
            server.start();
            logger.info("Undertow " + TowServer.getVersion() + " started");
        } catch (Exception e) {
            if (server != null) {
                try {
                    server.stop();
                } catch (Exception ex) {
                    // ignore
                }
                server = null;
            }
            throw new Exception("Unable to start Undertow server", e);
        }
    }

    @Override
    public void doStop() {
        shutdown();
        if (server != null) {
            try {
                server.stop();
                logger.info("Undertow " + TowServer.getVersion() + " stopped");
            } catch (Exception e) {
                logger.error("Unable to stop Undertow server", e);
            }
            server = null;
        }
    }

    @Override
    public void initialize() throws Exception {
        if (isAutoStart()) {
            start();
        }
    }

    @Override
    public void destroy() {
        try {
            stop();
        } catch (Exception e) {
            logger.error("Error while stopping Undertow server", e);
        }
    }

}
