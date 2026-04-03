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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used to provide metadata hints for bean methods.
 * Hints are propagated through the activity context and can be consumed
 * by various modules (e.g., MyBatis, JPA, or custom aspects).
 *
 * <p>Created: 2026. 03. 26.</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(Hints.class)
public @interface Hint {

    /**
     * Returns the type of the hint.
     * <p>The type is used to identify and categorize the hint metadata
     * (e.g., "layout", "transactional", "cache").</p>
     * @return the hint type
     */
    String type() default "";

    /**
     * Returns the metadata for the hint in APON format.
     * <p>This string is parsed into a {@link com.aspectran.utils.apon.Parameters} object
     * at bean initialization time.</p>
     * @return the hint metadata in APON format
     */
    String value() default "";

    /**
     * Returns whether the hint should be propagated to child method calls.
     * <p>If {@code true} (default), the hint will be available for all subsequent
     * method calls until it is popped from the stack. If {@code false}, the
     * hint will only be available to the current method (and its aspects) and
     * will be hidden from inner calls.</p>
     * @return {@code true} if the hint should be propagated; {@code false} otherwise
     */
    boolean propagated() default true;

}
