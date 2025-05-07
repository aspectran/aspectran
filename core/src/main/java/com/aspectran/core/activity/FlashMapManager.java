/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.core.activity;

import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * A strategy interface for retrieving and saving FlashMap instances.
 * See {@link FlashMap} for a general overview of flash attributes.
 * @see FlashMap
 * @since 8.4.0
 */
public interface FlashMapManager {

    /** Well-known name for the FlashMapManager object in the bean factory for this namespace. */
    String FLASH_MAP_MANAGER_BEAN_ID = "flashMapManager";

    /**
     * Find a FlashMap saved by a previous request that matches to the current
     * request, remove it from underlying storage, and also remove other
     * expired FlashMap instances.
     * <p>This method is invoked in the beginning of every request in contrast
     * to {@link #saveFlashMap}, which is invoked only when there are
     * flash attributes to be saved - i.e. before a redirect.
     * @param translet the current translet
     * @return a FlashMap matching the current request or {@code null}
     */
    @Nullable
    FlashMap retrieveAndUpdate(Translet translet);

    /**
     * Save the given FlashMap, in some underlying storage and set the start
     * of its expiration period.
     * <p><strong>NOTE:</strong> Invoke this method prior to a redirect in order
     * to allow saving the FlashMap in the HTTP session or in a response
     * cookie before the response is committed.
     * @param translet the current translet
     */
    void saveFlashMap(Translet translet);

}
