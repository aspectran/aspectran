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
package com.aspectran.core.component;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * <p>Created: 2017. 7. 4.</p>
 */
public abstract class AbstractComponent implements Component {

    private final Log log = LogFactory.getLog(getClass());

    private final Object lock = new Object();

    private volatile boolean initialized;

    private volatile boolean destroyed;

    protected abstract void doInitialize() throws Exception;

    protected abstract void doDestroy() throws Exception;

    @Override
    public void initialize() throws Exception {
        synchronized (lock) {
            if (destroyed) {
                throw new IllegalStateException("Already destroyed " + getComponentName());
            }
            if (initialized) {
                throw new IllegalStateException("Already initialized " + getComponentName());
            }

            doInitialize();

            log.info("Initialized " + getComponentName());

            initialized = true;
        }
    }

    @Override
    public void destroy() {
        synchronized (lock) {
            if (!initialized) {
                throw new IllegalStateException("Not yet initialized " + getComponentName());
            }
            if (destroyed) {
                throw new IllegalStateException("Already destroyed " + getComponentName());
            }

            try {
                doDestroy();
                log.info("Destroyed " + getComponentName());
            } catch (Exception e) {
                log.warn("Failed to destroy " + getComponentName(), e);
            }

            destroyed = true;
        }
    }

    @Override
    public boolean isAvailable() {
        synchronized (lock) {
            return (initialized && !destroyed);
        }
    }

    @Override
    public boolean isInitialized() {
        synchronized (lock) {
            return initialized;
        }
    }

    @Override
    public boolean isDestroyed() {
        synchronized (lock) {
            return destroyed;
        }
    }

    @Override
    public String getComponentName() {
        return getClass().getSimpleName() + '@' + Integer.toString(hashCode(), 16);
    }

}
