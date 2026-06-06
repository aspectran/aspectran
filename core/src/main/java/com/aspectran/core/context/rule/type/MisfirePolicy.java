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
package com.aspectran.core.context.rule.type;

import org.jspecify.annotations.Nullable;

/**
 * Enumeration of misfire policies for triggers.
 */
public enum MisfirePolicy {

    SMART_POLICY("smartPolicy"),
    IGNORE_MISFIRES("ignoreMisfires"),
    FIRE_NOW("fireNow"),
    RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT("rescheduleNowWithExistingRepeatCount"),
    RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT("rescheduleNowWithRemainingRepeatCount"),
    RESCHEDULE_NEXT_WITH_REMAINING_COUNT("rescheduleNextWithRemainingCount"),
    RESCHEDULE_NEXT_WITH_EXISTING_COUNT("rescheduleNextWithExistingCount"),
    FIRE_ONCE_NOW("fireOnceNow"),
    DO_NOTHING("doNothing");

    private final String alias;

    MisfirePolicy(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns a {@code MisfirePolicy} with a value represented
     * by the specified {@code String}.
     * @param alias the misfire policy as a {@code String}
     * @return a {@code MisfirePolicy}, may be {@code null}
     */
    @Nullable
    public static MisfirePolicy resolve(String alias) {
        if (alias != null) {
            for (MisfirePolicy type : values()) {
                if (type.alias.equalsIgnoreCase(alias)) {
                    return type;
                }
            }
        }
        return null;
    }

}
