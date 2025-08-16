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
 * Marks a class as an Aspectran aspect containing advice and/or join point definitions.
 * The aspect may be ordered and optionally isolated from other aspects.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Aspect {

    /** Optional aspect name. */
    String value() default "";

    /** Explicit identifier for the aspect. */
    String id() default "";

    /** Ordering of this aspect relative to others (lower values have higher precedence). */
    int order() default Integer.MAX_VALUE;

    /** Whether this aspect should be applied in isolation from other aspects. */
    boolean isolated() default false;

}
