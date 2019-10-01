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
import com.aspectran.core.util.lifecycle.AbstractLifeCycle;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import org.xnio.Option;
import org.xnio.OptionMap;

import java.io.IOException;

/**
 * The Undertow Server managed by Aspectran.
 *
 * @see <a href="http://undertow.io">Undertow</a>
 * @since 6.3.0
 */
public class TowServer extends AbstractLifeCycle implements InitializableBean, DisposableBean {

    private static final Log log = LogFactory.getLog(TowServer.class);

    private final Undertow.Builder builder = Undertow.builder();

    private Undertow server;

    private boolean autoStart;

    private int shutdownDelay;

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public void setShutdownDelay(int shutdownDelay) {
        this.shutdownDelay = shutdownDelay;
    }

    public void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    public void setHttpListeners(HttpListenerConfig... httpListenerConfigs) {
        if (httpListenerConfigs == null) {
            throw new IllegalArgumentException("httpListenerConfigs must not be null");
        }
        for (HttpListenerConfig listenerConfig : httpListenerConfigs) {
            builder.addListener(listenerConfig.getListenerBuilder());
        }
    }

    public void setHttpsListeners(HttpsListenerConfig... httpsListenerConfigs) throws IOException {
        if (httpsListenerConfigs == null) {
            throw new IllegalArgumentException("httpsListenerConfigs must not be null");
        }
        for (HttpsListenerConfig listenerConfig : httpsListenerConfigs) {
            builder.addListener(listenerConfig.getListenerBuilder());
        }
    }

    public void setAjpListeners(AjpListenerConfig... ajpListenerConfigs) {
        if (ajpListenerConfigs == null) {
            throw new IllegalArgumentException("ajpListenerConfigs must not be null");
        }
        for (AjpListenerConfig listenerConfig : ajpListenerConfigs) {
            builder.addListener(listenerConfig.getListenerBuilder());
        }
    }

    public void setHandler(HttpHandler handler) {
        builder.setHandler(handler);
    }

    public void setBufferSize(int bufferSize) {
        builder.setBufferSize(bufferSize);
    }

    public void setIoThreads(int ioThreads) {
        builder.setIoThreads(ioThreads);
    }

    public void setWorkerThreads(int workerThreads) {
        builder.setWorkerThreads(workerThreads);
    }

    public void setDirectBuffers(final boolean directBuffers) {
        builder.setDirectBuffers(directBuffers);
    }

    public <T> void setServerOption(final Option<T> option, final T value) {
        builder.setServerOption(option, value);
    }

    public <T> void setSocketOption(final Option<T> option, final T value) {
        builder.setSocketOption(option, value);
    }

    public <T> void setWorkerOption(final Option<T> option, final T value) {
        builder.setWorkerOption(option, value);
    }

    public Undertow.Builder getBuilder() {
        return builder;
    }

    @SuppressWarnings("unchecked")
    public void setServerOptions(TowOptions options) {
        if (options != null) {
            OptionMap optionMap = options.getOptionMap();
            for (Option option : optionMap) {
                builder.setServerOption(option, optionMap.get(option));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void setSocketOptions(TowOptions options) {
        if (options != null) {
            OptionMap optionMap = options.getOptionMap();
            for (Option option : optionMap) {
                builder.setSocketOption(option, optionMap.get(option));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void setWorkerOptions(TowOptions options) {
        if (options != null) {
            OptionMap optionMap = options.getOptionMap();
            for (Option option : optionMap) {
                builder.setWorkerOption(option, optionMap.get(option));
            }
        }
    }

    public void doStart() throws Exception {
        try {
            server = builder.build();
            server.start();
            log.info("Undertow server started");
        } catch (Exception e) {
            try {
                if (server != null) {
                    server.stop();
                    server = null;
                }
            } catch (Exception ex) {
                // ignore
            }
            throw new Exception("Unable to start Undertow server", e);
        }
    }

    public void doStop() {
        try {
            if (server != null) {
                server.stop();
                server = null;
                log.info("Stopped Undertow server");
            }
        } catch (Exception e) {
            log.error("Unable to stop Undertow server", e);
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
            if (shutdownDelay > 0) {
                Thread.sleep(shutdownDelay);
            }
            stop();
        } catch (Exception e) {
            log.error("Error while stopping Undertow server: " + e.getMessage(), e);
        }
    }

}
