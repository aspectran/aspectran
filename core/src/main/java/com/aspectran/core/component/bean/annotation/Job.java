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
 * Defines a single job to be executed as part of a {@link Schedule}.
 * <p>This annotation is used within the {@code jobs} array of a {@code @Schedule} annotation
 * to specify *what* to run (a translet) and whether it is currently active. The execution
 * time is determined by the trigger defined in the parent {@code @Schedule}.</p>
 *
 * <p>Example:</p>
 * <pre>
 * {@code
 * @Schedule(
 *     // ...
 *     jobs = {
 *         @Job(translet = "/batch/task1"),
 *         @Job(translet = "/batch/task2", disabled = true)
 *     }
 * )
 * }
 * </pre>
 *
 * @see Schedule
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Job {

    /**
     * Alias for {@link #translet()}.
     * <p>Allows for a more concise annotation declaration, e.g., {@code @Job("/my/translet")}.</p>
     * @return the name of the translet to execute
     */
    String value() default "";

    /**
     * The name of the translet to be executed when the schedule's trigger fires.
     * This is the primary link between the scheduler and the action to be performed.
     * @return the name of the translet
     */
    String translet() default "";

    /**
     * Specifies whether this job is disabled.
     * <p>If set to {@code true}, this job will be skipped even when the schedule's
     * trigger fires. This is useful for temporarily deactivating a specific job
     * without removing it from the configuration.</p>
     * @return {@code true} if the job is disabled, {@code false} otherwise
     */
    boolean disabled() default false;

}
