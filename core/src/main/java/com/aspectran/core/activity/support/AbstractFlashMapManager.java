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
import com.aspectran.core.activity.FlashMapManager;
import com.aspectran.core.activity.Translet;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A base class for {@link FlashMapManager} implementations.
 * @since 8.4.0
 */
public abstract class AbstractFlashMapManager implements FlashMapManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Object DEFAULT_FLASH_MAPS_MUTEX = new Object();

    private int flashMapTimeout = 180;

    /**
     * Set the amount of time in seconds after a {@link FlashMap} is saved
     * (at request completion) and before it expires.
     * <p>The default value is 180 seconds.
     */
    public void setFlashMapTimeout(int flashMapTimeout) {
        this.flashMapTimeout = flashMapTimeout;
    }

    /**
     * Return the amount of time in seconds before a FlashMap expires.
     */
    public int getFlashMapTimeout() {
        return this.flashMapTimeout;
    }

    @Override
    @Nullable
    public final FlashMap retrieveAndUpdate(Translet translet) {
        List<FlashMap> allFlashMaps = retrieveFlashMaps(translet);
        if (allFlashMaps == null || allFlashMaps.isEmpty()) {
            return null;
        }

        List<FlashMap> mapsToRemove = getExpiredFlashMaps(allFlashMaps);
        FlashMap match = getMatchingFlashMap(allFlashMaps, translet);
        if (match != null) {
            mapsToRemove.add(match);
        }

        if (!mapsToRemove.isEmpty()) {
            Object mutex = getFlashMapsMutex(translet);
            if (mutex != null) {
                synchronized (mutex) {
                    allFlashMaps = retrieveFlashMaps(translet);
                    if (allFlashMaps != null) {
                        allFlashMaps.removeAll(mapsToRemove);
                        updateFlashMaps(allFlashMaps, translet);
                    }
                }
            } else {
                allFlashMaps.removeAll(mapsToRemove);
                updateFlashMaps(allFlashMaps, translet);
            }
        }

        return match;
    }

    /**
     * Return a list of expired FlashMap instances contained in the given list.
     */
    @NonNull
    private List<FlashMap> getExpiredFlashMaps(@NonNull List<FlashMap> allMaps) {
        List<FlashMap> result = new ArrayList<>();
        for (FlashMap map : allMaps) {
            if (map.isExpired()) {
                result.add(map);
            }
        }
        return result;
    }

    /**
     * Return a FlashMap contained in the given list that matches the request.
     * @return a matching FlashMap or {@code null}
     */
    @Nullable
    private FlashMap getMatchingFlashMap(@NonNull List<FlashMap> allMaps, Translet translet) {
        List<FlashMap> result = new ArrayList<>();
        for (FlashMap flashMap : allMaps) {
            if (isFlashMapForRequest(flashMap, translet)) {
                result.add(flashMap);
            }
        }
        if (!result.isEmpty()) {
            Collections.sort(result);
            if (logger.isTraceEnabled()) {
                logger.trace("Found {}", result.getFirst());
            }
            return result.getFirst();
        }
        return null;
    }

    /**
     * Whether the given FlashMap matches the current request.
     * Uses the expected request path and query parameters saved in the FlashMap.
     */
    protected boolean isFlashMapForRequest(@NonNull FlashMap flashMap, Translet translet) {
        String expectedPath = flashMap.getTargetRequestName();
        if (expectedPath != null) {
            String requestName = translet.getRequestName();
            return expectedPath.equals(requestName);
        }
        return true;
    }

    @Override
    public final void saveFlashMap(@NonNull Translet translet) {
        if (!translet.hasOutputFlashMap()) {
            return;
        }

        FlashMap flashMap = translet.getOutputFlashMap();
        if (StringUtils.isEmpty(flashMap.getTargetRequestName())) {
            flashMap.setTargetRequestName(translet.getRequestName());
        }
        flashMap.startExpirationPeriod(getFlashMapTimeout());

        Object mutex = getFlashMapsMutex(translet);
        if (mutex != null) {
            synchronized (mutex) {
                List<FlashMap> allFlashMaps = retrieveFlashMaps(translet);
                if (allFlashMaps == null) {
                    allFlashMaps = new CopyOnWriteArrayList<>();
                }
                allFlashMaps.add(flashMap);
                updateFlashMaps(allFlashMaps, translet);
            }
        } else {
            List<FlashMap> allFlashMaps = retrieveFlashMaps(translet);
            if (allFlashMaps == null) {
                allFlashMaps = new ArrayList<>(1);
            }
            allFlashMaps.add(flashMap);
            updateFlashMaps(allFlashMaps, translet);
        }
    }

    /**
     * Retrieve saved FlashMap instances from the underlying storage.
     * @param translet the current translet
     * @return a List with FlashMap instances, or {@code null} if none found
     */
    @Nullable
    protected abstract List<FlashMap> retrieveFlashMaps(Translet translet);

    /**
     * Update the FlashMap instances in the underlying storage.
     * @param flashMaps a (potentially empty) list of FlashMap instances to save
     * @param translet the current translet
     */
    protected abstract void updateFlashMaps(List<FlashMap> flashMaps, Translet translet);

    /**
     * Obtain a mutex for modifying the FlashMap List as handled by
     * {@link #retrieveFlashMaps} and {@link #updateFlashMaps},
     * <p>The default implementation returns a shared static mutex.
     * Subclasses are encouraged to return a more specific mutex, or
     * {@code null} to indicate that no synchronization is necessary.
     * @param translet the current translet
     * @return the mutex to use (may be {@code null} if none applicable)
     * @since 4.0.3
     */
    @Nullable
    protected Object getFlashMapsMutex(Translet translet) {
        return DEFAULT_FLASH_MAPS_MUTEX;
    }

}
