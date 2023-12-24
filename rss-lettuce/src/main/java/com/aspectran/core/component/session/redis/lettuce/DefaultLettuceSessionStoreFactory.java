/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.component.session.redis.lettuce;

import com.aspectran.core.component.session.AbstractSessionStoreFactory;
import com.aspectran.core.component.session.SessionStore;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;

/**
 * Factory that creates a Redis-based session store using Lettuce as a client.
 *
 * <p>Created: 2019/12/06</p>
 *
 * @since 6.6.0
 */
public class DefaultLettuceSessionStoreFactory extends AbstractSessionStoreFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultLettuceSessionStoreFactory.class);

    private RedisConnectionPoolConfig poolConfig;

    public DefaultLettuceSessionStoreFactory() {
        super();
    }

    public void setPoolConfig(RedisConnectionPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    @Override
    public SessionStore getSessionStore() {
        if (logger.isDebugEnabled()) {
            logger.debug("RedisConnectionPoolConfig " + poolConfig);
        }
        RedisConnectionPool pool = new RedisConnectionPool(poolConfig);
        DefaultLettuceSessionStore sessionStore = new DefaultLettuceSessionStore(pool);
        if (getNonPersistentAttributes() != null) {
            sessionStore.setNonPersistentAttributes(getNonPersistentAttributes());
        }
        return sessionStore;
    }

}
