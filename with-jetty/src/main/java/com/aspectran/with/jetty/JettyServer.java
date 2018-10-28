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
package com.aspectran.with.jetty;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionDataStoreFactory;
import org.eclipse.jetty.util.thread.ThreadPool;

/**
 * The Jetty Server managed by Aspectran.
 *
 * <p>Created: 2016. 12. 22.</p>
 */
public class JettyServer extends Server implements InitializableBean, DisposableBean {

    private static final Log log = LogFactory.getLog(JettyServer.class);

    private boolean autoStart;

    private SessionDataStoreFactory sessionDataStoreFactory;

    public JettyServer() {
        super();
    }

    public JettyServer(int port) {
        super(port);
    }

    public JettyServer(ThreadPool pool) {
        super(pool);
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public SessionDataStoreFactory getSessionDataStoreFactory() {
        return sessionDataStoreFactory;
    }

    public void setSessionDataStoreFactory(SessionDataStoreFactory sessionDataStoreFactory) {
        this.sessionDataStoreFactory = sessionDataStoreFactory;
    }

    public void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    @Override
    public void initialize() throws Exception {
        synchronized (this) {
            if (sessionDataStoreFactory != null) {
                addBean(sessionDataStoreFactory);
            }
            if (autoStart) {
                start();
            }
        }
    }

    @Override
    public void destroy() {
        synchronized (this) {
            try {
                stop();
                if (sessionDataStoreFactory instanceof DisposableBean) {
                    ((DisposableBean)sessionDataStoreFactory).destroy();
                }
            } catch (Exception e) {
                log.error("JettyServer shutdown failed", e);
            }
        }
    }

}
