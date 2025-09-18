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
package com.aspectran.jetty.server.session;

import com.aspectran.utils.Assert;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.eclipse.jetty.session.DefaultSessionCache;
import org.eclipse.jetty.session.SessionCache;
import org.eclipse.jetty.session.SessionDataStore;
import org.eclipse.jetty.session.SessionDataStoreFactory;

/**
 * An extended version of Jetty's {@link SessionHandler}.
 */
public class JettySessionHandler extends SessionHandler {

    public JettySessionHandler() {
        super();
    }

    public void setSessionDataStore(SessionDataStoreFactory sessionDataStoreFactory) throws Exception {
        Assert.notNull(sessionDataStoreFactory, "sessionDataStoreFactory must not be null");
        SessionDataStore sessionDataStore = sessionDataStoreFactory.getSessionDataStore(this);
        if (getSessionCache() != null) {
            getSessionCache().setSessionDataStore(sessionDataStore);
        } else {
            SessionCache sessionCache = new DefaultSessionCache(this);
            sessionCache.setSessionDataStore(sessionDataStore);
            setSessionCache(sessionCache);
        }
    }

}
