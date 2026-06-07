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

    /**
     * Instructs the {@code Scheduler} that the {@code Trigger} should never be considered
     * to have misfired - that is, it will be fired as soon as it can be.
     * All misfired executions will be fired.
     */
    IGNORE_MISFIRES("ignoreMisfires"),

    /**
     * The default misfire policy. The behavior varies depending on the trigger type:
     * <ul>
     *   <li>CronTrigger: Same as {@link #FIRE_ONCE_NOW}.</li>
     *   <li>SimpleTrigger (Fixed Repeat): Same as {@link #RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT}.</li>
     *   <li>SimpleTrigger (Infinite Repeat): Same as {@link #RESCHEDULE_NEXT_WITH_REMAINING_COUNT}.</li>
     * </ul>
     */
    SMART_POLICY("smartPolicy"),

    /**
     * Instructs the {@code Scheduler} that the first misfired occurrence should be executed
     * as soon as possible, and any other misfired occurrences should be discarded.
     * Compatible with {@code CronTrigger}.
     */
    FIRE_ONCE_NOW("fireOnceNow"),

    /**
     * Instructs the {@code Scheduler} that all misfired occurrences should be discarded,
     * and the trigger should wait for the next scheduled fire time.
     * Compatible with {@code CronTrigger}.
     */
    DO_NOTHING("doNothing"),

    /**
     * Instructs the {@code Scheduler} that the first misfired occurrence should be executed
     * as soon as possible, and any other misfired occurrences should be discarded.
     * Compatible with {@code SimpleTrigger}.
     */
    FIRE_NOW("fireNow"),

    /**
     * Instructs the {@code Scheduler} that the {@code Trigger} should be rescheduled to fire
     * as soon as it can be, with its repeat count remaining unchanged.
     * Compatible with {@code SimpleTrigger}.
     */
    RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT("rescheduleNowWithExistingRepeatCount"),

    /**
     * Instructs the {@code Scheduler} that the {@code Trigger} should be rescheduled to fire
     * as soon as it can be, with the first misfired occurrence counting as one of its repeat
     * counts, and the remaining repeat count used for subsequent fire times.
     * Compatible with {@code SimpleTrigger}.
     */
    RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT("rescheduleNowWithRemainingRepeatCount"),

    /**
     * Instructs the {@code Scheduler} that the {@code Trigger} should be rescheduled to the
     * next scheduled fire time after the current time, with its repeat count remaining unchanged.
     * Compatible with {@code SimpleTrigger}.
     */
    RESCHEDULE_NEXT_WITH_EXISTING_COUNT("rescheduleNextWithExistingCount"),

    /**
     * Instructs the {@code Scheduler} that the {@code Trigger} should be rescheduled to the
     * next scheduled fire time after the current time, with its repeat count updated to reflect
     * the number of firings that were missed.
     * Compatible with {@code SimpleTrigger}.
     */
    RESCHEDULE_NEXT_WITH_REMAINING_COUNT("rescheduleNextWithRemainingCount");

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
