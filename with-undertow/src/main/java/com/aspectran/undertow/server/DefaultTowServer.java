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
package com.aspectran.undertow.server;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Undertow Server managed by Aspectran.
 *
 * @see <a href="http://undertow.io">Undertow</a>
 * @since 6.3.0
 */
public class DefaultTowServer extends AbstractTowServer implements InitializableBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTowServer.class);

    private Undertow undertow;

    @Override
    public Undertow getUndertow() {
        return undertow;
    }

    @Override
    public void doStart() throws Exception {
        try {
            undertow = buildServer();
            undertow.start();
            logger.info("Undertow {} started", TowServer.getVersion());
        } catch (Exception e) {
            if (undertow != null) {
                try {
                    undertow.stop();
                } catch (Exception ex) {
                    // ignore
                }
                undertow = null;
            }
            throw new Exception("Unable to start Undertow server", e);
        }
    }

    @Override
    public void doStop() {
        shutdown();
        if (undertow != null) {
            try {
                undertow.stop();
                logger.info("Undertow {} stopped", TowServer.getVersion());
            } catch (Exception e) {
                logger.error("Unable to stop Undertow server", e);
            }
            undertow = null;
        }
    }

    @Override
    public void initialize() throws Exception {
        if (isAutoStart() && !isRunning()) {
            start();
        }
    }

    @Override
    public void destroy() {
        if (isStoppable()) {
            try {
                stop();
            } catch (Exception e) {
                logger.error("Error stopping Undertow server", e);
            }
        }
    }

}
