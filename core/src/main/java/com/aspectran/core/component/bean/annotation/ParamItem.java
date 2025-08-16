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
 * Declares a parameter item for an action/request method.
 * <p>
 * Typically used with redirect/forward annotations to provide name/value
 * pairs, with optional tokenization, secrecy, and mandatory flags.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ParamItem.List.class)
public @interface ParamItem {

    /** Optional profile under which this parameter applies. */
    String profile() default "";

    /** The parameter name. */
    String name() default "";

    /** The parameter value. */
    String value() default "";

    /** Whether to tokenize the value for expression/property resolution. */
    boolean tokenize() default true;

    /** Whether this parameter is mandatory. */
    boolean mandatory() default false;

    /** Whether the value is secret (e.g., mask in logs). */
    boolean secret() default false;

    /**
     * Container annotation for repeatable {@link ParamItem} declarations.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface List {

        /** The contained parameter items. */
        ParamItem[] value();

    }

}
