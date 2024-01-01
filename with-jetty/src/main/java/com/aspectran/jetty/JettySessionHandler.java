/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.jetty;

import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionCacheFactory;
import org.eclipse.jetty.server.session.SessionDataStoreFactory;
import org.eclipse.jetty.server.session.SessionHandler;

/**
 * Extended Jetty's SessionHandler.
 */
public class JettySessionHandler extends SessionHandler {

    public JettySessionHandler() {
        super();
    }

    public void setSessionCache(SessionCacheFactory sessionCacheFactory) {
        setSessionCache(sessionCacheFactory.getSessionCache(this));
    }

    public void setSessionDataStore(SessionDataStoreFactory sessionDataStoreFactory) throws Exception {
        SessionCache sessionCache = getSessionCache();
        if (sessionCache != null) {
            sessionCache.setSessionDataStore(sessionDataStoreFactory.getSessionDataStore(this));
        } else {
            sessionCache = new DefaultSessionCache(this);
            sessionCache.setSessionDataStore(sessionDataStoreFactory.getSessionDataStore(this));
            setSessionCache(sessionCache);
        }
    }

}
