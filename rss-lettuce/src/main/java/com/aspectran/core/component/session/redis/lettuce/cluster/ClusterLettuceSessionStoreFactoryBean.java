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
package com.aspectran.core.component.session.redis.lettuce.cluster;

import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;

/**
 * FactoryBean variant of {@link ClusterLettuceSessionStoreFactory} that eagerly creates
 * and exposes a singleton {@link ClusterLettuceSessionStore} instance.
 *
 * <p>Created: 2024. 12. 26.</p>
 */
public class ClusterLettuceSessionStoreFactoryBean extends ClusterLettuceSessionStoreFactory
        implements InitializableFactoryBean<ClusterLettuceSessionStore> {

    private ClusterLettuceSessionStore sessionStore;

    /**
     * Initialize the session store.
     * @throws Exception if an error occurs during initialization
     */
    @Override
    public void initialize() throws Exception {
        if (sessionStore == null) {
            sessionStore = createSessionStore();
        }
    }

    /**
     * Return an instance (possibly shared or independent) of the object managed by this factory.
     * @return an instance of the session store
     */
    @Override
    public ClusterLettuceSessionStore getObject() {
        return sessionStore;
    }

}
