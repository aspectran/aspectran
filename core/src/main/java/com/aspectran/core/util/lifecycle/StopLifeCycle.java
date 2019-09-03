/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.util.lifecycle;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * A LifeCycle that when started will stop another LifeCycle.
 */
public class StopLifeCycle extends AbstractLifeCycle implements LifeCycle.Listener {

    private static final Log log = LogFactory.getLog(StopLifeCycle.class);

    private final LifeCycle lifecycle;

    public StopLifeCycle(LifeCycle lifecycle) {
        this.lifecycle = lifecycle;
        addLifeCycleListener(this);
    }

    @Override
    public void lifeCycleStarting(LifeCycle lifecycle) {
    }

    @Override
    public void lifeCycleStarted(LifeCycle lifecycle) {
        try {
            this.lifecycle.stop();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    @Override
    public void lifeCycleFailure(LifeCycle lifecycle, Throwable cause) {
    }

    @Override
    public void lifeCycleStopping(LifeCycle lifecycle) {
    }

    @Override
    public void lifeCycleStopped(LifeCycle lifecycle) {
    }

}