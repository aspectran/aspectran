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
package com.aspectran.core.service;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.loader.config.AspectranConfig;
import com.aspectran.core.context.loader.resource.AspectranClassLoader;

/**
 * The Interface AspectranService.
 */
public interface AspectranService extends AspectranServiceController {

    /**
     * Gets the application adapter.
     *
     * @return the application adapter
     */
    ApplicationAdapter getApplicationAdapter();

    /**
     * Gets the activity context.
     *
     * @return the activity context
     */
    ActivityContext getActivityContext();

    /**
     * Gets the aspectran class loader.
     *
     * @return the aspectran class loader
     */
    AspectranClassLoader getAspectranClassLoader();

    /**
     * Returns the Aspectran configuration parameters used to generate the AspectranContext.
     *
     * @return the Aspectran Configuration Parameters
     */
    AspectranConfig getAspectranConfig();

    /**
     * Sets the aspectran service life-cycle listener.
     *
     * @param aspectranServiceLifeCycleListener the new aspectran service life-cycle listener
     */
    void setAspectranServiceLifeCycleListener(AspectranServiceLifeCycleListener aspectranServiceLifeCycleListener);

    /**
     * Returns whether or not the java classes to be reload when the activity context is reloading.
     * you reload Java classes and activitiesyou reload Java classes and activitiesreload only the activity context.
     *
     * @return true, if the java classes and activity context is to be reload both
     */
    boolean isHardReload();

    /**
     * Returns whether this AspectranService is currently active.
     *
     * @return whether the AspectranService is still active
     */
    boolean isActive();

    /**
     * Returns the aspectran service controller.
     *
     * @return the aspectran service controller
     */
    AspectranServiceController getAspectranServiceController();

}
