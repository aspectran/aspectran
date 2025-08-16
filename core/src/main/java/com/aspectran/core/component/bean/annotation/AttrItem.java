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
 * Declares an attribute item for a bean method, typically used to supply
 * named configuration values for invocation or metadata processing.
 * Can be repeated on the same method via {@link AttrItem.List}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(AttrItem.List.class)
public @interface AttrItem {

    /** Optional profile under which this attribute applies. */
    String profile() default "";

    /** The attribute name. */
    String name() default "";

    /** The attribute value (may be tokenized). */
    String value() default "";

    /** Whether to tokenize the value for expression/property resolution. */
    boolean tokenize() default true;

    /** Whether this attribute is mandatory. */
    boolean mandatory() default false;

    /** Whether the value is secret (e.g., mask in logs). */
    boolean secret() default false;

    /**
     * Container annotation for repeatable {@link AttrItem} declarations.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface List {

        /** The contained attribute items. */
        AttrItem[] value();

    }

}
