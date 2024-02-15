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
package com.aspectran.jetty.server;

import com.aspectran.utils.annotation.jsr305.NonNull;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.eclipse.jetty.session.DefaultSessionCache;
import org.eclipse.jetty.session.SessionCache;
import org.eclipse.jetty.session.SessionCacheFactory;
import org.eclipse.jetty.session.SessionDataStoreFactory;

/**
 * Extended Jetty's SessionHandler.
 */
public class JettySessionHandler extends SessionHandler {

    public JettySessionHandler() {
        super();
    }

    public void setSessionCache(@NonNull SessionCacheFactory sessionCacheFactory) {
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
