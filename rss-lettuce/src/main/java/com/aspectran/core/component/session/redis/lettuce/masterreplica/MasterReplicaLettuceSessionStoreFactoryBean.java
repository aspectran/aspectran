/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.core.component.session.redis.lettuce.masterreplica;

import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;

/**
 * <p>Created: 2024. 12. 26.</p>
 */
public class MasterReplicaLettuceSessionStoreFactoryBean extends MasterReplicaLettuceSessionStoreFactory
        implements InitializableFactoryBean<MasterReplicaLettuceSessionStore> {

    private MasterReplicaLettuceSessionStore sessionStore;

    @Override
    public void initialize() throws Exception {
        if (sessionStore == null) {
            sessionStore = createSessionStore();
        }
    }

    @Override
    public MasterReplicaLettuceSessionStore getObject() {
        return sessionStore;
    }

}
