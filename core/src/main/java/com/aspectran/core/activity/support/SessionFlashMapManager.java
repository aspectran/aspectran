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
package com.aspectran.core.activity.support;

import com.aspectran.core.activity.FlashMap;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.List;

/**
 * Store and retrieve {@link FlashMap} instances to and from the HTTP session.
 * @since 8.4.0
 */
public class SessionFlashMapManager extends AbstractFlashMapManager {

    private static final String FLASH_MAPS_SESSION_ATTRIBUTE = SessionFlashMapManager.class.getName() + ".FLASH_MAPS";

    /**
     * Retrieves saved FlashMap instances from the HTTP session, if any.
     */
    @Override
    @Nullable
    protected List<FlashMap> retrieveFlashMaps(@NonNull Translet translet) {
        if (translet.hasSessionAdapter()) {
            SessionAdapter sessionAdapter = translet.getSessionAdapter();
            return sessionAdapter.getAttribute(FLASH_MAPS_SESSION_ATTRIBUTE);
        } else {
            return null;
        }
    }

    /**
     * Saves the given FlashMap instances in the HTTP session.
     */
    @Override
    protected void updateFlashMaps(List<FlashMap> flashMaps, @NonNull Translet translet) {
        SessionAdapter sessionAdapter = translet.getSessionAdapter();
        sessionAdapter.setAttribute(FLASH_MAPS_SESSION_ATTRIBUTE,
                (flashMaps != null && !flashMaps.isEmpty() ? flashMaps : null));
    }

    /**
     * Exposes the best available session mutex.
     */
    @Override
    protected Object getFlashMapsMutex(@NonNull Translet translet) {
        SessionAdapter sessionAdapter = translet.getSessionAdapter();
        return sessionAdapter.getAdaptee();
    }

}
