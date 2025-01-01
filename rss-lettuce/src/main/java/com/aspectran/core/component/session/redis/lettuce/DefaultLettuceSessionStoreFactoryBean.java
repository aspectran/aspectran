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
package com.aspectran.core.component.session.redis.lettuce;

import com.aspectran.core.component.bean.ablility.FactoryBean;

/**
 * <p>Created: 2024. 12. 26.</p>
 */
public class DefaultLettuceSessionStoreFactoryBean
        extends DefaultLettuceSessionStoreFactory implements FactoryBean<DefaultLettuceSessionStore> {

    @Override
    public DefaultLettuceSessionStore getObject() throws Exception {
        return createSessionStore();
    }

}
