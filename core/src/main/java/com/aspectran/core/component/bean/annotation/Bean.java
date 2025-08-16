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
 * Stereotype annotation indicating that the annotated type or method
 * contributes a bean to the Aspectran bean registry.
 * <p>
 * May be placed on a concrete class to register it as a bean or on a method
 * in a configuration class to declare a factory method. The attributes allow
 * configuring id, lifecycle callbacks, and lazy behavior.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Bean {

    /** Suggested bean id or name. */
    String value() default "";

    /** Explicit bean id to register under (takes precedence if set). */
    String id() default "";

    /** Name of an initialization method to invoke after dependency injection. */
    String initMethod() default "";

    /** Name of a destruction method to invoke on container shutdown. */
    String destroyMethod() default "";

    /** Whether to lazily initialize this bean on first use instead of at startup. */
    boolean lazyInit() default false;

    /** Whether to lazily invoke the destroy method rather than eagerly on shutdown. */
    boolean lazyDestroy() default false;

    /** Marks the bean as important for lifecycle ordering or startup. */
    boolean important() default false;

}
