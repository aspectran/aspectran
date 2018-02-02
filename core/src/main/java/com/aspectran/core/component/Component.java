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

/**
 * The lifecycle interface for generic components.
 *
 * <p>Created: 2017. 7. 4.</p>
 */
public interface Component {

    /**
     * Initialize the component.
     *
     * @throws Exception if the component fails to initialize
     */
    void initialize() throws Exception;

    /**
     * Destroy the component.
     */
    void destroy();

    /**
     * Returns whether the component is currently available.
     *
     * @return if true, the component is currently available
     */
    boolean isAvailable();

    /**
     * Returns whether or not the component has been initialized.
     *
     * @return ㅑf true, the component has been initialized
     */
    boolean isInitialized();

    /**
     * Returns whether or not the component has been destroyed.
     *
     * @return ㅑf true, the component has been destroyed
     */
    boolean isDestroyed();

    /**
     * Returns the component name.
     *
     * @return the component name
     */
    String getComponentName();

}
