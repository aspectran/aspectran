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
