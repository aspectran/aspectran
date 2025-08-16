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
 * Use one of the interval properties to specify the period
 * and optionally a start delay, repeat count, or repeat forever.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface SimpleTrigger {

    /** Delay in seconds before the first execution. */
    int startDelaySeconds() default 0;

    /** Interval between executions in milliseconds. */
    long intervalInMilliseconds() default 0L;

    /** Interval between executions in seconds. */
    int intervalInSeconds() default 0;

    /** Interval between executions in minutes. */
    int intervalInMinutes() default 0;

    /** Interval between executions in hours. */
    int intervalInHours() default 0;

    /** Number of times to repeat the job after the first run. */
    int repeatCount() default 0;

    /** Whether to repeat the job indefinitely. */
    boolean repeatForever() default false;

}
