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
package com.aspectran.core.component.bean.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configuration for a simple, fixed-interval trigger.
 * <p>This annotation is used within the {@code @Schedule} annotation to define
 * a schedule that repeats at a specified interval, either a fixed number of times
 * or indefinitely. It is suitable for tasks that need to run periodically
 * without complex calendar-based scheduling.</p>
 *
 * <p>Example:</p>
 * <pre>
 * {@code
 * @Schedule(
 *     id = "heartbeatSchedule",
 *     simpleTrigger = @SimpleTrigger(
 *         startDelaySeconds = 5,      // Start 5 seconds after scheduler starts
 *         intervalInMinutes = 1,      // Repeat every 1 minute
 *         repeatForever = true        // Repeat indefinitely
 *     ),
 *     jobs = { @Job(translet = "/system/checkStatus") }
 * )
 * public class SystemMonitorScheduler { /* ... * / }
 * }
 * </pre>
 *
 * @see Schedule
 * @see CronTrigger
 * @since 3.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface SimpleTrigger {

    /**
     * Delay in seconds before the first execution of the job group after the scheduler starts.
     * @return the start delay in seconds (default is 0)
     */
    int startDelaySeconds() default 0;

    /**
     * Interval between executions in milliseconds.
     * Only one of the interval properties (milliseconds, seconds, minutes, hours) should be set.
     * @return the interval in milliseconds (default is 0)
     */
    long intervalInMilliseconds() default 0L;

    /**
     * Interval between executions in seconds.
     * Only one of the interval properties (milliseconds, seconds, minutes, hours) should be set.
     * @return the interval in seconds (default is 0)
     */
    int intervalInSeconds() default 0;

    /**
     * Interval between executions in minutes.
     * Only one of the interval properties (milliseconds, seconds, minutes, hours) should be set.
     * @return the interval in minutes (default is 0)
     */
    int intervalInMinutes() default 0;

    /**
     * Interval between executions in hours.
     * Only one of the interval properties (milliseconds, seconds, minutes, hours) should be set.
     * @return the interval in hours (default is 0)
     */
    int intervalInHours() default 0;

    /**
     * Number of times to repeat the job after the first run.
     * <p>For example, a {@code repeatCount} of 9 means the job will execute 10 times in total
     * (initial execution + 9 repetitions). A value of {@code -1} indicates infinite repetition.</p>
     * @return the repeat count (default is 0)
     */
    int repeatCount() default 0;

    /**
     * Whether to repeat the job indefinitely.
     * <p>Setting this to {@code true} is equivalent to setting {@code repeatCount} to {@code -1}.</p>
     * @return {@code true} if the job should repeat forever (default is false)
     */
    boolean repeatForever() default false;

}
