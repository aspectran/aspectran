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
 * Declares a scheduled job configuration at the type or method level.
 * Supports simple and cron triggers and optional scheduler selection.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Schedule {

    /** Unique identifier for this schedule. */
    String id();

    /** Optional scheduler bean id to use. */
    String scheduler() default "";

    /** Simple trigger configuration. */
    SimpleTrigger simpleTrigger() default @SimpleTrigger;

    /** Cron trigger configuration. */
    CronTrigger cronTrigger() default @CronTrigger(expression = "");

    /** Jobs to be executed by this schedule. */
    Job[] jobs() default {};

}
