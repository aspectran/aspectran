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
package com.aspectran.core.service;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.resource.AspectranClassLoader;

/**
 * The Interface CoreService.
 */
public interface CoreService extends ServiceController {

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
     * Returns the Aspectran configuration parameters used to generate the AspectranService.
     *
     * @return the Aspectran Configuration Parameters
     */
    AspectranConfig getAspectranConfig();

    /**
     * Returns whether to reload both the Java class and the activity context.
     *
     * @return true if the Java classes and the activity context are reloaded both;
     *      false if only the activity context is reloaded
     */
    boolean isHardReload();

}
