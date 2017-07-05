/*
 * Copyright 2008-2017 Juho Jeong
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

import java.util.concurrent.atomic.AtomicBoolean;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * <p>Created: 2017. 7. 4.</p>
 */
public abstract class AbstractComponent implements Component {

    private final Log log = LogFactory.getLog(getClass());

    private final Object lock = new Object();

    private volatile boolean initializing;

    private volatile boolean initialized;

    private volatile boolean destroying;

    private volatile boolean destroyed;

    protected abstract void doInitialize() throws Exception;

    protected abstract void doDestroy() throws Exception;

    @Override
    public void initialize() throws Exception {
        synchronized (lock) {
            if (destroyed) {
                throw new IllegalStateException("Already destroyed component " + this);
            }
            if (initialized) {
                throw new IllegalStateException("Already initialized component " + this);
            }

            initializing = true;

            doInitialize();

            initializing = false;
            initialized = true;

            log.info("Initialized " + this.getClass().getSimpleName());
        }
    }

    @Override
    public void destroy() {
        synchronized (lock) {
            if (destroyed) {
                throw new IllegalStateException("Already destroyed " + this);
            }
            if (!initialized) {
                throw new IllegalStateException("Not initialized " + this);
            }

            destroying = true;

            try {
                doDestroy();
            } catch (Exception e) {
                log.warn("Failed to destroy " + this, e);
            }

            destroying = false;
            destroyed = true;

            log.info("Destroyed " + this.getClass().getSimpleName());
        }
    }

    public boolean isRunning() {
        return (initializing || initialized);
    }

    public boolean isAvailable() {
        return (initialized && !destroying);
    }

    public boolean isInitialized() {
        return (initialized);
    }

    public boolean isDestroyed() {
        return destroyed;
    }

}
