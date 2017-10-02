package com.aspectran.with.jetty;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;

/**
 * <p>Created: 2016. 12. 22.</p>
 */
public class JettyServer extends Server implements InitializableBean, DisposableBean {

    private static final Log log = LogFactory.getLog(JettyServer.class);

    public JettyServer() {
        super();
    }

    public JettyServer(int port) {
        super(port);
    }

    public JettyServer(ThreadPool pool) {
        super(pool);
    }

    @Override
    public void initialize() throws Exception {
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "false");
    }

    @Override
    public void destroy() {
        try {
            stop();
        } catch (Exception e) {
            log.error("JettyServer shutdown failed", e);
        }
    }

}
