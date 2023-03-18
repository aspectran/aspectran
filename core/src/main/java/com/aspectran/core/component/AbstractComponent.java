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
package com.aspectran.core.component;

import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.core.util.thread.AutoLock;

/**
 * Abstract Implementation of {@link Component}.
 *
 * <p>Created: 2017. 7. 4.</p>
 */
public abstract class AbstractComponent implements Component {

    private static final Logger logger = LoggerFactory.getLogger(AbstractComponent.class);

    private final AutoLock lock = new AutoLock();

    private volatile boolean initialized;

    private volatile boolean destroying;

    private volatile boolean destroyed;

    protected abstract void doInitialize() throws Exception;

    protected abstract void doDestroy() throws Exception;

    @Override
    public void initialize() throws Exception {
        try (AutoLock ignored = lock.lock()) {
            if (destroyed) {
                throw new IllegalStateException("Already destroyed " + getComponentName());
            }
            if (initialized) {
                throw new IllegalStateException("Already initialized " + getComponentName());
            }

            doInitialize();
            initialized = true;

            if (logger.isDebugEnabled()) {
                logger.debug("Initialized " + getComponentName());
            }
        }
    }

    @Override
    public void destroy() {
        try (AutoLock ignored = lock.lock()) {
            if (!initialized) {
                throw new IllegalStateException("Not yet initialized " + getComponentName());
            }
            if (destroying || destroyed) {
                throw new IllegalStateException("Already destroyed " + getComponentName());
            }

            try {
                destroying = true;
                doDestroy();
                if (logger.isDebugEnabled()) {
                    logger.debug("Destroyed " + getComponentName());
                }
            } catch (Exception e) {
                logger.warn("Failed to destroy " + getComponentName(), e);
            } finally {
                destroying = false;
            }

            destroyed = true;
        }
    }

    @Override
    public boolean isAvailable() {
        return (initialized && !destroying && !destroyed);
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isDestroying() {
        return destroying;
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public String getComponentName() {
        return getClass().getSimpleName() + '@' + Integer.toString(hashCode(), 16);
    }

}
