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

    private final Log log = LogFactory.getLog(AbstractComponent.class);

    private final AtomicBoolean initialized = new AtomicBoolean();

    private final AtomicBoolean destroyed = new AtomicBoolean();

    protected abstract void doInitialize() throws Exception;

    protected abstract void doDestroy() throws Exception;

    @Override
    public synchronized void initialize() throws Exception {
        if (this.destroyed.get()) {
            throw new IllegalStateException("Already destroyed component " + this);
        }
        if (this.initialized.get()) {
            throw new IllegalStateException("Already initialized component " + this);
        }

        doInitialize();

        log.info("Initialized component " + this);

        this.initialized.set(true);
    }

    @Override
    public synchronized void destroy() {
        if (this.initialized.get() && this.destroyed.compareAndSet(false, true)) {
            try {
                doDestroy();
                log.info("Destroyed component " + this);
            } catch (Exception e) {
                log.warn("Failed to destroy component " + this, e);
            }
        }
    }

    public boolean isInitialized() {
        return this.initialized.get();
    }

    public boolean isDestroyed() {
        return this.destroyed.get();
    }

}
