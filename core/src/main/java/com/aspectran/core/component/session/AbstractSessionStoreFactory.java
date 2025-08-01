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
package com.aspectran.core.component.session;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;

import static com.aspectran.core.component.session.AbstractSessionStore.DEFAULT_GRACE_PERIOD_SECS;
import static com.aspectran.core.component.session.AbstractSessionStore.DEFAULT_SAVE_PERIOD_SECS;

/**
 * Abstract Implementation for SessionStoreFactory.
 *
 * <p>Created: 2019/12/07</p>
 */
public abstract class AbstractSessionStoreFactory implements SessionStoreFactory, ApplicationAdapterAware {

    private ApplicationAdapter applicationAdapter;

    private int gracePeriodSecs = DEFAULT_GRACE_PERIOD_SECS;

    private int savePeriodSecs = DEFAULT_SAVE_PERIOD_SECS; // time in seconds between saves

    private String[] nonPersistentAttributes;

    public ApplicationAdapter getApplicationAdapter() {
        return applicationAdapter;
    }

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    public int getGracePeriodSecs() {
        return gracePeriodSecs;
    }

    public void setGracePeriodSecs(int gracePeriodSecs) {
        this.gracePeriodSecs = gracePeriodSecs;
    }

    public int getSavePeriodSecs() {
        return savePeriodSecs;
    }

    public void setSavePeriodSecs(int savePeriodSecs) {
        this.savePeriodSecs = savePeriodSecs;
    }

    @Override
    public String[] getNonPersistentAttributes() {
        return nonPersistentAttributes;
    }

    @Override
    public void setNonPersistentAttributes(String[] nonPersistentAttributes) {
        this.nonPersistentAttributes = nonPersistentAttributes;
    }

}
