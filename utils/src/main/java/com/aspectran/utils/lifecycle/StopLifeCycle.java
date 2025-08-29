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
package com.aspectran.utils.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link LifeCycle} implementation that, when started, will stop another specified {@link LifeCycle}.
 * <p>This class acts as a listener to its own lifecycle. When this {@code StopLifeCycle} instance
 * transitions to the {@link LifeCycle#STARTED} state, it triggers the {@link LifeCycle#stop()} method
 * on the {@code LifeCycle} component it wraps.</p>
 */
public class StopLifeCycle extends AbstractLifeCycle implements LifeCycle.Listener {

    private static final Logger logger = LoggerFactory.getLogger(StopLifeCycle.class);

    private final LifeCycle lifecycle;

    /**
     * Creates a new StopLifeCycle instance that will stop the given lifecycle component.
     * @param lifecycle the {@link LifeCycle} component to stop when this instance starts
     */
    public StopLifeCycle(LifeCycle lifecycle) {
        this.lifecycle = lifecycle;
        addLifeCycleListener(this);
    }

    /**
     * Callback method invoked when this {@code StopLifeCycle} instance has started.
     * <p>Upon receiving this event, it attempts to stop the wrapped {@link LifeCycle} component.</p>
     * @param lifecycle the {@link LifeCycle} instance that has started (which is this object itself)
     */
    @Override
    public void lifeCycleStarted(LifeCycle lifecycle) {
        try {
            this.lifecycle.stop();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() throws Exception {
    }

}
