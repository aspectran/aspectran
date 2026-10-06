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
 * Maps PATCH requests to the annotated action method as a translet.
 * Supports specifying translet name/path mapping and async execution options.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestToPatch {

    /** The translet name or request path (alias for {@link #translet()}). */
    String value() default "";

    /** The translet name or request path (alias for {@link #value()}). */
    String translet() default "";

    /** Whether to execute asynchronously. */
    boolean async() default false;

    /** Timeout in milliseconds for async execution (-1 for no timeout). */
    long timeout() default -1L;

}
