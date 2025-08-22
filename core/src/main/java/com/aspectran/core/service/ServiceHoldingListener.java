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
package com.aspectran.core.service;

/**
 * Listener interface for receiving events related to a {@link CoreService} being held or released
 * by the {@link CoreServiceHolder}.
 * <p>This allows external components to react when a service becomes globally available or is removed.
 *
 * @since 2025-02-13
 */
public interface ServiceHoldingListener {

    /**
     * Called after a {@link CoreService} has been successfully held by the {@link CoreServiceHolder}.
     * @param service the service that was held
     */
    default void afterServiceHolding(CoreService service) {
    }

    /**
     * Called before a {@link CoreService} is released from the {@link CoreServiceHolder}.
     * @param service the service that is about to be released
     */
    default void beforeServiceRelease(CoreService service) {
    }

}
