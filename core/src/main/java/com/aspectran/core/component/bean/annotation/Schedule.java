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
 * Declares a scheduled job configuration at the type (class) or method level.
 * <p>This annotation defines a group of jobs that share a single trigger configuration.
 * It specifies which scheduler bean to use, when the jobs will run (via a simple or cron trigger),
 * and which translets ({@link Job}s) are to be executed.</p>
 *
 * <p>Example Usage:</p>
 * <pre>
 * {@code
 * @Component
 * @Schedule(
 *     id = "myDailySchedule",
 *     scheduler = "myQuartzScheduler",
 *     cronTrigger = @CronTrigger(expression = "0 0 2 * * ?"), // Every day at 2 AM
 *     jobs = {
 *         @Job(translet = "/batch/dailyReport"),
 *         @Job(translet = "/batch/cleanupLogs")
 *     }
 * )
 * public class DailyTasks {
 *     // ... methods that define the translets ...
 * }
 * }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Schedule {

    /**
     * A unique identifier for this schedule group.
     * This ID is used to reference and manage the schedule.
     * @return the unique ID of the schedule
     */
    String id();

    /**
     * The ID of the scheduler bean to use for this schedule.
     * If not specified, the default scheduler bean configured in {@code <settings>}
     * (e.g., via {@code defaultSchedulerBean}) will be used.
     * @return the scheduler bean ID, or an empty string for the default
     */
    String scheduler() default "";

    /**
     * Configuration for a simple, fixed-interval trigger.
     * Only one of {@code simpleTrigger} or {@code cronTrigger} should be specified.
     * @return the simple trigger configuration
     */
    SimpleTrigger simpleTrigger() default @SimpleTrigger;

    /**
     * Configuration for a cron-based trigger.
     * Only one of {@code simpleTrigger} or {@code cronTrigger} should be specified.
     * @return the cron trigger configuration
     */
    CronTrigger cronTrigger() default @CronTrigger(expression = "");

    /**
     * An array of {@link Job} annotations, each defining a translet to be executed
     * when this schedule's trigger fires.
     * @return an array of jobs to be executed
     */
    Job[] jobs() default {};

}
