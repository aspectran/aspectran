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

import com.aspectran.core.context.rule.type.FormatType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares output transformation for an action/request method.
 * Supports choosing a format (e.g., JSON, XML), content type, template,
 * character encoding, and pretty-printing.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Transform {

    /** Preferred output format. */
    FormatType value() default FormatType.NONE;

    /** Alias of {@link #value()} for clarity in some usages. */
    FormatType format() default FormatType.NONE;

    /** Response content type to set. */
    String contentType() default "";

    /** Template identifier or path for rendering. */
    String template() default "";

    /** Output character encoding (e.g., UTF-8). */
    String encoding() default "";

    /** Whether to pretty-print the output when applicable. */
    boolean pretty() default false;

}
