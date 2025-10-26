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
package com.aspectran.core.component;

/**
 * Defines the contract for a component with a managed lifecycle.
 *
 * <p>This interface provides methods to initialize and destroy a component,
 * as well as check its current lifecycle state.
 * </p>
 *
 * <p>Created: 2017. 7. 4.</p>
 */
public interface Component {

    /**
     * Initializes the component.
     * @throws Exception if the component fails to initialize
     */
    void initialize() throws Exception;

    /**
     * Destroys the component, releasing any resources.
     */
    void destroy();

    /**
     * Returns whether the component is currently available for use.
     * A component is available if it has been initialized and is not destroyed.
     * @return true if the component is available, false otherwise
     */
    boolean isAvailable();

    /**
     * Returns whether the component is in a state where it can be initialized.
     * @return true if the component is ready to be initialized, false otherwise
     */
    boolean isInitializable();

    /**
     * Returns whether the component is currently in the process of initializing.
     * @return true if the component is initializing, false otherwise
     */
    boolean isInitializing();

    /**
     * Returns whether the component has been successfully initialized.
     * @return true if the component is initialized, false otherwise
     */
    boolean isInitialized();

    /**
     * Returns whether the component is currently in the process of being destroyed.
     * @return true if the component is being destroyed, false otherwise
     */
    boolean isDestroying();

    /**
     * Returns whether the component has been destroyed.
     * @return true if the component is destroyed, false otherwise
     */
    boolean isDestroyed();

    /**
     * Checks if the component is in a state where it can be initialized.
     * @throws IllegalStateException if the component is not in an initializable state
     */
    void checkInitializable();

    /**
     * Returns a descriptive name for the component, typically its class name and identity hash code.
     * @return the component name
     */
    String getComponentName();

}
