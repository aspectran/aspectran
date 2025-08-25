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
 * Configuration for a cron-based trigger using a cron expression.
 * <p>This annotation is used within the {@code @Schedule} annotation to define
 * a precise, calendar-like schedule for job execution. It relies on a standard
 * Cron expression to specify the firing times.</p>
 *
 * <p>Example:</p>
 * <pre>
 * {@code
 * @Schedule(
 *     id = "dailyReportSchedule",
 *     cronTrigger = @CronTrigger(expression = "0 0 2 * * ?"), // Every day at 2 AM
 *     jobs = { @Job(translet = "/batch/generateReport") }
 * )
 * public class DailyReportScheduler { /* ... * / }
 * }
 * </pre>
 *
 * @see Schedule
 * @see SimpleTrigger
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CronTrigger {

    /**
     * The Cron expression that defines the schedule.
     * <p>This is a string consisting of 6 or 7 fields separated by white space,
     * representing seconds, minutes, hours, day of month, month, day of week, and optionally year.</p>
     * <p>For detailed syntax, refer to Quartz CronTrigger documentation.</p>
     * @return the cron expression string
     */
    String expression();

}
