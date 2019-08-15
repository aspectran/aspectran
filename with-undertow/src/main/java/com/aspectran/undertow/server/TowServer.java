/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;

/**
 * The Undertow Server managed by Aspectran.
 *
 * @since 6.3.0
 */
public class TowServer implements InitializableBean, DisposableBean {

    private static final Log log = LogFactory.getLog(TowServer.class);

    private final Object monitor = new Object();

    private final Undertow.Builder builder = Undertow.builder();

    private Undertow server;

    private boolean autoStart;

    public void setListeners(Undertow.ListenerBuilder[] listenerBuilders) {
        for (Undertow.ListenerBuilder listenerBuilder : listenerBuilders) {
            builder.addListener(listenerBuilder);
        }
    }

    public void setHandler(HttpHandler handler) {
        builder.setHandler(handler);
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    public void start() throws Exception {
        synchronized (monitor) {
            log.info("Starting embedded Undertow server");
            server = builder.build();
            server.start();
            log.info("Undertow server started");
        }
    }

    public void stop() {
        synchronized (monitor) {
            log.info("Stopping embedded Undertow server");
            try {
                if (server != null) {
                    server.stop();
                    server = null;
                }
            } catch (Exception e) {
                log.error("Unable to stop embedded Undertow server", e);
            }
        }
    }

    @Override
    public void initialize() throws Exception {
        if (autoStart) {
            start();
        }
    }

    @Override
    public void destroy() {
        try {
            stop();
        } catch (Exception e) {
            log.error("Error while stopping Undertow server: " + e.getMessage(), e);
        }
    }

}
