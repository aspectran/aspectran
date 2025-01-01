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
package com.aspectran.core.component.bean.scope;

import com.aspectran.core.context.rule.type.ScopeType;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The Class SingletonScope.
 *
 * @since 2011. 3. 12.
 */
public final class SingletonScope extends AbstractScope {

    private static final ScopeType scopeType = ScopeType.SINGLETON;

    private final ReadWriteLock scopeLock = new ReentrantReadWriteLock();

    /**
     * Instantiates a new Singleton scope.
     */
    public SingletonScope() {
        super();
    }

    @Override
    public ScopeType getScopeType() {
        return scopeType;
    }

    @Override
    public ReadWriteLock getScopeLock() {
        return scopeLock;
    }

}
